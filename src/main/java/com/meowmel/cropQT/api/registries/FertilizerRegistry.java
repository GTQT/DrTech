package com.meowmel.cropQT.api.registries;

import com.drppp.drtech.api.unification.material.DrtechMaterials;
import com.meowmel.cropQT.api.potency.IPotencyData;
import com.meowmel.cropQT.api.potency.PotencyData;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.common.items.MetaItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 肥料登记表：哪些液体和固体能补肥，各补多少。
 *
 * <p>单位语义按 CropsNH 的 Potency 模型：
 * <ul>
 *     <li>液体按 <b>mB</b> 登记 {@link PotencyData}，默认每次施用消耗 1 mB；</li>
 *     <li>固体按 <b>个</b> 登记，默认每次施用消耗 1 个；</li>
 *     <li>{@link IPotencyData#getMaxStorage()} 可以压低这种肥料能把作物/机器喂到的上限
 *     （骨粉就是典型：5 点/个，但最多顶到 30）。</li>
 * </ul>
 *
 * <p>三张表各算各的：
 * <ul>
 *     <li>{@link #FLUIDS} —— 普通液肥（机器、CropManager 液肥罐）</li>
 *     <li>{@link #ITEMS} —— 固体肥料（CropManager 肥料槽、手动施肥）</li>
 *     <li>{@link #FERTILIZER_UNIT_FLUIDS} —— 农场 FU 组件专用（只认浓缩液肥，且无浓度加成）</li>
 * </ul>
 *
 * <p>默认值对齐源端 {@code PotencyLoader}：普通液肥 1/mB、浓缩液肥 10/mB、
 * GT 肥料 100/个、骨粉 {@code (5, 30)}。
 */
public final class FertilizerRegistry {

    /** 普通液体肥料：1 mB 补 1 点。 */
    public static final int FERTILIZER_FLUID_POTENCY = 1;
    /** 浓缩液体肥料：1 mB 补 10 点。 */
    public static final int ENRICHED_FERTILIZER_FLUID_POTENCY = 10;
    /** 农场 FU 组件：浓缩液肥按 1 mB 补 1 点（不吃浓度加成）。 */
    public static final int FERTILIZER_UNIT_FLUID_POTENCY = 1;
    /** 一份 GT 肥料顶 100 点。 */
    public static final int FERTILIZER_ITEM_POTENCY = 100;
    /** 一份骨粉顶 5 点，且最多把存储顶到 30。 */
    public static final int BONE_MEAL_POTENCY = 5;
    /** 骨粉的存储上限。 */
    public static final int BONE_MEAL_MAX_STORAGE = 30;

    /** 液体肥料 → 强度数据。 */
    public static final PotencyRegistry<Fluid> FLUIDS = new PotencyRegistry<>("fertilizer_fluid");
    /** 固体肥料 → 强度数据。键只取物品 + metadata，不看 NBT。 */
    public static final PotencyRegistry<ItemAndMetadata> ITEMS = new PotencyRegistry<>("fertilizer_item");
    /** FU 组件专用液体表。 */
    public static final PotencyRegistry<Fluid> FERTILIZER_UNIT_FLUIDS =
            new PotencyRegistry<>("fertilizer_unit_fluid");

    private FertilizerRegistry() {
    }

    /**
     * 登记默认肥料。由 {@code CropInitHandler.init()} 调用。
     *
     * <p>不能放进材料事件：材料流体的注册在 {@code OreDictUnifier.init()} 之后才完成。
     */
    public static void registerDefaults() {
        registerFluid(DrtechMaterials.Fertilizer.getFluid(), new PotencyData(FERTILIZER_FLUID_POTENCY));
        registerFluid(DrtechMaterials.EnrichedFertilizer.getFluid(), new PotencyData(ENRICHED_FERTILIZER_FLUID_POTENCY));
        registerUnitFluid(DrtechMaterials.EnrichedFertilizer.getFluid(), new PotencyData(FERTILIZER_UNIT_FLUID_POTENCY));

        if (MetaItems.FERTILIZER != null) {
            registerItem(MetaItems.FERTILIZER.getStackForm(), new PotencyData(FERTILIZER_ITEM_POTENCY));
        }
        registerItem(new ItemStack(Items.DYE, 1, 15), new PotencyData(BONE_MEAL_POTENCY, BONE_MEAL_MAX_STORAGE));
    }

    // ==================== 液体 ====================

    /** 该流体每 mB 的强度数据；不认识返回 {@code null}。 */
    @Nullable
    public static IPotencyData getFertilizerData(Fluid fluid) {
        return FLUIDS.get(fluid);
    }

    /** 该流体每 mB 补多少点；不认识返回 0。 */
    public static int getFertilizer(Fluid fluid) {
        return FLUIDS.getPotency(fluid);
    }

    public static void registerFluid(Fluid fluid, IPotencyData data) {
        FLUIDS.register(fluid, data);
    }

    /** FU 组件专用液体：只认登记到这张表的流体，且按 1 mB = 1 点结算。 */
    @Nullable
    public static IPotencyData getFertilizerUnitData(Fluid fluid) {
        return FERTILIZER_UNIT_FLUIDS.get(fluid);
    }

    public static int getFertilizerUnit(Fluid fluid) {
        return FERTILIZER_UNIT_FLUIDS.getPotency(fluid);
    }

    public static void registerUnitFluid(Fluid fluid, IPotencyData data) {
        FERTILIZER_UNIT_FLUIDS.register(fluid, data);
    }

    // ==================== 固体 ====================

    /** 该物品的强度数据；不看数量、不看 NBT。 */
    @Nullable
    public static IPotencyData getFertilizerData(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return ITEMS.get(new ItemAndMetadata(stack));
    }

    /** 该物品能补多少肥；不认识返回 0。 */
    public static int getFertilizer(@NotNull ItemStack stack) {
        IPotencyData data = getFertilizerData(stack);
        return data == null ? 0 : data.getPotency();
    }

    public static void registerItem(@NotNull ItemStack stack, IPotencyData data) {
        ITEMS.register(new ItemAndMetadata(stack), data);
    }
}
