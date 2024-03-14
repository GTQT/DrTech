package com.drppp.drtech.api.ItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

public class InfinteItemStackHandler extends ItemStackHandler {

    public InfinteItemStackHandler()
    {
        this(1);
    }

    public InfinteItemStackHandler(int size)
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }
    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }
}
