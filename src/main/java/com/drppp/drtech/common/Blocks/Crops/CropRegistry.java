package com.drppp.drtech.common.Blocks.Crops;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 作物类型注册表
 */

public class CropRegistry {
    private static final Map<String, CropType> REGISTRY = new HashMap<>();

    public static void register(CropType crop) { REGISTRY.put(crop.getId(), crop); }
    public static CropType get(String id) { return REGISTRY.get(id); }
    public static boolean exists(String id) { return REGISTRY.containsKey(id); }
    public static Map<String, CropType> getAll() { return REGISTRY; }

    public static void registerDefaults() {
        // ==================== Tier 1 ====================
        register(new CropType.Builder("wheat").displayName("小麦").tier(1)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(15)
                .addDrop(new ItemStack(Items.WHEAT)).addDrop(new ItemStack(Items.WHEAT_SEEDS))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("potato").displayName("马铃薯").tier(1)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(15)
                .addDrop(new ItemStack(Items.POTATO))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("carrot").displayName("胡萝卜").tier(1)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(15)
                .addDrop(new ItemStack(Items.CARROT))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("pumpkin").displayName("南瓜").tier(1)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(20)
                .addDrop(new ItemStack(Items.PUMPKIN_SEEDS))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("melon").displayName("西瓜").tier(1)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(20)
                .addDrop(new ItemStack(Items.MELON, 2))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("beetroot").displayName("甜菜根").tier(1)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(16)
                .addDrop(new ItemStack(Items.BEETROOT)).addDrop(new ItemStack(Items.BEETROOT_SEEDS))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("dandelion").displayName("蒲公英").tier(1)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(14)
                .addDrop(new ItemStack(Items.DYE, 2, 11))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("rose").displayName("玫瑰").tier(1)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(14)
                .addDrop(new ItemStack(Items.DYE, 2, 1))
                .renderType(CropRenderType.CROSS).build());

        // ==================== Tier 2 ====================
        register(new CropType.Builder("reed").displayName("甘蔗").tier(2)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Items.REEDS)).waterRequirement(0.5f)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("cyazint").displayName("蓝花").tier(2)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(18)
                .addDrop(new ItemStack(Items.DYE, 2, 4))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("tulip").displayName("郁金香").tier(2)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(18)
                .addDrop(new ItemStack(Items.DYE, 2, 14))
                .renderType(CropRenderType.CROSS).build());

        // ==================== Tier 3 ====================
        register(new CropType.Builder("cocoa").displayName("可可豆").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(24)
                .addDrop(new ItemStack(Items.DYE, 2, 3))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("flax").displayName("亚麻").tier(3)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(22)
                .addDrop(new ItemStack(Items.STRING, 2))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("hyacinth").displayName("风信子").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Items.DYE, 2, 6))
                .renderType(CropRenderType.CROSS).build());

        // ==================== Tier 4 ====================
        register(new CropType.Builder("stickreed").displayName("粘性甘蔗").tier(4)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(26)
                .addDrop(new ItemStack(Items.STICK)).addDrop(new ItemStack(Items.REEDS))
                .waterRequirement(0.3f).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("blackthorn").displayName("黑荆棘").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                .addDrop(new ItemStack(Items.DYE, 2, 0))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("wonderflower").displayName("奇妙花").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(new ItemStack(Items.DYE, 2, 5))
                .renderType(CropRenderType.CROSS).build());

        // ==================== Tier 5 ====================
        register(new CropType.Builder("ferru").displayName("铁叶草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(new ItemStack(Items.IRON_NUGGET, 2))
                .requiredBlocks("minecraft:iron_block")
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("shining").displayName("闪光草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(new ItemStack(Items.GLOWSTONE_DUST, 2))
                .lightRequirement(12).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("nether_wart_crop").displayName("地狱疣作物").tier(5)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(28)
                .addDrop(new ItemStack(Items.NETHER_WART))
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("sweet_flower").displayName("甜蜜花").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(new ItemStack(Items.SUGAR, 3))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("slimeplant").displayName("粘球草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(new ItemStack(Items.SLIME_BALL, 2))
                .renderType(CropRenderType.HASH).build());

        // ==================== Tier 6 ====================
        register(new CropType.Builder("redwheat").displayName("红石小麦").tier(6)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(32)
                .addDrop(new ItemStack(Items.REDSTONE, 2))
                .requiredBlocks("minecraft:redstone_block")
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("brown_mushroom").displayName("棕蘑菇").tier(6)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(30)
                .addDrop(new ItemStack(Blocks.BROWN_MUSHROOM, 2))
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("red_mushroom").displayName("红蘑菇").tier(6)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(30)
                .addDrop(new ItemStack(Blocks.RED_MUSHROOM, 2))
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());

        // ==================== Tier 7-9 ====================
        register(new CropType.Builder("aurelia").displayName("金叶草").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(35)
                .addDrop(new ItemStack(Items.GOLD_NUGGET, 2))
                .requiredBlocks("minecraft:gold_block")
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("dahlia").displayName("大丽金刚").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(new ItemStack(Items.DIAMOND))
                .requiredBlocks("minecraft:diamond_block").lightRequirement(12)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("green_diamond").displayName("绿金刚").tier(9)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(45)
                .addDrop(new ItemStack(Items.EMERALD))
                .requiredBlocks("minecraft:emerald_block").lightRequirement(12)
                .renderType(CropRenderType.HASH).build());
// ==================== 追加: 盆栽树 Tier 1 ====================
        register(new CropType.Builder("bonsai").displayName("盆栽树").tier(1)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(16)
                .addDrop(new ItemStack(Blocks.LOG, 4))
                .addDrop(new ItemStack(Blocks.SAPLING, 1))
                .addDrop(new ItemStack(Items.APPLE, 1))
                .renderType(CropRenderType.CROSS).build());

        // ==================== 追加: 仙人掌 Tier 2 ====================
        register(new CropType.Builder("cactus").displayName("仙人掌").tier(2)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Blocks.CACTUS, 2))
                .renderType(CropRenderType.HASH).build());

        // ==================== 追加: 皮革叶 Tier 3 ====================
        register(new CropType.Builder("leatherleaf").displayName("皮革叶").tier(3)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(new ItemStack(Items.LEATHER, 2))
                .renderType(CropRenderType.CROSS).build());

        // ==================== 追加: 碳化疣 Tier 5 ====================
        register(new CropType.Builder("carbon_wart").displayName("碳化疣").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(new ItemStack(Items.COAL, 2))
                .lightRequirement(0)
                .renderType(CropRenderType.HASH).build());

        // ==================== 追加: Tier 6 ====================
        register(new CropType.Builder("blazeflower").displayName("烈焰花").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(new ItemStack(Items.BLAZE_POWDER, 2))
                .lightRequirement(12)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("enderflower").displayName("末影花").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(new ItemStack(Items.ENDER_PEARL))
                .lightRequirement(0)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("explosivegrass").displayName("爆炸草").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(new ItemStack(Items.GUNPOWDER, 3))
                .renderType(CropRenderType.HASH).build());

        // ==================== 追加: Tier 7 ====================
        register(new CropType.Builder("tearflower").displayName("哭泣花").tier(7)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(36)
                .addDrop(new ItemStack(Items.GHAST_TEAR))
                .lightRequirement(0)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("prismaleaf").displayName("晶化叶").tier(7)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(36)
                .addDrop(new ItemStack(Items.PRISMARINE_SHARD, 2))
                .addDrop(new ItemStack(Items.PRISMARINE_CRYSTALS, 1))
                .waterRequirement(0.5f)
                .renderType(CropRenderType.HASH).build());

        // ==================== 追加: 星之疣 Tier 10 ====================
        register(new CropType.Builder("starwart").displayName("星之疣").tier(10)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(50)
                .addDrop(new ItemStack(Items.SKULL, 1, 1)) // 凋零骷髅头 meta=1
                .requiredBlocks("minecraft:nether_star_block") // 需信标基座或自定义方块
                .lightRequirement(0)
                .renderType(CropRenderType.HASH).build());
        // ==================== 杂草 ====================
        register(new CropType.Builder("weed").displayName("杂草").tier(0)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(12)
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());
    }
}
