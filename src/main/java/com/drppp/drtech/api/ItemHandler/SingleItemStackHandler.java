package com.drppp.drtech.api.ItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

public class SingleItemStackHandler extends ItemStackHandler {
    public SingleItemStackHandler()
    {
        this(1);
    }

    public SingleItemStackHandler(int size)
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }
    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }
}
