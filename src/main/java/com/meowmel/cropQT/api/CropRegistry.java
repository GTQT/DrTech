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
     * 手写作物的 id，同时也是它们的生长贴图目录名。
     *
     * <p><b>这张表是给图集拼接用的</b>（见 {@code CropTextureStitcher}）：拼接发生在
     * preInit，那时候注册表还是空的，没法从 {@link #getAll()} 里拿。
     * 加作物时这里和 {@link #registerVanillaCrops()} 要一起改 ——
     * {@link #ensureRegistered()} 末尾会核对一遍，漏了会在日志里报出来。
     */
    public static final String[] VANILLA_CROP_IDS = {
            "weed", "wheat", "potato", "carrot", "pumpkin", "melon", "beetroot",
            "reed", "cactus", "chorus_crop", "lotus_leaf", "nether_wart_crop",
            "brown_mushroom", "red_mushroom", "dandelion", "rose", "bonsai", "cocoa",
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

        // 核对：VANILLA_CROP_IDS 是给图集拼接用的另一份清单，漏改会让那株作物的贴图拼不进图集
        for (String id : VANILLA_CROP_IDS) {
            if (!REGISTRY.containsKey(id)) {
                gregtech.api.util.GTLog.logger.error(
                        "手写作物「{}」在 VANILLA_CROP_IDS 里但没登记 —— 它的生长贴图不会进图集，游戏里会看不见",
                        id);
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
        // 空作物架上自己长出来的东西，土壤不限
        register(new CropType.Builder("weed").displayName("杂草").tier(0)
                .soil(SoilRegistry.getAllSoils())
                .maxGrowthStage(5).harvestStage(5).stageRequirement(12)
                .addChanceDrop(MetaItems.PLANT_BALL.getStackForm(), 0.5f)
                .lightRequirement(0)
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x6B8E3A).renderType(CropRenderType.CROSS).build());

        // ---------------- 农田作物 ----------------
        register(new CropType.Builder("wheat").displayName("小麦").tier(1).soil(SoilTypes.farmland)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(15)
                .addDrop(new ItemStack(Items.WHEAT)).addDrop(new ItemStack(Items.WHEAT_SEEDS))
                .seedIcon(CropQTMaterialIconType.grain).seedColor(0xD8C15A)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("potato").displayName("马铃薯").tier(1).soil(SoilTypes.farmland)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(15)
                .addDrop(new ItemStack(Items.POTATO))
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xC8A165)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("carrot").displayName("胡萝卜").tier(1).soil(SoilTypes.farmland)
                .maxGrowthStage(7).harvestStage(7).stageRequirement(15)
                .addDrop(new ItemStack(Items.CARROT))
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xE07A28)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("pumpkin").displayName("南瓜").tier(1).soil(SoilTypes.farmland)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(20)
                .addDrop(new ItemStack(Items.PUMPKIN_SEEDS))
                .addDrop(new ItemStack(Blocks.PUMPKIN))
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xE08A1E)
                .renderType(CropRenderType.CROSS).build());

        register(new CropType.Builder("melon").displayName("西瓜").tier(1).soil(SoilTypes.farmland)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(20)
                .addDrop(new ItemStack(Items.MELON, 2))
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0x6FA82F)
                .renderType(CropRenderType.CROSS).build());

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

        register(new CropType.Builder("cactus").displayName("仙人掌").tier(2).soil(SoilTypes.sand)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(22)
                .addDrop(new ItemStack(Blocks.CACTUS, 2))
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
        register(new CropType.Builder("dandelion").displayName("蒲公英").tier(1).soil(SoilTypes.dirtGrass)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(14)
                .addDrop(new ItemStack(Items.DYE, 2, 11))
                .seedIcon(CropQTMaterialIconType.flower).seedColor(0xF2D24B)
                .renderType(CropRenderType.FLOWER).build());

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

        register(new CropType.Builder("cocoa").displayName("可可豆").tier(3).soil(SoilTypes.dirtGrass)
                .maxGrowthStage(4).harvestStage(4).stageRequirement(24)
                .addDrop(new ItemStack(Items.DYE, 2, 3))
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0x8B5A2B)
                .renderType(CropRenderType.CROSS).build());
    }
}
