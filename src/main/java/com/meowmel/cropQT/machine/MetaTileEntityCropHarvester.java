package com.meowmel.cropQT.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.meowmel.cropQT.api.capability.impl.CropHarvesterLogic;
import com.meowmel.cropQT.api.capability.impl.CropRangeLogic;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 作物收割机：范围收割成熟作物。
 *
 * <p>以机器为中心扫一个长方体——水平半径界面可调（上限 {@code (档位+1)²}，同物品收集器），
 * 垂直固定上下各 2 格。每 50 tick 过一遍范围内的作物架，把成熟的收进自己的输出槽。
 *
 * <h2>只管收割</h2>
 * 浇水、施肥、除草都不归它 —— 那些是作物监管机的活。要照顾作物就两台摆一起，
 * 半径调成一样大。
 *
 * <h2>没有 RecipeMap</h2>
 * 它不是配方机器，界面手绘。输出槽的网格布局照 {@code MetaTileEntityUniversalCollector}。
 */
public class MetaTileEntityCropHarvester extends MetaTileEntityCropRangeMachine {

    private final CropHarvesterLogic logic;

    public MetaTileEntityCropHarvester(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.logic = new CropHarvesterLogic(this);
        initializeInventory();
    }

    @Override
    public CropRangeLogic<?> getLogic() {
        return logic;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityCropHarvester(metaTileEntityId, getTier());
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected ICubeRenderer getOverlayRenderer() {
        // 收集者那一对贴图：收割本身就是「把东西收进来」
        return isActive() ? Textures.BLOWER_ACTIVE_OVERLAY : Textures.BLOWER_OVERLAY;
    }

    // ==================== 槽位 ====================

    /** 输出槽数随档位涨：LV 4 个，IV 36 个。同物品收集器的算法。 */
    public int getItemSize() {
        return (1 + getTier()) * (1 + getTier());
    }

    @Override
    protected @NotNull IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, getItemSize(), this, true);
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return true;
    }

    // ==================== 存档 ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        logic.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        logic.readFromNBT(data);
    }

    // ==================== 界面 ====================

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        int invTier = (int) Math.sqrt(getItemSize());
        syncManager.registerSlotGroup("cropqt_harvester_out", invTier);

        List<List<IWidget>> grid = new ArrayList<>();
        for (int row = 0; row < invTier; row++) {
            List<IWidget> cells = new ArrayList<>();
            for (int col = 0; col < invTier; col++) {
                cells.add(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(exportItems, row * invTier + col)
                                .slotGroup("cropqt_harvester_out")
                                .accessibility(false, true)));
            }
            grid.add(cells);
        }

        DoubleSyncValue progress = new DoubleSyncValue(logic::getProgressPercent);
        syncManager.syncValue("cropqt_harvester_progress", progress);

        IntSyncValue rangeSync = SyncHandlers.intNumber(this::getRange, this::setRange);
        syncManager.syncValue("cropqt_harvester_range", rangeSync);

        BooleanSyncValue autoItemsSync = new BooleanSyncValue(this::isAutoOutputItems, this::setAutoOutputItems);
        syncManager.syncValue("cropqt_harvester_auto_items", autoItemsSync);

        int gridHeight = invTier * 18;
        int backgroundHeight = 12 + 24 + 8 + gridHeight + 8 + 18 + 8 + 76 + 14;

        return GTGuis.createPanel(this, Math.max(176, 20 + invTier * 18 + 8 + 16), backgroundHeight)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                // 工作半径，照物品收集器
                .child(new ButtonWidget<>()
                        .pos(7, 18).size(16)
                        .onMousePressed(mouse -> {
                            rangeSync.setIntValue(rangeSync.getIntValue() - 1);
                            return true;
                        })
                        .overlay(IKey.str("-1")))
                .child(IKey.dynamic(() -> I18n.format("drtech.machine.crop_harvester.range",
                                rangeSync.getIntValue()))
                        .asWidget().pos(27, 22))
                .child(new ButtonWidget<>()
                        .pos(75, 18).size(16)
                        .onMousePressed(mouse -> {
                            rangeSync.setIntValue(rangeSync.getIntValue() + 1);
                            return true;
                        })
                        .overlay(IKey.str("+1")))
                .child(new ProgressWidget()
                        .value(progress)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, -1)
                        .direction(ProgressWidget.Direction.RIGHT)
                        .size(24, 16).pos(146, 18))
                .child(new Grid()
                        .top(44)
                        .height(gridHeight)
                        .size(invTier * 18, gridHeight)
                        .minElementMargin(0, 0)
                        .minColWidth(18)
                        .minRowHeight(18)
                        .leftRel(0.5f)
                        .matrix(grid))
                .child(new ToggleButton()
                        .pos(7, 44 + gridHeight + 8).size(18)
                        .value(new BoolValue.Dynamic(autoItemsSync::getBoolValue, autoItemsSync::setBoolValue))
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .tooltipAutoUpdate(true))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    // ==================== 展示 ====================

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.crop_harvester.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.crop_harvester.tooltip.2", getItemSize()));
        tooltip.add(I18n.format("drtech.machine.crop_harvester.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.crop_harvester.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.crop_harvester.tooltip.5", getMaxRange()));
    }
}
