package com.meowmel.cropQT.api.registries;

import net.minecraftforge.fluids.Fluid;

/**
 * 液体肥料登记表：哪些流体能补肥，每 mB 补多少。
 *
 * <p>固体肥料不在这里——它是 {@code fertilizer_applicator} 工具自带的耐久，
 * 每次消耗一点，用完即弃，中间不经过任何登记表。
 *
 * <p>两种默认流体由 {@link com.meowmel.cropQT.fluid.FluidFertilizer} 与
 * {@link com.meowmel.cropQT.fluid.FluidEnrichedFertilizer} 在注册时自行挂进来。
 */
public final class FertilizerRegistry {

    /** 液体肥料 → 每 mB 提供的肥料点数。 */
    public static final PotencyRegistry<Fluid> FLUIDS = new PotencyRegistry<>("fertilizer_fluid");

    private FertilizerRegistry() {
    }

    /** 该流体每 mB 能补多少肥；不认识返回 0。 */
    public static int getFertilizer(Fluid fluid) {
        return FLUIDS.getPotency(fluid);
    }

    public static boolean isKnown(Fluid fluid) {
        return FLUIDS.exists(fluid);
    }

    /**
     * 往登记表里加一种液体肥料。
     *
     * @param fertilizerPerBucket 一桶（1000 mB）能补多少肥
     */
    public static void registerFluid(Fluid fluid, int fertilizerPerBucket) {
        FLUIDS.register(fluid, fertilizerPerBucket / 1000);
    }
}
