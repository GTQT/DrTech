package com.meowmel.cropQT.api.capability.impl;

import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import com.meowmel.cropQT.api.registries.HydrationRegistry;
import com.meowmel.cropQT.machine.MetaTileEntityCropSupervisor;
import com.meowmel.cropQT.tile.TileCropStick;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * 作物监管机的干活逻辑：范围浇水、施肥、除草。
 *
 * <p>三件事各自独立开关，默认<b>只开浇水</b> —— 施肥和除草要玩家主动打开。
 * 尤其是施肥：液体/固体肥料都是要花资源做的，机器不该闷声吃光。
 *
 * <h2>能耗</h2>
 * 浇水一次和施肥一次各扣 {@code V[档位] / 32} EU。除草<b>免费</b> —— 它不清液体也不清物品，
 * 收个电费没道理。
 *
 * <h2>施肥优先吃固体</h2>
 * 先看输入槽里有没有登记的固体肥料（GT 肥料 / 骨粉），有就吃一个；
 * 没有才退回去抽液肥罐。这样玩家可以拿几组骨粉顶着用，不必先铺管道。
 *
 * @see MetaTileEntityCropSupervisor
 */
public class CropSupervisorLogic extends CropRangeLogic<MetaTileEntityCropSupervisor> {

    /** 每次浇水 / 施肥的耗电 = 本机电压 / 这个除数。 */
    private static final int AUX_ENERGY_DIVISOR = 32;

    /** 作物架的水位低于这个值就补水。 */
    private static final int WATER_THRESHOLD = 180;
    /** 作物架的肥位低于这个值就施肥。 */
    private static final int FERTILIZER_THRESHOLD = 180;

    private boolean waterEnabled = true;
    private boolean fertilizerEnabled = false;
    private boolean weedingEnabled = false;

    public CropSupervisorLogic(MetaTileEntityCropSupervisor machine) {
        super(machine);
    }

    @Override
    public boolean isAnyEnabled() {
        return waterEnabled || fertilizerEnabled || weedingEnabled;
    }

    // ==================== 开关 ====================

    public boolean isWaterEnabled() {
        return waterEnabled;
    }

    public void setWaterEnabled(boolean value) {
        this.waterEnabled = value;
        machine.markDirty();
    }

    public boolean isFertilizerEnabled() {
        return fertilizerEnabled;
    }

    public void setFertilizerEnabled(boolean value) {
        this.fertilizerEnabled = value;
        machine.markDirty();
    }

    public boolean isWeedingEnabled() {
        return weedingEnabled;
    }

    public void setWeedingEnabled(boolean value) {
        this.weedingEnabled = value;
        machine.markDirty();
    }

    // ==================== 存档 ====================

    public void writeToNBT(NBTTagCompound data) {
        data.setBoolean("water", waterEnabled);
        data.setBoolean("fertilize", fertilizerEnabled);
        data.setBoolean("weed", weedingEnabled);
        data.setInteger("progress", getProgress());
    }

    public void readFromNBT(NBTTagCompound data) {
        waterEnabled = data.getBoolean("water");
        fertilizerEnabled = data.getBoolean("fertilize");
        weedingEnabled = data.getBoolean("weed");
        setProgress(data.getInteger("progress"));
    }

    // ==================== 三个动作 ====================

    @Override
    protected void actOn(TileCropStick crop) {
        // 除草要在最前面：杂草也算「有作物」，先清掉就不必再判后面那两件事了
        if (weedingEnabled && crop.isWeedPlant()) {
            crop.destroyCrop();
            return;
        }
        // 杂草不是作物，别给它浇水施肥——旧版管理器就是这么把玩家的水肥喂给杂草的
        if (!crop.hasCrop() || crop.isWeedPlant()) {
            return;
        }
        if (waterEnabled) {
            tryWater(crop);
        }
        if (fertilizerEnabled) {
            tryFertilize(crop);
        }
    }

    private void tryWater(TileCropStick crop) {
        if (crop.getWaterStorage() > WATER_THRESHOLD) {
            return;
        }
        // 剩余空间要从架子上实时问——它是按脚下土壤算的，土壤换了值就变
        int room = crop.getMaxWater() - crop.getWaterStorage();
        FluidTank tank = machine.waterTank();
        if (room <= 0 || tank == null) {
            return;
        }
        FluidStack stored = tank.getFluid();
        int potency = stored == null ? 0 : HydrationRegistry.getWater(stored.getFluid());
        if (potency <= 0 || !consumeEnergy(energyPerAction(AUX_ENERGY_DIVISOR))) {
            return;
        }
        // 先算「要抽多少 mB 才够填满」，但真正抽多少要看架子收下了多少 ——
        // addWater 会夹到剩余空间，按抽的量倒推会让多出来的那部分白抽
        int wantedMb = Math.min(stored.amount, (room + potency - 1) / potency);
        int accepted = crop.addWater(wantedMb * potency);
        if (accepted > 0) {
            tank.drain((accepted + potency - 1) / potency, true);
        }
    }

    private void tryFertilize(TileCropStick crop) {
        if (crop.getFertilizerStorage() > FERTILIZER_THRESHOLD) {
            return;
        }
        int room = crop.getMaxFertilizer() - crop.getFertilizerStorage();
        if (room <= 0) {
            return;
        }
        // 先看固体：输入槽里有登记的肥料就吃一个，省得为几撮骨粉铺一条管道
        if (fertilizeFromItem(crop)) {
            return;
        }
        fertilizeFromTank(crop, room);
    }

    /** @return 真的施上肥了返回 true */
    private boolean fertilizeFromItem(TileCropStick crop) {
        int slot = machine.findSolidFertilizerSlot();
        if (slot < 0) {
            return false;
        }
        int potency = FertilizerRegistry.getFertilizer(machine.getImportItems().getStackInSlot(slot));
        if (potency <= 0 || !consumeEnergy(energyPerAction(AUX_ENERGY_DIVISOR))) {
            return false;
        }
        if (crop.addFertilizer(potency) <= 0) {
            return false;
        }
        // 走 extractItem 而不是 shrink：前者会走 handler 的 onContentsChanged
        machine.getImportItems().extractItem(slot, 1, false);
        machine.markDirty();
        return true;
    }

    private void fertilizeFromTank(TileCropStick crop, int room) {
        FluidTank tank = machine.fertilizerTank();
        if (tank == null) {
            return;
        }
        FluidStack stored = tank.getFluid();
        int potency = stored == null ? 0 : FertilizerRegistry.getFertilizer(stored.getFluid());
        if (potency <= 0 || !consumeEnergy(energyPerAction(AUX_ENERGY_DIVISOR))) {
            return;
        }
        int wantedMb = Math.min(stored.amount, (room + potency - 1) / potency);
        int accepted = crop.addFertilizer(wantedMb * potency);
        if (accepted > 0) {
            tank.drain((accepted + potency - 1) / potency, true);
        }
    }
}
