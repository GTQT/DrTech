package com.meowmel.cropQT.api.capability.impl;

import com.drppp.drtech.api.unification.material.DrtechMaterials;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.mutation.CropMutation;
import com.meowmel.cropQT.api.mutation.MutationRegistry;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.machine.MetaTileEntityCropBreeder;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 作物育种机的干活逻辑：把 2~4 颗已分析的亲本种子杂交成一颗新种子。
 *
 * <p>数值全部对齐源端 {@code MTECropBreeder}：
 * <ul>
 *   <li>液肥按 mB 直接扣，不再做 potency 折算（罐里只收浓缩液肥）；</li>
 *   <li>配方耗时 / EU 来自变异配方本身（{@link CropMutation#getBreedingMachineRecipeEUt()}），
 *       机器按自身电压对它们做超频；EU 高于机器电压的配方直接跳过；</li>
 *   <li>成功率 {@code min(100, 40 + (tier - LV) * 10)}，失败照样扣料。</li>
 * </ul>
 */
public class CropBreederRecipeLogic extends CropMachineRecipeLogic<MetaTileEntityCropBreeder> {

    /** 每档机器每轮的基础液肥消耗（mB）。 */
    public static final int FERTILIZER_PER_TIER = 144;
    /** 每点属性（growth + gain + resistance）附加的液肥消耗（mB）。 */
    public static final int FERTILIZER_PER_STAT = 72;

    /** 成功率 = min(100, 40 + (档位 - LV) * 10)。 */
    private static final int MIN_CHANCE = 40;
    private static final int MAX_CHANCE = 100;
    private static final int CHANCE_PER_TIER = 10;

    /** 本轮解析出来的育种方案；{@code null} 表示条件不满足。 */
    @Nullable
    private Resolved resolved;

    public CropBreederRecipeLogic(MetaTileEntityCropBreeder machine, RecipeMap<?> recipeMap,
                                  Supplier<IEnergyContainer> energy) {
        super(machine, recipeMap, energy);
    }

    /** 本档位的产出成功率（%）。 */
    public int getOutputChance() {
        return Math.min(MAX_CHANCE,
                MIN_CHANCE + Math.max(0, machine.getTier() - GTValues.LV) * CHANCE_PER_TIER);
    }

    // ==================== 状态机 ====================

    @Override
    protected boolean canStart() {
        resolved = resolve();
        return resolved != null;
    }

    @Override
    protected int getWorkDuration() {
        return resolved == null ? -1 : resolved.duration;
    }

    @Override
    protected long getEnergyPerTick() {
        return resolved == null ? 0L : resolved.eut;
    }

    @Override
    protected void onWorkComplete() {
        if (resolved == null) {
            return;
        }
        // 无论成功与否都要扣料
        machine.getImportFluids().getTankAt(0).drain(new FluidStack(
                DrtechMaterials.EnrichedFertilizer.getFluid(), resolved.fluid), true);
        consumeOneParentEach();

        // 掷骰决定这一次有没有出种子。用世界的随机源，和 MutationRegistry 的
        // pickDeterministic 是同一个 —— 换成 Math.random() 会让那套可复现性失效
        if (machine.getWorld().rand.nextInt(MAX_CHANCE) < getOutputChance()) {
            GTTransferUtils.insertItem(machine.getExportItems(), resolved.child.copy(), false);
        }
        machine.markDirty();
    }

    // ==================== 方案解析 ====================

    @Nullable
    private Resolved resolve() {
        List<String> parentIds = new ArrayList<>();
        List<CropStats> parentStats = new ArrayList<>();
        int slots = machine.getInputSlots();
        int analyzedSlots = 0;
        for (int slot = 0; slot < slots; slot++) {
            ItemStack seed = machine.getImportItems().getStackInSlot(slot);
            if (seed.isEmpty()) {
                continue;
            }
            CropStats stats = ItemCropSeed.getCropStats(seed);
            String cropId = ItemCropSeed.getCropId(seed);
            if (!stats.isAnalyzed() || cropId.isEmpty()) {
                continue;
            }
            parentIds.add(cropId);
            parentStats.add(stats);
            analyzedSlots++;
        }
        // 源端要求至少两颗已分析亲本；同种亲本允许重复（配方表里有自交配方）
        if (analyzedSlots < CropMutation.MIN_PARENTS) {
            return null;
        }

        CropMutation mutation = MutationRegistry.pickDeterministic(parentIds, machine.getWorld().rand);
        if (mutation == null) {
            return null;
        }
        // 配方 EU 高于机器电压：这台机器做不了
        int recipeEut = mutation.getBreedingMachineRecipeEUt();
        long machineVoltage = GTValues.V[machine.getTier()];
        if (recipeEut > machineVoltage) {
            return null;
        }
        CropType output = CropRegistry.get(mutation.getResult());
        if (output == null) {
            return null;
        }

        CropStats childStats = averageStats(parentStats);
        int fluid = fluidCost(childStats);
        FluidStack stored = machine.getImportFluids().getTankAt(0).getFluid();
        if (stored == null || stored.getFluid() != DrtechMaterials.EnrichedFertilizer.getFluid()
                || stored.amount < fluid) {
            return null;
        }

        int[] overclock = CropMachineRecipeLogic.overclock(recipeEut,
                mutation.getBreedingMachineRecipeDuration(), machineVoltage);
        ItemStack child = ItemCropSeed.createSeedBag(mutation.getResult(), childStats);
        if (!GTTransferUtils.insertItem(machine.getExportItems(), child, true).isEmpty()) {
            return null;
        }
        return new Resolved(mutation, output, childStats, fluid, child, overclock[0], overclock[1]);
    }

    /** 子代属性 = 各亲本属性的算术平均，向下取整（源端 getNewSeedStats）。 */
    private static CropStats averageStats(List<CropStats> parents) {
        int growth = 0, gain = 0, resistance = 0;
        for (CropStats stats : parents) {
            growth += stats.getGrowth();
            gain += stats.getGain();
            resistance += stats.getResistance();
        }
        int n = parents.size();
        return new CropStats(growth / n, gain / n, resistance / n).analyze();
    }

    /**
     * 一轮育种的液肥用量（mB，浓缩液肥）。
     *
     * <p>源端公式：{@code max(1, tier * 144) + max(1, (gr + ga + re) * 72)}。
     * 这里的 {@code tier} 是机器档位。
     */
    private int fluidCost(CropStats stats) {
        return Math.max(1, machine.getTier() * FERTILIZER_PER_TIER)
                + Math.max(1, (stats.getGrowth() + stats.getGain() + stats.getResistance())
                        * FERTILIZER_PER_STAT);
    }

    /** 每个用到的亲本槽各扣一颗。 */
    private void consumeOneParentEach() {
        int slots = machine.getInputSlots();
        for (int slot = 0; slot < slots; slot++) {
            if (!machine.getImportItems().getStackInSlot(slot).isEmpty()) {
                machine.getImportItems().extractItem(slot, 1, false);
            }
        }
    }

    /** 一轮育种方案。 */
    private static final class Resolved {
        final CropMutation mutation;
        final CropType output;
        final CropStats stats;
        final int fluid;
        final ItemStack child;
        final int eut;
        final int duration;

        Resolved(CropMutation mutation, CropType output, CropStats stats, int fluid, ItemStack child,
                 int eut, int duration) {
            this.mutation = mutation;
            this.output = output;
            this.stats = stats;
            this.fluid = fluid;
            this.child = child;
            this.eut = eut;
            this.duration = duration;
        }
    }
}
