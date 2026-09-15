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
import gregtech.api.util.GTTransferUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Supplier;

/**
 * 基因提取机的干活逻辑：把一颗已分析的种子拆成 4 个基因球之一。
 *
 * <p>一次只提取一样——用界面上的按钮切换要抽的是物种还是某一项属性。
 *
 * <p><b>基因球会被消耗</b>——和合成器相反（合成器的球是反复用的模板）。
 *
 * <h2>模式是即时生效的</h2>
 * {@link #getMode()} 在 {@link #canStart()} 和 {@link #onWorkComplete()} 里各读一次，
 * 所以玩家在一轮跑到一半时切模式，出来的球就跟着变，不用等这一轮跑完。
 * 这是刻意的：按钮就在界面上，想让玩家点得动。
 */
public class CropGeneExtractorRecipeLogic extends CropMachineRecipeLogic<MetaTileEntityCropGeneExtractor> {

    /** 抽取物种要 5 分钟，抽单项属性减半。 */
    public static final int DURATION_SPECIES = 6000;
    public static final int DURATION_STAT = DURATION_SPECIES / 2;

    /** 抽取模式：物种 / 生长 / 产量 / 抗性。 */
    public static final int MODE_SPECIES = 0;
    public static final int MODE_GROWTH = 1;
    public static final int MODE_GAIN = 2;
    public static final int MODE_RESISTANCE = 3;
    public static final int MODE_COUNT = 4;

    /** 当前抽取模式，界面可切换。 */
    private int mode = MODE_SPECIES;

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
        return GTTransferUtils.insertItem(machine.getExportItems(), orbFor(cropId, stats), true).isEmpty();
    }

    @Override
    protected int getWorkDuration() {
        return mode == MODE_SPECIES ? DURATION_SPECIES : DURATION_STAT;
    }

    @Override
    protected long getEnergyPerTick() {
        CropType crop = CropRegistry.get(ItemCropSeed.getCropId(input()));
        return crop == null ? 0L : GTValues.V[CropSynthesizerRecipeLogic.minimumTierFor(crop)];
    }

    @Override
    protected void onWorkComplete() {
        ItemStack input = input();
        ItemStack orb = orbFor(ItemCropSeed.getCropId(input), ItemCropSeed.getCropStats(input));

        machine.getImportItems().extractItem(MetaTileEntityCropGeneExtractor.SLOT_INPUT, 1, false);
        GTTransferUtils.insertItem(machine.getExportItems(), orb, false);
        machine.markDirty();
    }

    // ==================== 模式 ====================

    public int getMode() {
        return mode;
    }

    /** 螺丝刀切下一档。 */
    public void cycleMode() {
        setMode(mode + 1);
    }

    public void setMode(int mode) {
        this.mode = Math.floorMod(mode, MODE_COUNT);
        machine.markDirty();
    }

    /** 当前模式的 lang 键，给聊天栏提示用。 */
    public String currentModeNameKey() {
        return modeNameKey(mode);
    }

    /** 模式名，用于聊天栏提示与 tooltip。 */
    public static String modeNameKey(int mode) {
        switch (mode) {
            case MODE_GROWTH:     return "cropqt.gene.mode.growth";
            case MODE_GAIN:       return "cropqt.gene.mode.gain";
            case MODE_RESISTANCE: return "cropqt.gene.mode.resistance";
            case MODE_SPECIES:
            default:              return "cropqt.gene.mode.species";
        }
    }

    // ==================== 存档 ====================

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = super.serializeNBT();
        data.setInteger("GeneMode", mode);
        return data;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        mode = compound.getInteger("GeneMode");
    }

    // ==================== 材料 ====================

    private ItemStack input() {
        return machine.getImportItems().getStackInSlot(MetaTileEntityCropGeneExtractor.SLOT_INPUT);
    }

    /** 按当前模式造出结果球。 */
    private ItemStack orbFor(String cropId, CropStats stats) {
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
