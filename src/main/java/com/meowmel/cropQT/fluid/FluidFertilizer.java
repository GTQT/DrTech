package com.meowmel.cropQT.fluid;

import com.drppp.drtech.Tags;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.FluidState;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * 液体肥料。
 *
 * <p>给工业农场（M7）这类能用管道喂肥的机器用；手动玩法的作物架走
 * {@link com.meowmel.cropQT.item.ItemWateringCan} 与物品肥料。
 *
 * <p>注册走 GT 的 {@link FluidBuilder}——它顺带把桶和贴图占位都处理了。
 */
public final class FluidFertilizer {

    /** 流体的注册名（不带 modid）。 */
    public static final String NAME = "fertilizer";

    /** 一桶（1000 mB）能补多少肥。 */
    public static final int POTENCY_PER_BUCKET = 2000;

    /** 颜色：土褐色。 */
    private static final int COLOR = 0x7A5230;

    @Nullable
    private static Fluid fluid;

    private FluidFertilizer() {
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
