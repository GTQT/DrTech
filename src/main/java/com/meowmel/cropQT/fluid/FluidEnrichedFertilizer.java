package com.meowmel.cropQT.fluid;

import com.drppp.drtech.Tags;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.FluidState;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * 浓缩液体肥料。
 *
 * <p>比 {@link FluidFertilizer} 更浓——同样的量补更多肥。留给后续机器消耗
 * （种子生成机等），让「先浓缩再喂」这条路有存在的意义。
 */
public final class FluidEnrichedFertilizer {

    /** 流体的注册名（不带 modid）。 */
    public static final String NAME = "enriched_fertilizer";

    /** 一桶（1000 mB）能补多少肥。约为普通液体肥料的 4 倍。 */
    public static final int POTENCY_PER_BUCKET = 8000;

    /** 颜色：比普通液体肥料更深。 */
    private static final int COLOR = 0x4A2F19;

    @Nullable
    private static Fluid fluid;

    private FluidEnrichedFertilizer() {
    }

    /** 已注册的流体；注册前为 {@code null}。 */
    @Nullable
    public static Fluid get() {
        return fluid;
    }

    /** 注册流体并挂进 {@link FertilizerRegistry}。由 {@code CropInitHandler.init()} 调用。 */
    public static void register() {
        fluid = new FluidBuilder()
                .name(NAME)
                .translation("fluid." + Tags.MODID + "." + NAME)
                .color(COLOR)
                .state(FluidState.LIQUID)
                .build(Tags.MODID, null, null);
        FertilizerRegistry.registerFluid(fluid, POTENCY_PER_BUCKET);
    }
}
