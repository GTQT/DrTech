package com.meowmel.cropQT.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import com.meowmel.cropQT.api.registries.HydrationRegistry;
import com.meowmel.cropQT.tile.TileCropStick;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.mui.widget.GTFluidSlot;
import gregtechfoodoption.client.GTFOClientHandler;
import gregtechfoodoption.client.GTFOGuiTextures;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 作物管理器：大范围自动照料作物架。
 *
 * <p>以机器为中心扫一个长方体——水平半径 {@code 3 + 2×档位}（LV 为 5、IV 为 13），
 * 垂直半径固定 2（上下各两格）。每个周期照顾一遍范围内的作物架：
 * 收获成熟作物、给缺水的浇水、给缺肥的施肥。
 *
 * <p>三个动作各自独立开关，默认只开收获——浇水和施肥要玩家主动打开，
 * 否则机器会悄悄把玩家的水肥库存吃光。
 *
 * <p><b>本机只能用液体肥料。</b>源端还能吃物品肥料，但本系统的固体肥料
 * 已经改成了带耐久的施肥器工具（见 M3 偏差），所以这里只认流体。
 */
public class MetaTileEntityCropManager extends MetaTileEntityCropMachine {

    /** 扫描周期（tick）。源端是 50。 */
    private static final int WORK_CYCLE = 50;

    /** 垂直半径固定为 2。 */
    private static final int VERTICAL_RADIUS = 2;

    /** 水罐容量 / 每档。 */
    private static final int WATER_CAPACITY_PER_TIER = 32000;
    /** 液肥罐容量 / 每档。 */
    private static final int FERTILIZER_CAPACITY_PER_TIER = 144 * 64 * 4;

    private static final int TANK_WATER = 0;
    private static final int TANK_FERTILIZER = 1;

    /** 每次收获耗电 = 输入电压 / 8。 */
    private static final int HARVEST_ENERGY_DIVISOR = 8;
    /** 每次浇水 / 施肥耗电 = 输入电压 / 32。 */
    private static final int AUX_ENERGY_DIVISOR = 32;

    /** 每档收获加成：LV +5%、IV +25%。 */
    private static final double HARVEST_BONUS_PER_TIER = 0.05;

    /** 缓存里一个作物架都没有时的刷新间隔（tick），有作物时用较长的间隔。 */
    private static final int CACHE_REFRESH_EMPTY = 100;
    private static final int CACHE_REFRESH_ANY = 600;

    /** 作物架的水位低于这个值就补水。 */
    private static final int WATER_THRESHOLD = 180;
    /** 作物架的肥位低于这个值就施肥。 */
    private static final int FERTILIZER_THRESHOLD = 180;

    /** 缓存的作物架。 */
    private final List<TileCropStick> cropCache = new ArrayList<>();
    /** 下次刷新缓存的时间点。 */
    private long nextCacheRefresh = 0L;

    private boolean harvestEnabled = true;
    private boolean waterEnabled = false;
    private boolean fertilizerEnabled = false;

    public MetaTileEntityCropManager(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityCropManager(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.crop_manager.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.crop_manager.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.crop_manager.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.crop_manager.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.crop_manager.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.crop_manager.tooltip.6"));
        tooltip.add(I18n.format("drtech.machine.crop_manager.tooltip.7"));
    }

    // ==================== 容量与半径 ====================

    /** 水平扫描半径：{@code 3 + 2×档位}。LV 为 5，IV 为 13。 */
    public int getHorizontalRadius() {
        return 3 + Math.max(0, 2 * getTier());
    }

    public int getWaterCapacity() {
        return getTier() * WATER_CAPACITY_PER_TIER;
    }

    public int getFertilizerCapacity() {
        return getTier() * FERTILIZER_CAPACITY_PER_TIER;
    }

    @Override
    protected @NotNull FluidTankList createImportFluidHandler() {
        return new FluidTankList(false,
                new FluidTank(getWaterCapacity()),
                new FluidTank(getFertilizerCapacity()));
    }

    // ==================== 状态机 ====================

    @Override
    protected boolean canStart() {
        return harvestEnabled || waterEnabled || fertilizerEnabled;
    }

    @Override
    protected int getWorkDuration() {
        return WORK_CYCLE;
    }

    /** 这台机器不按 tick 扣电，而是在每个动作发生时单独扣——见三个 apply 方法。 */
    @Override
    protected long getEnergyPerTick() {
        return 0L;
    }

    @Override
    protected void onWorkComplete() {
        refreshCacheIfDue();
        for (TileCropStick crop : cropCache) {
            // 缓存要活到下一次刷新，期间方块可能被拆掉或区块卸载——失效的直接跳过
            if (crop.isInvalid() || crop.getWorld() == null) {
                continue;
            }
            if (harvestEnabled) {
                tryHarvest(crop);
            }
            if (waterEnabled) {
                tryWater(crop);
            }
            if (fertilizerEnabled) {
                tryFertilize(crop);
            }
        }
    }

    // ==================== 缓存 ====================

    private void refreshCacheIfDue() {
        long timer = getOffsetTimer();
        if (timer < nextCacheRefresh) {
            return;
        }
        nextCacheRefresh = timer + (cropCache.isEmpty() ? CACHE_REFRESH_EMPTY : CACHE_REFRESH_ANY);

        cropCache.clear();
        BlockPos origin = getPos();
        int radius = getHorizontalRadius();
        for (int dy = -VERTICAL_RADIUS; dy <= VERTICAL_RADIUS; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    TileEntity tile = getWorld().getTileEntity(origin.add(dx, dy, dz));
                    if (tile instanceof TileCropStick) {
                        TileCropStick crop = (TileCropStick) tile;
                        if (!crop.isInvalid()) {
                            cropCache.add(crop);
                        }
                    }
                }
            }
        }
    }

    // ==================== 三个动作 ====================

    private void tryHarvest(TileCropStick crop) {
        if (!crop.hasCrop() || crop.isWeedPlant() || !crop.isMature()) {
            return;
        }
        if (!consumeEnergy(GTValues.V[getTier()] / HARVEST_ENERGY_DIVISOR)) {
            return;
        }
        List<net.minecraft.item.ItemStack> drops = crop.getHarvestDrops();
        crop.harvest();
        for (net.minecraft.item.ItemStack drop : drops) {
            gregtech.api.util.GTTransferUtils.insertItem(exportItems, drop, false);
        }
        markDirty();
    }

    private void tryWater(TileCropStick crop) {
        if (!crop.hasCrop() || crop.getWaterStorage() > WATER_THRESHOLD) {
            return;
        }
        FluidTank tank = waterTank();
        int room = crop.getMaxWater() - crop.getWaterStorage();
        if (tank == null || room <= 0) {
            return;
        }
        FluidStack stored = tank.getFluid();
        int potency = stored == null ? 0 : HydrationRegistry.getWater(stored.getFluid());
        if (potency <= 0 || !consumeEnergy(GTValues.V[getTier()] / AUX_ENERGY_DIVISOR)) {
            return;
        }
        int drained = Math.min(stored.amount, Math.max(1, room / potency));
        tank.drain(drained, true);
        crop.addWater(drained * potency);
    }

    private void tryFertilize(TileCropStick crop) {
        if (!crop.hasCrop() || crop.getFertilizerStorage() > FERTILIZER_THRESHOLD) {
            return;
        }
        FluidTank tank = fertilizerTank();
        int room = crop.getMaxFertilizer() - crop.getFertilizerStorage();
        if (tank == null || room <= 0) {
            return;
        }
        FluidStack stored = tank.getFluid();
        int potency = stored == null ? 0 : FertilizerRegistry.getFertilizer(stored.getFluid());
        if (potency <= 0 || !consumeEnergy(GTValues.V[getTier()] / AUX_ENERGY_DIVISOR)) {
            return;
        }
        int drained = Math.min(stored.amount, Math.max(1, room / potency));
        tank.drain(drained, true);
        crop.addFertilizer(drained * potency);
    }

    /** 扣电；不够就返回 false 且不扣。 */
    private boolean consumeEnergy(long amount) {
        if (amount <= 0) {
            return true;
        }
        if (energyContainer.getEnergyStored() < amount) {
            return false;
        }
        energyContainer.removeEnergy(amount);
        return true;
    }

    private FluidTank waterTank() {
        return importFluids.getTankAt(TANK_WATER) instanceof FluidTank tank ? tank : null;
    }

    private FluidTank fertilizerTank() {
        return importFluids.getTankAt(TANK_FERTILIZER) instanceof FluidTank tank ? tank : null;
    }

    // ==================== 开关 ====================

    public boolean isHarvestEnabled() { return harvestEnabled; }

    public void setHarvestEnabled(boolean value) {
        this.harvestEnabled = value;
        markDirty();
    }

    public boolean isWaterEnabled() { return waterEnabled; }

    public void setWaterEnabled(boolean value) {
        this.waterEnabled = value;
        markDirty();
    }

    public boolean isFertilizerEnabled() { return fertilizerEnabled; }

    public void setFertilizerEnabled(boolean value) {
        this.fertilizerEnabled = value;
        markDirty();
    }

    // ==================== 存档 ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("harvest", harvestEnabled);
        data.setBoolean("water", waterEnabled);
        data.setBoolean("fertilize", fertilizerEnabled);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        harvestEnabled = data.getBoolean("harvest");
        waterEnabled = data.getBoolean("water");
        fertilizerEnabled = data.getBoolean("fertilize");
    }

    // ==================== UI ====================

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        DoubleSyncValue progress = registerProgressSync(syncManager);
        BooleanSyncValue harvestSync = new BooleanSyncValue(this::isHarvestEnabled, this::setHarvestEnabled);
        BooleanSyncValue waterSync = new BooleanSyncValue(this::isWaterEnabled, this::setWaterEnabled);
        BooleanSyncValue fertSync = new BooleanSyncValue(this::isFertilizerEnabled, this::setFertilizerEnabled);
        syncManager.syncValue("cropqt_harvest", harvestSync);
        syncManager.syncValue("cropqt_water", waterSync);
        syncManager.syncValue("cropqt_fertilize", fertSync);

        return GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(6, 6))
                .child(new GTFluidSlot()
                        .pos(20, 22).size(18, 36)
                        .syncHandler(GTFluidSlot.sync(importFluids.getTankAt(TANK_WATER))
                                .showAmountOnSlot(false).accessibility(false, true)))
                .child(new GTFluidSlot()
                        .pos(42, 22).size(18, 36)
                        .syncHandler(GTFluidSlot.sync(importFluids.getTankAt(TANK_FERTILIZER))
                                .showAmountOnSlot(false).accessibility(false, true)))
                .child(new ToggleButton()
                        .pos(70, 22).size(18)
                        .value(harvestSync)
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT))
                .child(new ToggleButton()
                        .pos(70, 40).size(18)
                        .value(waterSync)
                        .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT))
                .child(new ToggleButton()
                        .pos(70, 58).size(18)
                        .value(fertSync)
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT))
                .child(new com.cleanroommc.modularui.widgets.ProgressWidget()
                        .value(progress)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, -1)
                        .direction(com.cleanroommc.modularui.widgets.ProgressWidget.Direction.RIGHT)
                        .size(24, 16).pos(100, 32))
                .child(com.cleanroommc.modularui.widgets.SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    @Override
    public OrientedOverlayRenderer getOverlayRenderer() {
        return GTFOClientHandler.FARMER_OVERLAY;
    }
}
