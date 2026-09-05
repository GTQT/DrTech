package com.brachy84.mechtech.api.capability.impl;

import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class EnergyRelayItemHandler extends ItemStackHandler {

    private List<Integer> erroredSlots = new ArrayList<>();

    public EnergyRelayItemHandler(int size) {
        super(size);
    }

    public boolean hasError(int slot) {
        return erroredSlots.contains(slot);
    }

    public void errorSlot(int slot) {
        if(!erroredSlots.contains(slot)) {
            erroredSlots.add(slot);
        }
    }

    public void resolvedError(int slot) {
        if(hasError(slot)) {
            erroredSlots.remove(slot);
        }
    }

    public List<Integer> getErroredSlots() {
        return erroredSlots;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return MetaItems.TOOL_DATA_STICK.isItemEqual(stack);
    }
}
