package com.drppp.drtech.api.crop;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 环境因素计算器: 光照、湿度、营养值
 */
public class EnvironmentCalculator {

    private static final Map<Block, Float> NUTRIENT_MAP = new HashMap<>();

    static {
        NUTRIENT_MAP.put(Blocks.DIRT, 0.5f);
        NUTRIENT_MAP.put(Blocks.FARMLAND, 1.0f);
        NUTRIENT_MAP.put(Blocks.GRASS, 0.6f);
        NUTRIENT_MAP.put(Blocks.MYCELIUM, 0.7f);
        NUTRIENT_MAP.put(Blocks.IRON_BLOCK, 0.8f);
        NUTRIENT_MAP.put(Blocks.GOLD_BLOCK, 0.8f);
        NUTRIENT_MAP.put(Blocks.DIAMOND_BLOCK, 0.9f);
        NUTRIENT_MAP.put(Blocks.REDSTONE_BLOCK, 0.8f);
        NUTRIENT_MAP.put(Blocks.LAPIS_BLOCK, 0.7f);
        NUTRIENT_MAP.put(Blocks.EMERALD_BLOCK, 0.9f);
        NUTRIENT_MAP.put(Blocks.SOUL_SAND, 0.4f);
        NUTRIENT_MAP.put(Blocks.END_STONE, 0.3f);
    }

    public static float calcLight(World world, BlockPos cropPos) {
        int skyLight = world.getLightFor(EnumSkyBlock.SKY, cropPos.up());
        int blockLight = world.getLightFor(EnumSkyBlock.BLOCK, cropPos.up());
        int totalLight = Math.max(skyLight, blockLight);
        return totalLight / 15.0f;
    }

    public static float calcHumidity(World world, BlockPos cropPos) {
        float humidity = 0;
        int searchRadius = 4;

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int dy = -1; dy <= 0; dy++) {
                    BlockPos checkPos = cropPos.add(dx, dy, dz);
                    IBlockState state = world.getBlockState(checkPos);
                    if (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) {
                        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                        humidity += 1.0f / (distance + 1);
                    }
                }
            }
        }

        IBlockState below = world.getBlockState(cropPos.down());
        if (below.getBlock() == Blocks.FARMLAND) {
            int moisture = below.getBlock().getMetaFromState(below);
            if (moisture > 0) {
                humidity += 0.5f;
            }
        }

        if (world.getBiome(cropPos).getRainfall() > 0) {
            humidity += 0.2f;
        }

        return Math.min(1.0f, humidity);
    }

    public static float calcNutrients(World world, BlockPos cropPos) {
        BlockPos below1 = cropPos.down();
        BlockPos below2 = cropPos.down(2);

        float nutrient = 0;
        IBlockState state1 = world.getBlockState(below1);
        IBlockState state2 = world.getBlockState(below2);

        nutrient += NUTRIENT_MAP.getOrDefault(state1.getBlock(), 0.2f);
        nutrient += NUTRIENT_MAP.getOrDefault(state2.getBlock(), 0.1f) * 0.5f;

        return Math.min(1.0f, nutrient);
    }

    /**
     * 获取作物架下方1~2格的所有方块ID(含meta)
     * 返回列表, canGrowAt中遍历匹配
     */
    public static List<String> getBlocksBelowIds(World world, BlockPos cropPos) {
        List<String> result = new ArrayList<>();
        for (int depth = 1; depth <= 2; depth++) {
            IBlockState state = world.getBlockState(cropPos.down(depth));
            Block block = state.getBlock();
            int meta = block.getMetaFromState(state);
            result.add(block.getRegistryName().toString() + ":" + meta);
            result.add(block.getRegistryName().toString());
        }
        return result;
    }

    /**
     * 检查blockBelow是否匹配requiredBlock
     * 支持两种格式:
     *   "modid:name:meta" 精确匹配(含meta)
     *   "modid:name" 只匹配方块不管meta
     */
    public static boolean matchesBlock(String actual, String required) {
        if (actual.equals(required)) return true;
        // actual是 "mod:name:meta", required是 "mod:name" -> 去掉meta比较
        String actualNoMeta = actual.substring(0, actual.lastIndexOf(':'));
        return actualNoMeta.equals(required);
    }

    public static float calcEnvironmentScore(World world, BlockPos cropPos) {
        float light = calcLight(world, cropPos);
        float humidity = calcHumidity(world, cropPos);
        float nutrients = calcNutrients(world, cropPos);
        return (light * 0.35f + humidity * 0.30f + nutrients * 0.35f);
    }

    public static int calcGrowthTicks(float envScore, CropStats cropStats, int cropTier) {
        int baseTicks = 200 + cropTier * 50;
        float envMod = 1.5f - envScore;
        float statMod = 1.0f / cropStats.getGrowthRateMultiplier();
        return Math.max(50, (int)(baseTicks * envMod * statMod));
    }
}
