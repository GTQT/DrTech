package com.drppp.drtech.common.MetaTileEntities.single;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import gregtech.common.mui.widget.GTFluidSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityUniversalCollector extends TieredMetaTileEntity {

    private static final int BASE_EU_CONSUMPTION = 6;
    private final int tier;
    private final int maxRange;
    // 状态控制
    private int range;
    private boolean autoOutputItems;
    private boolean autoOutputFluids;
    private EnumFacing outputFacingItems;
    private EnumFacing outputFacingFluids;
    private boolean isWorking;

    public MetaTileEntityUniversalCollector(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.tier = tier;
        this.maxRange = (int) Math.pow((getTier() + 1), 2);
        this.range = maxRange;
        initializeInventory();
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        FilteredFluidHandler[] fluidHandlers = new FilteredFluidHandler[getTankSize()];
        for (int i = 0; i < getTankSize(); i++) {
            fluidHandlers[i] = new FilteredFluidHandler(getTankCapacity());
        }
        exportFluids = new FluidTankList(false, fluidHandlers);
        exportItems = new GTItemStackHandler(this, getItemSize());
    }

    protected int getTankSize() {
        return 1 + Math.min(GTValues.UHV, getTier());
    }

    protected int getItemSize() {
        return getTankSize() * getTankSize();
    }

    protected int getTankCapacity() {
        return 8_000 * Math.min(Integer.MAX_VALUE, 1 << getTier());
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public boolean hasFrontFacing() {
        return true;
    }

    @Override
    public IItemHandlerModifiable getExportItems() {
        return exportItems;
    }

    @Override
    public FluidTankList getExportFluids() {
        return exportFluids;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityUniversalCollector(metaTileEntityId, tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SimpleOverlayRenderer renderer = isWorking ? Textures.BLOWER_ACTIVE_OVERLAY : Textures.BLOWER_OVERLAY;
        renderer.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.AIR_VENT_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        // 渲染自动输出状态
        if (isAutoOutputItems() && outputFacingItems != null) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(outputFacingItems, renderState,
                    RenderUtil.adjustTrans(translation, outputFacingItems, 2), pipeline);
        }
        if (isAutoOutputFluids() && outputFacingFluids != null) {
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(outputFacingFluids, renderState,
                    RenderUtil.adjustTrans(translation, outputFacingFluids, 2), pipeline);
        }
    }

    private void getRangeMetaTileEntity() {
        BlockPos centerPos = this.getPos();
        int radius = range;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos currentPos = centerPos.add(x, y, z);
                    MetaTileEntity te = GTUtility.getMetaTileEntity(this.getWorld(), currentPos);
                    if (te != null && !(te instanceof MetaTileEntityQuantumTank)
                            && !(te instanceof MetaTileEntityQuantumChest)
                            && !currentPos.equals(centerPos)) {
                        GTTransferUtils.moveInventoryItems(te.getExportItems(), this.exportItems);
                        GTTransferUtils.transferFluids(te.getExportFluids(), this.exportFluids);
                    }
                }
            }
        }
    }

    public void getStorageContainer(World world, BlockPos center) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adjacentPos = center.offset(facing);
            if (world.getTileEntity(adjacentPos) != null
                    && world.getTileEntity(adjacentPos).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
                var iInventory = world.getTileEntity(adjacentPos)
                        .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
                GTTransferUtils.moveInventoryItems(this.exportItems, iInventory);
            }
            if (world.getTileEntity(adjacentPos) != null
                    && world.getTileEntity(adjacentPos).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
                var fluidInventory = world.getTileEntity(adjacentPos)
                        .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
                GTTransferUtils.transferFluids(this.exportFluids, fluidInventory);
            }
            if (GTUtility.getMetaTileEntity(this.getWorld(), adjacentPos) != null
                    && GTUtility.getMetaTileEntity(this.getWorld(), adjacentPos) instanceof MetaTileEntityQuantumTank) {
                var s = GTUtility.getMetaTileEntity(this.getWorld(), adjacentPos);
                GTTransferUtils.transferFluids(this.exportFluids, s.getFluidInventory());
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        boolean isWorkingNow = energyContainer.getEnergyStored() >= getEnergyConsumedPerTick() &&
                isBlockRedstonePowered();

        // 每10 tick执行一次收集（避免频繁搜索造成卡顿）
        if (getOffsetTimer() % 10 == 0) {
            if (isWorkingNow) {
                energyContainer.removeEnergy(getEnergyConsumedPerTick());
                // 收集范围内的机器
                getRangeMetaTileEntity();
                // 收集相邻的容器
                getStorageContainer(getWorld(), getPos());
            }
        }

        // 自动输出
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputItems()) {
                pushItemsIntoNearbyHandlers(getOutputFacingItems());
            }
            if (isAutoOutputFluids()) {
                pushFluidsIntoNearbyHandlers(getOutputFacingFluids());
            }
        }

        if (isWorkingNow != isWorking) {
            this.isWorking = isWorkingNow;
            writeCustomData(IS_WORKING, buffer -> buffer.writeBoolean(isWorkingNow));
        }
    }

    protected int getEnergyConsumedPerTick() {
        return BASE_EU_CONSUMPTION * (1 << (getTier() - 1));
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getOutputFacingItems() == facing) return false;
            if (hasFrontFacing() && facing == getFrontFacing()) return false;
            if (!getWorld().isRemote) {
                setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        this.outputFacingFluids = outputFacing;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> {
                buf.writeByte(outputFacingItems.getIndex());
                buf.writeByte(outputFacingFluids.getIndex());
            });
            markDirty();
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacingItems == null || this.outputFacingFluids == null) {
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    public EnumFacing getOutputFacingItems() {
        return outputFacingItems == null ? getFrontFacing().getOpposite() : outputFacingItems;
    }

    public EnumFacing getOutputFacingFluids() {
        return outputFacingFluids == null ? getFrontFacing().getOpposite() : outputFacingFluids;
    }

    // ==================== 自动输出控制 ====================

    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutputItems));
            markDirty();
        }
    }

    public boolean isAutoOutputFluids() {
        return autoOutputFluids;
    }

    public void setAutoOutputFluids(boolean autoOutputFluids) {
        this.autoOutputFluids = autoOutputFluids;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_FLUIDS, buf -> buf.writeBoolean(autoOutputFluids));
            markDirty();
        }
    }

    // ==================== 范围控制 ====================

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = MathHelper.clamp(range, 1, maxRange);
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    // ==================== UI 构建（仅保留槽位、范围选择、自动输出按钮）====================

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        int invTier = (int) Math.sqrt(getItemSize());
        guiSyncManager.registerSlotGroup("item_inv", invTier);

        List<List<IWidget>> widgetsItem = new ArrayList<>();
        for (int i = 0; i < invTier; i++) {
            widgetsItem.add(new ArrayList<>());
            for (int j = 0; j < invTier; j++) {
                int index = i * invTier + j;

                IItemHandlerModifiable handler = exportItems;
                widgetsItem.get(i)
                        .add(new ItemSlot()
                                .slot(SyncHandlers.itemSlot(handler, index)
                                        .slotGroup("item_inv")
                                        .accessibility(true, true)));
            }
            IFluidTank tankHandler = exportFluids.getTankAt(i);
            widgetsItem.get(i).add(new GTFluidSlot()
                    .syncHandler(GTFluidSlot.sync(tankHandler)
                            .accessibility(true, true))
            );
        }


        // 范围选择同步值
        IntSyncValue rangeSync = SyncHandlers.intNumber(this::getRange, this::setRange);
        guiSyncManager.syncValue("range", 0, rangeSync);

        // 自动输出同步值
        BooleanSyncValue workingStateValueItems = new BooleanSyncValue(this::isAutoOutputItems,
                this::setAutoOutputItems);
        guiSyncManager.syncValue("working_state_items", workingStateValueItems);

        BooleanSyncValue workingStateValueFluids = new BooleanSyncValue(this::isAutoOutputFluids,
                this::setAutoOutputFluids);
        guiSyncManager.syncValue("working_state_fluids", workingStateValueFluids);

        // 计算UI高度

        int backgroundHeight = 25 + 18 + 24 + 18 * invTier + 94;

        return GTGuis.createPanel(this, Math.max(176, 10 + 18 * (invTier + 1)), backgroundHeight)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(Flow.column()
                        .margin(5)
                        .child(Flow.row()
                                .widthRel(1.0f)
                                .top(13)
                                .height(24)
                                .child(new ButtonWidget<>()
                                        .marginRight(5)
                                        .size(20)
                                        .onMousePressed(mouse -> {
                                            int range = rangeSync.getIntValue();
                                            if (range > 1) {
                                                rangeSync.setIntValue(range - 1);
                                            }
                                            return true;
                                        })
                                        .overlay(IKey.str("-1")))
                                .child(KeyUtil.lang(TextFormatting.WHITE,
                                                "gregtech.machine.item_collector.gui.collect_range",
                                                () -> new Object[]{
                                                        TextFormattingUtil.formatNumbers(rangeSync.getIntValue())})
                                        .alignment(Alignment.Center)
                                        .asWidget()
                                        .height(20)
                                        .expanded()
                                        .background(GTGuiTextures.DISPLAY))
                                .child(new ButtonWidget<>()
                                        .marginLeft(5)
                                        .size(20)
                                        .onMousePressed(mouse -> {
                                            int range = rangeSync.getIntValue();
                                            if (range < maxRange) {
                                                rangeSync.setIntValue(range + 1);
                                            }
                                            return true;
                                        })
                                        .overlay(IKey.str("+1")))
                        )

                        // 槽位
                        .child(new Grid()
                                .top(10 + 24 + 8)
                                .height(invTier * 18)
                                .size((invTier + 1) * 18, invTier * 18)
                                .minElementMargin(0, 0)
                                .minColWidth(18)
                                .minRowHeight(18)
                                .leftRel(0.5f)
                                .matrix(widgetsItem))

                        // 自动输出按钮
                        .child(Flow.row()
                                .pos(5, 23 + 24 + 18 * invTier)
                                .size(36, 18)
                                .child(new ToggleButton()
                                        .value(new BoolValue.Dynamic(workingStateValueItems::getBoolValue,
                                                workingStateValueItems::setBoolValue))
                                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                                        .tooltipAutoUpdate(true))
                                .child(new ToggleButton()
                                        .value(new BoolValue.Dynamic(workingStateValueFluids::getBoolValue,
                                                workingStateValueFluids::setBoolValue))
                                        .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                                        .tooltipAutoUpdate(true)))

                )

                // 玩家背包
                .child(SlotGroupWidget.playerInventory(false)
                        .bottom(7)
                        .left(7)
                );

    }

    // ==================== 数据同步 ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Range", range);
        tag.setInteger("OutputFacing", getOutputFacingItems().getIndex());
        tag.setInteger("OutputFacingF", getOutputFacingFluids().getIndex());
        tag.setBoolean("AutoOutputItems", autoOutputItems);
        tag.setBoolean("AutoOutputFluids", autoOutputFluids);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.range = tag.getInteger("Range");
        this.outputFacingItems = EnumFacing.VALUES[tag.getInteger("OutputFacing")];
        this.outputFacingFluids = EnumFacing.VALUES[tag.getInteger("OutputFacingF")];
        this.autoOutputItems = tag.getBoolean("AutoOutputItems");
        this.autoOutputFluids = tag.getBoolean("AutoOutputFluids");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isWorking);
        buf.writeBoolean(autoOutputFluids);
        buf.writeBoolean(autoOutputItems);
        buf.writeByte(getOutputFacingFluids().getIndex());
        buf.writeByte(getOutputFacingItems().getIndex());
        buf.writeInt(range);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isWorking = buf.readBoolean();
        this.autoOutputFluids = buf.readBoolean();
        this.autoOutputItems = buf.readBoolean();
        this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
        this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
        this.range = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
            this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutputItems = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == IS_WORKING) {
            this.isWorking = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.universal_collector.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick", getEnergyConsumedPerTick()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.max_voltage_in", energyContainer.getInputVoltage(),
                GTValues.VNF[getTier()]));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", getItemSize()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity_mult", getTankSize(),
                getTankCapacity()));
        tooltip.add(I18n.format("gregtech.machine.universal_collector.tooltip.range", maxRange));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}