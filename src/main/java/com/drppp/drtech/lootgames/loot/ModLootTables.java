package com.drppp.drtech.lootgames.loot;

import com.drppp.drtech.Tags;
import com.drppp.drtech.lootgames.api.loot.GameLootTable;
import com.drppp.drtech.lootgames.api.loot.LootTableRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * All LootGames loot table definitions.
 * Call {@link #init()} during mod initialization to register all tables.
 */
public class ModLootTables {

    // === Minesweeper loot tables (one per chest position) ===
    public static final ResourceLocation MS_LEVEL2 = location("ms/level2");
    public static final ResourceLocation MS_LEVEL3 = location("ms/level3");
    public static final ResourceLocation MS_LEVEL4 = location("ms/level4");
    public static final ResourceLocation MS_LEVEL5 = location("ms/level5");

    // === GameOfLight loot tables (one per chest position) ===
    public static final ResourceLocation GOL_LEVEL1 = location("gol/level1");
    public static final ResourceLocation GOL_LEVEL2 = location("gol/level2");
    public static final ResourceLocation GOL_LEVEL3 = location("gol/level3");
    public static final ResourceLocation GOL_LEVEL4 = location("gol/level4");

    public static void init() {
        // Minesweeper
        LootTableRegistry.register(GameLootTable.builder(MS_LEVEL2).rolls(1, 3)
                .add(new ItemStack(Items.IRON_INGOT), 30, 1, 4)
                .add(new ItemStack(Items.GOLD_INGOT), 20, 1, 3)
                .add(new ItemStack(Items.REDSTONE), 25, 2, 8)
                .add(new ItemStack(Items.BONE), 25, 2, 6)
                .build());

        LootTableRegistry.register(GameLootTable.builder(MS_LEVEL3).rolls(2, 4)
                .add(new ItemStack(Items.GOLD_INGOT), 25, 1, 4)
                .add(new ItemStack(Items.DIAMOND), 10, 1, 2)
                .add(new ItemStack(Items.EMERALD), 15, 1, 3)
                .add(new ItemStack(Items.EXPERIENCE_BOTTLE), 20, 2, 6)
                .add(new ItemStack(Items.GOLDEN_APPLE), 10, 1, 2)
                .add(new ItemStack(Items.IRON_INGOT), 20, 2, 6)
                .build());

        LootTableRegistry.register(GameLootTable.builder(MS_LEVEL4).rolls(2, 5)
                .add(new ItemStack(Items.DIAMOND), 20, 1, 3)
                .add(new ItemStack(Items.EMERALD), 20, 2, 4)
                .add(new ItemStack(Items.EXPERIENCE_BOTTLE), 25, 3, 8)
                .add(new ItemStack(Items.GOLDEN_APPLE), 15, 1, 3)
                .add(new ItemStack(Blocks.OBSIDIAN), 20, 2, 6)
                .build());

        LootTableRegistry.register(GameLootTable.builder(MS_LEVEL5).rolls(3, 6)
                .add(new ItemStack(Items.DIAMOND), 25, 2, 5)
                .add(new ItemStack(Items.EMERALD), 25, 3, 6)
                .add(new ItemStack(Items.NETHER_STAR), 5, 1, 1)
                .add(new ItemStack(Items.EXPERIENCE_BOTTLE), 30, 4, 12)
                .add(new ItemStack(Items.GOLDEN_APPLE, 1, 1), 10, 1, 2) // Enchanted golden apple
                .add(new ItemStack(Items.DIAMOND_SWORD), 5, 1, 1)
                .build());

        // GameOfLight
        LootTableRegistry.register(GameLootTable.builder(GOL_LEVEL1).rolls(1, 2)
                .add(new ItemStack(Items.IRON_INGOT), 30, 1, 3)
                .add(new ItemStack(Items.BREAD), 25, 2, 5)
                .add(new ItemStack(Items.REDSTONE), 25, 2, 6)
                .add(new ItemStack(Items.COAL), 20, 3, 8)
                .build());

        LootTableRegistry.register(GameLootTable.builder(GOL_LEVEL2).rolls(2, 3)
                .add(new ItemStack(Items.GOLD_INGOT), 25, 1, 3)
                .add(new ItemStack(Items.IRON_INGOT), 25, 2, 5)
                .add(new ItemStack(Items.DIAMOND), 10, 1, 1)
                .add(new ItemStack(Items.EXPERIENCE_BOTTLE), 20, 2, 4)
                .add(new ItemStack(Items.GOLDEN_APPLE), 10, 1, 1)
                .build());

        LootTableRegistry.register(GameLootTable.builder(GOL_LEVEL3).rolls(2, 4)
                .add(new ItemStack(Items.DIAMOND), 20, 1, 2)
                .add(new ItemStack(Items.EMERALD), 20, 2, 4)
                .add(new ItemStack(Items.EXPERIENCE_BOTTLE), 25, 3, 6)
                .add(new ItemStack(Items.GOLDEN_APPLE), 15, 1, 2)
                .add(new ItemStack(Items.SADDLE), 10, 1, 1)
                .build());

        LootTableRegistry.register(GameLootTable.builder(GOL_LEVEL4).rolls(3, 5)
                .add(new ItemStack(Items.DIAMOND), 25, 2, 4)
                .add(new ItemStack(Items.EMERALD), 25, 3, 5)
                .add(new ItemStack(Items.NETHER_STAR), 5, 1, 1)
                .add(new ItemStack(Items.EXPERIENCE_BOTTLE), 30, 4, 10)
                .add(new ItemStack(Items.GOLDEN_APPLE, 1, 1), 10, 1, 2)
                .add(new ItemStack(Items.ENDER_PEARL), 10, 1, 3)
                .build());
    }

    private static ResourceLocation location(String path) {
        return new ResourceLocation(Tags.MODID, "lootgames/" + path);
    }
}
