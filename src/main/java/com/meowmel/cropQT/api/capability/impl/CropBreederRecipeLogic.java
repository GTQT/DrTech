package com.meowmel.cropQT.api.capability.impl;

import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.mutation.CropMutation;
import com.meowmel.cropQT.api.mutation.MutationRegistry;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
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
 * <p>产物由 {@link MutationRegistry} 的确定性配方决定——与世界里的作物架走同一张配方表，
 * 所以玩家在田里试出来的组合，在机器上照样成立。
 *
 * <p><b>失败也消耗材料</b>：掷骰没中照样扣亲本与液肥。这是源端行为，
 * 也是「育种是有成本的事」这个设计的一部分。
 *
 * <h2>「方案」每轮重算一次</h2>
 * {@link #canStart()} 顺手把这一轮的方案解析好放进 {@link #resolved}，
 * 后面的 {@link #getWorkDuration()} / {@link #onWorkComplete()} 直接读它。
 * 状态机每轮会问两次 {@code canStart()}（开工前、收尾前），所以收尾时读到的是
 * 按<b>当时</b>的槽位重新算过的方案 —— 玩家中途换了亲本，产出就跟着换。
 */
public class CropBreederRecipeLogic extends CropMachineRecipeLogic<MetaTileEntityCropBreeder> {

    /** 每点作物 tier 消耗的液肥（mB，按浓缩液肥计）。 */
    public static final int FERTILIZER_PER_TIER = 144;
    /** 每点属性消耗的液肥。 */
    public static final int FERTILIZER_PER_STAT = FERTILIZER_PER_TIER / 2;

    /** 浓缩液体肥料的浓度基线（点数/mB）。普通液体肥料按浓度折算成更多 mB。 */
    private static final int BASELINE_POTENCY = 8;

    /** 成功率 = min(100, 40 + (档位 - LV) * 10)。 */
    private static final int MIN_CHANCE = 40;
    private static final int MAX_CHANCE = 100;
    private static final int CHANCE_PER_TIER = 10;

    /** 基准耗时（tick），每个档位 ×1.3。 */
    private static final int BASE_DURATION = 400;

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
        if (resolved == null) {
            return -1;
        }
        // 产物越高级，一轮越久
        return (int) (BASE_DURATION * Math.pow(1.3, Math.max(0, resolved.output.getTier() - 1)));
    }

    @Override
    protected long getEnergyPerTick() {
        return resolved == null ? 0L : GTValues.V[machine.getTier()];
    }

    @Override
    protected void onWorkComplete() {
        if (resolved == null) {
            return;
        }
        // 无论成功与否都要扣料
        machine.getImportFluids().getTankAt(0).drain(resolved.fluid, true);
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
        }
        // 同种亲本允许重复——配方表里本来就有 stickreed × stickreed 这种
        if (parentIds.size() < CropMutation.MIN_PARENTS) {
            return null;
        }

        CropMutation mutation = MutationRegistry.pickDeterministic(parentIds, machine.getWorld().rand);
        if (mutation == null) {
            return null;
        }
        CropType output = CropRegistry.get(mutation.getResult());
        if (output == null) {
            return null;
        }

        CropStats childStats = averageStats(parentStats);
        int fluid = fluidCost(output, childStats);
        FluidStack stored = machine.getImportFluids().getTankAt(0).getFluid();
        int potency = stored == null ? 0 : FertilizerRegistry.getFertilizer(stored.getFluid());
        if (potency <= 0 || stored.amount < fluid) {
            return null;
        }

        ItemStack child = ItemCropSeed.createSeedBag(mutation.getResult(), childStats);
        if (!GTTransferUtils.insertItem(machine.getExportItems(), child, true).isEmpty()) {
            return null;
        }
        return new Resolved(mutation, output, childStats, fluid, child);
    }

    /** 子代属性 = 各亲本属性的算术平均，向下取整。 */
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
     * 一次育种的液肥用量（mB）。
     *
     * <p>按浓度折算：浓缩液体肥料是基线，普通液体肥料要按比例多消耗。
     */
    private int fluidCost(CropType output, CropStats stats) {
        int base = Math.max(1, output.getTier() * FERTILIZER_PER_TIER)
                + Math.max(1, (stats.getGrowth() + stats.getGain() + stats.getResistance()) * FERTILIZER_PER_STAT);
        FluidStack stored = machine.getImportFluids().getTankAt(0).getFluid();
        int potency = stored == null ? BASELINE_POTENCY : FertilizerRegistry.getFertilizer(stored.getFluid());
        if (potency <= 0) {
            return base;
        }
        return Math.max(1, base * BASELINE_POTENCY / potency);
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

        Resolved(CropMutation mutation, CropType output, CropStats stats, int fluid, ItemStack child) {
            this.mutation = mutation;
            this.output = output;
            this.stats = stats;
            this.fluid = fluid;
            this.child = child;
        }
    }
}
