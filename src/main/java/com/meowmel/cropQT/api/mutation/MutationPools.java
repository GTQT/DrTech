package com.meowmel.cropQT.api.mutation;

import java.util.Arrays;

/**
 * 预定义的变异池。
 *
 * <p>池的作用只有一个：让「两株同类作物放在一起」在没有确定性配方时也能杂交出
 * 这个池里的别的成员，而不是什么都不发生。
 *
 * <p>成员是写死的 id 列表。id 必须对得上真实存在的作物 —— 材料驱动的作物 id 形如
 * {@code <材料名>_<外型>}（见 {@code CropMaterialType}），手写的原版作物用 MC 的物品名。
 * 写错不会报错，只是那一条永远杂交不出来，所以 {@code MutationPoolRecipeWrapper}
 * 会把认不出的成员跳过不展示，JEI 里看到的就是实际能出的。
 */
public final class MutationPools {

    /** 花卉 / 染料来源。 */
    public static final MutationPool FLOWER = new MutationPool("flower");

    /** 谷物 / 纤维类。 */
    public static final MutationPool GRAIN = new MutationPool("grain");

    /** 矿石叶系列 —— 金属驱动的那些作物，底土是它们各自的金属。 */
    public static final MutationPool ORE_LEAF = new MutationPool("ore_leaf");

    private MutationPools() {
    }

    /** 登记默认池。由 {@code CropInitHandler.init()} 调用。 */
    public static void registerDefaults() {
        FLOWER.addAll(Arrays.asList(
                "dandelion", "rose",
                "mica_flower", "sulfur_flower", "osmium_flower", "iridium_flower"));

        GRAIN.addAll(Arrays.asList("wheat", "reed"));

        ORE_LEAF.addAll(Arrays.asList(
                "copper_fiber", "tin_twig", "iron_leaf", "lead_leaf", "silver_leaf", "gold_leaf",
                "aluminium_leaf", "nickel_leaf", "zinc_leaf", "tungsten_leaf", "titanium_leaf",
                "platinum_leaf", "manganese_leaf", "uranium238_leaf", "salt_root"));

        MutationRegistry.register(FLOWER);
        MutationRegistry.register(GRAIN);
        MutationRegistry.register(ORE_LEAF);
    }
}
