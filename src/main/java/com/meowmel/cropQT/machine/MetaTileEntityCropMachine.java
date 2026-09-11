package com.meowmel.cropQT.machine;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

/**
 * 作物系统 5 台机器的公共基类。
 *
 * <h2>为什么不继承 {@code SimpleMachineMetaTileEntity}</h2>
 * 它的 handler 尺寸、{@code buildUI}、{@code getSound}、{@code addInformation} 全都硬解引用
 * {@code workable.getRecipeMap()}。本系统的机器不走 RecipeMap（配方逻辑是动态的、逐条查作物表），
 * 传 null 会在打开 UI 时直接抛异常。
 *
 * <p>GTQT 里「完全自定义逻辑的单方块机器」的既有做法是直接继承 {@link TieredMetaTileEntity}
 * （参考 {@code MetaTileEntityBlockBreaker} / {@code MetaTileEntityPump} / {@code MetaTileEntityFisher}），
 * 本项目也已有多台机器这么写。
 *
 * <h2>基类负责什么</h2>
 * <ul>
 *     <li>「能不能开工 → 扣电推进 → 跑完结算」这套单轮状态机</li>
 *     <li>进度同步到客户端（MUI2 进度条要读）</li>
 *     <li>进度存档</li>
 *     <li>面板上进度条的同步值</li>
 * </ul>
 *
 * <p>子类只要回答四个问题：现在能开工吗、一轮跑多久、每 tick 耗多少电、跑完做什么。
 * 槽位 / 罐的布局各机器不同，由子类自己覆写 {@code createXxxHandler} 与 {@code buildUI}。
 */
public abstract class MetaTileEntityCropMachine extends TieredMetaTileEntity {

    /** 进度同步用的网络数据 id。同一台机器只有一个进度，各机器共用一个 id 即可。 */
    private static final int DATA_ID_PROGRESS = 4800;
    /** 同步值的名字。 */
    private static final String SYNC_PROGRESS = "cropqt_progress";

    /** 当前轮已跑的 tick 数。断电时保留，来电接着跑。 */
    private int progress = 0;
    /** 当前轮的总时长（tick）；0 表示没有进行中的轮次。 */
    private int maxProgress = 0;

    /** 上次同步出去的进度，用来避免每 tick 都发包。 */
    private int lastSyncedProgress = Integer.MIN_VALUE;
    private int lastSyncedMaxProgress = Integer.MIN_VALUE;

    protected MetaTileEntityCropMachine(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        // 子类的槽位/罐字段要等 super() 跑完才就绪，所以这里再初始化一次
        initializeInventory();
    }

    // ==================== 子类回答的四个问题 ====================

    /**
     * 现在能不能开工。
     *
     * <p>开工前和每轮收尾时各问一次——后者是为了在「产物放不下」时停在原地等，
     * 而不是把产物挤掉。
     */
    protected abstract boolean canStart();

    /** 这一轮要跑多少 tick。必须为正数。 */
    protected abstract int getWorkDuration();

    /** 本轮每 tick 消耗多少 EU。 */
    protected abstract long getEnergyPerTick();

    /** 一轮跑完的结算：扣材料、出产物。 */
    protected abstract void onWorkComplete();

    // ==================== 状态机 ====================

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }
        tickWork();
        syncProgressIfNeeded();
    }

    private void tickWork() {
        if (maxProgress <= 0) {
            if (!canStart()) {
                return;
            }
            int duration = getWorkDuration();
            if (duration <= 0) {
                return;
            }
            maxProgress = duration;
            progress = 0;
            markDirty();
        }

        long energyPerTick = getEnergyPerTick();
        if (energyContainer.getEnergyStored() < energyPerTick) {
            // 断电：进度保留，来电接着跑，不重置
            return;
        }
        energyContainer.removeEnergy(energyPerTick);

        if (++progress < maxProgress) {
            return;
        }

        // 收尾前再确认一次条件，放不下就停在最后一刻等
        if (!canStart()) {
            progress = maxProgress;
            return;
        }
        progress = 0;
        maxProgress = 0;
        onWorkComplete();
        markDirty();
    }

    /** 进度百分比（0~1），供界面进度条使用。 */
    public double getProgressPercent() {
        return maxProgress <= 0 ? 0.0 : (double) progress / (double) maxProgress;
    }

    /** 进度是否在推进。渲染与状态文本用。 */
    public boolean isWorking() {
        return maxProgress > 0;
    }

    // ==================== 同步 ====================

    private void syncProgressIfNeeded() {
        if (lastSyncedProgress == progress && lastSyncedMaxProgress == maxProgress) {
            return;
        }
        lastSyncedProgress = progress;
        lastSyncedMaxProgress = maxProgress;
        writeCustomData(DATA_ID_PROGRESS, buf -> {
            buf.writeInt(progress);
            buf.writeInt(maxProgress);
        });
        // 进度影响正面贴图的运行态
        scheduleRenderUpdate();
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(progress);
        buf.writeInt(maxProgress);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.progress = buf.readInt();
        this.maxProgress = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == DATA_ID_PROGRESS) {
            this.progress = buf.readInt();
            this.maxProgress = buf.readInt();
            scheduleRenderUpdate();
        }
    }

    /**
     * 注册进度同步值，供面板里的进度条绑定。
     *
     * <p>子类在 {@code buildUI} 里调一次，把返回值交给 {@code ProgressWidget.value(...)}。
     */
    protected DoubleSyncValue registerProgressSync(PanelSyncManager syncManager) {
        DoubleSyncValue value = new DoubleSyncValue(this::getProgressPercent);
        syncManager.syncValue(SYNC_PROGRESS, value);
        return value;
    }

    // ==================== 存档 ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("CropProgress", progress);
        data.setInteger("CropMaxProgress", maxProgress);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        progress = data.getInteger("CropProgress");
        maxProgress = data.getInteger("CropMaxProgress");
    }

    // ==================== 展示 ====================

    /** 作物机器泡水不炸——它们本来就是温室里的东西。 */
    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    /** 有进度在推就算「运行中」，正面贴图会切到运行态。 */
    @Override
    public boolean isActive() {
        return isWorking();
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (getOverlayRenderer() == null)
            return;
        this.getOverlayRenderer().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), isWorking(),  isWorking());
    }

    public OrientedOverlayRenderer getOverlayRenderer() {
        return null;
    }
}
