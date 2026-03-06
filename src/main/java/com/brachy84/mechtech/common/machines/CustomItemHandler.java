package com.brachy84.mechtech.common.machines;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Collections;

public class CustomItemHandler extends ItemStackHandler {

    public CustomItemHandler(int size) {
        super(size);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    public void clear() {
        Collections.fill(this.stacks, ItemStack.EMPTY);
    }

    public void setSlotSilent(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
    }
}
