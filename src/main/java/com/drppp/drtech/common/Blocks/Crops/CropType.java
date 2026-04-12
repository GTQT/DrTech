package com.drppp.drtech.common.Blocks.Crops;

import net.minecraft.item.ItemStack;

import java.util.*;

/**
 * 定义一种作物类型的所有属性
 */

public class CropType {
    private final String id;
    private final String displayName;
    private final int tier;
    private final int maxGrowthStage;
    private final int harvestStage;
    private final int stageRequirement;
    private final List<ItemStack> drops;           // 固定掉落(可后续追加)
    private final List<ChanceDrop> chanceDrops;    // 概率掉落(可后续追加)
    private final String lootTable;
    private final String[] requiredBlocks;
    private final float lightRequirement;
    private final float waterRequirement;
    private final CropRenderType renderType;
    private final boolean canBeBreedResult;
    private final CompareMode lightCompare;
    private final float lightRequirementMax;  // RANGE模式的上限
    private final CompareMode humidityCompare;
    private final float waterRequirementMax;  // RANGE模式的上限
    private final Map<String, List<ItemStack>> blockDrops;        // 方块ID → 特定掉落
    private final Map<String, List<ChanceDrop>> blockChanceDrops; // 方块ID → 特定概率掉落

    private CropType(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.tier = builder.tier;
        this.maxGrowthStage = builder.maxGrowthStage;
        this.harvestStage = builder.harvestStage;
        this.stageRequirement = builder.stageRequirement;
        this.drops = new ArrayList<>(builder.drops);          // 可变副本
        this.chanceDrops = new ArrayList<>(builder.chanceDrops); // 可变副本
        this.lootTable = builder.lootTable;
        this.requiredBlocks = builder.requiredBlocks;
        this.lightRequirement = builder.lightRequirement;
        this.waterRequirement = builder.waterRequirement;
        this.renderType = builder.renderType;
        this.canBeBreedResult = builder.canBeBreedResult;
        this.lightCompare = builder.lightCompare;
        this.lightRequirementMax = builder.lightRequirementMax;
        this.humidityCompare = builder.humidityCompare;
        this.waterRequirementMax = builder.waterRequirementMax;
        this.blockDrops = new HashMap<>(builder.blockDrops);
        this.blockChanceDrops = new HashMap<>(builder.blockChanceDrops);
    }

    // ==================== 后续追加掉落物(init阶段使用) ====================
    /** 注册后追加方块特定掉落(init阶段使用) */
    public CropType addBlockDropLate(String blockId, ItemStack item) {
        this.blockDrops.computeIfAbsent(blockId, k -> new ArrayList<>()).add(item);
        return this;
    }

    public CropType addBlockChanceDropLate(String blockId, ItemStack item, float chance) {
        this.blockChanceDrops.computeIfAbsent(blockId, k -> new ArrayList<>())
                .add(new ChanceDrop(item, chance));
        return this;
    }
    /**
     * 注册后追加固定掉落物。
     * 用于init阶段添加依赖其他mod的物品(如GTCEU MetaItems)。
     * preInit阶段这些物品可能还未初始化。
     */
    public CropType addDropLate(ItemStack item) {
        this.drops.add(item);
        return this;
    }

    /**
     * 注册后追加概率掉落物。
     */
    public CropType addChanceDropLate(ItemStack item, float chance) {
        this.chanceDrops.add(new ChanceDrop(item, chance));
        return this;
    }

    // ==================== 掉落计算 ====================

    /**
     * 获取本次收获的所有掉落物(固定+概率)
     */
    public List<ItemStack> rollDrops(Random rand, int gainBonus) {
        List<ItemStack> result = new ArrayList<>();
        // 固定掉落
        for (ItemStack drop : drops) {
            ItemStack copy = drop.copy();
            copy.setCount(copy.getCount() + gainBonus);
            result.add(copy);
        }
        // 概率掉落
        for (ChanceDrop cd : chanceDrops) {
            if (rand.nextFloat() < cd.chance) {
                ItemStack copy = cd.item.copy();
                result.add(copy);
            }
        }
        return result;
    }
    /**
     * 获取本次收获的所有掉落物(固定+概率)
     * @param blocksBelowIds 下方方块ID列表(由EnvironmentCalculator提供)
     */
    public List<ItemStack> rollDrops(Random rand, int gainBonus, List<String> blocksBelowIds) {
        List<ItemStack> result = new ArrayList<>();

        // 检查是否有方块特定掉落
        boolean matchedBlock = false;
        if (blocksBelowIds != null && !blockDrops.isEmpty()) {
            for (String blockId : blocksBelowIds) {
                // 精确匹配(带meta)
                if (blockDrops.containsKey(blockId)) {
                    for (ItemStack drop : blockDrops.get(blockId)) {
                        ItemStack copy = drop.copy();
                        copy.setCount(copy.getCount() + gainBonus);
                        result.add(copy);
                    }
                    if (blockChanceDrops.containsKey(blockId)) {
                        for (ChanceDrop cd : blockChanceDrops.get(blockId)) {
                            if (rand.nextFloat() < cd.chance) result.add(cd.item.copy());
                        }
                    }
                    matchedBlock = true;
                    break;
                }
                // 不带meta匹配
                String noMeta = blockId.contains(":") ?
                        blockId.substring(0, blockId.lastIndexOf(':')) : blockId;
                if (blockDrops.containsKey(noMeta)) {
                    for (ItemStack drop : blockDrops.get(noMeta)) {
                        ItemStack copy = drop.copy();
                        copy.setCount(copy.getCount() + gainBonus);
                        result.add(copy);
                    }
                    if (blockChanceDrops.containsKey(noMeta)) {
                        for (ChanceDrop cd : blockChanceDrops.get(noMeta)) {
                            if (rand.nextFloat() < cd.chance) result.add(cd.item.copy());
                        }
                    }
                    matchedBlock = true;
                    break;
                }
            }
        }

        // 没有匹配到特定方块 → 使用默认掉落
        if (!matchedBlock) {
            for (ItemStack drop : drops) {
                ItemStack copy = drop.copy();
                copy.setCount(copy.getCount() + gainBonus);
                result.add(copy);
            }
            for (ChanceDrop cd : chanceDrops) {
                if (rand.nextFloat() < cd.chance) result.add(cd.item.copy());
            }
        }

        return result;
    }
    // ==================== Getters ====================

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getTier() { return tier; }
    public int getMaxGrowthStage() { return maxGrowthStage; }
    public int getHarvestStage() { return harvestStage; }
    public int getStageRequirement() { return stageRequirement; }
    public List<ItemStack> getDrops() { return drops; }
    public List<ChanceDrop> getChanceDrops() { return chanceDrops; }
    public String getLootTable() { return lootTable; }
    public String[] getRequiredBlocks() { return requiredBlocks; }
    public float getLightRequirement() { return lightRequirement; }
    public float getWaterRequirement() { return waterRequirement; }
    public CropRenderType getRenderType() { return renderType; }
    public boolean canBeBreedResult() { return canBeBreedResult; }
    public CompareMode getLightCompare() { return lightCompare; }
    public float getLightRequirementMax() { return lightRequirementMax; }
    public CompareMode getHumidityCompare() { return humidityCompare; }
    public float getWaterRequirementMax() { return waterRequirementMax; }
    public Map<String, List<ItemStack>> getBlockDrops() { return blockDrops; }
    public Map<String, List<ChanceDrop>> getBlockChanceDrops() { return blockChanceDrops; }

    public boolean canGrowAt(float light, float humidity, List<String> blocksBelowIds) {
        if (!checkValue(light, lightRequirement, lightRequirementMax, lightCompare)) return false;
        if (!checkValue(humidity, waterRequirement, waterRequirementMax, humidityCompare)) return false;
        if (requiredBlocks != null && requiredBlocks.length > 0) {
            for (String req : requiredBlocks) {
                for (String actual : blocksBelowIds) {
                    if (actual.equals(req)) return true;
                }
            }
            return false;
        }
        return true;
    }

    private static boolean checkValue(float actual, float min, float max, CompareMode mode) {
        switch (mode) {
            case LESS:    return actual <= min;
            case RANGE:   return actual >= min && actual <= max;
            case GREATER:
            default:      return actual >= min;
        }
    }

    // ==================== 概率掉落数据 ====================

    public static class ChanceDrop {
        public final ItemStack item;
        public final float chance; // 0.0~1.0

        public ChanceDrop(ItemStack item, float chance) {
            this.item = item;
            this.chance = chance;
        }
    }

    // ==================== Builder ====================

    public static class Builder {
        private final String id;
        private String displayName;
        private int tier = 1;
        private int maxGrowthStage = 7;
        private int harvestStage = 7;
        private int stageRequirement = 20;
        private List<ItemStack> drops = new ArrayList<>();
        private List<ChanceDrop> chanceDrops = new ArrayList<>();
        private String lootTable = null;
        private String[] requiredBlocks = new String[0];
        private float lightRequirement = 9;
        private float waterRequirement = 0;
        private CropRenderType renderType = CropRenderType.CROSS;
        private boolean canBeBreedResult = true;
        private CompareMode lightCompare = CompareMode.GREATER;
        private float lightRequirementMax = 15;
        private CompareMode humidityCompare = CompareMode.GREATER;
        private float waterRequirementMax = 1.0f;
        private Map<String, List<ItemStack>> blockDrops = new HashMap<>();
        private Map<String, List<ChanceDrop>> blockChanceDrops = new HashMap<>();

        public Builder(String id) { this.id = id; this.displayName = id; }

        public Builder displayName(String name) { this.displayName = name; return this; }
        public Builder tier(int tier) { this.tier = tier; return this; }
        public Builder maxGrowthStage(int s) { this.maxGrowthStage = s; return this; }
        public Builder harvestStage(int s) { this.harvestStage = s; return this; }
        public Builder stageRequirement(int r) { this.stageRequirement = r; return this; }
        public Builder addDrop(ItemStack s) { this.drops.add(s); return this; }


        /**
         * 底下放特定方块时产出特定物品
         * 例: .addBlockDrop("minecraft:iron_block", new ItemStack(Items.IRON_INGOT))
         *     .addBlockDrop("gregtech:meta_block_compressed_3:7", leadDust)
         */
        public Builder addBlockDrop(String blockId, ItemStack item) {
            this.blockDrops.computeIfAbsent(blockId, k -> new ArrayList<>()).add(item);
            return this;
        }

        /** 底下放特定方块时的概率掉落 */
        public Builder addBlockChanceDrop(String blockId, ItemStack item, float chance) {
            this.blockChanceDrops.computeIfAbsent(blockId, k -> new ArrayList<>())
                    .add(new ChanceDrop(item, chance));
            return this;
        }
        /** 概率掉落: chance范围0.0~1.0 */
        public Builder addChanceDrop(ItemStack item, float chance) {
            this.chanceDrops.add(new ChanceDrop(item, chance));
            return this;
        }
        /** 从战利品表获取掉落 */
        public Builder lootTable(String table) { this.lootTable = table; return this; }
        public Builder requiredBlocks(String... b) { this.requiredBlocks = b; return this; }
        public Builder lightRequirement(float l) { this.lightRequirement = l; return this; }
        public Builder waterRequirement(float w) { this.waterRequirement = w; return this; }
        public Builder renderType(CropRenderType t) { this.renderType = t; return this; }
        /** 设为false则不允许通过杂交产出此作物 */
        public Builder canBeBreedResult(boolean v) { this.canBeBreedResult = v; return this; }
        /** 光照小于等于某值 (蘑菇/暗处作物) */
        public Builder lightRequirementLess(float max) {
            this.lightRequirement = max;
            this.lightCompare = CompareMode.LESS;
            return this;
        }
        /** 光照在范围内 */
        public Builder lightRequirementRange(float min, float max) {
            this.lightRequirement = min;
            this.lightRequirementMax = max;
            this.lightCompare = CompareMode.RANGE;
            return this;
        }
        /** 湿度小于等于某值 (干旱作物) */
        public Builder waterRequirementLess(float max) {
            this.waterRequirement = max;
            this.humidityCompare = CompareMode.LESS;
            return this;
        }
        /** 湿度在范围内 */
        public Builder waterRequirementRange(float min, float max) {
            this.waterRequirement = min;
            this.waterRequirementMax = max;
            this.humidityCompare = CompareMode.RANGE;
            return this;
        }
        public CropType build() { return new CropType(this); }
    }
}
