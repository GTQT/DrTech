package com.drppp.drtech.common.Blocks.Crops;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 作物类型注册表
 */
public class CropRegistry {
    private static final Map<String, CropType> REGISTRY = new HashMap<>();

    public static void register(CropType crop) {
        REGISTRY.put(crop.getId(), crop);
    }

    public static CropType get(String id) {
        return REGISTRY.get(id);
    }

    public static boolean exists(String id) {
        return REGISTRY.containsKey(id);
    }

    public static Map<String, CropType> getAll() {
        return REGISTRY;
    }

    /**
     * 注册默认作物（仿IC2）
     */
    public static void registerDefaults() {
        // ===== Tier 1: 基础作物 =====
        register(new CropType.Builder("wheat")
            .displayName("小麦")
            .tier(1).maxGrowthStage(7).harvestStage(7)
            .addDrop(new ItemStack(Items.WHEAT, 1))
            .addDrop(new ItemStack(Items.WHEAT_SEEDS, 1))
            .lightRequirement(9)
            .build());

        register(new CropType.Builder("pumpkin")
            .displayName("南瓜")
            .tier(1).maxGrowthStage(4).harvestStage(4)
            .addDrop(new ItemStack(Items.PUMPKIN_SEEDS, 1))
            .lightRequirement(9)
            .build());

        register(new CropType.Builder("melon")
            .displayName("西瓜")
            .tier(1).maxGrowthStage(4).harvestStage(4)
            .addDrop(new ItemStack(Items.MELON, 2))
            .lightRequirement(9)
            .build());

        register(new CropType.Builder("potato")
            .displayName("马铃薯")
            .tier(1).maxGrowthStage(7).harvestStage(7)
            .addDrop(new ItemStack(Items.POTATO, 1))
            .lightRequirement(9)
            .build());

        register(new CropType.Builder("carrot")
            .displayName("胡萝卜")
            .tier(1).maxGrowthStage(7).harvestStage(7)
            .addDrop(new ItemStack(Items.CARROT, 1))
            .lightRequirement(9)
            .build());

        // ===== Tier 2 =====
        register(new CropType.Builder("reed")
            .displayName("甘蔗")
            .tier(2).maxGrowthStage(4).harvestStage(4)
            .addDrop(new ItemStack(Items.REEDS, 1))
            .lightRequirement(9).waterRequirement(0.5f)
            .build());

        register(new CropType.Builder("cyazint")
            .displayName("蓝花")
            .tier(2).maxGrowthStage(4).harvestStage(4)
            .addDrop(new ItemStack(Items.DYE, 2, 4))
            .lightRequirement(9)
            .build());

        // ===== Tier 3 =====
        register(new CropType.Builder("cocoa")
            .displayName("可可豆")
            .tier(3).maxGrowthStage(4).harvestStage(4)
            .addDrop(new ItemStack(Items.DYE, 2, 3))
            .lightRequirement(9)
            .build());

        // ===== Tier 4 =====
        register(new CropType.Builder("stickreed")
            .displayName("粘性甘蔗")
            .tier(4).maxGrowthStage(4).harvestStage(4)
            .addDrop(new ItemStack(Items.STICK, 1))
            .addDrop(new ItemStack(Items.REEDS, 1))
            .lightRequirement(9).waterRequirement(0.3f)
            .build());

        // ===== Tier 5 =====
        register(new CropType.Builder("nether_wart_crop")
            .displayName("地狱疣作物")
            .tier(5).maxGrowthStage(4).harvestStage(4)
            .addDrop(new ItemStack(Items.NETHER_WART, 1))
            .lightRequirement(0)
            .build());

        register(new CropType.Builder("ferru")
            .displayName("铁叶草")
            .tier(5).maxGrowthStage(5).harvestStage(5)
            .addDrop(new ItemStack(Items.IRON_NUGGET, 2))
            .lightRequirement(9)
            .requiredBlocks("minecraft:iron_block")
            .build());

        register(new CropType.Builder("shining")
            .displayName("闪光草")
            .tier(5).maxGrowthStage(5).harvestStage(5)
            .addDrop(new ItemStack(Items.GLOWSTONE_DUST, 2))
            .lightRequirement(12)
            .build());

        // ===== Tier 6 =====
        register(new CropType.Builder("redwheat")
            .displayName("红石小麦")
            .tier(6).maxGrowthStage(7).harvestStage(7)
            .addDrop(new ItemStack(Items.REDSTONE, 2))
            .lightRequirement(9)
            .requiredBlocks("minecraft:redstone_block")
            .build());

        // ===== Tier 7 =====
        register(new CropType.Builder("aurelia")
            .displayName("金叶草")
            .tier(7).maxGrowthStage(5).harvestStage(5)
            .addDrop(new ItemStack(Items.GOLD_NUGGET, 2))
            .lightRequirement(9)
            .requiredBlocks("minecraft:gold_block")
            .build());

        // ===== 杂草 =====
        register(new CropType.Builder("weed")
            .displayName("杂草")
            .tier(0).maxGrowthStage(5).harvestStage(5)
            .lightRequirement(0)
            .build());
    }
}
