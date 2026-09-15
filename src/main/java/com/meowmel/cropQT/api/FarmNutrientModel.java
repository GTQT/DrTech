package com.meowmel.cropQT.api;

/**
 * 工业农场的生长模拟公式。
 *
 * <p>照搬 CropsNH 的两段静态计算——世界里的作物架和农场用的是同一套数，
 * 所以「田里长得快」的组合在机器里同样长得快。
 *
 * <p>这两段计算原本是 {@code TileEntityCropSticks} 的静态方法（那里要读世界方块与储量），
 * 农场只需要传标量进来，所以单独抽出来，不依赖 `TileCropStick`。
 */
public final class FarmNutrientModel {

    /** 世界里作物架的结算周期，用作换算基准。 */
    public static final int TICK_RATE = 256;
    /** 每代基础生长速度。 */
    public static final int BASE_GROWTH_SPEED = 6;
    /** 每代基础营养点。 */
    public static final int BASE_NUTRIENT_VALUE = 5;
    /** 水量加成封顶值。 */
    public static final int MAX_WATER_BONUS_AT = 100;
    /** 肥量加成封顶值。 */
    public static final int MAX_FERTILIZER_BONUS_AT = 100;
    /** 露天加成。 */
    public static final int SKY_ACCESS_BONUS = 2;
    /** 计入加成的喜好群上限。 */
    public static final int MAX_LIKED_BIOME_TAG_COUNT = 2;
    /** 每个喜好群提供的营养点。 */
    public static final int LIKED_BIOME_BONUS = 14;
    /** 湿度加成下限。 */
    public static final float LOW_HUMIDITY_THRESHOLD = 0.5f;
    /** 湿度加成上限。 */
    public static final float HIGH_HUMIDITY_THRESHOLD = 0.8f;
    /** 营养点换算倍率。 */
    public static final int NUTRIENT_POINT_SCALE = 5;
    /** 每点 tier 需要的营养点。 */
    public static final int NUTRIENTS_NEEDED_PER_TIER = 10;

    private FarmNutrientModel() {
    }

    /**
     * 每代产出的营养点。
     *
     * @param likedBiomeTagsCount 命中的「喜好群」数量（含环境模块补的）
     * @param biomeHumidity       所在地生物群系的降雨量
     * @param canSeeSky           能否看到天空
     * @param waterStorage        模拟储水量
     * @param fertilizerStorage   模拟储肥量
     */
    public static int getNutrientsPerCycle(int likedBiomeTagsCount, float biomeHumidity, boolean canSeeSky,
                                           int waterStorage, int fertilizerStorage) {
        likedBiomeTagsCount = Math.min(MAX_LIKED_BIOME_TAG_COUNT, likedBiomeTagsCount);
        waterStorage = Math.min(MAX_WATER_BONUS_AT, waterStorage);
        fertilizerStorage = Math.min(MAX_FERTILIZER_BONUS_AT, fertilizerStorage);

        int nutrients = BASE_NUTRIENT_VALUE;
        nutrients += (waterStorage + 9) / 10;
        nutrients += (fertilizerStorage + 9) / 10;
        nutrients += canSeeSky ? SKY_ACCESS_BONUS : 0;

        // 湿度加成与喜好群加成取大者，不叠加
        float humidityBonus = (biomeHumidity - LOW_HUMIDITY_THRESHOLD) / (HIGH_HUMIDITY_THRESHOLD - LOW_HUMIDITY_THRESHOLD);
        humidityBonus = Math.max(0.0f, Math.min(1.0f, humidityBonus)) * LIKED_BIOME_BONUS;
        nutrients += Math.max((int) humidityBonus, likedBiomeTagsCount * LIKED_BIOME_BONUS);
        return nutrients;
    }

    /**
     * 每代生长速率。
     *
     * <p>营养点够 {@code tier × 10} 就按盈余加速，不够就按缺口**减速**（最低 0，即停摆）。
     *
     * @param nutrientPoints 营养点
     * @param tier           作物 tier
     * @param growth         基因组的生长属性
     */
    public static int getGrowthRate(int nutrientPoints, int tier, int growth) {
        nutrientPoints *= NUTRIENT_POINT_SCALE;
        int need = tier * NUTRIENTS_NEEDED_PER_TIER;
        if (need < 0) {
            return 0;
        }
        int baseSpeed = BASE_GROWTH_SPEED + growth;
        if (nutrientPoints >= need) {
            return baseSpeed * (100 + (nutrientPoints - need)) / 100;
        }
        return Math.max(baseSpeed * (100 - (need - nutrientPoints) * 4) / 100, 0);
    }

    /**
     * 本周期推进的生长百分比。
     *
     * @param cropGrowthDuration 作物完整生长一轮的时长（{@link CropType#getGrowthDuration()}）
     * @param growthSpeed        单代生长速率（{@link #getGrowthRate} 的结果）
     * @param cycleDuration      本周期经过的 tick 数
     * @param extraMultiplier    升级单元提供的额外倍率
     * @return 0~1 的进度增量；生长速率为 0 时返回 -1（表示长不了）
     */
    public static double getProgressPerCycle(int cropGrowthDuration, int growthSpeed, int cycleDuration,
                                             double extraMultiplier) {
        if (growthSpeed <= 0 || cropGrowthDuration <= 0) {
            return -1.0d;
        }
        // 长满一轮要几个「生长 tick」
        int growthTicksPerHarvest = (cropGrowthDuration / growthSpeed)
                + (cropGrowthDuration % growthSpeed == 0 ? 0 : 1);
        double perTick = 1.0d / growthTicksPerHarvest;
        return perTick * ((double) cycleDuration / TICK_RATE) * extraMultiplier;
    }

    /**
     * 平均掉落倍数：产量属性越高掉得越多。
     *
     * <p>这是 {@link CropStats#getYieldBonus} 的<b>期望值</b>——那个方法是
     * {@code gain/8} 再加一个 {@code (gain%8)/31} 概率的 +1。农场要的是每周期的期望产出，
     * 不能每周期掷一次骰，所以在这里把随机项摊平成概率。
     */
    public static double getAvgDropRounds(int gain) {
        return 1.0d + gain / 8.0d + (gain % 8) / 31.0d;
    }
}
