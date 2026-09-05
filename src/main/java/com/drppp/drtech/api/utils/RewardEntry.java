package com.drppp.drtech.api.Utils;

import net.minecraft.item.ItemStack;

/**
 * 奖励条目，包含物品和对应的概率 (0-100)。
 */
public class RewardEntry {

    private final ItemStack itemStack;
    private final int probability; // 0-100

    public RewardEntry(ItemStack itemStack, int probability) {
        if (probability < 0 || probability > 100) {
            throw new IllegalArgumentException("Probability must be between 0 and 100, got: " + probability);
        }
        this.itemStack = itemStack.copy();
        this.probability = probability;
    }

    public ItemStack getItemStack() {
        return itemStack.copy();
    }

    public int getProbability() {
        return probability;
    }

    public RewardEntry copy() {
        return new RewardEntry(itemStack.copy(), probability);
    }
}
