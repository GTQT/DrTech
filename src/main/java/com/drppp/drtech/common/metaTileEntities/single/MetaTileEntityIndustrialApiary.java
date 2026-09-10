package com.drppp.drtech.common.metaTileEntities.single;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.api.ItemHandler.InOutItemStackHandler;
import com.drppp.drtech.api.ItemHandler.OnlyBeesStackhandler;
import com.drppp.drtech.api.ItemHandler.OnlyUpgradeStackhandler;
import com.drppp.drtech.api.utils.DrtechUtils;
import com.drppp.drtech.api.utils.GT_ApiaryUpgrade;
import com.drppp.drtech.api.utils.ItemId;
import com.drppp.drtech.client.Textures;
import com.drppp.drtech.network.SyncInit;
import com.drppp.drtech.network.UpdateTileEntityPacket;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.FlowerManager;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeeRoot;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.IBeekeepingMode;
import forestry.api.arboriculture.EnumGermlingType;
import forestry.api.core.BiomeHelper;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.core.ForestryAPI;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorState;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IIndividual;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.items.ItemBeeGE;
import forestry.apiculture.items.ItemRegistryApiculture;
import forestry.core.errors.EnumErrorCode;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 工业蜂箱（Industrial Apiary）。
 *
 * <p>把林业（Forestry）的蜜蜂繁育流程自动化：往内部槽位放入蜂后/公主与雄蜂，
 * 机器按蜜蜂基因组的寿命推进一个"生命周期"，结束时产出公主、雄蜂以及该物种的产物，
 * 全程消耗 EU。
 *
 * <p>机器有四层状态，别把它们混在一起：
 * <ul>
 *     <li>{@code isActive} —— 是否开机。停机后不会再开始新流程，但已缓存的状态仍保留。</li>
 *     <li>{@code isWorkingEnabled} —— 是否允许工作。暂停只阻断流程推进。</li>
 *     <li>{@code isProcessing} —— 当前是否真的有一个流程在推进。</li>
 *     <li>{@link #isWorking()} —— 上面两者同时成立，渲染与状态文本都以它为准。</li>
 * </ul>
 *
 * <p>升级槽里的 {@link GT_ApiaryUpgrade} 由 {@link #updateModifiers()} 汇总成一组
 * 环境/速率修正值并缓存在字段中，机器自身再作为 {@link IBeeModifier} 把这些修正值
 * 提供给林业 API，因此林业在计算产物与寿命时会直接读到本机的升级效果。
 */
public class MetaTileEntityIndustrialApiary extends TieredMetaTileEntity implements IWorkable,
        IBeeHousing, IBeeHousingInventory, IErrorLogic, IBeeModifier, IBeeListener {

    // ==================== 常量 ====================

    /** 单个生命周期对应的基础 tick 数，基因组寿命会以它为基准换算成机器运行时间。 */
    public static final int beeCycleLength = 550;
    /** 未叠加任何升级与超频时的基础 EU/t 消耗。 */
    public static final int baseEUtUsage = 37;

    /** 蜜蜂槽位：蜂后 / 公主。 */
    private static final int SLOT_QUEEN = 0;
    /** 蜜蜂槽位：雄蜂。 */
    private static final int SLOT_DRONE = 1;
    /** 升级槽起始下标。 */
    private static final int UPGRADE_SLOT_START = 0;
    /** 升级槽数量，必须与 {@link #inventoryUpgrade} 的容量一致。 */
    private static final int UPGRADE_SLOT_COUNT = 4;
    /** 输出槽数量，必须与 {@link #inventoryOutput} / {@link #mOutputItems} 的容量一致。 */
    private static final int OUTPUT_SLOT_COUNT = 12;

    /** 每推进这么多 tick 校验一次工作条件与能量。 */
    private static final int WORK_CHECK_INTERVAL = 100;
    /** 进度同步到客户端的最小间隔（tick）。 */
    private static final int PROGRESS_SYNC_INTERVAL = 5;
    /** 客户端蜜蜂粒子效果的播放间隔（tick）。 */
    private static final int FX_INTERVAL = 2;

    /** 网络数据包 ID：当前流程的 EU/t 消耗。 */
    private static final int DATA_ID_EU_USAGE = 4800;
    /** 网络数据包 ID：当前正在处理的蜂后。 */
    private static final int DATA_ID_USED_QUEEN = 4801;
    /** 网络数据包 ID：错误状态集合。 */
    private static final int DATA_ID_ERROR_STATES = 4802;
    /** 网络数据包 ID：是否有流程正在推进。 */
    private static final int DATA_ID_PROCESSING = 4803;
    /** 网络数据包 ID：进度（已运行 tick / 总时长 / 百分比）。 */
    private static final int DATA_ID_PROGRESS = 4804;
    /** 网络数据包 ID：所有者 UUID。 */
    private static final int DATA_ID_OWNER_UUID = 1919;
    /** 网络数据包 ID：所有者名称。 */
    private static final int DATA_ID_OWNER_NAME = 1920;

    // ==================== 状态 ====================

    protected final ICubeRenderer renderer;
    private final IBeeRoot beeRoot = (IBeeRoot) AlleleManager.alleleRegistry.getSpeciesRoot("rootBees");

    /** 是否处于开机状态，停机后不会再开始新流程。 */
    private boolean isActive = true;
    /** 是否允许工作，暂停只阻断流程推进。 */
    private boolean isWorkingEnabled = true;
    /** 当前是否真的有一个流程在推进，用于渲染与状态文本。 */
    private boolean isProcessing = false;

    /** 当前流程已运行的 tick 数。 */
    private int mProgresstime = 0;
    /** 当前流程的总时长（tick），0 表示没有进行中的流程。 */
    private int mMaxProgresstime = 100;
    /** 进度百分比（0~100），仅用于显示。 */
    private int progressPer = 0;
    /** 当前流程的 EU/t 消耗。 */
    private int mEUt = 0;

    /** 当前速度等级，超频倍率为 {@code 1 << mSpeed}。 */
    private int mSpeed = 0;
    /** 为 true 时 {@link #mSpeed} 始终跟随升级给出的上限，否则只允许在手动调低后固定。 */
    private boolean mLockedSpeed = true;
    /** 是否把产出的蜂后自动放回蜂后槽。 */
    private boolean mAutoQueen = true;

    /** 机器所有者，用于林业的繁育统计；未放置者时为 null。 */
    private UUID uid = null;
    /** 机器所有者名称，与 {@link #uid} 一起下发到客户端。 */
    private String name = null;

    // ==================== 内部容器 ====================

    private final ItemStackHandler inventoryBees = new OnlyBeesStackhandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirty();
            if (slot == SLOT_QUEEN) {
                clearBeeCaches();
            }
        }
    };
    private final ItemStackHandler inventoryUpgrade = new OnlyUpgradeStackhandler(UPGRADE_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            upgradeInventoryDirty = true;
            markDirty();
        }
    };
    private final ItemStackHandler inventoryOutput = new InOutItemStackHandler(OUTPUT_SLOT_COUNT, false) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirty();
        }
    };

    /** 待写入输出槽的产出。流程结算时先填这里，再由 {@link #flushPendingOutputs()} 派发。 */
    private final ItemStack[] mOutputItems = new ItemStack[OUTPUT_SLOT_COUNT];

    // ==================== 流程状态 ====================

    /** 当前流程消耗的蜂后（来自蜂后槽，流程结束时已从槽位取走）。 */
    private ItemStack usedQueen = null;
    /** {@link #usedQueen} 对应的蜜蜂对象，是蜂后或公主缓存的解析结果。 */
    private IBee usedQueenBee = null;
    /** 蜜蜂效果的可变数据，[0] 主效果、[1] 隐性效果；{@code doFX} 会整体替换它。 */
    private IEffectData[] effectData = new IEffectData[2];
    /** 本次流程是否禁用授粉（繁育流程与"不授粉"升级都会置位）。 */
    private boolean retrievingPollenInThisOperation = false;
    /** 授粉过程中暂存的花粉，授粉成功后清空。 */
    private IIndividual retrievedpollen = null;
    /** 授粉尝试间隔（tick），随流程总时长动态计算。 */
    private int pollinationDelay = 100;

    /** 升级槽内容变更后置位，触发 {@link #updateModifiers()} 重新汇总修正值。 */
    private boolean upgradeInventoryDirty = true;
    /** 错误状态集合变更后置位，触发一次增量同步。 */
    private boolean errorStatesDirty = false;

    /** 上一次同步到客户端的进度，三项全部相同才跳过同步。 */
    private int lastSyncedProgressTime = Integer.MIN_VALUE;
    private int lastSyncedMaxProgress = Integer.MIN_VALUE;
    private int lastSyncedProgressPercent = Integer.MIN_VALUE;

    // ==================== 环境修正值（由升级汇总） ====================

    private String flowerType = "";
    private BlockPos flowercoords = null;
    private Block flowerBlock;
    private int flowerBlockMeta;
    private float terrorityMod = 1f;
    private float mutationMod = 1f;
    private float lifespanMod = 1f;
    private float productionMod = 2f;
    private float floweringMod = 1f;
    private float geneticDecayMod = 1f;
    private float energyMod = 1f;
    private boolean sealedMod = false;
    private boolean selfLightedMod = false;
    private boolean selfUnlightedMod = false;
    private boolean sunlightSimulatedMod = false;
    private Biome biomeOverride = null;
    private float humidityMod = 0f;
    private float temperatureMod = 0f;
    private boolean isAutomated = false;
    private boolean isRetrievingPollen = false;

    /** 当前生效的错误状态集合，由林业的工作条件校验写入。 */
    private final Set<IErrorState> mErrorStates = new HashSet<>();

    public MetaTileEntityIndustrialApiary(ResourceLocation metaTileEntityId, ICubeRenderer renderer) {
        super(metaTileEntityId, GTValues.UHV);
        this.renderer = renderer;
        // 数组字段初始化为 null，统一成 ItemStack.EMPTY 免得后面到处判空
        Arrays.fill(this.mOutputItems, ItemStack.EMPTY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityIndustrialApiary(this.metaTileEntityId, renderer);
    }

    // ==================== 渲染 ====================

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.renderOverlays(renderState, translation, pipeline);
    }

    protected void renderOverlays(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        this.renderer.renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isActive(), isWorking());
    }

    // ==================== 主循环 ====================

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            if (DrtConfig.machine.EnableIndustrialApiaryTx) {
                tickClientBeeFx();
            }
            return;
        }
        tickServerWork();
    }

    /** 客户端：按固定间隔播放当前蜂后的粒子效果。 */
    private void tickClientBeeFx() {
        if (!isWorking() || getOffsetTimer() % FX_INTERVAL != 0) {
            return;
        }
        if (usedQueen == null || usedQueen.isEmpty()) {
            return;
        }
        // 效果只在服务端真正生效，客户端这里只负责表现
        final IBee bee = beeRoot.getMember(usedQueen);
        if (bee != null) {
            effectData = bee.doFX(effectData, this);
        }
    }

    /** 服务端：推进已有流程或尝试开始新流程，并把进度同步给客户端。 */
    private void tickServerWork() {
        if (isWorking()) {
            tickRunningProcess();
        } else if (isActive() && isWorkingEnabled()) {
            tryStartProcess();
        }

        progressPer = this.mMaxProgresstime > 0
                ? (int) (((float) this.mProgresstime / (float) this.mMaxProgresstime) * 100)
                : 0;
        // 起止两端必定同步，中间按固定间隔追赶
        if (getOffsetTimer() % PROGRESS_SYNC_INTERVAL == 0 || mProgresstime == 0 || mProgresstime >= mMaxProgresstime) {
            syncProgressData();
        }
    }

    /** 推进一个已经在运行的流程：扣电、累加进度、结算产出。 */
    private void tickRunningProcess() {
        if (this.mProgresstime < 0) {
            this.mProgresstime++;
            return;
        }
        if (this.energyContainer.addEnergy(-this.mEUt) >= 0) {
            // 电量不足，停止推进但保留进度，来电后从断点继续
            setProcessing(false);
            return;
        }
        if (this.hasErrors()) {
            // 环境不满足时每 100 tick 复检一次，避免每 tick 都重新搜花
            if (getOffsetTimer() % WORK_CHECK_INTERVAL == 0 && !canWork(usedQueen)) {
                setProcessing(false);
            }
            return;
        }

        this.mProgresstime++;
        tickPollination();

        if (this.mProgresstime % WORK_CHECK_INTERVAL == 0 && !canWork(usedQueen)) {
            setProcessing(false);
            return;
        }
        if (this.mProgresstime >= this.mMaxProgresstime) {
            finishProcess();
        }
    }

    /** 每 tick 施加蜜蜂效果，并按授粉间隔尝试授粉。 */
    private void tickPollination() {
        if (usedQueen == null || usedQueen.isEmpty()) {
            return;
        }
        if (usedQueenBee == null) {
            usedQueenBee = beeRoot.getMember(usedQueen);
        }
        if (usedQueenBee == null) {
            return;
        }

        doEffect();

        // 繁育流程与"不授粉"升级都跳过授粉
        if (retrievingPollenInThisOperation || floweringMod <= 0f) {
            return;
        }
        if (this.mProgresstime % pollinationDelay != 0) {
            return;
        }
        if (retrievedpollen == null) {
            retrievedpollen = usedQueenBee.retrievePollen(this);
        }
        if (retrievedpollen != null && (usedQueenBee.pollinateRandom(this, retrievedpollen)
                || this.mProgresstime % (pollinationDelay * 5) == 0)) {
            retrievedpollen = null;
        }
    }

    /** 流程结束：结算加速效果、刷新修正值并清空状态。 */
    private void finishProcess() {
        if (usedQueenBee != null && usedQueen != null && !usedQueen.isEmpty()) {
            doAcceleratedEffects();
        }
        updateModifiers();
        flushPendingOutputs();
        resetProcessingState();
    }

    /** 尝试开始一个新流程。 */
    private void tryStartProcess() {
        if (hasPendingProcess()) {
            // 有被中断的流程（比如刚断电），电量恢复后直接继续而不重新结算
            if (this.energyContainer.getEnergyStored() > mEUt) {
                setProcessing(true);
            }
        } else if (this.energyContainer.getEnergyStored() > mEUt) {
            if (checkRecipe()) {
                setProcessing(true);
            }
        }
    }

    /** 判断当前是否有一个被中断但尚未丢弃的流程。 */
    private boolean hasPendingProcess() {
        return usedQueen != null && !usedQueen.isEmpty() && this.mMaxProgresstime > 0;
    }

    // ==================== 流程结算 ====================

    /**
     * 结算一个完整流程：校验工作条件、生成产出、计算耗时与耗电。
     *
     * @return 是否成功开始了一个流程；false 时调用方不应把机器置为运行中
     */
    public boolean checkRecipe() {
        updateModifiers();
        if (!canWork()) {
            return false;
        }

        final ItemStack queenStack = getQueen();
        usedQueen = queenStack.copy();
        setUsedQueen(usedQueen);

        if (beeRoot.getType(queenStack) == EnumBeeType.QUEEN) {
            processQueenCycle(queenStack);
        } else {
            processBreedingCycle();
        }
        return true;
    }

    /**
     * 处理一个完整的蜂后生命周期：产出公主/雄蜂与物种产物，并计算耗时耗电。
     *
     * @param queenStack 槽位中待处理的蜂后
     */
    private void processQueenCycle(ItemStack queenStack) {
        final IBee bee = beeRoot.getMember(queenStack);
        usedQueenBee = bee;

        // ---- 生命周期：蜂箱与养蜂模式的寿命修正叠加后，决定本次流程包含的"代数" ----
        float lifespanModifier = this.getLifespanModifier(null, null, 1.f);
        final IBeekeepingMode mode = beeRoot.getBeekeepingMode(this.getWorld());
        final IBeeModifier beeModifier = mode.getBeeModifier();
        lifespanModifier *= beeModifier.getLifespanModifier(null, null, 1.f);
        // 寿命修正描述的是"活得久"，换算成每代消耗的血量要取倒数
        final float cycles = bee.getHealth() / (1.f / lifespanModifier);

        // ---- 产物：按掉落表累计期望数量 ----
        final Map<ItemId, ItemStack> pollen = collectPollen(bee, cycles);
        // 花粉在本次结算开头一次性产完，清掉上一轮残留的暂存
        retrievedpollen = null;
        // 装筛网时本流程禁用授粉，产物里改为直接给花粉
        retrievingPollenInThisOperation = isRetrievingPollen;

        final IBeeGenome genome = bee.getGenome();
        final float speed = genome.getSpeed();
        final float prodMod = getProductionModifier(null, 0f) + beeModifier.getProductionModifier(null, 0f);
        final DropTable drops = collectProductDrops(genome, speed, prodMod, cycles);

        // ---- 写入输出槽：蜜蜂固定占前几格，产物与花粉依次往后排 ----
        final int maxSlots = mOutputItems.length;
        final IApiaristTracker breedingTracker = beeRoot.getBreedingTracker(getWorld(), getOwner());
        final int beeOutputEnd = writeBeeOutputs(bee, queenStack, breedingTracker, 0);

        // 蜂后已经消耗掉，先清空槽位再写产物，方便产物回填
        setQueen(ItemStack.EMPTY);
        final int itemOutputEnd = writeItemOutputs(drops, beeOutputEnd, maxSlots);
        writePollenOutputs(pollen, itemOutputEnd, maxSlots);

        // ---- 超频 ----
        applyOverclock(beeOutputEnd, cycles);
    }

    /**
     * 处理一次繁育：公主 + 雄蜂 → 蜂后。结果直接写入 0 号输出槽。
     */
    private void processBreedingCycle() {
        // 繁育期间不授粉
        retrievingPollenInThisOperation = true;

        this.mMaxProgresstime = 100;
        this.mProgresstime = 0;
        final int usedDivider = Math.min(100, 1 << this.mSpeed);
        this.mMaxProgresstime /= usedDivider;
        setEUtUsage(computeEUtUsage(usedDivider));

        final IBee princess = beeRoot.getMember(getQueen());
        usedQueenBee = princess;
        final IBee drone = beeRoot.getMember(getDrone());
        if (princess == null || drone == null) {
            // 条件校验保证了这里不该为空，真出现了就放弃本次结算
            return;
        }
        princess.mate(drone);

        final NBTTagCompound nbt = new NBTTagCompound();
        princess.writeToNBT(nbt);
        final ItemRegistryApiculture apicultureItems = ModuleApiculture.getItems();
        this.mOutputItems[0] = apicultureItems.beeQueenGE.getItemStack();
        this.mOutputItems[0].setTagCompound(nbt);
        beeRoot.getBreedingTracker(getWorld(), getOwner()).registerQueen(princess);

        setQueen(ItemStack.EMPTY);
        final ItemStack droneStack = getDrone();
        droneStack.setCount(droneStack.getCount() - 1);
        if (droneStack.getCount() == 0) {
            setDrone(ItemStack.EMPTY);
        }
    }

    /** 一次流程的产物掉落表：{@link #amounts} 是期望个数，{@link #prototypes} 是写入时用的原型堆。 */
    private static final class DropTable {
        private final Map<ItemId, Float> amounts = new LinkedHashMap<>();
        private final Map<ItemId, ItemStack> prototypes = new LinkedHashMap<>();

        /** 累加一个产物的期望个数，并记下它第一次出现时的原型堆。 */
        private void merge(ItemStack stack, float amount) {
            final ItemId id = DrtechUtils.ItemIdManager.createNoCopy(stack);
            amounts.merge(id, amount, Float::sum);
            prototypes.computeIfAbsent(id, k -> stack);
        }
    }

    /**
     * 汇总一次流程的产物掉落表。
     *
     * <p>主产物按原概率计算；副产物概率减半；只有主、副产物同时处于"欢欣"状态
     * （环境完全满足）时才计算特产。
     *
     * @param genome  蜂后基因组
     * @param speed   基因组速度
     * @param prodMod 产量修正
     * @param cycles  本次流程包含的代数
     */
    private DropTable collectProductDrops(IBeeGenome genome, float speed, float prodMod, float cycles) {
        final DropTable drops = new DropTable();
        final IAlleleBeeSpecies primary = genome.getPrimary();
        final IAlleleBeeSpecies secondary = genome.getSecondary();

        for (Map.Entry<ItemStack, Float> entry : primary.getProductChances().entrySet()) {
            drops.merge(entry.getKey(),
                    getFinalChance(entry.getValue(), speed, prodMod) * entry.getKey().getCount() * cycles);
        }
        for (Map.Entry<ItemStack, Float> entry : secondary.getProductChances().entrySet()) {
            drops.merge(entry.getKey(),
                    getFinalChance(entry.getValue() / 2f, speed, prodMod) * entry.getKey().getCount() * cycles);
        }
        if (primary.isJubilant(genome, this) && secondary.isJubilant(genome, this)) {
            for (Map.Entry<ItemStack, Float> entry : primary.getSpecialtyChances().entrySet()) {
                drops.merge(entry.getKey(),
                        getFinalChance(entry.getValue(), speed, prodMod) * entry.getKey().getCount() * cycles);
            }
        }
        return drops;
    }

    /**
     * 收集本次流程的花粉产出。只有装了筛网升级且授粉修正大于 0 时才生效。
     *
     * @param cycles 本次流程包含的代数
     * @return 花粉堆，同种花粉已经累加过数量
     */
    private Map<ItemId, ItemStack> collectPollen(IBee bee, float cycles) {
        final Map<ItemId, ItemStack> pollen = new LinkedHashMap<>();
        if (!isRetrievingPollen || floweringMod <= 0f) {
            return pollen;
        }
        // 代数的小数部分按概率进位，避免低代数时产物被完全抹掉
        final int wholeCycles = (int) cycles
                + (getWorld().rand.nextFloat() < (cycles - (float) ((int) cycles)) ? 1 : 0);
        for (int cycle = 0; cycle < wholeCycles; cycle++) {
            final IIndividual retrieved = bee.retrievePollen(this);
            if (retrieved == null) {
                continue;
            }
            final ItemStack stack = retrieved.getGenome()
                    .getSpeciesRoot()
                    .getMemberStack(retrieved, EnumGermlingType.POLLEN);
            if (stack == null) {
                continue;
            }
            final ItemId id = DrtechUtils.ItemIdManager.createNoCopy(stack);
            final ItemStack accumulated = pollen.get(id);
            if (accumulated == null) {
                final ItemStack first = stack.copy();
                first.setCount(stack.getCount());
                pollen.put(id, first);
            } else {
                accumulated.setCount(accumulated.getCount() + stack.getCount());
            }
        }
        return pollen;
    }

    /**
     * 写入公主与雄蜂。能正常繁殖时生成公主 + 雄蜂，蜂后寿命已耗尽时只退化为公主。
     *
     * @return 写入后的下一个空闲输出下标
     */
    private int writeBeeOutputs(IBee bee, ItemStack queenStack, IApiaristTracker breedingTracker, int index) {
        if (!bee.canSpawn()) {
            final ItemStack convert = new ItemBeeGE(EnumBeeType.PRINCESS).getItemStack();
            final NBTTagCompound nbt = new NBTTagCompound();
            queenStack.writeToNBT(nbt);
            convert.setTagCompound(nbt);
            this.mOutputItems[index++] = convert;
            return index;
        }

        final IBee princess = bee.spawnPrincess(this);
        if (princess != null) {
            breedingTracker.registerPrincess(princess);
            this.mOutputItems[index++] = beeRoot.getMemberStack(princess, EnumBeeType.PRINCESS);
        }

        final List<IBee> spawnedDrones = bee.spawnDrones(this);
        if (spawnedDrones.isEmpty()) {
            return index;
        }
        // 同种雄蜂只占一个槽位，其余累加数量
        final Map<ItemId, ItemStack> seenDrones = new HashMap<>(spawnedDrones.size());
        for (IBee droneBee : spawnedDrones) {
            final ItemStack droneStack = beeRoot.getMemberStack(droneBee, EnumBeeType.DRONE);
            breedingTracker.registerDrone(droneBee);
            final ItemId droneId = DrtechUtils.ItemIdManager.createNoCopy(droneStack);
            final ItemStack existing = seenDrones.get(droneId);
            if (existing != null) {
                existing.setCount(existing.getCount() + droneStack.getCount());
            } else {
                this.mOutputItems[index++] = droneStack;
                seenDrones.put(droneId, droneStack);
            }
        }
        return index;
    }

    /**
     * 把掉落表写入输出槽。数量按期望值向下取整，小数部分按概率进位；
     * 超过单组上限时拆成多组。
     *
     * @return 写入后的下一个空闲输出下标
     */
    private int writeItemOutputs(DropTable drops, int index, int maxSlots) {
        for (Map.Entry<ItemId, Float> entry : drops.amounts.entrySet()) {
            final float amount = entry.getValue();
            final ItemStack stack = drops.prototypes.get(entry.getKey()).copy();
            stack.setCount((int) amount + (getWorld().rand.nextFloat() < (amount - (float) (int) amount) ? 1 : 0));
            if (stack.getCount() <= 0 || index >= maxSlots) {
                continue;
            }
            while (true) {
                if (stack.getCount() <= stack.getMaxStackSize()) {
                    this.mOutputItems[index++] = stack;
                    break;
                }
                this.mOutputItems[index++] = stack.splitStack(stack.getMaxStackSize());
                if (index >= maxSlots) {
                    break;
                }
            }
        }
        return index;
    }

    /**
     * 写入花粉。花粉不参与超频放大，输出槽放不下时直接丢弃。
     */
    private void writePollenOutputs(Map<ItemId, ItemStack> pollen, int index, int maxSlots) {
        for (ItemStack pollenStack : pollen.values()) {
            if (index >= maxSlots) {
                break;
            }
            this.mOutputItems[index++] = pollenStack;
        }
    }

    /**
     * 应用超频：流程时长压缩多少倍，蜜蜂产出就放大多少倍，EU/t 同步上升。
     *
     * <p>压缩倍数取 {@code 1 << mSpeed}，但最多只把基础时长压到 1/100，
     * 否则高等级升级会让流程短到无法正常结算授粉。
     *
     * @param beeOutputEnd 蜜蜂产出在 {@link #mOutputItems} 中的结束下标（不含），产出固定从 0 号槽开始排
     * @param cycles       本次流程包含的代数
     */
    private void applyOverclock(int beeOutputEnd, float cycles) {
        final int baseTime = (int) (cycles * (float) beeCycleLength);
        final int maxDivider = baseTime / 100;
        final int usedDivider = 1 << this.mSpeed;
        final int actualDivider = usedDivider / Math.min(usedDivider, maxDivider);

        this.mMaxProgresstime = baseTime / Math.min(usedDivider, maxDivider);
        for (int slot = beeOutputEnd - 1; slot >= 0; slot--) {
            // 这些槽位刚写入过蜜蜂，判空只是防御，正常不会命中
            if (!this.mOutputItems[slot].isEmpty()) {
                this.mOutputItems[slot].setCount(this.mOutputItems[slot].getCount() * actualDivider);
            }
        }

        // 授粉不用每 tick 都尝试，按单代时长分摊，但不低于 20 tick
        pollinationDelay = Math.max((int) (this.mMaxProgresstime / cycles), 20);

        this.mProgresstime = 0;
        setEUtUsage(computeEUtUsage(usedDivider));
    }

    /**
     * 按超频倍率计算 EU/t 消耗：基础消耗 × 环境耗电修正 × 倍率，
     * 倍率大于 1 时额外叠加安培补偿。
     */
    private int computeEUtUsage(int usedDivider) {
        int eu = (int) ((float) baseEUtUsage * this.energyMod * usedDivider);
        if (usedDivider == 2) {
            eu += 32;
        } else if (usedDivider > 2) {
            eu += 32 * (usedDivider << (this.mSpeed - 2));
        }
        return eu;
    }

    /**
     * 中止当前流程。
     *
     * @param active 取消后机器的开机状态。停机时传 {@code false}：在归还蜂后、清空产出后彻底停机；
     *               传 {@code true} 只丢弃流程状态并保持开机
     */
    public void cancelProcess(boolean active) {
        final boolean hadQueen = usedQueen != null && !usedQueen.isEmpty()
                && beeRoot.isMember(usedQueen, EnumBeeType.QUEEN);

        if (!active && !this.getWorld().isRemote && hadQueen) {
            // 归还蜂后并彻底停机
            Arrays.fill(mOutputItems, ItemStack.EMPTY);
            setEUtUsage(0);
            mProgresstime = 0;
            mMaxProgresstime = 0;
            progressPer = 0;
            setProcessing(false);
            setActive(false);
            // 蜂后能还就还，还不了就丢进输出槽
            if (inventoryBees.getStackInSlot(SLOT_QUEEN).isEmpty()) {
                setQueen(usedQueen);
            } else {
                GTTransferUtils.insertItem(inventoryOutput, usedQueen, false);
            }
            setWorkingEnabled(false);
            setUsedQueen(null);
            clearBeeCaches();
            syncProgressData();
            return;
        }

        // 没有需要归还的蜂后，只丢弃流程状态
        setProcessing(false);
        setActive(active);
        setUsedQueen(null);
        clearBeeCaches();
    }

    /** 清空一次流程的全部中间状态。 */
    private void resetProcessingState() {
        Arrays.fill(mOutputItems, ItemStack.EMPTY);
        setEUtUsage(0);
        mProgresstime = 0;
        mMaxProgresstime = 0;
        progressPer = 0;
        setUsedQueen(null);
        setProcessing(false);
        clearBeeCaches();
        syncProgressData();
    }

    /** 把待输出队列里的东西优先塞回机器内部槽位，剩下的丢进输出槽。 */
    private void flushPendingOutputs() {
        for (int slot = 0; slot < mOutputItems.length; slot++) {
            final ItemStack output = mOutputItems[slot];
            if (output.isEmpty()) {
                continue;
            }
            if (!tryRouteBeeOutput(output, slot)) {
                GTTransferUtils.insertItem(inventoryOutput, output.copy(), false);
            }
            mOutputItems[slot] = ItemStack.EMPTY;
        }
    }

    /**
     * 尝试把产出的蜜蜂放回机器内部槽位。
     *
     * @param slotIndex 该产出在 {@link #mOutputItems} 中的下标，0 号槽参与"自动蜂后"回填
     * @return true 表示已被内部槽位接走，调用方不应再写进输出槽
     */
    private boolean tryRouteBeeOutput(ItemStack stack, int slotIndex) {
        if (stack.isEmpty()) {
            return true;
        }
        if (isAutomated) {
            if (beeRoot.isMember(stack, EnumBeeType.QUEEN) || beeRoot.isMember(stack, EnumBeeType.PRINCESS)) {
                if (inventoryBees.insertItem(SLOT_QUEEN, stack.copy(), true).isEmpty()) {
                    inventoryBees.insertItem(SLOT_QUEEN, stack.copy(), false);
                    return true;
                }
            } else if (beeRoot.isMember(stack, EnumBeeType.DRONE)
                    && inventoryBees.insertItem(SLOT_DRONE, stack.copy(), true).isEmpty()) {
                inventoryBees.insertItem(SLOT_DRONE, stack.copy(), false);
                return true;
            }
        }
        if (mAutoQueen && slotIndex == 0 && beeRoot.isMember(stack, EnumBeeType.QUEEN)
                && inventoryBees.insertItem(SLOT_QUEEN, stack.copy(), true).isEmpty()) {
            inventoryBees.insertItem(SLOT_QUEEN, stack.copy(), false);
            return true;
        }
        return false;
    }

    /** 清掉所有与当前蜂后绑定的缓存，蜂后变更或流程结束时调用。 */
    private void clearBeeCaches() {
        usedQueenBee = null;
        retrievedpollen = null;
        retrievingPollenInThisOperation = false;
        flowercoords = null;
        flowerBlock = null;
        flowerBlockMeta = 0;
        flowerType = "";
        Arrays.fill(effectData, null);
    }

    /** 把某个容器里的东西全部吐到机器脚下。 */
    private void dropInventoryContents(ItemStackHandler handler) {
        final BlockPos pos = getPos();
        final World world = getWorld();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            final ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy()));
            handler.extractItem(slot, stack.getCount(), false);
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        dropInventoryContents(inventoryBees);
        dropInventoryContents(inventoryUpgrade);
        dropInventoryContents(inventoryOutput);
    }

    // ==================== 状态读写 ====================

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        if (this.isWorkingEnabled == b) {
            if (!b) {
                setProcessing(false);
            }
            return;
        }
        this.isWorkingEnabled = b;
        markDirty();
        writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        if (!b) {
            setProcessing(false);
        }
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    /** 切换开机状态并同步到客户端。 */
    public void setActive(boolean active) {
        this.isActive = active;
        markDirty();
        writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(isActive));
    }

    public boolean isWorking() {
        return this.isActive && this.isProcessing;
    }

    private void setProcessing(boolean processing) {
        if (this.isProcessing == processing) {
            return;
        }
        this.isProcessing = processing;
        markDirty();
        writeCustomData(DATA_ID_PROCESSING, buf -> buf.writeBoolean(this.isProcessing));
    }

    /** 设置当前流程的 EU/t 消耗并同步到客户端。 */
    private void setEUtUsage(int euUsage) {
        this.mEUt = euUsage;
        markDirty();
        writeCustomData(DATA_ID_EU_USAGE, buf -> buf.writeInt(this.mEUt));
    }

    /** 设置当前正在处理的蜂后并同步到客户端。 */
    private void setUsedQueen(ItemStack queenStack) {
        this.usedQueen = queenStack == null ? ItemStack.EMPTY : queenStack;
        markDirty();
        writeCustomData(DATA_ID_USED_QUEEN, buf -> buf.writeItemStack(this.usedQueen));
    }

    /** 记录机器所有者，用于林业的繁育统计。 */
    public void setUUID(EntityPlayer player) {
        this.uid = player.getUniqueID();
        this.name = player.getName();
        this.writeCustomData(DATA_ID_OWNER_UUID, buf -> buf.writeUniqueId(this.uid));
        this.writeCustomData(DATA_ID_OWNER_NAME, buf -> buf.writeString(name));
    }

    @Nullable
    @Override
    public GameProfile getOwner() {
        if (uid != null) {
            return new GameProfile(uid, name);
        }
        return null;
    }

    @Override
    public int getProgress() {
        return this.mProgresstime;
    }

    @Override
    public int getMaxProgress() {
        return mMaxProgresstime;
    }

    private double getProgressPercent() {
        return this.getMaxProgress() == 0 ? 0.0 : (double) this.getProgress() / ((double) this.getMaxProgress());
    }

    // ==================== 网络同步 ====================

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeBoolean(this.isProcessing);
        if (usedQueen == null) {
            usedQueen = ItemStack.EMPTY;
        }
        buf.writeItemStack(usedQueen);
        buf.writeInt(mEUt);
        buf.writeInt(mProgresstime);
        buf.writeInt(mMaxProgresstime);
        buf.writeInt(progressPer);
        writeData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
        isWorkingEnabled = buf.readBoolean();
        isProcessing = buf.readBoolean();
        try {
            usedQueen = buf.readItemStack();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mEUt = buf.readInt();
        mProgresstime = buf.readInt();
        mMaxProgresstime = buf.readInt();
        progressPer = buf.readInt();
        readData(buf);
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == DATA_ID_OWNER_UUID) {
            this.uid = buf.readUniqueId();
            return;
        }
        if (dataId == DATA_ID_OWNER_NAME) {
            this.name = buf.readString(500);
            return;
        }
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == DATA_ID_PROCESSING) {
            isProcessing = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == DATA_ID_PROGRESS) {
            mProgresstime = buf.readInt();
            mMaxProgresstime = buf.readInt();
            progressPer = buf.readInt();
        } else if (dataId == DATA_ID_EU_USAGE) {
            mEUt = buf.readInt();
        } else if (dataId == DATA_ID_USED_QUEEN) {
            try {
                usedQueen = buf.readItemStack();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (dataId == DATA_ID_ERROR_STATES) {
            readData(buf);
        }
    }

    /** 进度有变化时才发包，避免每 tick 都刷一次同步。 */
    private void syncProgressData() {
        if (getWorld() == null || getWorld().isRemote) {
            return;
        }
        if (lastSyncedProgressTime == mProgresstime
                && lastSyncedMaxProgress == mMaxProgresstime
                && lastSyncedProgressPercent == progressPer) {
            return;
        }
        lastSyncedProgressTime = mProgresstime;
        lastSyncedMaxProgress = mMaxProgresstime;
        lastSyncedProgressPercent = progressPer;
        writeCustomData(DATA_ID_PROGRESS, buf -> {
            buf.writeInt(mProgresstime);
            buf.writeInt(mMaxProgresstime);
            buf.writeInt(progressPer);
        });
    }

    // ==================== 存档 ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", isActive);
        data.setBoolean("isWorkingEnabled", isWorkingEnabled);
        data.setBoolean("isProcessing", isProcessing);
        data.setInteger("Progresstime", this.mProgresstime);
        data.setInteger("maxProgresstime", this.mMaxProgresstime);
        data.setTag("invBees", inventoryBees.serializeNBT());
        data.setTag("invUpgrade", inventoryUpgrade.serializeNBT());
        data.setTag("invOutputs", inventoryOutput.serializeNBT());
        data.setInteger("mEut", mEUt);
        data.setInteger("mSpeed", mSpeed);
        if (usedQueen != null) {
            final NBTTagCompound queenTag = new NBTTagCompound();
            usedQueen.writeToNBT(queenTag);
            data.setTag("usedQueen", queenTag);
        }
        data.setBoolean("autoQueen", mAutoQueen);

        final NBTTagList outputList = new NBTTagList();
        for (int i = 0; i < mOutputItems.length; i++) {
            if (!mOutputItems[i].isEmpty()) {
                final NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                mOutputItems[i].writeToNBT(itemTag);
                outputList.appendTag(itemTag);
            }
        }
        data.setTag("mOutputItems", outputList);

        if (uid != null) {
            data.setUniqueId("PlayerUUID", uid);
        }
        if (name != null) {
            data.setString("PlayerName", name);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isActive = data.getBoolean("isActive");
        isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        isProcessing = data.getBoolean("isProcessing");
        mProgresstime = data.getInteger("Progresstime");
        mMaxProgresstime = data.getInteger("maxProgresstime");
        inventoryBees.deserializeNBT(data.getCompoundTag("invBees"));
        inventoryUpgrade.deserializeNBT(data.getCompoundTag("invUpgrade"));
        inventoryOutput.deserializeNBT(data.getCompoundTag("invOutputs"));
        upgradeInventoryDirty = true;
        clearBeeCaches();
        mEUt = data.getInteger("mEut");
        mSpeed = data.getInteger("mSpeed");

        if (data.hasKey("usedQueen")) {
            usedQueen = new ItemStack(data.getCompoundTag("usedQueen"));
            if (!usedQueen.isEmpty()) {
                usedQueenBee = beeRoot.getMember(usedQueen);
            }
        } else {
            usedQueen = null;
        }
        mAutoQueen = data.getBoolean("autoQueen");

        Arrays.fill(mOutputItems, ItemStack.EMPTY);
        final NBTTagList outputList = data.getTagList("mOutputItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < outputList.tagCount(); i++) {
            final NBTTagCompound itemTag = outputList.getCompoundTagAt(i);
            final int slot = itemTag.getInteger("Slot");
            if (slot >= 0 && slot < mOutputItems.length) {
                mOutputItems[slot] = new ItemStack(itemTag);
            }
        }

        if (data.hasKey("PlayerUUIDMost")) {
            uid = data.getUniqueId("PlayerUUID");
        }
        if (data.hasKey("PlayerName")) {
            name = data.getString("PlayerName");
        }
        errorStatesDirty = false;
    }

    // ==================== 能力 ====================

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            // 顶面接蜜蜂（进料），其余面接输出槽
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                    side == EnumFacing.UP ? this.inventoryBees : this.inventoryOutput);
        }
        return super.getCapability(capability, side);
    }

    // ==================== UI ====================

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        final ModularUI.Builder builder = ModularUI.builder(Textures.BACKGROUND, 176, 166);
        buildApiaryHeader(builder);
        buildBeeInventory(builder);
        buildUpgradeInventory(builder);
        buildOutputInventory(builder);
        buildApiaryControls(builder);
        builder.bindPlayerInventory(entityPlayer.inventory, 88);
        return builder.build(this.getHolder(), entityPlayer);
    }

    /** 标题、状态文本与进度条。 */
    private void buildApiaryHeader(ModularUI.Builder builder) {
        builder.widget(new LabelWidget(7, 5, "drtech.gui.industrial_apiary.title"));
        builder.widget(new DynamicLabelWidget(7, 80, this::getApiaryStatusText, 0x404040));
        builder.widget(new ProgressWidget(this::getProgressPercent, 86, 39, 20, 20,
                GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(this::addErrorText));
    }

    /** 蜂后与雄蜂槽位。 */
    private void buildBeeInventory(ModularUI.Builder builder) {
        builder.widget(new LabelWidget(8, 17, "drtech.gui.industrial_apiary.bees", 0x404040));
        builder.slot(inventoryBees, SLOT_QUEEN, 8, 28, true, true, Textures.BEE_QUEEN_LOGO);
        builder.slot(inventoryBees, SLOT_DRONE, 8, 50, true, true, Textures.BEE_DRONE_LOGO);
    }

    /** 升级槽位，两列排布。 */
    private void buildUpgradeInventory(ModularUI.Builder builder) {
        builder.widget(new LabelWidget(40, 17, "drtech.gui.industrial_apiary.upgrades", 0x404040));
        for (int i = 0; i < inventoryUpgrade.getSlots(); i++) {
            builder.slot(inventoryUpgrade, i, 40 + i % 2 * 18, 28 + i / 2 * 18, true, true, GuiTextures.SLOT);
        }
    }

    /** 输出槽位，三列排布，只出不进。 */
    private void buildOutputInventory(ModularUI.Builder builder) {
        builder.widget(new LabelWidget(118, 5, "drtech.gui.industrial_apiary.output", 0x404040));
        for (int i = 0; i < inventoryOutput.getSlots(); i++) {
            builder.slot(inventoryOutput, i, 116 + i % 3 * 18, 14 + i / 3 * 18, true, false, GuiTextures.SLOT);
        }
    }

    /** 暂停 / 停机按钮。 */
    private void buildApiaryControls(ModularUI.Builder builder) {
        builder.widget(new ApiaryActionButton(38, 66, 34, 14,
                "drtech.gui.industrial_apiary.pause",
                "drtech.gui.industrial_apiary.tooltip.1",
                "pause"));
        builder.widget(new ApiaryActionButton(76, 66, 34, 14,
                "drtech.gui.industrial_apiary.stop",
                "drtech.gui.industrial_apiary.tooltip.2",
                "stop"));
    }

    /** 状态栏文本，按停机 → 暂停 → 运行 → 等电 → 就绪的优先级取第一条命中的。 */
    private String getApiaryStatusText() {
        if (!isActive()) return localize("drtech.gui.industrial_apiary.status.stopped");
        if (!isWorkingEnabled()) return localize("drtech.gui.industrial_apiary.status.paused");
        if (isWorking()) return localize("drtech.gui.industrial_apiary.status.working", progressPer);
        if (hasPendingProcess()) return localize("drtech.gui.industrial_apiary.status.waiting_eu");
        return localize("drtech.gui.industrial_apiary.status.ready");
    }

    private String localize(String key, Object... args) {
        return new TextComponentTranslation(key, args).getFormattedText();
    }

    private void pauseApiary() {
        setWorkingEnabled(false);
        setProcessing(false);
    }

    private void resumeApiary() {
        setActive(true);
        setWorkingEnabled(true);
    }

    private void stopApiary() {
        cancelProcess(false);
        setWorkingEnabled(false);
    }

    private void togglePauseApiary() {
        if (isWorkingEnabled()) {
            pauseApiary();
        } else {
            resumeApiary();
        }
    }

    private void toggleStopApiary() {
        if (isActive()) {
            stopApiary();
        } else {
            resumeApiary();
        }
    }

    /** 处理客户端按钮发过来的动作，由 {@code UpdateTileEntityPacketHandler} 调用。 */
    public void handleApiaryClientAction(String action) {
        if ("pause".equals(action)) {
            togglePauseApiary();
        } else if ("stop".equals(action)) {
            toggleStopApiary();
        }
    }

    /** 进度条悬浮提示：耗电、环境温度湿度，以及当前全部错误状态。 */
    protected void addErrorText(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.1", this.mEUt));
        textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.2", getTemperature()));
        textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.3", getHumidity()));
        if (!hasErrors()) {
            textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.5"));
            return;
        }
        textList.add(new TextComponentTranslation("drtech.industrial_apiary.tootip.4"));
        mErrorStates.forEach(state -> textList.add(new TextComponentTranslation(state.getUnlocalizedDescription())));
    }

    /**
     * 自绘的方形按钮：客户端点击后只发包，动作由服务端执行，避免两端状态不一致。
     */
    private class ApiaryActionButton extends Widget {
        private final String labelKey;
        private final String tooltipKey;
        private final String action;

        private ApiaryActionButton(int x, int y, int width, int height, String labelKey, String tooltipKey, String action) {
            super(x, y, width, height);
            this.labelKey = labelKey;
            this.tooltipKey = tooltipKey;
            this.action = action;
        }

        @Override
        public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
            super.drawInBackground(mouseX, mouseY, partialTicks, context);
            final int x = getPosition().x;
            final int y = getPosition().y;
            final int width = getSize().width;
            final int height = getSize().height;
            final boolean hovered = isMouseOverElement(mouseX, mouseY);
            drawSolidRect(x, y, width, height, hovered ? 0xFF546477 : 0xFF3F4A5A);
            drawBorder(x, y, width, height, 1, hovered ? 0xFF8DEBFF : 0xFF6F7C8F);
            drawStringSized(localize(getDisplayLabelKey()), x + width / 2.0, y + 3.0, 0xFFFFFFFF, true, 0.75F, true);
        }

        /** 按钮文字随状态在"暂停/启动"、"停机/启动"之间切换。 */
        private String getDisplayLabelKey() {
            if ("pause".equals(action) && !isWorkingEnabled()) {
                return "drtech.gui.industrial_apiary.start";
            }
            if ("stop".equals(action) && !isActive()) {
                return "drtech.gui.industrial_apiary.start";
            }
            return labelKey;
        }

        @Override
        public void drawInForeground(int mouseX, int mouseY) {
            super.drawInForeground(mouseX, mouseY);
            if (isMouseOverElement(mouseX, mouseY)) {
                drawHoveringText(ItemStack.EMPTY, Collections.singletonList(localize(tooltipKey)), 300, mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
                final NBTTagCompound tag = new NBTTagCompound();
                tag.setString("industrialApiaryAction", action);
                SyncInit.NETWORK.sendToServer(new UpdateTileEntityPacket(MetaTileEntityIndustrialApiary.this.getPos(), tag));
                playButtonClickSound();
                return true;
            }
            return false;
        }
    }

    // ==================== 升级修正值 ====================

    /**
     * 重新汇总升级槽提供的修正值。
     *
     * <p>只在升级槽内容变更后真正执行，其余时候直接返回，所以可以放心在每个流程开始前调用。
     */
    public void updateModifiers() {
        if (!upgradeInventoryDirty) {
            return;
        }

        final GT_ApiaryModifier mods = new GT_ApiaryModifier();
        for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
            final ItemStack stack = inventoryUpgrade.getStackInSlot(UPGRADE_SLOT_START + i);
            if (stack.isEmpty()) {
                continue;
            }
            final GT_ApiaryUpgrade upgrade = GT_ApiaryUpgrade.getUpgrade(stack);
            if (upgrade != null) {
                upgrade.applyModifiers(mods, stack);
            }
        }

        terrorityMod = mods.territory;
        mutationMod = mods.mutation;
        lifespanMod = mods.lifespan;
        productionMod = mods.production;
        floweringMod = mods.flowering;
        geneticDecayMod = mods.geneticDecay;
        energyMod = mods.energy;
        sealedMod = mods.isSealed;
        selfLightedMod = mods.isSelfLighted;
        selfUnlightedMod = mods.isSelfUnlighted;
        sunlightSimulatedMod = mods.isSunlightSimulated;
        biomeOverride = mods.biomeOverride;
        humidityMod = mods.humidity;
        temperatureMod = mods.temperature;
        isAutomated = mods.isAutomated;
        isRetrievingPollen = mods.isCollectingPollen;

        // 锁定速度时直接跟随升级上限，否则只允许往下调
        mSpeed = mLockedSpeed ? mods.maxSpeed : Math.min(mSpeed, mods.maxSpeed);
        upgradeInventoryDirty = false;
    }

    /** 升级汇总出来的修正值，字段含义与 {@link GT_ApiaryUpgrade} 中的升级一一对应。 */
    public static class GT_ApiaryModifier {
        public float territory = 1f;
        public float mutation = 1f;
        public float lifespan = 1f;
        public float production = 2f;
        public float flowering = 1f;
        public float geneticDecay = 1f;
        public boolean isSealed = false;
        public boolean isSelfLighted = false;
        public boolean isSelfUnlighted = false;
        public boolean isSunlightSimulated = false;
        public boolean isAutomated = false;
        public boolean isCollectingPollen = false;
        public Biome biomeOverride = null;
        public float energy = 1f;
        public float temperature = 0f;
        public float humidity = 0f;
        public int maxSpeed = 0;
    }

    // ==================== 错误状态 ====================

    @Override
    public boolean setCondition(boolean b, IErrorState iErrorState) {
        final boolean changed = b ? mErrorStates.add(iErrorState) : mErrorStates.remove(iErrorState);
        if (changed) {
            errorStatesDirty = true;
        }
        return b;
    }

    @Override
    public boolean contains(IErrorState iErrorState) {
        return mErrorStates.contains(iErrorState);
    }

    @Override
    public boolean hasErrors() {
        return !mErrorStates.isEmpty();
    }

    @Override
    public void clearErrors() {
        if (!mErrorStates.isEmpty()) {
            mErrorStates.clear();
            errorStatesDirty = true;
        }
    }

    @Override
    public void writeData(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(mErrorStates.size());
        for (IErrorState state : mErrorStates) {
            packetBuffer.writeString(state.getUniqueName());
        }
    }

    @Override
    public void readData(PacketBuffer packetBuffer) {
        mErrorStates.clear();
        for (int i = packetBuffer.readInt(); i > 0; i--) {
            final IErrorState errorState = ForestryAPI.errorStateRegistry.getErrorState(packetBuffer.readString(32767));
            if (errorState != null) {
                mErrorStates.add(errorState);
            }
        }
        errorStatesDirty = false;
    }

    public ImmutableSet<IErrorState> getErrorStates() {
        return ImmutableSet.copyOf(mErrorStates);
    }

    /** 错误状态有变化时才发包，避免每 tick 都同步一遍。 */
    private void syncErrorStatesIfNeeded() {
        if (!getWorld().isRemote && errorStatesDirty) {
            errorStatesDirty = false;
            writeCustomData(DATA_ID_ERROR_STATES, this::writeData);
        }
    }

    /**
     * 校验当前蜜蜂能否在此蜂箱里工作，结果写入 {@link #mErrorStates}。
     *
     * @return 是否没有任何错误
     */
    private boolean applyBeeConditions(IBee bee) {
        if (bee == null) {
            // 槽位里的东西不是合法蜜蜂，按"没有蜂后"处理
            setCondition(true, EnumErrorCode.NO_QUEEN);
            syncErrorStatesIfNeeded();
            return false;
        }
        for (IErrorState err : bee.getCanWork(this)) {
            setCondition(true, err);
        }
        setCondition(!checkFlower(bee), EnumErrorCode.NO_FLOWER);
        // "内部黑暗"升级可以屏蔽夜晚限制
        if (contains(EnumErrorCode.NOT_NIGHT) && isSelfUnlightedMod()) {
            setCondition(false, EnumErrorCode.NOT_NIGHT);
        }
        syncErrorStatesIfNeeded();
        return !hasErrors();
    }

    /** 校验槽位里的蜂后能否继续工作；槽位为空或还是公主时视为可以。 */
    private boolean canWork(ItemStack queenStack) {
        clearErrors();
        if (queenStack == null || queenStack.isEmpty()) {
            syncErrorStatesIfNeeded();
            return true;
        }
        if (beeRoot.isMember(queenStack, EnumBeeType.PRINCESS)) {
            syncErrorStatesIfNeeded();
            return true;
        }
        return applyBeeConditions(beeRoot.getMember(queenStack));
    }

    /** 校验蜂后槽与雄蜂槽的组合能否开始新流程。 */
    private boolean canWork() {
        clearErrors();
        final ItemStack queenStack = getQueen();
        final EnumBeeType beeType = beeRoot.getType(queenStack);
        if (beeType == EnumBeeType.PRINCESS) {
            setCondition(!beeRoot.isDrone(getDrone()), EnumErrorCode.NO_DRONE);
            syncErrorStatesIfNeeded();
            return !hasErrors();
        }
        if (beeType == EnumBeeType.QUEEN) {
            return applyBeeConditions(beeRoot.getMember(queenStack));
        }
        setCondition(true, EnumErrorCode.NO_QUEEN);
        syncErrorStatesIfNeeded();
        return false;
    }

    /**
     * 在当前范围内找一朵该品种接受的花，结果缓存在 {@link #flowercoords}。
     *
     * <p>缓存的花会重新校验一次，被挖掉或换掉后会重新搜索；换品种也会让缓存失效。
     *
     * @return 是否找到了可用的花
     */
    private boolean checkFlower(IBee bee) {
        final World world = getWorld();
        final String currentFlowerType = bee.getGenome().getFlowerProvider().getFlowerType();
        if (!this.flowerType.equals(currentFlowerType)) {
            flowercoords = null;
        }

        if (flowercoords != null) {
            final IBlockState state = world.getBlockState(flowercoords);
            final Block block = state.getBlock();
            final int meta = block.getMetaFromState(state);
            if (block != flowerBlock || meta != flowerBlockMeta) {
                if (!FlowerManager.flowerRegistry.isAcceptedFlower(currentFlowerType, world, flowercoords)) {
                    flowercoords = null;
                } else {
                    flowerBlock = block;
                    flowerBlockMeta = meta;
                }
            }
        }

        if (flowercoords == null) {
            final float territory = getTerritoryModifier(null, 1f)
                    * BeeManager.beeRoot.getBeekeepingMode(world).getBeeModifier().getTerritoryModifier(null, 1f);
            final List<BlockPos> acceptedFlowers = FlowerManager.flowerRegistry
                    .getAcceptedFlowerCoordinates(this, bee, currentFlowerType, (int) territory);
            if (!acceptedFlowers.isEmpty()) {
                flowercoords = acceptedFlowers.get(0);
                final IBlockState state = world.getBlockState(flowercoords);
                flowerBlock = state.getBlock();
                flowerBlockMeta = flowerBlock.getMetaFromState(state);
                this.flowerType = currentFlowerType;
            }
        }
        return flowercoords != null;
    }

    // ==================== 蜜蜂效果 ====================

    /** 每 tick 施加一次蜜蜂效果；不可叠加的效果直接跳过。 */
    private void doEffect() {
        if (usedQueenBee == null) {
            return;
        }
        final IBeeGenome genome = usedQueenBee.getGenome();
        final IAlleleBeeEffect effect = genome.getEffect();
        if (!effect.isCombinable()) {
            return;
        }
        effectData[0] = effect.validateStorage(effectData[0]);
        effect.doEffect(genome, effectData[0], this);

        // 隐性效果能叠加时也一并生效
        final IAlleleBeeEffect secondary = (IAlleleBeeEffect) genome.getInactiveAllele(EnumBeeChromosome.EFFECT);
        if (!secondary.isCombinable()) {
            return;
        }
        effectData[1] = effect.validateStorage(effectData[1]);
        secondary.doEffect(genome, effectData[1], this);
    }

    /**
     * 流程结束时补放一次加速后的效果。
     *
     * <p>这里不使用 {@code isCombinable} 判定，因为效果已经积累了一整个流程，
     * 需要保证和过程外的表现一致；单个效果出错也不能影响流程本身的结算。
     */
    private void doAcceleratedEffects() {
        if (usedQueenBee == null) {
            return;
        }
        final IBeeGenome genome = usedQueenBee.getGenome();
        final IAlleleBeeEffect effect = genome.getEffect();
        try {
            effectData[0] = effect.doEffect(genome, effectData[0], this);
            if (!effect.isCombinable()) {
                return;
            }
            final IAlleleBeeEffect secondary = (IAlleleBeeEffect) genome.getInactiveAllele(EnumBeeChromosome.EFFECT);
            effectData[1] = secondary.doEffect(genome, effectData[1], this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** 产出概率的调参权重，改动会直接影响所有产物的产出速率。 */
    private static final float CHANCE_MODIFIER_WEIGHT = 8f;

    /**
     * 由物种自带概率推算本机实际的单代产出概率。
     *
     * <p>三部分相加：概率权重带来的线性加成、基础概率开方后的速度缩放，
     * 以及产量修正作为指数对基础概率的对数级放大；最后减 3 抵消基数。
     *
     * @param baseChance 物种自带的出现概率
     * @param speed      基因组速度
     * @param prodMod    产量修正
     */
    private float getFinalChance(float baseChance, float speed, float prodMod) {
        final double chance = (1 + CHANCE_MODIFIER_WEIGHT / 6)
                * Math.pow(baseChance, 0.5) * 2f * (1 + speed)
                + Math.pow(prodMod, Math.pow(baseChance, 1.0 / 3.0)) - 3;
        return (float) chance;
    }

    // ==================== IBeeHousing / IBeeHousingInventory ====================

    @Override
    public Iterable<IBeeModifier> getBeeModifiers() {
        return Collections.singletonList(this);
    }

    @Override
    public Iterable<IBeeListener> getBeeListeners() {
        return Collections.singletonList(this);
    }

    @Override
    public IBeeHousingInventory getBeeInventory() {
        return this;
    }

    @Override
    public IBeekeepingLogic getBeekeepingLogic() {
        return DUMMY_BEEKEEPING_LOGIC;
    }

    @Override
    public ItemStack getQueen() {
        return inventoryBees.getStackInSlot(SLOT_QUEEN);
    }

    @Override
    public ItemStack getDrone() {
        return inventoryBees.getStackInSlot(SLOT_DRONE);
    }

    @Override
    public void setQueen(ItemStack itemStack) {
        setBeeStack(SLOT_QUEEN, itemStack);
    }

    @Override
    public void setDrone(ItemStack itemStack) {
        setBeeStack(SLOT_DRONE, itemStack);
    }

    /** 清空或写入某个蜜蜂槽位；写入前先做一次模拟插入，放不下就原样保留。 */
    private void setBeeStack(int slot, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            final ItemStack existing = inventoryBees.getStackInSlot(slot);
            if (!existing.isEmpty()) {
                inventoryBees.extractItem(slot, existing.getCount(), false);
            }
            return;
        }
        if (inventoryBees.insertItem(slot, itemStack, true).isEmpty()) {
            inventoryBees.insertItem(slot, itemStack, false);
        }
    }

    @Override
    public Vec3d getBeeFXCoordinates() {
        return new Vec3d(getPos().getX(), getPos().getY(), getPos().getZ());
    }

    @Override
    public boolean addProduct(ItemStack itemStack, boolean b) {
        // 产物统一走 mOutputItems 结算，林业不应该直接往这里塞东西
        throw new RuntimeException("Should not happen :F");
    }

    @Override
    public void wearOutEquipment(int i) {
    }

    @Override
    public void onQueenDeath() {
    }

    @Override
    public boolean onPollenRetrieved(IIndividual iIndividual) {
        return false;
    }

    /** 工业蜂箱自己在内部结算花粉，所以永远不需要林业代劳。 */
    private static final IBeekeepingLogic DUMMY_BEEKEEPING_LOGIC = new IBeekeepingLogic() {

        @Override
        public boolean canWork() {
            return true;
        }

        @Override
        public void doWork() {
        }

        @Override
        public void clearCachedValues() {
        }

        @Override
        public void syncToClient() {
        }

        @Override
        public void syncToClient(EntityPlayerMP entityPlayerMP) {
        }

        @Override
        public int getBeeProgressPercent() {
            return 0;
        }

        @Override
        public boolean canDoBeeFX() {
            return false;
        }

        @Override
        public void doBeeFX() {
        }

        @Override
        public List<BlockPos> getFlowerPositions() {
            return new ArrayList<>();
        }

        @Override
        public void readData(PacketBuffer data) throws IOException {
            IBeekeepingLogic.super.readData(data);
        }

        @Override
        public void writeData(PacketBuffer data) {
            IBeekeepingLogic.super.writeData(data);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbtTagCompound) {
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
            return null;
        }
    };

    // ==================== IBeeModifier ====================

    @Override
    public float getTerritoryModifier(IBeeGenome iBeeGenome, float v) {
        return Math.min(5, terrorityMod);
    }

    @Override
    public float getMutationModifier(IBeeGenome iBeeGenome, IBeeGenome iBeeGenome1, float v) {
        return mutationMod;
    }

    @Override
    public float getLifespanModifier(IBeeGenome iBeeGenome, @Nullable IBeeGenome iBeeGenome1, float v) {
        return lifespanMod;
    }

    @Override
    public float getProductionModifier(IBeeGenome iBeeGenome, float v) {
        return productionMod;
    }

    @Override
    public float getFloweringModifier(IBeeGenome iBeeGenome, float v) {
        return floweringMod;
    }

    @Override
    public float getGeneticDecay(IBeeGenome iBeeGenome, float v) {
        return geneticDecayMod;
    }

    @Override
    public boolean isSealed() {
        return sealedMod;
    }

    @Override
    public boolean isSelfLighted() {
        if (selfUnlightedMod) {
            return false;
        }
        return selfLightedMod;
    }

    @Override
    public boolean isSunlightSimulated() {
        return sunlightSimulatedMod;
    }

    /** "内部黑暗"升级是否生效，温度和光照相关逻辑都会读它。 */
    private boolean isSelfUnlightedMod() {
        return selfUnlightedMod;
    }

    @Override
    public boolean isHellish() {
        return getBiome() == Biomes.HELL;
    }

    @Override
    public Biome getBiome() {
        if (biomeOverride != null) {
            return biomeOverride;
        }
        return getWorld().getBiome(this.getPos());
    }

    @Override
    public EnumTemperature getTemperature() {
        if (BiomeHelper.isBiomeHellish(getBiome())) {
            return EnumTemperature.HELLISH;
        }
        return EnumTemperature.getFromValue(getBiome().getTemperature(getPos()) + temperatureMod);
    }

    @Override
    public EnumHumidity getHumidity() {
        return EnumHumidity.getFromValue(getBiome().getRainfall() + humidityMod);
    }

    @Override
    public int getBlockLightValue() {
        if (selfUnlightedMod) {
            return 0;
        }
        return getLightValue();
    }

    @Override
    public int getActualLightValue() {
        if (selfUnlightedMod) {
            return 0;
        }
        return getWorld().getLightFromNeighbors(getPos().up());
    }

    @Override
    public boolean canBlockSeeTheSky() {
        return getWorld().canBlockSeeSky(getPos().add(0, 2, 0));
    }

    @Override
    public boolean isRaining() {
        return this.getWorld().isRainingAt(getPos().add(0, 2, 0));
    }

    // ==================== IErrorLogic 以外的蜂箱接口 ====================

    @Override
    public IErrorLogic getErrorLogic() {
        return this;
    }

    @Override
    public World getWorldObj() {
        return getWorld();
    }

    @Override
    public BlockPos getCoordinates() {
        return getPos();
    }
}
