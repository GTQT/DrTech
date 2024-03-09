package com.drppp.drtech.api.capability;

import gregtech.api.items.metaitem.stats.ISubItemHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.tools.nsc.transform.patmat.ScalaLogic;

public class HeatExchangerItem implements IHeatExchanger , ICapabilityProvider , ISubItemHandler {
    protected final ItemStack itemStack;
    private int reactorExchangeHeatRate;
    private int elementExchangeHeatRate;
    private int maxHeat;
    private boolean reactorInteraction;
    private boolean elementInteraction;

    public HeatExchangerItem() {
        this.itemStack = ItemStack.EMPTY;
    }
    public HeatExchangerItem(ItemStack itemStack, int reactorExchangeHeatRate, int elementExchangeHeatRate, int maxHeat, boolean reactorInteraction, boolean elementInteraction) {
        this.itemStack = itemStack;
        this.reactorExchangeHeatRate = reactorExchangeHeatRate;
        this.elementExchangeHeatRate = elementExchangeHeatRate;
        this.maxHeat = maxHeat;
        this.reactorInteraction = reactorInteraction;
        this.elementInteraction = elementInteraction;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability==null)
            return false;
        return capability==DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER? (T) this : null;
    }


    @Override
    public int getMaxHeat() {
        return this.maxHeat;
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
        itemStack.getTagCompound().setInteger("Heat", amount);
    }

    @Override
    public boolean reactorInteraction() {
        return this.reactorInteraction;
    }

    @Override
    public boolean elementInteraction() {
        return this.elementInteraction;
    }

    @Override
    public int getReactorExchangeHeatRate() {
        return this.reactorExchangeHeatRate;
    }

    @Override
    public int getElementExchangeHeatRate() {
        return this.elementExchangeHeatRate;
    }
    @Override
    public String getItemSubType(ItemStack itemStack) {
        return "";
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTabs, NonNullList<ItemStack> nonNullList) {
        ItemStack copy = itemStack.copy();
        var electricItem = copy.getCapability(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER, null);
        if (electricItem != null) {
            // electricItem.charge(electricItem.getMaxCharge(), electricItem.getTier(), true, false);
            nonNullList.add(copy);
        } else {
            nonNullList.add(itemStack);
        }
    }
}
