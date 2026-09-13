package com.meowmel.cropQT.block;

import com.drppp.drtech.DrTechMain;
import gregtech.api.GTValues;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * 工业农场的种子床：农场每一段中间那一行。
 *
 * <h2>它决定整个农场的等级</h2>
 * 照源端：苗床是<b>分档的方块</b>，摆上去哪一档，农场就是哪一档 ——
 * 容量、基础耗电、每轮水肥用量、收割轮数加成全都跟着它走。
 * 结构里所有苗床、以及所有升级仓，<b>必须同一档</b>，否则农场停机。
 *
 * <p>这和「农场等级 = 能量仓电压」是两码事：电压只决定<b>能喂多少功率</b>，
 * 以及升级仓档次对它的上限约束。两者之间的落差正是超频生长加速仓存在的理由。
 *
 * <h2>档位与 meta</h2>
 * meta 是枚举序号，从 MV 起算。源端的 meta 就等于 GT 档位号，但 1.7.10 的 GTNH
 * 比 1.12.2 的 GTCEu <b>多一个 UMV 档</b>，所以两边档位号在 UIV 之后会错开：
 * 我们到 UXV 为止只有 <b>11 档</b>（MV..UXV，即 2..12）。
 *
 * <p>各档数值全部照搬源端，见 {@link #getCapacity} 一带。
 */
public class BlockSeedBed extends VariantBlock<BlockSeedBed.SeedBedType> {

    /** 最低档。 */
    public static final int MIN_TIER = GTValues.MV;
    /** 最高档。GTCEu 没有 UMV，所以这里是 12 而不是源端的 13。 */
    public static final int MAX_TIER = GTValues.UXV;

    /** 每档的储水/储肥容量。 */
    private static final int[] CAPACITY = new int[MAX_TIER - MIN_TIER + 1];
    /** 每轮的水肥消耗。 */
    private static final int[] CONSUMPTION = new int[MAX_TIER - MIN_TIER + 1];

    /** 生产模式一个周期占多少个生长 tick 的比例。与农场的 100/256 对齐。 */
    private static final double CYCLE_TICK_RATE_SCALAR = 100.0d / 256.0d;

    /** 每档收割轮数加成 = 档位 × 这个数。 */
    public static final double HARVEST_ROUND_BONUS = 0.2d;

    static {
        for (int i = 0; i < CAPACITY.length; i++) {
            int tier = MIN_TIER + i;
            // 源端：半径 3 + 2×档位，容量是这片的面积
            int radius = 3 + Math.max(0, 2 * tier);
            int diameter = radius * 2 + 1;
            CAPACITY[i] = diameter * diameter;
            // 消耗按容量折算到一个周期上
            CONSUMPTION[i] = (int) Math.ceil(CAPACITY[i] * CYCLE_TICK_RATE_SCALAR);
        }
    }

    public BlockSeedBed() {
        super(Material.IRON);
        setTranslationKey("drtech.seed_bed");
        setHardness(3.0f);
        setResistance(6.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(SeedBedType.values()[0]));
        setRegistryName("seed_bed");
        setCreativeTab(DrTechMain.DrTechTab);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
                                    @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    // ==================== 各档数值 ====================

    private static int index(int tier) {
        return Math.max(0, Math.min(CAPACITY.length - 1, tier - MIN_TIER));
    }

    /** 该档的储水/储肥上限。 */
    public static int getCapacity(int tier) {
        return CAPACITY[index(tier)];
    }

    /** 该档每轮的耗水；也是耗肥——源端两者恒等。 */
    public static int getWaterConsumption(int tier) {
        return CONSUMPTION[index(tier)];
    }

    public static int getFertilizerConsumption(int tier) {
        return getWaterConsumption(tier);
    }

    /** 该档的基础耗电。源端用 {@code VP}，本仓库那个数组叫 {@code VA}，数值完全一样。 */
    public static long getBaseEUt(int tier) {
        return GTValues.VA[Math.max(0, Math.min(GTValues.VA.length - 1, tier))];
    }

    /** 该档的收割轮数加成（加法）。{@code tier × 0.2}，从 MV 起算就是 +0.4。 */
    public static double getHarvestRoundBonus(int tier) {
        return tier * HARVEST_ROUND_BONUS;
    }

    /** 这一档的农场应该有多长（几段）。档次越高要求越长。 */
    public static int getMultiLength(int tier) {
        return tier - MIN_TIER + 1;
    }

    // ==================== 枚举 ====================

    /** 11 档，序号即 meta。 */
    public enum SeedBedType implements IStringSerializable {
        MV("mv"), HV("hv"), EV("ev"), IV("iv"), LUV("luv"), ZPM("zpm"),
        UV("uv"), UHV("uhv"), UEV("uev"), UIV("uiv"), UXV("uxv");

        private final String name;

        SeedBedType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getName() {
            return this.name;
        }

        /** 对应的 GT 电压档。 */
        public int getTier() {
            return MIN_TIER + ordinal();
        }
    }
}
