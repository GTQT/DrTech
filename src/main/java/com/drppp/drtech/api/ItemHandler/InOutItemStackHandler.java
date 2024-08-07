package com.drppp.drtech.api.ItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class InOutItemStackHandler extends ItemStackHandler {
    private boolean canin=true;
    public InOutItemStackHandler(int size,boolean canin )
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.canin = canin;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return this.canin;
    }

}
