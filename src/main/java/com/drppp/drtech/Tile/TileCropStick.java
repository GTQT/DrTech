package com.drppp.drtech.Tile;


import com.drppp.drtech.common.Blocks.Crops.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileCropStick extends TileEntity implements ITickable {

    private String cropId = "";
    private CropStats stats = new CropStats();
    private int growthStage = 0;
    private boolean doubleCropStick = false;
    private int tickCounter = 0;
    private boolean isWeed = false;

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        tickCounter++;
        if (tickCounter % 20 != 0) return;

        if (hasCrop()) {
            updateWeedCheck();
        } else if (doubleCropStick) {
            updateCrossBreeding();
        }
    }
    //随机刻控制成长
    public void onBlockRandomTick()
    {
        if (hasCrop())
        {
            updateGrowth();
        }
    }
    private void updateGrowth() {
        CropType type = getCropType();
        if (type == null || growthStage >= type.getMaxGrowthStage()) return;

        float envScore = EnvironmentCalculator.calcEnvironmentScore(world, pos);
        int growthInterval = EnvironmentCalculator.calcGrowthTicks(envScore, stats, type.getTier());
        int interval = Math.max(1, growthInterval / 20);

        if (tickCounter % interval == 0) {
            float light = EnvironmentCalculator.calcLight(world, pos);
            float humidity = EnvironmentCalculator.calcHumidity(world, pos);
            String blockBelow = EnvironmentCalculator.getBlockBelowId(world, pos);

            if (type.canGrowAt(light * 15, humidity, blockBelow)) {
                growthStage++;
                syncToClient();
            }
        }
    }

    private void updateWeedCheck() {
        if (isWeed) {
            if (tickCounter % 100 == 0) spreadWeed();
            return;
        }
        if (stats.hasWeedRisk(world.rand)) {
            convertToWeed();
        }
    }

    private void convertToWeed() {
        this.cropId = "weed";
        this.isWeed = true;
        this.growthStage = 0;
        syncToClient();
    }

    private void spreadWeed() {
        BlockPos[] neighbors = {pos.north(), pos.south(), pos.east(), pos.west()};
        for (BlockPos nPos : neighbors) {
            TileEntity te = world.getTileEntity(nPos);
            if (!(te instanceof TileCropStick)) continue;
            TileCropStick neighbor = (TileCropStick) te;
            if (neighbor.hasCrop() && !neighbor.isWeed) {
                if (!neighbor.stats.resistWeedSpread(world.rand)) {
                    neighbor.convertToWeed();
                }
            } else if (!neighbor.hasCrop() && neighbor.doubleCropStick) {
                if (world.rand.nextInt(3) == 0) {
                    neighbor.cropId = "weed";
                    neighbor.stats = new CropStats(stats.getGrowth(), 1, 1);
                    neighbor.isWeed = true;
                    neighbor.growthStage = 0;
                    neighbor.syncToClient();
                }
            }
        }
    }

    private void updateCrossBreeding() {
        if (tickCounter % 100 != 0) return;

        List<String> parentCrops = new ArrayList<>();
        List<CropStats> parentStats = new ArrayList<>();

        BlockPos[] neighbors = {pos.north(), pos.south(), pos.east(), pos.west()};
        for (BlockPos nPos : neighbors) {
            TileEntity te = world.getTileEntity(nPos);
            if (!(te instanceof TileCropStick)) continue;
            TileCropStick neighbor = (TileCropStick) te;
            // 改进: 只有生长过半才可作为杂交亲本
            if (neighbor.hasCrop() && neighbor.canCrossBreed() && !neighbor.isWeed) {
                parentCrops.add(neighbor.cropId);
                parentStats.add(neighbor.stats);
            }
        }

        if (parentCrops.size() < 2) return;

        CrossBreedingRegistry.CrossResult result =
                CrossBreedingRegistry.tryCrossBreed(parentCrops, parentStats, world.rand);
        if (result != null) {
            this.cropId = result.cropId;
            this.stats = result.stats;
            this.growthStage = 0;
            this.doubleCropStick = false;
            this.isWeed = result.cropId.equals("weed");
            syncToClient();
        }
    }

    /**
     * 判断是否可以作为杂交亲本
     * 改进: 生长阶段必须超过总阶段的一半
     */
    public boolean canCrossBreed() {
        CropType type = getCropType();
        if (type == null) return false;
        return growthStage >= (type.getMaxGrowthStage() / 2 + 1);
    }

    // ==================== 外部接口 ====================

    public void plantCrop(String cropId, CropStats stats) {
        this.cropId = cropId;
        this.stats = stats;
        this.growthStage = 0;
        this.isWeed = false;
        this.doubleCropStick = false;
        syncToClient();
    }

    public boolean harvest() {
        if (!hasCrop() || !isMature()) return false;
        this.growthStage = 0;
        syncToClient();
        return true;
    }

    public void destroyCrop() {
        this.cropId = "";
        this.stats = new CropStats();
        this.growthStage = 0;
        this.isWeed = false;
        syncToClient();
    }

    public void setDoubleCropStick(boolean value) {
        this.doubleCropStick = value;
        syncToClient();
    }

    // ==================== 查询 ====================

    public boolean hasCrop() { return cropId != null && !cropId.isEmpty(); }

    public boolean isMature() {
        CropType type = getCropType();
        return type != null && growthStage >= type.getHarvestStage();
    }

    public boolean isDoubleCropStick() { return doubleCropStick; }
    public String getCropId() { return cropId; }
    public CropStats getStats() { return stats; }
    public int getGrowthStage() { return growthStage; }
    public boolean isWeedPlant() { return isWeed; }

    @Nullable
    public CropType getCropType() {
        if (cropId == null || cropId.isEmpty()) return null;
        return CropRegistry.get(cropId);
    }

    // ==================== NBT ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("cropId", cropId != null ? cropId : "");
        nbt.setInteger("growthStage", growthStage);
        nbt.setBoolean("doubleCropStick", doubleCropStick);
        nbt.setBoolean("isWeed", isWeed);
        nbt.setInteger("tickCounter", tickCounter);
        if (stats != null) stats.writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        cropId = nbt.getString("cropId");
        growthStage = nbt.getInteger("growthStage");
        doubleCropStick = nbt.getBoolean("doubleCropStick");
        isWeed = nbt.getBoolean("isWeed");
        tickCounter = nbt.getInteger("tickCounter");
        stats = CropStats.readFromNBT(nbt);
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
        // 客户端收到数据后触发重渲染
        if (world != null && world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    /**
     * 核心同步方法 - 标记dirty + 通知客户端刷新方块
     */
    private void syncToClient() {
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            // flags=3: 通知客户端+触发重渲染
            world.notifyBlockUpdate(pos, state, state, 3);
            // 额外标记需要重渲染的区域
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }
}
