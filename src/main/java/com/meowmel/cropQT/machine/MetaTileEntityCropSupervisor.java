package com.meowmel.cropQT.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
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
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.meowmel.cropQT.api.capability.impl.CropRangeLogic;
import com.meowmel.cropQT.api.capability.impl.CropSupervisorLogic;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.mui.widget.GTFluidSlot;
import gregtechfoodoption.client.GTFOClientHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 作物监管机：范围浇水、施肥、除草。
 *
 * <p>以机器为中心扫一个长方体——水平半径界面可调（上限 {@code (档位+1)²}，同物品收集器），
 * 垂直固定上下各 2 格。每 50 tick 过一遍范围内的作物架：
 * 给缺水的补水、给缺肥的补肥、把长出来的杂草清掉。
 *
 * <h2>三件事各自独立开关，默认只开浇水</h2>
 * 肥是要花资源做的，机器不该闷声吃光。除草默认也关着——它会把架子上的草直接清掉，
 * 万一玩家自己种的什么被判定成草就糟了。
 *
 * <h2>没有 RecipeMap</h2>
 * 它不是配方机器，界面手绘，照 {@link MetaTileEntityCropRangeMachine} 那一套。
 */
public class MetaTileEntityCropSupervisor extends MetaTileEntityCropRangeMachine {

    /** 水罐容量 / 每档。 */
    private static final int WATER_CAPACITY_PER_TIER = 32000;
    /** 液肥罐容量 / 每档。 */
    private static final int FERTILIZER_CAPACITY_PER_TIER = 144 * 64 * 4;

    /** 水罐在 {@code importFluids} 里的下标。 */
    public static final int TANK_WATER = 0;
    /** 液肥罐在 {@code importFluids} 里的下标。 */
    public static final int TANK_FERTILIZER = 1;

    private final CropSupervisorLogic logic;

    public MetaTileEntityCropSupervisor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.logic = new CropSupervisorLogic(this);
        initializeInventory();
    }

    @Override
    public CropRangeLogic<?> getLogic() {
        return logic;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityCropSupervisor(metaTileEntityId, getTier());
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected ICubeRenderer getOverlayRenderer() {
        return GTFOClientHandler.SPRINKLER_OVERLAY;
    }

    // ==================== 罐 ====================

    public int getWaterCapacity() {
        return getTier() * WATER_CAPACITY_PER_TIER;
    }

    public int getFertilizerCapacity() {
        return getTier() * FERTILIZER_CAPACITY_PER_TIER;
    }

    public FluidTank waterTank() {
        return importFluids.getTankAt(TANK_WATER) instanceof FluidTank tank ? tank : null;
    }

    public FluidTank fertilizerTank() {
        return importFluids.getTankAt(TANK_FERTILIZER) instanceof FluidTank tank ? tank : null;
    }

    @Override
    protected @NotNull FluidTankList createImportFluidHandler() {
        return new FluidTankList(false,
                new NotifiableFluidTank(getWaterCapacity(), this, false),
                new NotifiableFluidTank(getFertilizerCapacity(), this, false));
    }

    // ==================== 槽位 ====================

    /** 输入槽数随档位涨：LV 2 个，IV 6 个。 */
    public int getInputSlots() {
        return 1 + getTier();
    }

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, getInputSlots(), this, false) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                // 只收登记过的固体肥料（GT 肥料 / 骨粉），别把种子什么的塞进来
                return FertilizerRegistry.getFertilizer(stack) > 0;
            }
        };
    }

    /** 第一个装着固体肥料的槽；没有返回 -1。 */
    public int findSolidFertilizerSlot() {
        for (int slot = 0; slot < importItems.getSlots(); slot++) {
            if (FertilizerRegistry.getFertilizer(importItems.getStackInSlot(slot)) > 0) {
                return slot;
            }
        }
        return -1;
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
        DoubleSyncValue progress = new DoubleSyncValue(logic::getProgressPercent);
        syncManager.syncValue("cropqt_supervisor_progress", progress);

        BooleanSyncValue waterSync = new BooleanSyncValue(logic::isWaterEnabled, logic::setWaterEnabled);
        BooleanSyncValue fertSync = new BooleanSyncValue(logic::isFertilizerEnabled, logic::setFertilizerEnabled);
        BooleanSyncValue weedSync = new BooleanSyncValue(logic::isWeedingEnabled, logic::setWeedingEnabled);
        syncManager.syncValue("cropqt_supervisor_water", waterSync);
        syncManager.syncValue("cropqt_supervisor_fert", fertSync);
        syncManager.syncValue("cropqt_supervisor_weed", weedSync);

        IntSyncValue rangeSync = SyncHandlers.intNumber(this::getRange, this::setRange);
        syncManager.syncValue("cropqt_supervisor_range", rangeSync);

        BooleanSyncValue autoItemsSync = new BooleanSyncValue(this::isAutoOutputItems, this::setAutoOutputItems);
        BooleanSyncValue autoFluidsSync = new BooleanSyncValue(this::isAutoOutputFluids, this::setAutoOutputFluids);
        syncManager.syncValue("cropqt_supervisor_auto_items", autoItemsSync);
        syncManager.syncValue("cropqt_supervisor_auto_fluids", autoFluidsSync);

        ModularPanel panel = GTGuis.createPanel(this, 176, 196)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(6, 6))
                // 固体肥料输入槽，数量随档位
                .child(new GTFluidSlot()
                        .pos(20, 42).size(18, 36)
                        .syncHandler(GTFluidSlot.sync(importFluids.getTankAt(TANK_WATER))
                                .showAmountOnSlot(false).accessibility(true, true)))
                .child(new GTFluidSlot()
                        .pos(42, 42).size(18, 36)
                        .syncHandler(GTFluidSlot.sync(importFluids.getTankAt(TANK_FERTILIZER))
                                .showAmountOnSlot(false).accessibility(true, true)))
                .child(new ToggleButton()
                        .pos(70, 42).size(18)
                        .value(waterSync)
                        .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT))
                .child(new ToggleButton()
                        .pos(70, 60).size(18)
                        .value(fertSync)
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT))
                .child(new ToggleButton()
                        .pos(70, 78).size(18)
                        .value(weedSync)
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT))
                .child(new ProgressWidget()
                        .value(progress)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, -1)
                        .direction(ProgressWidget.Direction.RIGHT)
                        .size(24, 16).pos(100, 46))
                // 工作半径，照物品收集器
                .child(new ButtonWidget<>()
                        .pos(100, 72).size(16)
                        .onMousePressed(mouse -> {
                            rangeSync.setIntValue(rangeSync.getIntValue() - 1);
                            return true;
                        })
                        .overlay(IKey.str("-1")))
                .child(IKey.dynamic(() -> I18n.format("drtech.machine.crop_supervisor.range",
                                rangeSync.getIntValue()))
                        .asWidget().pos(120, 76))
                .child(new ButtonWidget<>()
                        .pos(156, 72).size(16)
                        .onMousePressed(mouse -> {
                            rangeSync.setIntValue(rangeSync.getIntValue() + 1);
                            return true;
                        })
                        .overlay(IKey.str("+1")))
                .child(new ToggleButton()
                        .pos(100, 94).size(18)
                        .value(new BoolValue.Dynamic(autoItemsSync::getBoolValue, autoItemsSync::setBoolValue))
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .tooltipAutoUpdate(true))
                .child(new ToggleButton()
                        .pos(118, 94).size(18)
                        .value(new BoolValue.Dynamic(autoFluidsSync::getBoolValue, autoFluidsSync::setBoolValue))
                        .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                        .tooltipAutoUpdate(true))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));

        // 输入槽横排在最上面
        for (int i = 0; i < getInputSlots(); i++) {
            panel.child(new ItemSlot()
                    .pos(20 + i * 18, 20)
                    .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                    .slot(SyncHandlers.itemSlot(importItems, i)
                            .singletonSlotGroup().accessibility(true, true)));
        }
        return panel;
    }

    // ==================== 展示 ====================

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.crop_supervisor.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.crop_supervisor.tooltip.2", getInputSlots()));
        tooltip.add(I18n.format("drtech.machine.crop_supervisor.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.crop_supervisor.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.crop_supervisor.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.crop_supervisor.tooltip.6", getMaxRange()));
    }
}
