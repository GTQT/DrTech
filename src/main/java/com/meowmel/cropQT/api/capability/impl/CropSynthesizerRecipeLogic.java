package com.meowmel.cropQT.api.capability.impl;

import com.meowmel.cropQT.api.CropGeneOrb;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.machine.MetaTileEntityCropSynthesizer;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 作物合成器的干活逻辑：4 个基因球 + UUM → 一颗完整的种子。
 *
 * <p>和提取器配对成闭环：提取器把种子拆成 4 个球，合成器再拼回去。
 *
 * <p><b>基因球不消耗</b>——它是可重复使用的模板，只有 UUM 被吃掉。
 * 这一点和提取器相反（提取器会把球吃掉），别搞混。
 *
 * <h2>「方案」每轮重算一次</h2>
 * 同育种机：{@link #canStart()} 顺手解析好方案存进 {@link #resolved}，
 * {@link #getWorkDuration()} / {@link #onWorkComplete()} 直接读。
 */
public class CropSynthesizerRecipeLogic extends CropMachineRecipeLogic<MetaTileEntityCropSynthesizer> {

    /** 每个属性点消耗的 UUM（mB）。 */
    public static final int UUM_PER_STAT = 100;
    /** 每点作物 tier 消耗的 UUM（mB）。 */
    public static final int UUM_PER_TIER = 750;

    /** 合成器按 3 安培跑。 */
    private static final int AMPERAGE = 3;

    /** 基准耗时（tick），tier 1 时为 5 分钟。 */
    private static final int BASE_DURATION = 6000;
    /** 耗时的对数底，tier 1→6000、tier 16→12000。 */
    private static final double DURATION_LOG_BASE = Math.log(16.0d);

    /** 本轮解析出来的合成方案；{@code null} 表示条件不满足。 */
    @Nullable
    private Resolved resolved;

    public CropSynthesizerRecipeLogic(MetaTileEntityCropSynthesizer machine, RecipeMap<?> recipeMap,
                                      Supplier<IEnergyContainer> energy) {
        super(machine, recipeMap, energy);
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
        // 5 分钟起步，按 log16 增长到 10 分钟
        double factor = 1.0 + Math.log(Math.max(1, resolved.crop.getTier())) / DURATION_LOG_BASE;
        return (int) (BASE_DURATION * factor);
    }

    @Override
    protected long getEnergyPerTick() {
        return resolved == null ? 0L : GTValues.V[minimumTierFor(resolved.crop)] * AMPERAGE;
    }

    @Override
    protected void onWorkComplete() {
        if (resolved == null) {
            return;
        }
        machine.getImportFluids().getTankAt(0).drain(resolved.uum, true);
        GTTransferUtils.insertItem(machine.getExportItems(), resolved.seed.copy(), false);
        // 基因球不消耗——它们是可重复使用的模板
        machine.markDirty();
    }

    // ==================== 方案解析 ====================

    /**
     * 解析 4 个槽里的球，凑齐物种 + 三围才返回方案。
     *
     * <p>槽位无关：球可以放任意槽，靠 NBT 里的 kind 分类。同类的第二个会被忽略。
     */
    @Nullable
    private Resolved resolve() {
        String speciesId = null;
        int growth = -1, gain = -1, resistance = -1;

        int slots = MetaTileEntityCropSynthesizer.INPUT_SLOT_COUNT;
        for (int slot = 0; slot < slots; slot++) {
            ItemStack orb = machine.getImportItems().getStackInSlot(slot);
            CropGeneOrb.Kind kind = CropGeneOrb.getKind(orb);
            if (kind == null) {
                continue;
            }
            switch (kind) {
                case SPECIES:
                    if (speciesId == null) speciesId = CropGeneOrb.getValue(orb);
                    break;
                case GROWTH:
                    if (growth < 0) growth = statOf(orb);
                    break;
                case GAIN:
                    if (gain < 0) gain = statOf(orb);
                    break;
                case RESISTANCE:
                    if (resistance < 0) resistance = statOf(orb);
                    break;
            }
        }

        if (speciesId == null || growth < 1 || gain < 1 || resistance < 1) {
            return null;
        }
        CropType crop = CropRegistry.get(speciesId);
        if (crop == null) {
            return null;
        }

        // 机器电压必须够这台作物要求的最低档
        if (machine.getTier() < minimumTierFor(crop)) {
            return null;
        }

        int uum = uumCost(crop, growth, gain, resistance);
        FluidStack stored = machine.getImportFluids().getTankAt(0).getFluid();
        if (stored == null || stored.getFluid() != Materials.UUMatter.getFluid() || stored.amount < uum) {
            return null;
        }

        ItemStack seed = ItemCropSeed.createSeedBag(speciesId, new CropStats(growth, gain, resistance).analyze());
        if (!GTTransferUtils.insertItem(machine.getExportItems(), seed, true).isEmpty()) {
            return null;
        }
        return new Resolved(speciesId, growth, gain, resistance, crop, uum, seed);
    }

    private static int statOf(ItemStack orb) {
        Integer value = CropGeneOrb.getStat(orb);
        return value == null ? -1 : value;
    }

    /**
     * 合成某作物需要的最低机器电压档。
     *
     * <p>源端取「作物的育种机档位」，我们没有那个字段，用作物 tier 作代理并夹在 EV~IV 之间
     * ——我们只做 EV / IV 两档，上界就是 IV。
     *
     * <p>提取器也用这个判据，所以它是 public static 的。
     */
    public static int minimumTierFor(CropType crop) {
        return Math.min(GTValues.IV, Math.max(GTValues.EV, crop.getTier()));
    }

    /** UUM 用量：tier 决定底量，三围决定增量。 */
    public static int uumCost(CropType crop, int growth, int gain, int resistance) {
        return crop.getTier() * UUM_PER_TIER + (growth + gain + resistance) * UUM_PER_STAT;
    }

    /** 一轮合成方案。 */
    private static final class Resolved {
        final String speciesId;
        final int growth;
        final int gain;
        final int resistance;
        final CropType crop;
        final int uum;
        final ItemStack seed;

        Resolved(String speciesId, int growth, int gain, int resistance, CropType crop, int uum, ItemStack seed) {
            this.speciesId = speciesId;
            this.growth = growth;
            this.gain = gain;
            this.resistance = resistance;
            this.crop = crop;
            this.uum = uum;
            this.seed = seed;
        }
    }
}
