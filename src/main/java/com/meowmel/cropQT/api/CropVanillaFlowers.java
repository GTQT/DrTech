package com.meowmel.cropQT.api;

import com.meowmel.cropQT.api.unification.material.info.CropQTMaterialIconType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;

/**
 * CropsNH 原版花（第二批）：源端 {@code crops/vanilla/flowers/Crop*} 的 9 种花。
 *
 * <p>源端 {@code CropVanillaFlower} 的公共值：tier 2、时长 600、{@code dirtGrass}、
 * X 形渲染、{@code flower} 种子形状；掉落 = 对应染料×1（75%）+ 对应花×1（25%），
 * 两条独立掷骰。cropQT 没有 X 枚举，按第一批的约定落到 {@code CROSS}。
 *
 * <p>{@code dandelion} 与 {@code poppy} 在 cropQT 里已有旧 id（{@code dandelion}/{@code rose}），
 * 这里按第一批“旧 id 保留 + 新 id 追加”的约定处理：{@code dandelion} 直接改数值，
 * {@code poppy} 作为新条目追加。
 */
public final class CropVanillaFlowers {

    /** 源端 {@code Constants.FLOWER_DYE_DROP_CHANCE} = 7500 / 10000。 */
    private static final float DYE_DROP_CHANCE = 0.75f;
    /** 源端 {@code Constants.FLOWER_ITEM_DROP_CHANCE} = 2500 / 10000。 */
    private static final float FLOWER_ITEM_DROP_CHANCE = 0.25f;

    private CropVanillaFlowers() {
    }

    /** 登记 8 种新花（蒲公英在 {@link CropRegistry} 里就地改）。 */
    public static void registerAll() {
        register("allium", "绒球葱", "allium", 0x6B2166, 13,
                new ItemStack(Blocks.RED_FLOWER, 1, 2),
                BiomeDictionary.Type.DRY, BiomeDictionary.Type.SANDY, BiomeDictionary.Type.SPARSE);

        register("azureBluet", "蓝花美耳草", "azure_bluet", 0x3D3D4C, 7,
                new ItemStack(Blocks.RED_FLOWER, 1, 3),
                BiomeDictionary.Type.HILLS, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.LUSH);

        register("oxeyeDaisy", "滨菊", "oxeye_daisy", 0x3D3D4C, 7,
                new ItemStack(Blocks.RED_FLOWER, 1, 8),
                BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.FOREST);

        register("pinkTulip", "粉红色郁金香", "pink_tulip", 0x80365C, 9,
                new ItemStack(Blocks.RED_FLOWER, 1, 7),
                BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.WET);

        register("redTulip", "红色郁金香", "red_tulip", 0x7A0707, 1,
                new ItemStack(Blocks.RED_FLOWER, 1, 4),
                BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.HOT);

        register("orangeTulip", "橙色郁金香", "orange_tulip", 0x92460C, 14,
                new ItemStack(Blocks.RED_FLOWER, 1, 5),
                BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.WET);

        register("whiteTulip", "白色郁金香", "white_tulip", 0x3D3D4C, 7,
                new ItemStack(Blocks.RED_FLOWER, 1, 6),
                BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.COLD);

        register("poppy", "虞美人", "poppy", 0x3A0102, 1,
                new ItemStack(Blocks.RED_FLOWER, 1, 0),
                BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.HILLS);
    }

    private static void register(String id, String displayName, String texturePath, int color,
                                 int dyeMeta, ItemStack flower, BiomeDictionary.Type... biomes) {
        CropRegistry.register(new CropType.Builder(id).displayName(displayName)
                .tier(2).growthDuration(600).maxGrowthStage(4).harvestStage(4)
                .soil(SoilTypes.dirtGrass)
                .addChanceDrop(new ItemStack(Items.DYE, 1, dyeMeta), DYE_DROP_CHANCE)
                .addChanceDrop(flower, FLOWER_ITEM_DROP_CHANCE)
                .texturePath(texturePath)
                .seedIcon(CropQTMaterialIconType.flower).seedColor(color)
                .renderType(CropRenderType.HASH)
                .dropChance((float) Math.pow(0.95, 2))
                .likedBiomes(biomes)
                .build());
    }
}
