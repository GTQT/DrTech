package com.drppp.drtech.common.MetaTileEntities.muti.electric.generator;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.unification.material.Materials;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

/**
 * 发电聚变堆核心逻辑：对应《发电聚变堆》设计文档"四、聚变堆启动流程"的状态机。
 * <p>
 * OFFLINE → MAGNETIZING(建磁) → FUEL_INJECTING(注入DT) → RF_HEATING(RF加热点火)
 * → IGNITED(α粒子自加热点火) → RUNNING(自持燃烧，输出电力)
 * <p>
 * 能量输出：每tick输出 = 第一壁上限 × 冷却剂倍率 + 中子捕获加成
 * 燃料消耗：DT消耗 = 基础消耗 × 氚增殖包层系数
 * 建磁消耗：启动前一次性累计扣除 EU（超导磁体档位决定总量）
 */
public class FusionReactorLogic {

    public enum State {
        OFFLINE, MAGNETIZING, FUEL_INJECTING, RF_HEATING, IGNITED, RUNNING
    }

    private static final int MAX_HEAT = 100_000;
    /** 点火后 α 自加热至完全燃烧所需额外热量 */
    private static final int IGNITION_HEAT = 100_000;
    /** 建磁充电速率上限 EU/t */
    private static final long MAGNETIZE_RATE = 16_000_000L;
    /** RF 加热功率上限 EU/t */
    private static final long RF_HEAT_RATE = 8_000_000L;
    /** 点火后自持阶段 RF 维持功率比例 */
    private static final double SUSTAIN_RF_RATIO = 0.25;
    /** 中子捕获加成基数：额外输出 = 基础输出 × 该比例 × 中子捕获效率 */
    private static final double NEUTRON_BONUS_BASE = 0.10;
    /** 基础燃料消耗（mB/t，氘+氚合计），随输出功率线性放大 */
    private static final double BASE_FUEL_PER_M_EU = 1.0D;

    /** 状态同步专用自定义数据码（避免与基类 WORKABLE_ACTIVE 冲突） */
    private static final int FUSION_STATE_DATA = GregtechDataCodes.assignId();

    private final MetaTileEntityFusionReactor host;

    private State state = State.OFFLINE;
    private long magnetizeStored = 0;
    private long magnetizeNeeded = 0;
    private int heatProgress = 0;
    private long rfPower = 0;
    private long outputEU = 0;
    private long coreOutput = 0;
    private int fuelConsumption = 0;
    private int injectTicks = 0;
    private static final int INJECT_TICKS = 100;

    public FusionReactorLogic(MetaTileEntityFusionReactor host) {
        this.host = host;
    }

    public State getState() {
        return state;
    }

    public long getOutputEU() {
        return outputEU;
    }

    public long getCoreOutput() {
        return coreOutput;
    }

    public int getHeatProgress() {
        return heatProgress;
    }

    public int getMaxHeat() {
        return MAX_HEAT + IGNITION_HEAT;
    }

    public long getRfPower() {
        return rfPower;
    }

    public long getMagnetizeStored() {
        return magnetizeStored;
    }

    public long getMagnetizeNeeded() {
        return magnetizeNeeded;
    }

    public int getFuelConsumption() {
        return fuelConsumption;
    }

    public int getInjectTicks() {
        return injectTicks;
    }

    /** 结构成型时由宿主调用：读取五类分级外壳档位并重算建磁需求 */
    public void onStructureFormed() {
        FusionCasingStats magnet = host.getMagnetStats();
        this.magnetizeNeeded = magnet == null ? 0 : magnet.getMagnetizeEU();
        this.state = State.MAGNETIZING;
    }

    /** 结构失效时由宿主调用 */
    public void onStructureInvalidated() {
        this.state = State.OFFLINE;
        this.magnetizeStored = 0;
        this.heatProgress = 0;
        this.rfPower = 0;
        this.outputEU = 0;
        this.coreOutput = 0;
        this.fuelConsumption = 0;
        this.injectTicks = 0;
        this.magnetizeNeeded = 0;
    }

    public void updateLogic() {
        if (!this.host.isWorkingEnabled() || !this.host.isStructureFormed()) {
            if (this.outputEU != 0) {
                this.outputEU = 0;
                this.host.markDirty();
            }
            return;
        }
        if (this.host.getWorld() == null || this.host.getWorld().isRemote) return;

        FusionCasingStats firstWall = this.host.getFirstWallStats();
        FusionCasingStats coolant = this.host.getCoolantStats();
        FusionCasingStats neutron = this.host.getNeutronStats();
        FusionCasingStats breeding = this.host.getBreedingStats();

        switch (this.state) {
            case MAGNETIZING:
                this.updateMagnetizing();
                break;
            case FUEL_INJECTING:
                this.updateFuelInjecting();
                break;
            case RF_HEATING:
                this.updateRfHeating(firstWall);
                break;
            case IGNITED:
                this.updateIgnited(firstWall);
                break;
            case RUNNING:
                this.updateRunning(firstWall, coolant, neutron, breeding);
                break;
            default:
                break;
        }
    }

    // ============ 状态机 ============

    /** 建磁：从输入能量仓抽取 EU，累计到超导磁体所需建磁量 */
    private void updateMagnetizing() {
        long drained = drainInput(Math.min(MAGNETIZE_RATE, this.magnetizeNeeded - this.magnetizeStored));
        this.magnetizeStored += drained;
        this.rfPower = drained;
        if (this.magnetizeStored >= this.magnetizeNeeded) {
            this.magnetizeStored = this.magnetizeNeeded;
            this.injectTicks = 0;
            this.state = State.FUEL_INJECTING;
        }
        this.host.markDirty();
    }

    /** 注入 DT 燃料：检查氘/氚流体是否充足，充足则注入一段进度后进入加热 */
    private void updateFuelInjecting() {
        if (hasFuel(1)) {
            this.injectTicks++;
            this.rfPower = 0;
            if (this.injectTicks >= INJECT_TICKS) {
                this.injectTicks = 0;
                this.state = State.RF_HEATING;
            }
        } else {
            this.injectTicks = Math.max(0, this.injectTicks - 1);
            this.rfPower = 0;
        }
        this.host.markDirty();
    }

    /** RF 加热点火：消耗输入 EU 作为 RF 功率，加热等离子体至劳森判据 */
    private void updateRfHeating(FusionCasingStats firstWall) {
        long power = drainInput(RF_HEAT_RATE);
        this.rfPower = power;
        long maxOutput = firstWall == null ? 4_000_000L : firstWall.getMaxOutput();
        // 加热速率随输入功率与第一壁档位提升；满功率约 25 秒点火（500 tick）
        long rate = Math.max(1L, (long) ((double) power / RF_HEAT_RATE * 100.0D * (1.0D + (double) maxOutput / 4_000_000.0D)));
        this.heatProgress = Math.min(MAX_HEAT, this.heatProgress + (int) rate);
        if (this.heatProgress >= MAX_HEAT) {
            this.state = State.IGNITED; // 达到劳森判据，α 粒子自加热开始
        }
        this.host.markDirty();
    }

    /** 点火：α 自加热，RF 需求大幅下降，温度继续爬升至完全燃烧 */
    private void updateIgnited(FusionCasingStats firstWall) {
        long sustain = (long) (RF_HEAT_RATE * SUSTAIN_RF_RATIO);
        long power = drainInput(sustain);
        this.rfPower = power;
        this.heatProgress = Math.min(MAX_HEAT + IGNITION_HEAT, this.heatProgress + 1000);
        if (this.heatProgress >= MAX_HEAT + IGNITION_HEAT) {
            this.state = State.RUNNING;
        }
        this.host.markDirty();
    }

    /** 自持燃烧：输出电力 = 第一壁上限 × 冷却剂倍率 + 中子捕获加成；消耗 DT 燃料 */
    private void updateRunning(FusionCasingStats firstWall, FusionCasingStats coolant,
                               FusionCasingStats neutron, FusionCasingStats breeding) {
        long base = firstWall == null ? 4_000_000L : firstWall.getMaxOutput();
        double mult = coolant == null ? 1.0D : coolant.getCoolantMultiplier();
        this.coreOutput = (long) ((double) base * mult);

        double nEff = neutron == null ? 0.45D : neutron.getNeutronEfficiency();
        long bonus = (long) ((double) this.coreOutput * NEUTRON_BONUS_BASE * nEff);
        this.outputEU = this.coreOutput + bonus;

        double consumeMult = breeding == null ? 0.90D : breeding.getFuelConsumptionMultiplier();
        int need = (int) Math.max(1, ((double) this.coreOutput / 1_000_000.0D) * BASE_FUEL_PER_M_EU * consumeMult);

        if (hasFuel(need)) {
            drainFuel(need);
            this.fuelConsumption = need;
            this.rfPower = 0;
            this.host.getOutEnergyContainer().addEnergy(this.outputEU);
        } else {
            // 燃料耗尽：退回注入阶段
            this.fuelConsumption = 0;
            this.outputEU = 0;
            this.injectTicks = 0;
            this.state = State.FUEL_INJECTING;
        }
        this.host.markDirty();
    }

    // ============ 辅助 ============

    private long drainInput(long max) {
        if (this.host.getEnergyContainer() == null || max <= 0) return 0;
        long stored = this.host.getEnergyContainer().getEnergyStored();
        long toDrain = Math.min(max, stored);
        if (toDrain > 0) {
            this.host.getEnergyContainer().changeEnergy(-toDrain);
        }
        return toDrain;
    }

    private boolean hasFuel(int amount) {
        IMultipleTankHandler tanks = this.host.getInputFluidInventory();
        if (tanks == null) return false;
        Fluid deuterium = Materials.Deuterium.getFluid();
        Fluid tritium = Materials.Tritium.getFluid();
        int d = 0;
        int t = 0;
        for (IMultipleTankHandler.ITankEntry entry : tanks.getFluidTanks()) {
            FluidStack fs = entry.getFluid();
            if (fs != null && fs.getFluid() == deuterium) d += fs.amount;
            else if (fs != null && fs.getFluid() == tritium) t += fs.amount;
        }
        return d >= amount && t >= amount;
    }

    private void drainFuel(int amount) {
        IMultipleTankHandler tanks = this.host.getInputFluidInventory();
        if (tanks == null) return;
        drainFromTanks(tanks, Materials.Deuterium.getFluid(), amount);
        drainFromTanks(tanks, Materials.Tritium.getFluid(), amount);
    }

    private static void drainFromTanks(IMultipleTankHandler tanks, Fluid fluid, int amount) {
        int remaining = amount;
        for (IMultipleTankHandler.ITankEntry entry : tanks.getFluidTanks()) {
            if (remaining <= 0) break;
            FluidStack fs = entry.getFluid();
            if (fs != null && fs.getFluid() == fluid) {
                FluidStack drained = entry.drain(new FluidStack(fluid, remaining), true);
                if (drained != null) remaining -= drained.amount;
            }
        }
    }

    public boolean isActive() {
        return this.state == State.RUNNING || this.state == State.IGNITED;
    }

    // ============ NBT / 网络同步 ============

    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        data.setInteger("state", this.state.ordinal());
        data.setLong("magnetizeStored", this.magnetizeStored);
        data.setLong("magnetizeNeeded", this.magnetizeNeeded);
        data.setInteger("heatProgress", this.heatProgress);
        data.setLong("rfPower", this.rfPower);
        data.setLong("outputEU", this.outputEU);
        data.setLong("coreOutput", this.coreOutput);
        data.setInteger("fuelConsumption", this.fuelConsumption);
        data.setInteger("injectTicks", this.injectTicks);
        return data;
    }

    public void readFromNBT(@Nonnull NBTTagCompound data) {
        this.state = State.values()[Math.max(0, Math.min(data.getInteger("state"), State.values().length - 1))];
        this.magnetizeStored = data.getLong("magnetizeStored");
        this.magnetizeNeeded = data.getLong("magnetizeNeeded");
        this.heatProgress = data.getInteger("heatProgress");
        this.rfPower = data.getLong("rfPower");
        this.outputEU = data.getLong("outputEU");
        this.coreOutput = data.getLong("coreOutput");
        this.fuelConsumption = data.getInteger("fuelConsumption");
        this.injectTicks = data.getInteger("injectTicks");
    }

    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        buf.writeInt(this.state.ordinal());
        buf.writeLong(this.magnetizeStored);
        buf.writeLong(this.magnetizeNeeded);
        buf.writeInt(this.heatProgress);
        buf.writeLong(this.rfPower);
        buf.writeLong(this.outputEU);
        buf.writeLong(this.coreOutput);
        buf.writeInt(this.fuelConsumption);
    }

    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        this.state = State.values()[buf.readInt()];
        this.magnetizeStored = buf.readLong();
        this.magnetizeNeeded = buf.readLong();
        this.heatProgress = buf.readInt();
        this.rfPower = buf.readLong();
        this.outputEU = buf.readLong();
        this.coreOutput = buf.readLong();
        this.fuelConsumption = buf.readInt();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == FUSION_STATE_DATA) {
            this.state = State.values()[buf.readInt()];
            host.scheduleRenderUpdate();
        }
    }

    /** 状态变化时同步到客户端 */
    public void syncState() {
        World world = this.host.getWorld();
        if (world != null && !world.isRemote) {
            this.host.writeCustomData(FUSION_STATE_DATA, buffer -> buffer.writeInt(this.state.ordinal()));
        }
    }
}
