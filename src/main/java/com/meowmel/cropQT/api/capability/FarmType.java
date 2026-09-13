package com.meowmel.cropQT.api.capability;

import org.jetbrains.annotations.NotNull;

/**
 * 工业农场的五种升级类型。
 *
 * <p>每一种对应一种升级仓（{@link IFarmPart}）。农场靠<b>数仓的种类与个数</b>决定
 * 装了哪些升级，不再看结构里摆的方块——所以这些类型是注册式的，
 * 不走 NBT、也不进存档。
 *
 * <h2>上限与耗电是类型自带的</h2>
 * 放在枚举里而不是散在农场那边，是为了「加一种新升级」只改一个地方：
 * 这里加一项、注册几个仓，农场的计数与校验自动跟上。
 *
 * <p>数值全部照搬源端，别改。
 */
public enum FarmType {

    /** 环境强化：解锁环境模块槽。 */
    ENVIRONMENTAL_ENHANCEMENT("environmental_enhancement", 2, 0.5d),
    /** 生长加速：加法提升生长速度。不限个数，受段数限制。 */
    GROWTH_ACCELERATION("growth_acceleration", -1, 1.25d),
    /** 肥料：乘法提升生长速度，并加收割轮数。 */
    FERTILIZER("fertilizer", 1, 0.5d),
    /** 高级收割：乘法提升收割轮数。 */
    ADVANCED_HARVESTING("advanced_harvesting", 2, 0.5d),
    /** 超频生长加速：拿电压与基础耗电的落差换生长速度。 */
    OVERCLOCKED_GROWTH_ACCELERATION("overclocked_growth_acceleration", 1, 0.0d);

    // ==================== 效果常量 ====================

    /** 生长加速：每个 +1.0 生长速度（加法）。 */
    public static final double GROWTH_ACCELERATION_BONUS = 1.0d;

    /** 肥料：生长速度 ×1.5。 */
    public static final double FERTILIZER_GROWTH_MULTIPLIER = 0.5d;
    /** 肥料：收割轮数 +0.5。 */
    public static final double FERTILIZER_HARVEST_ROUND_BONUS = 0.5d;

    /** 高级收割：每个收割轮数 ×1.2。 */
    public static final double ADVANCED_HARVESTING_ROUND_MULTIPLIER = 0.2d;

    // ==================== 枚举本体 ====================

    private final String name;
    /** 全农场最多装几个；{@code -1} 表示不限。 */
    private final int maxCount;
    /** 每个额外增加多少倍「基础耗电」。 */
    private final double powerIncrease;

    FarmType(String name, int maxCount, double powerIncrease) {
        this.name = name;
        this.maxCount = maxCount;
        this.powerIncrease = powerIncrease;
    }

    public @NotNull String getName() {
        return name;
    }

    /** 上限；{@code -1} 表示不限。 */
    public int getMaxCount() {
        return maxCount;
    }

    /** 有没有数量上限。 */
    public boolean isCapped() {
        return maxCount >= 0;
    }

    /** 每个额外的基础耗电倍率。 */
    public double getPowerIncrease() {
        return powerIncrease;
    }
}
