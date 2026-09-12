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
import com.meowmel.cropQT.api.CropGeneOrb;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.item.ItemCropSeed;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.unification.material.Materials;
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
 * 作物合成器：4 个基因球 + UUM → 一颗完整的种子。
 *
 * <p>和提取器配对成闭环：提取器把种子拆成 4 个球，合成器再拼回去。
 *
 * <p><b>基因球不消耗</b>——它是可重复使用的模板，只有 UUM 被吃掉。
 * 这一点和提取器相反（提取器会把球吃掉），别搞混。
 */
public class MetaTileEntityCropSynthesizer extends MetaTileEntityCropMachine {

    /** 输入槽：4 个基因球（槽位无关，靠 NBT 里的 kind 区分）。 */
    private static final int INPUT_SLOT_COUNT = 4;
    private static final int SLOT_OUTPUT = 0;

    private static final int TANK_CAPACITY = 16000;

    /** 每个属性点消耗的 UUM（mB）。 */
    public static final int UUM_PER_STAT = 100;
    /** 每点作物 tier 消耗的 UUM（mB）。 */
    public static final int UUM_PER_TIER = 750;

    /** 合成器按 3 安培跑。 */
    private static final int AMPERAGE = 3;

    /** 基准耗时（tick），tier 1 时为 5 分钟。 */
    private static final int BASE_DURATION = 6000;
    /** 耗时的对数底，tier 1→6000、tier 16→12000。 */
    private static final double DURATION_LOG_BASE = Math.log(16.0d);

    /** 本轮解析出来的合成方案；{@code null} 表示条件不满足。 */
    @Nullable
    private Resolved resolved;

    public MetaTileEntityCropSynthesizer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityCropSynthesizer(metaTileEntityId, getTier());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.crop_synthesizer.tooltip.6"));
    }

    // ==================== 槽位与罐 ====================

    @Override
    protected @NotNull IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, INPUT_SLOT_COUNT);
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

    /**
     * 解析 4 个槽里的球，凑齐物种 + 三围才返回方案。
     *
     * <p>槽位无关：球可以放任意槽，靠 NBT 里的 kind 分类。同类的第二个会被忽略。
     */
    @Nullable
    private Resolved resolve() {
        String speciesId = null;
        int growth = -1, gain = -1, resistance = -1;

        for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
            ItemStack orb = importItems.getStackInSlot(slot);
            CropGeneOrb.Kind kind = CropGeneOrb.getKind(orb);
            if (kind == null) {
                continue;
            }
            switch (kind) {
                case SPECIES:
                    if (speciesId == null) speciesId = CropGeneOrb.getValue(orb);
                    break;
                case GROWTH:
                    if (growth < 0) growth = statOf(orb);
                    break;
                case GAIN:
                    if (gain < 0) gain = statOf(orb);
                    break;
                case RESISTANCE:
                    if (resistance < 0) resistance = statOf(orb);
                    break;
            }
        }

        if (speciesId == null || growth < 1 || gain < 1 || resistance < 1) {
            return null;
        }
        CropType crop = CropRegistry.get(speciesId);
        if (crop == null) {
            return null;
        }

        // 机器电压必须够这台作物要求的最低档
        int minTier = minimumTierFor(crop);
        if (getTier() < minTier) {
            return null;
        }

        int uum = uumCost(crop, growth, gain, resistance);
        FluidStack stored = importFluids.getTankAt(0).getFluid();
        if (stored == null || stored.getFluid() != Materials.UUMatter.getFluid() || stored.amount < uum) {
            return null;
        }

        ItemStack seed = ItemCropSeed.createSeedBag(speciesId, new CropStats(growth, gain, resistance).analyze());
        if (!GTTransferUtils.insertItem(exportItems, seed, true).isEmpty()) {
            return null;
        }
        return new Resolved(speciesId, growth, gain, resistance, crop, uum, seed);
    }

    private static int statOf(ItemStack orb) {
        Integer value = CropGeneOrb.getStat(orb);
        return value == null ? -1 : value;
    }

    /**
     * 合成某作物需要的最低机器电压档。
     *
     * <p>源端取「作物的育种机档位」，我们没有那个字段，用作物 tier 作代理并夹在 EV~IV 之间
     * ——我们只做 EV / IV 两档，上界就是 IV。
     */
    public static int minimumTierFor(CropType crop) {
        return Math.min(GTValues.IV, Math.max(GTValues.EV, crop.getTier()));
    }

    /** UUM 用量：tier 决定底量，三围决定增量。 */
    public static int uumCost(CropType crop, int growth, int gain, int resistance) {
        return crop.getTier() * UUM_PER_TIER + (growth + gain + resistance) * UUM_PER_STAT;
    }

    @Override
    protected int getWorkDuration() {
        if (resolved == null) {
            return -1;
        }
        // 5 分钟起步，按 log16 增长到 10 分钟
        double factor = 1.0 + Math.log(Math.max(1, resolved.crop.getTier())) / DURATION_LOG_BASE;
        return (int) (BASE_DURATION * factor);
    }

    @Override
    protected long getEnergyPerTick() {
        return resolved == null ? 0L : GTValues.V[minimumTierFor(resolved.crop)] * AMPERAGE;
    }

    @Override
    protected void onWorkComplete() {
        if (resolved == null) {
            return;
        }
        importFluids.getTankAt(0).drain(resolved.uum, true);
        GTTransferUtils.insertItem(exportItems, resolved.seed.copy(), false);
        // 基因球不消耗——它们是可重复使用的模板
        markDirty();
    }

    /** 一轮合成方案。 */
    private static final class Resolved {
        final String speciesId;
        final int growth;
        final int gain;
        final int resistance;
        final CropType crop;
        final int uum;
        final ItemStack seed;

        Resolved(String speciesId, int growth, int gain, int resistance, CropType crop, int uum, ItemStack seed) {
            this.speciesId = speciesId;
            this.growth = growth;
            this.gain = gain;
            this.resistance = resistance;
            this.crop = crop;
            this.uum = uum;
            this.seed = seed;
        }
    }

    // ==================== UI ====================

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        DoubleSyncValue progress = registerProgressSync(syncManager);
        ModularPanel panel = GTGuis.createPanel(this, 176, 166)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(6, 6))
                .child(new GTFluidSlot()
                        .pos(16, 22)
                        .size(18)
                        .syncHandler(GTFluidSlot.sync(importFluids.getTankAt(0))
                                .showAmountOnSlot(false).accessibility(true, true)))
                .child(new ProgressWidget()
                        .value(progress)
                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, -1)
                        .direction(ProgressWidget.Direction.RIGHT)
                        .size(24, 16).pos(40, 24));

        // 4 个输入球横排 + 右侧产物
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            panel.child(new ItemSlot()
                    .pos(76 + i * 18, 24)
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
        return Textures.CROP_SYNTHESIZER_OVERLAY;
    }
}
