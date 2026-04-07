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

    // 木棍占位符: 代替尚未添加的mod物品
    private static ItemStack placeholder(int count) { return new ItemStack(Items.STICK, count); }
    private static ItemStack placeholder() { return placeholder(1); }

    public static void registerDefaults() {
        // ================================================================
        //  Tier 0: 杂草
        // ================================================================
        register(new CropType.Builder("weed").displayName("杂草").tier(0)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(12)
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 1
        // ================================================================
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
                .addDrop(new ItemStack(Blocks.PUMPKIN))
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

        register(new CropType.Builder("bonsai").displayName("盆栽树").tier(1)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(20)
                .addDrop(new ItemStack(Blocks.LOG, 4))
                .addDrop(new ItemStack(Blocks.SAPLING, 1))
                .addChanceDrop(new ItemStack(Blocks.LOG, 2),0.2f)
                .addChanceDrop(new ItemStack(Items.APPLE), 0.3f)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("chorus_crop").displayName("紫颂果").tier(1)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(18)
                .addDrop(new ItemStack(Items.CHORUS_FRUIT, 2))

                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("lotus_leaf").displayName("莲叶").tier(1)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(14)
                .addDrop(new ItemStack(Items.DYE, 2, 10)) // 绿色染料
                .waterRequirement(0.3f)
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 2
        // ================================================================
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

        register(new CropType.Builder("cactus").displayName("仙人掌").tier(2)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Blocks.CACTUS, 2))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("xp_berry").displayName("经验莓").tier(2)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(18)
                .addDrop(placeholder()) // 经验球通过代码发放
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("gel_grass").displayName("凝球草").tier(2)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(18)
                .addDrop(placeholder()) // 植物球
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 3
        // ================================================================
        register(new CropType.Builder("cocoa").displayName("可可豆").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(24)
                .addDrop(new ItemStack(Items.DYE, 2, 3))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("flax").displayName("亚麻").tier(3)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(22)
                .addDrop(new ItemStack(Items.STRING, 4))
                .addChanceDrop(new ItemStack(Items.STRING, 4),0.2f)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("hyacinth").displayName("风信子").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Items.DYE, 2, 6))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("leatherleaf").displayName("皮革叶").tier(3)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(new ItemStack(Items.LEATHER, 2))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("milk_wart").displayName("牛奶疣").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Items.MILK_BUCKET)) // 或placeholder
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("egg_berry").displayName("鸡蛋莓").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Items.EGG, 2))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("rubber_grass").displayName("橡胶草").tier(3)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(22)
                .addDrop(placeholder()) // 橡胶
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("mercury_flower").displayName("水银花").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(24)
                .addDrop(placeholder()) // 水银
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("papyrus").displayName("纸莎草").tier(3)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(22)
                .addDrop(new ItemStack(Items.PAPER, 3))
                .waterRequirement(0.3f)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("meat_berry").displayName("肉芽莓").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(24)
                .addDrop(new ItemStack(Items.BEEF, 1))
                .addChanceDrop(new ItemStack(Items.PORKCHOP), 0.3f)
                .addChanceDrop(new ItemStack(Items.CHICKEN), 0.3f)
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 4
        // ================================================================
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

        register(new CropType.Builder("rapeseed").displayName("油菜花").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(placeholder()) // 菜籽油
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("snapdragon").displayName("金鱼草").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(placeholder()) // 金鱼草产物
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("fertilizer_grass").displayName("肥料草").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(new ItemStack(Items.DYE, 4, 15)) // 骨粉
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("corpse_wart").displayName("尸骨瘤").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(new ItemStack(Items.ROTTEN_FLESH, 2))
                .addDrop(new ItemStack(Items.BONE, 1))
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("spider_bite").displayName("蜘蛛咬").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(new ItemStack(Items.SPIDER_EYE, 1))
                .addDrop(new ItemStack(Items.STRING, 2))
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("fishing_rod_crop").displayName("钓鱼竿").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .lootTable("minecraft:gameplay/fishing")
                .waterRequirement(0.5f).renderType(CropRenderType.CROSS).build());

        // 神秘莓系列 (6种)
        register(new CropType.Builder("mystical_order").displayName("秩序神秘莓").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                .addDrop(placeholder()).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("mystical_chaos").displayName("混沌神秘莓").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                .addDrop(placeholder()).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("mystical_wind").displayName("风神秘莓").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                .addDrop(placeholder()).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("mystical_fire").displayName("火神秘莓").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                .addDrop(placeholder()).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("mystical_water").displayName("水神秘莓").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                .addDrop(placeholder()).waterRequirement(0.3f).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("mystical_earth").displayName("地神秘莓").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                .addDrop(placeholder()).renderType(CropRenderType.CROSS).build());

        // 16色神秘花
        String[][] mysticColors = {
                {"white","白色"},{"black","黑色"},{"red","红色"},{"green","绿色"},
                {"brown","棕色"},{"orange","橙色"},{"magenta","品红色"},{"light_blue","淡蓝色"},
                {"yellow","黄色"},{"lime","黄绿色"},{"pink","粉色"},{"gray","灰色"},
                {"silver","淡灰"},{"cyan","青色"},{"blue","蓝色"},{"purple","紫色"}
        };
        for (int i = 0; i < mysticColors.length; i++) {
            register(new CropType.Builder("mystic_flower_" + mysticColors[i][0])
                    .displayName(mysticColors[i][1] + "神秘花").tier(4)
                    .maxGrowthStage(4).harvestStage(4).stageRequirement(24)
                    .addDrop(placeholder()) // 对应颜色的神秘花瓣
                    .renderType(CropRenderType.CROSS).build());
        }

        // 石莲系列 (6色)
        register(new CropType.Builder("redstone_lotus").displayName("红石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(new ItemStack(Items.REDSTONE, 2)).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("white_lotus").displayName("白石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(new ItemStack(Items.QUARTZ, 2)).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("gray_lotus").displayName("灰石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(new ItemStack(Items.FLINT, 2)).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("black_lotus_small").displayName("黑石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(new ItemStack(Items.COAL, 2)).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("yellow_lotus").displayName("黄石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(new ItemStack(Items.GLOWSTONE_DUST, 2)).renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("nether_lotus").displayName("下界石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(new ItemStack(Items.NETHER_WART, 2)).lightRequirement(0)
                .renderType(CropRenderType.CROSS).build());

        // 盆栽树变种
        register(new CropType.Builder("livingwood_bonsai").displayName("活木盆栽树").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(placeholder()) // 活木
                .renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("livingrock_bonsai").displayName("活石盆栽树").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(placeholder()) // 活石
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 5
        // ================================================================
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

        register(new CropType.Builder("carbon_wart").displayName("碳化疣").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(new ItemStack(Items.COAL, 2))
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("oil_berry").displayName("石油浆果").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(placeholder()) // 石油
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("corruption_wart").displayName("腐化瘤").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(placeholder()) // 腐化物
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("lead_leaf").displayName("铅叶子").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(placeholder()) // 铅粒
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("salt_root").displayName("盐根").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(placeholder()) // 盐
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("mica_grass").displayName("云母草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(placeholder()) // 云母
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("ascend_berry").displayName("飞升莓").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(placeholder()) // 飞升果
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("arid_lotus").displayName("干旱莲").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(placeholder()) // 硼砂
                .addChanceDrop(new ItemStack(Items.CLAY_BALL, 2), 0.4f) // 黏土
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 6
        // ================================================================
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

        register(new CropType.Builder("blazeflower").displayName("烈焰花").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(new ItemStack(Items.BLAZE_POWDER, 2))
                .lightRequirement(12).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("enderflower").displayName("末影花").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(new ItemStack(Items.ENDER_PEARL))
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("explosivegrass").displayName("爆炸草").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(new ItemStack(Items.GUNPOWDER, 3))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("demon_claw").displayName("恶魔爪").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(new ItemStack(Items.QUARTZ, 3)) // 下界石英
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("whale_claw").displayName("云鲸爪").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(placeholder()) // 赛特斯石英
                .addChanceDrop(placeholder(), 0.3f) // 充能赛特斯
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("tin_leaf").displayName("锡叶子").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("copper_leaf").displayName("铜叶子").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("titanium_leaf").displayName("钛叶子").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(34)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        // ================================================================
        //  Tier 7
        // ================================================================
        register(new CropType.Builder("aurelia").displayName("金叶草").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(35)
                .addDrop(new ItemStack(Items.GOLD_NUGGET, 2))
                .requiredBlocks("minecraft:gold_block")
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("tearflower").displayName("哭泣花").tier(7)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(36)
                .addDrop(new ItemStack(Items.GHAST_TEAR))
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("prismaleaf").displayName("晶化叶").tier(7)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(36)
                .addDrop(new ItemStack(Items.PRISMARINE_SHARD, 2))
                .addDrop(new ItemStack(Items.PRISMARINE_CRYSTALS, 1))
                .waterRequirement(0.5f).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("silver_leaf").displayName("银叶子").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(36)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("aluminum_leaf").displayName("铝叶子").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(36)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("zinc_leaf").displayName("锌叶子").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(36)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("nickel_leaf").displayName("镍叶子").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(36)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        // ================================================================
        //  Tier 8
        // ================================================================
        register(new CropType.Builder("dahlia").displayName("大丽金刚").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(new ItemStack(Items.DIAMOND))
                .requiredBlocks("minecraft:diamond_block").lightRequirement(12)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("black_lotus").displayName("黑莲花").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(placeholder()) // 黑莲花
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("thunder_grass").displayName("雷鸣草").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(placeholder()) // 钍
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("manganese_leaf").displayName("锰叶子").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        // ================================================================
        //  Tier 9
        // ================================================================
        register(new CropType.Builder("green_diamond").displayName("绿金刚").tier(9)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(45)
                .addDrop(new ItemStack(Items.EMERALD))
                .requiredBlocks("minecraft:emerald_block").lightRequirement(12)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("lucky_clover").displayName("幸运草").tier(9)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(44)
                .addDrop(placeholder()) // 幸运奖品
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("wither_flower").displayName("凋零花").tier(9)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(44)
                .addDrop(new ItemStack(Items.NETHER_STAR))
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 10
        // ================================================================
        register(new CropType.Builder("starwart").displayName("星之疣").tier(10)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(50)
                .addDrop(new ItemStack(Items.SKULL, 1, 1))
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("nuclear_grass").displayName("核能草").tier(10)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(50)
                .addDrop(placeholder()) // 铀238
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("bedrock_heart").displayName("基岩之心").tier(10)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(52)
                .addDrop(placeholder()) // 基岩碎片
                .renderType(CropRenderType.HASH).build());

        // ================================================================
        //  Tier 11
        // ================================================================
        register(new CropType.Builder("space_berry").displayName("空间莓").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(55)
                .addDrop(placeholder()) // 空间物质
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("platinum_leaf").displayName("铂叶子").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(55)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("tungsten_leaf").displayName("钨叶子").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(55)
                .addDrop(placeholder()).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("money_grass").displayName("多金草").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(55)
                .addDrop(placeholder()) // 货币
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("mech_brain").displayName("机械脑").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(58)
                .addDrop(placeholder()) // 电路板
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("heartfruit").displayName("心鸣果").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(60)
                .addDrop(placeholder()) // 心脏
                .canBeBreedResult(false) // 不允许杂交产出
                .renderType(CropRenderType.CROSS).build());
    }
}
