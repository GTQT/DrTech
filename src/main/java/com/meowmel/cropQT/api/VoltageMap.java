package com.meowmel.cropQT.api;

import gregtech.api.GTValues;

/**
 * CropsNH（GTNH/GT5U）与 GTQT 之间的电压档位映射。
 *
 * <h2>为什么需要这张表</h2>
 * 两个平台的 {@code V[]} 数值完全一致，但 UIV 之后的<b>名字错开一档</b>：
 *
 * <pre>
 * 索引      11     12      13
 * GTNH     UIV    UMV     UXV
 * GTQT     UIV    UXV     OpV
 * </pre>
 *
 * 所以移植时不能直接抄 {@code VoltageIndex.UMV} 这类常量名，必须<b>按数值索引</b>翻译。
 * 本类只收口这件事：数值即索引，{@link #fromCropsNH(int)} 恒等返回；名字/电压查询统一走
 * GTQT 的 {@link GTValues}。
 *
 * <p>机器档位范围也集中在这里，避免 300 多个注册循环里各写各的：
 * <ul>
 *     <li>单方块（CropManager / SeedGenerator / Breeder）：{@code LV..UXV}</li>
 *     <li>基因机器（GeneExtractor / Synthesizer）：{@code EV..UXV}</li>
 *     <li>工业农场 / 种子床 / 升级组件：{@code MV..OpV}</li>
 * </ul>
 *
 * <p>OpV 的外壳/线缆/电池在 GTQT 已存在，但 hatch 是否覆盖到 OpV 尚未逐档确认
 * （方案 D1）。若实现时确认不可用，把 {@link #FARM_MAX_TIER} 改成 {@link #FARM_FALLBACK_MAX_TIER}
 * 即可，其余代码不用动。
 */
public final class VoltageMap {

    /** CropsNH 的 UMV 索引。 */
    public static final int SOURCE_UMV = 12;
    /** CropsNH 的 UXV 索引。 */
    public static final int SOURCE_UXV = 13;

    /** 单方块最低档：LV。 */
    public static final int SINGLE_BLOCK_MIN_TIER = GTValues.LV;
    /** 单方块最高档：GTQT UXV（CropsNH UMV）。 */
    public static final int SINGLE_BLOCK_MAX_TIER = GTValues.UXV;

    /** 基因提取/合成最低档：EV。 */
    public static final int GENE_MACHINE_MIN_TIER = GTValues.EV;
    /** 基因提取/合成最高档：GTQT UXV。 */
    public static final int GENE_MACHINE_MAX_TIER = GTValues.UXV;

    /** 工业农场、种子床、升级组件最低档：MV。 */
    public static final int FARM_MIN_TIER = GTValues.MV;
    /** 工业农场、种子床、升级组件最高档：GTQT OpV（CropsNH UXV）。 */
    public static final int FARM_MAX_TIER = GTValues.OpV;
    /** OpV hatch 不可用时的降级上限：GTQT UXV。 */
    public static final int FARM_FALLBACK_MAX_TIER = GTValues.UXV;

    private VoltageMap() {
    }

    /**
     * CropsNH 档位索引 → GTQT 档位索引。
     *
     * <p>恒等映射：两边的 {@code V[]} 按索引一一对应，差异只在常量名。
     * 保留这个方法是为了让移植代码显式写出「这是源端档位」，而不是裸传一个 int。
     */
    public static int fromCropsNH(int cropsNHTier) {
        return cropsNHTier;
    }

    /** 该档位的 GTQT 名字（如 {@code "UXV"}）；越界返回 {@code "?"}。 */
    public static String name(int tier) {
        if (tier < 0 || tier >= GTValues.VN.length) {
            return "?";
        }
        return GTValues.VN[tier];
    }

    /** 该档位的配方电压（{@code V - V/16}）；越界返回 0。 */
    public static long eu(int tier) {
        if (tier < 0 || tier >= GTValues.VA.length) {
            return 0L;
        }
        return GTValues.VA[tier];
    }

    /** 该档位的满电压；越界返回 0。 */
    public static long fullEu(int tier) {
        if (tier < 0 || tier >= GTValues.V.length) {
            return 0L;
        }
        return GTValues.V[tier];
    }

    /** 档位区间内的档数，例如 MV..OpV = 12。 */
    public static int count(int minTier, int maxTier) {
        return Math.max(0, maxTier - minTier + 1);
    }

    /** 把档位夹到 {@code [minTier, maxTier]}。 */
    public static int clamp(int tier, int minTier, int maxTier) {
        return Math.max(minTier, Math.min(maxTier, tier));
    }
}
