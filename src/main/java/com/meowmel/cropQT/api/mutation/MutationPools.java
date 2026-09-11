package com.meowmel.cropQT.api.mutation;

import java.util.Arrays;

/**
 * 预定义的变异池。
 *
 * <p><b>目前只登记了几个示范池</b>，成员是写死的 id 列表。按标签分组（颜色 / 来源 /
 * 作物类型 / 属性 / 掉落）需要作物先带标签，那是 M5 给 137 个作物补归属时一起做的事——
 * 到那时把这些写死的列表换成按标签自动收集即可。
 *
 * <p>池的作用只有一个：让「两株同类作物放在一起」在没有确定性配方时也能杂交出
 * 这个池里的别的成员，而不是什么都不发生。
 */
public final class MutationPools {

    /** 花卉 / 染料来源。 */
    public static final MutationPool FLOWER = new MutationPool("flower");

    /** 谷物 / 纤维类。 */
    public static final MutationPool GRAIN = new MutationPool("grain");

    /** 矿石叶系列。 */
    public static final MutationPool ORE_LEAF = new MutationPool("ore_leaf");

    private MutationPools() {
    }

    /** 登记默认池。由 {@code CropInitHandler.init()} 调用。 */
    public static void registerDefaults() {
        FLOWER.addAll(Arrays.asList(
                "dandelion", "rose", "cyazint", "tulip", "hyacinth", "wonderflower",
                "mystic_flower_white", "mystic_flower_black", "mystic_flower_red", "mystic_flower_green",
                "mystic_flower_brown", "mystic_flower_orange", "mystic_flower_magenta", "mystic_flower_light_blue",
                "mystic_flower_yellow", "mystic_flower_lime", "mystic_flower_pink", "mystic_flower_gray",
                "mystic_flower_silver", "mystic_flower_cyan", "mystic_flower_blue", "mystic_flower_purple"));

        GRAIN.addAll(Arrays.asList("wheat", "reed", "papyrus", "stickreed"));

        ORE_LEAF.addAll(Arrays.asList(
                "ferru", "coppon_fiber", "lead_leaf", "argentia_leaf", "auronia_leaf", "tine_twig",
                "nickel_leaf", "zinc_leaf", "aluminum_leaf", "titanium_leaf", "tungsten_leaf",
                "platinum_leaf", "manganese_leaf", "salty_root"));

        MutationRegistry.register(FLOWER);
        MutationRegistry.register(GRAIN);
        MutationRegistry.register(ORE_LEAF);
    }
}
