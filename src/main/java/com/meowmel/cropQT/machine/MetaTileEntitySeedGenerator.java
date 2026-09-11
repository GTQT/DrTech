package com.meowmel.cropQT.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import com.meowmel.cropQT.item.ItemCropSeed;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.mui.widget.GTFluidSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 种子生成器：复制一份已分析的种子袋。
 *
 * <p>消耗液体肥料，把输入槽里的种子袋原样复制一份到输出槽。
 * 未分析的种子不给复制——「先分析再扩繁」是这条链的入口约束。
 */
public class MetaTileEntitySeedGenerator extends MetaTileEntityCropMachine {

    /** 输入槽：待复制的种子袋。 */
    private static final int SLOT_INPUT = 0;
    /** 输出槽：复制出来的种子袋。 */
    private static final int SLOT_OUTPUT = 0;

    /** 肥料罐容量，16 桶。 */
    private static final int TANK_CAPACITY = 16000;

    /** 每复制一份要消耗的液体肥料（mB）。 */
    public static final int FERTILIZER_PER_COPY = 50;

    /** 基准耗时（tier 0）。每高一档减半，最低 20 tick。 */
    private static final int BASE_DURATION = 200;

    public MetaTileEntitySeedGenerator(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntitySeedGenerator(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.seed_generator.tooltip.6"));
    }

    // ==================== 槽位与罐 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    @Override
    protected @NotNull IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    @Override
    protected @NotNull FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new FluidTank(TANK_CAPACITY));
    }

    // ==================== 工作逻辑 ====================

    @Override
    protected boolean canStart() {
        ItemStack input = importItems.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty() || !hasEnoughFertilizer()) {
            return false;
        }
        // 只扩繁已分析的种子
        if (!ItemCropSeed.getCropStats(input).isAnalyzed()) {
            return false;
        }
        return canOutput(input);
    }

    /** 产物槽能不能再塞一份同样的种子袋。用模拟插入判定，与真正写入走同一条路。 */
    private boolean canOutput(ItemStack input) {
        return GTTransferUtils.insertItem(exportItems, copyOf(input), true).isEmpty();
    }

    /** 按输入种子袋造一份复制品。 */
    private static ItemStack copyOf(ItemStack input) {
        return ItemCropSeed.createSeedBag(ItemCropSeed.getCropId(input), ItemCropSeed.getCropStats(input));
    }

    /** 罐里是不是液体肥料，且够一次的量。 */
    private boolean hasEnoughFertilizer() {
        FluidStack stored = importFluids.getTankAt(0).getFluid();
        if (stored == null || stored.amount < FERTILIZER_PER_COPY) {
            return false;
        }
        return FertilizerRegistry.getFertilizer(stored.getFluid()) > 0;
    }

    @Override
    protected int getWorkDuration() {
        return Math.max(20, BASE_DURATION >> Math.max(0, getTier() - GTValues.LV));
    }

    @Override
    protected long getEnergyPerTick() {
        return GTValues.V[getTier()];
    }

    @Override
    protected void onWorkComplete() {
        ItemStack input = importItems.getStackInSlot(SLOT_INPUT);
        ItemStack copy = copyOf(input);
        if (copy.isEmpty()) {
            return;
        }

        importItems.extractItem(SLOT_INPUT, 1, false);
        importFluids.getTankAt(0).drain(FERTILIZER_PER_COPY, true);
        GTTransferUtils.insertItem(exportItems, copy, false);
        markDirty();
    }

    // ==================== UI ====================

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        DoubleSyncValue progress = registerProgressSync(syncManager);

        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(6, 6))
                .child(new ItemSlot()
                        .pos(40, 24)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(importItems, SLOT_INPUT)
                                .singletonSlotGroup().accessibility(true, false)))
                .child(new ItemSlot()
                        .pos(116, 24)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(exportItems, SLOT_OUTPUT)
                                .singletonSlotGroup().accessibility(false, true)))
                .child(new GTFluidSlot()
                        .pos(40, 52)
                        .size(18, 36)
                        .syncHandler(GTFluidSlot.sync(importFluids.getTankAt(0))
                                .showAmountOnSlot(false).accessibility(false, true)))
                .child(new ProgressWidget()
                        .value(progress)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, -1)
                        .direction(ProgressWidget.Direction.RIGHT)
                        .size(24, 16).pos(76, 26))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    @Override
    public OrientedOverlayRenderer getOverlayRenderer() {
        return Textures.SEED_GENERATOR_OVERLAY;
    }
}
