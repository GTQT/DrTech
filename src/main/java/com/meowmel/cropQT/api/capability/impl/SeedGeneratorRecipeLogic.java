package com.meowmel.cropQT.api.capability.impl;

import com.drppp.drtech.api.unification.material.DrtechMaterials;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.machine.MetaTileEntitySeedGenerator;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * 种子生成器的干活逻辑 —— CropsNH {@code MTESeedGenerator.checkRecipe} 的复刻。
 *
 * <h2>源端数值</h2>
 * <ul>
 *     <li>基准耗时 {@code 8 秒 = 160 tick}，基准 EU/t {@code 30}（LV 配方电压），1A。</li>
 *     <li>每点 stat（growth/gain/resistance 之和）消耗 {@code 36 mB} 浓缩液肥：
 *     {@code max(1, (gr+ga+re) * 36)}，按 mB 直接扣，不做浓度换算。</li>
 *     <li>只接受浓缩液肥（{@code CropsNHFluids.enrichedFertilizer}，对应
 *     {@link DrtechMaterials#EnrichedFertilizer}）。</li>
 *     <li>一份已分析种子 → <b>一份</b>同样的种子（源端 {@code OUTPUT_COUNT = 1}）。</li>
 *     <li>{@code crossingThreshold < 0} 的作物不能扩繁，只能走合成机。</li>
 *     <li>作物声明了 {@code duplicationCatalysts} 时，输入槽里的匹配催化剂会被消耗一份。</li>
 * </ul>
 *
 * <h2>超频</h2>
 * 走 GT5U 的不完美超频（每档 ×4 EU、耗时 ÷2），基准是 LV 的 30 EU/t；
 * 超频次数受机器电压限制，直到再超一档就会超过 {@code V[tier]} 为止。
 */
public class SeedGeneratorRecipeLogic extends CropMachineRecipeLogic<MetaTileEntitySeedGenerator> {

    /** 每点 stat 的浓缩液肥消耗（mB）。 */
    public static final int FERTILIZER_PER_STAT = 36;

    /** 基准耗时：8 秒。 */
    public static final int BASE_DURATION = 160;

    /** 基准 EU/t：LV 配方电压。 */
    public static final long BASE_EUT = 30L;

    public SeedGeneratorRecipeLogic(MetaTileEntitySeedGenerator machine, RecipeMap<?> recipeMap,
                                    Supplier<IEnergyContainer> energy) {
        super(machine, recipeMap, energy);
    }

    @Override
    protected boolean canStart() {
        ItemStack input = machine.getImportItems()
                .getStackInSlot(MetaTileEntitySeedGenerator.SLOT_INPUT);
        if (input.isEmpty() || !ItemCropSeed.getCropStats(input).isAnalyzed()) {
            return false;
        }

        CropType crop = CropRegistry.get(ItemCropSeed.getCropId(input));
        if (crop == null || crop.getCrossingThreshold() < 0) {
            return false;
        }

        FluidStack fertilizer = storedFertilizer();
        if (fertilizer == null || fertilizer.amount < fertilizerPerCycle(input)) {
            return false;
        }
        return canOutput(input);
    }

    @Override
    protected int getWorkDuration() {
        return Math.max(1, BASE_DURATION >> overclockCount());
    }

    @Override
    protected long getEnergyPerTick() {
        return BASE_EUT * (1L << (2 * overclockCount()));
    }

    /**
     * 源端 {@code calculateOverclockedNess(30, 160)} 的超频次数：
     * EU 每次 ×4，直到下一档会超过机器电压为止。
     */
    private int overclockCount() {
        long voltage = GTValues.V[machine.getTier()];
        long eut = BASE_EUT;
        int overclocks = 0;
        while (eut * 4 <= voltage) {
            eut *= 4;
            overclocks++;
        }
        return overclocks;
    }

    @Override
    protected void onWorkComplete() {
        ItemStack input = machine.getImportItems()
                .getStackInSlot(MetaTileEntitySeedGenerator.SLOT_INPUT);
        ItemStack product = productOf(input);
        FluidStack fertilizer = storedFertilizer();
        if (product.isEmpty() || fertilizer == null) {
            return;
        }

        GTTransferUtils.insertItem(machine.getExportItems(), product, false);
        machine.getImportItems().extractItem(MetaTileEntitySeedGenerator.SLOT_INPUT, 1, false);
        consumeCatalyst(input);
        machine.getImportFluids().getTankAt(0).drain(fertilizerPerCycle(input), true);
    }

    // ==================== 材料 ====================

    /** 产物：一份同样的已分析种子。 */
    private static ItemStack productOf(ItemStack input) {
        ItemStack seed = ItemCropSeed.createSeedBag(
                ItemCropSeed.getCropId(input), ItemCropSeed.getCropStats(input));
        if (!seed.isEmpty()) {
            seed.setCount(1);
        }
        return seed;
    }

    private boolean canOutput(ItemStack input) {
        return GTTransferUtils.insertItem(machine.getExportItems(), productOf(input), true).isEmpty();
    }

    /** 罐里存的浓缩液肥；不是浓缩液肥（或空了）返回 null。 */
    @Nullable
    private FluidStack storedFertilizer() {
        FluidStack stored = machine.getImportFluids().getTankAt(0).getFluid();
        if (stored == null || stored.amount <= 0) {
            return null;
        }
        return stored.getFluid() == DrtechMaterials.EnrichedFertilizer.getFluid() ? stored : null;
    }

    /** 源端：{@code max(1, (gr+ga+re) * 36)} mB，直接按 mB 扣。 */
    private static int fertilizerPerCycle(ItemStack input) {
        int stats = ItemCropSeed.getCropStats(input).getGrowth()
                + ItemCropSeed.getCropStats(input).getGain()
                + ItemCropSeed.getCropStats(input).getResistance();
        return Math.max(1, stats * FERTILIZER_PER_STAT);
    }

    /**
     * 消耗一份匹配的催化剂。
     *
     * <p>源端只处理作物声明过的 {@code duplicationCatalysts}；这里在输入槽 1..
     * 找第一份匹配的物品（NBT 敏感），不要求必须存在 —— 有就扣，没有也照常出种子。
     */
    private void consumeCatalyst(ItemStack input) {
        CropType crop = CropRegistry.get(ItemCropSeed.getCropId(input));
        if (crop == null) {
            return;
        }
        List<ItemStack> catalysts = crop.getDuplicationCatalysts();
        if (catalysts.isEmpty()) {
            return;
        }
        for (int slot = MetaTileEntitySeedGenerator.SLOT_CATALYST;
             slot < machine.getImportItems().getSlots(); slot++) {
            ItemStack stack = machine.getImportItems().getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            for (ItemStack catalyst : catalysts) {
                if (ItemStack.areItemsEqual(stack, catalyst)
                        && ItemStack.areItemStackTagsEqual(stack, catalyst)) {
                    machine.getImportItems().extractItem(slot, 1, false);
                    return;
                }
            }
        }
    }
}
