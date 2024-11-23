package com.drppp.drtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BatteryEnergyContainerHandler extends MTETrait implements IEnergyContainer {
    protected final long maxCapacity;
    protected long energyStored;

    private List<IElectricItem> batteries = new ArrayList<>();
    protected long batteryEnergyStored=0;
    protected long batterymaxCapacity=0;

    private final long maxInputVoltage;
    private final long maxInputAmperage;
    private final long maxOutputVoltage;
    private final long maxOutputAmperage;
    protected long lastEnergyInputPerSec = 0L;
    protected long lastEnergyOutputPerSec = 0L;
    protected long energyInputPerSec = 0L;
    protected long energyOutputPerSec = 0L;
    private Predicate<EnumFacing> sideInputCondition;
    private Predicate<EnumFacing> sideOutputCondition;
    protected long amps = 0L;

    public BatteryEnergyContainerHandler(MetaTileEntity tileEntity, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
        super(tileEntity);
        this.maxCapacity = maxCapacity;
        this.maxInputVoltage = maxInputVoltage;
        this.maxInputAmperage = maxInputAmperage;
        this.maxOutputVoltage = maxOutputVoltage;
        this.maxOutputAmperage = maxOutputAmperage;
    }

    public void setSideInputCondition(Predicate<EnumFacing> sideInputCondition) {
        this.sideInputCondition = sideInputCondition;
    }

    public void setSideOutputCondition(Predicate<EnumFacing> sideOutputCondition) {
        this.sideOutputCondition = sideOutputCondition;
    }

    public static BatteryEnergyContainerHandler emitterContainer(MetaTileEntity tileEntity, long maxCapacity, long maxOutputVoltage, long maxOutputAmperage) {
        return new BatteryEnergyContainerHandler(tileEntity, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static BatteryEnergyContainerHandler receiverContainer(MetaTileEntity tileEntity, long maxCapacity, long maxInputVoltage, long maxInputAmperage) {
        return new BatteryEnergyContainerHandler(tileEntity, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    public long getInputPerSec() {
        return this.lastEnergyInputPerSec;
    }

    public long getOutputPerSec() {
        return this.lastEnergyOutputPerSec;
    }

    public @NotNull String getName() {
        return "EnergyContainer";
    }

    public <T> T getCapability(Capability<T> capability) {
        return capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER ? GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(this) : null;
    }

    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("EnergyStored", this.energyStored);
        return compound;
    }

    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        this.energyStored = compound.getLong("EnergyStored");
        this.notifyEnergyListener(true);
    }

    public long getEnergyStored() {
        return this.energyStored+this.batteryEnergyStored;
    }

    public void setEnergyStored(long energyStored) {
        if (energyStored > this.energyStored) {
            this.energyInputPerSec += energyStored - this.energyStored;
        } else {
            this.energyOutputPerSec += this.energyStored - energyStored;
        }

        this.energyStored = energyStored;
        if (!this.metaTileEntity.getWorld().isRemote) {
            this.metaTileEntity.markDirty();
            this.notifyEnergyListener(false);
        }

    }

    protected void notifyEnergyListener(boolean isInitialChange) {
        if (this.metaTileEntity instanceof BatteryEnergyContainerHandler.IEnergyChangeListener) {
            ((BatteryEnergyContainerHandler.IEnergyChangeListener)this.metaTileEntity).onEnergyChanged(this, isInitialChange);
        }

    }

    public boolean dischargeOrRechargeEnergyContainers(IItemHandlerModifiable itemHandler, int slotIndex) {
        ItemStack stackInSlot = itemHandler.getStackInSlot(slotIndex);
        if (stackInSlot.isEmpty()) {
            return false;
        } else {
            IElectricItem electricItem = (IElectricItem)stackInSlot.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, (EnumFacing)null);
            if (electricItem != null) {
                return this.handleElectricItem(electricItem);
            } else {
                if (ConfigHolder.compat.energy.nativeEUToFE) {
                    IEnergyStorage energyStorage = (IEnergyStorage)stackInSlot.getCapability(CapabilityEnergy.ENERGY, (EnumFacing)null);
                    if (energyStorage != null) {
                        return this.handleForgeEnergyItem(energyStorage);
                    }
                }

                return false;
            }
        }
    }

    private boolean handleElectricItem(IElectricItem electricItem) {
        int machineTier = GTUtility.getTierByVoltage(Math.max(this.getInputVoltage(), this.getOutputVoltage()));
        int chargeTier = Math.min(machineTier, electricItem.getTier());
        double chargePercent = (double)this.getEnergyStored() / ((double)this.getEnergyCapacity() * 1.0);
        long chargedBy;
        if (electricItem.canProvideChargeExternally() && this.getEnergyCanBeInserted() > 0L && chargePercent <= 0.33 && chargeTier == machineTier) {
            chargedBy = electricItem.discharge(this.getEnergyCanBeInserted(), machineTier, false, true, false);
            this.addEnergy(chargedBy);
            return chargedBy > 0L;
        } else if (chargePercent > 0.66) {
            chargedBy = electricItem.charge(this.getEnergyStored(), chargeTier, false, false);
            this.removeEnergy(chargedBy);
            return chargedBy > 0L;
        } else {
            return false;
        }
    }

    private boolean handleForgeEnergyItem(IEnergyStorage energyStorage) {
        int machineTier = GTUtility.getTierByVoltage(Math.max(this.getInputVoltage(), this.getOutputVoltage()));
        double chargePercent = (double)this.getEnergyStored() / ((double)this.getEnergyCapacity() * 1.0);
        if (chargePercent > 0.66) {
            long chargedBy = FeCompat.insertEu(energyStorage, GTValues.V[machineTier]);
            this.removeEnergy(chargedBy);
            return chargedBy > 0L;
        } else {
            return false;
        }
    }
    public void syncBattriesInfo()
    {
        writeCustomData(2024,x->x.writeLong(this.batterymaxCapacity));
        writeCustomData(2025,x->x.writeLong(this.batteryEnergyStored));
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if(discriminator==2024)
            this.batterymaxCapacity = buf.readLong();
        if(discriminator==2025)
            this.batteryEnergyStored = buf.readLong();
    }

    public void update() {
        this.amps = 0L;
        if (!this.getMetaTileEntity().getWorld().isRemote) {
            if (this.metaTileEntity.getOffsetTimer() % 20L == 0L) {
                this.lastEnergyOutputPerSec = this.energyOutputPerSec;
                this.lastEnergyInputPerSec = this.energyInputPerSec;
                this.energyOutputPerSec = 0L;
                this.energyInputPerSec = 0L;
            }
            if(this.metaTileEntity.getItemInventory().getSlots()>0)
            {
                batteries.clear();
                this.batterymaxCapacity=0;
                this.batteryEnergyStored=0;
                syncBattriesInfo();
                for (int i = 0; i < this.metaTileEntity.getItemInventory().getSlots(); i++)
                {
                    ItemStack itemStack = this.metaTileEntity.getItemInventory().getStackInSlot(i);
                    if(itemStack!=null && !itemStack.isEmpty() && itemStack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM,null))
                    {
                        batteries.add(itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM,null));
                    }
                }
                if(batteries.size()>0)
                {
                    batteries.forEach(x->{
                        this.batterymaxCapacity += x.getMaxCharge();
                        this.batteryEnergyStored += x.getCharge();
                    });
                    syncBattriesInfo();
                }
                else
                {
                    this.batteryEnergyStored = 0;
                    this.batterymaxCapacity = 0;
                    syncBattriesInfo();
                }
            }
            if (this.getEnergyStored() >= this.getOutputVoltage() && this.getOutputVoltage() > 0L && this.getOutputAmperage() > 0L) {
                long outputVoltage = this.getOutputVoltage();
                long outputAmperes = Math.min(this.getEnergyStored() / outputVoltage, this.getOutputAmperage());
                if (outputAmperes == 0L) {
                    return;
                }

                long amperesUsed = 0L;
                EnumFacing[] var7 = EnumFacing.VALUES;
                int var8 = var7.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    EnumFacing side = var7[var9];
                    if (this.outputsEnergy(side)) {
                        TileEntity tileEntity = this.metaTileEntity.getNeighbor(side);
                        EnumFacing oppositeSide = side.getOpposite();
                        if (tileEntity != null && tileEntity.hasCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide)) {
                            IEnergyContainer energyContainer = (IEnergyContainer)tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide);
                            if (energyContainer != null && energyContainer.inputsEnergy(oppositeSide)) {
                                amperesUsed += energyContainer.acceptEnergyFromNetwork(oppositeSide, outputVoltage, outputAmperes - amperesUsed);
                                if (amperesUsed == outputAmperes) {
                                    break;
                                }
                            }
                        }
                    }
                }

                if (amperesUsed > 0L) {
                    this.changeEnergy(this.getEnergyStored() - amperesUsed * outputVoltage);
                }
            }

        }
    }

    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        if (this.amps >= this.getInputAmperage()) {
            return 0L;
        } else {
            long canAccept = this.getEnergyCapacity() - this.getEnergyStored();
            if (voltage > 0L && (side == null || this.inputsEnergy(side))) {
                if (voltage > this.getInputVoltage()) {
                    this.metaTileEntity.doExplosion((float)GTUtility.getExplosionPower(voltage));
                    return Math.min(amperage, this.getInputAmperage() - this.amps);
                }

                if (canAccept >= voltage) {
                    long amperesAccepted = Math.min(canAccept / voltage, Math.min(amperage, this.getInputAmperage() - this.amps));
                    if (amperesAccepted > 0L) {
                        this.changeEnergy(this.getEnergyStored() + voltage * amperesAccepted);
                        this.amps += amperesAccepted;
                        return amperesAccepted;
                    }
                }
            }

            return 0L;
        }
    }

    public long getEnergyCapacity() {
        return this.maxCapacity+this.batterymaxCapacity;
    }

    public boolean inputsEnergy(EnumFacing side) {
        return !this.outputsEnergy(side) && this.getInputVoltage() > 0L && (this.sideInputCondition == null || this.sideInputCondition.test(side));
    }

    public boolean outputsEnergy(EnumFacing side) {
        return this.getOutputVoltage() > 0L && (this.sideOutputCondition == null || this.sideOutputCondition.test(side));
    }

    public long changeEnergy(long energyToAdd) {
        //首先由电池处理传递过来的电量
        energyToAdd = changeBatteryEnergy(energyToAdd);
        //剩下没处理完毕的电量交给能源仓缓存处理
        long oldEnergyStored = this.energyStored;
        long newEnergyStored = this.maxCapacity - oldEnergyStored < energyToAdd ? this.maxCapacity : oldEnergyStored + energyToAdd;
        if (newEnergyStored < 0L) {
            newEnergyStored = 0L;
        }
        this.setEnergyStored(newEnergyStored);
        return newEnergyStored - oldEnergyStored;
    }
    public long changeBatteryEnergy(long energyToAdd)
    {
        long leftenergy = energyToAdd;
        if(this.batteries.size()==0)
            return  energyToAdd;
        int count = this.batteries.size();
        long average_energy = leftenergy/count;
        for (int i = 0; i < count; i++) {
            var battery = batteries.get(i);
            //充电
            if(average_energy>0){
                long charged = battery.charge(average_energy,GTUtility.getTierByVoltage(maxInputVoltage),false,false);
                leftenergy -= charged;
            }
            //耗电
            else {
                long charged = battery.charge(Math.abs(average_energy),GTUtility.getTierByVoltage(maxInputVoltage),false,false);
                leftenergy += charged;
            }
        }
        return leftenergy;
    }
    public long getOutputVoltage() {
        return this.maxOutputVoltage;
    }

    public long getOutputAmperage() {
        return this.maxOutputAmperage;
    }

    public long getInputAmperage() {
        return this.maxInputAmperage;
    }

    public long getInputVoltage() {
        return this.maxInputVoltage;
    }

    public String toString() {
        return "EnergyContainerHandler{maxCapacity=" + this.maxCapacity + ", energyStored=" + this.energyStored + ", maxInputVoltage=" + this.maxInputVoltage + ", maxInputAmperage=" + this.maxInputAmperage + ", maxOutputVoltage=" + this.maxOutputVoltage + ", maxOutputAmperage=" + this.maxOutputAmperage + ", amps=" + this.amps + '}';
    }

    public interface IEnergyChangeListener {
        void onEnergyChanged(IEnergyContainer var1, boolean var2);
    }
}
