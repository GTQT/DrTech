package com.brachy84.mechtech.client.gui;

import gregtech.api.gui.widgets.SlotWidget;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Predicate;

public class SlotArmorWorkbench extends SlotWidget {

    private Predicate<ItemStack> filter;
    private Type type = Type.ARMOR;

    public SlotArmorWorkbench(IItemHandler inventory, int slotIndex, int xPosition, int yPosition) {
        super(inventory, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotArmorWorkbench setType(Type type) {
        this.type = type;
        return this;
    }

    public SlotArmorWorkbench setFilter(Predicate<ItemStack> filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public boolean canPutStack(ItemStack stack) {
        return super.canPutStack(stack) && !getHandle().getHasStack() && filter.test(stack);
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        MODULE, BATTERY, ARMOR
    }
}
