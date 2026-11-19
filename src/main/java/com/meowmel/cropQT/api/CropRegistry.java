package com.meowmel.cropQT.api;

import com.meowmel.cropQT.api.unification.material.info.CropQTMaterialIconType;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 作物注册表。
 *
 * <p>现在有两个来源：
 * <ul>
 *     <li><b>材料驱动</b>（大多数）—— {@code CropMaterialScanner} 在
 *         {@code PostMaterialEvent} 里扫材料表生成，见 {@link com.meowmel.cropQT.api.unification.CropMaterialType}</li>
 *     <li><b>手写的原版作物</b>（{@link #registerVanillaCrops()}）—— 小麦、甘蔗这些
 *         没有对应 GT 材料的，材料驱动覆盖不到，只能手写</li>
 * </ul>
 *
 * <p>两者都往同一张表里塞，所以<b>id 不能撞</b>：材料驱动的 id 形如
 * {@code <材料名>_<外型>}，手写的用 MC 的物品名，目前没有交集。
 */
public class CropRegistry {

    /**
     * 手写作物的<b>生长贴图目录名</b>（不是作物 id）。
     *
     * <p><b>这张表是给图集拼接用的</b>（见 {@code CropTextureStitcher}）：拼接发生在
     * preInit，那时候注册表还是空的，没法从 {@link #getAll()} 里拿。
     * 目录名和作物 id 可以不同（比如 {@code sugarCane} 用 {@code sugar_cane}，
     * 石中百合用 {@code stone_lilies/stone}），以 {@link CropType#getTexturePath()} 为准。
     * 加作物时这里和 {@link #registerVanillaCrops()} 要一起改 ——
     * {@link #ensureRegistered()} 末尾会核对一遍，漏了会在日志里报出来。
     */
    public static final String[] VANILLA_CROP_IDS = {
            // 旧手写作物
            "weed", "wheat", "potato", "carrot", "pumpkin", "melon", "beetroot",
            "reed", "cactus", "chorus_crop", "lotus_leaf", "nether_wart_crop",
            "brown_mushroom", "red_mushroom", "dandelion", "rose", "bonsai", "cocoa",
            "fishing_rod_crop",
            // CropsNH 测试作物（CropParityCrops）
            "sugar_cane", "blue_orchid", "stone_lilies/stone", "olivia", "star_wart",
            "space_flower", "redstraw", "magical_nightshade", "god_of_thunder", "oil_berry",
            "zomplant", "trollplant", "bonsai_oak", "enderbloom",
            // 第二批：盆景树（CropBonsais）
            "bonsai_spruce", "bonsai_birch", "bonsai_jungle", "bonsai_acacia", "bonsai_dark_oak",
            // 第二批：原版花（CropVanillaFlowers）
            "allium", "azure_bluet", "oxeye_daisy", "pink_tulip", "red_tulip",
            "orange_tulip", "white_tulip", "poppy",
            // 第三批：矿石莓（CropOreBerries）
            "ore_berries/tin", "ore_berries/aluminium", "ore_berries/copper", "ore_berries/gold",
            "ore_berries/iron", "ore_berries/cobalt", "ore_berries/essence",
            // 第三批：石中百合（CropStoneLilies）
            "stone_lilies/sand", "stone_lilies/clay", "stone_lilies/netherStone",
            "stone_lilies/endStone", "stone_lilies/redGranite", "stone_lilies/blackGranite",
            "stone_lilies/basalt", "stone_lilies/marble", "stone_lilies/soulSand",
            // 第三批：怪物系（CropMobs）
            "goldfish", "inkbloom", "spidernip", "corpseplant", "blazereed",
            "corium", "egg_plant", "slimeplant", "creeperweed", "tearstalks",
    };

    private static final Map<String, CropType> REGISTRY = new HashMap<>();

    /** 手写表是否已经登记过。 */
    private static boolean vanillaRegistered = false;

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
     * 登记手写的原版作物。
     *
     * <p>用独立标志位而不是「表为空才登记」：材料驱动的作物在
     * {@code PostMaterialEvent} 里先跑了，那时候表已经非空，
     * 按「表为空」判断会让这份手写表永远登记不上。
     */
    public static void ensureRegistered() {
        if (vanillaRegistered) {
            return;
        }
        vanillaRegistered = true;
        registerVanillaCrops();
        CropParityCrops.registerAll();
        CropBonsais.registerAll();
        CropVanillaFlowers.registerAll();
        CropOreBerries.registerAll();
        CropStoneLilies.registerAll();
        CropMobs.registerAll();

        // 核对：VANILLA_CROP_IDS 是给图集拼接用的目录清单，漏改会让那株作物的贴图拼不进图集。
        // 表里放的是目录名，所以要拿已登记作物的 getTexturePath() 反查，而不是 id。
        java.util.Set<String> usedTexturePaths = new java.util.HashSet<>();
        for (CropType crop : REGISTRY.values()) {
            if (crop.getTexturePath() != null) {
                usedTexturePaths.add(crop.getTexturePath());
            }
        }
        for (String dir : VANILLA_CROP_IDS) {
            if (!usedTexturePaths.contains(dir)) {
                gregtech.api.util.GTLog.logger.error(
                        "贴图目录「{}」在 VANILLA_CROP_IDS 里但没有作物用它 —— 图集里会白拼一堆贴图",
                        dir);
            }
        }
    }

    /**
     * 手写的原版作物。
     *
     * <p>这些没有对应的 GT 材料，材料驱动覆盖不到，所以保留一份小表。
     *
     * <p>种子也走材质形状：{@code seedIcon} 给形状、{@code seedColor} 给颜色
     * （材质底图是灰度的，不染色出来就是灰的）。
     */
    private static void registerVanillaCrops() {

        // ---------------- 杂草 ----------------
        // 空作物架上自己长出来的东西，土壤不限。
        // 数值对齐 CropsNH CropWeed：tier 1 / 600t / 4 阶段 / 无掉落 / 不显示在 JEI
        register(new CropType.Builder("weed").displayName("杂草").tier(1)
                .soil(SoilRegistry.getAllSoils())
                .growthDuration(600).maxGrowthStage(4).harvestStage(4).stageRequirement(12)
                .lightRequirement(0)
                .hideFromNEI(true)
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x6B8E3A).renderType(CropRenderType.HASH).build());

        // ---------------- 特殊作物 ----------------
        // 钓鱼竿：产物不是固定掉落表，而是整张原版钓鱼战利品表
        // （鱼 / 垃圾 / 宝藏三选一，各自再随机）。它种在水面上，要够湿才长。
        register(new CropType.Builder("fishing_rod_crop").displayName("cropqt.crop.fishing_rod_crop.name")
                .tier(4).soil(SoilTypes.water)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(26)
                .lootTable("minecraft:gameplay/fishing")
                .waterRequirement(0.5f)
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x4A90C4)
                .renderType(CropRenderType.CROSS).build());

        // ---------------- 农田作物 ----------------
        // 以下四条的 tier / 时长 / 阶段 / 掉落 / 光照 / 渲染 / 掉落倍率对齐 CropsNH
        register(new CropType.Builder("wheat").displayName("小麦").tier(1).soil(SoilTypes.farmland)
                .growthDuration(1000).maxGrowthStage(7).harvestStage(7).stageRequirement(15)
                .addDrop(new ItemStack(Items.WHEAT))
                .lightRequirement(9)
                .dropChance(0.95f)
                .likedBiomes(net.minecraftforge.common.BiomeDictionary.Type.PLAINS,
                        net.minecraftforge.common.BiomeDictionary.Type.LUSH)
                .seedIcon(CropQTMaterialIconType.grain).seedColor(0xD8C15A)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("potato").displayName("马铃薯").tier(1).soil(SoilTypes.farmland)
                .growthDuration(800).maxGrowthStage(3).harvestStage(3).stageRequirement(15)
                .addDrop(new ItemStack(Items.POTATO))
                .lightRequirement(9)
                .dropChance(0.95f)
                .likedBiomes(net.minecraftforge.common.BiomeDictionary.Type.PLAINS,
                        net.minecraftforge.common.BiomeDictionary.Type.COLD,
                        net.minecraftforge.common.BiomeDictionary.Type.CONIFEROUS)
                .texturePath("potato")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xC0A51B)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("carrot").displayName("胡萝卜").tier(2).soil(SoilTypes.farmland)
                .growthDuration(800).maxGrowthStage(3).harvestStage(3).stageRequirement(15)
                .addDrop(new ItemStack(Items.CARROT))
                .lightRequirement(9)
                .dropChance(0.9025f)
                .likedBiomes(net.minecraftforge.common.BiomeDictionary.Type.PLAINS,
                        net.minecraftforge.common.BiomeDictionary.Type.HOT,
                        net.minecraftforge.common.BiomeDictionary.Type.SANDY)
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xE07A28)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("pumpkin").displayName("南瓜").tier(2).soil(SoilTypes.farmland)
                .growthDuration(1000).maxGrowthStage(4).harvestStage(4).stageRequirement(20)
                .addDrop(new ItemStack(Blocks.PUMPKIN))
                .lightRequirement(9)
                .dropChance(0.9025f)
                .likedBiomes(net.minecraftforge.common.BiomeDictionary.Type.PLAINS,
                        net.minecraftforge.common.BiomeDictionary.Type.WET)
                .texturePath("pumpkin")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xCAB16E)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("melon").displayName("西瓜").tier(2).soil(SoilTypes.farmland)
                .growthDuration(1200).maxGrowthStage(4).harvestStage(4).stageRequirement(20)
                .addChanceDrop(new ItemStack(Items.MELON, 4), 0.6666f)
                .addChanceDrop(new ItemStack(Blocks.MELON_BLOCK), 0.3333f)
                .lightRequirement(9)
                .dropChance(0.9025f)
                .likedBiomes(net.minecraftforge.common.BiomeDictionary.Type.WET,
                        net.minecraftforge.common.BiomeDictionary.Type.LUSH)
                .texturePath("melon")
                .seedIcon(CropQTMaterialIconType.grain).seedColor(0x2A220B)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("beetroot").displayName("甜菜根").tier(1).soil(SoilTypes.farmland)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(16)
                .addDrop(new ItemStack(Items.BEETROOT)).addDrop(new ItemStack(Items.BEETROOT_SEEDS))
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x9E2B2B)
                .renderType(CropRenderType.CROSS).build());

        // ---------------- 需要特定环境 ----------------
        register(new CropType.Builder("reed").displayName("甘蔗").tier(2).soil(SoilTypes.dirtGrass)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Items.REEDS)).waterRequirement(0.8f)
                .seedIcon(CropQTMaterialIconType.grain).seedColor(0x8FBF5A)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("cactus").displayName("仙人掌").tier(3).soil(SoilTypes.sand)
                .growthDuration(450).maxGrowthStage(3).harvestStage(3).stageRequirement(22)
                .addDrop(new ItemStack(Blocks.CACTUS))
                .lightRequirement(0)
                .dropChance(0.857375f)
                .likedBiomes(net.minecraftforge.common.BiomeDictionary.Type.SANDY,
                        net.minecraftforge.common.BiomeDictionary.Type.HOT,
                        net.minecraftforge.common.BiomeDictionary.Type.DRY)
                .seedIcon(CropQTMaterialIconType.spore).seedColor(0x4C8B3A)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("chorus_crop").displayName("紫颂果").tier(1).soil(SoilTypes.end)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(18)
                .addDrop(new ItemStack(Items.CHORUS_FRUIT, 2))
                .seedIcon(CropQTMaterialIconType.magic).seedColor(0x8B5A9E)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("lotus_leaf").displayName("睡莲").tier(1).soil(SoilTypes.water)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(14)
                .addDrop(new ItemStack(Blocks.WATERLILY, 1))
                .waterRequirement(0.9f)
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x4C9E6E)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("nether_wart_crop").displayName("地狱疣").tier(5)
                .soil(SoilTypes.soulsand)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(28)
                .addDrop(new ItemStack(Items.NETHER_WART))
                .lightRequirement(0)
                .seedIcon(CropQTMaterialIconType.spore).seedColor(0x8B1A1A).renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("brown_mushroom").displayName("棕色蘑菇").tier(6)
                .soil(SoilTypes.mushroom)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(30)
                .addDrop(new ItemStack(Blocks.BROWN_MUSHROOM, 2))
                .lightRequirementLess(1)
                .seedIcon(CropQTMaterialIconType.spore).seedColor(0x9E7B5A).renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("red_mushroom").displayName("红色蘑菇").tier(6)
                .soil(SoilTypes.mushroom)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(30)
                .addDrop(new ItemStack(Blocks.RED_MUSHROOM, 2))
                .lightRequirementLess(1)
                .seedIcon(CropQTMaterialIconType.spore).seedColor(0xD14B3A).renderType(CropRenderType.CROSS).build());

        // ---------------- 花与树 ----------------
        register(new CropType.Builder("dandelion").displayName("蒲公英").tier(2).soil(SoilTypes.dirtGrass)
                .growthDuration(600).maxGrowthStage(4).harvestStage(4).stageRequirement(14)
                .addChanceDrop(new ItemStack(Items.DYE, 1, 11), 0.75f)
                .addChanceDrop(new ItemStack(Blocks.YELLOW_FLOWER, 1, 0), 0.25f)
                .dropChance(0.9025f)
                .likedBiomes(net.minecraftforge.common.BiomeDictionary.Type.PLAINS,
                        net.minecraftforge.common.BiomeDictionary.Type.LUSH,
                        net.minecraftforge.common.BiomeDictionary.Type.FOREST)
                .texturePath("dandelion")
                .seedIcon(CropQTMaterialIconType.flower).seedColor(0x8E6B13)
                .renderType(CropRenderType.HASH).build());

        register(new CropType.Builder("rose").displayName("玫瑰").tier(1).soil(SoilTypes.dirtGrass)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(14)
                .addDrop(new ItemStack(Items.DYE, 2, 1))
                .seedIcon(CropQTMaterialIconType.flower).seedColor(0xD14B4B)
                .renderType(CropRenderType.FLOWER).build());

        register(new CropType.Builder("bonsai").displayName("盆栽树").tier(1).soil(SoilTypes.dirtGrass)
                .maxGrowthStage(5).harvestStage(5).stageRequirement(20)
                .addDrop(new ItemStack(Blocks.LOG, 4))
                .addDrop(new ItemStack(Blocks.SAPLING, 1))
                .addChanceDrop(new ItemStack(Blocks.LOG, 2), 0.2f)
                .addChanceDrop(new ItemStack(Items.APPLE), 0.3f)
                .seedIcon(CropQTMaterialIconType.bonsai).seedColor(0x5A8B3A)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("cocoa").displayName("可可豆").tier(3).soil(SoilTypes.farmland)
                .growthDuration(1700).maxGrowthStage(4).harvestStage(4).stageRequirement(24)
                .addDrop(new ItemStack(Items.DYE, 1, 3))
                .lightRequirement(9)
                .dropChance(0.857375f)
                .likedBiomes(net.minecraftforge.common.BiomeDictionary.Type.JUNGLE,
                        net.minecraftforge.common.BiomeDictionary.Type.WET,
                        net.minecraftforge.common.BiomeDictionary.Type.DENSE,
                        net.minecraftforge.common.BiomeDictionary.Type.LUSH)
                .texturePath("cocoa")
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0x734120)
                .renderType(CropRenderType.HASH).build());
    }
}
