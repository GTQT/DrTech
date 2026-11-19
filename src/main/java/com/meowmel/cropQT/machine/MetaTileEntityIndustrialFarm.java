package com.meowmel.cropQT.machine;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.drppp.drtech.common.blocks.BlocksInit;
import com.meowmel.cropQT.api.CropConstants;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.DropTracker;
import com.meowmel.cropQT.api.FarmNutrientModel;
import com.meowmel.cropQT.api.SubSoilRequirement;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
import com.meowmel.cropQT.api.capability.FarmType;
import com.meowmel.cropQT.api.registries.HydrationRegistry;
import com.meowmel.cropQT.block.BlockFarmUpgrade;
import com.meowmel.cropQT.block.BlockSeedBed;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.item.ItemEnvironmentalModule;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.IEnergyContainer;
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
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gregtech.api.util.RelativeDirection.*;

/**
 * 工业农场：把作物架上的那一套搬进机器里，用 EU 换产出。
 *
 * <h2>等级由苗床与升级方块的档次决定</h2>
 * 只有一个控制器。体段顶部的 {@code U} 位摆的是五种升级组件方块（源端 {@code IFUpgradeElement}），
 * 结构里的所有升级组件与所有苗床<b>必须同一档</b>，那个档就是农场的升级等级
 * （{@link #upgradeTier}）：容量、基础耗电、每轮水肥用量、收割轮数加成、段数上限全跟着它走。
 *
 * <p>电压只决定<b>能喂多少功率</b>；能喂到多高由玻璃档位约束（源端 {@code 能量仓档位 <= 玻璃档}）。
 * 电压与基础耗电之间的落差正是超频生长加速组件存在的理由 —— 用低档苗床配高压电，超频换生长速度。
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

    /** 源端 body 段数下限 1（整机至少 1 段 = 头 + 1 体 + 尾）。 */
    private static final int MIN_SLICES = CropConstants.FARM_MIN_SLICES;
    /** 源端 MAX_SLICES = 1 + MAX_CASING_TIER - MIN_CASING_TIER，MV..OpV 即 12。 */
    private static final int MAX_SLICES = CropConstants.FARM_MAX_SLICES;

    // ==================== 等级 ====================

    /** 农场最低档次（对应源端的 MIN_CASING_TIER）。 */
    private static final int MIN_FARM_TIER = GTValues.MV;
    /** 顶档（本仓库 OpV，对应源端 UXV）。 */
    private static final int MAX_FARM_TIER = BlockSeedBed.MAX_TIER;

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
    /**
     * 结构里玻璃的等级（GT 玻璃方块自带档位）。源端要求玻璃 ≥ MV，
     * 并且高档次农场必须用对应档位的玻璃（源端 {@code chainAllGlasses}）。
     */
    private int glassTier = TIER_UNSET;
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

    /** 「产物是战利品表」的作物攒下的抽取次数，小数部分留到下一周期。 */
    private double lootRollProgress = 0.0d;

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
                .where('G', glassElement())
                .where('S', seedBedElement())
                .where('U', upgradeElement())
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

    /**
     * 玻璃：GT 四种玻璃各自带档位，整座结构必须同档且 ≥ MV。
     *
     * <p>源端用 {@code chainAllGlasses} 记录玻璃档，并校验能量仓的
     * {@code getTierForStructure()} 不得超过玻璃档（源端 UMV 以上才豁免）。
     * 这里同样的档位会被记下来供停机校验使用。
     */
    private static IStructureElement glassElement() {
        BlockGlassCasing.CasingType[] types = BlockGlassCasing.CasingType.values();
        IStructureElement[] byType = new IStructureElement[types.length];
        for (int i = 0; i < types.length; i++) {
            BlockGlassCasing.CasingType type = types[i];
            byType[i] = Elements.onPass(
                    context -> {
                        if (context.getController() instanceof MetaTileEntityIndustrialFarm farm) {
                            farm.agreeGlassTier(glassTierOf(type));
                        }
                    },
                    Elements.block(MetaBlocks.TRANSPARENT_CASING.getState(type)));
        }
        return Elements.chain(byType);
    }

    /**
     * 五类升级组件方块 + 木框架（源端 {@code U} 位允许木框架）。
     *
     * <p>照源端：升级是方块，不是仓；每种方块 × 每一档都会在结构匹配时
     * 累计一次数量并参与「全结构同档」的约定。
     */
    /**
     * 玻璃档位。编译依赖的 GTQT 发行 jar 里 {@code CasingType} 还没有
     * {@code getGlassTier}（源码分支才加了 {@code IGlassTier}），所以这里按枚举名
     * 写死，与源码里的构造参数保持一致：Tempered=MV、Cleanroom=HV、Laminated=IV、Fusion=UV。
     */
    private static int glassTierOf(BlockGlassCasing.CasingType type) {
        switch (type) {
            case CLEANROOM_GLASS:
                return GTValues.HV;
            case LAMINATED_GLASS:
                return GTValues.IV;
            case FUSION_GLASS:
                return GTValues.UV;
            case TEMPERED_GLASS:
            default:
                return GTValues.MV;
        }
    }

    private static IStructureElement upgradeElement() {
        List<IStructureElement> elements = new ArrayList<>();
        BlockSeedBed.SeedBedType[] types = BlockSeedBed.SeedBedType.values();
        for (BlockFarmUpgrade block : BlockFarmUpgrade.all().keySet()) {
            FarmType type = block.getFarmType();
            for (BlockSeedBed.SeedBedType variant : types) {
                int tier = variant.getTier();
                elements.add(Elements.onPass(
                        context -> {
                            if (context.getController() instanceof MetaTileEntityIndustrialFarm farm) {
                                farm.addUpgradeUnit(type);
                                farm.agreeUpgradeTier(tier);
                            }
                        },
                        Elements.block(block.getState(variant))));
            }
        }
        // 源端允许木框架占一个升级位。
        elements.add(Elements.frames(gregtech.api.unification.material.Materials.Wood));
        return Elements.chain(elements.toArray(new IStructureElement[0]));
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

        // 升级等级与组件个数在结构匹配时已由回调记下（苗床 + 五类升级方块同一套约定）。
        // 这里不再额外清空/累加，否则会把刚匹配到的计数抹掉。

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

    /**
     * 每次结构检查前清空「这一遍数出来的」组件计数与档位。
     *
     * <p>结构匹配的回调只会在匹配成功时触发，但一次结构检查可能调用多次
     * （初始成型、增量复检、拆改后重查）；不在这里归零，计数会一遍遍累加。
     * 清空放在这里而不是 {@link #formStructure}，是因为成型回调只跑一次，
     * 而计数必须在“匹配过程中”就攒好。
     */
    @Override
    public void checkStructurePattern() {
        this.upgradeTier = TIER_UNSET;
        this.glassTier = TIER_UNSET;
        if (unitCounts != null) {
            this.unitCounts.clear();
        }
        super.checkStructurePattern();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.importItems = new ItemHandlerList();
        this.exportItems = new ItemHandlerList();
        this.importFluids = new FluidTankList(false);
        this.energyContainer = new EnergyContainerList(Collections.emptyList());
        this.upgradeTier = TIER_UNSET;
        this.glassTier = TIER_UNSET;
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
        return upgradeTier >= MIN_FARM_TIER && upgradeTier <= MAX_FARM_TIER;
    }

    /** 玻璃等级是否可用：源端要求 ≥ MV，且整座结构同档。 */
    public boolean isGlassTierValid() {
        return glassTier >= BlockSeedBed.MIN_TIER && glassTier <= BlockSeedBed.MAX_TIER;
    }

    public int getGlassTier() {
        return glassTier;
    }

    /** 结构匹配时记录玻璃档位；混档记成冲突。 */
    private void agreeGlassTier(int tier) {
        if (glassTier == TIER_UNSET) {
            glassTier = tier;
        } else if (glassTier != tier) {
            glassTier = TIER_CONFLICT;
        }
    }

    /** 结构匹配时累计某个升级方块的个数。 */
    private void addUpgradeUnit(FarmType type) {
        unitCounts.merge(type, 1, Integer::sum);
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
    /** 能量仓电压超过了升级档次（已废弃：源端无此规则，留作旧存档兼容的占位值）。 */
    public static final int ERROR_VOLTAGE_OVER_TIER = 5;
    /** 玻璃等级低于 MV，或结构里混用了不同档位的玻璃。 */
    public static final int ERROR_GLASS_TIER = 6;
    /** 能量仓类型不合法：没装 OCGAU 却接了多安仓、多安与普通混用、接了激光仓，或 OCGAU 下多安仓超过 1 个。 */
    public static final int ERROR_HATCH_TYPE = 7;

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
        if (!isGlassTierValid()) {
            return ERROR_GLASS_TIER;
        }
        // 源端：玻璃档以下不允许接更高档的能量仓（`glassTier < UMV && hatch.getTierForStructure() > glassTier`）。
        // 本仓库没有 UMV 玻璃，四种 GT 玻璃最高到 UV，因此这条一直生效。
        if (energyContainer != null && glassTier >= 0
                && energyContainer.getHighestInputVoltage() > GTValues.V[glassTier]) {
            return ERROR_GLASS_TIER;
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
        // 源端超频生长加速仓只从 ZPM 起注册（BlockOverclockedGrowthAccelerationUnit.MIN_TIER = ZPM）。
        if (getUnitCount(FarmType.OVERCLOCKED_GROWTH_ACCELERATION) > 0
                && upgradeTier < GTValues.ZPM) {
            return ERROR_TIER_INVALID;
        }
        // 源端没有「能量仓电压不得超过升级档次」这条规则：高压仓只是喂得进更多功率，
        // 想把这部分功率转成超频，必须装超频生长加速仓（OCGAU），否则多出来的电压没有去处。
        if (!isEnergyHatchConfigurationValid()) {
            return ERROR_HATCH_TYPE;
        }
        return ERROR_NONE;
    }

    /**
     * 源端 OCGAU 的 exotic hatch 规则：
     * <ul>
     *   <li>没装 OCGAU 时不允许接多安能量仓；</li>
     *   <li>装了 OCGAU 时多安仓最多 1 个，且不能与普通能量仓混用；</li>
     *   <li>激光仓一律不允许（本仓库激光走独立的 {@code INPUT_LASER} 能力，结构也接不进来，
     *       这里仍显式挡一次，和源端行为一致）。</li>
     * </ul>
     * 只在结构成型后检查：成型前 {@code getAbilities} 是空的，会把「还没接仓」误判成合法。
     */
    private boolean isEnergyHatchConfigurationValid() {
        if (!isStructureFormed()) {
            return true;
        }
        if (!getAbilities(MultiblockAbility.INPUT_LASER).isEmpty()) {
            return false;
        }
        List<IEnergyContainer> energyHatches = getAbilities(MultiblockAbility.INPUT_ENERGY);
        if (energyHatches == null || energyHatches.isEmpty()) {
            return true;
        }
        int multiAmp = 0;
        for (IEnergyContainer hatch : energyHatches) {
            if (hatch.getInputAmperage() > 2) {
                multiAmp++;
            }
        }
        boolean hasOcgau = getUnitCount(FarmType.OVERCLOCKED_GROWTH_ACCELERATION) > 0;
        if (hasOcgau) {
            // 源端 MAX_MULTIAMP_EHATCH_AMOUNT = 1
            if (multiAmp > 1) {
                return false;
            }
            return multiAmp == 0 || multiAmp == energyHatches.size();
        }
        return multiAmp == 0;
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
            case ERROR_GLASS_TIER:          return "cropqt.farm.error.glass";
            case ERROR_HATCH_TYPE:          return "cropqt.farm.error.hatch";
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

    // ---- 只读查询：给 TOP / GUI 用。源端的 scanner 文本在 GTCEu 里没有对应 API（没有 getInfoData），
    // ---- 所以把同样的字段暴露成 getter，由显示层自行排版。

    /** 种子槽里的种子；只读，别拿去改。 */
    public ItemStack getSeedStack() {
        return getSeed();
    }

    /** 种子对应的作物；没有种子或 id 已失效时返回 null。 */
    @Nullable
    public CropType getSeedCrop() {
        ItemStack seed = getSeed();
        if (seed.isEmpty()) {
            return null;
        }
        return CropRegistry.get(ItemCropSeed.getCropId(seed));
    }

    /** 当前种子的营养分；没种子返回 -1（源端也是只在有种子时才报这一行）。 */
    public int getNutrientScoreForDisplay() {
        CropType crop = getSeedCrop();
        return crop == null ? -1 : getNutrientScore(crop);
    }

    /** 肥料单元是否已装：决定每周期报的是普通液肥还是浓缩液肥。 */
    public boolean hasFertilizerUnit() {
        return getUnitCount(FarmType.FERTILIZER) > 0;
    }

    // ==================== 内部库存 ====================

    private ItemStackHandler createFarmInventory() {
        return new GTItemStackHandler(this, INVENTORY_SIZE) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                // 源端：机器跑着的时候不许插东西（输入模式自己吞种子/底土走 setStackInSlot，不受影响）。
                if (maxProgress > 0) {
                    return false;
                }
                if (slot == SLOT_SEED) {
                    if (!isAnalyzedSeed(stack) || needsSubSoil(stack)) {
                        return false;
                    }
                    CropType crop = CropRegistry.get(ItemCropSeed.getCropId(stack));
                    if (crop == null || crop.getMinSeedBedTier() > upgradeTier) {
                        return false;
                    }
                    // 源端用「模拟成有肥状态后生长速度 > 0」判可种；这能挡住高 tier 作物塞进低档苗床。
                    int growthRate = FarmNutrientModel.getGrowthRate(
                            getNutrientScore(crop), crop.getTier(),
                            ItemCropSeed.getCropStats(stack).getGrowth());
                    return growthRate > 0;
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
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                // 源端：运行中不可抽；有底土时种子/底土只能走输出模式。
                if (maxProgress > 0) {
                    return ItemStack.EMPTY;
                }
                if (slot < SLOT_ENV_START && !getSubSoil().isEmpty()) {
                    return ItemStack.EMPTY;
                }
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                // 源端：种子/底土槽上限 = 苗床并行容量，其余（环境槽）固定 1。
                if (slot == SLOT_SEED || slot == SLOT_SUB_SOIL) {
                    return Math.max(1, getSeedBedCapacity());
                }
                return 1;
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
    /**
     * 从输入总线吞种子与底土进内部槽。
     *
     * <p>源端不是「一次吞一个」：它把种子槽补到苗床容量（并行数），
     * 底土槽也跟着补到同样的数量；已经有一份种子时只吞同种种子。
     */
    private void runInputMode() {
        int capacity = getSeedBedCapacity();
        if (capacity <= 0) {
            return;
        }
        ItemStack existing = getSeed();
        if (!existing.isEmpty() && existing.getCount() >= capacity) {
            return;
        }

        // 找一份可用的种子：槽里已有就找同种，否则找第一份已分析的。
        int seedSlot = -1;
        ItemStack candidate = ItemStack.EMPTY;
        for (int slot = 0; slot < importItems.getSlots(); slot++) {
            ItemStack stack = importItems.getStackInSlot(slot);
            if (!isAnalyzedSeed(stack)) {
                continue;
            }
            if (!existing.isEmpty() && !sameSeed(existing, stack)) {
                continue;
            }
            CropType crop = CropRegistry.get(ItemCropSeed.getCropId(stack));
            if (crop == null || crop.getMinSeedBedTier() > upgradeTier) {
                continue;
            }
            seedSlot = slot;
            candidate = stack;
            break;
        }
        if (seedSlot < 0) {
            return;
        }

        CropType crop = CropRegistry.get(ItemCropSeed.getCropId(candidate));
        int wanted = capacity - (existing.isEmpty() ? 0 : existing.getCount());
        int available = 0;
        for (int slot = 0; slot < importItems.getSlots(); slot++) {
            if (sameSeed(candidate, importItems.getStackInSlot(slot))) {
                available += importItems.getStackInSlot(slot).getCount();
            }
        }
        int parallels = Math.min(wanted, available);

        SubSoilRequirement requirement = crop.getSubSoilRequirement();
        ItemStack existingSubSoil = getSubSoil();
        ItemStack subSoilTemplate = existingSubSoil;
        if (requirement != null) {
            if (!existingSubSoil.isEmpty() && !requirement.matches(blockStateOf(existingSubSoil))) {
                return;
            }
            int subSoilHave = existingSubSoil.isEmpty() ? 0 : existingSubSoil.getCount();
            parallels = Math.min(parallels, subSoilHave + countSubSoil(requirement));
            if (subSoilTemplate.isEmpty()) {
                subSoilTemplate = findSubSoilTemplate(requirement);
            }
        }
        if (parallels <= 0 || (requirement != null && subSoilTemplate.isEmpty())) {
            return;
        }

        // 扣种子
        int remaining = parallels;
        for (int slot = 0; slot < importItems.getSlots() && remaining > 0; slot++) {
            if (!sameSeed(candidate, importItems.getStackInSlot(slot))) {
                continue;
            }
            remaining -= importItems.extractItem(slot, remaining, false).getCount();
        }
        ItemStack newSeed = (existing.isEmpty() ? candidate : existing).copy();
        newSeed.setCount((existing.isEmpty() ? 0 : existing.getCount()) + parallels);
        setSeed(newSeed);

        // 扣底土：和种子并行数一致；已有底土只补差额，超出则保持原数量。
        if (requirement != null) {
            int subSoilHave = existingSubSoil.isEmpty() ? 0 : existingSubSoil.getCount();
            int need = Math.max(0, parallels - subSoilHave);
            int consumed = 0;
            for (int slot = 0; slot < importItems.getSlots() && need > 0; slot++) {
                ItemStack stack = importItems.getStackInSlot(slot);
                if (stack.isEmpty() || isAnalyzedSeed(stack) || !requirement.matches(blockStateOf(stack))) {
                    continue;
                }
                int taken = importItems.extractItem(slot, need, false).getCount();
                need -= taken;
                consumed += taken;
            }
            ItemStack newSubSoil = subSoilTemplate.copy();
            newSubSoil.setCount(subSoilHave + consumed);
            setSubSoil(newSubSoil);
        }
    }

    private static boolean sameSeed(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty()
                && ItemStack.areItemsEqual(a, b)
                && ItemStack.areItemStackTagsEqual(a, b);
    }

    private int countSubSoil(SubSoilRequirement requirement) {
        int total = 0;
        for (int slot = 0; slot < importItems.getSlots(); slot++) {
            ItemStack stack = importItems.getStackInSlot(slot);
            if (stack.isEmpty() || isAnalyzedSeed(stack)) {
                continue;
            }
            if (requirement.matches(blockStateOf(stack))) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /** 在输入总线里找一块满足要求的底土，只取模板不消耗。 */
    private ItemStack findSubSoilTemplate(SubSoilRequirement requirement) {
        for (int slot = 0; slot < importItems.getSlots(); slot++) {
            ItemStack candidate = importItems.getStackInSlot(slot);
            if (candidate.isEmpty() || isAnalyzedSeed(candidate)) {
                continue;
            }
            if (requirement.matches(blockStateOf(candidate))) {
                return candidate.copy();
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

    /**
     * 把内部种子与底土退回输出总线。
     *
     * <p>源端不是「能塞多少塞多少」：并行数取种子与底土数量的较小值，
     * 再被输出总线的剩余空间压一次（ejectionHelper 先模拟、塞不下就把并行数往下压），
     * 一次 5 tick 全部退完。这里用 {@code simulate=true} 先探容量，避免塞一半卡住。
     */
    private void runOutputMode() {
        ItemStack seed = getSeed();
        ItemStack subSoil = getSubSoil();
        int parallels = Integer.MAX_VALUE;
        if (!seed.isEmpty()) {
            parallels = seed.getCount();
        }
        if (!subSoil.isEmpty()) {
            parallels = Math.min(parallels, subSoil.getCount());
        }
        if (parallels == Integer.MAX_VALUE || parallels <= 0) {
            return;
        }

        int fit = Integer.MAX_VALUE;
        if (!seed.isEmpty()) {
            fit = Math.min(fit, countOutputFit(seed, parallels));
        }
        if (!subSoil.isEmpty()) {
            fit = Math.min(fit, countOutputFit(subSoil, parallels));
        }
        int moved = Math.min(parallels, fit);
        if (moved <= 0) {
            return;
        }

        if (!seed.isEmpty()) {
            ItemStack leftover = GTTransferUtils.insertItem(exportItems, GTUtility.copy(moved, seed), false);
            int inserted = moved - leftover.getCount();
            ItemStack rest = seed.copy();
            rest.setCount(seed.getCount() - inserted);
            setSeed(rest.getCount() <= 0 ? ItemStack.EMPTY : rest);
        }
        if (!subSoil.isEmpty()) {
            ItemStack leftover = GTTransferUtils.insertItem(exportItems, GTUtility.copy(moved, subSoil), false);
            int inserted = moved - leftover.getCount();
            ItemStack rest = subSoil.copy();
            rest.setCount(subSoil.getCount() - inserted);
            setSubSoil(rest.getCount() <= 0 ? ItemStack.EMPTY : rest);
        }
    }

    /** 输出总线在不改动内容的前提下还能装下多少个 {@code template}（上限 {@code amount}）。 */
    private int countOutputFit(ItemStack template, int amount) {
        ItemStack probe = template.copy();
        probe.setCount(amount);
        ItemStack leftover = GTTransferUtils.insertItem(exportItems, probe, true);
        return amount - leftover.getCount();
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

        // 源端：种子数量超过苗床容量不能种；作物自身的最低苗床档次也要满足。
        if (seed.getCount() > getSeedBedCapacity()) {
            return;
        }
        if (crop.getMinSeedBedTier() > upgradeTier) {
            return;
        }

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
        // 累积掉落（源端：dropChance * 1.03^gain 轮，每轮按条目 chance 期望展开）。
        // 并行数 = 种子堆叠数；水肥与电力不随并行放大，只有产出放大。
        int parallels = seed.getCount();
        double rounds = FarmNutrientModel.getAvgDropRounds(crop.getDropChance(), stats.getGain())
                * getHarvestRoundMultiplier();
        double avgIncrease = (stats.getGain() + 1) / 100.0d;
        for (ItemStack drop : crop.getDrops()) {
            dropTracker.add(drop, (drop.getCount() + avgIncrease) * rounds * progressPerCycle * parallels);
        }
        for (CropType.ChanceDrop chanceDrop : crop.getChanceDrops()) {
            dropTracker.add(chanceDrop.item,
                    (chanceDrop.item.getCount() + avgIncrease) * chanceDrop.chance * rounds
                            * progressPerCycle * parallels);
        }
        accumulateLootRolls(crop, rounds * progressPerCycle * parallels);
        pushCompletedDrops();
    }

    /**
     * 给「产物是战利品表」的作物（钓鱼竿）攒抽取次数。
     *
     * <p>固定掉落表能按小数累积（1.3 份就攒 1.3），战利品表没这回事 ——
     * 它只能整次抽取。所以这里把预期次数攒成一个小数，
     * 每跨过 1 就真抽一次表，抽出来的东西原样进 {@link DropTracker}。
     *
     * <p>抽表要 {@link WorldServer}（{@code getHarvestDrops()} 里也是这么判的），
     * 客户端那一侧整个 {@code updateFormedValid} 都不跑，不用额外挡。
     */
    private void accumulateLootRolls(CropType crop, double expectedRolls) {
        String table = crop.getLootTable();
        if (table == null || table.isEmpty() || expectedRolls <= 0.0d) {
            return;
        }
        this.lootRollProgress += expectedRolls;
        int whole = (int) this.lootRollProgress;
        if (whole <= 0) {
            return;
        }
        this.lootRollProgress -= whole;

        if (!(getWorld() instanceof WorldServer server)) {
            return;
        }
        LootTable lootTable = server.getLootTableManager()
                .getLootTableFromLocation(new ResourceLocation(table));
        // 一次抽表要建一次 LootContext，别在循环里重复建
        LootContext context = new LootContext.Builder(server).build();
        for (int i = 0; i < whole; i++) {
            for (ItemStack rolled : lootTable.generateLootForPools(getWorld().rand, context)) {
                dropTracker.add(rolled, rolled.getCount());
            }
        }
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
        // 从「苗床档次定的耗电」往上涨，看能 4 倍几次才撞到「能量仓给的电压」。
        // 源端不设次数上限：能超几次完全由「组件档次定的基准耗电」与「仓室电压」的落差决定。
        long ceiling = this.energyContainer.getHighestInputVoltage();
        long eut = powerUsage;
        int overclocks = 0;
        while (eut <= Long.MAX_VALUE / OverclockingLogic.STD_VOLTAGE_FACTOR
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
    private boolean consumeFluidPotency(int neededPotency, boolean water, boolean fertilizerUnit) {
        int remaining = neededPotency;
        for (int i = 0; i < importFluids.getTanks(); i++) {
            IFluidTank tank = importFluids.getTankAt(i);
            FluidStack stored = tank.getFluid();
            if (stored == null || stored.amount <= 0) {
                continue;
            }
            int potency;
            if (water) {
                potency = HydrationRegistry.getWater(stored.getFluid());
            } else if (fertilizerUnit) {
                // 源端：装了肥料单元时只收浓缩液肥，且不吃 potency 加成（1 点/mB）。
                potency = FertilizerRegistry.getFertilizerUnit(stored.getFluid());
            } else {
                potency = FertilizerRegistry.getFertilizer(stored.getFluid());
            }
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
        return consumeFluidPotency(getWaterPotencyNeededPerCycle(), true, false);
    }

    private boolean consumeFertilizer() {
        // 装了肥料单元才强制要求供肥；否则"没有肥"只是让营养低一点
        boolean ok = consumeFluidPotency(getFertilizerPotencyNeededPerCycle(), false,
                getUnitCount(FarmType.FERTILIZER) > 0);
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
                // 176x166 是原来只放槽位与进度条的高度；下面新增了三行信息，
                // 面板加高 24px，免得和自动贴在底部的玩家背包撞在一起。
                .setSize(176, 190)
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

                    // 收获倍率 / 生长倍率 / 水肥需求：源端把它们放在 scanner 文本里，
                    // GTCEu 没有 getInfoData，就摊在 GUI 下方，玩家不用扫描器也能看。
                    IntSyncValue harvestPctSync = new IntSyncValue(
                            () -> (int) Math.round(getHarvestRoundMultiplier() * 100.0d));
                    syncManager.syncValue("cropqt_farm_harvest_pct", harvestPctSync);
                    IntSyncValue growthPctSync = new IntSyncValue(
                            () -> (int) Math.round(getGrowthSpeedMultiplier() * 100.0d));
                    syncManager.syncValue("cropqt_farm_growth_pct", growthPctSync);
                    IntSyncValue waterSync = new IntSyncValue(this::getWaterPotencyNeededPerCycle);
                    syncManager.syncValue("cropqt_farm_water_per_cycle", waterSync);
                    IntSyncValue fertSync = new IntSyncValue(this::getFertilizerPotencyNeededPerCycle);
                    syncManager.syncValue("cropqt_farm_fert_per_cycle", fertSync);
                    IntSyncValue nutrientSync = new IntSyncValue(this::getNutrientScoreForDisplay);
                    syncManager.syncValue("cropqt_farm_nutrient", nutrientSync);
                    IntSyncValue fertUnitSync = new IntSyncValue(() -> hasFertilizerUnit() ? 1 : 0);
                    syncManager.syncValue("cropqt_farm_fert_unit", fertUnitSync);

                    parent.child(IKey.dynamic(() -> net.minecraft.client.resources.I18n.format(
                                    "cropqt.farm.display.modifiers",
                                    harvestPctSync.getIntValue(), growthPctSync.getIntValue()))
                            .asWidget().pos(8, 66));
                    parent.child(IKey.dynamic(() -> net.minecraft.client.resources.I18n.format(
                                    fertUnitSync.getIntValue() > 0
                                            ? "cropqt.farm.display.water_fert.enriched"
                                            : "cropqt.farm.display.water_fert",
                                    waterSync.getIntValue(), fertSync.getIntValue()))
                            .asWidget().pos(8, 78));
                    parent.child(IKey.dynamic(() -> nutrientSync.getIntValue() < 0
                                    ? ""
                                    : net.minecraft.client.resources.I18n.format(
                                            "cropqt.farm.display.nutrient",
                                            nutrientSync.getIntValue(),
                                            CropConstants.MAX_NUTRIENT_SCORE))
                            .asWidget().pos(8, 90));

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
