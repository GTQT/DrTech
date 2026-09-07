package com.drppp.drtech.api.ItemHandler;

import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class PailItemStackHandler extends ItemStackHandler {
    //test
    public PailItemStackHandler()
    {

        this(1);
    }

    public PailItemStackHandler(int size)
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.getItem() == ItemsInit.ITEM_BLOCK_STORAGE_PAIL) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }
}
