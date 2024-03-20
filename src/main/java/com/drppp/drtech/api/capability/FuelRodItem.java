package com.drppp.drtech.api.capability;

import gregtech.api.capability.GregtechCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class FuelRodItem implements IFuelRodData, ICapabilityProvider {
    protected final ItemStack itemStack;
    private int baseOutEnergy;
    private int X1Energy;
    private float baseOutHeat;
    private int pulseNum;
    private int durability;
    private int maxDurability;
    private final ItemStack outItem;
    private boolean isMox;
    private float moxMulti;
    protected final List<BiConsumer<ItemStack, Integer>> listeners = new ArrayList<>();
    public FuelRodItem(ItemStack itemStack, int baseOutEnergy, float baseOutHeat, int pulseNum, boolean isMox,float moxMulti, int maxDurability,ItemStack item,int x1Energy) {
        this.itemStack = itemStack;
        this.baseOutEnergy = baseOutEnergy;
        this.baseOutHeat = baseOutHeat;
        this.pulseNum = pulseNum;
        this.durability = maxDurability;
        this.maxDurability = maxDurability;
        this.outItem = item;
        this.X1Energy = x1Energy;
        this.isMox = isMox;
        this.moxMulti = moxMulti;
        if(itemStack.getTagCompound()!=null && !itemStack.getTagCompound().hasKey("Durability"))
        {
            itemStack.getTagCompound().setInteger("Durability",maxDurability);
        }
    }


    public void setDurability(int durability) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        itemStack.getTagCompound().setInteger("Durability", durability);
        listeners.forEach(l -> l.accept(itemStack, durability));
        //this.durability = durability;
    }

    @Override
    public ItemStack outItem() {
        return outItem;
    }

    @Override
    public boolean isMox() {
        return this.isMox;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setMaxDurability(int maxDurability) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        itemStack.getTagCompound().setInteger("MaxDurability", maxDurability);
        this.maxDurability = maxDurability;

    }

    @Override
    public float getBaseHeat() {
      return this.baseOutHeat;
    }

    @Override
    public int getBaseEnergy() {
      return this.baseOutEnergy;
    }

    @Override
    public int get1XEnergy() {
        return this.X1Energy;
    }

    @Override
    public int getPulseNum() {
       return this.pulseNum;
    }

    @Override
    public int getDurability() {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null)
            return this.durability;
        if(tagCompound.hasKey("Durability"))
            return tagCompound.getInteger("Durability");
        return this.durability;
    }

    @Override
    public int getMaxDurability() {
        return this.maxDurability;
    }

    @Override
    public float getMoxMulti() {
        return this.moxMulti;
    }

    @Override
    public void addChargeListener(BiConsumer<ItemStack, Integer> chargeListener) {
        listeners.add(chargeListener);
    }
    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability==null)
            return false;
        return capability == DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD;
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD? (T) this : null;
    }



}
