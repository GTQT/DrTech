package com.meowmel.cropQT.api.registries;

import com.drppp.drtech.api.unification.material.DrtechMaterials;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.NotNull;

/**
 * 肥料登记表：哪些液体和固体能补肥，各补多少。
 *
 * <h2>两张表</h2>
 * <ul>
 *     <li>{@link #FLUIDS} —— 按 mB 算点数，给洒水那类走管道的机器用</li>
 *     <li>{@link #ITEMS} —— 按个算点数，给往槽里塞固体肥料的机器用</li>
 * </ul>
 * 两张表各算各的，别互相换算：机器自己决定「优先吃哪个、吃不到怎么退而求其次」。
 *
 * <p><b>单位不一样</b>：流体表存的是<b>每 mB</b> 的点数（所以 {@link #registerFluid} 要除以 1000），
 * 物品表存的是<b>每个</b>的点数。别把两个方法搞混。
 *
 * <p>{@code fertilizer_applicator} 那个工具<b>不走这里</b> —— 它自带耐久、用完即弃，
 * 中间不经过任何登记表。这里管的是能被机器自动化消耗的肥料。
 *
 * <p>默认的两种液体肥料是 {@link DrtechMaterials#Fertilizer} 与
 * {@link DrtechMaterials#EnrichedFertilizer} 两个材料对应的流体；默认的两种固体肥料是
 * GT 的 {@code MetaItems.FERTILIZER} 与原版骨粉。都由 {@link #registerDefaults()} 挂进来
 * （跟 {@link HydrationRegistry} 一个写法）。
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

    /** 一份 GT 肥料顶多少点。 */
    public static final int GT_FERTILIZER_POTENCY = 400;
    /** 一份骨粉顶多少点。 */
    public static final int BONE_MEAL_POTENCY = 200;

    /**
     * 液体肥料 → 每 mB 提供的肥料点数。
     */
    public static final PotencyRegistry<Fluid> FLUIDS = new PotencyRegistry<>("fertilizer_fluid");

    /**
     * 固体肥料 → 每个物品提供的肥料点数。
     *
     * <p>键用 {@link ItemAndMetadata}：它只取物品与 metadata、带正确的值语义，
     * 正好能把骨粉（{@code Items.DYE} 的 15 号）这种靠 metadata 区分的物品算进来。
     */
    public static final PotencyRegistry<ItemAndMetadata> ITEMS = new PotencyRegistry<>("fertilizer_item");

    private FertilizerRegistry() {
    }

    /**
     * 登记默认的肥料。由 {@code CropInitHandler.init()} 调用。
     *
     * <p><b>不能在材料事件里调</b>：材料的流体由 GT 在 {@code GTFluidRegistration}
     * 里统一产出（在 {@code OreDictUnifier.init()} 之后），那时候才拿得到 {@code Fluid}。
     */
    public static void registerDefaults() {
        registerFluid(DrtechMaterials.Fertilizer.getFluid(), FERTILIZER_PER_BUCKET);
        registerFluid(DrtechMaterials.EnrichedFertilizer.getFluid(), ENRICHED_FERTILIZER_PER_BUCKET);

        // GT 的肥料物品是 metaItem，理论上 preInit 就注册好了；真拿不到就跳过，
        // 别让一条作物肥料的登记把整个作物系统带崩
        if (MetaItems.FERTILIZER != null) {
            registerItem(MetaItems.FERTILIZER.getStackForm(), GT_FERTILIZER_POTENCY);
        }
        registerItem(new ItemStack(Items.DYE, 1, 15), BONE_MEAL_POTENCY);
    }

    // ==================== 液体 ====================

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

    // ==================== 固体 ====================

    /**
     * 该物品能补多少肥；不认识返回 0。
     *
     * <p>只看物品与 metadata，不看 NBT、不看数量。
     */
    public static int getFertilizer(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return ITEMS.getPotency(new ItemAndMetadata(stack));
    }

    /**
     * 往登记表里加一种固体肥料。
     *
     * @param potencyPerItem 一个物品能补多少肥
     */
    public static void registerItem(@NotNull ItemStack stack, int potencyPerItem) {
        ITEMS.register(new ItemAndMetadata(stack), potencyPerItem);
    }
}
