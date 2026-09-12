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
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.mutation.CropMutation;
import com.meowmel.cropQT.api.mutation.MutationRegistry;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 作物育种机：把 2~4 颗已分析的亲本种子杂交成一颗新种子。
 *
 * <p>产物由 {@link MutationRegistry} 的确定性配方决定——与世界里的作物架走同一张配方表，
 * 所以玩家在田里试出来的组合，在机器上照样成立。
 *
 * <p><b>失败也消耗材料</b>：掷骰没中照样扣亲本与液肥。这是源端行为，
 * 也是「育种是有成本的事」这个设计的一部分。
 */
public class MetaTileEntityCropBreeder extends MetaTileEntityCropMachine {

    /** 每点作物 tier 消耗的液肥（mB，按浓缩液肥计）。 */
    public static final int FERTILIZER_PER_TIER = 144;
    /** 每点属性消耗的液肥。 */
    public static final int FERTILIZER_PER_STAT = FERTILIZER_PER_TIER / 2;

    /** 浓缩液体肥料的浓度基线（点数/mB）。普通液体肥料按浓度折算成更多 mB。 */
    private static final int BASELINE_POTENCY = 8;

    /** 低电压档给 3 个输入槽，HV 及以上给 6 个。 */
    private static final int MIN_INPUT_SLOTS = 3;
    private static final int MAX_INPUT_SLOTS = 6;

    private static final int SLOT_OUTPUT = 0;
    private static final int TANK_CAPACITY = 16000;

    /** 成功率 = min(100, 40 + (档位 - LV) * 10)。 */
    private static final int MIN_CHANCE = 40;
    private static final int MAX_CHANCE = 100;
    private static final int CHANCE_PER_TIER = 10;

    /** 基准耗时（tick），每个档位 ×1.3。 */
    private static final int BASE_DURATION = 400;

    /** 本轮解析出来的育种方案；{@code null} 表示条件不满足。 */
    @Nullable
    private Resolved resolved;

    public MetaTileEntityCropBreeder(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityCropBreeder(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.crop_breeder.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.crop_breeder.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.crop_breeder.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.crop_breeder.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.crop_breeder.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.crop_breeder.tooltip.6"));
        tooltip.add(I18n.format("drtech.machine.crop_breeder.tooltip.7"));
    }

    /** 本档位有几个输入槽。 */
    public int getInputSlots() {
        return getTier() < GTValues.HV ? MIN_INPUT_SLOTS : MAX_INPUT_SLOTS;
    }

    /** 本档位的产出成功率（%）。 */
    public int getOutputChance() {
        return Math.min(MAX_CHANCE, MIN_CHANCE + Math.max(0, getTier() - GTValues.LV) * CHANCE_PER_TIER);
    }

    // ==================== 槽位与罐 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, MAX_INPUT_SLOTS) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return slot < getInputSlots()
                        && !ItemCropSeed.getCropId(stack).isEmpty()
                        && ItemCropSeed.getCropStats(stack).isAnalyzed();
            }
        };
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
        resolved = resolve();
        return resolved != null;
    }

    @Nullable
    private Resolved resolve() {
        List<String> parentIds = new ArrayList<>();
        List<CropStats> parentStats = new ArrayList<>();
        for (int slot = 0; slot < getInputSlots(); slot++) {
            ItemStack seed = importItems.getStackInSlot(slot);
            if (seed.isEmpty()) {
                continue;
            }
            CropStats stats = ItemCropSeed.getCropStats(seed);
            String cropId = ItemCropSeed.getCropId(seed);
            if (!stats.isAnalyzed() || cropId.isEmpty()) {
                continue;
            }
            parentIds.add(cropId);
            parentStats.add(stats);
        }
        // 同种亲本允许重复——配方表里本来就有 stickreed × stickreed 这种
        if (parentIds.size() < CropMutation.MIN_PARENTS) {
            return null;
        }

        CropMutation mutation = MutationRegistry.pickDeterministic(parentIds, getWorld().rand);
        if (mutation == null) {
            return null;
        }
        CropType output = CropRegistry.get(mutation.getResult());
        if (output == null) {
            return null;
        }

        CropStats childStats = averageStats(parentStats);
        int fluid = fluidCost(output, childStats);
        FluidStack stored = importFluids.getTankAt(0).getFluid();
        int potency = stored == null ? 0 : FertilizerRegistry.getFertilizer(stored.getFluid());
        if (potency <= 0 || stored.amount < fluid) {
            return null;
        }

        ItemStack child = ItemCropSeed.createSeedBag(mutation.getResult(), childStats);
        if (!GTTransferUtils.insertItem(exportItems, child, true).isEmpty()) {
            return null;
        }
        return new Resolved(mutation, output, childStats, fluid, child);
    }

    /** 子代属性 = 各亲本属性的算术平均，向下取整。 */
    private static CropStats averageStats(List<CropStats> parents) {
        int growth = 0, gain = 0, resistance = 0;
        for (CropStats stats : parents) {
            growth += stats.getGrowth();
            gain += stats.getGain();
            resistance += stats.getResistance();
        }
        int n = parents.size();
        return new CropStats(growth / n, gain / n, resistance / n).analyze();
    }

    /**
     * 一次育种的液肥用量（mB）。
     *
     * <p>按浓度折算：浓缩液体肥料是基线，普通液体肥料要按比例多消耗。
     */
    private int fluidCost(CropType output, CropStats stats) {
        int base = Math.max(1, output.getTier() * FERTILIZER_PER_TIER)
                + Math.max(1, (stats.getGrowth() + stats.getGain() + stats.getResistance()) * FERTILIZER_PER_STAT);
        FluidStack stored = importFluids.getTankAt(0).getFluid();
        int potency = stored == null ? BASELINE_POTENCY : FertilizerRegistry.getFertilizer(stored.getFluid());
        if (potency <= 0) {
            return base;
        }
        return Math.max(1, base * BASELINE_POTENCY / potency);
    }

    @Override
    protected int getWorkDuration() {
        if (resolved == null) {
            return -1;
        }
        // 产物越高级，一轮越久
        return (int) (BASE_DURATION * Math.pow(1.3, Math.max(0, resolved.output.getTier() - 1)));
    }

    @Override
    protected long getEnergyPerTick() {
        return resolved == null ? 0L : GTValues.V[getTier()];
    }

    @Override
    protected void onWorkComplete() {
        if (resolved == null) {
            return;
        }
        // 无论成功与否都要扣料
        importFluids.getTankAt(0).drain(resolved.fluid, true);
        consumeOneParentEach();

        // 掷骰决定这一次有没有出种子
        if (getWorld().rand.nextInt(MAX_CHANCE) < getOutputChance()) {
            GTTransferUtils.insertItem(exportItems, resolved.child.copy(), false);
        }
        markDirty();
    }

    /** 每个用到的亲本槽各扣一颗。 */
    private void consumeOneParentEach() {
        for (int slot = 0; slot < getInputSlots(); slot++) {
            if (!importItems.getStackInSlot(slot).isEmpty()) {
                importItems.extractItem(slot, 1, false);
            }
        }
    }

    /** 一轮育种方案。 */
    private static final class Resolved {
        final CropMutation mutation;
        final CropType output;
        final CropStats stats;
        final int fluid;
        final ItemStack child;

        Resolved(CropMutation mutation, CropType output, CropStats stats, int fluid, ItemStack child) {
            this.mutation = mutation;
            this.output = output;
            this.stats = stats;
            this.fluid = fluid;
            this.child = child;
        }
    }

    // ==================== UI ====================

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        DoubleSyncValue progress = registerProgressSync(syncManager);
        int slots = getInputSlots();

        ModularPanel panel = GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(6, 6))
                .child(new GTFluidSlot()
                        .pos(16, 22).size(18)
                        .syncHandler(GTFluidSlot.sync(importFluids.getTankAt(0))
                                .showAmountOnSlot(false).accessibility(true, true)))
                .child(new ProgressWidget()
                        .value(progress)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, -1)
                        .direction(ProgressWidget.Direction.RIGHT)
                        .size(24, 16).pos(40, 24));

        // 亲本槽：3 个时一行，6 个时两行
        for (int i = 0; i < slots; i++) {
            panel.child(new ItemSlot()
                    .pos(76 + i % 3 * 18, 14 + i / 3 * 18)
                    .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                    .slot(SyncHandlers.itemSlot(importItems, i)
                            .singletonSlotGroup().accessibility(true, true)));
        }
        return panel
                .child(new ItemSlot()
                        .pos(148, 24)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(exportItems, SLOT_OUTPUT)
                                .singletonSlotGroup().accessibility(false, true)))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    @Override
    public OrientedOverlayRenderer getOverlayRenderer() {
        return Textures.CROP_BREEDER_OVERLAY;
    }
}
