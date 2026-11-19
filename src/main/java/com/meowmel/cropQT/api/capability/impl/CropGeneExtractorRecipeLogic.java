package com.meowmel.cropQT.api.capability.impl;

import com.meowmel.cropQT.api.CropGeneOrb;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.machine.MetaTileEntityCropGeneExtractor;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTTransferUtils;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

/**
 * 基因提取机的干活逻辑：把一颗已分析的种子拆成 4 个基因球之一。
 *
 * <p>抽哪一项由<b>电路槽里的编程电路</b>决定，对齐源端 {@code MTECropGeneExtractor}：
 * 1 = 物种，2 = 生长，3 = 产量，4 = 抗性。数据球放第二个输入槽，
 * 抽完消耗一颗种子 + 一个空数据球，产出一个带内容的基因球。
 *
 * <p>机器电压必须够这台作物要求的「育种机档位」；耗时物种 5 分钟、单项属性 2.5 分钟。
 */
public class CropGeneExtractorRecipeLogic extends CropMachineRecipeLogic<MetaTileEntityCropGeneExtractor> {

    /** 抽取物种要 5 分钟，抽单项属性减半。 */
    public static final int DURATION_SPECIES = 6000;
    public static final int DURATION_STAT = DURATION_SPECIES / 2;

    /** 电路值 → 抽取模式。 */
    public static final int CIRCUIT_MIN = 1;
    public static final int CIRCUIT_MAX = 4;

    /** 抽取模式：物种 / 生长 / 产量 / 抗性。 */
    public static final int MODE_SPECIES = 0;
    public static final int MODE_GROWTH = 1;
    public static final int MODE_GAIN = 2;
    public static final int MODE_RESISTANCE = 3;
    public static final int MODE_COUNT = 4;

    public CropGeneExtractorRecipeLogic(MetaTileEntityCropGeneExtractor machine, RecipeMap<?> recipeMap,
                                        Supplier<IEnergyContainer> energy) {
        super(machine, recipeMap, energy);
    }

    // ==================== 状态机 ====================

    @Override
    protected boolean canStart() {
        ItemStack input = input();
        if (input.isEmpty()) {
            return false;
        }
        CropStats stats = ItemCropSeed.getCropStats(input);
        String cropId = ItemCropSeed.getCropId(input);
        if (!stats.isAnalyzed() || cropId.isEmpty()) {
            return false;
        }
        CropType crop = CropRegistry.get(cropId);
        if (crop == null) {
            return false;
        }
        // 机器电压要够这台作物要求的最低档
        if (machine.getTier() < CropSynthesizerRecipeLogic.minimumTierFor(crop)) {
            return false;
        }
        if (!hasDataOrb()) {
            return false;
        }
        int mode = getMode();
        if (mode < 0) {
            return false;
        }
        return GTTransferUtils.insertItem(machine.getExportItems(), orbFor(cropId, stats, mode), true).isEmpty();
    }

    @Override
    protected int getWorkDuration() {
        int base = getMode() == MODE_SPECIES ? DURATION_SPECIES : DURATION_STAT;
        // 源端 calculateOverclockedNess(VP[minTier], getRecipeDuration(circuit))：
        // 机器档位高于作物要求档位时按 EU×4 / 时长÷2 超频
        return overclock(baseEutPerTick(), base, GTValues.V[machine.getTier()])[1];
    }

    @Override
    protected long getEnergyPerTick() {
        return overclock(baseEutPerTick(), 1, GTValues.V[machine.getTier()])[0];
    }

    /** 源端 VP[作物要求的最低档]；GTQT 的 VA 与 GT5U 的 VP 数值相同。 */
    private long baseEutPerTick() {
        CropType crop = CropRegistry.get(ItemCropSeed.getCropId(input()));
        return crop == null ? 0L : GTValues.VA[CropSynthesizerRecipeLogic.minimumTierFor(crop)];
    }

    @Override
    protected void onWorkComplete() {
        ItemStack input = input();
        int mode = getMode();
        ItemStack orb = orbFor(ItemCropSeed.getCropId(input), ItemCropSeed.getCropStats(input),
                Math.max(MODE_SPECIES, mode));

        // 消耗一颗种子和一个空数据球
        machine.getImportItems().extractItem(MetaTileEntityCropGeneExtractor.SLOT_INPUT, 1, false);
        machine.getImportItems().extractItem(MetaTileEntityCropGeneExtractor.SLOT_DATA_ORB, 1, false);
        GTTransferUtils.insertItem(machine.getExportItems(), orb, false);
        machine.markDirty();
    }

    // ==================== 模式（编程电路） ====================

    /**
     * 当前模式，由电路槽里的编程电路决定。
     *
     * @return 0..3；没有电路或电路值越界时返回 -1
     */
    public int getMode() {
        ItemStack circuit = machine.getImportItems()
                .getStackInSlot(MetaTileEntityCropGeneExtractor.SLOT_CIRCUIT);
        if (circuit.isEmpty() || !MetaItems.INTEGRATED_CIRCUIT.isItemEqual(circuit)) {
            return -1;
        }
        int config = IntCircuitIngredient.getCircuitConfiguration(circuit);
        if (config < CIRCUIT_MIN || config > CIRCUIT_MAX) {
            return -1;
        }
        return config - CIRCUIT_MIN;
    }

    // ==================== 材料 ====================

    private ItemStack input() {
        return machine.getImportItems().getStackInSlot(MetaTileEntityCropGeneExtractor.SLOT_INPUT);
    }

    /** 数据球槽里必须放一个空数据球（或任意可写的数据球）。 */
    private boolean hasDataOrb() {
        ItemStack orb = machine.getImportItems()
                .getStackInSlot(MetaTileEntityCropGeneExtractor.SLOT_DATA_ORB);
        return !orb.isEmpty() && MetaItems.TOOL_DATA_ORB.isItemEqual(orb);
    }

    /** 按模式造出结果球。 */
    private ItemStack orbFor(String cropId, CropStats stats, int mode) {
        switch (mode) {
            case MODE_GROWTH:
                return CropGeneOrb.createStat(CropGeneOrb.Kind.GROWTH, stats.getGrowth());
            case MODE_GAIN:
                return CropGeneOrb.createStat(CropGeneOrb.Kind.GAIN, stats.getGain());
            case MODE_RESISTANCE:
                return CropGeneOrb.createStat(CropGeneOrb.Kind.RESISTANCE, stats.getResistance());
            case MODE_SPECIES:
            default:
                return CropGeneOrb.create(CropGeneOrb.Kind.SPECIES, cropId);
        }
    }
}
