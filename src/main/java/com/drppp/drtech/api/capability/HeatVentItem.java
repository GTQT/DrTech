package com.drppp.drtech.api.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeatVentItem  implements IHeatVent , ICapabilityProvider {
    protected final ItemStack itemStack;
    private int HeatAbsorptionRate;
    private int HeatDissipationRate;
    private int MaxHeat;
    private boolean reactorInteraction;
    private boolean elementInteraction;
    public HeatVentItem(){
        this.itemStack =ItemStack.EMPTY;
    }
    public HeatVentItem(ItemStack itemStack, int heatAbsorptionRate, int heatDissipationRate, int maxHeat, boolean reactorInteraction, boolean elementInteraction) {
        this.itemStack = itemStack;
        this.HeatAbsorptionRate = heatAbsorptionRate;
        this.HeatDissipationRate = heatDissipationRate;
        this.MaxHeat = maxHeat;
        this.reactorInteraction = reactorInteraction;
        this.elementInteraction = elementInteraction;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability==null)
            return false;
        return capability==DrtechCommonCapabilities.CAPABILITY_HEAT_VENT;
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == DrtechCommonCapabilities.CAPABILITY_HEAT_VENT? (T) this : null;
    }

    @Override
    public int getHeatAbsorptionRate() {
        return this.HeatAbsorptionRate;
    }

    @Override
    public int getHeatDissipationRate() {
        return this.HeatDissipationRate;
    }

    @Override
    public int getMaxHeat() {
        return this.MaxHeat;
    }

    @Override
    public int getHeat() {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null)
            return 0;
        if(tagCompound.hasKey("Heat"))
            return tagCompound.getInteger("Heat");
        return 0;
    }

    @Override
    public void setHeat(int amount) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        itemStack.getTagCompound().setInteger("Heat", Math.max(amount,0));
    }

    @Override
    public boolean reactorInteraction() {
        return this.reactorInteraction;
    }

    @Override
    public boolean elementInteraction() {
        return this.elementInteraction;
    }
}
