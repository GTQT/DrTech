package com.meowmel.cropQT.api;

/**
 * 源端（CropsNH）常量集中表。
 *
 * <p>数值逐项对齐 {@code com.gtnewhorizon.cropsnh} 的对应类，注释里的「来源」指向
 * 原始文件，方便复核时逐行比对。任何新代码都应优先引用本类，而不是再抄一遍字面量。
 *
 * <p>注意：{@link FarmNutrientModel}、{@code FarmType}、{@code BlockSeedBed} 目前仍各自
 * 持有同类常量，P1/P4 重构时再统一改为引用这里；在那之前两边数值必须保持一致。
 */
public final class CropConstants {

    private CropConstants() {}

    // ==================== 作物架：TileEntityCropSticks ====================

    /** 生长 tick 周期（来源：TileEntityCropSticks.TICK_RATE）。 */
    public static final int TICK_RATE = 256;
    /** 基础生长速度（来源：BASE_GROWTH_SPEED）。 */
    public static final int BASE_GROWTH_SPEED = 6;
    /** 基础养分（来源：BASE_NUTRIENT_VALUE）。 */
    public static final int BASE_NUTRIENT_VALUE = 5;

    /** 水量加成封顶值（来源：MAX_WATER_BONUS_AT）。 */
    public static final int MAX_WATER_BONUS_AT = 100;
    /** 肥量加成封顶值（来源：MAX_FERTILIZER_BONUS_AT）。 */
    public static final int MAX_FERTILIZER_BONUS_AT = 100;
    /** 见天加成（来源：SKY_ACCESS_BONUS）。 */
    public static final int SKY_ACCESS_BONUS = 2;

    /** 计入的偏好群系上限（来源：MAX_LIKED_BIOME_TAG_COUNT）。 */
    public static final int MAX_LIKED_BIOME_TAG_COUNT = 2;
    /** 每个偏好群系的养分加成（来源：LIKED_BIOME_BONUS）。 */
    public static final int LIKED_BIOME_BONUS = 14;
    /** 低湿度阈值（来源：LOW_HUMIDITY_THRESHOLD）。 */
    public static final float LOW_HUMIDITY_THRESHOLD = 0.5f;
    /** 高湿度阈值（来源：HIGH_HUMIDITY_THRESHOLD）。 */
    public static final float HIGH_HUMIDITY_THRESHOLD = 0.8f;
    /** 高湿度加成（来源：HIGH_HUMIDITY_BONUS）。 */
    public static final int HIGH_HUMIDITY_BONUS = 14;

    /** 养分点数缩放（来源：NUTRIENT_POINT_SCALE）。 */
    public static final int NUTRIENT_POINT_SCALE = 5;
    /** 每 tier 所需养分（来源：NUTRIENTS_NEEDED_PER_TIER）。 */
    public static final int NUTRIENTS_NEEDED_PER_TIER = 10;
    /** 理论最大养分分（来源：MAX_NUTRIENT_SCORE = getNutrientsPerCycle(2, 0.5, true, 100, 100) = 55）。 */
    public static final int MAX_NUTRIENT_SCORE = 55;

    /** 作物架水量存储上限（来源：MTECropManager.WATER_CAP）。 */
    public static final int WATER_STORAGE_MAX = 200;
    /** 作物架水量补充阈值（来源：WATER_THRESHOLD）。 */
    public static final int WATER_STORAGE_THRESHOLD = 180;
    /** 作物架肥量存储上限（来源：FERTILIZER_CAP）。 */
    public static final int FERTILIZER_STORAGE_MAX = 200;
    /** 作物架液肥补充阈值（来源：FERTILIZER_LIQUID_THRESHOLD）。 */
    public static final int FERTILIZER_LIQUID_THRESHOLD = 180;
    /** 作物架 WeedEX 存储上限（来源：WEEDEX_CAP）。 */
    public static final int WEEDEX_STORAGE_MAX = 150;
    /** 作物架 WeedEX 补充阈值（来源：WEEDEX_THRESHOLD）。 */
    public static final int WEEDEX_STORAGE_THRESHOLD = 75;
    /** 每次 WeedEX 施加量（来源：WEEDEX_COST）。 */
    public static final int WEEDEX_COST = 10;

    /** 种子属性最小值（来源：Constants.MIN_SEED_STAT）。 */
    public static final int MIN_SEED_STAT = 1;
    /** 种子属性最大值（来源：Constants.MAX_SEED_STAT）。 */
    public static final int MAX_SEED_STAT = 31;

    /** 手动施肥的存储上限（来源：MANUAL_FERTILIZER_MAX_STORAGE）。 */
    public static final int MANUAL_FERTILIZER_MAX_STORAGE = 100;
    /** 手动施肥的最低阈值（来源：MIN_THRESHOLD）。 */
    public static final int MANUAL_FERTILIZER_MIN_THRESHOLD = 10;

    /** 摔落践踏的最小下落距离（来源：MIN_FALL_DISTANCE_FOR_TRAMPLING）。 */
    public static final float MIN_FALL_DISTANCE_FOR_TRAMPLING = 0.75f;

    // ==================== CropManager：MTECropManager ====================

    /** 主循环周期（来源：GLOBAL_UPDATE_RATE = 2*20+10）。 */
    public static final int MANAGER_UPDATE_RATE = 50;
    /** 缓存为空时的刷新周期（来源：CACHE_REFRESH_EMPTY）。 */
    public static final int MANAGER_CACHE_REFRESH_EMPTY = 100;
    /** 缓存非空时的刷新周期（来源：CACHE_REFRESH_ANY）。 */
    public static final int MANAGER_CACHE_REFRESH_ANY = 600;

    /** 水平半径基数（来源：getHorizontalRadius = 3 + max(0, 2*tier)）。 */
    public static final int MANAGER_RADIUS_BASE = 3;
    /** 每 tier 增加的水平半径。 */
    public static final int MANAGER_RADIUS_PER_TIER = 2;
    /** 垂直半径（来源：vertical radius 2）。 */
    public static final int MANAGER_VERTICAL_RADIUS = 2;

    /** 每 tier 的收获加成概率（来源：harvestBonusChance = 0.05*tier）。 */
    public static final float MANAGER_HARVEST_BONUS_PER_TIER = 0.05f;
    /** 收获功耗除数（来源：powerUsage = maxEUInput/8）。 */
    public static final int MANAGER_HARVEST_POWER_DIVISOR = 8;
    /** 浇水/施肥/除草功耗除数（来源：powerUsageSecondary = maxEUInput/32）。 */
    public static final int MANAGER_SECONDARY_POWER_DIVISOR = 32;

    /** 水罐每 tier 容量（来源：water cap = tier*32000）。 */
    public static final int MANAGER_WATER_CAPACITY_PER_TIER = 32_000;
    /** WeedEX 罐每 tier 容量（来源：tier*750*2）。 */
    public static final int MANAGER_WEEDEX_CAPACITY_PER_TIER = 750 * 2;
    /** 液肥罐每 tier 容量（来源：tier*144*64*4）。 */
    public static final int MANAGER_LIQUID_FERTILIZER_CAPACITY_PER_TIER = 144 * 64 * 4;
    /** 内部 EU 缓冲每 tier（来源：getCapacity = 32000*tier）。 */
    public static final int MANAGER_EU_BUFFER_PER_TIER = 32_000;

    /** WeedEX 槽数（来源：WEEDEX_SLOT_COUNT）。 */
    public static final int MANAGER_WEEDEX_SLOTS = 2;
    /** 肥料槽数（来源：FERTILIZER_SLOT_COUNT）。 */
    public static final int MANAGER_FERTILIZER_SLOTS = 4;
    /** 输出槽数（来源：OUTPUT_SLOT_COUNT）。 */
    public static final int MANAGER_OUTPUT_SLOTS = 15;
    /** 电池槽数（来源：BATTERY_SLOT_COUNT）。 */
    public static final int MANAGER_BATTERY_SLOTS = 1;
    /** 总槽数（来源：TOTAL_SLOT_COUNT）。 */
    public static final int MANAGER_TOTAL_SLOTS = MANAGER_WEEDEX_SLOTS + MANAGER_FERTILIZER_SLOTS
            + MANAGER_OUTPUT_SLOTS + MANAGER_BATTERY_SLOTS;

    /** 电池槽下标（来源：SLOT_BATTERY）。 */
    public static final int MANAGER_SLOT_BATTERY = MANAGER_TOTAL_SLOTS - 1;

    // ==================== 种子机：MTESeedGenerator ====================

    /** 每点属性的液肥消耗（来源：FERTILIZER_PER_STAT）。 */
    public static final int SEED_GENERATOR_FERTILIZER_PER_STAT = 36;
    /** 基础时长（来源：BASE_RECIPE_DURATION = 8*SECONDS）。 */
    public static final int SEED_GENERATOR_DURATION = 8 * 20;
    /** 基础 EU/t（来源：BASE_RECIPE_EUT = TierEU.RECIPE_LV）。 */
    public static final int SEED_GENERATOR_EUT = 30;

    // ==================== 育种机：MTECropBreeder ====================

    /** 每 tier 的液肥消耗（来源：FERTILIZER_PER_TIER）。 */
    public static final int BREEDER_FERTILIZER_PER_TIER = 144;
    /** 每点属性的液肥消耗（来源：FERTILIZER_PER_STAT）。 */
    public static final int BREEDER_FERTILIZER_PER_STAT = 72;
    /** 成功率下限（来源：MIN_CHANCE）。 */
    public static final int BREEDER_MIN_CHANCE = 40;
    /** 成功率上限（来源：MAX_CHANCE）。 */
    public static final int BREEDER_MAX_CHANCE = 100;
    /** 每 tier 增加的成功率（来源：CHANCE_PER_TIER）。 */
    public static final int BREEDER_CHANCE_PER_TIER = 10;

    // ==================== 基因提取机：MTECropGeneExtractor ====================

    /** 物种 orb 时长（来源：DURATION_SPECIMEN = 5*MINUTES）。 */
    public static final int GENE_EXTRACTOR_DURATION_SPECIMEN = 5 * 60 * 20;
    /** 属性 orb 时长（来源：DURATION_STAT）。 */
    public static final int GENE_EXTRACTOR_DURATION_STAT = 3_000;

    // ==================== 合成机：MTECropSynthesizer ====================

    /** 每点属性的 UUM 消耗（来源：UUM_PER_STAT）。 */
    public static final int SYNTHESIZER_UUM_PER_STAT = 100;
    /** 每 tier 的 UUM 消耗（来源：UUM_PER_TIER）。 */
    public static final int SYNTHESIZER_UUM_PER_TIER = 750;
    /** 合成机电流（来源：AMPERAGE）。 */
    public static final int SYNTHESIZER_AMPERAGE = 3;
    /** 基础时长（来源：300s + 300s*ln(tier)/ln(16) 的基数）。 */
    public static final int SYNTHESIZER_BASE_DURATION = 300 * 20;
    /** 时长的对数底（来源：ln(16)）。 */
    public static final double SYNTHESIZER_DURATION_LOG_BASE = Math.log(16);

    // ==================== 工业农场：MTEIndustrialFarm ====================

    /** 每个生长周期（来源：CYCLE_DURATION = 5*SECONDS）。 */
    public static final int FARM_CYCLE_DURATION = 5 * 20;
    /** 输入/输出模式的切换期（来源：5 ticks）。 */
    public static final int FARM_MODE_SWITCH_DURATION = 5;
    /** 周期速率缩放：CYCLE_DURATION / TICK_RATE（来源：CYCLE_TICK_RATE_SCALAR）。 */
    public static final float FARM_CYCLE_TICK_RATE_SCALAR = (float) FARM_CYCLE_DURATION / TICK_RATE;

    /** 模拟水罐容量（来源：SIMULATED_WATER_STORAGE）。 */
    public static final int FARM_SIMULATED_WATER_STORAGE = 200;
    /** 有肥料单元时的模拟肥罐容量（来源：SIMULATED_FERTILIZER_STORAGE_WHEN_PROVIDED）。 */
    public static final int FARM_SIMULATED_FERTILIZER_STORAGE_PROVIDED = 200;
    /** 无肥料单元时的模拟肥罐容量（来源：SIMULATED_FERTILIZER_STORAGE_WHEN_NOT_PROVIDED）。 */
    public static final int FARM_SIMULATED_FERTILIZER_STORAGE_NOT_PROVIDED = 0;
    /** 多安能量仓上限（来源：MAX_MULTIAMP_EHATCH_AMOUNT）。 */
    public static final int FARM_MAX_MULTIAMP_HATCHES = 1;

    /** 结构最短段数（来源：MIN_SLICES）。 */
    public static final int FARM_MIN_SLICES = 1;
    /** 结构最长段数（来源：MAX_SLICES = 1 + MAX_CASING_TIER - MIN_CASING_TIER）。 */
    public static final int FARM_MAX_SLICES = 12;

    /** 生长加速单元每台加成（来源：GROWTH_SPEED_BONUS）。 */
    public static final double FARM_GAU_GROWTH_BONUS = 1.0;
    /** 肥料单元每台生长加成（来源：GROWTH_SPEED_MULTIPLIER）。 */
    public static final double FARM_FU_GROWTH_MULTIPLIER = 0.5;
    /** 肥料单元每台收获轮数加成（来源：HARVEST_ROUND_BONUS）。 */
    public static final double FARM_FU_HARVEST_BONUS = 0.5;
    /** 高级收获单元每台收获轮数加成（来源：HARVEST_ROUND_MULTIPLIER）。 */
    public static final double FARM_AHU_HARVEST_BONUS = 0.2;

    /** 种子床每 tier 的收获轮数加成（来源：BlockSeedBed.HARVEST_ROUND_BONUS）。 */
    public static final double SEED_BED_HARVEST_ROUND_BONUS_PER_TIER = 0.2;
}
