package com.meowmel.cropQT.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.DropTracker;
import com.meowmel.cropQT.api.FarmNutrientModel;
import com.meowmel.cropQT.api.SubSoilRequirement;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import com.meowmel.cropQT.api.registries.HydrationRegistry;
import com.meowmel.cropQT.block.BlockIndustrialFarmUnit;
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
import gregtech.api.pattern.StructureContributionKey;
import gregtech.api.pattern.StructurePieceKey;
import gregtech.api.pattern.casing.DeclarativePatternBuilder;
import gregtech.api.pattern.casing.GTStructureChannels;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.pattern.element.Elements;
import gregtech.api.pattern.element.IStructureElement;
import gregtech.api.pattern.element.StructureDefinition;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gregtech.api.util.RelativeDirection.BACK;
import static gregtech.api.util.RelativeDirection.RIGHT;
import static gregtech.api.util.RelativeDirection.UP;

/**
 * 工业农场：把作物架上的那一套搬进机器里，用 EU 换产出。
 *
 * <h2>结构</h2>
 * 三段拼接，横截面固定 5 宽 × 4 高，沿轴向堆叠 1~13 段：
 * <pre>
 * head：          body（可重复 1~13）：      tail：
 * " cCc "         " gUg "                    " cDc "
 * "cCCCc"         "g   g"                    "cDDDc"
 * "cC~Cc"         "csssc"                    "cDDDc"
 * "c   c"         "     "                    "c   c"
 * </pre>
 * 仓室<b>只能放在 head 段的 'C' 位</b>——源端就是这么限制的。
 * 末段 {@code 'D'} 在源端靠 StructureLib 的 {@code shouldSkip} 不做校验，GTQT 的 DSL 没有这个语义，
 * 所以这里退化成纯外壳（功能不受影响，仓室本来也不放那儿）。
 *
 * <h2>等级由输入电压决定</h2>
 * 只有一个控制器。{@link #getFarmTier()} 读能量仓的最高输入电压换算成 GT 电压档，
 * 段数上限 = {@code 农场等级 - MV + 1}。喂什么电压，就是什么等级。
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
    private static final int MIN_SLICES = 1;
    private static final int MAX_SLICES = 13;

    // ==================== 等级 ====================

    /** 农场最低等级（对应源端的 MIN_CASING_TIER），同时是超频计算的基准。 */
    private static final int MIN_FARM_TIER = GTValues.MV;
    /** 本项目只做到 IV。 */
    private static final int MAX_FARM_TIER = GTValues.IV;
    /** 超频次数上限——只作护栏，IV 档实际只会算出 3 次。 */
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
    /** 环境模块槽数量，与环境强化单元的上限一致。 */
    private static final int ENV_SLOT_COUNT = BlockIndustrialFarmUnit.ENVIRONMENTAL_MAX_COUNT;
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

    // ==================== 单元计数 ====================

    /**
     * 五种升级单元共用结构里的 {@code 'U'} 位，靠 match 回调把各自的类型写进
     * 结构贡献（{@link StructureContributionKey}），成型时再一次性读出来。
     *
     * <p>不能用「校验前清零 + 回调累加」那种写法：{@code doStructureCheck()} <b>每 tick</b> 都会被调用，
     * 但真正的结构匹配是事件驱动 / 异步的、隔很久才跑一次，那样清出来的计数绝大多数 tick 都是 0。
     * 贡献是每次匹配现攒的，天然跟着匹配走。
     */
    private static final StructureContributionKey<Integer, Integer> ENV_UNIT_KEY =
            StructureContributionKey.sum("drtech:industrial_farm/environmental_units");
    private static final StructureContributionKey<Integer, Integer> GROWTH_UNIT_KEY =
            StructureContributionKey.sum("drtech:industrial_farm/growth_units");
    private static final StructureContributionKey<Integer, Integer> FERTILIZER_UNIT_KEY =
            StructureContributionKey.sum("drtech:industrial_farm/fertilizer_units");
    private static final StructureContributionKey<Integer, Integer> ADVANCED_HARVEST_UNIT_KEY =
            StructureContributionKey.sum("drtech:industrial_farm/advanced_harvest_units");
    private static final StructureContributionKey<Integer, Integer> OVERCLOCKED_UNIT_KEY =
            StructureContributionKey.sum("drtech:industrial_farm/overclocked_units");

    // ==================== 状态 ====================

    /** 内部库存：种子 / 底土 / 环境模块。 */
    private final ItemStackHandler farmInventory = createFarmInventory();

    /** 农场等级，由输入电压换算。 */
    private int farmTier = MIN_FARM_TIER;
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

    // 升级单元计数，成型时从结构贡献里读出
    private int envUnitCount = 0;
    private int growthUnitCount = 0;
    private int fertilizerUnitCount = 0;
    private int advancedHarvestUnitCount = 0;
    private int overclockedUnitCount = 0;

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

    // ==================== 结构定义 ====================

    private static final StructureDefinition<?> STRUCTURE_DEFINITION = StructureDefinition.getOrBuild(
            "drtech:industrial_farm", MetaTileEntityIndustrialFarm::buildStructure);

    @Override
    protected @NotNull StructureDefinition<?> createStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    private static StructureDefinition<?> buildStructure() {
        return DeclarativePatternBuilder.start(RIGHT, UP, BACK)
                .piece(PIECE_HEAD)
                .aisle(" cCc ", "cCCCc", "cC~Cc", "c   c")
                .repeatablePiece(PIECE_BODY, MIN_SLICES, MAX_SLICES)
                .aisle(" gUg ", "g   g", "csssc", "     ")
                .withAisleChannel(GTStructureChannels.STRUCTURE_LENGTH.getName())
                .piece(PIECE_TAIL)
                .aisle(" cDc ", "cDDDc", "cDDDc", "c   c")
                .self('~', MetaTileEntityIndustrialFarm.class)
                .blocks('c', getCasingState())
                .blocks('D', getCasingState())
                .blocks('g', getGlassesState())
                .blocks('s', getSeedBedState())
                .where('U', farmUnitsElement())
                // 仓室只开在 head 段的 'C' 位——源端也是这么限制的
                .casing('C', getCasingState())
                    .maintenance()
                    .energyInput(1, 2)
                    .itemInput(1, 4)
                    .itemOutput(1, 4)
                    .fluidInput(1, 2)
                .buildStructureDefinition();
    }

    /** 五种升级单元共用 {@code 'U'}，命中哪个就把哪个的计数往结构贡献里加一。 */
    private static IStructureElement farmUnitsElement() {
        BlockIndustrialFarmUnit.UnitType[] types = BlockIndustrialFarmUnit.UnitType.values();
        IStructureElement[] unitElements = new IStructureElement[types.length];
        for (BlockIndustrialFarmUnit.UnitType type : types) {
            unitElements[type.ordinal()] = Elements.onPass(
                    context -> context.getCollector().emit(unitKey(type), 1),
                    Elements.block(getUnitState(type)));
        }
        return Elements.chain(unitElements);
    }

    private static StructureContributionKey<Integer, Integer> unitKey(BlockIndustrialFarmUnit.UnitType type) {
        switch (type) {
            case ENVIRONMENTAL_ENHANCEMENT:       return ENV_UNIT_KEY;
            case GROWTH_ACCELERATION:             return GROWTH_UNIT_KEY;
            case FERTILIZER:                      return FERTILIZER_UNIT_KEY;
            case ADVANCED_HARVESTING:             return ADVANCED_HARVEST_UNIT_KEY;
            case OVERCLOCKED_GROWTH_ACCELERATION: return OVERCLOCKED_UNIT_KEY;
            default: throw new IllegalArgumentException("Unknown unit type: " + type);
        }
    }

    // ==================== 方块 / 贴图 ====================

    /** 控制器与所有外壳的方块。GTQT 会<b>反射</b>这个方法拿 CTM 基座，缺了会让组件静默失去连接纹理。 */
    public static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    protected static IBlockState getGlassesState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    protected static IBlockState getSeedBedState() {
        return com.drppp.drtech.common.blocks.BlocksInit.SEED_BED
                .getState(BlockSeedBed.SeedBedType.SEED_BED);
    }

    protected static IBlockState getUnitState(BlockIndustrialFarmUnit.UnitType type) {
        return com.drppp.drtech.common.blocks.BlocksInit.INDUSTRIAL_FARM_UNIT.getState(type);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @NotNull ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
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
        this.envUnitCount = getAggregate(formed, ENV_UNIT_KEY);
        this.growthUnitCount = getAggregate(formed, GROWTH_UNIT_KEY);
        this.fertilizerUnitCount = getAggregate(formed, FERTILIZER_UNIT_KEY);
        this.advancedHarvestUnitCount = getAggregate(formed, ADVANCED_HARVEST_UNIT_KEY);
        this.overclockedUnitCount = getAggregate(formed, OVERCLOCKED_UNIT_KEY);

        this.importItems = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.exportItems = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.importFluids = new FluidTankList(false, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.farmTier = resolveFarmTier();
        markDirty();
    }

    /** 读结构贡献里的计数；一次都没命中时贡献不存在，按 0 算。 */
    private static int getAggregate(@NotNull FormedStructureView formed,
                                    @NotNull StructureContributionKey<Integer, Integer> key) {
        Integer value = formed.getAggregate(key);
        return value == null ? 0 : value;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.importItems = new ItemHandlerList();
        this.exportItems = new ItemHandlerList();
        this.importFluids = new FluidTankList(false);
        this.energyContainer = new EnergyContainerList(Collections.emptyList());
        this.progress = 0;
        this.maxProgress = 0;
    }

    /** 由能量仓的最高输入电压换算农场等级。 */
    private int resolveFarmTier() {
        long voltage = this.energyContainer.getHighestInputVoltage();
        for (int tier = MAX_FARM_TIER; tier >= MIN_FARM_TIER; tier--) {
            if (voltage >= GTValues.V[tier]) {
                return tier;
            }
        }
        return MIN_FARM_TIER;
    }

    public int getFarmTier() {
        return farmTier;
    }

    public int getSlices() {
        return slices;
    }

    /** 本等级允许的最大段数。 */
    public int getMaxSlicesForTier() {
        return Math.min(MAX_SLICES, farmTier - MIN_FARM_TIER + 1);
    }

    /** 结构校验：段数不能超过等级允许的长度。 */
    public boolean isSliceCountValid() {
        return slices <= getMaxSlicesForTier();
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
                // 槽位数 = 环境强化单元数，但不会超过槽位上限（单元不限量，槽位有限）
                int envIndex = slot - SLOT_ENV_START;
                if (envIndex < 0 || envIndex >= Math.min(envUnitCount, ENV_SLOT_COUNT)) {
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
        if (!isSliceCountValid()) {
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
        // 扣电
        long eu = getPowerUsage();
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
        for (int i = 0; i < Math.min(envUnitCount, ENV_SLOT_COUNT); i++) {
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
     * <p>各单元的数量上限（环境 2 / 肥料 1 / 高级收割 2 / 超频 1）在这里<b>按效果封顶</b>，
     * 不做结构校验——超出的单元不生效，但不会让已经搭好的机器失效。
     */
    public double getGrowthSpeedMultiplier() {
        double multiplier = 1.0d;
        multiplier += growthUnitCount * BlockIndustrialFarmUnit.GROWTH_ACCELERATION_BONUS;
        multiplier *= 1.0d + Math.min(fertilizerUnitCount, BlockIndustrialFarmUnit.FERTILIZER_MAX_COUNT)
                * BlockIndustrialFarmUnit.FERTILIZER_GROWTH_MULTIPLIER;
        if (overclockedUnitCount > 0) {
            multiplier *= Math.pow(2.0d, getOverclockCount());
        }
        return multiplier;
    }

    /** 收割轮数倍率：等级 + 肥料加成是加法，高级收割是乘法。 */
    public double getHarvestRoundMultiplier() {
        double multiplier = 1.0d;
        multiplier += farmTier * 0.2d;
        multiplier += Math.min(fertilizerUnitCount, BlockIndustrialFarmUnit.FERTILIZER_MAX_COUNT)
                * BlockIndustrialFarmUnit.FERTILIZER_HARVEST_ROUND_BONUS;
        multiplier *= 1.0d + Math.min(advancedHarvestUnitCount, BlockIndustrialFarmUnit.ADVANCED_HARVESTING_MAX_COUNT)
                * BlockIndustrialFarmUnit.ADVANCED_HARVESTING_ROUND_MULTIPLIER;
        return multiplier;
    }

    /**
     * 超频次数：以 <b>MV（农场最低等级）</b>为基准，看当前等级能 4 倍它几次。
     *
     * <p>基准为什么是 MV 而不是农场自己的等级：后者恒等于能量仓电压（{@link #resolveFarmTier()} 就是
     * 从电压反推的），拿它当基准的话「能 4 倍几次」永远是 0，超频单元会变成一个纯装饰方块。
     * 源端之所以有超频余量，是因为它的基准来自<b>组件 tier</b>、而电压来自<b>仓室 tier</b>，两者天然有落差；
     * 我们按你的决定把等级并成了电压一个变量，就得把基准钉在农场的最低等级上，这个落差才重新出现。
     *
     * <p>耗电不用另外加：{@link #getPowerUsage()} 本来就按 {@code V[farmTier]} 收，
     * 而 {@code V[MV] × 4^超频次数} 正好等于 {@code V[farmTier]}——超频的电费已经付在基础耗电里了。
     * 水肥则在 {@link #getOverclockPotencyMultiplier()} 里按 2^超频次数 放大，这是超频的实际代价。
     */
    private int getOverclockCount() {
        if (overclockedUnitCount <= 0) {
            return 0;
        }
        long eut = GTValues.V[MIN_FARM_TIER];
        long ceiling = GTValues.V[farmTier];
        int overclocks = 0;
        while (overclocks < MAX_OVERCLOCKS
                && eut * (long) OverclockingLogic.STD_VOLTAGE_FACTOR <= ceiling) {
            eut *= (long) OverclockingLogic.STD_VOLTAGE_FACTOR;
            overclocks++;
        }
        return overclocks;
    }

    /** 超频倍率（水肥消耗也要跟着放大）。 */
    private double getOverclockPotencyMultiplier() {
        return overclockedUnitCount > 0 ? Math.pow(2.0d, getOverclockCount()) : 1.0d;
    }

    // ==================== 耗电 / 耗水 / 耗肥 ====================

    /** 基础耗电 + 各升级单元的附加耗电。 */
    public long getPowerUsage() {
        long base = GTValues.V[farmTier];
        long power = base;
        power += (long) (base * BlockIndustrialFarmUnit.ENVIRONMENTAL_POWER_INCREASE
                * Math.min(envUnitCount, BlockIndustrialFarmUnit.ENVIRONMENTAL_MAX_COUNT));
        power += (long) (base * BlockIndustrialFarmUnit.GROWTH_ACCELERATION_POWER_INCREASE * growthUnitCount);
        power += (long) (base * BlockIndustrialFarmUnit.FERTILIZER_POWER_INCREASE
                * Math.min(fertilizerUnitCount, BlockIndustrialFarmUnit.FERTILIZER_MAX_COUNT));
        power += (long) (base * BlockIndustrialFarmUnit.ADVANCED_HARVESTING_POWER_INCREASE
                * Math.min(advancedHarvestUnitCount, BlockIndustrialFarmUnit.ADVANCED_HARVESTING_MAX_COUNT));
        return Math.max(1L, power);
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

    /** 种子床容量：{@code (7 + 4×等级)²}，照搬源端。 */
    public int getSeedBedCapacity() {
        int diameter = 2 * (3 + 2 * farmTier) + 1;
        return diameter * diameter;
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
        return fertilizerUnitCount == 0 || ok;
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

                    IntSyncValue tierSync = new IntSyncValue(this::getFarmTier);
                    syncManager.syncValue("cropqt_farm_tier", tierSync);
                    IntSyncValue slicesSync = new IntSyncValue(this::getSlices);
                    syncManager.syncValue("cropqt_farm_slices", slicesSync);
                    parent.child(IKey.dynamic(() -> net.minecraft.client.resources.I18n.format(
                                    "cropqt.farm.display.tier",
                                    GTValues.VN[Math.min(tierSync.getIntValue(), GTValues.VN.length - 1)],
                                    slicesSync.getIntValue()))
                            .asWidget().pos(5, 18));

                    IntSyncValue modeSync = new IntSyncValue(this::getMode, this::setMode);
                    syncManager.syncValue("cropqt_farm_mode", modeSync);
                    parent.child(IKey.dynamic(() -> net.minecraft.client.resources.I18n.format(
                                    modeNameKey(modeSync.getIntValue())))
                            .asWidget().pos(5, 30));

                    // 段数超过等级允许的长度时机器会停摆，得让玩家看见原因
                    IntSyncValue maxSlicesSync = new IntSyncValue(this::getMaxSlicesForTier);
                    syncManager.syncValue("cropqt_farm_max_slices", maxSlicesSync);
                    parent.child(IKey.dynamic(() -> slicesSync.getIntValue() <= maxSlicesSync.getIntValue()
                                    ? ""
                                    : net.minecraft.client.resources.I18n.format(
                                            "cropqt.farm.display.slice_overflow",
                                            slicesSync.getIntValue(), maxSlicesSync.getIntValue()))
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
        switch (mode) {
            case MODE_FARM:   return "cropqt.farm.mode.farm";
            case MODE_OUTPUT: return "cropqt.farm.mode.output";
            case MODE_INPUT:
            default:          return "cropqt.farm.mode.input";
        }
    }

    @Override
    public boolean isActive() {
        return isStructureFormed() && maxProgress > 0 && mode == MODE_FARM;
    }
}
