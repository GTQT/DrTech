package com.meowmel.cropQT.api.registries;

import com.drppp.drtech.api.unification.material.DrtechMaterials;
import net.minecraftforge.fluids.Fluid;

/**
 * 液体肥料登记表：哪些流体能补肥，每 mB 补多少。
 *
 * <p>固体肥料不在这里——它是 {@code fertilizer_applicator} 工具自带的耐久，
 * 每次消耗一点，用完即弃，中间不经过任何登记表。
 *
 * <p>两种默认肥料是 {@link DrtechMaterials#Fertilizer} 与
 * {@link DrtechMaterials#EnrichedFertilizer} 两个材料对应的流体，
 * 由 {@link #registerDefaults()} 挂进来（跟 {@link HydrationRegistry} 一个写法）。
 */
public final class FertilizerRegistry {

    /**
     * 普通液体肥料：一桶补 2000 点。
     */
    public static final int FERTILIZER_PER_BUCKET = 2000;
    /**
     * 浓缩液体肥料：一桶补 8000 点，约为普通的 4 倍。
     */
    public static final int ENRICHED_FERTILIZER_PER_BUCKET = 8000;

    /**
     * 液体肥料 → 每 mB 提供的肥料点数。
     */
    public static final PotencyRegistry<Fluid> FLUIDS = new PotencyRegistry<>("fertilizer_fluid");

    private FertilizerRegistry() {
    }

    /**
     * 登记默认的两种液体肥料。由 {@code CropInitHandler.init()} 调用。
     *
     * <p><b>不能在材料事件里调</b>：材料的流体由 GT 在 {@code GTFluidRegistration}
     * 里统一产出（在 {@code OreDictUnifier.init()} 之后），那时候才拿得到 {@code Fluid}。
     */
    public static void registerDefaults() {
        registerFluid(DrtechMaterials.Fertilizer.getFluid(), FERTILIZER_PER_BUCKET);
        registerFluid(DrtechMaterials.EnrichedFertilizer.getFluid(), ENRICHED_FERTILIZER_PER_BUCKET);
    }

    /**
     * 该流体每 mB 能补多少肥；不认识返回 0。
     */
    public static int getFertilizer(Fluid fluid) {
        return FLUIDS.getPotency(fluid);
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
