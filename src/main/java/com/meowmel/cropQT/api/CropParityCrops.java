package com.meowmel.cropQT.api;

import com.meowmel.cropQT.api.unification.material.info.CropQTMaterialIconType;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;

/**
 * CropsNH 测试作物：源端 20 条「本体」作物的逐条复刻。
 *
 * <p>数据来源是 CropsNH 的 {@code crops/**} 与 {@code loaders/CropLoader.java}，
 * 清单见 {@code docs/cropsnh-crop-table.md}（R0xx 编号）。这里只写数据，不碰机器代码。
 *
 * <p><b>等级 / 时长 / 阶段 / 掉落权重都按源端写</b>：
 * <ul>
 *     <li>{@code tier} = {@code getTier()}；</li>
 *     <li>{@code growthDuration} = {@code getGrowthDuration()}；</li>
 *     <li>{@code maxGrowthStage} = {@code getMaxGrowthStage()}；</li>
 *     <li>掉落权重 / 10000 换成 cropQT 的概率掉落（源端每条掉落独立掷骰，cropQT 的
 *         {@code addChanceDrop} 同样是独立判定，语义一致）；</li>
 *     <li>{@code dropChance} = 源端 {@code getDropChance()}（默认 {@code 0.95^tier}）。</li>
 * </ul>
 *
 * <p><b>以下差别是有意的</b>：
 * <ul>
 *     <li>源端的 {@code MachineOnlyGrowthRequirement}（星之疣 / 太空花 / 魔法夜影）没有对应机制，
 *         这三种在世界作物架上也能长；工业农场仍按正常流程工作。</li>
 *     <li>源端三件套掉落物是 CropsNH 自定义物品（starWart / spaceFlower / magicEssence /
 *         thunderLeaf / oilBerry），本模组没有对应物品，用最接近的 GT 物品代替，
 *         每处都在下面用 {@code [替代]} 标出。</li>
 *     <li>底土里 {@code shadowmetal}（神秘时代）与 {@code space}（星系）没有对应材料/方块，
 *         这两种作物不设底土，避免硬底土模式下永远长不出来。</li>
 * </ul>
 */
public final class CropParityCrops {

    private CropParityCrops() {
    }

    /** 源端 {@code NHCropCard.getDropChance()} 的默认值：{@code 0.95^tier}。 */
    private static float defaultDropChance(int tier) {
        return (float) Math.pow(0.95, tier);
    }

    /**
     * 登记全部 20 条测试作物。
     *
     * <p>其中小麦 / 胡萝卜 / 仙人掌 / 杂草的 id 与手写表重合，直接在
     * {@link CropRegistry#registerVanillaCrops()} 里按源端数值改了，这里只补其余 16 条。
     */
    public static void registerAll() {
        // ---------------- 原版 ----------------

        // 甘蔗：源端 soil = sugarcane（沙 + 泥土），掉 2 个甘蔗，无光照要求，X 形渲染
        CropRegistry.register(new CropType.Builder("sugarCane").displayName("甘蔗")
                .tier(2).growthDuration(400).maxGrowthStage(3).harvestStage(3)
                .soil(SoilTypes.sugarcane)
                .addDrop(new ItemStack(Items.REEDS, 2))
                .lightRequirement(0)
                .texturePath("sugar_cane")
                .seedIcon(CropQTMaterialIconType.grain).seedColor(0x8FBF5A)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(2))
                .likedBiomes(BiomeDictionary.Type.WET, BiomeDictionary.Type.HOT)
                .build());

        // 地狱疣：灵魂沙、无光照要求、掉落倍率 2.0
        CropRegistry.register(new CropType.Builder("netherwart").displayName("地狱疣")
                .tier(5).growthDuration(2000).maxGrowthStage(2).harvestStage(2)
                .soil(SoilTypes.soulsand)
                .addDrop(new ItemStack(Items.NETHER_WART))
                .lightRequirement(0)
                .texturePath("nether_wart_crop")
                .seedIcon(CropQTMaterialIconType.spore).seedColor(0x8B1A1A)
                .renderType(CropRenderType.HASH)
                .dropChance(2.0f)
                .likedBiomes(BiomeDictionary.Type.NETHER, BiomeDictionary.Type.HOT)
                .build());

        // 红色蘑菇：源端 mushroom = 石头 + 泥土/草 + 菌丝；光照 <= 9
        CropRegistry.register(new CropType.Builder("redMushroom").displayName("红色蘑菇")
                .tier(1).growthDuration(400).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.mushroomAll)
                .addDrop(new ItemStack(Blocks.RED_MUSHROOM))
                .lightRequirementLess(9)
                .texturePath("red_mushroom")
                .seedIcon(CropQTMaterialIconType.spore).seedColor(0xD14B3A)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(1))
                .likedBiomes(BiomeDictionary.Type.MUSHROOM, BiomeDictionary.Type.WET,
                        BiomeDictionary.Type.SWAMP)
                .build());

        // 蓝花：染料 75% + 蓝花本体 25%，两条独立判定
        CropRegistry.register(new CropType.Builder("blueOrchid").displayName("蓝花")
                .tier(2).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.dirtGrass)
                .addChanceDrop(new ItemStack(Items.DYE, 1, 12), 0.75f)
                .addChanceDrop(new ItemStack(Blocks.RED_FLOWER, 1, 1), 0.25f)
                .lightRequirement(0)
                .texturePath("blue_orchid")
                .seedIcon(CropQTMaterialIconType.flower).seedColor(0x4A90C4)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(2))
                .likedBiomes(BiomeDictionary.Type.WET, BiomeDictionary.Type.LUSH,
                        BiomeDictionary.Type.SWAMP)
                .build());

        // ---------------- 石中百合 ----------------

        // 石中百合·石头：9 个石粉，底土 = 石头
        CropRegistry.register(new CropType.Builder("stone").displayName("石中百合（石头）")
                .tier(1).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone)
                .subSoil(SubSoilRequirements.stone)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.Stone, 9))
                .lightRequirement(0)
                .texturePath("stone_lilies/stone")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x9E9E9E)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .likedBiomes(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS)
                .build());

        // ---------------- 材料作物 ----------------

        // 橄榄石：粉 75% / 宝石 25%，底土 = 橄榄石，机器 MV
        CropRegistry.register(new CropType.Builder("olivia").displayName("橄榄石作物")
                .tier(2).growthDuration(400).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone)
                .subSoil(SubSoilRequirements.olivine)
                .addChanceDrop(OreDictUnifier.get(OrePrefix.dust, Materials.Olivine), 0.75f)
                .addChanceDrop(OreDictUnifier.get(OrePrefix.gem, Materials.Olivine), 0.25f)
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.gem, Materials.Olivine),
                        OreDictUnifier.get(OrePrefix.dust, Materials.Olivine))
                .machineBreedingRecipeTier(2)   // 源端 VoltageIndex.MV
                .lightRequirement(0)
                .texturePath("olivia")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x79CD79)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(2))
                .likedBiomes(BiomeDictionary.Type.HILLS, BiomeDictionary.Type.DRY)
                .build());

        // 星之疣：底土 = 下界之星块，机器 EV / 种子床 EV
        // [替代] 源端产物是 CropsNH 的 starWart 物品 -> 下界之星粉
        CropRegistry.register(new CropType.Builder("starWart").displayName("星之疣")
                .tier(12).growthDuration(7200).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone)
                .subSoil(SubSoilRequirements.netherStar)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.NetherStar))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.dust, Materials.NetherStar),
                        OreDictUnifier.get(OrePrefix.gem, Materials.NetherStar),
                        new ItemStack(Items.SKULL, 3, 1))
                .machineBreedingRecipeTier(4)   // 源端 VoltageIndex.EV
                .minSeedBedTier(4)
                .lightRequirement(0)
                .texturePath("star_wart")
                .seedIcon(CropQTMaterialIconType.spore).seedColor(0x8985BE)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(12))
                .likedBiomes(BiomeDictionary.Type.NETHER, BiomeDictionary.Type.DEAD)
                .build());

        // 太空花：机器 IV / 种子床 IV；源端底土是星系方块，这里不设底土
        // [替代] 源端产物是 CropsNH 的 spaceFlower 物品 -> 下界之星粉
        CropRegistry.register(new CropType.Builder("spaceFlower").displayName("太空花")
                .tier(13).growthDuration(15000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.farmland)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.NetherStar))
                .machineBreedingRecipeTier(5)   // 源端 VoltageIndex.IV
                .minSeedBedTier(5)
                .lightRequirement(0)
                .texturePath("space_flower")
                .seedIcon(CropQTMaterialIconType.flower).seedColor(0xFFC200)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(13))
                .likedBiomes(BiomeDictionary.Type.COLD, BiomeDictionary.Type.DRY,
                        BiomeDictionary.Type.DEAD)
                .build());

        // 红石作物：光照 5..12，底土 = 红石，机器 MV，掉落倍率 0.85
        CropRegistry.register(new CropType.Builder("redstraw").displayName("红石作物")
                .tier(6).growthDuration(1500).maxGrowthStage(7).harvestStage(7)
                .soil(SoilTypes.farmland)
                .subSoil(SubSoilRequirements.redstone)
                .addDrop(new ItemStack(Items.REDSTONE))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.dust, Materials.Redstone))
                .machineBreedingRecipeTier(2)   // 源端 VoltageIndex.MV
                .lightRequirementRange(5, 12)
                .texturePath("redstraw")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0xD45555)
                .renderType(CropRenderType.HASH)
                .dropChance(0.85f)
                .likedBiomes(BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.HOT)
                .build());

        // 魔法夜影：机器 HV / 种子床 HV；底土是神秘时代的 shadowmetal，本模组不联动 -> 不设底土
        // [替代] 源端产物是 CropsNH 的 magicEssence -> 下界之星粉
        CropRegistry.register(new CropType.Builder("magicalNightshade").displayName("魔法夜影")
                .tier(13).growthDuration(23436).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.farmland)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.NetherStar))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.dust, Materials.NetherStar))
                .machineBreedingRecipeTier(3)   // 源端 VoltageIndex.HV
                .minSeedBedTier(3)
                .lightRequirement(0)
                .texturePath("magical_nightshade")
                .seedIcon(CropQTMaterialIconType.magic).seedColor(0xB4009C)
                .renderType(CropRenderType.HASH)
                // 源端：0.95^tier * 0.75
                .dropChance(defaultDropChance(13) * 0.75f)
                .likedBiomes(BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.COLD)
                .build());

        // 雷神草：底土 = 钍，机器 HV / 种子床 HV
        // [替代] 源端产物是 CropsNH 的 thunderLeaf -> 钍粉
        CropRegistry.register(new CropType.Builder("godOfThunder").displayName("雷神草")
                .tier(9).growthDuration(3600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.stone)
                .subSoil(SubSoilRequirements.thorium)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.Thorium))
                .duplicationCatalysts(OreDictUnifier.get(OrePrefix.dust, Materials.Thorium))
                .machineBreedingRecipeTier(3)   // 源端 VoltageIndex.HV
                .minSeedBedTier(3)
                .lightRequirement(0)
                .texturePath("god_of_thunder")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x0C290C)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(9))
                .likedBiomes(BiomeDictionary.Type.WASTELAND, BiomeDictionary.Type.SPARSE)
                .build());

        // 油莓：土壤 = 油砂复合组，机器 MV
        // [替代] 源端产物是 CropsNH 的 oilBerry 物品 -> 油砂粉
        CropRegistry.register(new CropType.Builder("oilBerry").displayName("油莓")
                .tier(8).growthDuration(1200).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.oil)
                .addDrop(OreDictUnifier.get(OrePrefix.dust, Materials.Oilsands, 2))
                .machineBreedingRecipeTier(2)   // 源端 VoltageIndex.MV
                .lightRequirement(0)
                .texturePath("oil_berry")
                .seedIcon(CropQTMaterialIconType.oreberry).seedColor(0x333333)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(8))
                .likedBiomes(BiomeDictionary.Type.HOT, BiomeDictionary.Type.WASTELAND)
                .build());

        // 丧尸草：土壤 = 灵魂沙（源端 graveyard 在无 TiC 时就是灵魂沙）
        CropRegistry.register(new CropType.Builder("zomplant").displayName("丧尸草")
                .tier(3).growthDuration(1800).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.soulsand)
                .addChanceDrop(new ItemStack(Items.ROTTEN_FLESH), 0.995f)
                .addChanceDrop(new ItemStack(Items.SKULL, 1, 2), 0.005f)
                .lightRequirement(0)
                .texturePath("zomplant")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x5A7A3A)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(3))
                .likedBiomes(BiomeDictionary.Type.DEAD, BiomeDictionary.Type.WASTELAND)
                .build());

        // 巨魔植物：土壤 = 砖块，机器 EV
        // [替代] 源端宝石是 GT5U 的 Spinel（本模组无此材料）-> 红宝石；
        //        IC2 Plantball / Scrap -> GT 的同名物品
        CropRegistry.register(new CropType.Builder("trollplant").displayName("巨魔植物")
                .tier(6).growthDuration(4800).maxGrowthStage(8).harvestStage(8)
                .soil(SoilTypes.brick)
                .addChanceDrop(OreDictUnifier.get(OrePrefix.gem, Materials.Ruby), 0.625f)
                .addChanceDrop(OreDictUnifier.get(OrePrefix.dust, Materials.Plutonium241), 0.125f)
                .addChanceDrop(MetaItems.PLANT_BALL.getStackForm(), 0.125f)
                .addChanceDrop(MetaItems.SCRAP.getStackForm(), 0.125f)
                .duplicationCatalysts(new ItemStack(Items.BRICK, 2),
                        OreDictUnifier.get(OrePrefix.screw, Materials.Ruby, 5),
                        MetaItems.SCRAP.getStackForm(8))
                .machineBreedingRecipeTier(4)   // 源端 VoltageIndex.EV
                .lightRequirement(0)
                .texturePath("trollplant")
                // 源端 color1 = 0x000000、color2 = 0xFFFFFF：第 0 层染黑、第 1 层留白
                .seedIcon(CropQTMaterialIconType.troll).seedColor(0x000000)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(6))
                .likedBiomes(BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.SPOOKY)
                .build());

        // ---------------- 树 ----------------

        // 盆栽橡树：树苗 30% / 原木 ×10 60% / 苹果 ×2 5%
        CropRegistry.register(new CropType.Builder("bonsaiOak").displayName("盆栽橡树")
                .tier(1).growthDuration(1600).maxGrowthStage(3).harvestStage(3)
                .soil(SoilTypes.dirtGrass)
                .addChanceDrop(new ItemStack(Blocks.SAPLING, 1, 0), 0.30f)
                .addChanceDrop(new ItemStack(Blocks.LOG, 10, 0), 0.60f)
                .addChanceDrop(new ItemStack(Items.APPLE, 2), 0.05f)
                .lightRequirement(0)
                .texturePath("bonsai_oak")
                .seedIcon(CropQTMaterialIconType.bonsai).seedColor(0x5A8B3A)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .likedBiomes(BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.FOREST,
                        BiomeDictionary.Type.HILLS)
                .build());

        // ---------------- 怪物作物 ----------------

        // 末影花：底土 = 末地石，机器 HV，末影珍珠粉 62.5% + 末影珍珠 37.5%
        CropRegistry.register(new CropType.Builder("enderbloom").displayName("末影花")
                .tier(10).growthDuration(3000).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.farmland)
                .subSoil(SubSoilRequirements.endStone)
                .addChanceDrop(OreDictUnifier.get(OrePrefix.dust, Materials.EnderPearl), 0.625f)
                .addChanceDrop(new ItemStack(Items.ENDER_PEARL), 0.375f)
                .duplicationCatalysts(new ItemStack(Items.ENDER_PEARL),
                        OreDictUnifier.get(OrePrefix.dust, Materials.EnderPearl))
                .machineBreedingRecipeTier(3)   // 源端 VoltageIndex.HV
                .lightRequirement(0)
                .texturePath("enderbloom")
                .seedIcon(CropQTMaterialIconType.vanilla).seedColor(0x085245)
                .renderType(CropRenderType.HASH)
                .dropChance(defaultDropChance(10))
                .likedBiomes(BiomeDictionary.Type.END, BiomeDictionary.Type.COLD)
                .build());
    }
}
