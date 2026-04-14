package com.drppp.drtech.Tile;


import com.drppp.drtech.api.crop.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

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
    private boolean pendingSync = false;

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (pendingSync) { pendingSync = false; doSyncToClient(); }
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

    // ==================== 生长 ====================

    private void tickCropGrowth() {
        CropType type = getCropType();
        if (type == null || growthStage >= type.getMaxGrowthStage()) return;

        float light = EnvironmentCalculator.calcLight(world, pos);
        float humidity = EnvironmentCalculator.calcHumidity(world, pos);
        List<String> blocksBelowIds = EnvironmentCalculator.getBlocksBelowIds(world, pos);
        if (!type.canGrowAt(light * 15, humidity, blocksBelowIds)) return;

        float envScore = EnvironmentCalculator.calcEnvironmentScore(world, pos);
        int baseIncr = stats.rollGrowthIncrement(world.rand);
        float envMult = 0.3f + envScore * 1.2f;
        int increment = Math.max(1, Math.round(baseIncr * envMult));

        growthProgress += increment;
        if (growthProgress >= type.getStageRequirement()) {
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
            this.stats = new CropStats(world.rand.nextInt(8)+3, world.rand.nextInt(3)+1, world.rand.nextInt(3)+1);
            this.growthStage = 0; this.growthProgress = 0; this.isWeed = true;
            markDirtyAndScheduleSync();
        }
    }

    private void tickWeedGrowth() {
        CropType wt = CropRegistry.get("weed");
        int maxS = wt != null ? wt.getMaxGrowthStage() : 5;
        int req = wt != null ? wt.getStageRequirement() : 12;
        if (growthStage < maxS) {
            growthProgress += stats.rollGrowthIncrement(world.rand);
            if (growthProgress >= req) { growthProgress = 0; growthStage++; markDirtyAndScheduleSync(); }
        }
        if (growthStage >= maxS) spreadWeed();
    }

    private void spreadWeed() {
        for (BlockPos nPos : new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()}) {
            TileEntity te = world.getTileEntity(nPos);
            if (te instanceof TileCropStick) {
                TileCropStick n = (TileCropStick) te;
                if (n.hasCrop() && !n.isWeed) {
                    if (!n.stats.tryResistWeed(world.rand)) n.convertToWeed();
                } else if (!n.hasCrop() && world.rand.nextInt(3) == 0) {
                    n.cropId = "weed"; n.stats = new CropStats(stats.getGrowth(),1,1);
                    n.isWeed = true; n.growthStage = 0; n.growthProgress = 0;
                    n.markDirtyAndScheduleSync();
                }
            } else {
                IBlockState bs = world.getBlockState(nPos.down());
                if (bs.getBlock() == Blocks.FARMLAND && world.rand.nextInt(5) == 0)
                    world.setBlockState(nPos.down(), Blocks.GRASS.getDefaultState(), 2);
            }
        }
    }

    private void convertToWeed() {
        cropId = "weed"; isWeed = true; growthStage = 0; growthProgress = 0;
        markDirtyAndScheduleSync();
    }

    // ==================== 杂交(新逻辑) ====================

    /**
     * 新杂交逻辑:
     * 1. 收集所有参与杂交的作物
     * 2. 将所有参与者两两组合查询杂交产物表, 同种产物权重相加
     * 3. 每个参与者杂交出自身的权重固定500
     * 4. 加杂草权重50
     * 5. 按权重随机, 结果受Tier限制和canBeBreedResult检查
     */
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

        // 构建产物权重表
        Map<String, Integer> weightTable = new LinkedHashMap<>();

        // 1. 每个参与者杂交出自身: 权重500
        Set<String> parentIds = new HashSet<>();
        for (TileCropStick p : participants) {
            parentIds.add(p.cropId);
            weightTable.merge(p.cropId, 500, Integer::sum);
        }

        // 2. 所有参与者两两组合查询杂交配方, 产物权重相加
        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                Map<String, Integer> products = CrossBreedingRegistry.getProducts(
                        participants.get(i).cropId, participants.get(j).cropId);
                for (Map.Entry<String, Integer> e : products.entrySet()) {
                    weightTable.merge(e.getKey(), e.getValue(), Integer::sum);
                }
            }
        }

        // 3. 杂草权重
        weightTable.merge("weed", 50, Integer::sum);

        // Tier限制
        int maxParentTier = participants.stream()
                .map(t -> CropRegistry.get(t.cropId))
                .filter(Objects::nonNull)
                .mapToInt(CropType::getTier)
                .max().orElse(1);

        // 移除不合格的产物
        weightTable.entrySet().removeIf(e -> {
            CropType ct = CropRegistry.get(e.getKey());
            if (ct == null) return true;
            if (ct.getTier() > maxParentTier + 1) return true;
            if (!ct.canBeBreedResult() && !parentIds.contains(e.getKey())) return true;
            return false;
        });

        if (weightTable.isEmpty()) return;

        // 按权重随机选择
        int total = weightTable.values().stream().mapToInt(Integer::intValue).sum();
        int roll = world.rand.nextInt(total);
        String result = null;
        for (Map.Entry<String, Integer> e : weightTable.entrySet()) {
            roll -= e.getValue();
            if (roll < 0) { result = e.getKey(); break; }
        }
        if (result == null) return;

        // 子代属性
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
        this.growthStage = 0; this.growthProgress = 0;
        this.doubleCropStick = false;
        this.isWeed = result.equals("weed");
        markDirtyAndScheduleSync();
    }

    // ==================== 收获(含概率掉落+战利品表) ====================

    /**
     * 获取收获掉落物列表(外部调用, 用于BlockCropStick)
     */
    public List<ItemStack> getHarvestDrops() {
        CropType type = getCropType();
        if (type == null) return Collections.emptyList();

        List<ItemStack> drops = new ArrayList<>();

        // 获取下方方块ID列表
        List<String> blocksBelowIds = EnvironmentCalculator.getBlocksBelowIds(world, pos);

        // 固定+概率掉落(根据方块决定)
        drops.addAll(type.rollDrops(world.rand, stats.getYieldBonus(world.rand), blocksBelowIds));

        // 战利品表掉落(不受方块影响)
        if (type.getLootTable() != null && !type.getLootTable().isEmpty()) {
            if (world instanceof WorldServer) {
                WorldServer ws = (WorldServer) world;
                LootTable table = ws.getLootTableManager().getLootTableFromLocation(
                        new ResourceLocation(type.getLootTable()));
                LootContext.Builder ctxBuilder = new LootContext.Builder(ws);
                drops.addAll(table.generateLootForPools(world.rand, ctxBuilder.build()));
            }
        }

        return drops;
    }

    // ==================== 外部接口 ====================

    public void plantCrop(String cropId, CropStats stats) {
        this.cropId = cropId; this.stats = stats;
        this.growthStage = 0; this.growthProgress = 0;
        this.isWeed = false; this.doubleCropStick = false;
        immediateSync();
    }

    public boolean harvest() {
        if (!hasCrop() || !isMature()) return false;
        this.growthStage = 0; this.growthProgress = 0;
        immediateSync(); return true;
    }

    public void destroyCrop() {
        this.cropId = ""; this.stats = new CropStats();
        this.growthStage = 0; this.growthProgress = 0; this.isWeed = false;
        immediateSync();
    }

    public void setDoubleCropStick(boolean v) { this.doubleCropStick = v; immediateSync(); }

    public boolean hasCrop() { return cropId != null && !cropId.isEmpty(); }
    public boolean isMature() { CropType t = getCropType(); return t != null && growthStage >= t.getHarvestStage(); }
    public boolean isDoubleCropStick() { return doubleCropStick; }
    public String getCropId() { return cropId; }
    public CropStats getStats() { return stats; }
    public int getGrowthStage() { return growthStage; }
    public int getGrowthProgress() { return growthProgress; }
    public boolean isWeedPlant() { return isWeed; }
    @Nullable public CropType getCropType() { return (cropId == null || cropId.isEmpty()) ? null : CropRegistry.get(cropId); }

    // ==================== NBT ====================

    @Override public NBTTagCompound writeToNBT(NBTTagCompound n) {
        super.writeToNBT(n);
        n.setString("cropId", cropId != null ? cropId : "");
        n.setInteger("growthStage", growthStage); n.setInteger("growthProgress", growthProgress);
        n.setBoolean("doubleCropStick", doubleCropStick); n.setBoolean("isWeed", isWeed);
        n.setInteger("tickCounter", tickCounter); n.setBoolean("pendingSync", pendingSync);
        if (stats != null) stats.writeToNBT(n); return n;
    }

    @Override public void readFromNBT(NBTTagCompound n) {
        super.readFromNBT(n);
        cropId = n.getString("cropId"); growthStage = n.getInteger("growthStage");
        growthProgress = n.getInteger("growthProgress"); doubleCropStick = n.getBoolean("doubleCropStick");
        isWeed = n.getBoolean("isWeed"); tickCounter = n.getInteger("tickCounter");
        pendingSync = n.getBoolean("pendingSync"); stats = CropStats.readFromNBT(n);
    }

    @Override public SPacketUpdateTileEntity getUpdatePacket() { return new SPacketUpdateTileEntity(pos, 1, getUpdateTag()); }
    @Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }
    @Override public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
        if (world != null && world.isRemote) { IBlockState s = world.getBlockState(pos); world.notifyBlockUpdate(pos, s, s, 3); }
    }

    private void markDirtyAndScheduleSync() { markDirty(); pendingSync = true; }
    private void immediateSync() { markDirty(); pendingSync = false; doSyncToClient(); }
    private void doSyncToClient() {
        if (world != null && !world.isRemote) {
            IBlockState s = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, s, s, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }
}

