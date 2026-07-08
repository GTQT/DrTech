package com.drppp.drtech.hooked;

import net.minecraft.item.ItemStack;

public class ItemHookComponents extends ItemMultiVariant {
    public ItemHookComponents() {
        super("hooked_component", new String[]{"plant_fiber", "rope", "iron_chain_link", "iron_chain"});
    }

    public ItemStack stack(HookComponentType type, int count) {
        return new ItemStack(this, count, type.ordinal());
    }
}
