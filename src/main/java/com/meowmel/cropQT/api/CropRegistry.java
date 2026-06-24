package com.meowmel.cropQT.api;

import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.loaders.recipes.CraftingReceipe;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 作物类型注册表
 * 所有作物在 init 阶段统一注册 (此时所有 mod 物品已就绪)
 */
public class CropRegistry {
    private static final Map<String, CropType> REGISTRY = new HashMap<>();

    public static void register(CropType crop) { REGISTRY.put(crop.getId(), crop); }
    public static CropType get(String id) { return REGISTRY.get(id); }
    public static boolean exists(String id) { return REGISTRY.containsKey(id); }
    public static Map<String, CropType> getAll() { return REGISTRY; }

    public static void registerAll() {
        // ================================================================
        //  Tier 0: 杂草
        // ================================================================
        register(new CropType.Builder("weed").displayName("杂草").tier(0)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(12)
                .addChanceDrop(MetaItems.PLANT_BALL.getStackForm(), 0.5f)
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
                .addChanceDrop(new ItemStack(Blocks.LOG, 2), 0.2f)
                .addChanceDrop(new ItemStack(Items.APPLE), 0.3f)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("chorus_crop").displayName("紫颂果").tier(1)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(18)
                .addDrop(new ItemStack(Items.CHORUS_FRUIT, 2))
                .requiredBlocks(Blocks.END_STONE)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("lotus_leaf").displayName("睡莲").tier(1)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(14)
                .addDrop(new ItemStack(Blocks.WATERLILY, 1))
                .waterRequirement(0.9f)
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 2
        // ================================================================
        register(new CropType.Builder("reed").displayName("甘蔗").tier(2)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Items.REEDS)).waterRequirement(0.8f)
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
                .addDrop(new ItemStack(ItemsInit.ITEM_XP_BERRY))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("gel_grass").displayName("凝球草").tier(2)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(18)
                .addDrop(MetaItems.PLANT_BALL.getStackForm(8))
                .addChanceDrop(new ItemStack(Blocks.VINE), 0.4f)
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
                .addChanceDrop(new ItemStack(Items.STRING, 4), 0.2f)
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
                .addDrop(DrMetaItems.MILK_WART.getStackForm())
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("egg_berry").displayName("鸡蛋莓").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addChanceDrop(new ItemStack(Items.EGG, 1), 0.4f)
                .addChanceDrop(new ItemStack(Items.FEATHER, 1), 0.4f)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("rubber_grass").displayName("橡胶草").tier(3)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(22)
                .addDrop(DrMetaItems.XJC.getStackForm(4))
                .addChanceDrop(DrMetaItems.XJC.getStackForm(2), 0.3f)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("mercury_flower").displayName("水银花").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(24)
                .addDrop(CraftingReceipe.getItemStack("thaumcraft:shimmerleaf", 3L))
                //.requiredBlocks()
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("hemp_stem").displayName("大麻茎").tier(3)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(22)
                .addDrop(DrMetaItems.HEMP_STEM.getStackForm())
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("papyrus").displayName("纸莎草").tier(3)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(22)
                .addDrop(new ItemStack(Items.PAPER, 3))
                .waterRequirement(0.8f)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("meat_berry").displayName("肉芽莓").tier(3)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(24)
                .addDrop(new ItemStack(Items.BEEF, 1))
                .addChanceDrop(new ItemStack(Items.PORKCHOP), 0.3f)
                .addChanceDrop(new ItemStack(Items.CHICKEN), 0.3f)
                .requiredBlocks("thaumcraft:flesh_block")
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 4
        // ================================================================
        register(new CropType.Builder("stickreed").displayName("粘性甘蔗").tier(4)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(26)
                .addDrop(new ItemStack(Items.REEDS))
                .addDrop(CraftingReceipe.getItemStack("gregtech:meta_item_1:438", 2L))
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
                .addDrop(DrMetaItems.RAPESEED_FLOWER.getStackForm(3))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("snapdragon").displayName("金鱼草").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(DrMetaItems.SNAPDRAGON.getStackForm(3))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("fertilizer_grass").displayName("肥料草").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(new ItemStack(Items.DYE, 3, 15))
                .addChanceDrop(MetaItems.FERTILIZER.getStackForm(2), 0.2f)
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
                .requiredBlocks(Blocks.WATER)
                .waterRequirement(0.5f).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("canola_flower").displayName("油菜花").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(DrMetaItems.CANOLA_FLOWER.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("hops").displayName("啤酒花").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(DrMetaItems.HOPS.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("indigo_blossom").displayName("靛蓝花").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(24)
                .addDrop(DrMetaItems.INDIGO_BLOSSOM.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        // 神秘莓系列 (6种)
        {
            ItemStack crystal_essence = CraftingReceipe.getItemStack("thaumcraft:crystal_essence");
            ItemStack ignis = crystal_essence.copy(); DrtechUtils.addAspectsToItemStack(ignis, "ignis", 1);
            ItemStack aer = crystal_essence.copy(); DrtechUtils.addAspectsToItemStack(aer, "aer", 1);
            ItemStack aqua = crystal_essence.copy(); DrtechUtils.addAspectsToItemStack(aqua, "aqua", 1);
            ItemStack terra = crystal_essence.copy(); DrtechUtils.addAspectsToItemStack(terra, "terra", 1);
            ItemStack ordo = crystal_essence.copy(); DrtechUtils.addAspectsToItemStack(ordo, "ordo", 1);
            ItemStack perditio = crystal_essence.copy(); DrtechUtils.addAspectsToItemStack(perditio, "perditio", 1);

            register(new CropType.Builder("mystical_order").displayName("秩序神秘莓").tier(4)
                    .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                    .addDrop(ordo)
                    .renderType(CropRenderType.CROSS).build());
            register(new CropType.Builder("mystical_chaos").displayName("混沌神秘莓").tier(4)
                    .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                    .addDrop(perditio)
                    .renderType(CropRenderType.CROSS).build());
            register(new CropType.Builder("mystical_wind").displayName("风神秘莓").tier(4)
                    .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                    .addDrop(aer)
                    .renderType(CropRenderType.CROSS).build());
            register(new CropType.Builder("mystical_fire").displayName("火神秘莓").tier(4)
                    .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                    .addDrop(ignis)
                    .renderType(CropRenderType.CROSS).build());
            register(new CropType.Builder("mystical_water").displayName("水神秘莓").tier(4)
                    .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                    .addDrop(aqua)
                    .waterRequirement(0.3f).renderType(CropRenderType.CROSS).build());
            register(new CropType.Builder("mystical_earth").displayName("地神秘莓").tier(4)
                    .maxGrowthStage(5).harvestStage(5).stageRequirement(25)
                    .addDrop(terra)
                    .renderType(CropRenderType.CROSS).build());
        }

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
                    .addDrop(CraftingReceipe.getItemStack("botania:petal", i))
                    .renderType(CropRenderType.CROSS).build());
        }
        // 盆栽树变种
        register(new CropType.Builder("livingwood_bonsai").displayName("活木盆栽树").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(CraftingReceipe.getItemStack("<botania:livingwood>"))
                .requiredBlocks("botania:livingwood")
                .renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("livingrock_bonsai").displayName("活石盆栽树").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addDrop(CraftingReceipe.getItemStack("<botania:livingrock>"))
                .requiredBlocks("botania:livingrock")
                .renderType(CropRenderType.CROSS).build());
        // 石莲系列 (6色)
        register(new CropType.Builder("redstone_lotus").displayName("红石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addChanceDrop(Materials.GraniteRed.getItemForm(OrePrefix.dust, 5), 0.5f)
                .addChanceDrop(Materials.Granite.getItemForm(OrePrefix.dust, 5), 0.5f)
                .requiredBlocks(Blocks.OBSIDIAN)
                .renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("white_lotus").displayName("白石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addChanceDrop(Materials.Marble.getItemForm(OrePrefix.dust, 5), 0.5f)
                .addChanceDrop(Materials.Diorite.getItemForm(OrePrefix.dust, 5), 0.5f)
                .requiredBlocks(Blocks.OBSIDIAN)
                .renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("gray_lotus").displayName("灰石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addChanceDrop(Materials.Stone.getItemForm(OrePrefix.dust, 5), 0.5f)
                .addChanceDrop(Materials.Andesite.getItemForm(OrePrefix.dust, 5), 0.5f)
                .requiredBlocks(Blocks.OBSIDIAN)
                .renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("black_lotus_small").displayName("黑石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .addChanceDrop(Materials.GraniteBlack.getItemForm(OrePrefix.dust, 5), 0.5f)
                .addChanceDrop(Materials.Basalt.getItemForm(OrePrefix.dust, 5), 0.5f)
                .requiredBlocks(Blocks.OBSIDIAN)
                .renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("yellow_lotus").displayName("黄石莲").tier(4)
                .stageRequirement(26)
                .addChanceDrop(Materials.Endstone.getItemForm(OrePrefix.dust, 6), 0.5f)
                .addChanceDrop(new ItemStack(Blocks.SAND, 2), 0.5f)
                .requiredBlocks(Blocks.OBSIDIAN)
                .renderType(CropRenderType.CROSS).build());
        register(new CropType.Builder("nether_lotus").displayName("下界石莲").tier(4)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(Materials.Netherrack.getItemForm(OrePrefix.dust, 6))
                .requiredBlocks(Blocks.OBSIDIAN)
                .lightRequirement(0)
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 5
        // ================================================================
        register(new CropType.Builder("ferru").displayName("铁叶草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(new ItemStack(Items.IRON_NUGGET, 2))
                .requiredBlocks(Blocks.IRON_BLOCK)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("shining").displayName("闪光草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(new ItemStack(Items.GLOWSTONE_DUST, 2))
                .requiredBlocks(Blocks.GLOWSTONE)
                .lightRequirement(12).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("nether_wart_crop").displayName("地狱疣作物").tier(5)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(28)
                .addDrop(new ItemStack(Items.NETHER_WART))
                .requiredBlocks(Blocks.SOUL_SAND)
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
                .requiredBlocks(Blocks.COAL_BLOCK)
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("oil_berry").displayName("石油浆果").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(DrMetaItems.OIL_BERRY.getStackForm())
                .addDrop(CraftingReceipe.getItemStack("<extrabees:propolis:1>", 4L))
                .requiredBlocks("gregtech:ore_oilsands_0")
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("corruption_wart").displayName("腐化瘤").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(CraftingReceipe.getItemStack("<thaumcraft:taint_fibre>"))
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("lead_leaf").displayName("铅叶子").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(Materials.Lead.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Lead))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("salt_root").displayName("盐根").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addChanceDrop(Materials.Salt.getItemForm(OrePrefix.dust, 4), 0.5f)
                .addChanceDrop(Materials.RockSalt.getItemForm(OrePrefix.dust, 4), 0.5f)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("mica_grass").displayName("云母草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(Materials.Mica.getItemForm(OrePrefix.dust, 3))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("ascend_berry").displayName("飞升莓").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(new ItemStack(ItemsInit.ITEM_SOAR_XP_BERRY))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("arid_lotus").displayName("干旱莲").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addChanceDrop(new ItemStack(Items.CLAY_BALL, 2), 0.4f)
                .addDrop(Materials.Borax.getItemForm(OrePrefix.dust, 3))
                .waterRequirementLess(0.5f)
                .renderType(CropRenderType.CROSS).build());

        // --- MetaCrops Tier 5: 矿石叶子 ---
        register(new CropType.Builder("argentia_leaf").displayName("银叶草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(DrMetaItems.ARGENTIA_LEAF.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Silver))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("auronia_leaf").displayName("金叶草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(DrMetaItems.AURONIA_LEAF.getStackForm())
                .requiredBlocks(Blocks.GOLD_BLOCK)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("bauxia_leaf").displayName("铝土叶").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(DrMetaItems.BAUXIA_LEAF.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Aluminium))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("coppon_fiber").displayName("铜纤维草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(DrMetaItems.COPPON_FIBER.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Copper))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("ferrofern_leaf").displayName("铁蕨叶").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(DrMetaItems.FERROFERN_LEAF.getStackForm())
                .requiredBlocks(Blocks.IRON_BLOCK)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("micadia_flower").displayName("云母花").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(DrMetaItems.MICADIA_FLOWER.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("plumbilia_leaf").displayName("铅叶草").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(DrMetaItems.PLUMBILIA_LEAF.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Lead))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("salty_root").displayName("盐根").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(28)
                .addDrop(DrMetaItems.SALTY_ROOT.getStackForm())
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("tine_twig").displayName("锡枝").tier(5)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(DrMetaItems.TINE_TWIG.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Tin))
                .renderType(CropRenderType.HASH).build());

        // ================================================================
        //  Tier 6
        // ================================================================
        register(new CropType.Builder("redwheat").displayName("红石小麦").tier(6)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(32)
                .addDrop(new ItemStack(Items.REDSTONE, 2))
                .lightRequirementLess(7f)
                .requiredBlocks(Blocks.REDSTONE_BLOCK)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("brown_mushroom").displayName("棕蘑菇").tier(6)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(30)
                .addDrop(new ItemStack(Blocks.BROWN_MUSHROOM, 2))
                .lightRequirementLess(1).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("red_mushroom").displayName("红蘑菇").tier(6)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(30)
                .addDrop(new ItemStack(Blocks.RED_MUSHROOM, 2))
                .lightRequirementLess(1).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("blazeflower").displayName("烈焰花").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(new ItemStack(Items.BLAZE_POWDER, 2))
                .requiredBlocks(Blocks.MAGMA)
                .lightRequirement(12).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("enderflower").displayName("末影花").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(new ItemStack(Items.ENDER_PEARL))
                .requiredBlocks("gregtech:meta_block_compressed_26")
                .lightRequirementLess(1).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("explosivegrass").displayName("爆炸草").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(30)
                .addDrop(new ItemStack(Items.GUNPOWDER, 3))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("demon_claw").displayName("恶魔爪").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(new ItemStack(Items.QUARTZ, 3))
                .requiredBlocks(Blocks.QUARTZ_BLOCK)
                .lightRequirementLess(1).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("whale_claw").displayName("云鲸爪").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addChanceDrop(Materials.CertusQuartz.getItemForm(OrePrefix.dust, 2), 0.5f)
                .addChanceDrop(CraftingReceipe.getItemStack("<appliedenergistics2:material:2>", 2L), 0.5f)
                .requiredBlocks("gregtech:meta_block_compressed_13:6")
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("tin_leaf").displayName("锡叶子").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(Materials.Tin.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Tin))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("copper_leaf").displayName("铜叶子").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(Materials.Copper.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Copper))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("titanium_leaf").displayName("钛叶子").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(34)
                .addDrop(Materials.Titanium.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Titanium))
                .renderType(CropRenderType.HASH).build());

        // --- MetaCrops Tier 6 ---
        register(new CropType.Builder("galvania_leaf").displayName("电镀叶").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(DrMetaItems.GALVANIA_LEAF.getStackForm())
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("nickelback_leaf").displayName("镍叶草").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(DrMetaItems.NICKELBACK_LEAF.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Nickel))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("scheelinium_leaf").displayName("钨叶草").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(34)
                .addDrop(DrMetaItems.SCHEELINIUM_LEAF.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Tungsten))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("thiosulfine_flower").displayName("硫磺花").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(32)
                .addDrop(DrMetaItems.THIOSULFINE_FLOWER.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("titania_leaf").displayName("钛叶草").tier(6)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(34)
                .addDrop(DrMetaItems.TITANIA_LEAF.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Titanium))
                .renderType(CropRenderType.HASH).build());

        // ================================================================
        //  Tier 7
        // ================================================================
        register(new CropType.Builder("aurelia").displayName("金叶草").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(35)
                .addDrop(new ItemStack(Items.GOLD_NUGGET, 2))
                .requiredBlocks(Blocks.GOLD_BLOCK)
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
                .addDrop(Materials.Silver.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Silver))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("aluminum_leaf").displayName("铝叶子").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(36)
                .addDrop(Materials.Aluminium.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Aluminium))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("zinc_leaf").displayName("锌叶子").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(36)
                .addDrop(Materials.Zinc.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Zinc))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("nickel_leaf").displayName("镍叶子").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(36)
                .addDrop(Materials.Nickel.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Nickel))
                .renderType(CropRenderType.HASH).build());

        // --- MetaCrops Tier 7 ---
        register(new CropType.Builder("iridine_flower").displayName("铱花").tier(7)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(36)
                .addDrop(DrMetaItems.IRIDINE_FLOWER.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("osmianth_flower").displayName("锇花").tier(7)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(36)
                .addDrop(DrMetaItems.OSMIANTH_FLOWER.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("platina_leaf").displayName("铂叶草").tier(7)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(36)
                .addDrop(DrMetaItems.PLATINA_LEAF.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Platinum))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("stargatium_leaf").displayName("星门叶").tier(7)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(36)
                .addDrop(DrMetaItems.STARGATIUM_LEAF.getStackForm())
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("thunder_flower").displayName("雷霆花").tier(7)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(36)
                .addDrop(DrMetaItems.THUNDER_FLOWER.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 8
        // ================================================================
        register(new CropType.Builder("dahlia").displayName("大丽金刚").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(new ItemStack(Items.DIAMOND))
                .requiredBlocks(Blocks.DIAMOND_BLOCK).lightRequirement(12)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("black_lotus").displayName("黑莲花").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(CraftingReceipe.getItemStack("<botania:blacklotus>"))
                .addChanceDrop(CraftingReceipe.getItemStack("<botania:blacklotus:1>"), 0.01f)
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("thunder_grass").displayName("雷鸣草").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(Materials.Thulium.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Thulium))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("manganese_leaf").displayName("锰叶子").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(Materials.Manganese.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Manganese))
                .renderType(CropRenderType.HASH).build());

        // --- MetaCrops Tier 8 ---
        register(new CropType.Builder("bobs_yer_uncle_berry").displayName("鲍勃叔叔浆果").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.BOBS_YER_UNCLE_BERRY.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("magic_essence").displayName("魔法精华").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.MAGIC_ESSENCE.getStackForm())
                .lightRequirement(0)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("pyrolusium_leaf.0").displayName("软锰矿叶").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.PYROLUSIUM_LEAF_0.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Manganese))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("pyrolusium_leaf.1").displayName("软锰矿叶I").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.PYROLUSIUM_LEAF_1.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Manganese))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("pyrolusium_leaf.2").displayName("软锰矿叶II").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.PYROLUSIUM_LEAF_2.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Manganese))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("pyrolusium_leaf.3").displayName("软锰矿叶III").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.PYROLUSIUM_LEAF_3.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Manganese))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("pyrolusium_leaf.banana").displayName("香蕉软锰矿叶").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.PYROLUSIUM_LEAF_BANANA.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Manganese))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("pyrolusium_leaf.canada").displayName("加拿大软锰矿叶").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.PYROLUSIUM_LEAF_CANADA.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Manganese))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("pyrolusium_leaf.no_egg").displayName("无蛋软锰矿叶").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.PYROLUSIUM_LEAF_NO_EGG.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Manganese))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("reactoria_leaf").displayName("反应堆叶").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.REACTORIA_LEAF.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Uranium))
                .lightRequirement(0)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("reactoria_stem").displayName("反应堆茎").tier(8)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(40)
                .addDrop(DrMetaItems.REACTORIA_STEM.getStackForm())
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Uranium))
                .lightRequirement(0)
                .renderType(CropRenderType.HASH).build());

        // ================================================================
        //  Tier 9
        // ================================================================
        register(new CropType.Builder("green_diamond").displayName("绿金刚").tier(9)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(45)
                .addDrop(new ItemStack(Items.EMERALD))
                .requiredBlocks(Blocks.EMERALD_BLOCK).lightRequirement(12)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("lucky_clover").displayName("幸运草").tier(9)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(44)
                .addDrop(DrMetaItems.LUCKY_CLOVER.getStackForm(3))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("wither_flower").displayName("凋零花").tier(9)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(65)
                .addDrop(new ItemStack(Items.SKULL, 1, 1))
                .requiredBlocks("gregtech:meta_block_compressed_100:2")
                .lightRequirement(0).renderType(CropRenderType.CROSS).build());

        // --- MetaCrops Tier 9 ---
        register(new CropType.Builder("space_flower").displayName("太空花").tier(9)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(45)
                .addDrop(DrMetaItems.SPACE_FLOWER.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("uua_berry").displayName("UUA浆果").tier(9)
                .maxGrowthStage(6).harvestStage(6).stageRequirement(45)
                .addDrop(DrMetaItems.UUA_BERRY.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 10
        // ================================================================
        register(new CropType.Builder("starwart").displayName("星之疣").tier(10)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(80)
                .addDrop(new ItemStack(Items.NETHER_STAR))
                .requiredBlocks("gregtech:meta_block_compressed_100:2")
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("nuclear_grass").displayName("核能草").tier(10)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(50)
                .addDrop(Materials.Uranium238.getItemForm(OrePrefix.nugget, 2))
                .addChanceDrop(Materials.Uranium235.getItemForm(OrePrefix.nugget, 1), 0.1f)
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Uranium))
                .lightRequirement(0).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("bedrock_heart").displayName("基岩之心").tier(10)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(52)
                .addDrop(CraftingReceipe.getItemStack("<enderio:item_material:20>"))
                .requiredBlocks(Blocks.BEDROCK)
                .renderType(CropRenderType.HASH).build());

        // --- MetaCrops Tier 10 ---
        register(new CropType.Builder("star_wart").displayName("星之疣").tier(10)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(50)
                .addDrop(DrMetaItems.STAR_WART.getStackForm())
                .lightRequirement(0)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("uum_berry").displayName("UUM浆果").tier(10)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(50)
                .addDrop(DrMetaItems.UUM_BERRY.getStackForm())
                .renderType(CropRenderType.CROSS).build());

        // ================================================================
        //  Tier 11
        // ================================================================
        register(new CropType.Builder("space_berry").displayName("空间莓").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(55)
                .addChanceDrop(MetaItems.UU_MATER.getStackForm(), 0.05f)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("platinum_leaf").displayName("铂叶子").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(55)
                .addDrop(Materials.Platinum.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Platinum))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("tungsten_leaf").displayName("钨叶子").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(55)
                .addDrop(Materials.Tungsten.getItemForm(OrePrefix.nugget, 2))
                .requiredBlocks(MetaBlocks.COMPRESSED.get(Materials.Tungsten))
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("money_grass").displayName("多金草").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(55)
                .addDrop(new ItemStack(Blocks.GOLD_BLOCK, 4))
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("mech_brain").displayName("机械脑").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(200)
                .requiredBlocks(
                        MetaBlocks.COMPRESSED.get(Materials.Silicon),
                        MetaBlocks.COMPRESSED.get(Materials.Phosphorus),
                        MetaBlocks.COMPRESSED.get(Materials.Naquadah),
                        MetaBlocks.COMPRESSED.get(Materials.Neutronium))
                .addBlockDrop("gregtech:meta_block_compressed_6:3", MetaItems.SILICON_BOULE.getStackForm())
                .addBlockDrop("gregtech:meta_block_compressed_4:14", MetaItems.PHOSPHORUS_BOULE.getStackForm())
                .addBlockDrop("gregtech:meta_block_compressed_7:12", MetaItems.NAQUADAH_BOULE.getStackForm())
                .addBlockDrop("gregtech:meta_block_compressed_7:15", MetaItems.NEUTRONIUM_BOULE.getStackForm())
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("heartfruit").displayName("心鸣果").tier(11)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(60)
                .addDrop(CraftingReceipe.getItemStack("pollution:heartfruit"))
                .canBeBreedResult(false) // 不允许杂交产出
                .renderType(CropRenderType.CROSS).build());
    }
}
