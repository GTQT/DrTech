package com.drppp.drtech.Tile;


import com.drppp.drtech.common.Blocks.Crops.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public class TileCropStick extends TileEntity implements ITickable {

    public static final int GROWTH_CYCLE = 256;

    private String cropId = "";
    private CropStats stats = new CropStats();
    private int growthStage = 0;
    private int growthProgress = 0;
    private boolean doubleCropStick = false;
    private int tickCounter = 0;
    private boolean isWeed = false;

    /**
     * 延迟同步标记:
     * update()内部产生的所有状态变化只设此标记为true + markDirty(),
     * 绝不调用notifyBlockUpdate/markBlockRangeForRenderUpdate。
     * 实际同步在下一个正常tick开头执行。
     *
     * 这彻底切断了WorldAccelerator的递归链:
     * WorldAccelerator.handleTEMode → TileCropStick.update()
     * → 只修改字段+markDirty → 不触发任何方块更新 → 不会让WorldAccelerator重新扫描
     */
    private boolean pendingSync = false;
    
    @Override
    public void update() {
        if (world == null || world.isRemote) return;

        // === 每个tick开头: 处理挂起的同步 ===
        // 此时是正常游戏tick流程, 不在WorldAccelerator递归路径中
        if (pendingSync) {
            pendingSync = false;
            doSyncToClient();
        }

        // === 生长轮判定 ===
        tickCounter++;
        if (tickCounter % GROWTH_CYCLE != 0) return;

        if (hasCrop()) {
            if (isWeed) tickWeedGrowth();
            else tickCropGrowth();
        } else if (doubleCropStick) {
            tickCrossBreeding();
        } else {
            tickEmptyStickWeed();
        }
    }

    // ==================== 作物生长(含环境验证和修正) ====================

    private void tickCropGrowth() {
        CropType type = getCropType();
        if (type == null || growthStage >= type.getMaxGrowthStage()) return;

        float light = EnvironmentCalculator.calcLight(world, pos);
        float humidity = EnvironmentCalculator.calcHumidity(world, pos);
        String blockBelow = EnvironmentCalculator.getBlockBelowId(world, pos);

        if (!type.canGrowAt(light * 15, humidity, blockBelow)) return;

        float envScore = EnvironmentCalculator.calcEnvironmentScore(world, pos);
        int baseIncrement = stats.rollGrowthIncrement(world.rand);
        float envMultiplier = 0.3f + envScore * 1.2f;
        int actualIncrement = Math.max(1, Math.round(baseIncrement * envMultiplier));

        growthProgress += actualIncrement;

        int requirement = type.getStageRequirement();
        if (growthProgress >= requirement) {
            growthProgress = 0;
            growthStage++;
            markDirtyAndScheduleSync();
        } else {
            markDirty();
        }
    }

    // ==================== 杂草 ====================

    private void tickEmptyStickWeed() {
        if (world.rand.nextInt(100) < 10) {
            this.cropId = "weed";
            this.stats = new CropStats(world.rand.nextInt(8) + 3, world.rand.nextInt(3) + 1, world.rand.nextInt(3) + 1);
            this.growthStage = 0;
            this.growthProgress = 0;
            this.isWeed = true;
            markDirtyAndScheduleSync();
        }
    }

    private void tickWeedGrowth() {
        CropType weedType = CropRegistry.get("weed");
        int maxStage = weedType != null ? weedType.getMaxGrowthStage() : 5;
        int requirement = weedType != null ? weedType.getStageRequirement() : 12;

        if (growthStage < maxStage) {
            int increment = stats.rollGrowthIncrement(world.rand);
            growthProgress += increment;
            if (growthProgress >= requirement) {
                growthProgress = 0;
                growthStage++;
                markDirtyAndScheduleSync();
            }
        }

        if (growthStage >= maxStage) spreadWeed();
    }

    private void spreadWeed() {
        BlockPos[] neighbors = {pos.north(), pos.south(), pos.east(), pos.west()};
        for (BlockPos nPos : neighbors) {
            TileEntity te = world.getTileEntity(nPos);
            if (te instanceof TileCropStick) {
                TileCropStick neighbor = (TileCropStick) te;
                if (neighbor.hasCrop() && !neighbor.isWeed) {
                    if (!neighbor.stats.tryResistWeed(world.rand)) {
                        neighbor.convertToWeed();
                    }
                } else if (!neighbor.hasCrop()) {
                    if (world.rand.nextInt(3) == 0) {
                        neighbor.cropId = "weed";
                        neighbor.stats = new CropStats(stats.getGrowth(), 1, 1);
                        neighbor.isWeed = true;
                        neighbor.growthStage = 0;
                        neighbor.growthProgress = 0;
                        neighbor.markDirtyAndScheduleSync();
                    }
                }
            } else {
                IBlockState belowState = world.getBlockState(nPos.down());
                if (belowState.getBlock() == Blocks.FARMLAND && world.rand.nextInt(5) == 0) {
                    // flag=2: 发送给客户端但不触发邻居方块更新
                    world.setBlockState(nPos.down(), Blocks.GRASS.getDefaultState(), 2);
                }
            }
        }
    }

    private void convertToWeed() {
        this.cropId = "weed";
        this.isWeed = true;
        this.growthStage = 0;
        this.growthProgress = 0;
        markDirtyAndScheduleSync();
    }

    // ==================== 杂交 ====================

    private void tickCrossBreeding() {
        List<TileCropStick> allMature = new ArrayList<>();
        BlockPos[] neighbors = {pos.north(), pos.south(), pos.east(), pos.west()};
        for (BlockPos nPos : neighbors) {
            TileEntity te = world.getTileEntity(nPos);
            if (te instanceof TileCropStick) {
                TileCropStick n = (TileCropStick) te;
                if (n.hasCrop() && n.isMature() && !n.isWeed) allMature.add(n);
            }
        }
        if (allMature.size() < 2) return;

        List<TileCropStick> participants = new ArrayList<>();
        for (TileCropStick n : allMature) {
            if (world.rand.nextInt(100) < n.stats.getCrossBreedChance()) participants.add(n);
        }
        if (participants.size() < 2) return;

        String result = determineCrossResult(participants);
        if (result == null) return;

        List<CropStats> allStats = new ArrayList<>();
        for (BlockPos nPos : neighbors) {
            TileEntity te = world.getTileEntity(nPos);
            if (te instanceof TileCropStick) {
                TileCropStick n = (TileCropStick) te;
                if (n.hasCrop() && !n.isWeed) allStats.add(n.stats);
            }
        }

        this.cropId = result;
        this.stats = CropStats.inheritFrom(allStats.toArray(new CropStats[0]), world.rand);
        this.growthStage = 0;
        this.growthProgress = 0;
        this.doubleCropStick = false;
        this.isWeed = result.equals("weed");
        markDirtyAndScheduleSync();
    }

    private String determineCrossResult(List<TileCropStick> participants) {
        String parentA = participants.get(0).cropId;
        String parentB = participants.get(1).cropId;

        Map<String, Integer> crossProducts = new HashMap<>();
        for (int i = 0; i < participants.size(); i++)
            for (int j = i + 1; j < participants.size(); j++) {
                Map<String, Integer> p = CrossBreedingRegistry.getProducts(
                        participants.get(i).cropId, participants.get(j).cropId);
                for (Map.Entry<String, Integer> e : p.entrySet())
                    crossProducts.merge(e.getKey(), e.getValue(), Integer::sum);
            }

        Map<String, Integer> wt = new LinkedHashMap<>();
        wt.put(parentA, 40);
        if (!parentB.equals(parentA)) wt.put(parentB, 40);
        else wt.merge(parentA, 40, Integer::sum);

        if (!crossProducts.isEmpty()) {
            int total = crossProducts.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<String, Integer> e : crossProducts.entrySet())
                wt.merge(e.getKey(), Math.max(1, 20 * e.getValue() / total), Integer::sum);
        }
        wt.merge("weed", 5, Integer::sum);

        int maxTier = participants.stream()
                .map(t -> CropRegistry.get(t.cropId))
                .filter(Objects::nonNull)
                .mapToInt(CropType::getTier)
                .max().orElse(1);

        int totalW = wt.values().stream().mapToInt(Integer::intValue).sum();
        int roll = world.rand.nextInt(totalW);
        for (Map.Entry<String, Integer> e : wt.entrySet()) {
            roll -= e.getValue();
            if (roll < 0) {
                CropType rt = CropRegistry.get(e.getKey());
                if (rt != null && rt.getTier() > maxTier + 1) continue;
                return e.getKey();
            }
        }
        return parentA;
    }

    // ==================== 外部接口(玩家交互, 立即同步) ====================

    public void plantCrop(String cropId, CropStats stats) {
        this.cropId = cropId;
        this.stats = stats;
        this.growthStage = 0;
        this.growthProgress = 0;
        this.isWeed = false;
        this.doubleCropStick = false;
        immediateSync();
    }

    public boolean harvest() {
        if (!hasCrop() || !isMature()) return false;
        this.growthStage = 0;
        this.growthProgress = 0;
        immediateSync();
        return true;
    }

    public void destroyCrop() {
        this.cropId = "";
        this.stats = new CropStats();
        this.growthStage = 0;
        this.growthProgress = 0;
        this.isWeed = false;
        immediateSync();
    }

    public void setDoubleCropStick(boolean v) {
        this.doubleCropStick = v;
        immediateSync();
    }

    // ==================== 查询 ====================

    public boolean hasCrop() { return cropId != null && !cropId.isEmpty(); }
    public boolean isMature() { CropType t = getCropType(); return t != null && growthStage >= t.getHarvestStage(); }
    public boolean isDoubleCropStick() { return doubleCropStick; }
    public String getCropId() { return cropId; }
    public CropStats getStats() { return stats; }
    public int getGrowthStage() { return growthStage; }
    public int getGrowthProgress() { return growthProgress; }
    public boolean isWeedPlant() { return isWeed; }

    @Nullable
    public CropType getCropType() {
        return (cropId == null || cropId.isEmpty()) ? null : CropRegistry.get(cropId);
    }

    // ==================== NBT ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound n) {
        super.writeToNBT(n);
        n.setString("cropId", cropId != null ? cropId : "");
        n.setInteger("growthStage", growthStage);
        n.setInteger("growthProgress", growthProgress);
        n.setBoolean("doubleCropStick", doubleCropStick);
        n.setBoolean("isWeed", isWeed);
        n.setInteger("tickCounter", tickCounter);
        n.setBoolean("pendingSync", pendingSync);
        if (stats != null) stats.writeToNBT(n);
        return n;
    }

    @Override
    public void readFromNBT(NBTTagCompound n) {
        super.readFromNBT(n);
        cropId = n.getString("cropId");
        growthStage = n.getInteger("growthStage");
        growthProgress = n.getInteger("growthProgress");
        doubleCropStick = n.getBoolean("doubleCropStick");
        isWeed = n.getBoolean("isWeed");
        tickCounter = n.getInteger("tickCounter");
        pendingSync = n.getBoolean("pendingSync");
        stats = CropStats.readFromNBT(n);
    }

    // ==================== 同步 ====================

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
        if (world != null && world.isRemote) {
            IBlockState s = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, s, s, 3);
        }
    }

    /**
     * update()内部专用: 只标记脏+设置pendingSync
     * 绝不触发任何方块更新通知, 彻底避免WorldAccelerator递归
     */
    private void markDirtyAndScheduleSync() {
        markDirty();
        pendingSync = true;
    }

    /**
     * 外部接口专用(plantCrop/harvest等玩家交互):
     * 不在WorldAccelerator调用链中, 可安全地立即同步
     */
    private void immediateSync() {
        markDirty();
        pendingSync = false;
        doSyncToClient();
    }

    /**
     * 实际执行客户端同步
     */
    private void doSyncToClient() {
        if (world != null && !world.isRemote) {
            IBlockState s = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, s, s, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }
}
