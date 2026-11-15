package com.meowmel.cropQT.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.drppp.drtech.client.Textures;
import com.meowmel.cropQT.api.CropConstants;
import com.meowmel.cropQT.client.CropUITextures;
import com.meowmel.cropQT.api.CropTooltips;
import com.meowmel.cropQT.api.potency.IPotencyData;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import com.meowmel.cropQT.item.tool.ItemWeedEX;
import com.meowmel.cropQT.tile.TileCropStick;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.mui.widget.GTFluidSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 作物管理机（Crop Manager）—— CropsNH {@code MTECropManager} 的复刻。
 *
 * <p>源端没有「监管机 / 收割机」的拆分：一台机器管理半径内所有作物架，浇水、施肥、
 * 除草、收获全都在这台机器里按 50 tick 一轮完成。cropQT 旧版把功能拆成
 * {@code MetaTileEntityCropSupervisor} 与 {@code MetaTileEntityCropHarvester}，与源端
 * 结构不一致，因此这里按源端重新实现；旧机器只在 {@code legacyCropMachines} 打开时注册。
 *
 * <h2>槽位布局（22 格，与源端一致）</h2>
 * <ul>
 *     <li>0..1：WeedEX 罐（两格）</li>
 *     <li>2..5：固体肥料（四格）</li>
 *     <li>6..20：输出（15 格）</li>
 *     <li>21：电池（充电/放电）</li>
 * </ul>
 *
 * <h2>三种流体罐</h2>
 * 水（Hydration 登记表）、WeedEX（WeedEX 登记表）、液肥（Fertilizer 登记表）。
 * 容量分别为 {@code tier*32000}、{@code tier*1500}、{@code tier*36864}。
 *
 * <h2>两档功率</h2>
 * 收获按 {@code maxEUInput/8}，浇水/施肥/除草按 {@code maxEUInput/32}。
 * 源端每株每项功能各扣一次二次功率，这里是同样的账。
 */
public class MetaTileEntityCropManager extends TieredMetaTileEntity {

    // ==================== 槽位 ====================
    public static final int SLOT_WEEDEX_START = 0;
    public static final int SLOT_FERT_START = 2;
    public static final int SLOT_OUTPUT_START = 6;
    public static final int SLOT_OUTPUT_END = 20;
    public static final int SLOT_BATTERY = 21;
    public static final int SLOT_COUNT = 22;

    // ==================== 罐 ====================
    public static final int TANK_WATER = 0;
    public static final int TANK_WEEDEX = 1;
    public static final int TANK_FERTILIZER = 2;

    private CropManagerItemHandler inventory;
    private NotifiableFluidTank waterTank;
    private NotifiableFluidTank weedExTank;
    private NotifiableFluidTank fertilizerTank;
    /** WeedEX 罐耐久换来的 WeedEX 点数（源端 weedEXStored）。 */
    private int weedExBuffer;

    // ==================== 范围缓存 ====================
    private final Set<TileCropStick> cropCache = new HashSet<>();
    private boolean cacheInvalid;
    private int ticksSinceRefresh;
    private int updateCycle;

    // ==================== 开关 ====================
    private boolean harvestEnabled = true;
    private boolean weedExEnabled = false;
    private boolean waterEnabled = false;
    private boolean fertilizerEnabled = false;

    // ==================== 溢出 ====================
    private final List<ItemStack> overflow = new ArrayList<>();

    public MetaTileEntityCropManager(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        rebuildTanks();
    }

    // ==================== 初始化 ====================

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        // MetaTileEntity 构造期就会调用；此时 tier 尚未赋值，但本处理器不依赖 tier。
        this.inventory = new CropManagerItemHandler(this, SLOT_COUNT);
        return this.inventory;
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new OutputViewHandler(this);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long voltage = GTValues.V[Math.max(0, getTier())];
        // 源端：maxEUStore = V[tier] * (tier * V[tier])
        long capacity = voltage * (Math.max(1, getTier()) * voltage);
        this.energyContainer = EnergyContainerHandler.receiverContainer(this, capacity, voltage, 1L);
    }

    private void rebuildTanks() {
        int tier = Math.max(1, getTier());
        this.waterTank = new NotifiableFluidTank(tier * 32000, this, false);
        this.weedExTank = new NotifiableFluidTank(tier * 1500, this, false);
        this.fertilizerTank = new NotifiableFluidTank(tier * 36864, this, false);
        this.importFluids = new FluidTankList(false, waterTank, weedExTank, fertilizerTank);
        this.fluidInventory = new FluidHandlerProxy(importFluids, exportFluids);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCropManager(metaTileEntityId, getTier());
    }

    // ==================== 主循环 ====================

    @Override
    public void update() {
        super.update();
        World world = getWorld();
        if (world == null || world.isRemote) {
            return;
        }

        handleBattery();

        if (getOffsetTimer() % CropConstants.MANAGER_UPDATE_RATE != 0) {
            return;
        }

        if (energyContainer.getEnergyStored() < getMaxEUInput()) {
            return;
        }

        updateCycle++;
        ticksSinceRefresh++;
        int refreshRate = cropCache.isEmpty() ? CropConstants.MANAGER_CACHE_REFRESH_EMPTY
                : CropConstants.MANAGER_CACHE_REFRESH_ANY;
        if (cacheInvalid || ticksSinceRefresh >= refreshRate) {
            updateCropCache(world);
        }

        refillWeedEx();
        processSecondaryFunctions();
        harvest(world);
    }

    private void updateCropCache(World world) {
        cropCache.clear();
        cacheInvalid = false;
        ticksSinceRefresh = 0;
        int radius = CropConstants.MANAGER_RADIUS_BASE + CropConstants.MANAGER_RADIUS_PER_TIER * Math.max(0, getTier());
        int vertical = CropConstants.MANAGER_VERTICAL_RADIUS;
        BlockPos origin = getPos();
        for (int y = -vertical; y <= vertical; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (world.getTileEntity(origin.add(x, y, z)) instanceof TileCropStick crop) {
                        cropCache.add(crop);
                    }
                }
            }
        }
    }

    // ==================== 电池 ====================

    private void handleBattery() {
        ItemStack battery = inventory.getStackInSlot(SLOT_BATTERY);
        if (battery.isEmpty()) {
            return;
        }
        IElectricItem electricItem = battery.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null) {
            return;
        }
        long stored = energyContainer.getEnergyStored();
        long capacity = energyContainer.getEnergyCapacity();
        if (stored * 2 > capacity / 3) {
            long canCharge = capacity - stored;
            if (canCharge > 0) {
                long charged = electricItem.discharge(canCharge, getTier(), false, true, false);
                if (charged > 0) {
                    energyContainer.addEnergy(charged);
                }
            }
        } else if (stored < capacity / 3) {
            long wanted = capacity / 3 - stored;
            long discharged = electricItem.charge(wanted, getTier(), false, true);
            if (discharged > 0) {
                energyContainer.removeEnergy(discharged);
            }
        }
    }

    // ==================== 二次功能（水/WeedEX/肥） ====================

    /**
     * 源端 {@code refillWeedEX()}：把 WeedEX 罐的耐久换成罐里的 WeedEX。
     *
     * <p>每 1 点耐久 = {@link ItemWeedEX#POTENCY_PER_USE} 点 WeedEX，罐子用掉最后一次后消失。
     * 本仓库没有 WeedEX 流体，因此罐子补进来的是机器内部的 WeedEX 缓冲
     * （{@link #weedExBuffer}），容量与流体罐一致（tier × 1500）；流体侧仍然照常可用，
     * 供整合包提供的 {@code weedex} 流体走 {@code WeedEXRegistry}。
     */
    private void refillWeedEx() {
        int capacity = getTier() * CropConstants.MANAGER_WEEDEX_CAPACITY_PER_TIER;
        if (weedExBuffer >= capacity) {
            return;
        }
        for (int slot = SLOT_WEEDEX_START; slot < SLOT_FERT_START; slot++) {
            ItemStack can = inventory.getStackInSlot(slot);
            if (can.isEmpty() || !(can.getItem() instanceof ItemWeedEX)) {
                continue;
            }
            while (weedExBuffer + ItemWeedEX.POTENCY_PER_USE <= capacity) {
                int damage = can.getItemDamage();
                if (damage + 1 > can.getMaxDamage()) {
                    can.setCount(0);
                    break;
                }
                can.setItemDamage(damage + 1);
                weedExBuffer += ItemWeedEX.POTENCY_PER_USE;
            }
        }
    }

    private void processSecondaryFunctions() {
        long secondary = getPowerUsageSecondary();
        for (TileCropStick crop : new ArrayList<>(cropCache)) {
            if (crop.isInvalid() || !crop.hasCrop()) {
                cacheInvalid = true;
                continue;
            }

            if (energyContainer.getEnergyStored() < secondary) {
                break;
            }

            if (waterEnabled && applyHydration(crop, true)) {
                energyContainer.removeEnergy(secondary);
                applyHydration(crop, false);
            }
            if (energyContainer.getEnergyStored() < secondary) {
                break;
            }
            if (weedExEnabled) {
                if (applyWeedEx(crop, true)) {
                    energyContainer.removeEnergy(secondary);
                    applyWeedEx(crop, false);
                }
            }
            if (energyContainer.getEnergyStored() < secondary) {
                break;
            }
            if (fertilizerEnabled && applyFertilizer(crop, true)) {
                energyContainer.removeEnergy(secondary);
                applyFertilizer(crop, false);
            }
        }
    }

    private boolean applyHydration(TileCropStick crop, boolean simulate) {
        FluidStack fluid = waterTank.getFluid();
        if (fluid == null || fluid.amount <= 0 || crop.getWaterStorage() > CropConstants.WATER_STORAGE_THRESHOLD) {
            return false;
        }
        int drain = Math.min(fluid.amount,
                CropConstants.WATER_STORAGE_MAX - crop.getWaterStorage());
        if (drain <= 0) {
            return false;
        }
        if (!simulate) {
            waterTank.drain(drain, true);
        }
        return crop.addWater(drain, CropConstants.WATER_STORAGE_THRESHOLD,
                CropConstants.WATER_STORAGE_MAX, simulate);
    }

    private boolean applyWeedEx(TileCropStick crop, boolean simulate) {
        if (crop.getWeedExStorage() > CropConstants.WEEDEX_STORAGE_THRESHOLD) {
            return false;
        }
        // 罐子缓冲优先，其次才是流体罐（源端只有一种存储，这里两者等价）
        if (weedExBuffer < CropConstants.WEEDEX_COST
                && weedExTank.getFluidAmount() < CropConstants.WEEDEX_COST) {
            return false;
        }
        if (!simulate) {
            if (weedExBuffer >= CropConstants.WEEDEX_COST) {
                weedExBuffer -= CropConstants.WEEDEX_COST;
            } else {
                weedExTank.drain(CropConstants.WEEDEX_COST, true);
            }
        }
        return crop.addWeedEx(CropConstants.WEEDEX_COST, CropConstants.WEEDEX_STORAGE_THRESHOLD,
                CropConstants.WEEDEX_STORAGE_MAX, simulate);
    }

    private boolean applyFertilizer(TileCropStick crop, boolean simulate) {
        int stored = crop.getFertilizerStorage();
        int amount = 0;
        int applyCap = CropConstants.FERTILIZER_STORAGE_MAX;
        int threshold = CropConstants.FERTILIZER_LIQUID_THRESHOLD;

        FluidStack liquid = fertilizerTank.getFluid();
        if (liquid != null && liquid.amount > 0) {
            if (stored > threshold) {
                return false;
            }
            amount = Math.min(liquid.amount, applyCap - stored);
            if (amount <= 0) {
                return false;
            }
            if (!simulate) {
                fertilizerTank.drain(amount, true);
            }
        } else {
            for (int i = SLOT_FERT_START; i < SLOT_OUTPUT_START; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack.isEmpty()) {
                    continue;
                }
                IPotencyData data = FertilizerRegistry.getFertilizerData(stack);
                if (data == null || stack.getCount() < data.getUnitsConsumedPerApplication()) {
                    continue;
                }
                int cap = data.getMaxStorage(CropConstants.FERTILIZER_STORAGE_MAX);
                int solidThreshold = Math.max(cap / 2, cap - data.getPotency() / 2);
                if (stored > solidThreshold) {
                    continue;
                }
                if (!simulate) {
                    inventory.extractItem(i, data.getUnitsConsumedPerApplication(), false);
                }
                amount = data.getPotency();
                applyCap = cap;
                threshold = solidThreshold;
                break;
            }
        }

        if (amount <= 0) {
            return false;
        }
        return crop.addFertilizer(amount, threshold, applyCap, simulate);
    }

    // ==================== 收获 ====================

    private void harvest(World world) {
        if (!harvestEnabled || !hasOutputSpace()) {
            return;
        }
        retryOverflow();
        if (!overflow.isEmpty()) {
            return;
        }

        long power = getPowerUsage();
        double bonus = 1.0d + CropConstants.MANAGER_HARVEST_BONUS_PER_TIER * getTier();

        for (TileCropStick crop : new ArrayList<>(cropCache)) {
            if (crop.isInvalid() || !crop.hasCrop()) {
                cacheInvalid = true;
                continue;
            }
            if (energyContainer.getEnergyStored() < power) {
                break;
            }
            if (!crop.canHarvest()) {
                continue;
            }
            List<ItemStack> drops = crop.getHarvestDrops(bonus);
            crop.harvest();
            energyContainer.removeEnergy(power);

            List<ItemStack> merged = mergeStacks(drops);
            for (ItemStack drop : merged) {
                ItemStack remaining = tryInsertOutputStack(drop);
                if (!remaining.isEmpty()) {
                    overflow.add(remaining);
                }
            }
        }
    }

    private void retryOverflow() {
        if (overflow.isEmpty()) {
            return;
        }
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : overflow) {
            ItemStack left = tryInsertOutputStack(stack);
            if (!left.isEmpty()) {
                remaining.add(left);
            }
        }
        overflow.clear();
        overflow.addAll(remaining);
    }

    private boolean hasOutputSpace() {
        for (int i = SLOT_OUTPUT_START; i <= SLOT_OUTPUT_END; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private ItemStack tryInsertOutputStack(ItemStack drop) {
        ItemStack remaining = drop.copy();
        for (int i = SLOT_OUTPUT_START; i <= SLOT_OUTPUT_END && !remaining.isEmpty(); i++) {
            ItemStack slot = inventory.getStackInSlot(i);
            int max = Math.min(remaining.getMaxStackSize(), 64);
            if (slot.isEmpty()) {
                int move = Math.min(max, remaining.getCount());
                ItemStack placed = remaining.copy();
                placed.setCount(move);
                inventory.setStackInSlot(i, placed);
                remaining.shrink(move);
            } else if (ItemStack.areItemsEqual(slot, remaining)
                    && ItemStack.areItemStackTagsEqual(slot, remaining)
                    && slot.getCount() < max) {
                int move = Math.min(max - slot.getCount(), remaining.getCount());
                slot.grow(move);
                inventory.setStackInSlot(i, slot);
                remaining.shrink(move);
            }
        }
        return remaining;
    }

    private static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            boolean found = false;
            for (ItemStack existing : merged) {
                if (ItemStack.areItemsEqual(existing, stack) && ItemStack.areItemStackTagsEqual(existing, stack)) {
                    existing.grow(stack.getCount());
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(stack.copy());
            }
        }
        return merged;
    }

    // ==================== 功率 ====================

    public long getMaxEUInput() {
        return GTValues.V[Math.max(0, getTier())];
    }

    public long getPowerUsage() {
        return Math.max(1, getMaxEUInput() / 8);
    }

    public long getPowerUsageSecondary() {
        return Math.max(1, getMaxEUInput() / 32);
    }

    public long getStoredEU() {
        return energyContainer.getEnergyStored();
    }

    // ==================== 槽/罐访问 ====================

    public IItemHandlerModifiable getInventory() {
        return inventory;
    }

    public NotifiableFluidTank getWaterTank() {
        return waterTank;
    }

    public NotifiableFluidTank getWeedExTank() {
        return weedExTank;
    }

    public NotifiableFluidTank getFertilizerTank() {
        return fertilizerTank;
    }

    public boolean isHarvestEnabled() {
        return harvestEnabled;
    }

    /** 供 TOP / GUI 读数：是否允许自动浇水。 */
    public boolean isWaterEnabled() {
        return waterEnabled;
    }

    /** 供 TOP / GUI 读数：是否允许自动除杂草。 */
    public boolean isWeedExEnabled() {
        return weedExEnabled;
    }

    /** 供 TOP / GUI 读数：是否允许自动施肥。 */
    public boolean isFertilizerEnabled() {
        return fertilizerEnabled;
    }

    /** WeedEX 罐耐久换来的点数缓冲（源端 weedEXStored）。 */
    public int getWeedExBuffer() {
        return weedExBuffer;
    }

    /** WeedEX 缓冲上限。 */
    public int getWeedExBufferCapacity() {
        return Math.max(1, getTier() * CropConstants.MANAGER_WEEDEX_CAPACITY_PER_TIER);
    }

    /** 水平工作半径（源端 3 + 2×tier）。 */
    public int getWorkingRadius() {
        return CropConstants.MANAGER_RADIUS_BASE
                + CropConstants.MANAGER_RADIUS_PER_TIER * Math.max(0, getTier());
    }

    /** 垂直工作半径（源端固定 2）。 */
    public int getVerticalRadius() {
        return CropConstants.MANAGER_VERTICAL_RADIUS;
    }

    /** 收割加成概率（源端 0.05×tier）。 */
    public float getHarvestBonusChance() {
        return CropConstants.MANAGER_HARVEST_BONUS_PER_TIER * Math.max(0, getTier());
    }

    /** 当前缓存里的作物架数量（诊断/TOP 用）。 */
    public int getCachedCropCount() {
        return cropCache.size();
    }

    public void setHarvestEnabled(boolean value) {
        this.harvestEnabled = value;
        markDirty();
    }

    public void setWaterEnabled(boolean value) {
        this.waterEnabled = value;
        markDirty();
    }

    public void setWeedExEnabled(boolean value) {
        this.weedExEnabled = value;
        markDirty();
    }

    public void setFertilizerEnabled(boolean value) {
        this.fertilizerEnabled = value;
        markDirty();
    }

    // ==================== NBT ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("mWater", waterTank.getFluidAmount());
        data.setInteger("mWeedEx", weedExTank.getFluidAmount());
        data.setInteger("mWeedExBuffer", weedExBuffer);
        data.setInteger("mLiquidFertilizer", fertilizerTank.getFluidAmount());
        data.setBoolean("mHarvestEnabled", harvestEnabled);
        data.setBoolean("mWeedExEnabled", weedExEnabled);
        data.setBoolean("mWaterEnabled", waterEnabled);
        data.setBoolean("mFertilizerEnabled", fertilizerEnabled);

        NBTTagList overflowList = new NBTTagList();
        for (ItemStack stack : overflow) {
            if (!stack.isEmpty()) {
                overflowList.appendTag(stack.writeToNBT(new NBTTagCompound()));
            }
        }
        data.setTag("mDropOverflow", overflowList);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        harvestEnabled = !data.hasKey("mHarvestEnabled") || data.getBoolean("mHarvestEnabled");
        weedExEnabled = data.getBoolean("mWeedExEnabled");
        waterEnabled = data.getBoolean("mWaterEnabled");
        fertilizerEnabled = data.getBoolean("mFertilizerEnabled");
        weedExBuffer = Math.max(0, data.getInteger("mWeedExBuffer"));

        overflow.clear();
        NBTTagList overflowList = data.getTagList("mDropOverflow", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < overflowList.tagCount(); i++) {
            ItemStack stack = new ItemStack(overflowList.getCompoundTagAt(i));
            if (!stack.isEmpty()) {
                overflow.add(stack);
            }
        }
    }

    // ==================== 界面 ====================

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        BooleanSyncValue harvestSync = new BooleanSyncValue(this::isHarvestEnabled, this::setHarvestEnabled);
        BooleanSyncValue waterSync = new BooleanSyncValue(this::isWaterEnabled, this::setWaterEnabled);
        BooleanSyncValue weedSync = new BooleanSyncValue(this::isWeedExEnabled, this::setWeedExEnabled);
        BooleanSyncValue fertSync = new BooleanSyncValue(this::isFertilizerEnabled, this::setFertilizerEnabled);
        syncManager.syncValue("cropqt_manager_harvest", harvestSync);
        syncManager.syncValue("cropqt_manager_water", waterSync);
        syncManager.syncValue("cropqt_manager_weedex", weedSync);
        syncManager.syncValue("cropqt_manager_fert", fertSync);

        // 布局与 CropsNH 的 MTECropManagerGui 同构：
        // 左列 6 个输入槽（2 列 × 3 行）→ 中间三条竖直液柱 → 右侧 5×3 输出；
        // 开关排在液柱下方，电池放在左列底部。旧版 176x196 会把输出第 5 列顶到面板外，
        // 并让自动贴底的玩家背包压住肥料行与电池，所以这里重排并加高面板。
        ModularPanel panel = GTGuis.createPanel(this, 176, 206)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(6, 6));

        // 0..1 WeedEX 罐、2..5 固体肥料（2 × 2）
        for (int i = SLOT_WEEDEX_START; i < SLOT_FERT_START; i++) {
            panel.child(new ItemSlot()
                    .pos(8 + (i - SLOT_WEEDEX_START) * 18, 20)
                    .background(GTGuiTextures.SLOT)
                    .backgroundOverlay(CropUITextures.OVERLAY_SLOT_WEED_EX)
                    .slot(SyncHandlers.itemSlot(inventory, i).singletonSlotGroup().accessibility(true, true)));
        }
        for (int i = SLOT_FERT_START; i < SLOT_OUTPUT_START; i++) {
            int index = i - SLOT_FERT_START;
            panel.child(new ItemSlot()
                    .pos(8 + (index % 2) * 18, 40 + (index / 2) * 18)
                    .background(GTGuiTextures.SLOT)
                    .backgroundOverlay(CropUITextures.OVERLAY_SLOT_FERTILIZER)
                    .slot(SyncHandlers.itemSlot(inventory, i).singletonSlotGroup().accessibility(true, true)));
        }

        // 三条竖直液柱（水 / WeedEX / 液肥）：宽 10、高 54，与源端等高
        panel.child(new GTFluidSlot()
                .pos(44, 20).size(10, 54)
                .syncHandler(GTFluidSlot.sync(waterTank).showAmountOnSlot(false).accessibility(true, true)));
        panel.child(new GTFluidSlot()
                .pos(56, 20).size(10, 54)
                .syncHandler(GTFluidSlot.sync(weedExTank).showAmountOnSlot(false).accessibility(true, true)));
        panel.child(new GTFluidSlot()
                .pos(68, 20).size(10, 54)
                .syncHandler(GTFluidSlot.sync(fertilizerTank).showAmountOnSlot(false).accessibility(true, true)));

        // 输出 5×3，右对齐到面板内（x = 82..172）；输出槽只能抽、不能放
        for (int i = SLOT_OUTPUT_START; i <= SLOT_OUTPUT_END; i++) {
            int index = i - SLOT_OUTPUT_START;
            panel.child(new ItemSlot()
                    .pos(82 + (index % 5) * 18, 20 + (index / 5) * 18)
                    .background(GTGuiTextures.SLOT)
                    .slot(SyncHandlers.itemSlot(inventory, i).singletonSlotGroup().accessibility(false, true)));
        }

        // 四个开关排在液柱下方，使用 CropsNH 的按钮贴图，并给出 enabled/disabled 提示
        panel.child(new ToggleButton()
                .pos(44, 80).size(18)
                .value(waterSync)
                .overlay(CropUITextures.BUTTON_TOGGLE_WATER)
                .addTooltip(true, IKey.lang("drtech_tooltip.cropManager.toggle.water"))
                .addTooltip(true, IKey.lang("drtech_tooltip.cropManager.toggle.enabled"))
                .addTooltip(false, IKey.lang("drtech_tooltip.cropManager.toggle.water"))
                .addTooltip(false, IKey.lang("drtech_tooltip.cropManager.toggle.disabled")));
        panel.child(new ToggleButton()
                .pos(62, 80).size(18)
                .value(weedSync)
                .overlay(CropUITextures.BUTTON_TOGGLE_WEED_EX)
                .addTooltip(true, IKey.lang("drtech_tooltip.cropManager.toggle.weedEX"))
                .addTooltip(true, IKey.lang("drtech_tooltip.cropManager.toggle.enabled"))
                .addTooltip(false, IKey.lang("drtech_tooltip.cropManager.toggle.weedEX"))
                .addTooltip(false, IKey.lang("drtech_tooltip.cropManager.toggle.disabled")));
        panel.child(new ToggleButton()
                .pos(80, 80).size(18)
                .value(fertSync)
                .overlay(CropUITextures.BUTTON_TOGGLE_FERTILIZER)
                .addTooltip(true, IKey.lang("drtech_tooltip.cropManager.toggle.fertilizer"))
                .addTooltip(true, IKey.lang("drtech_tooltip.cropManager.toggle.enabled"))
                .addTooltip(false, IKey.lang("drtech_tooltip.cropManager.toggle.fertilizer"))
                .addTooltip(false, IKey.lang("drtech_tooltip.cropManager.toggle.disabled")));
        panel.child(new ToggleButton()
                .pos(98, 80).size(18)
                .value(harvestSync)
                .overlay(CropUITextures.BUTTON_TOGGLE_HARVEST)
                .addTooltip(true, IKey.lang("drtech_tooltip.cropManager.toggle.harvest"))
                .addTooltip(true, IKey.lang("drtech_tooltip.cropManager.toggle.enabled"))
                .addTooltip(false, IKey.lang("drtech_tooltip.cropManager.toggle.harvest"))
                .addTooltip(false, IKey.lang("drtech_tooltip.cropManager.toggle.disabled")));

        // 电池：左列底部，配充电器底纹（与源端一致）
        panel.child(new ItemSlot()
                .pos(8, 78)
                .background(GTGuiTextures.SLOT)
                .backgroundOverlay(GTGuiTextures.IN_SLOT_OVERLAY)
                .slot(SyncHandlers.itemSlot(inventory, SLOT_BATTERY).singletonSlotGroup().accessibility(true, true)));

        panel.child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
        return panel;
    }

    // ==================== 提示 ====================

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech_tooltip.cropManager.description"));
        tooltip.add(CropTooltips.machineType("cropManager"));
        tooltip.add(I18n.format("drtech_tooltip.cropManager.tooltip.0"));
        tooltip.add(I18n.format("drtech_tooltip.cropManager.tooltip.1", CropTooltips.eu(getPowerUsage())));
        tooltip.add(I18n.format("drtech_tooltip.cropManager.tooltip.2", CropTooltips.eu(getPowerUsageSecondary())));
        tooltip.add(I18n.format("drtech_tooltip.cropManager.tooltip.3", CropTooltips.number(2)));
        int diameter = 2 * (3 + 2 * getTier()) + 1;
        tooltip.add(I18n.format("drtech_tooltip.cropManager.tooltip.4",
                CropTooltips.number(diameter), CropTooltips.number(5)));
        tooltip.add(I18n.format("drtech_tooltip.cropManager.tooltip.5",
                CropTooltips.percent(0.05d * getTier())));
        tooltip.add(I18n.format("drtech_tooltip.cropManager.tooltip.6",
                CropTooltips.fluid(getTier() * 32000L)));
        tooltip.add(I18n.format("drtech_tooltip.cropManager.tooltip.7",
                CropTooltips.fluid(getTier() * 144L * 64L * 4L)));
        tooltip.add(I18n.format("drtech_tooltip.cropManager.tooltip.8",
                CropTooltips.fluid(getTier() * 750L * 2L)));
    }

    @Override
    public String getMetaName() {
        return CropTooltips.name("cropManager", getTier());
    }

    /**
     * 源端 {@code MTECropManager.getTextureSet/getFront/...}：六个面都是「该档机壳 + 收割机图案」，
     * 顶/底用箱子图案（{@code Casing_CropHarvester_Boxes}），其余四面用刀刃图案
     * （{@code Casing_CropHarvester_Cutter}）。GTCEu 的机壳由 {@link TieredMetaTileEntity}
     * 自己画，这里补图案层即可，与源端「机壳 + overlay」的叠加顺序一致。
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        for (EnumFacing side : EnumFacing.VALUES) {
            SimpleOverlayRenderer overlay = (side == EnumFacing.UP || side == EnumFacing.DOWN)
                    ? Textures.CROP_HARVESTER_BOXES
                    : Textures.CROP_HARVESTER_CUTTER;
            overlay.renderOrientedState(renderState, translation, pipeline, Cuboid6.full, side, false, true);
        }
    }

    // ==================== 内部处理器 ====================

    private static class CropManagerItemHandler extends NotifiableItemStackHandler {

        CropManagerItemHandler(MetaTileEntity entity, int slots) {
            super(entity, slots, entity, false);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (slot >= SLOT_WEEDEX_START && slot < SLOT_FERT_START) {
                return stack.getItem() instanceof ItemWeedEX;
            }
            if (slot >= SLOT_FERT_START && slot < SLOT_OUTPUT_START) {
                return FertilizerRegistry.getFertilizerData(stack) != null;
            }
            if (slot == SLOT_BATTERY) {
                return stack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_BATTERY ? 1 : 64;
        }
    }

    /** 输出槽的只读视图：自动化只能从 6..20 抽取，不能塞入。 */
    private static class OutputViewHandler implements IItemHandlerModifiable {

        private final MetaTileEntityCropManager manager;

        OutputViewHandler(MetaTileEntityCropManager manager) {
            this.manager = manager;
        }

        @Override
        public int getSlots() {
            return SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return manager.inventory.getStackInSlot(SLOT_OUTPUT_START + slot);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            manager.inventory.setStackInSlot(SLOT_OUTPUT_START + slot, stack);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return manager.inventory.extractItem(SLOT_OUTPUT_START + slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return manager.inventory.getSlotLimit(SLOT_OUTPUT_START + slot);
        }
    }
}
