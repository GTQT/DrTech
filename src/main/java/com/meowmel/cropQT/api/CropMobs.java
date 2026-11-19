package com.meowmel.cropQT.api;

import com.meowmel.cropQT.api.unification.material.info.CropQTMaterialIconType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;

/**
 * CropsNH 怪物系作物（{@code crops/mobs/*}）的复刻。
 *
 * <p>12 条里 {@code zomplant} / {@code enderbloom} 已在第一批写过，这里补其余 10 条。
 * 全部是原版掉落，没有跨模组依赖。
 *
 * <p><b>与源端的差异</b>：
 * <ul>
 *     <li>{@code goldfish} 源端掉自定义金鱼食物，本模组没有该物品，用生鱼代替
 *         （{@code [替代]}）；源端的“踩到会尖叫”与成长时随机尖叫是音效行为，本模组的
 *         作物架没有对应钩子，不复刻。</li>
 *     <li>{@code corpseplant} 的土壤源端是 {@code graveyard}（无 TiC 时=灵魂沙），
 *         这里直接用灵魂沙土壤。</li>
 * </ul>
 */
public final class CropMobs {

    private CropMobs() {
    }

    private static float defaultDropChance(int tier) {
        return (float) Math.pow(0.95, tier);
    }

    public static void registerAll() {
        // 金鱼：tier 4、600t、阶段 3；[替代] 生鱼
        CropRegistry.register(new CropType.Builder("goldfish").displayName("金鱼草")
                .tier(4).growthDuration(600).maxGrowthStage(3).harvestStage(3)
                .soil(SoilTypes.farmland)
                .addChanceDrop(new ItemStack(Items.FISH), 0.60f)
                .texturePath("goldfish")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xF8910F)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(4))
                .likedBiomes(BiomeDictionary.Type.RIVER, BiomeDictionary.Type.WET, BiomeDictionary.Type.SPOOKY)
                .build());

        // 墨花：tier 4、600t、阶段 4、泥土/草方块；墨囊
        CropRegistry.register(new CropType.Builder("inkbloom").displayName("墨花")
                .tier(4).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.dirtGrass)
                .addDrop(new ItemStack(Items.DYE, 1, 0))
                .texturePath("inkbloom")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x000000)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(4))
                .likedBiomes(BiomeDictionary.Type.WET, BiomeDictionary.Type.OCEAN)
                .build());

        // 蜘蛛草：tier 4、2400t；线 66.66% / 蜘蛛眼 16.67% / 蜘蛛网 16.67%
        CropRegistry.register(new CropType.Builder("spidernip").displayName("蜘蛛草")
                .tier(4).growthDuration(2400).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.farmland)
                .addChanceDrop(new ItemStack(Items.STRING), 0.6666f)
                .addChanceDrop(new ItemStack(Items.SPIDER_EYE), 0.1667f)
                .addChanceDrop(new ItemStack(Blocks.WEB), 0.1667f)
                .texturePath("spidernip")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xBFBFBF)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(4))
                .likedBiomes(BiomeDictionary.Type.WET, BiomeDictionary.Type.LUSH)
                .build());

        // 尸花：tier 5、800t；骨粉 62.5% / 腐肉 25% / 骨头 12.5%；土壤=灵魂沙
        CropRegistry.register(new CropType.Builder("corpseplant").displayName("尸花")
                .tier(5).growthDuration(800).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.soulsand)
                .addChanceDrop(new ItemStack(Items.DYE, 1, 15), 0.625f)
                .addChanceDrop(new ItemStack(Items.ROTTEN_FLESH), 0.25f)
                .addChanceDrop(new ItemStack(Items.BONE), 0.125f)
                .texturePath("corpseplant")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x551515)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(5))
                .likedBiomes(BiomeDictionary.Type.DEAD, BiomeDictionary.Type.SPOOKY)
                .build());

        // 烈焰芦苇：tier 6、3600t、下界岩、X 形；烈焰粉 75% / 烈焰棒 25%；机器 MV
        CropRegistry.register(new CropType.Builder("blazereed").displayName("烈焰芦苇")
                .tier(6).growthDuration(3600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.netherrack)
                .addChanceDrop(new ItemStack(Items.BLAZE_POWDER), 0.75f)
                .addChanceDrop(new ItemStack(Items.BLAZE_ROD), 0.25f)
                .duplicationCatalysts(new ItemStack(Items.BLAZE_POWDER, 2))
                .duplicationCatalysts(new ItemStack(Items.BLAZE_ROD))
                .texturePath("blazereed")
                .seedIcon(CropQTMaterialIconType.grain).seedColor(0xB38C00)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(6))
                .machineBreedingRecipeTier(2)
                .likedBiomes(BiomeDictionary.Type.NETHER, BiomeDictionary.Type.HOT)
                .build());

        // 皮花：tier 6、800t；皮革
        CropRegistry.register(new CropType.Builder("corium").displayName("皮花")
                .tier(6).growthDuration(800).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.farmland)
                .addDrop(new ItemStack(Items.LEATHER))
                .texturePath("corium")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x542716)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(6))
                .likedBiomes(BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.FOREST)
                .build());

        // 蛋茄：tier 6、1200t、阶段 3、光照≥9；鸡蛋 60% / 羽毛 30% / 鸡肉 10%
        CropRegistry.register(new CropType.Builder("eggPlant").displayName("蛋茄")
                .tier(6).growthDuration(1200).maxGrowthStage(3).harvestStage(3)
                .soil(SoilTypes.farmland)
                .addChanceDrop(new ItemStack(Items.EGG), 0.60f)
                .addChanceDrop(new ItemStack(Items.FEATHER), 0.30f)
                .addChanceDrop(new ItemStack(Items.CHICKEN), 0.10f)
                .lightRequirement(9)
                .texturePath("egg_plant")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xB3A57D)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(6))
                .likedBiomes(BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.FOREST)
                .build());

        // 史莱姆草：tier 6、1200t、泥土/草方块、盆景种子形状；黏液球
        CropRegistry.register(new CropType.Builder("slimeplant").displayName("史莱姆草")
                .tier(6).growthDuration(1200).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.dirtGrass)
                .addDrop(new ItemStack(Items.SLIME_BALL))
                .duplicationCatalysts(new ItemStack(Items.SLIME_BALL))
                .texturePath("slimeplant")
                .seedIcon(CropQTMaterialIconType.bonsai).seedColor(0x5A3F1E)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(6))
                .likedBiomes(BiomeDictionary.Type.WET, BiomeDictionary.Type.SWAMP)
                .build());

        // 苦力怕草：tier 7、1000t；火药
        CropRegistry.register(new CropType.Builder("creeperweed").displayName("苦力怕草")
                .tier(7).growthDuration(1000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.farmland)
                .addDrop(new ItemStack(Items.GUNPOWDER))
                .texturePath("creeperweed")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x396B37)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(7))
                .likedBiomes(BiomeDictionary.Type.DEAD, BiomeDictionary.Type.SPOOKY)
                .build());

        // 泪茎：tier 8、4800t；恶魂之泪 66.66%
        CropRegistry.register(new CropType.Builder("tearstalks").displayName("泪茎")
                .tier(8).growthDuration(4800).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.farmland)
                .addChanceDrop(new ItemStack(Items.GHAST_TEAR), 0.6666f)
                .duplicationCatalysts(new ItemStack(Items.GHAST_TEAR))
                .texturePath("tearstalks")
                .seedIcon(CropQTMaterialIconType.grain).seedColor(0xBFBFBF)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(8))
                .likedBiomes(BiomeDictionary.Type.NETHER, BiomeDictionary.Type.SPOOKY)
                .build());
    }
}
