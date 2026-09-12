package com.meowmel.cropQT.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.CropGeneOrb;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.item.ItemCropSeed;
import gregtech.api.GTValues;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 基因提取器：把一颗已分析的种子拆成 4 个基因球之一。
 *
 * <p>一次只提取一样——用界面上的按钮切换要抽的是物种还是某一项属性。
 * 源端用「电路槽选 1~4」，这里改成按钮：电路槽要接 GT 的幽灵电路体系，
 * 而本系统这 4 台机器都不走 RecipeMap，为它单独接一套不划算。
 *
 * <p><b>基因球会被消耗</b>——和合成器相反（合成器的球是反复用的模板）。
 */
public class MetaTileEntityCropGeneExtractor extends MetaTileEntityCropMachine {

    private static final int SLOT_INPUT = 0;
    private static final int SLOT_OUTPUT = 0;

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

    public MetaTileEntityCropGeneExtractor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityCropGeneExtractor(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.gene_extractor.tooltip.6"));
    }

    // ==================== 槽位 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        // 只收「已分析」的种子袋
        return new GTItemStackHandler(this, 1) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return !ItemCropSeed.getCropId(stack).isEmpty() && ItemCropSeed.getCropStats(stack).isAnalyzed();
            }
        };
    }

    @Override
    protected @NotNull IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    // ==================== 工作逻辑 ====================

    @Override
    protected boolean canStart() {
        ItemStack input = importItems.getStackInSlot(SLOT_INPUT);
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
        if (getTier() < MetaTileEntityCropSynthesizer.minimumTierFor(crop)) {
            return false;
        }
        return GTTransferUtils.insertItem(exportItems, orbFor(cropId, stats), true).isEmpty();
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

    @Override
    protected int getWorkDuration() {
        return mode == MODE_SPECIES ? DURATION_SPECIES : DURATION_STAT;
    }

    @Override
    protected long getEnergyPerTick() {
        CropType crop = CropRegistry.get(ItemCropSeed.getCropId(importItems.getStackInSlot(SLOT_INPUT)));
        return crop == null ? 0L : GTValues.V[MetaTileEntityCropSynthesizer.minimumTierFor(crop)];
    }

    @Override
    protected void onWorkComplete() {
        ItemStack input = importItems.getStackInSlot(SLOT_INPUT);
        ItemStack orb = orbFor(ItemCropSeed.getCropId(input), ItemCropSeed.getCropStats(input));

        input.shrink(1);
        GTTransferUtils.insertItem(exportItems, orb, false);
        markDirty();
    }

    // ==================== 模式 ====================

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = Math.floorMod(mode, MODE_COUNT);
        markDirty();
    }

    /** 模式名，用于界面与 tooltip。 */
    public static String modeNameKey(int mode) {
        switch (mode) {
            case MODE_GROWTH:     return "cropqt.gene.mode.growth";
            case MODE_GAIN:       return "cropqt.gene.mode.gain";
            case MODE_RESISTANCE: return "cropqt.gene.mode.resistance";
            case MODE_SPECIES:
            default:              return "cropqt.gene.mode.species";
        }
    }

    // ==================== UI ====================

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        DoubleSyncValue progress = registerProgressSync(syncManager);
        IntSyncValue modeSync = SyncHandlers.intNumber(this::getMode, this::setMode);
        syncManager.syncValue("cropqt_gene_mode", modeSync);

        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(6, 6))
                .child(new ItemSlot()
                        .pos(40, 24)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(importItems, SLOT_INPUT)
                                .singletonSlotGroup().accessibility(true, true)))
                .child(new ProgressWidget()
                        .value(progress)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, -1)
                        .direction(ProgressWidget.Direction.RIGHT)
                        .size(24, 16).pos(76, 26))
                .child(new ItemSlot()
                        .pos(116, 24)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(exportItems, SLOT_OUTPUT)
                                .singletonSlotGroup().accessibility(false, true)))
                .child(new ButtonWidget<>()
                        .pos(40, 50).size(100, 16)
                        .background(GTGuiTextures.DISPLAY)
                        .overlay(IKey.dynamic(() -> net.minecraft.client.resources.I18n.format(
                                modeNameKey(modeSync.getIntValue()))))
                        .onMousePressed(mouse -> {
                            modeSync.setIntValue(modeSync.getIntValue() + 1);
                            return true;
                        }))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    // ==================== 存档 ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("GeneMode", mode);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        mode = data.getInteger("GeneMode");
    }

    @Override
    public OrientedOverlayRenderer getOverlayRenderer() {
        return Textures.GENE_EXTRACTOR_OVERLAY;
    }
}
