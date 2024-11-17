package com.drppp.drtech.api.ItemHandler;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class FilterItemStackHandler  extends ItemStackHandler {
    OrePrefix orePrefix;
    IItemHandler inventory;
    public FilterItemStackHandler()
    {

    }
    public FilterItemStackHandler(OrePrefix orePrefix,IItemHandler inv)
    {
        this.orePrefix = orePrefix;
        this.inventory = inv;
    }
    @Override
    public int getSlots() {
        return inventory.getSlots();
    }
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }
    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(OreDictUnifier.getPrefix(stack) != null && orePrefix!=null)
        {
            var fix = OreDictUnifier.getPrefix(stack);
            if(!orePrefix.equals(fix))
                return stack;
        }
        return  inventory.insertItem(slot,stack,simulate);
    }
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.extractItem(slot, amount, simulate);
    }
    @Override
    public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(slot);
    }
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return inventory.isItemValid(slot,stack);
    }
}
