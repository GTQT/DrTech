package com.meowmel.cropQT.api;

import com.meowmel.cropQT.api.unification.material.info.CropQTMaterialIconType;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;

/**
 * CropsNH 石中百合（{@code crops/stoneilies/*}）10 条本体。
 *
 * <p>基类 {@code CropBaseStoneLily}：tier 1、600t、阶段 1~4、石头土壤、X 形渲染、
 * 种子形状 vanilla。各子类只改掉落、底土、喜好群系，个别改 tier / 时长 / 机器等级。
 *
 * <p>掉落照源端：石头类掉 9 份对应石粉（EndStone 是 2 份、Clay 是 1 个黏土球、
 * Sand 是 4 个沙子、SoulSand 是 25% 概率 1 个）。
 */
public final class CropStoneLilies {

    private CropStoneLilies() {
    }

    private static float defaultDropChance(int tier) {
        return (float) Math.pow(0.95, tier);
    }

    public static void registerAll() {
        // 石头：石粉 ×9 —— 已在 CropParityCrops 里作为 R140「stone」写过，这里不重复登记。

        // 沙子：沙子 ×4
        CropRegistry.register(new CropType.Builder("sandLily").displayName("石中百合·沙子")
                .tier(1).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.sand)
                .addDrop(new ItemStack(Blocks.SAND, 4))
                .texturePath("stone_lilies/sand")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x87836B)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .machineBreedingRecipeTier(1)
                .likedBiomes(BiomeDictionary.Type.SANDY, BiomeDictionary.Type.DRY, BiomeDictionary.Type.HOT)
                .build());

        // 黏土：黏土球 ×1，机器 MV，850t
        CropRegistry.register(new CropType.Builder("clayLily").displayName("石中百合·黏土")
                .tier(1).growthDuration(850).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.clay)
                .addDrop(new ItemStack(Items.CLAY_BALL))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.dust, Materials.Clay))
                .duplicationCatalysts(new ItemStack(Items.CLAY_BALL))
                .texturePath("stone_lilies/clay")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x666B7F)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.WET, BiomeDictionary.Type.RIVER, BiomeDictionary.Type.SWAMP)
                .build());

        // 下界岩：下界岩粉 ×9
        CropRegistry.register(new CropType.Builder("netherStoneLily").displayName("石中百合·下界岩")
                .tier(1).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.netherrack)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.Netherrack, 9))
                .texturePath("stone_lilies/netherStone")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x911717)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .machineBreedingRecipeTier(1)
                .likedBiomes(BiomeDictionary.Type.NETHER, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY)
                .build());

        // 末地石：末地石粉 ×2，机器 MV，850t
        CropRegistry.register(new CropType.Builder("endStoneLily").displayName("石中百合·末地石")
                .tier(1).growthDuration(850).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.endStone)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.Endstone, 2))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.dust, Materials.Endstone))
                .texturePath("stone_lilies/endStone")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xABA67E)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.END, BiomeDictionary.Type.DRY, BiomeDictionary.Type.COLD)
                .build());

        // 红花岗岩：红花岗岩粉 ×9
        CropRegistry.register(new CropType.Builder("redGraniteLily").displayName("石中百合·红花岗岩")
                .tier(1).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.redGranite)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.GraniteRed, 9))
                .texturePath("stone_lilies/redGranite")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x871C52)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .machineBreedingRecipeTier(1)
                .likedBiomes(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS)
                .build());

        // 黑花岗岩：黑花岗岩粉 ×9
        CropRegistry.register(new CropType.Builder("blackGraniteLily").displayName("石中百合·黑花岗岩")
                .tier(1).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.blackGranite)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.GraniteBlack, 9))
                .texturePath("stone_lilies/blackGranite")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x000000)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .machineBreedingRecipeTier(1)
                .likedBiomes(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS)
                .build());

        // 玄武岩：玄武岩粉 ×9
        CropRegistry.register(new CropType.Builder("basaltLily").displayName("石中百合·玄武岩")
                .tier(1).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.basalt)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.Basalt, 9))
                .texturePath("stone_lilies/basalt")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x080808)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .machineBreedingRecipeTier(1)
                .likedBiomes(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS)
                .build());

        // 大理石：大理石粉 ×9
        CropRegistry.register(new CropType.Builder("marbleLily").displayName("石中百合·大理石")
                .tier(1).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.marble)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.Marble, 9))
                .texturePath("stone_lilies/marble")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xC8C8C8)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .machineBreedingRecipeTier(1)
                .likedBiomes(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS)
                .build());

        // 灵魂沙：25% 掉 1 个灵魂沙；tier 2、2000t、机器 HV
        CropRegistry.register(new CropType.Builder("soulSandLily").displayName("石中百合·灵魂沙")
                .tier(2).growthDuration(2000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.soulSand)
                .addChanceDrop(new ItemStack(Blocks.SOUL_SAND), 0.25f)
                .duplicationCatalysts(new ItemStack(Blocks.SOUL_SAND))
                .texturePath("stone_lilies/soulSand")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x281A12)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(2))
                .machineBreedingRecipeTier(3)
                .likedBiomes(BiomeDictionary.Type.NETHER, BiomeDictionary.Type.SPOOKY, BiomeDictionary.Type.SANDY)
                .build());
    }
}
