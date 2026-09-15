package com.meowmel.cropQT.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.api.metaTileEntity.DrtechMultiblockAbility;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.DropTracker;
import com.meowmel.cropQT.api.FarmNutrientModel;
import com.meowmel.cropQT.api.SubSoilRequirement;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import com.meowmel.cropQT.api.capability.FarmType;
import com.meowmel.cropQT.api.capability.IFarmPart;
import com.meowmel.cropQT.api.registries.HydrationRegistry;
import com.meowmel.cropQT.block.BlockSeedBed;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.item.ItemEnvironmentalModule;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.pattern.FormedStructureView;
import gregtech.api.pattern.StructurePieceKey;
import gregtech.api.pattern.casing.DeclarativePatternBuilder;
import gregtech.api.pattern.casing.GTStructureChannels;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.pattern.element.Elements;
import gregtech.api.pattern.element.IStructureElement;
import gregtech.api.pattern.element.StructureDefinition;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gregtech.api.util.RelativeDirection.*;

/**
 * 工业农场：把作物架上的那一套搬进机器里，用 EU 换产出。
 *
 * <h2>等级由升级仓与苗床的档次决定</h2>
 * 只有一个控制器。体段顶部每段必须装一个升级仓（{@code IFarmPart}），
 * 结构里的所有升级仓与所有苗床<b>必须同一档</b>，那个档就是农场的升级等级
 * （{@link #upgradeTier}）：容量、基础耗电、每轮水肥用量、收割轮数加成、段数上限全跟着它走。
 *
 * <p>电压只决定<b>能喂多少功率</b>，而且还要受档次限制（{@code 能量仓档位 <= 升级等级}）。
 * 两者之间的落差正是超频生长加速仓存在的理由 —— 用低档苗床配高压电，超频换生长速度。
 *
 * <h2>三种模式</h2>
 * <ul>
 *     <li>{@link #MODE_INPUT}（5 tick）—— 从输入总线吞种子与底土</li>
 *     <li>{@link #MODE_FARM}（100 tick）—— 耗水耗肥推进生长，产出累积</li>
 *     <li>{@link #MODE_OUTPUT}（5 tick）—— 把种子与底土退回输出总线</li>
 * </ul>
 * 界面上的按钮切模式。
 *
 * <h2>产出是小数累积的</h2>
 * 每周期只推进一点点生长，掉落自然也是小数。{@link DropTracker} 把它们攒起来，
 * 攒够 1 个才吐出去——所以低等级农场的产出不是"没有"，而是"攒得慢"。
 */
public class MetaTileEntityIndustrialFarm extends MultiblockWithDisplayBase {

    // ==================== 结构 ====================

    private static final String PIECE_HEAD = "head";
    private static final String PIECE_BODY = "body";
    /** 取段数用；{@code repeatablePiece} 只沿一条轴重复，所以下标是 0。 */
    private static final StructurePieceKey BODY_PIECE = StructurePieceKey.of(PIECE_BODY);
    private static final String PIECE_TAIL = "tail";

    /** 最少 / 最多堆叠段数。 */
    private static final int MIN_SLICES = 3;
    private static final int MAX_SLICES = 13;

    // ==================== 等级 ====================

    /** 农场最低档次（对应源端的 MIN_CASING_TIER）。 */
    private static final int MIN_FARM_TIER = GTValues.MV;
    /** 超频次数上限——只作护栏，正常范围里用不满。 */
    private static final int MAX_OVERCLOCKS = 8;

    // ==================== 周期 ====================

    /** 生产模式一个周期。 */
    public static final int CYCLE_DURATION = 100;
    /** 输入 / 输出模式的周期——近乎瞬时。 */
    private static final int MODE_SWITCH_DURATION = 5;

    // ==================== 模式 ====================

    public static final int MODE_INPUT = 0;
    public static final int MODE_FARM = 1;
    public static final int MODE_OUTPUT = 2;
    private static final int MODE_COUNT = 3;

    // ==================== 槽位 ====================

    /** 种子槽。 */
    public static final int SLOT_SEED = 0;
    /** 底土槽。 */
    public static final int SLOT_SUB_SOIL = 1;
    /** 环境模块起始槽。 */
    public static final int SLOT_ENV_START = 2;
    /** 环境模块槽数量，与环境强化升级的上限一致。 */
    private static final int ENV_SLOT_COUNT = 2;
    /** 内部槽总数。 */
    private static final int INVENTORY_SIZE = SLOT_ENV_START + ENV_SLOT_COUNT;

    // ==================== 模拟 ====================

    /** 模拟储水量——机器里的作物永远"不缺水"，水由液罐按周期消耗。 */
    private static final int SIMULATED_WATER_STORAGE = 200;
    /** 机器里永远看得到天空。 */
    private static final boolean SIMULATED_CAN_SEE_SKY = true;
    /** 本周期有肥料供给时的模拟储肥量。 */
    private static final int SIMULATED_FERT_PROVIDED = 200;
    /** 没有肥料时的模拟储肥量。 */
    private static final int SIMULATED_FERT_NOT_PROVIDED = 0;

    // ==================== 升级等级 ====================

    /** 结构里还没碰到过任何分档组件。 */
    private static final int TIER_UNSET = -1;
    /** 结构里出现了不止一种档次。 */
    private static final int TIER_CONFLICT = -2;

    // ==================== 状态 ====================

    /** 内部库存：种子 / 底土 / 环境模块。 */
    private final ItemStackHandler farmInventory = createFarmInventory();

    /**
     * 升级等级：由升级仓与苗床共同约定，结构里所有部件必须同一档。
     *
     * <p>这是整套机制的核心变量。容量、基础耗电、水肥消耗、收割轮数加成全跟着它走，
     * 它同时还是能量仓电压的上限。电压与它之间的落差，就是超频生长加速仓的立足点。
     */
    private int upgradeTier = TIER_UNSET;
    /** 实际搭出来的段数。 */
    private int slices = MIN_SLICES;
    /** 当前模式。 */
    private int mode = MODE_INPUT;
    /** 当前周期已跑 tick 数。 */
    private int progress = 0;
    /** 当前周期总时长；0 表示没有进行中的周期。 */
    private int maxProgress = 0;
    /** 本周期是否有肥料供给（用于营养计算）。 */
    private boolean hasFertilizer = false;

    /** 五种升级各装了几个，成型时从升级仓数出来。 */
    private final EnumMap<FarmType, Integer> unitCounts = new EnumMap<>(FarmType.class);

    /** 本周期实际执行了几次超频。没装超频仓时是 0。 */
    private int expectedOCs = 0;
    /** 本周期每 tick 实际耗电（含超频）。 */
    private long expectedEUt = 0;

    /** 小数累积产出。 */
    private final DropTracker dropTracker = new DropTracker();

    // 成型后填充的聚合 handler
    private ItemHandlerList importItems = new ItemHandlerList();
    private ItemHandlerList exportItems = new ItemHandlerList();
    private FluidTankList importFluids = new FluidTankList(false);
    private EnergyContainerList energyContainer = new EnergyContainerList(Collections.emptyList());

    public MetaTileEntityIndustrialFarm(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity holder) {
        return new MetaTileEntityIndustrialFarm(metaTileEntityId);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world,
                               @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("drtech.machine.industrial_farm.tooltip.1"));
        tooltip.add(I18n.format("drtech.machine.industrial_farm.tooltip.2"));
        tooltip.add(I18n.format("drtech.machine.industrial_farm.tooltip.3"));
        tooltip.add(I18n.format("drtech.machine.industrial_farm.tooltip.4"));
        tooltip.add(I18n.format("drtech.machine.industrial_farm.tooltip.5"));
        tooltip.add(I18n.format("drtech.machine.industrial_farm.tooltip.6"));
        tooltip.add(I18n.format("drtech.machine.industrial_farm.tooltip.7"));
        tooltip.add(I18n.format("drtech.machine.industrial_farm.tooltip.8"));
        tooltip.add(I18n.format("drtech.machine.industrial_farm.tooltip.9"));
    }

    // ==================== 结构定义 ====================

    private static final StructureDefinition<?> STRUCTURE_DEFINITION = StructureDefinition.getOrBuild(
            "drtech:industrial_farm", MetaTileEntityIndustrialFarm::buildStructure);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildStructure() {
        return DeclarativePatternBuilder.start(RIGHT, DOWN, BACK)
                .piece(PIECE_HEAD)
                .aisle(" CCC ", "CCCCC", "CCCCC", "C   C")
                .repeatablePiece(PIECE_BODY, MIN_SLICES, MAX_SLICES)
                .aisle(" GUG ", "G   G", "CSSSC", "     ")
                .withAisleChannel(GTStructureChannels.STRUCTURE_LENGTH.getName())
                .piece(PIECE_TAIL)
                .aisle(" CCC ", "CCCCC", "CC~CC", "C   C")
                .self('~', MetaTileEntityIndustrialFarm.class)
                .blocks('G', getGlassesState())
                .where('S', seedBedElement())
                .hatch('U', DrtechMultiblockAbility.FARM_PART)
                .casing('C', getCasingState())
                    .maintenance()
                    .energyInput(1, 2)
                    .itemInput(1, 4)
                    .itemOutput(1, 4)
                    .fluidInput(1, 2)
                .buildStructureDefinition();
    }

    /**
     * 苗床：11 档都收，但整座农场的苗床必须同一档。
     *
     * <p>用 {@code chain} 逐档试，命中哪一档就在回调里记下那一档 ——
     * {@code Elements.onPass} 的回调没有返回值，没法当场把结构判失败，
     * 所以冲突只能成型后再查（{@link #isUpgradeTierValid()}），
     * 表现成「停机 + 界面红字」，跟段数校验同一套。
     */
    private static IStructureElement seedBedElement() {
        BlockSeedBed.SeedBedType[] types = BlockSeedBed.SeedBedType.values();
        IStructureElement[] byTier = new IStructureElement[types.length];
        for (int i = 0; i < types.length; i++) {
            int tier = types[i].getTier();
            byTier[i] = Elements.onPass(
                    context -> {
                        // 预览 / 建造提示时没有控制器，那种情况下只判方块、不记档次
                        if (context.getController() instanceof MetaTileEntityIndustrialFarm farm) {
                            farm.agreeUpgradeTier(tier);
                        }
                    },
                    Elements.block(getSeedBedState(types[i])));
        }
        return Elements.chain(byTier);
    }

    // ==================== 方块 / 贴图 ====================

    /**
     * 农场的外壳：砖砌农业外壳（照搬源端）。
     *
     * <p>这个方法名是<b>反射约定</b>——{@code MultiblockControllerBase.getCasingBlock()} 会按名字找它，
     * 拿到之后给所有贴在农场上的仓做底图。所以改了这里，外壳和升级仓的底色会一起变。
     */
    public static IBlockState getCasingState() {
        return BlocksInit.COMMON_CASING.getState(
                com.drppp.drtech.common.blocks.metaBlocks.MetaCasing.MetalCasingType.BRICKED_AGRICULTURAL_CASING);
    }

    protected static IBlockState getGlassesState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    protected static IBlockState getSeedBedState(BlockSeedBed.SeedBedType type) {
        return BlocksInit.SEED_BED.getState(type);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @NotNull ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return com.drppp.drtech.client.Textures.BRICKED_AGRICULTURAL_CASING;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.MULTIBLOCK_WORKABLE_OVERLAY;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(codechicken.lib.render.CCRenderState renderState,
                                     codechicken.lib.vec.Matrix4 translation,
                                     codechicken.lib.render.pipeline.IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline,
                getFrontFacing(), isActive(), isActive());
    }

    // ==================== 成型 ====================

    @Override
    protected void formStructure(@NotNull FormedStructureView formed) {
        super.formStructure(formed);

        // 段数要问 piece repeat，不能问 STRUCTURE_LENGTH 通道：
        // 那个通道在这套 DSL 里是**建造输入**（给结构建造器指定要搭多长），
        // 匹配完了没人把结果写回去，读出来恒为 0——段数上限就永远不会生效。
        // 蒸馏塔 / 大型蒸馏器读的就是 getPieceRepeat，这里是同一套。
        this.slices = Math.max(MIN_SLICES, formed.getPieceRepeat(BODY_PIECE, 0));

        // 升级等级：苗床档次在匹配过程中已经记进 upgradeTier 了，
        // 这里再让所有升级仓来跟它对齐——两边都必须同一档
        this.unitCounts.clear();
        for (IFarmPart part : getAbilities(DrtechMultiblockAbility.FARM_PART)) {
            unitCounts.merge(part.getFarmType(), 1, Integer::sum);
            agreeUpgradeTier(part.getPartTier());
        }

        this.importItems = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.exportItems = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.importFluids = new FluidTankList(false, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        refreshExpectedOverclock();
        markDirty();
    }

    /**
     * 让一个分档组件（苗床或升级仓）跟当前约定的档次对齐。
     *
     * <p>第一个碰到的定调，之后每一个都必须一样；出现第二种就记成冲突，
     * 由 {@link #isUpgradeTierValid()} 判停机。
     */
    private void agreeUpgradeTier(int tier) {
        if (upgradeTier == TIER_UNSET) {
            upgradeTier = tier;
        } else if (upgradeTier != tier) {
            upgradeTier = TIER_CONFLICT;
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.importItems = new ItemHandlerList();
        this.exportItems = new ItemHandlerList();
        this.importFluids = new FluidTankList(false);
        this.energyContainer = new EnergyContainerList(Collections.emptyList());
        this.upgradeTier = TIER_UNSET;
        this.unitCounts.clear();
        this.expectedOCs = 0;
        this.expectedEUt = 0;
        this.progress = 0;
        this.maxProgress = 0;
    }

    /**
     * 升级等级是否可用：所有分档组件同档、且不低于农场最低档。
     *
     * <p>跟段数校验一样是「成型但停机」，不是结构错误——摆错一档就该给提示，
     * 而不是让玩家对着不成型的机器猜哪里错了。
     */
    public boolean isUpgradeTierValid() {
        return upgradeTier >= MIN_FARM_TIER && upgradeTier <= BlockSeedBed.MAX_TIER;
    }

    /** 某个类型装了几个。 */
    public int getUnitCount(FarmType type) {
        return unitCounts.getOrDefault(type, 0);
    }

    public int getSlices() {
        return slices;
    }

    /**
     * 本档次允许的最大段数。
     *
     * <p>照源端：段数不是「你想搭多长就搭多长」，而是由组件档次反推 ——
     * 档次越高，农场能装的升级仓越多、也就被要求造得越长。
     */
    public int getMaxSlicesForTier() {
        return isUpgradeTierValid() ? Math.min(MAX_SLICES, BlockSeedBed.getMultiLength(upgradeTier)) : MIN_SLICES;
    }

    /** 结构校验：段数不能超过档次允许的长度。 */
    public boolean isSliceCountValid() {
        return slices <= getMaxSlicesForTier();
    }

    // 停机原因。界面靠它出红字，值本身要跨端同步，所以用 int。
    public static final int ERROR_NONE = 0;
    /** 段数超过档次允许的长度。 */
    public static final int ERROR_SLICE_OVERFLOW = 1;
    /** 升级仓与苗床档次不一致（或压根没装分档组件）。 */
    public static final int ERROR_TIER_INVALID = 2;
    /** 某种升级超出了数量上限。 */
    public static final int ERROR_UNIT_CAP = 3;
    /** 生长加速与超频同时装了。 */
    public static final int ERROR_EXCLUSIVE = 4;
    /** 能量仓电压超过了升级档次。 */
    public static final int ERROR_VOLTAGE_OVER_TIER = 5;

    /**
     * 整机停在哪一步；{@link #ERROR_NONE} 表示配置没问题。
     *
     * <p>下面每一条在源端都是<b>结构错误</b>（不成型）。这里一律做成「成型但停机 + 界面红字」，
     * 跟段数校验同一套：摆错一步就该给提示，而不是让玩家对着不成型的机器猜哪里错了。
     */
    public int getConfigError() {
        if (!isUpgradeTierValid()) {
            return ERROR_TIER_INVALID;
        }
        if (!isSliceCountValid()) {
            return ERROR_SLICE_OVERFLOW;
        }
        for (FarmType type : FarmType.values()) {
            if (type.isCapped() && getUnitCount(type) > type.getMaxCount()) {
                return ERROR_UNIT_CAP;
            }
        }
        // 源端 SE_OCGAU_EXCLUSIVITY
        if (getUnitCount(FarmType.GROWTH_ACCELERATION) > 0
                && getUnitCount(FarmType.OVERCLOCKED_GROWTH_ACCELERATION) > 0) {
            return ERROR_EXCLUSIVE;
        }
        // 能量仓电压不得超过升级档次——想接高压电就得整套升上去
        long voltage = this.energyContainer.getHighestInputVoltage();
        if (voltage > 0 && GTUtility.getFloorTierByVoltage(voltage) > upgradeTier) {
            return ERROR_VOLTAGE_OVER_TIER;
        }
        return ERROR_NONE;
    }

    public boolean isConfigurationValid() {
        return getConfigError() == ERROR_NONE;
    }

    /** 停机原因的 lang 键；没问题时返回 null。 */
    @Nullable
    public static String configErrorKey(int error) {
        switch (error) {
            case ERROR_SLICE_OVERFLOW:      return "cropqt.farm.error.slice";
            case ERROR_TIER_INVALID:        return "cropqt.farm.error.tier";
            case ERROR_UNIT_CAP:            return "cropqt.farm.error.unit_cap";
            case ERROR_EXCLUSIVE:           return "cropqt.farm.error.exclusive";
            case ERROR_VOLTAGE_OVER_TIER:   return "cropqt.farm.error.voltage";
            default:                        return null;
        }
    }

    public int getUpgradeTier() {
        return upgradeTier;
    }

    /** 实际执行了几次超频；没装超频仓或没落差时是 0。 */
    public int getOverclockCount() {
        return expectedOCs;
    }

    // ==================== 内部库存 ====================

    private ItemStackHandler createFarmInventory() {
        return new GTItemStackHandler(this, INVENTORY_SIZE) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == SLOT_SEED) {
                    return isAnalyzedSeed(stack) && !needsSubSoil(stack);
                }
                // 底土禁止手动插入——只能由输入模式从输入总线吞
                if (slot == SLOT_SUB_SOIL) {
                    return false;
                }
                // 槽位数 = 环境强化升级数，但不会超过槽位上限（仓可以多装，槽位有限）
                int envIndex = slot - SLOT_ENV_START;
                if (envIndex < 0
                        || envIndex >= Math.min(getUnitCount(FarmType.ENVIRONMENTAL_ENHANCEMENT), ENV_SLOT_COUNT)) {
                    return false;
                }
                return stack.getItem() instanceof ItemEnvironmentalModule
                        && ItemEnvironmentalModule.getBiomeTag(stack.getMetadata()) != null;
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot < SLOT_ENV_START ? 64 : 1;
            }
        };
    }

    private static boolean isAnalyzedSeed(ItemStack stack) {
        return !stack.isEmpty()
                && !ItemCropSeed.getCropId(stack).isEmpty()
                && ItemCropSeed.getCropStats(stack).isAnalyzed()
                && CropRegistry.exists(ItemCropSeed.getCropId(stack));
    }

    /** 这袋种子需要的底土，没有对应要求时返回 null。 */
    @Nullable
    private static SubSoilRequirement getRequiredSubSoil(ItemStack seed) {
        CropType crop = CropRegistry.get(ItemCropSeed.getCropId(seed));
        return crop == null ? null : crop.getSubSoilRequirement();
    }

    private static boolean needsSubSoil(ItemStack seed) {
        return getRequiredSubSoil(seed) != null;
    }

    private ItemStack getSeed() {
        return farmInventory.getStackInSlot(SLOT_SEED);
    }

    private ItemStack getSubSoil() {
        return farmInventory.getStackInSlot(SLOT_SUB_SOIL);
    }

    private void setSeed(ItemStack stack) {
        farmInventory.setStackInSlot(SLOT_SEED, stack == null ? ItemStack.EMPTY : stack);
    }

    private void setSubSoil(ItemStack stack) {
        farmInventory.setStackInSlot(SLOT_SUB_SOIL, stack == null ? ItemStack.EMPTY : stack);
    }

    // ==================== 工作循环 ====================

    @Override
    protected void updateFormedValid() {
        if (!isConfigurationValid()) {
            return;
        }
        if (maxProgress <= 0) {
            maxProgress = getDurationForMode();
            progress = 0;
        }
        if (++progress < maxProgress) {
            return;
        }
        this.progress = 0;
        this.maxProgress = 0;
        runCurrentMode();
        markDirty();
    }

    private int getDurationForMode() {
        return mode == MODE_FARM ? CYCLE_DURATION : MODE_SWITCH_DURATION;
    }

    private void runCurrentMode() {
        switch (mode) {
            case MODE_INPUT:  runInputMode();  break;
            case MODE_FARM:   runFarmMode();   break;
            case MODE_OUTPUT: runOutputMode(); break;
            default: break;
        }
    }

    // ==================== 输入模式 ====================

    /** 从输入总线吞种子与底土进内部槽。 */
    private void runInputMode() {
        if (!getSeed().isEmpty()) {
            return;
        }
        // 找第一个已分析的种子
        for (int slot = 0; slot < importItems.getSlots(); slot++) {
            ItemStack candidate = importItems.getStackInSlot(slot);
            if (!isAnalyzedSeed(candidate)) {
                continue;
            }
            SubSoilRequirement requirement = getRequiredSubSoil(candidate);
            ItemStack subSoilFound = ItemStack.EMPTY;
            if (requirement != null) {
                subSoilFound = findSubSoil(requirement);
                if (subSoilFound.isEmpty()) {
                    // 缺底土：这次先不吞
                    return;
                }
            }
            importItems.extractItem(slot, 1, false);
            ItemStack seed = candidate.copy();
            seed.setCount(1);
            setSeed(seed);
            if (!subSoilFound.isEmpty()) {
                setSubSoil(subSoilFound);
            }
            return;
        }
    }

    /** 在输入总线里找一块满足要求的底土。 */
    private ItemStack findSubSoil(SubSoilRequirement requirement) {
        for (int slot = 0; slot < importItems.getSlots(); slot++) {
            ItemStack candidate = importItems.getStackInSlot(slot);
            if (candidate.isEmpty() || isAnalyzedSeed(candidate)) {
                continue;
            }
            if (requirement.matches(blockStateOf(candidate))) {
                ItemStack taken = importItems.extractItem(slot, 1, false);
                if (!taken.isEmpty()) {
                    return taken;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 物品 → 方块状态。
     *
     * <p>底土在世界里是方块，但在机器里以方块物品的形式存在，
     * 所以要还原成方块状态才能交给 {@link SubSoilRequirement} 判定。
     */
    private static IBlockState blockStateOf(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        return block.getStateFromMeta(stack.getMetadata());
    }

    // ==================== 输出模式 ====================

    /** 把内部种子与底土退回输出总线。 */
    private void runOutputMode() {
        ItemStack seed = getSeed();
        if (!seed.isEmpty()) {
            ItemStack leftover = GTTransferUtils.insertItem(exportItems, seed.copy(), false);
            if (leftover.isEmpty()) {
                setSeed(ItemStack.EMPTY);
            } else {
                setSeed(leftover);
                return;
            }
        }
        ItemStack subSoil = getSubSoil();
        if (!subSoil.isEmpty()) {
            ItemStack leftover = GTTransferUtils.insertItem(exportItems, subSoil.copy(), false);
            if (leftover.isEmpty()) {
                setSubSoil(ItemStack.EMPTY);
            } else {
                setSubSoil(leftover);
            }
        }
    }

    // ==================== 生产模式 ====================

    private void runFarmMode() {
        ItemStack seed = getSeed();
        if (seed.isEmpty()) {
            return;
        }
        CropType crop = CropRegistry.get(ItemCropSeed.getCropId(seed));
        if (crop == null) {
            return;
        }
        CropStats stats = ItemCropSeed.getCropStats(seed);

        // 底土对不上就停摆（种子里带底土要求，但槽里的不是那一块）
        SubSoilRequirement requirement = crop.getSubSoilRequirement();
        if (requirement != null) {
            ItemStack subSoil = getSubSoil();
            if (subSoil.isEmpty() || !requirement.matches(blockStateOf(subSoil))) {
                return;
            }
        }

        // 耗水耗肥：只按等级与升级算，不随种子数量缩放（源端如此）
        if (!consumeWater() || !consumeFertilizer()) {
            return;
        }
        // 扣电（含超频）
        long eu = getActualPowerUsage();
        if (this.energyContainer.getEnergyStored() < eu) {
            return;
        }
        this.energyContainer.removeEnergy(eu);

        // 推进生长
        double progressPerCycle = getGrowthProgressPerCycle(crop, stats);
        if (progressPerCycle <= 0.0d) {
            return;
        }
        // 累积掉落
        double rounds = FarmNutrientModel.getAvgDropRounds(stats.getGain()) * getHarvestRoundMultiplier();
        for (ItemStack drop : crop.getDrops()) {
            dropTracker.add(drop, drop.getCount() * rounds * progressPerCycle);
        }
        for (CropType.ChanceDrop chanceDrop : crop.getChanceDrops()) {
            dropTracker.add(chanceDrop.item,
                    chanceDrop.item.getCount() * chanceDrop.chance * rounds * progressPerCycle);
        }
        pushCompletedDrops();
    }

    /** 把攒够整数的产出推进输出总线。 */
    private void pushCompletedDrops() {
        for (ItemStack stack : dropTracker.drainComplete()) {
            ItemStack leftover = GTTransferUtils.insertItem(exportItems, stack, false);
            if (!leftover.isEmpty()) {
                // 放不下就塞回去，下个周期再试
                dropTracker.add(leftover, leftover.getCount());
            }
        }
    }

    // ==================== 生长计算 ====================

    /** 本周期推进的生长百分比；长不了返回 0。 */
    private double getGrowthProgressPerCycle(CropType crop, CropStats stats) {
        int nutrients = getNutrientScore(crop);
        int growthSpeed = FarmNutrientModel.getGrowthRate(nutrients, crop.getTier(), stats.getGrowth());
        return Math.max(0.0d, FarmNutrientModel.getProgressPerCycle(
                crop.getGrowthDuration(), growthSpeed, CYCLE_DURATION, getGrowthSpeedMultiplier()));
    }

    /** 营养点：生物群系 + 环境模块补的喜好群。 */
    private int getNutrientScore(CropType crop) {
        Set<BiomeDictionary.Type> biomeTags = new HashSet<>(
                BiomeDictionary.getTypes(getWorld().getBiome(getPos())));
        for (int i = 0; i < Math.min(getUnitCount(FarmType.ENVIRONMENTAL_ENHANCEMENT), ENV_SLOT_COUNT); i++) {
            ItemStack module = farmInventory.getStackInSlot(SLOT_ENV_START + i);
            if (module.isEmpty() || !(module.getItem() instanceof ItemEnvironmentalModule)) {
                continue;
            }
            BiomeDictionary.Type tag = ItemEnvironmentalModule.getBiomeTag(module.getMetadata());
            if (tag != null) {
                biomeTags.add(tag);
            }
        }
        int likedBiomes = 0;
        for (BiomeDictionary.Type liked : crop.getLikedBiomes()) {
            if (biomeTags.contains(liked)) {
                likedBiomes++;
            }
        }
        float rainfall = getWorld().getBiome(getPos()).getRainfall();
        int fertilizerStorage = hasFertilizer ? SIMULATED_FERT_PROVIDED : SIMULATED_FERT_NOT_PROVIDED;
        return FarmNutrientModel.getNutrientsPerCycle(
                likedBiomes, rainfall, SIMULATED_CAN_SEE_SKY, SIMULATED_WATER_STORAGE, fertilizerStorage);
    }

    /**
     * 生长速度倍率：生长加速是加法、肥料是乘法、超频是 2^n。
     *
     * <p>超频那项读的是本周期实际算出来的 {@link #expectedOCs}，不是「有没有装超频仓」——
     * 装了但没落差（比如苗床档次就是当前电压档）时超频次数是 0，那就该当没装。
     */
    public double getGrowthSpeedMultiplier() {
        double multiplier = 1.0d;
        multiplier += getUnitCount(FarmType.GROWTH_ACCELERATION) * FarmType.GROWTH_ACCELERATION_BONUS;
        multiplier *= 1.0d + Math.min(getUnitCount(FarmType.FERTILIZER),
                FarmType.FERTILIZER.getMaxCount()) * FarmType.FERTILIZER_GROWTH_MULTIPLIER;
        if (expectedOCs > 0) {
            multiplier *= Math.pow(2.0d, expectedOCs);
        }
        return multiplier;
    }

    /** 收割轮数倍率：苗床档次 + 肥料加成是加法，高级收割是乘法。 */
    public double getHarvestRoundMultiplier() {
        double multiplier = 1.0d;
        multiplier += isUpgradeTierValid() ? BlockSeedBed.getHarvestRoundBonus(upgradeTier) : 0.0d;
        multiplier += Math.min(getUnitCount(FarmType.FERTILIZER),
                FarmType.FERTILIZER.getMaxCount()) * FarmType.FERTILIZER_HARVEST_ROUND_BONUS;
        multiplier *= 1.0d + Math.min(getUnitCount(FarmType.ADVANCED_HARVESTING),
                FarmType.ADVANCED_HARVESTING.getMaxCount()) * FarmType.ADVANCED_HARVESTING_ROUND_MULTIPLIER;
        return multiplier;
    }

    /**
     * 算本周期能超频几次。
     *
     * <h2>落差从哪来</h2>
     * 基准是<b>苗床档次决定的耗电</b>，上限是<b>能量仓的电压</b>——两个互相独立的变量。
     * 用 MV 苗床配 EV 电就有 2 次超频可用；苗床和电压同档时超频次数是 0，装了也没用。
     * 这正是源端的机制：那边基准来自组件 tier、上限来自仓室 tier。
     *
     * <p>没装超频仓时直接清零，不去算。
     */
    private void refreshExpectedOverclock() {
        long powerUsage = getPowerUsage();
        if (getUnitCount(FarmType.OVERCLOCKED_GROWTH_ACCELERATION) <= 0 || !isUpgradeTierValid()) {
            this.expectedOCs = 0;
            this.expectedEUt = powerUsage;
            return;
        }
        // 从「苗床档次定的耗电」往上涨，看能 4 倍几次才撞到「能量仓给的电压」
        long ceiling = this.energyContainer.getHighestInputVoltage();
        long eut = powerUsage;
        int overclocks = 0;
        while (overclocks < MAX_OVERCLOCKS
                && eut * (long) OverclockingLogic.STD_VOLTAGE_FACTOR <= ceiling) {
            eut *= (long) OverclockingLogic.STD_VOLTAGE_FACTOR;
            overclocks++;
        }
        this.expectedOCs = overclocks;
        this.expectedEUt = eut;
    }

    /** 超频倍率（水肥消耗也要跟着放大）。 */
    private double getOverclockPotencyMultiplier() {
        return expectedOCs > 0 ? Math.pow(2.0d, expectedOCs) : 1.0d;
    }

    // ==================== 耗电 / 耗水 / 耗肥 ====================

    /** 基础耗电（苗床档次决定）+ 各升级的附加耗电。 */
    public long getPowerUsage() {
        long base = isUpgradeTierValid() ? BlockSeedBed.getBaseEUt(upgradeTier) : GTValues.VA[MIN_FARM_TIER];
        long power = base;
        for (FarmType type : FarmType.values()) {
            int count = getUnitCount(type);
            if (count <= 0) {
                continue;
            }
            if (type.isCapped()) {
                count = Math.min(count, type.getMaxCount());
            }
            power += (long) (base * type.getPowerIncrease() * count);
        }
        return Math.max(1L, power);
    }

    /** 本周期实际要扣的电（含超频）。 */
    public long getActualPowerUsage() {
        return expectedOCs > 0 ? expectedEUt : getPowerUsage();
    }

    /** 每周期要补的水量（"potency"，不是 mB）。 */
    public int getWaterPotencyNeededPerCycle() {
        int capacity = getSeedBedCapacity();
        return (int) Math.ceil(capacity * ((double) CYCLE_DURATION / FarmNutrientModel.TICK_RATE)
                * getOverclockPotencyMultiplier());
    }

    /** 每周期要补的肥量。 */
    public int getFertilizerPotencyNeededPerCycle() {
        return getWaterPotencyNeededPerCycle();
    }

    /** 种子床容量，由苗床档次决定。 */
    public int getSeedBedCapacity() {
        return isUpgradeTierValid() ? BlockSeedBed.getCapacity(upgradeTier) : 0;
    }

    /** 按 potency 消耗流体；不足时返回 false 且不消耗。 */
    private boolean consumeFluidPotency(int neededPotency, boolean water) {
        int remaining = neededPotency;
        for (int i = 0; i < importFluids.getTanks(); i++) {
            IFluidTank tank = importFluids.getTankAt(i);
            FluidStack stored = tank.getFluid();
            if (stored == null || stored.amount <= 0) {
                continue;
            }
            int potency = water
                    ? HydrationRegistry.getWater(stored.getFluid())
                    : FertilizerRegistry.getFertilizer(stored.getFluid());
            if (potency <= 0) {
                continue;
            }
            int amount = Math.min(stored.amount, (remaining + potency - 1) / potency);
            tank.drain(amount, true);
            remaining -= amount * potency;
            if (remaining <= 0) {
                break;
            }
        }
        return remaining <= 0;
    }

    private boolean consumeWater() {
        return consumeFluidPotency(getWaterPotencyNeededPerCycle(), true);
    }

    private boolean consumeFertilizer() {
        // 装了肥料单元才强制要求供肥；否则"没有肥"只是让营养低一点
        boolean ok = consumeFluidPotency(getFertilizerPotencyNeededPerCycle(), false);
        this.hasFertilizer = ok;
        return getUnitCount(FarmType.FERTILIZER) == 0 || ok;
    }

    // ==================== 模式 ====================

    public int getMode() {
        return mode;
    }

    /** 界面按钮改模式。客户端也会调到，所以这里只动状态，不碰网络。 */
    public void setMode(int newMode) {
        int clamped = Math.floorMod(newMode, MODE_COUNT);
        if (clamped == this.mode) {
            return;
        }
        this.mode = clamped;
        this.progress = 0;
        this.maxProgress = 0;
        if (this.mode != MODE_FARM) {
            // 离开生产模式就把没攒够的零头清掉——不然切回来会凭空多一截产出
            this.dropTracker.clear();
        }
        markDirty();
        scheduleRenderUpdate();
    }

    // ==================== 存档 ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("cropqt_mode", mode);
        data.setInteger("cropqt_progress", progress);
        data.setInteger("cropqt_max_progress", maxProgress);
        data.setTag("cropqt_inventory", farmInventory.serializeNBT());
        dropTracker.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        mode = data.getInteger("cropqt_mode");
        progress = data.getInteger("cropqt_progress");
        maxProgress = data.getInteger("cropqt_max_progress");
        farmInventory.deserializeNBT(data.getCompoundTag("cropqt_inventory"));
        dropTracker.readFromNBT(data);
    }

    // ==================== 界面 ====================

    @Override
    protected @NotNull MultiblockUIFactory createUIFactory() {
        return new MultiblockUIFactory(this)
                .setSize(176, 166)
                .disableDisplay()
                .disableButtons()
                .addScreenChildren((parent, syncManager) -> {
                    parent.child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5));

                    // 档次（升级仓与苗床共同约定的那个）+ 段数 + 超频次数
                    IntSyncValue tierSync = new IntSyncValue(this::getUpgradeTier);
                    syncManager.syncValue("cropqt_farm_upgrade_tier", tierSync);
                    IntSyncValue slicesSync = new IntSyncValue(this::getSlices);
                    syncManager.syncValue("cropqt_farm_slices", slicesSync);
                    IntSyncValue maxSlicesSync = new IntSyncValue(this::getMaxSlicesForTier);
                    syncManager.syncValue("cropqt_farm_max_slices", maxSlicesSync);
                    IntSyncValue ocSync = new IntSyncValue(this::getOverclockCount);
                    syncManager.syncValue("cropqt_farm_oc", ocSync);

                    parent.child(IKey.dynamic(() -> {
                                int raw = tierSync.getIntValue();
                                String name = raw >= 0 && raw < GTValues.VN.length ? GTValues.VN[raw] : "?";
                                return net.minecraft.client.resources.I18n.format(
                                        "cropqt.farm.display.tier",
                                        name, slicesSync.getIntValue(), maxSlicesSync.getIntValue());
                            })
                            .asWidget().pos(5, 18));

                    IntSyncValue modeSync = new IntSyncValue(this::getMode, this::setMode);
                    syncManager.syncValue("cropqt_farm_mode", modeSync);
                    parent.child(IKey.dynamic(() -> net.minecraft.client.resources.I18n.format(
                                    modeNameKey(modeSync.getIntValue())))
                            .asWidget().pos(5, 30));

                    // 超频次数只在真的超得动时才显示
                    parent.child(IKey.dynamic(() -> ocSync.getIntValue() <= 0
                                    ? ""
                                    : net.minecraft.client.resources.I18n.format(
                                            "cropqt.farm.display.overclock", ocSync.getIntValue()))
                            .asWidget().pos(5, 42));

                    // 配置有问题时机器会停摆，得让玩家看见是哪一条
                    IntSyncValue errorSync = new IntSyncValue(this::getConfigError);
                    syncManager.syncValue("cropqt_farm_error", errorSync);
                    parent.child(IKey.dynamic(() -> {
                                String key = configErrorKey(errorSync.getIntValue());
                                return key == null ? "" : net.minecraft.client.resources.I18n.format(key);
                            })
                            .asWidget().pos(86, 30));

                    // 种子 / 底土 / 环境模块
                    for (int i = 0; i < INVENTORY_SIZE; i++) {
                        parent.child(new ItemSlot()
                                .pos(8 + i * 18, 46)
                                .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                                .slot(SyncHandlers.itemSlot(farmInventory, i)
                                        .singletonSlotGroup().accessibility(true, true)));
                    }

                    // 进度条
                    DoubleSyncValue progressSync = new DoubleSyncValue(this::getCycleProgressPercent);
                    syncManager.syncValue("cropqt_farm_progress", progressSync);
                    parent.child(new ProgressWidget()
                            .pos(86, 46).size(60, 16)
                            .value(progressSync)
                            .texture(GTGuiTextures.PROGRESS_BAR_ARROW, -1)
                            .direction(ProgressWidget.Direction.RIGHT));

                    // 模式按钮
                    parent.child(new CycleButtonWidget()
                            .pos(150, 4).size(18)
                            .value(modeSync)
                            .length(MODE_COUNT)
                            .background(GTGuiTextures.BUTTON)
                            .overlay(GTGuiTextures.BUTTON_MULTI_MAP)
                            .disableHoverBackground()
                            .tooltipBuilder(t -> t.addLine(IKey.dynamic(
                                    () -> net.minecraft.client.resources.I18n.format(
                                            modeNameKey(modeSync.getIntValue()))))));
                });
    }

    /** 本周期进度 0~1。 */
    public double getCycleProgressPercent() {
        return maxProgress <= 0 ? 0.0d : Math.min(1.0d, (double) progress / (double) maxProgress);
    }

    private static String modeNameKey(int mode) {
        return switch (mode) {
            case MODE_FARM -> "cropqt.farm.mode.farm";
            case MODE_OUTPUT -> "cropqt.farm.mode.output";
            default -> "cropqt.farm.mode.input";
        };
    }

    @Override
    public boolean isActive() {
        return isStructureFormed() && maxProgress > 0 && mode == MODE_FARM;
    }
}
