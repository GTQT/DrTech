package com.drppp.drtech.api.ItemHandler;

import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class SingleItemStackHandler extends ItemStackHandler {
    public boolean lockCap = false;
    public Capability[] slotCaps;
    public SingleItemStackHandler()
    {
        this(1,false);
    }
    public SingleItemStackHandler(int size,boolean lock)
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.slotCaps = new Capability[size];
        this.lockCap = lock;
    }
    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(lockCap && slotCaps[slot]!=null && !stack.hasCapability(slotCaps[slot],null))
        {
            return ItemStack.EMPTY;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        var nbt = super.serializeNBT();
        nbt.setBoolean("lockCap",lockCap);
        for (int i = 0; i < this.getSlots(); i++) {
            nbt.setInteger("slotCap"+i,getIntMapCap(slotCaps[i]));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.lockCap = nbt.getBoolean("lockCap");
        this.slotCaps = new Capability[this.getSlots()];
        for (int i = 0; i < this.getSlots(); i++) {
            slotCaps[i] = getCapMapInt(nbt.getInteger("slotCap"+i));
        }
    }
    private Capability getCapMapInt(int i)
    {
        switch (i)
        {
            case 0:
                return DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD;
            case 1:
                return DrtechCommonCapabilities.CAPABILITY_HEAT_VENT;
            case 2:
                return DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER;
            case 3:
                return DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR;
            case 4:
                return DrtechCommonCapabilities.CAPABILITY_COOLANT_CELL;
        }
        return  null;
    }
    private int getIntMapCap(Capability i)
    {
        if(i==null)
            return -1;
        if (i.equals(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD))
        {
            return 0;
        }else if (i.equals(DrtechCommonCapabilities.CAPABILITY_HEAT_VENT))
        {
            return 1;
        }
        else if (i.equals(DrtechCommonCapabilities.CAPABILITY_HEAT_EXCHANGER))
        {
            return 2;
        }
        else if (i.equals(DrtechCommonCapabilities.CAPABILITY_NEUTRON_REFLECTOR))
        {
            return 3;
        }
        else if (i.equals(DrtechCommonCapabilities.CAPABILITY_COOLANT_CELL))
        {
            return 4;
        }
        return   -1;
    }
}
