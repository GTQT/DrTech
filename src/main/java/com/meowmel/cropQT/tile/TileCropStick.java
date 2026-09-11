package com.meowmel.cropQT.tile;


import com.meowmel.cropQT.api.*;
import com.meowmel.cropQT.api.mutation.CropMutation;
import com.meowmel.cropQT.api.mutation.MutationRegistry;
import com.meowmel.cropQT.handler.CropConfig;
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

    /**
     * 底土不满足时的生长惩罚除数。
     *
     * <p>是<b>软惩罚</b>而不是硬门槛：放错底土的作物照样能长，只是慢到玩家自然会去修。
     * 这样不会让已建好的农场突然死掉。
     */
    public static final int SUB_SOIL_PENALTY = 50;

    private String cropId = "";
    private CropStats stats = new CropStats();
    private int growthStage = 0;
    private int growthProgress = 0;
    private boolean doubleCropStick = false;
    private int tickCounter = 0;
    private boolean isWeed = false;
    private boolean pendingSync = false;

    /**
     * 当前储水量。上限由脚下的土壤组决定（见 {@link #getMaxWater()}）。
     *
     * <p>水位只提供<b>正向加成</b>（最多 +30%），断水不会让作物停摆。
     */
    private int waterStorage = 0;
    /** 当前储肥量。加成上限 +50%。 */
    private int fertilizerStorage = 0;

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

        // 配置把生长关掉时，连水肥都不该扣——否则玩家会白养一堆地
        double growthMultiplier = CropConfig.getGrowthMultiplier();
        if (growthMultiplier <= 0.0d) return;

        float light = EnvironmentCalculator.calcLight(world, pos);
        float humidity = EnvironmentCalculator.calcHumidity(world, pos);
        if (!type.canGrowAt(light * 15, humidity)) return;

        // 先扣这一轮的水肥，再按剩余储量算加成——这样储量见底的那一轮加成立刻掉下来
        consumeStorages(type);

        float envScore = EnvironmentCalculator.calcEnvironmentScore(
                world, pos, getWaterRatio(), getFertilizerRatio());
        int baseIncr = stats.rollGrowthIncrement(world.rand);
        float envMult = 0.3f + envScore * 1.2f;
        int increment = Math.max(1, Math.round(baseIncr * envMult));

        // 底土不满足 = 软惩罚：照样长，但慢到玩家自然会去补底土
        if (!isSubSoilSatisfied()) {
            increment = Math.max(1, increment / SUB_SOIL_PENALTY);
        }

        // 配置倍率放在最后——底土惩罚是"这块地不行"，配置是"这个存档想快一点"，两回事
        if (growthMultiplier != 1.0d) {
            increment = Math.max(1, (int) Math.round(increment * growthMultiplier));
        }

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

    // ==================== 杂交 ====================

    /** 自交分支的出现概率（百分比）。照 CropsNH，一半一半。 */
    private static final int SELF_CROSS_CHANCE = 50;

    /**
     * 杂交一轮。流程照 CropsNH：
     * <ol>
     *     <li>收集四邻成熟非杂草作物，各自掷骰决定是否参与</li>
     *     <li>一半概率走<b>自交</b>：随机挑一株参与者原样产出，属性重掷</li>
     *     <li>否则查确定性配方，命中就按权重挑一个</li>
     *     <li>没命中就查变异池，命中的池里随机挑一个、再随机挑成员</li>
     *     <li>产物还要过 tier 上限与 {@code canBeBreedResult} 两道闸</li>
     * </ol>
     */
    private void tickCrossBreeding() {
        BlockPos[] neighbors = {pos.north(), pos.south(), pos.east(), pos.west()};

        List<TileCropStick> allMature = new ArrayList<>();
        for (BlockPos nPos : neighbors) {
            TileEntity te = world.getTileEntity(nPos);
            if (te instanceof TileCropStick) {
                TileCropStick n = (TileCropStick) te;
                if (n.hasCrop() && n.isMature() && !n.isWeed) allMature.add(n);
            }
        }
        if (allMature.size() < 2) return;

        double chanceMultiplier = CropConfig.getCrossBreedChanceMultiplier();
        List<TileCropStick> participants = new ArrayList<>();
        for (TileCropStick n : allMature) {
            int chance = (int) Math.round(n.stats.getCrossBreedChance() * chanceMultiplier);
            if (world.rand.nextInt(100) < Math.min(100, chance)) participants.add(n);
        }
        if (participants.size() < 2) return;

        List<String> parentIds = new ArrayList<>(participants.size());
        for (TileCropStick p : participants) {
            parentIds.add(p.cropId);
        }

        String result = pickBreedingResult(parentIds);
        if (result == null) return;

        // 子代属性取四邻所有作物的平均（不只参与者）
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

    /**
     * 掷出这一轮的杂交产物。
     *
     * @param parentIds 参与者的作物 id，可含重复（两株同种作物会各占一项）
     * @return 作物 id；没有合法产物时返回 {@code null}
     */
    @Nullable
    private String pickBreedingResult(List<String> parentIds) {
        if (world.rand.nextInt(100) < SELF_CROSS_CHANCE) {
            String self = parentIds.get(world.rand.nextInt(parentIds.size()));
            return isBreedResultAllowed(self, parentIds) ? self : null;
        }

        CropMutation mutation = MutationRegistry.pickDeterministic(parentIds, world.rand);
        if (mutation != null) {
            String result = mutation.getResult();
            return isBreedResultAllowed(result, parentIds) ? result : null;
        }

        String pooled = MutationRegistry.pickFromPools(parentIds, world.rand);
        if (pooled == null) {
            return null;
        }
        return isBreedResultAllowed(pooled, parentIds) ? pooled : null;
    }

    /**
     * 产物能不能落在这一格上。
     *
     * <p>两道闸：tier 不超过最高父本 +1；以及要么允许作为杂交产物
     * （{@code canBeBreedResult}），要么本来就在参与者里（自交不受这条限制）。
     */
    private boolean isBreedResultAllowed(String cropId, List<String> parentIds) {
        CropType result = CropRegistry.get(cropId);
        if (result == null) {
            return false;
        }
        if (!result.canBeBreedResult() && !parentIds.contains(cropId)) {
            return false;
        }
        int maxParentTier = 1;
        for (String parentId : parentIds) {
            CropType parent = CropRegistry.get(parentId);
            if (parent != null) {
                maxParentTier = Math.max(maxParentTier, parent.getTier());
            }
        }
        return result.getTier() <= maxParentTier + 1;
    }

    // ==================== 收获(含概率掉落+战利品表) ====================

    /**
     * 获取收获掉落物列表(外部调用, 用于BlockCropStick)
     */
    public List<ItemStack> getHarvestDrops() {
        CropType type = getCropType();
        if (type == null) return Collections.emptyList();

        List<ItemStack> drops = new ArrayList<>();

        // 土壤 + 底土的方块 ID：按方块区分的掉落表不区分是哪一格
        List<String> blocksBelowIds = EnvironmentCalculator.getSoilAndSubSoilIds(world, pos);

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

    /**
     * 种下一株作物。
     *
     * <p>土壤不符合作物要求时<b>拒绝种植</b>——这是硬门槛，与底土的软惩罚相对。
     *
     * @return 是否种成功；调用方据此给玩家提示
     */
    public boolean plantCrop(String cropId, CropStats stats) {
        CropType type = CropRegistry.get(cropId);
        if (type == null || !isSoilValid(type)) {
            return false;
        }
        this.cropId = cropId; this.stats = stats;
        this.growthStage = 0; this.growthProgress = 0;
        this.isWeed = false; this.doubleCropStick = false;
        immediateSync();
        return true;
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

    // ==================== 水 / 肥储量 ====================

    /** 扣掉一个生长周期的水与肥。脚下不是已登记土壤时（理论上不会）不扣。 */
    private void consumeStorages(CropType type) {
        ISoilList soil = getSoilType();
        if (soil == null) {
            return;
        }
        int tier = type.getTier();
        waterStorage = Math.max(0, waterStorage - soil.getWaterUsage(tier));
        fertilizerStorage = Math.max(0, fertilizerStorage - soil.getFertilizerUsage(tier));
    }

    /** 储水上限，来自脚下的土壤组；不是土壤时返回 0。 */
    public int getMaxWater() {
        ISoilList soil = getSoilType();
        return soil == null ? 0 : soil.getWaterCapacity();
    }

    /** 储肥上限，同上。 */
    public int getMaxFertilizer() {
        ISoilList soil = getSoilType();
        return soil == null ? 0 : soil.getFertilizerCapacity();
    }

    public int getWaterStorage() { return waterStorage; }

    public int getFertilizerStorage() { return fertilizerStorage; }

    /** 水位 0~1，用于加成计算与界面显示。 */
    public float getWaterRatio() {
        int max = getMaxWater();
        return max <= 0 ? 0f : Math.min(1f, waterStorage / (float) max);
    }

    /** 肥位 0~1。 */
    public float getFertilizerRatio() {
        int max = getMaxFertilizer();
        return max <= 0 ? 0f : Math.min(1f, fertilizerStorage / (float) max);
    }

    /** 补水。 @return 实际补进去的量，满了或补不进时为 0 */
    public int addWater(int amount) {
        return addToStorage(amount, true);
    }

    /** 补肥。 @return 实际补进去的量 */
    public int addFertilizer(int amount) {
        return addToStorage(amount, false);
    }

    private int addToStorage(int amount, boolean water) {
        if (amount <= 0) {
            return 0;
        }
        int max = water ? getMaxWater() : getMaxFertilizer();
        int current = water ? waterStorage : fertilizerStorage;
        int added = Math.min(amount, max - current);
        if (added <= 0) {
            return 0;
        }
        if (water) {
            waterStorage += added;
        } else {
            fertilizerStorage += added;
        }
        markDirtyAndScheduleSync();
        return added;
    }

    // ==================== 种植条件查询 ====================

    /**
     * 脚下的土壤是否符合作物的要求。
     *
     * <p>作物没声明土壤组（{@code null}）表示不限土壤，恒为 {@code true}。
     */
    public boolean isSoilValid(@Nullable CropType type) {
        if (type == null) {
            return true;
        }
        ISoilList required = type.getSoilTypes();
        return required == null || required.contains(EnvironmentCalculator.soilState(world, pos));
    }

    /**
     * 当前底土是否满足要求。
     *
     * <p>没有底土要求的作物恒为 {@code true}。
     */
    public boolean isSubSoilSatisfied() {
        CropType type = getCropType();
        if (type == null || !type.hasSubSoilRequirement()) return true;
        return type.getSubSoilRequirement().isMet(world, pos);
    }

    /** 脚下土壤所属的组；不是任何已登记土壤时返回 {@code null}。 */
    @Nullable
    public ISoilList getSoilType() {
        return SoilRegistry.getSoilFor(EnvironmentCalculator.soilState(world, pos));
    }

    /**
     * 当前不满足的生长条件，供分析仪 / TOP / tooltip 展示。
     *
     * <p>只列「种下去之后还能改」的条件——底土。土壤不在其中：它是种植时的硬门槛，
     * 一旦种下就改不了了，列出来只会让玩家以为还有救。
     */
    public List<GrowthRequirement> getUnmetRequirements() {
        CropType type = getCropType();
        if (type == null) {
            return Collections.emptyList();
        }
        SubSoilRequirement subSoil = type.getSubSoilRequirement();
        if (subSoil == null || subSoil.isMet(world, pos)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(subSoil);
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
        n.setInteger("waterStorage", waterStorage);
        n.setInteger("fertilizerStorage", fertilizerStorage);
        if (stats != null) stats.writeToNBT(n); return n;
    }

    @Override public void readFromNBT(NBTTagCompound n) {
        super.readFromNBT(n);
        cropId = n.getString("cropId"); growthStage = n.getInteger("growthStage");
        growthProgress = n.getInteger("growthProgress"); doubleCropStick = n.getBoolean("doubleCropStick");
        isWeed = n.getBoolean("isWeed"); tickCounter = n.getInteger("tickCounter");
        pendingSync = n.getBoolean("pendingSync"); stats = CropStats.readFromNBT(n);
        waterStorage = n.getInteger("waterStorage");
        fertilizerStorage = n.getInteger("fertilizerStorage");
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
