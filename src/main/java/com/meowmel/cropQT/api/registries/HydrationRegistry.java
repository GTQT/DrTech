package com.meowmel.cropQT.api.registries;

import com.meowmel.cropQT.api.potency.IPotencyData;
import com.meowmel.cropQT.api.potency.PotencyData;
import gregtech.api.unification.material.Materials;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * 水合登记表：哪些流体能给作物补水，每 mB 补多少。
 *
 * <p>默认值对齐源端：原版水 {@code (1)}、蒸馏水 {@code (2)}。
 * 别的模组的水可以自行 {@link #register(Fluid, IPotencyData)} 登记。
 */
public final class HydrationRegistry {

    /** 流体 → 每 mB 的强度数据。 */
    public static final PotencyRegistry<Fluid> FLUIDS = new PotencyRegistry<>("hydration");

    private HydrationRegistry() {
    }

    /** 登记默认值。由 {@code CropInitHandler.init()} 调用。 */
    public static void registerDefaults() {
        register(FluidRegistry.WATER, new PotencyData(1));
        if (Materials.DistilledWater.getFluid() != null) {
            register(Materials.DistilledWater.getFluid(), new PotencyData(2));
        }
    }

    /** 该流体的强度数据；不认识返回 {@code null}。 */
    @Nullable
    public static IPotencyData getWaterData(Fluid fluid) {
        return FLUIDS.get(fluid);
    }

    /** 该流体每 mB 补多少水；不认识返回 0。 */
    public static int getWater(Fluid fluid) {
        return FLUIDS.getPotency(fluid);
    }

    public static boolean isKnown(Fluid fluid) {
        return FLUIDS.isRegistered(fluid);
    }

    public static void register(Fluid fluid, IPotencyData data) {
        FLUIDS.register(fluid, data);
    }
}
