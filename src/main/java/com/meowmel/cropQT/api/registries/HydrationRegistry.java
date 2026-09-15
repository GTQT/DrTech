package com.meowmel.cropQT.api.registries;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * 水合登记表：哪些流体能给作物架补水，每 mB 补多少。
 *
 * <p>默认只认原版水（1 mB = 1 点）。别的 mod 的水可以自行 {@link #register}。
 */
public final class HydrationRegistry {

    /** 流体 → 每 mB 提供的补水量。 */
    public static final PotencyRegistry<Fluid> FLUIDS = new PotencyRegistry<>("hydration");

    private HydrationRegistry() {
    }

    /** 登记默认值。由 {@code CropInitHandler.init()} 调用。 */
    public static void registerDefaults() {
        FLUIDS.register(FluidRegistry.WATER, 1);
    }

    /** 该流体的补水量；不认识返回 0。 */
    public static int getWater(Fluid fluid) {
        return FLUIDS.getPotency(fluid);
    }

    public static boolean isKnown(Fluid fluid) {
        return FLUIDS.exists(fluid);
    }

    /**
     * 往登记表里加一个流体。
     *
     * @param waterPerBucket 一桶（1000 mB）能补多少水
     */
    public static void register(Fluid fluid, int waterPerBucket) {
        FLUIDS.register(fluid, waterPerBucket / 1000);
    }
}
