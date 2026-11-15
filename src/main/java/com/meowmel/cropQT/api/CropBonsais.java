package com.meowmel.cropQT.api;

import com.meowmel.cropQT.api.unification.material.info.CropQTMaterialIconType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;

/**
 * CropsNH 盆景树（第二批）：源端 {@code crops/abstracts/CropBonsai} 的 6 种树，
 * 其中 {@code bonsaiRubber} 依赖 IC2 的橡胶树苗/橡胶原木，按“不做联动”跳过；
 * {@code bonsaiOak} 已在 {@link CropParityCrops} 第一批实现，这里只补 5 种。
 *
 * <p>源端数值（{@code CropBonsai}）：tier 1、时长 1600、阶段 1~3、{@code dirtGrass}、
 * X 形渲染、{@code bonsai} 种子形状；掉落 = 树苗 30% + 原木×10 60%，
 * 深色橡木额外掉苹果×2 5%。
 *
 * <p>源端用 {@code Blocks.log/log2} 的 meta 区分树种；cropQT 的掉落是独立
 * {@link ItemStack}，所以这里直接带 meta。替换种子（源端 {@code addAlternateSeed}）
 * 对应 {@code BlockCropStick#initVanillaSeedMap}，树苗暂时只映射到 {@code bonsai}（旧 id）。
 */
public final class CropBonsais {

    private CropBonsais() {
    }

    /** 源端 {@code NHCropCard#getDropChance()} 的默认值：{@code 0.95^tier}。 */
    private static float defaultDropChance(int tier) {
        return (float) Math.pow(0.95, tier);
    }

    /** 登记 5 种本体的盆景树（不依赖其他模组）。 */
    public static void registerAll() {
        register("bonsaiSpruce", "云杉盆景", "bonsai_spruce", 0x50361A,
                new ItemStack(Blocks.SAPLING, 1, 1), new ItemStack(Blocks.LOG, 1, 1), false,
                BiomeDictionary.Type.FOREST, BiomeDictionary.Type.CONIFEROUS, BiomeDictionary.Type.MOUNTAIN);

        register("bonsaiBirch", "白桦盆景", "bonsai_birch", 0xCFE3BA,
                new ItemStack(Blocks.SAPLING, 1, 2), new ItemStack(Blocks.LOG, 1, 2), false,
                BiomeDictionary.Type.FOREST, BiomeDictionary.Type.HILLS);

        register("bonsaiJungle", "丛林盆景", "bonsai_jungle", 0x393D0D,
                new ItemStack(Blocks.SAPLING, 1, 3), new ItemStack(Blocks.LOG, 1, 3), false,
                BiomeDictionary.Type.HOT, BiomeDictionary.Type.JUNGLE,
                BiomeDictionary.Type.WET, BiomeDictionary.Type.DENSE);

        register("bonsaiAcacia", "金合欢盆景", "bonsai_acacia", 0x795A0D,
                new ItemStack(Blocks.SAPLING, 1, 4), new ItemStack(Blocks.LOG2, 1, 0), false,
                BiomeDictionary.Type.HOT, BiomeDictionary.Type.SPARSE,
                BiomeDictionary.Type.SAVANNA, BiomeDictionary.Type.PLAINS);

        register("bonsaiDarkOak", "深色橡木盆景", "bonsai_dark_oak", 0x684C29,
                new ItemStack(Blocks.SAPLING, 1, 5), new ItemStack(Blocks.LOG2, 1, 1), true,
                BiomeDictionary.Type.DENSE, BiomeDictionary.Type.SPOOKY, BiomeDictionary.Type.FOREST);
    }

    private static void register(String id, String displayName, String texturePath, int color,
                                 ItemStack sapling, ItemStack log, boolean dropsApple,
                                 BiomeDictionary.Type... biomes) {
        ItemStack logDrop = log.copy();
        logDrop.setCount(10);

        CropType.Builder builder = new CropType.Builder(id).displayName(displayName)
                .tier(1).growthDuration(1600).maxGrowthStage(3).harvestStage(3)
                .soil(SoilTypes.dirtGrass)
                .addChanceDrop(sapling, 0.30f)
                .addChanceDrop(logDrop, 0.60f)
                .texturePath(texturePath)
                .seedIcon(CropQTMaterialIconType.bonsai).seedColor(color)
                .renderType(CropRenderType.CROSS)
                .dropChance(defaultDropChance(1))
                .likedBiomes(biomes);

        if (dropsApple) {
            builder.addChanceDrop(new ItemStack(Items.APPLE, 2), 0.05f);
        }
        CropRegistry.register(builder.build());
    }
}
