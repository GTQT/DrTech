package com.meowmel.cropQT.handler;

import com.meowmel.cropQT.api.CropRenderType;
import net.minecraftforge.common.config.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 作物系统的可调项。
 *
 * <p>挂在 {@code DrtConfig.crop} 下（分类名「作物」），配置文件仍然是 {@code config/drtech.cfg}。
 * 这里只放<b>会影响世界内行为</b>的三个旋钮：长得快慢、杂交成不成、植物长什么样。
 * 数值类的定义（哪块土保多少水、哪个作物多少 tier）属于数据，不在这里调。
 */
public class CropConfig {

    /** 渲染模式：用作物自己的，还是全局强制成一种。 */
    public enum RenderModeOverride {
        /** 用每个作物自己声明的形状。 */
        AUTO(null),
        CROSS(CropRenderType.CROSS),
        HASH(CropRenderType.HASH),
        FLOWER(CropRenderType.FLOWER);

        @Nullable
        private final CropRenderType forced;

        RenderModeOverride(@Nullable CropRenderType forced) {
            this.forced = forced;
        }

        /** 被强制成的形状；{@code AUTO} 返回 {@code null}。 */
        @Nullable
        public CropRenderType getForced() {
            return forced;
        }
    }

    @Config.Comment({
            "作物生长速度倍率。1.0 为默认速度，越大越快。",
            "影响的是每个生长周期推进的进度增量，不改变成熟所需的阶段数。",
            "取 0 会禁用生长（作物永远停在第 0 阶段）。"
    })
    @Config.Name("生长速度倍率")
    @Config.RangeDouble(min = 0.0d, max = 100.0d)
    public double growthMultiplier = 1.0d;

    @Config.Comment({
            "作物渲染形状。",
            "  AUTO   - 用每个作物自己声明的形状（默认）",
            "  CROSS  - 全部强制成十字交叉",
            "  HASH   - 全部强制成井字架状",
            "  FLOWER - 全部强制成花坛状"
    })
    @Config.Name("作物渲染形状")
    public RenderModeOverride renderMode = RenderModeOverride.AUTO;

    @Config.Comment("是否启用杂草。关闭后空架不会长草，作物也不会被杂草覆盖。")
    @Config.Name("启用杂草")
    public boolean enableWeeds = true;

    @Config.Comment("杂草是否可以覆盖已有作物。关闭后杂草只会长在空架上。")
    @Config.Name("杂草可覆盖作物")
    public boolean weedsWipePlants = true;

    @Config.Comment("空架每个生长周期长出杂草的概率分母（1/N）。")
    @Config.Name("杂草生成概率分母")
    @Config.RangeInt(min = 1, max = 100000)
    public int weedSpawnChance = 100;

    @Config.Comment("已有杂草时向四邻蔓延的概率分母；实际判定为 rand(N) - growth <= 2。")
    @Config.Name("杂草蔓延概率分母")
    @Config.RangeInt(min = 2, max = 100000)
    public int weedSpreadChance = 50;

    @Config.Comment("空的双层架每个生长周期尝试杂交的概率分母（1/N）。")
    @Config.Name("杂交尝试概率分母")
    @Config.RangeInt(min = 1, max = 1000)
    public int breedingChance = 3;

    @Config.Comment({
            "底土需求是否作为硬门槛（源端行为，默认 true）。",
            "true  - 底土不满足时作物完全停止生长；",
            "false - 旧版软惩罚：仍会生长，但增量除以 50。"
    })
    @Config.Name("底土硬需求")
    public boolean subSoilHardRequirement = true;

    // ==================== 属性变异（源端 ConfigurationHandler 的 crops 段） ====================

    @Config.Comment({
            "「单亲 / 自交」路径使用中档变异数组所需的肥料下限。",
            "源端默认 50。"
    })
    @Config.Name("中肥变异阈值（单亲）")
    @Config.RangeInt(min = 1, max = 199)
    public int mediumFertilizerSpreadingThreshold = 50;

    @Config.Comment({
            "「单亲 / 自交」路径使用高档变异数组所需的肥料下限。",
            "源端默认 101（手动施肥上限 100 之上）。"
    })
    @Config.Name("高肥变异阈值（单亲）")
    @Config.RangeInt(min = 1, max = 199)
    public int highFertilizerSpreadingThreshold = 101;

    @Config.Comment({
            "「多亲杂交」路径使用中档变异数组所需的肥料下限。源端默认 50。"
    })
    @Config.Name("中肥变异阈值（杂交）")
    @Config.RangeInt(min = 1, max = 199)
    public int mediumFertilizerBreedingThreshold = 50;

    @Config.Comment({
            "「多亲杂交」路径使用高档变异数组所需的肥料下限。源端默认 101。"
    })
    @Config.Name("高肥变异阈值（杂交）")
    @Config.RangeInt(min = 1, max = 199)
    public int highFertilizerBreedingThreshold = 101;

    @Config.Comment({
            "单亲（自交）在低肥时的属性变异候选值。",
            "每次繁殖从数组里随机抽一个值加到双亲属性均值上，再截断到 1~31。",
            "源端默认 {-1, 0, 0}。"
    })
    @Config.Name("单亲低肥变异")
    public int[] lowFertilizerSpreadingVariations = {-1, 0, 0};

    @Config.Comment("单亲中肥变异。源端默认 {0}（保持属性不变，方便复制模板）。")
    @Config.Name("单亲中肥变异")
    public int[] mediumFertilizerSpreadingVariations = {0};

    @Config.Comment("单亲高肥变异。源端默认 {0, 0, 0, 1, 2, 3, 4}。")
    @Config.Name("单亲高肥变异")
    public int[] highFertilizerSpreadingVariations = {0, 0, 0, 1, 2, 3, 4};

    @Config.Comment("多亲低肥变异。源端默认 {-1, -1, 0, 1, 1}。")
    @Config.Name("多亲低肥变异")
    public int[] lowFertilizerBreedingVariations = {-1, -1, 0, 1, 1};

    @Config.Comment("多亲中肥变异。源端默认 {-2, -1, 0, 1, 2}。")
    @Config.Name("多亲中肥变异")
    public int[] mediumFertilizerBreedingVariations = {-2, -1, 0, 1, 2};

    @Config.Comment("多亲高肥变异。源端默认 {0, 0, 0, 1, 2, 3, 4}。")
    @Config.Name("多亲高肥变异")
    public int[] highFertilizerBreedingVariations = {0, 0, 0, 1, 2, 3, 4};

    @Config.Comment({
            "是否注册旧版的作物监管机 / 作物收割机。",
            "源端 CropsNH 没有这两台机器：功能都合并进了作物管理机。",
            "默认关闭；仅在需要继续使用旧存档机器时打开。"
    })
    @Config.Name("注册旧版作物机器")
    public boolean legacyCropMachines = false;

    // ==================== 读取 ====================

    /**
     * 实际的生长倍率。
     *
     * <p>{@code 0} 是合法值（禁用生长），所以这里不做「非正数兜底成 1」那种处理。
     */
    public static double getGrowthMultiplier() {
        return com.drppp.drtech.DrtConfig.crop.growthMultiplier;
    }

    public static boolean isWeedsEnabled() {
        return com.drppp.drtech.DrtConfig.crop.enableWeeds;
    }

    public static boolean doWeedsWipePlants() {
        return com.drppp.drtech.DrtConfig.crop.weedsWipePlants;
    }

    public static int getWeedSpawnChance() {
        return com.drppp.drtech.DrtConfig.crop.weedSpawnChance;
    }

    public static int getWeedSpreadChance() {
        return com.drppp.drtech.DrtConfig.crop.weedSpreadChance;
    }

    public static int getBreedingChance() {
        return com.drppp.drtech.DrtConfig.crop.breedingChance;
    }

    /** 底土是否硬门槛（源端为硬门槛）。 */
    public static boolean isSubSoilHardRequirement() {
        return com.drppp.drtech.DrtConfig.crop.subSoilHardRequirement;
    }

    /**
     * 源端 {@code getStatVariation(boolean hasSingleParent, int minFertilizer)}：
     * 按「是否单亲」和「参与植株里的最低肥料量」挑一组变异候选值。
     *
     * @param singleParent 参与繁殖的植株是否只有 1 株（自交 / 单亲扩散）
     * @param minFertilizer 参与植株的肥料存储最小值
     * @return 非空的变异候选数组
     */
    public static int[] getStatVariation(boolean singleParent, int minFertilizer) {
        CropConfig cfg = com.drppp.drtech.DrtConfig.crop;
        if (singleParent) {
            if (minFertilizer >= cfg.highFertilizerSpreadingThreshold) {
                return nonEmpty(cfg.highFertilizerSpreadingVariations, cfg.mediumFertilizerSpreadingVariations);
            }
            if (minFertilizer >= cfg.mediumFertilizerSpreadingThreshold) {
                return nonEmpty(cfg.mediumFertilizerSpreadingVariations, cfg.lowFertilizerSpreadingVariations);
            }
            return nonEmpty(cfg.lowFertilizerSpreadingVariations, cfg.mediumFertilizerSpreadingVariations);
        }
        if (minFertilizer >= cfg.highFertilizerBreedingThreshold) {
            return nonEmpty(cfg.highFertilizerBreedingVariations, cfg.mediumFertilizerBreedingVariations);
        }
        if (minFertilizer >= cfg.mediumFertilizerBreedingThreshold) {
            return nonEmpty(cfg.mediumFertilizerBreedingVariations, cfg.lowFertilizerBreedingVariations);
        }
        return nonEmpty(cfg.lowFertilizerBreedingVariations, cfg.mediumFertilizerBreedingVariations);
    }

    /** 配置里数组可以被玩家清空，这里兜一层，避免 {@code nextInt(0)} 崩溃。 */
    private static int[] nonEmpty(int[] preferred, int[] fallback) {
        if (preferred != null && preferred.length > 0) return preferred;
        if (fallback != null && fallback.length > 0) return fallback;
        return new int[]{0};
    }

    /** 是否注册旧版监管机 / 收割机。 */
    public static boolean isLegacyCropMachinesEnabled() {
        return com.drppp.drtech.DrtConfig.crop.legacyCropMachines;
    }

    /**
     * 某株作物最终该用哪种形状。
     *
     * @param declared 作物自己声明的形状；作物不存在时可以传 {@code null}
     * @return 全局强制的那一种；{@code AUTO} 时返回 {@code declared}
     */
    @Nullable
    public static CropRenderType resolveRenderType(@Nullable CropRenderType declared) {
        CropRenderType forced = com.drppp.drtech.DrtConfig.crop.renderMode.getForced();
        return forced != null ? forced : declared;
    }

    /** {@link #resolveRenderType} 的带兜底版本，给渲染器用。 */
    @NotNull
    public static CropRenderType resolveRenderTypeOrDefault(@Nullable CropRenderType declared) {
        CropRenderType resolved = resolveRenderType(declared);
        return resolved != null ? resolved : CropRenderType.CROSS;
    }
}
