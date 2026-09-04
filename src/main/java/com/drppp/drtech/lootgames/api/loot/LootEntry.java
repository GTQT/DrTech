package com.drppp.drtech.lootgames.api.loot;

import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * A single entry in a game loot table.
 * Each entry has a weighted chance, item stack, and count range.
 */
public class LootEntry {
    private final ItemStack stack;
    private final int weight;
    private final int minCount;
    private final int maxCount;

    public LootEntry(ItemStack stack, int weight, int minCount, int maxCount) {
        this.stack = stack;
        this.weight = Math.max(1, weight);
        this.minCount = Math.max(1, minCount);
        this.maxCount = Math.max(this.minCount, maxCount);
    }

    public ItemStack getStack() { return stack; }
    public int getWeight() { return weight; }
    public int getMinCount() { return minCount; }
    public int getMaxCount() { return maxCount; }

    /**
     * Generates an ItemStack with a random count within [minCount, maxCount].
     */
    public ItemStack generateStack(Random rand) {
        ItemStack result = stack.copy();
        int count = minCount + (maxCount > minCount ? rand.nextInt(maxCount - minCount + 1) : 0);
        result.setCount(count);
        return result;
    }
}
