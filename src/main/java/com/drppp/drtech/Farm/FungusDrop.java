package com.drppp.drtech.Farm;

import net.minecraft.item.ItemStack;

public class FungusDrop {
    public ItemStack DropItem;
    public int Prob;//1-100

    public FungusDrop(ItemStack dropItem, int prob) {
        DropItem = dropItem;
        Prob = prob;
    }
}
