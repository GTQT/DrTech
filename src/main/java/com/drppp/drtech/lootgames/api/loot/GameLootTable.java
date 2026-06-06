package com.drppp.drtech.lootgames.api.loot;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A named loot table for LootGames minigames.
 * Contains weighted entries and determines how many items to generate per roll.
 */
public class GameLootTable {
    private final ResourceLocation name;
    private final List<LootEntry> entries;
    private final int minRolls;
    private final int maxRolls;
    private final int totalWeight;

    private GameLootTable(ResourceLocation name, List<LootEntry> entries, int minRolls, int maxRolls) {
        this.name = name;
        this.entries = entries;
        this.minRolls = Math.max(1, minRolls);
        this.maxRolls = Math.max(this.minRolls, maxRolls);
        this.totalWeight = entries.stream().mapToInt(LootEntry::getWeight).sum();
    }

    public ResourceLocation getName() { return name; }
    public List<LootEntry> getEntries() { return entries; }
    public int getMinRolls() { return minRolls; }
    public int getMaxRolls() { return maxRolls; }

    /**
     * Rolls the table and returns a list of generated ItemStacks.
     */
    public List<ItemStack> generate(Random rand) {
        int rollCount = minRolls + (maxRolls > minRolls ? rand.nextInt(maxRolls - minRolls + 1) : 0);
        List<ItemStack> result = new ArrayList<>();

        for (int i = 0; i < rollCount; i++) {
            LootEntry picked = pickEntry(rand);
            if (picked != null) {
                result.add(picked.generateStack(rand));
            }
        }

        return result;
    }

    private LootEntry pickEntry(Random rand) {
        if (entries.isEmpty() || totalWeight <= 0) return null;
        int roll = rand.nextInt(totalWeight);
        int cumulative = 0;
        for (LootEntry entry : entries) {
            cumulative += entry.getWeight();
            if (roll < cumulative) {
                return entry;
            }
        }
        return entries.get(entries.size() - 1);
    }

    public static Builder builder(ResourceLocation name) {
        return new Builder(name);
    }

    public static class Builder {
        private final ResourceLocation name;
        private final List<LootEntry> entries = new ArrayList<>();
        private int minRolls = 1;
        private int maxRolls = 3;

        private Builder(ResourceLocation name) {
            this.name = name;
        }

        public Builder rolls(int min, int max) {
            this.minRolls = min;
            this.maxRolls = max;
            return this;
        }

        public Builder add(LootEntry entry) {
            entries.add(entry);
            return this;
        }

        public Builder add(ItemStack stack, int weight, int minCount, int maxCount) {
            entries.add(new LootEntry(stack, weight, minCount, maxCount));
            return this;
        }

        public GameLootTable build() {
            return new GameLootTable(name, entries, minRolls, maxRolls);
        }
    }
}
