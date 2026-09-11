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
            "杂交成功率倍率。1.0 为默认。",
            "乘在作物自身「参与杂交的意愿」上（CropStats 的 crossBreedChance），",
            "最终概率仍会被截断到 0~100。"
    })
    @Config.Name("杂交成功率倍率")
    @Config.RangeDouble(min = 0.0d, max = 10.0d)
    public double crossBreedChanceMultiplier = 1.0d;

    @Config.Comment({
            "作物渲染形状。",
            "  AUTO   - 用每个作物自己声明的形状（默认）",
            "  CROSS  - 全部强制成十字交叉",
            "  HASH   - 全部强制成井字架状",
            "  FLOWER - 全部强制成花坛状"
    })
    @Config.Name("作物渲染形状")
    public RenderModeOverride renderMode = RenderModeOverride.AUTO;

    // ==================== 读取 ====================

    /**
     * 实际的生长倍率。
     *
     * <p>{@code 0} 是合法值（禁用生长），所以这里不做「非正数兜底成 1」那种处理。
     */
    public static double getGrowthMultiplier() {
        return com.drppp.drtech.DrtConfig.crop.growthMultiplier;
    }

    /** 实际的杂交概率倍率。 */
    public static double getCrossBreedChanceMultiplier() {
        return com.drppp.drtech.DrtConfig.crop.crossBreedChanceMultiplier;
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
