package com.meowmel.cropQT.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 环境因素计算器：光照、湿度、营养值。
 *
 * <p>营养值来自作物架脚下的<b>土壤组</b>（{@link SoilRegistry}），没有写死的方块表——
 * 「这块地天生好不好」由 {@link ISoilList#getBaseNutrients()} 定义，
 * 「能存多少水 / 肥」由保水保肥上限定义。
 *
 * <p>底土（y-2）不参与营养计算。它是独立的门槛，见 {@link SubSoilRequirement}。
 */
public class EnvironmentCalculator {

    /** 土壤相对作物架的垂直偏移——作物架直接踩着的那一格。 */
    public static final int SOIL_DEPTH = 1;

    /** 底土相对作物架的垂直偏移，与 {@link SubSoilRequirement} 保持一致。 */
    public static final int SUB_SOIL_DEPTH = SubSoilRequirement.SUB_SOIL_DEPTH;

    /** 脚下不是任何已登记土壤组时的兜底营养值——比最贫瘠的土壤还差一点。 */
    private static final float BARREN_NUTRIENTS = 0.10f;

    private EnvironmentCalculator() {
    }

    // ==================== 光照 / 湿度 ====================

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

        IBlockState soil = soilState(world, cropPos);
        if (soil.getBlock() == Blocks.FARMLAND) {
            int moisture = soil.getBlock().getMetaFromState(soil);
            if (moisture > 0) {
                humidity += 0.5f;
            }
        }

        if (world.getBiome(cropPos).getRainfall() > 0) {
            humidity += 0.2f;
        }

        return Math.min(1.0f, humidity);
    }

    // ==================== 营养 ====================

    /**
     * 营养值（0~1）：由脚下的土壤组决定。
     *
     * <p>不是任何已登记土壤组时按 {@link #BARREN_NUTRIENTS} 处理——玩家把作物架
     * 架在钻石块上不再能换来高营养，矿物来源改由底土机制负责。
     */
    public static float calcNutrients(World world, BlockPos cropPos) {
        ISoilList soil = SoilRegistry.getSoilFor(soilState(world, cropPos));
        return soil == null ? BARREN_NUTRIENTS : soil.getBaseNutrients();
    }

    // ==================== 格子取样 ====================

    /** 作物架脚下那格（土壤）。 */
    public static IBlockState soilState(World world, BlockPos cropPos) {
        return world.getBlockState(cropPos.down(SOIL_DEPTH));
    }

    /** 再往下一格（底土）。 */
    public static IBlockState subSoilState(World world, BlockPos cropPos) {
        return world.getBlockState(cropPos.down(SUB_SOIL_DEPTH));
    }

    // ==================== 方块 ID 采集 ====================

    /**
     * 土壤那格的方块 ID，形如 {@code ["mod:name:meta", "mod:name"]}。
     *
     * <p>两种形式都给，调用方按需匹配带不带元数据。
     */
    public static List<String> getSoilId(World world, BlockPos cropPos) {
        return idsOf(soilState(world, cropPos));
    }

    /** 底土那格的方块 ID，格式同 {@link #getSoilId}。 */
    public static List<String> getSubSoilId(World world, BlockPos cropPos) {
        return idsOf(subSoilState(world, cropPos));
    }

    /**
     * 土壤 + 底土的方块 ID 合并。
     *
     * <p>给「按底下方块区分掉落」的作物用——它们不区分是哪一格，只要下方有就行。
     */
    public static List<String> getSoilAndSubSoilIds(World world, BlockPos cropPos) {
        List<String> ids = new ArrayList<>(4);
        ids.addAll(idsOf(soilState(world, cropPos)));
        ids.addAll(idsOf(subSoilState(world, cropPos)));
        return ids;
    }

    private static List<String> idsOf(IBlockState state) {
        Block block = state.getBlock();
        ResourceLocation name = block.getRegistryName();
        if (name == null) {
            return Collections.emptyList();
        }
        int meta = block.getMetaFromState(state);
        List<String> ids = new ArrayList<>(2);
        ids.add(name + ":" + meta);
        ids.add(name.toString());
        return ids;
    }

    // ==================== 汇总 ====================

    /** 储量满时的加成上限：水 30%、肥 50%，叠起来最多 1.95 倍。 */
    public static final float WATER_BONUS = 0.30f;
    public static final float FERTILIZER_BONUS = 0.50f;

    /** 不带储量加成的环境分（等于水位与肥位都是 0）。 */
    public static float calcEnvironmentScore(World world, BlockPos cropPos) {
        return calcEnvironmentScore(world, cropPos, 0f, 0f);
    }

    /**
     * 带储量加成的环境分。
     *
     * <p>水和肥都是<b>正向加成</b>：没水没肥不会掉到基础分以下，喂满了才显著加速。
     * 这是刻意的——作物不该因为断水就停摆，否则玩家离线一趟回来会发现整个农场死光。
     *
     * @param waterRatio      水位 0~1
     * @param fertilizerRatio 肥位 0~1
     */
    public static float calcEnvironmentScore(World world, BlockPos cropPos,
                                             float waterRatio, float fertilizerRatio) {
        float light = calcLight(world, cropPos);
        float humidity = calcHumidity(world, cropPos);
        float nutrients = calcNutrients(world, cropPos);
        float base = light * 0.35f + humidity * 0.30f + nutrients * 0.35f;
        return base * (1f + clamp01(waterRatio) * WATER_BONUS)
                * (1f + clamp01(fertilizerRatio) * FERTILIZER_BONUS);
    }

    private static float clamp01(float value) {
        return value < 0f ? 0f : (value > 1f ? 1f : value);
    }

    /** 由环境分与属性推算一轮生长的基准 tick 数。机器（育种机 / 工业农场）算周期用。 */
    public static int calcGrowthTicks(float envScore, CropStats cropStats, int cropTier) {
        int baseTicks = 200 + cropTier * 50;
        float envMod = 1.5f - envScore;
        float statMod = 1.0f / cropStats.getGrowthRateMultiplier();
        return Math.max(50, (int) (baseTicks * envMod * statMod));
    }
}
