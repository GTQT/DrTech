package com.meowmel.cropQT.api.capability.impl;

import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.machine.MetaTileEntitySeedGenerator;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 种子生成器的干活逻辑：把一份已分析的种子扩繁成两份。
 *
 * <p>一份种子进去、<b>两份</b>同样的种子出来，代价是液体肥料、电和时间。
 * 它是整条作物链里唯一的净产出环节 —— 别处的种子只守恒或变少：
 * 打掉作物架退回一份、育种机吃 2~4 份亲本出 1 份、提取器把种子换成基因球。
 * 想攒种子填满农场，就靠这里。
 *
 * <p>未分析的种子不给扩繁——「先分析再扩繁」是这条链的入口约束。
 *
 * <h2>肥料按浓度算量</h2>
 * 一轮要的是固定的 {@link #FERTILIZER_POINTS_PER_CYCLE 肥力点}，实际抽多少 mB
 * 由 {@link FertilizerRegistry#getFertilizer} 的浓度反推：普通液肥 2 点/mB、
 * 浓缩液肥 8 点/mB，所以浓缩肥一轮只抽四分之一的量。
 */
public class SeedGeneratorRecipeLogic extends CropMachineRecipeLogic<MetaTileEntitySeedGenerator> {

    /**
     * 一轮扩繁要消耗的「肥力点」。
     *
     * <p>用肥力点而不是 mB 当单位，是为了让浓缩肥真的更省 ——
     * 100 点折成普通液肥正好 50 mB，和这台机器最早的定价一致。
     */
    public static final int FERTILIZER_POINTS_PER_CYCLE = 100;

    /** 一份种子进去，几份出来。 */
    public static final int OUTPUT_COUNT = 2;

    /** 基准耗时（tier 0）。每高一档减半，最低 20 tick。 */
    private static final int BASE_DURATION = 200;

    public SeedGeneratorRecipeLogic(MetaTileEntitySeedGenerator machine, RecipeMap<?> recipeMap,
                                    Supplier<IEnergyContainer> energy) {
        super(machine, recipeMap, energy);
    }

    @Override
    protected boolean canStart() {
        ItemStack input = input();
        if (input.isEmpty()) {
            return false;
        }
        // 只扩繁已分析的种子
        if (!ItemCropSeed.getCropStats(input).isAnalyzed()) {
            return false;
        }
        FluidStack fertilizer = storedFertilizer();
        if (fertilizer == null || fertilizer.amount < fertilizerPerCycle(fertilizer)) {
            return false;
        }
        return canOutput(input);
    }

    @Override
    protected int getWorkDuration() {
        return Math.max(20, BASE_DURATION >> Math.max(0, machine.getTier() - GTValues.LV));
    }

    @Override
    protected long getEnergyPerTick() {
        return GTValues.V[machine.getTier()];
    }

    @Override
    protected void onWorkComplete() {
        // 基类在调这里之前刚问过一次 canStart()，产物槽装得下是已经确认过的，
        // 所以不必再模拟插入一遍 —— 直接结算
        ItemStack product = productOf(input());
        FluidStack fertilizer = storedFertilizer();
        if (product.isEmpty() || fertilizer == null) {
            return;
        }

        GTTransferUtils.insertItem(machine.getExportItems(), product, false);
        machine.getImportItems().extractItem(MetaTileEntitySeedGenerator.SLOT_INPUT, 1, false);
        machine.getImportFluids().getTankAt(0).drain(fertilizerPerCycle(fertilizer), true);
    }

    // ==================== 材料 ====================

    private ItemStack input() {
        return machine.getImportItems().getStackInSlot(MetaTileEntitySeedGenerator.SLOT_INPUT);
    }

    /** 产物槽能不能再塞下这一轮的产物（两份）。模拟插入判定，与真正写入走同一条路。 */
    private boolean canOutput(ItemStack input) {
        return GTTransferUtils.insertItem(machine.getExportItems(), productOf(input), true).isEmpty();
    }

    /** 按输入种子造一轮产物：一份进、两份出。 */
    private static ItemStack productOf(ItemStack input) {
        ItemStack seed = ItemCropSeed.createSeedBag(
                ItemCropSeed.getCropId(input), ItemCropSeed.getCropStats(input));
        if (seed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        seed.setCount(OUTPUT_COUNT);
        return seed;
    }

    /** 罐里存的液体肥料；不是肥料（或空了）返回 null。 */
    @Nullable
    private FluidStack storedFertilizer() {
        FluidStack stored = machine.getImportFluids().getTankAt(0).getFluid();
        if (stored == null || stored.amount <= 0) {
            return null;
        }
        return FertilizerRegistry.getFertilizer(stored.getFluid()) > 0 ? stored : null;
    }

    /**
     * 一轮要抽多少 mB。
     *
     * <p>向上取整——宁可多要 1 mB，也别让浓缩肥靠舍入白占便宜。
     */
    private static int fertilizerPerCycle(FluidStack fertilizer) {
        // 调进来的都经过 storedFertilizer()，浓度一定为正；max 只是让这个除法
        // 在任何输入下都不会抛 ArithmeticException 把服务端 tick 带下去
        int potency = Math.max(1, FertilizerRegistry.getFertilizer(fertilizer.getFluid()));
        return (FERTILIZER_POINTS_PER_CYCLE + potency - 1) / potency;
    }
}
