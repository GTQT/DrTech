package com.meowmel.cropQT.api.capability.impl;

import com.meowmel.cropQT.machine.MetaTileEntityCropRangeMachine;
import com.meowmel.cropQT.tile.TileCropStick;
import gregtech.api.GTValues;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 「范围机器」的公共逻辑：圈定一片区域，每 {@link #WORK_CYCLE} tick 过一遍里面的作物架。
 *
 * <p>作物监管机（浇水/施肥/除草）和作物收割机都建立在这上面 —— 两台的区别只在
 * {@link #runCycle()} 里干什么、以及 {@link #isAnyEnabled()} 怎么算。
 *
 * <h2>为什么缓存</h2>
 * 全量扫一遍长方体是个三方循环。LV 半径 4 是 {@code 9×9×5 = 405} 格，
 * IV 半径 36 是 {@code 73×73×5 = 26645} 格 —— 每 50 tick 扫一次太贵。
 * 所以缓存一份作物架列表，隔一段时间重扫：空场 100 tick 一次（场地随时可能种上），
 * 有作物时 600 tick（30 秒）一次。
 *
 * <p>代价是新放下的作物架最多要等 30 秒才被认到。缓存里的条目用之前都要查
 * {@code isInvalid()}，被拆掉或区块卸载的直接跳过。
 *
 * <h2>进度</h2>
 * {@code progress} 是「这一轮跑到第几 tick」，断电时保留、来电接着跑。
 * 它只是给界面进度条看的，不影响动作的判定。
 *
 * @param <M> 宿主机器类型
 */
public abstract class CropRangeLogic<M extends MetaTileEntityCropRangeMachine> {

    /** 一轮的周期（tick）。 */
    public static final int WORK_CYCLE = 50;

    /** 垂直半径固定为 2（上下各两格）。 */
    private static final int VERTICAL_RADIUS = 2;

    /** 缓存里一个作物架都没有时的刷新间隔（tick），有作物时用较长的间隔。 */
    private static final int CACHE_REFRESH_EMPTY = 100;
    private static final int CACHE_REFRESH_ANY = 600;

    @NotNull
    protected final M machine;

    /** 缓存的作物架。不存档——它只是当前世界的快照。 */
    private final List<TileCropStick> cropCache = new ArrayList<>();
    /** 下次刷新缓存的时间点。 */
    private long nextCacheRefresh = 0L;

    /** 当前周期已跑的 tick 数。断电时保留，来电接着跑。 */
    private int progress = 0;

    protected CropRangeLogic(@NotNull M machine) {
        this.machine = machine;
    }

    // ==================== 子类回答的问题 ====================

    /** 有没有开着至少一个动作。全关着就完全不动，连扫描都不扫。 */
    public abstract boolean isAnyEnabled();

    /**
     * 对单个作物架干活，每轮每个架子调一次。
     *
     * <p>遍历与失效检查由基类做，子类只管「这个架子上该做什么」。
     */
    protected abstract void actOn(TileCropStick crop);

    // ==================== 状态机 ====================

    /** 每 tick 调一次；只在服务端调。 */
    public void update() {
        if (!isAnyEnabled()) {
            progress = 0;
            return;
        }
        if (++progress < WORK_CYCLE) {
            return;
        }
        progress = 0;
        refreshCacheIfDue();
        for (TileCropStick crop : cropCache) {
            // 缓存要活到下一次刷新，期间方块可能被拆掉或区块卸载——失效的直接跳过
            if (crop.isInvalid() || crop.getWorld() == null) {
                continue;
            }
            actOn(crop);
        }
    }

    /** 进度百分比（0~1），给界面进度条用。 */
    public double getProgressPercent() {
        return isAnyEnabled() ? (double) progress / WORK_CYCLE : 0.0;
    }

    /** 扣电；不够就返回 false 且不扣。 */
    protected boolean consumeEnergy(long amount) {
        if (amount <= 0) {
            return true;
        }
        if (machine.getEnergyContainer().getEnergyStored() < amount) {
            return false;
        }
        machine.getEnergyContainer().removeEnergy(amount);
        return true;
    }

    /** 一次动作的耗电 = 本机电压 / 这个除数。 */
    protected long energyPerAction(int divisor) {
        return Math.max(1, GTValues.V[machine.getTier()] / divisor);
    }

    /** 当前扫到的作物架。子类干活时用。 */
    @NotNull
    protected List<TileCropStick> getCropCache() {
        return cropCache;
    }

    // ==================== 存档 ====================

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    // ==================== 扫描 ====================

    private void refreshCacheIfDue() {
        long timer = machine.getOffsetTimer();
        if (timer < nextCacheRefresh) {
            return;
        }
        nextCacheRefresh = timer + (cropCache.isEmpty() ? CACHE_REFRESH_EMPTY : CACHE_REFRESH_ANY);

        cropCache.clear();
        BlockPos origin = machine.getPos();
        int radius = machine.getRange();
        for (int dy = -VERTICAL_RADIUS; dy <= VERTICAL_RADIUS; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    TileEntity tile = machine.getWorld().getTileEntity(origin.add(dx, dy, dz));
                    if (tile instanceof TileCropStick crop && !crop.isInvalid()) {
                        cropCache.add(crop);
                    }
                }
            }
        }
    }
}
