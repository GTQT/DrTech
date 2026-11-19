package com.meowmel.cropQT.api;

import com.meowmel.cropQT.api.unification.material.info.CropQTMaterialIconType;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;

/**
 * CropsNH 矿石莓（{@code crops/oreBerries/*}）的复刻。
 *
 * <p>源端 12 条里，只有 7 条的本体不依赖别的模组：
 * {@code tin} / {@code aluminium} / {@code copper} / {@code gold} / {@code iron} / {@code cobalt}
 * 的底土与产物都能在 GT 里找到同名材料；{@code essence} 的底土是头颅方块、产物是 TiC 的
 * 经验莓，这里用经验瓶代替。
 *
 * <p>剩下 5 条（{@code ardite} / {@code thaumium} / {@code void} / {@code thauminite} /
 * {@code knightmetal}）的金属只存在于 TiC / 神秘时代 / 暮色森林，本模组没有对应材料，
 * 也没有可替代的方块，按“跨模组联动不做”跳过。
 *
 * <p>数值全部照源端：tier、6000t 生长时长、阶段 1~4、石头土壤、光照 ≤10、
 * 机器等级 MV、产物为对应金属的粒（源端 TiC 莓子 6 个≈6 粒，{@code cobalt} 源端本身就是 1 粒）。
 * 矿石莓自带伤害（源端 {@code getEntityDamage()=1.0F}），本模组的作物架没有踩踏伤害机制，
 * 这一条无法复刻。
 */
public final class CropOreBerries {

    private CropOreBerries() {
    }

    private static float defaultDropChance(int tier) {
        return (float) Math.pow(0.95, tier);
    }

    public static void registerAll() {
        // ---------------- 金属莓 ----------------

        // 锡：tier 4，源端掉 6 个 TiC 莓子，这里换成 6 粒锡
        CropRegistry.register(new CropType.Builder("tinOreBerry").displayName("锡矿石莓")
                .tier(4).growthDuration(6000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.tin)
                .addDrop(OreDictUnifier.get(OrePrefix.nugget, Materials.Tin, 6))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.nugget, Materials.Tin))
                .lightRequirementLess(10)
                .texturePath("ore_berries/tin")
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0x8F8F8F)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(4))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS)
                .build());

        // 铝：tier 5
        CropRegistry.register(new CropType.Builder("aluminiumOreBerry").displayName("铝矿石莓")
                .tier(5).growthDuration(6000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.aluminium)
                .addDrop(OreDictUnifier.get(OrePrefix.nugget, Materials.Aluminium, 6))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.nugget, Materials.Aluminium))
                .lightRequirementLess(10)
                .texturePath("ore_berries/aluminium")
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0x5687A3)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(5))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.COLD, BiomeDictionary.Type.DRY)
                .build());

        // 铜：tier 5
        CropRegistry.register(new CropType.Builder("copperOreBerry").displayName("铜矿石莓")
                .tier(5).growthDuration(6000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.copper)
                .addDrop(OreDictUnifier.get(OrePrefix.nugget, Materials.Copper, 6))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.nugget, Materials.Copper))
                .lightRequirementLess(10)
                .texturePath("ore_berries/copper")
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0xA84D11)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(5))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.SAVANNA, BiomeDictionary.Type.HOT, BiomeDictionary.Type.SANDY)
                .build());

        // 金：tier 5
        CropRegistry.register(new CropType.Builder("goldOreBerry").displayName("金矿石莓")
                .tier(5).growthDuration(6000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.gold)
                .addDrop(OreDictUnifier.get(OrePrefix.nugget, Materials.Gold, 6))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.nugget, Materials.Gold))
                .lightRequirementLess(10)
                .texturePath("ore_berries/gold")
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0xB3B315)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(5))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.MESA, BiomeDictionary.Type.SANDY)
                .build());

        // 铁：tier 5
        CropRegistry.register(new CropType.Builder("ironOreBerry").displayName("铁矿石莓")
                .tier(5).growthDuration(6000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.iron)
                .addDrop(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron, 6))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.nugget, Materials.Iron))
                .lightRequirementLess(10)
                .texturePath("ore_berries/iron")
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0x7D7D7D)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(5))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS)
                .build());

        // 钴：tier 5，源端直接掉 1 粒
        CropRegistry.register(new CropType.Builder("cobaltOreBerry").displayName("钴矿石莓")
                .tier(5).growthDuration(6000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.cobalt)
                .addDrop(OreDictUnifier.get(OrePrefix.nugget, Materials.Cobalt))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.nugget, Materials.Cobalt))
                .lightRequirementLess(10)
                .texturePath("ore_berries/cobalt")
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0x3737AD)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(5))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.NETHER, BiomeDictionary.Type.DRY)
                .build());

        // ---------------- 精华莓 ----------------

        // 精华：tier 5，底土是头颅方块；源端产物是 TiC 的经验莓，这里用经验瓶代替
        // [替代] 经验瓶
        CropRegistry.register(new CropType.Builder("essenceOreBerry").displayName("精华矿石莓")
                .tier(5).growthDuration(6000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone).subSoil(SubSoilRequirements.skull)
                .addDrop(new ItemStack(Items.EXPERIENCE_BOTTLE))
                .texturePath("ore_berries/essence")
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0xFF6BB3)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(5))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.DEAD)
                .build());
    }
}
