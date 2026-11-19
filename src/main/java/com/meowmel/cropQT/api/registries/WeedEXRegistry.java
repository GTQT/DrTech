package com.meowmel.cropQT.api.registries;

import com.meowmel.cropQT.api.potency.IPotencyData;
import com.meowmel.cropQT.api.potency.PotencyData;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * 除草剂（WeedEX）登记表：哪些流体能补 WeedEX 罐，每 mB 补多少。
 *
 * <p>源端登记了两种：CropsNH 自己的 {@code weedex} 流体 {@code (1)}，以及 GT5U 的
 * {@code Materials.WeedEX9000} {@code (10)}。GTCEu/GTQT 没有后者，DrTech 目前也没有
 * 自己的 WeedEX 流体，所以默认登记表是<b>空的</b>：
 * <ul>
 *     <li>WeedEX 罐仍可通过「罐子物品」补充（每点耐久 = 10 点），这条路不依赖流体；</li>
 *     <li>若整合包提供了名为 {@code weedex} 的流体，会自动按 1 点/mB 登记；</li>
 *     <li>其他模组可调用 {@link #register(Fluid, IPotencyData)} 补充。</li>
 * </ul>
 */
public final class WeedEXRegistry {

    /** 流体 → 每 mB 的强度数据。 */
    public static final PotencyRegistry<Fluid> FLUIDS = new PotencyRegistry<>("weedex");

    private WeedEXRegistry() {
    }

    /** 登记默认值。由 {@code CropInitHandler.init()} 调用。 */
    public static void registerDefaults() {
        Fluid weedex = FluidRegistry.getFluid("weedex");
        if (weedex != null) {
            register(weedex, new PotencyData(1));
        }
    }

    /** 该流体的强度数据；不认识返回 {@code null}。 */
    @Nullable
    public static IPotencyData getWeedExData(Fluid fluid) {
        return FLUIDS.get(fluid);
    }

    /** 该流体每 mB 补多少 WeedEX；不认识返回 0。 */
    public static int getWeedEx(Fluid fluid) {
        return FLUIDS.getPotency(fluid);
    }

    public static void register(Fluid fluid, IPotencyData data) {
        FLUIDS.register(fluid, data);
    }
}
