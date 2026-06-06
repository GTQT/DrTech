package com.drppp.drtech.loaders;

import com.drppp.drtech.api.Utils.RewardBoxManager;
import com.drppp.drtech.api.Utils.RewardEntry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * 奖励箱注册类。
 * 所有 RewardBoxManager 的注册调用集中在此处，通过 {@link #init()} 统一初始化。
 */
public class ModRewardBoxes {

    private ModRewardBoxes() {
    }

    public static void init() {
        registerTestRewardBox();
        registerTierRewardBoxes();
        registerModRewardBoxes();
    }

    private static void registerTestRewardBox() {
        RewardBoxManager.registerRewardBox("test", Arrays.asList(
                new RewardEntry(new ItemStack(Items.DIAMOND), 10),
                new RewardEntry(new ItemStack(Items.IRON_INGOT), 30),
                new RewardEntry(new ItemStack(Items.GOLD_INGOT), 25),
                new RewardEntry(new ItemStack(Items.EMERALD), 15),
                new RewardEntry(new ItemStack(Items.APPLE), 20)
        ));
    }

    private static List<RewardEntry> tierRewards(ItemStack... materials) {
        List<RewardEntry> list = new java.util.ArrayList<>();
        for (ItemStack stack : materials) {
            if (!stack.isEmpty()) list.add(new RewardEntry(stack, 25));
        }
        list.add(new RewardEntry(new ItemStack(Items.EXPERIENCE_BOTTLE), 30));
        list.add(new RewardEntry(new ItemStack(Items.GOLDEN_APPLE), 10));
        return list;
    }

    private static void registerTierRewardBoxes() {
        RewardBoxManager.registerRewardBox("stone_age", tierRewards(
                new ItemStack(Items.FLINT), new ItemStack(Items.COAL),
                new ItemStack(Items.BONE), new ItemStack(Items.ROTTEN_FLESH)));
        RewardBoxManager.registerRewardBox("steam_age", tierRewards(
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.GUNPOWDER)));
        RewardBoxManager.registerRewardBox("lv", tierRewards(
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.GLOWSTONE_DUST)));
        RewardBoxManager.registerRewardBox("mv", tierRewards(
                new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.EMERALD), new ItemStack(Items.BLAZE_ROD)));
        RewardBoxManager.registerRewardBox("hv", tierRewards(
                new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD),
                new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.ENDER_PEARL)));
        RewardBoxManager.registerRewardBox("ev", tierRewards(
                new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD),
                new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.EXPERIENCE_BOTTLE)));
        RewardBoxManager.registerRewardBox("iv", tierRewards(
                new ItemStack(Items.DIAMOND), new ItemStack(Items.NETHER_STAR),
                new ItemStack(Items.EMERALD), new ItemStack(Blocks.OBSIDIAN)));
        RewardBoxManager.registerRewardBox("luv", tierRewards(
                new ItemStack(Items.DIAMOND), new ItemStack(Items.NETHER_STAR),
                new ItemStack(Items.EMERALD), new ItemStack(Items.ENDER_PEARL)));
        RewardBoxManager.registerRewardBox("zpm", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.EMERALD), new ItemStack(Items.ENDER_PEARL)));
        RewardBoxManager.registerRewardBox("uv", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.EMERALD), new ItemStack(Blocks.OBSIDIAN)));
        RewardBoxManager.registerRewardBox("uhv", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.EMERALD), new ItemStack(Items.GOLDEN_APPLE, 1, 1)));
    }

    private static void registerModRewardBoxes() {
        // AE
        RewardBoxManager.registerRewardBox("ae1", tierRewards(
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.FLINT)));
        RewardBoxManager.registerRewardBox("ae2", tierRewards(
                new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD),
                new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.ENDER_PEARL)));
        // Bees
        RewardBoxManager.registerRewardBox("bee1", tierRewards(
                new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.STRING),
                new ItemStack(Items.PAPER), new ItemStack(Items.SLIME_BALL)));
        RewardBoxManager.registerRewardBox("bee2", tierRewards(
                new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD)));
        RewardBoxManager.registerRewardBox("bee3", tierRewards(
                new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.NETHER_STAR),
                new ItemStack(Items.DIAMOND), new ItemStack(Items.GOLDEN_APPLE)));
        // Blood Magic
        RewardBoxManager.registerRewardBox("bm1", tierRewards(
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.BLAZE_ROD)));
        RewardBoxManager.registerRewardBox("bm2", tierRewards(
                new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD),
                new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.ENDER_PEARL)));
        RewardBoxManager.registerRewardBox("bm3", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.ENDER_PEARL)));
        // Computer
        RewardBoxManager.registerRewardBox("computer1", tierRewards(
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.COMPARATOR)));
        // Crops
        RewardBoxManager.registerRewardBox("crops", tierRewards(
                new ItemStack(Items.WHEAT), new ItemStack(Items.CARROT),
                new ItemStack(Items.POTATO), new ItemStack(Items.BEETROOT)));
        // Floppies
        RewardBoxManager.registerRewardBox("floppies", tierRewards(
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.PAPER)));
        // Food
        RewardBoxManager.registerRewardBox("food1", tierRewards(
                new ItemStack(Items.APPLE), new ItemStack(Items.BREAD),
                new ItemStack(Items.COOKED_BEEF), new ItemStack(Items.COOKED_PORKCHOP)));
        RewardBoxManager.registerRewardBox("food2", tierRewards(
                new ItemStack(Items.COOKED_CHICKEN), new ItemStack(Items.BAKED_POTATO),
                new ItemStack(Items.COOKIE), new ItemStack(Items.MUSHROOM_STEW)));
        RewardBoxManager.registerRewardBox("food3", tierRewards(
                new ItemStack(Items.CAKE), new ItemStack(Items.PUMPKIN_PIE),
                new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.COOKED_BEEF)));
        RewardBoxManager.registerRewardBox("food4", tierRewards(
                new ItemStack(Items.GOLDEN_APPLE, 1, 1), new ItemStack(Items.CAKE),
                new ItemStack(Items.GOLDEN_CARROT), new ItemStack(Items.COOKED_CHICKEN)));
        // Forestry
        RewardBoxManager.registerRewardBox("forest1", tierRewards(
                new ItemStack(Items.WOODEN_AXE), new ItemStack(Blocks.SAPLING),
                new ItemStack(Items.APPLE), new ItemStack(Items.STICK)));
        RewardBoxManager.registerRewardBox("forest2", tierRewards(
                new ItemStack(Items.IRON_AXE), new ItemStack(Items.APPLE),
                new ItemStack(Blocks.SAPLING), new ItemStack(Blocks.LOG)));
        RewardBoxManager.registerRewardBox("forest3", tierRewards(
                new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.GOLDEN_APPLE),
                new ItemStack(Blocks.SAPLING), new ItemStack(Blocks.LOG)));
        // Gardens
        RewardBoxManager.registerRewardBox("gardens", tierRewards(
                new ItemStack(Items.WHEAT), new ItemStack(Items.CARROT),
                new ItemStack(Items.POTATO), new ItemStack(Items.MELON)));
        // HEE
        RewardBoxManager.registerRewardBox("hee1", tierRewards(
                new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.BLAZE_ROD),
                new ItemStack(Blocks.OBSIDIAN), new ItemStack(Items.GOLD_INGOT)));
        RewardBoxManager.registerRewardBox("hee2", tierRewards(
                new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.NETHER_STAR), new ItemStack(Blocks.OBSIDIAN)));
        // Legendary
        RewardBoxManager.registerRewardBox("legendary", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.EMERALD), new ItemStack(Items.GOLDEN_APPLE, 1, 1)));
        // Magic
        RewardBoxManager.registerRewardBox("magic1", tierRewards(
                new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.GLOWSTONE_DUST),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.ENDER_PEARL)));
        RewardBoxManager.registerRewardBox("magic2", tierRewards(
                new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD)));
        RewardBoxManager.registerRewardBox("magic3", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.BLAZE_ROD),
                new ItemStack(Items.DIAMOND), new ItemStack(Items.ENDER_PEARL)));
        RewardBoxManager.registerRewardBox("magic4", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.EMERALD),
                new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.EXPERIENCE_BOTTLE)));
        RewardBoxManager.registerRewardBox("magic5", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.GOLDEN_APPLE, 1, 1), new ItemStack(Items.ENDER_PEARL)));
        // Rail
        RewardBoxManager.registerRewardBox("rail1", tierRewards(
                new ItemStack(Items.MINECART), new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.STICK)));
        RewardBoxManager.registerRewardBox("rail2", tierRewards(
                new ItemStack(Items.MINECART), new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.DIAMOND), new ItemStack(Items.IRON_INGOT)));
        RewardBoxManager.registerRewardBox("rail3", tierRewards(
                new ItemStack(Items.MINECART), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.EMERALD), new ItemStack(Items.GOLD_INGOT)));
        // Seeds
        RewardBoxManager.registerRewardBox("seeds", tierRewards(
                new ItemStack(Items.WHEAT), new ItemStack(Items.CARROT),
                new ItemStack(Items.POTATO), new ItemStack(Items.BEETROOT)));
        // Space
        RewardBoxManager.registerRewardBox("space1", tierRewards(
                new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD),
                new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.ENDER_PEARL)));
        RewardBoxManager.registerRewardBox("space2", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.EMERALD), new ItemStack(Items.GOLDEN_APPLE)));
        RewardBoxManager.registerRewardBox("space3", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.GOLDEN_APPLE, 1, 1), new ItemStack(Blocks.OBSIDIAN)));
        // Witch
        RewardBoxManager.registerRewardBox("witch1", tierRewards(
                new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.REDSTONE),
                new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.GUNPOWDER)));
        RewardBoxManager.registerRewardBox("witch2", tierRewards(
                new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.GLOWSTONE_DUST),
                new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.ENDER_PEARL)));
        RewardBoxManager.registerRewardBox("witch3", tierRewards(
                new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.EMERALD), new ItemStack(Items.EXPERIENCE_BOTTLE)));
        RewardBoxManager.registerRewardBox("witch4", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.ENDER_PEARL)));
        RewardBoxManager.registerRewardBox("witch5", tierRewards(
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.GOLDEN_APPLE, 1, 1), new ItemStack(Blocks.OBSIDIAN)));
    }
}
