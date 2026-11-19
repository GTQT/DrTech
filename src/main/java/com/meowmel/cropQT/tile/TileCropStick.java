package com.meowmel.cropQT.tile;

import com.meowmel.cropQT.api.CropConstants;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.EnvironmentCalculator;
import com.meowmel.cropQT.api.GrowthRequirement;
import com.meowmel.cropQT.api.ISoilList;
import com.meowmel.cropQT.api.SoilRegistry;
import com.meowmel.cropQT.api.SubSoilRequirement;
import com.meowmel.cropQT.api.mutation.CropMutation;
import com.meowmel.cropQT.api.mutation.MutationRegistry;
import com.meowmel.cropQT.handler.CropConfig;
import com.meowmel.cropQT.item.ItemCropSeed;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 作物架方块实体。
 *
 * <p>本类的生长/存储/杂草/病害逻辑逐字对齐 CropsNH 的 {@code TileEntityCropSticks}：
 * <ul>
 *     <li>水、肥、WeedEX 三件套共用 <b>阈值 + 上限 + simulate</b> 语义；</li>
 *     <li>生长进度是「对成熟所需时长」的单条进度，而不是旧版的阶段阈值；</li>
 *     <li>每 256 tick 一次 growth tick，末尾各扣 1 点水/肥；</li>
 *     <li>降雨补水 {@code addWater(11, 100, 101, false)}；</li>
 *     <li>营养分 → 生长速度的公式来自源端，逐行照抄。</li>
 * </ul>
 */
public class TileCropStick extends TileEntity implements ITickable {

    /** 生长周期长度（tick）。旧名保留，外部还有引用。 */
    public static final int GROWTH_CYCLE = CropConstants.TICK_RATE;

    /** 旧版底土软惩罚除数：不满足底土时生长增量除以它，保留为软惩罚（源端是硬需求）。 */
    @Deprecated
    public static final int SUB_SOIL_PENALTY = 50;

    private String cropId = "";
    private CropStats stats = new CropStats();
    /** 生长进度（点），0..crop.getGrowthDuration()。 */
    private int growthProgress = 0;
    /** 渲染用阶段缓存；实际阶段由 {@link #getGrowthStage()} 按进度比例换算。 */
    private int growthStage = 0;
    private boolean doubleCropStick = false;
    private int ticker = 0;
    private boolean isWeed = false;
    private boolean isSick = false;
    private boolean pendingSync = false;

    private int waterStorage = 0;
    private int fertilizerStorage = 0;
    private int weedExStorage = 0;

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (pendingSync) {
            pendingSync = false;
            doSyncToClient();
        }
        ticker = (ticker + 1) % CropConstants.TICK_RATE;
        if (ticker == 0) {
            onGrowthTick();
        }
    }

    // ==================== 生长 ====================

    public void onGrowthTick() {
        // 客户端也跑一次，这样玩家看得到「为什么不长」的状态。
        boolean canGrow = canGrow();
        if (world.isRemote) return;

        // 降雨补水：101 是刻意的，下一 tick 末尾会扣 1 点。
        if (world.isRainingAt(pos)) {
            addWater(11, 100, 101, false);
        }

        if (hasCrop()) {
            if (isSick) {
                spreadDisease();
                canGrow = false;
            }
            if (canGrow) {
                doGrowth();
            }
            if (isWeed) {
                if (spreadRoll(CropConfig.getWeedSpreadChance()) - stats.getGrowth() <= 2) {
                    spreadWeed();
                }
            } else if (CropConfig.isWeedsEnabled()) {
                CropType type = getCropType();
                if (type != null && type.spreadsWeeds()
                        && spreadRoll(CropConfig.getWeedSpreadChance()) - stats.getGrowth() <= 2) {
                    spreadWeed();
                }
            }
        } else {
            boolean bred = false;
            if (doubleCropStick) {
                bred = attemptBreeding();
            }
            if (!bred && CropConfig.isWeedsEnabled() && chanceRoll(CropConfig.getWeedSpawnChance())) {
                spawnWeed();
            }
        }

        // 源端顺序：每 growth tick 末尾各扣 1 点。
        if (fertilizerStorage > 0) fertilizerStorage--;
        if (waterStorage > 0) waterStorage--;
        markDirty();
    }

    /** nextInt(chance)==0，chance<=0 时视为永不发生，避免除零崩溃。 */
    private boolean chanceRoll(int chance) {
        return chance > 0 && world.rand.nextInt(chance) == 0;
    }

    /** nextInt(chance)，chance<=0 时返回 0。 */
    private int spreadRoll(int chance) {
        return chance > 0 ? world.rand.nextInt(chance) : 0;
    }

    /** 源端 canGrow：无作物/生病 → false；杂草恒 true；成熟时只做抽查并返回 false。 */
    public boolean canGrow() {
        if (!hasCrop() || isSick) return false;
        CropType type = getCropType();
        if (type == null) return false;
        if (isWeed) return true;
        if (isMature()) {
            if (world.rand.nextBoolean()) areGrowthRequirementsMet();
            return false;
        }
        return areGrowthRequirementsMet();
    }

    /**
     * 世界内生长条件：光照/湿度。
     *
     * <p>底土需求在 cropQT 里仍是<b>软惩罚</b>（见 {@link #doGrowth()}），不当作硬门槛；
     * 这是本模组与源端有意保留的差异，避免已建农场因换底土直接绝收。
     */
    public boolean areGrowthRequirementsMet() {
        CropType type = getCropType();
        if (type == null) return false;
        float light = EnvironmentCalculator.calcLight(world, pos) * 15f;
        float humidity = EnvironmentCalculator.calcHumidity(world, pos);
        return type.canGrowAt(light, humidity);
    }

    private void doGrowth() {
        CropType type = getCropType();
        if (type == null) return;
        int duration = Math.max(1, type.getGrowthDuration());
        if (growthProgress >= duration) return;

        // 源端是硬需求；cropQT 保留一个配置项可退回旧的软惩罚。
        if (CropConfig.isSubSoilHardRequirement() && !isSubSoilSatisfied()) {
            return;
        }

        int growthRate = calcGrowthRate();
        if (growthRate <= 0) {
            if (!passesResistanceCheck()) {
                isSick = true;
                markDirtyAndScheduleSync();
            }
            return;
        }

        int increment = (int) (growthRate * CropConfig.getGrowthMultiplier());
        // 软惩罚模式：底土不满足照样长，但慢到玩家自然会去补底土。
        if (!isSubSoilSatisfied()) {
            increment = Math.max(1, increment / SUB_SOIL_PENALTY);
        }
        growthProgress += increment;
        if (growthProgress > duration) growthProgress = duration;
        growthStage = computeStage();
        markDirty();
        markDirtyAndScheduleSync();
    }

    /** 营养分：biome 喜好标签 + 湿度 + 见天 + 水/肥储量。 */
    public int getNutrientScore() {
        if (!hasCrop()) return 0;
        Biome biome = world.getBiome(pos);
        Set<BiomeDictionary.Type> biomeTags = BiomeDictionary.getTypes(biome);
        Set<BiomeDictionary.Type> liked = getCropType() == null ? Collections.emptySet()
                : getCropType().getLikedBiomes();
        int likedBiomes = 0;
        for (BiomeDictionary.Type tag : liked) {
            if (biomeTags.contains(tag)) {
                likedBiomes++;
            }
        }
        boolean canSeeSky = world.canSeeSky(pos.up());
        return getNutrientsPerCycle(likedBiomes, biome.getRainfall(), canSeeSky, waterStorage, fertilizerStorage);
    }

    /** 源端逐字公式：每周期营养分。 */
    public static int getNutrientsPerCycle(int likedBiomeTagsCount, float biomeHumidity, boolean canSeeSky,
                                           int waterStorage, int fertilizerStorage) {
        int liked = Math.min(CropConstants.MAX_LIKED_BIOME_TAG_COUNT, likedBiomeTagsCount);
        waterStorage = Math.min(CropConstants.MAX_WATER_BONUS_AT, waterStorage);
        fertilizerStorage = Math.min(CropConstants.MAX_FERTILIZER_BONUS_AT, fertilizerStorage);

        int nutrients = CropConstants.BASE_NUTRIENT_VALUE;
        nutrients += (waterStorage + 9) / 10;
        nutrients += (fertilizerStorage + 9) / 10;
        nutrients += canSeeSky ? CropConstants.SKY_ACCESS_BONUS : 0;
        float humidityBonus = (biomeHumidity - CropConstants.LOW_HUMIDITY_THRESHOLD)
                / (CropConstants.HIGH_HUMIDITY_THRESHOLD - CropConstants.LOW_HUMIDITY_THRESHOLD);
        humidityBonus = MathHelper.clamp(humidityBonus, 0f, 1f) * CropConstants.HIGH_HUMIDITY_BONUS;
        nutrients += Math.max((int) humidityBonus, liked * CropConstants.LIKED_BIOME_BONUS);
        return nutrients;
    }

    /** 源端逐字公式：生长速度（每 growth tick 的进度增量，未乘配置倍率）。 */
    public static int getGrowthRate(int nutrientPoints, int tier, int growth) {
        int np = nutrientPoints * CropConstants.NUTRIENT_POINT_SCALE;
        int need = tier * CropConstants.NUTRIENTS_NEEDED_PER_TIER;
        if (need < 0) return 0;
        int base = CropConstants.BASE_GROWTH_SPEED + growth;
        if (np >= need) {
            return base * (100 + (np - need)) / 100;
        }
        return Math.max(base * (100 - (need - np) * 4) / 100, 0);
    }

    public int calcGrowthRate() {
        CropType type = getCropType();
        if (type == null) return 0;
        return getGrowthRate(getNutrientScore(), type.getTier(), stats.getGrowth());
    }

    /** 抗性判定：resistance > rand(31)。 */
    public boolean passesResistanceCheck() {
        return stats != null && stats.getResistance() > world.rand.nextInt(CropConstants.MAX_SEED_STAT);
    }

    // ==================== 杂草 ====================

    /** 源端 spawnWeed：31 抗性免疫；WeedEX 优先消耗；weedsWipePlants 时直接覆盖作物。 */
    public void spawnWeed() {
        if (hasCrop() && !isWeed
                && (!CropConfig.doWeedsWipePlants() || stats.getResistance() >= CropConstants.MAX_SEED_STAT)) {
            return;
        }
        if (weedExStorage > 0) {
            weedExStorage = Math.max(weedExStorage - CropConstants.WEEDEX_COST, 0);
            markDirtyAndScheduleSync();
            return;
        }
        this.cropId = "weed";
        this.isWeed = true;
        this.isSick = false;
        this.stats = new CropStats(world.rand.nextInt(8) + 3, 1, 1);
        this.growthProgress = 0;
        this.growthStage = 0;
        this.doubleCropStick = false;
        markDirtyAndScheduleSync();
    }

    /** 源端 spreadWeed：随机一个水平邻居，作物 → 病害，空架 → 杂草，空气 → 草。 */
    public void spreadWeed() {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        switch (world.rand.nextInt(4)) {
            case 0 -> ++x;
            case 1 -> --x;
            case 2 -> ++z;
            case 3 -> --z;
        }
        BlockPos target = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(target);
        if (te instanceof TileCropStick neighbour) {
            if (neighbour.hasCrop()) {
                if (!neighbour.isWeed && !neighbour.isSick) {
                    neighbour.transferDisease();
                }
            } else {
                neighbour.spawnWeed();
            }
        } else if (world.isAirBlock(target)) {
            IBlockState below = world.getBlockState(target.down());
            if (below.getBlock() == Blocks.GRASS || below.getBlock() == Blocks.DIRT
                    || below.getBlock() == Blocks.FARMLAND) {
                world.setBlockState(target.down(), Blocks.GRASS.getDefaultState(), 7);
                world.setBlockState(target, Blocks.TALLGRASS.getStateFromMeta(1), 7);
            }
        }
    }

    // ==================== 病害 ====================

    public boolean isSick() {
        return isSick;
    }

    public boolean cureDisease() {
        if (!hasCrop() || !isSick) return false;
        isSick = false;
        markDirtyAndScheduleSync();
        return true;
    }

    /** 源端 transferDisease：抗性可免；WeedEX 每次 2 点可挡；否则生病。 */
    public void transferDisease() {
        if (!hasCrop() || isSick || isWeed) return;
        if (passesResistanceCheck()) return;
        if (weedExStorage > 0) {
            weedExStorage = Math.max(weedExStorage - 2, 0);
            markDirtyAndScheduleSync();
            return;
        }
        isSick = true;
        markDirtyAndScheduleSync();
    }

    public void spreadDisease() {
        List<TileCropStick> neighbours = getNeighbours();
        neighbours.removeIf(t -> !t.hasCrop() || t.isWeed);
        if (neighbours.isEmpty()) return;
        neighbours.get(world.rand.nextInt(neighbours.size())).transferDisease();
    }

    public List<TileCropStick> getNeighbours() {
        List<TileCropStick> result = new ArrayList<>(4);
        for (BlockPos nPos : new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west()}) {
            TileEntity te = world.getTileEntity(nPos);
            if (te instanceof TileCropStick) result.add((TileCropStick) te);
        }
        return result;
    }

    // ==================== 杂交（世界） ====================

    /** 源端 canCross：有作物、无杂草、作物允许自交，且生长进度达到自交门槛。 */
    public boolean canCross() {
        CropType type = getCropType();
        if (type == null || isWeed || !hasCrop()) return false;
        float threshold = type.getCrossingThreshold();
        return threshold >= 0f && getGrowthPercent() >= threshold;
    }

    /** 源端 canBreed：有作物、无杂草，且生长进度达到杂交门槛。 */
    public boolean canBreed() {
        CropType type = getCropType();
        if (type == null || isWeed || !hasCrop()) return false;
        return getGrowthPercent() >= type.getBreedingThreshold();
    }

    /**
     * 源端 {@code attemptBreeding}：空的双层架上尝试长出新作物。
     *
     * <p>顺序与源端一致：先掷 {@code 1/breedingChance}；一半概率尝试「自交分支」
     * （只要求 {@link #canCross()}），否则走「杂交分支」（要求至少 2 株 {@link #canBreed()}，
     * 先查确定性变异、再查变异池）。属性由参与植株的均值加配置里的变异候选值得到。
     */
    private boolean attemptBreeding() {
        int breedingChance = CropConfig.getBreedingChance();
        if (breedingChance <= 0 || world.rand.nextInt(breedingChance) != 0) return false;

        List<TileCropStick> neighbours = new ArrayList<>();
        for (TileCropStick n : getNeighbours()) {
            if (n.hasCrop() && !n.isWeed) neighbours.add(n);
        }
        if (neighbours.isEmpty()) return false;

        // 源端 getBreedingResult：一半概率先试自交（crossingThreshold 门槛）。
        if (world.rand.nextBoolean()) {
            List<TileCropStick> crossers = new ArrayList<>();
            for (TileCropStick n : neighbours) {
                if (n.canCross()) crossers.add(n);
            }
            if (!crossers.isEmpty()) {
                String chosen = crossers.get(world.rand.nextInt(crossers.size())).cropId;
                List<TileCropStick> participants = new ArrayList<>();
                for (TileCropStick n : crossers) {
                    if (chosen.equals(n.cropId)) participants.add(n);
                }
                return finishBreeding(chosen, participants);
            }
        }

        // 杂交分支：只保留达到 breedingThreshold 的植株，至少 2 株。
        List<TileCropStick> breeders = new ArrayList<>();
        for (TileCropStick n : neighbours) {
            if (n.canBreed()) breeders.add(n);
        }
        if (breeders.size() < 2) return false;

        List<String> parentIds = new ArrayList<>(breeders.size());
        for (TileCropStick n : breeders) parentIds.add(n.cropId);

        CropMutation mutation = MutationRegistry.pickDeterministic(parentIds, world.rand);
        if (mutation != null) {
            // 只有真正参与该变异的植株贡献属性（源端 neighbours.removeIf 同样处理）。
            List<TileCropStick> participants = new ArrayList<>();
            for (TileCropStick n : breeders) {
                if (mutation.getParents().contains(n.cropId)) participants.add(n);
            }
            return finishBreeding(mutation.getResult(), participants);
        }

        String pooled = MutationRegistry.pickFromPools(parentIds, world.rand);
        return pooled != null && finishBreeding(pooled, breeders);
    }

    /**
     * 按源端 {@code variateStat} 计算三项属性并种下结果。
     *
     * @param resultId     结果作物 id
     * @param participants 参与繁殖的植株（已按变异亲本过滤）
     */
    private boolean finishBreeding(String resultId, List<TileCropStick> participants) {
        CropType result = CropRegistry.get(resultId);
        if (result == null || participants.isEmpty()) return false;

        List<String> parentIds = new ArrayList<>(participants.size());
        int minFertilizer = Integer.MAX_VALUE;
        for (TileCropStick n : participants) {
            parentIds.add(n.cropId);
            minFertilizer = Math.min(minFertilizer, n.getFertilizerStorage());
        }
        if (minFertilizer == Integer.MAX_VALUE) minFertilizer = 0;

        if (!isBreedResultAllowed(resultId, parentIds)) return false;
        // 源端还会检查 isValidSoilForCrop / wouldCropBeAbleToGrow
        if (!isSoilValid(result) || !areGrowthRequirementsMetFor(result)) return false;

        int[] variations = CropConfig.getStatVariation(participants.size() <= 1, minFertilizer);
        int growth = variateStat(participants, variations, 0);
        int gain = variateStat(participants, variations, 1);
        int resistance = variateStat(participants, variations, 2);

        this.cropId = resultId;
        // 源端新长出的作物是「未分析」状态
        this.stats = new CropStats(growth, gain, resistance, false);
        this.growthProgress = 0;
        this.growthStage = 0;
        this.doubleCropStick = false;
        this.isWeed = "weed".equals(resultId);
        this.isSick = false;
        markDirtyAndScheduleSync();
        return true;
    }

    /** 属性均值 + 随机变异，截断到 1..31（源端 {@code variateStat}）。 */
    private int variateStat(List<TileCropStick> participants, int[] variations, int stat) {
        int sum = 0;
        for (TileCropStick n : participants) {
            switch (stat) {
                case 1 -> sum += n.stats.getGain();
                case 2 -> sum += n.stats.getResistance();
                default -> sum += n.stats.getGrowth();
            }
        }
        int average = sum / participants.size();
        int variation = variations[world.rand.nextInt(variations.length)];
        return Math.max(CropConstants.MIN_SEED_STAT,
                Math.min(CropConstants.MAX_SEED_STAT, average + variation));
    }

    /** 该株当前环境能否让指定作物生长（源端 {@code wouldCropBeAbleToGrow}）。 */
    private boolean areGrowthRequirementsMetFor(CropType type) {
        float light = EnvironmentCalculator.calcLight(world, pos) * 15f;
        float humidity = EnvironmentCalculator.calcHumidity(world, pos);
        return type.canGrowAt(light, humidity);
    }

    private boolean isBreedResultAllowed(String id, List<String> parentIds) {
        CropType result = CropRegistry.get(id);
        if (result == null) return false;
        if (!result.canBeBreedResult() && !parentIds.contains(id)) return false;
        int maxParentTier = 1;
        for (String parentId : parentIds) {
            CropType parent = CropRegistry.get(parentId);
            if (parent != null) maxParentTier = Math.max(maxParentTier, parent.getTier());
        }
        return result.getTier() <= maxParentTier + 1;
    }

    // ==================== 收获 ====================

    /** 源端 canHarvest：成熟、非杂草、生长条件满足。 */
    public boolean canHarvest() {
        if (!hasCrop() || isWeed || !isMature() || !areGrowthRequirementsMet()) {
            return false;
        }
        return !CropConfig.isSubSoilHardRequirement() || isSubSoilSatisfied();
    }

    public List<ItemStack> getHarvestDrops() {
        return getHarvestDrops(1.0d);
    }

    /**
     * @param roundsMultiplier 收获轮数倍率：玩家 1.0；CropManager 传 {@code 1 + 0.05*tier}。
     */
    public List<ItemStack> getHarvestDrops(double roundsMultiplier) {
        CropType type = getCropType();
        if (type == null) return Collections.emptyList();

        List<ItemStack> drops = new ArrayList<>();
        List<String> blocksBelowIds = EnvironmentCalculator.getSoilAndSubSoilIds(world, pos);
        drops.addAll(type.rollDropsExpected(world.rand, stats.getGain(), roundsMultiplier, blocksBelowIds));

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

    // ==================== 践踏 ====================

    /** 方块碰撞回调：冲刺跑过或从高处落下会踩坏作物架（源端 {@code onEntityCollision}）。 */
    public void onEntityCollision(Entity target) {
        if (world == null || world.isRemote) return;
        if (!(target instanceof EntityLivingBase entity)) return;
        if (canTrample() && (shouldTrampleFromRunning(entity) || shouldTrampleFromFalling(entity))) {
            trample();
        }
    }

    private boolean canTrample() {
        if (!hasCrop() || isWeed) return false;
        // 31 抗性（满值）免疫践踏。
        return stats.getResistance() < CropConstants.MAX_SEED_STAT;
    }

    /** 冲刺：{@code 100*0.95^tier} 分之一的判定，再按抗性抵抗。 */
    private boolean shouldTrampleFromRunning(EntityLivingBase entity) {
        if (!entity.isSprinting()) return false;
        CropType type = getCropType();
        int tier = type == null ? 1 : Math.max(1, type.getTier());
        int maxRoll = Math.max(1, (int) (100.0d * Math.pow(0.95d, tier)));
        if (world.rand.nextInt(maxRoll) > 0) return false;
        return world.rand.nextInt(CropConstants.MAX_SEED_STAT) > stats.getResistance();
    }

    private static final float MIN_FALL_DISTANCE_FOR_TRAMPLING = 0.75f;

    /** 摔落：超过 0.75 格后按超出距离的概率践踏。 */
    private boolean shouldTrampleFromFalling(EntityLivingBase entity) {
        return entity.onGround && entity.fallDistance > MIN_FALL_DISTANCE_FOR_TRAMPLING
                && world.rand.nextFloat() < entity.fallDistance - MIN_FALL_DISTANCE_FOR_TRAMPLING;
    }

    /** 源端 {@code breakCropStick(true)}：掉架子 + 收获物 + 种子，耕地踩回泥土。 */
    private void trample() {
        IBlockState state = world.getBlockState(pos);
        int stickCount = isDoubleCropStick() ? 2 : 1;
        List<ItemStack> drops = new ArrayList<>();
        if (hasCrop() && !isWeed) {
            drops.addAll(getHarvestDrops(1.0d));
            drops.add(ItemCropSeed.createSeedBag(cropId, stats));
        }
        world.setBlockToAir(pos);
        BlockPos soilPos = pos.down();
        if (world.getBlockState(soilPos).getBlock() == Blocks.FARMLAND) {
            world.setBlockState(soilPos, Blocks.DIRT.getDefaultState(), 3);
        }
        Block.spawnAsEntity(world, pos, new ItemStack(state.getBlock(), stickCount));
        for (ItemStack drop : drops) {
            Block.spawnAsEntity(world, pos, drop);
        }
    }

    // ==================== 外部接口 ====================

    public boolean plantCrop(String cropId, CropStats stats) {
        CropType type = CropRegistry.get(cropId);
        if (type == null || !isSoilValid(type)) return false;
        this.cropId = cropId;
        this.stats = stats;
        this.growthProgress = 0;
        this.growthStage = 0;
        this.isWeed = false;
        this.isSick = false;
        this.doubleCropStick = false;
        this.waterStorage = Math.min(this.waterStorage, CropConstants.WATER_STORAGE_MAX);
        immediateSync();
        return true;
    }

    public boolean harvest() {
        if (!canHarvest()) return false;
        this.growthProgress = 0;
        this.growthStage = 0;
        immediateSync();
        return true;
    }

    public void destroyCrop() {
        this.cropId = "";
        this.stats = new CropStats();
        this.growthProgress = 0;
        this.growthStage = 0;
        this.isWeed = false;
        this.isSick = false;
        immediateSync();
    }

    // ==================== 水 / 肥 / WeedEX ====================

    /** 源端三件套语义：已超阈值拒绝；simulate 只判定；否则 clamp 到上限。 */
    public boolean addWater(int amount, int threshold, int maxStorage, boolean simulate) {
        if (waterStorage > threshold) return false;
        if (!simulate) {
            waterStorage = Math.min(maxStorage, waterStorage + amount);
            markDirtyAndScheduleSync();
        }
        return true;
    }

    public boolean addFertilizer(int amount, int threshold, int maxStorage, boolean simulate) {
        if (fertilizerStorage > threshold) return false;
        if (!simulate) {
            fertilizerStorage = Math.min(maxStorage, fertilizerStorage + amount);
            markDirtyAndScheduleSync();
        }
        return true;
    }

    public boolean addWeedEx(int amount, int threshold, int maxStorage, boolean simulate) {
        if (weedExStorage > threshold) return false;
        if (!simulate) {
            weedExStorage = Math.min(maxStorage, weedExStorage + amount);
            markDirtyAndScheduleSync();
        }
        return true;
    }

    /** 旧签名：返回实际补进去的量。 */
    public int addWater(int amount) {
        if (amount <= 0) return 0;
        int before = waterStorage;
        addWater(amount, CropConstants.WATER_STORAGE_THRESHOLD, CropConstants.WATER_STORAGE_MAX, false);
        return waterStorage - before;
    }

    /** 旧签名：返回实际补进去的量。 */
    public int addFertilizer(int amount) {
        if (amount <= 0) return 0;
        int before = fertilizerStorage;
        addFertilizer(amount, CropConstants.FERTILIZER_LIQUID_THRESHOLD,
                CropConstants.FERTILIZER_STORAGE_MAX, false);
        return fertilizerStorage - before;
    }

    public int getWaterStorage() { return waterStorage; }
    public int getFertilizerStorage() { return fertilizerStorage; }
    public int getWeedExStorage() { return weedExStorage; }

    public int getMaxWater() { return CropConstants.WATER_STORAGE_MAX; }
    public int getMaxFertilizer() { return CropConstants.FERTILIZER_STORAGE_MAX; }

    public float getWaterRatio() {
        int max = getMaxWater();
        return max <= 0 ? 0f : MathHelper.clamp(waterStorage / (float) max, 0f, 1f);
    }

    public float getFertilizerRatio() {
        int max = getMaxFertilizer();
        return max <= 0 ? 0f : MathHelper.clamp(fertilizerStorage / (float) max, 0f, 1f);
    }

    // ==================== 种植条件查询 ====================

    public boolean isSoilValid(@Nullable CropType type) {
        if (type == null) return true;
        ISoilList required = type.getSoilTypes();
        return required == null || required.contains(EnvironmentCalculator.soilState(world, pos));
    }

    public boolean isSubSoilSatisfied() {
        CropType type = getCropType();
        if (type == null || !type.hasSubSoilRequirement()) return true;
        return type.getSubSoilRequirement().isMet(world, pos);
    }

    @Nullable
    public ISoilList getSoilType() {
        return SoilRegistry.getSoilFor(EnvironmentCalculator.soilState(world, pos));
    }

    public List<GrowthRequirement> getUnmetRequirements() {
        List<GrowthRequirement> unmet = new ArrayList<>();
        CropType type = getCropType();
        if (type == null) return unmet;
        SubSoilRequirement subSoil = type.getSubSoilRequirement();
        if (subSoil != null && !subSoil.isMet(world, pos)) {
            unmet.add(subSoil);
        }
        return unmet;
    }

    public void setDoubleCropStick(boolean v) {
        this.doubleCropStick = v;
        immediateSync();
    }

    public boolean hasCrop() { return cropId != null && !cropId.isEmpty(); }
    public boolean hasWeed() { return isWeed; }

    /** 源端：进度达到作物生长时长即成熟。 */
    public boolean isMature() {
        CropType t = getCropType();
        return t != null && t.getGrowthDuration() <= growthProgress;
    }

    public boolean isDoubleCropStick() { return doubleCropStick; }
    public String getCropId() { return cropId; }
    public CropStats getStats() { return stats; }

    /**
     * 植物透镜的「分析」动作：把种子标记成已分析。
     *
     * @return true 表示这株作物确实存在、已标记
     */
    public boolean analyzeSeed() {
        if (stats == null || !hasCrop()) {
            return false;
        }
        if (!stats.isAnalyzed()) {
            stats.analyze();
            markDirty();
            immediateSync();
        }
        return true;
    }

    /** 渲染阶段：按进度比例换算到作物的最大阶段。 */
    public int getGrowthStage() {
        return computeStage();
    }

    private int computeStage() {
        CropType t = getCropType();
        if (t == null) return 0;
        int max = Math.max(1, t.getMaxGrowthStage());
        int duration = Math.max(1, t.getGrowthDuration());
        return MathHelper.clamp(growthProgress * max / duration, 0, max);
    }

    public int getGrowthProgress() { return hasCrop() ? growthProgress : 0; }

    public float getGrowthPercent() {
        CropType t = getCropType();
        if (t == null) return 0f;
        return MathHelper.clamp(growthProgress / (float) Math.max(1, t.getGrowthDuration()), 0f, 1f);
    }

    public void setGrowthProgress(int progress) {
        CropType t = getCropType();
        if (t == null) return;
        this.growthProgress = MathHelper.clamp(progress, 0, Math.max(1, t.getGrowthDuration()));
        markDirtyAndScheduleSync();
    }

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
        n.setInteger("growthProgress", growthProgress);
        n.setInteger("growthStage", growthStage);
        n.setBoolean("doubleCropStick", doubleCropStick);
        n.setBoolean("isWeed", isWeed);
        n.setBoolean("isSick", isSick);
        n.setInteger("ticker", ticker);
        n.setBoolean("pendingSync", pendingSync);
        n.setInteger("waterStorage", waterStorage);
        n.setInteger("fertilizerStorage", fertilizerStorage);
        n.setInteger("weedExStorage", weedExStorage);
        n.setBoolean("cropqt_v2", true);
        if (stats != null) stats.writeToNBT(n);
        return n;
    }

    @Override
    public void readFromNBT(NBTTagCompound n) {
        super.readFromNBT(n);
        cropId = n.getString("cropId");
        isWeed = n.getBoolean("isWeed");
        isSick = n.getBoolean("isSick");
        doubleCropStick = n.getBoolean("doubleCropStick");
        ticker = n.getInteger("ticker");
        pendingSync = n.getBoolean("pendingSync");
        stats = CropStats.readFromNBT(n);
        waterStorage = MathHelper.clamp(n.getInteger("waterStorage"), 0, CropConstants.WATER_STORAGE_MAX);
        fertilizerStorage = MathHelper.clamp(n.getInteger("fertilizerStorage"), 0,
                CropConstants.FERTILIZER_STORAGE_MAX);
        weedExStorage = MathHelper.clamp(n.getInteger("weedExStorage"), 0, CropConstants.WEEDEX_STORAGE_MAX);

        if (n.getBoolean("cropqt_v2")) {
            growthProgress = n.getInteger("growthProgress");
            growthStage = n.getInteger("growthStage");
        } else {
            // 旧存档：growthProgress 是「当前阶段内的进度」，按阶段比例折算到成熟时长。
            CropType t = getCropType();
            int oldStage = n.getInteger("growthStage");
            int oldProgress = n.getInteger("growthProgress");
            if (t != null) {
                int max = Math.max(1, t.getMaxGrowthStage());
                int duration = Math.max(1, t.getGrowthDuration());
                growthProgress = MathHelper.clamp((int) ((oldStage + MathHelper.clamp(oldProgress / 100f, 0f, 1f))
                        / max * duration), 0, duration);
            } else {
                growthProgress = 0;
            }
            growthStage = computeStage();
        }
    }

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

    private void markDirtyAndScheduleSync() {
        markDirty();
        pendingSync = true;
    }

    private void immediateSync() {
        markDirty();
        pendingSync = false;
        doSyncToClient();
    }

    private void doSyncToClient() {
        if (world != null && !world.isRemote) {
            IBlockState s = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, s, s, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }
}
