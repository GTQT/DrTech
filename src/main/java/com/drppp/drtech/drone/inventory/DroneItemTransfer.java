package com.drppp.drtech.drone.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/** Simulate-first item transfer used by both import and export actions. */
public final class DroneItemTransfer {

    private DroneItemTransfer() {}

    public static int transfer(IItemHandler source, IItemHandler target, DroneItemFilter filter,
            int maximum, boolean simulate) {
        if (source == null || target == null || maximum <= 0) return 0;
        DroneItemFilter effectiveFilter = filter == null ? DroneItemFilter.ANY : filter;
        int movedTotal = 0;
        for (int slot = 0; slot < source.getSlots() && movedTotal < maximum; slot++) {
            int remainingLimit = maximum - movedTotal;
            ItemStack preview = source.extractItem(slot, remainingLimit, true);
            if (preview.isEmpty() || !effectiveFilter.matches(preview)) continue;
            ItemStack rejected = ItemHandlerHelper.insertItemStacked(target, preview, true);
            int accepted = preview.getCount() - rejected.getCount();
            if (accepted <= 0) continue;
            if (simulate) {
                movedTotal += accepted;
                continue;
            }
            ItemStack extracted = source.extractItem(slot, accepted, false);
            if (extracted.isEmpty()) continue;
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, extracted, false);
            int moved = extracted.getCount() - remainder.getCount();
            movedTotal += moved;
            if (!remainder.isEmpty()) {
                ItemHandlerHelper.insertItemStacked(source, remainder, false);
                break;
            }
        }
        return movedTotal;
    }
}
