package com.meowmel.cropQT.api;

import gregtech.api.unification.material.info.MaterialIconType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 定义一种作物类型的所有属性
 *
 * <p>「种在哪」由两个独立字段决定，别搞混：
 * <ul>
 *     <li>{@link #soilTypes} —— 土壤，作物架<b>直接踩着的那一格</b>（y-1）。决定能不能种、保水保肥上限。</li>
 *     <li>{@link #subSoilRequirement} —— 底土，再往下一格（y-2）。决定矿物来源，可为 {@code null}。</li>
 * </ul>
 */

public class CropType {
    private final String id;
    private final String displayName;
    private final String texturePath; // 自定义作物贴图目录，null则使用id
    private final int seedColor;      // 种子袋染色RGB，0xFFFFFF为不着色
    private final MaterialIconType seedIcon; // 种子材质；null 则用默认种子模型
    private final int tier;
    private final int maxGrowthStage;
    private final int harvestStage;
    private final int stageRequirement;
    private final List<ItemStack> drops;           // 固定掉落(可后续追加)
    private final List<ChanceDrop> chanceDrops;    // 概率掉落(可后续追加)
    private final String lootTable;
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

    // ==================== 种植条件 ====================

    /** 允许的土壤组。默认 {@link SoilTypes#farmland}。 */
    private final ISoilList soilTypes;
    /** 底土要求；{@code null} 表示不挑底土。 */
    private final SubSoilRequirement subSoilRequirement;
    /** 偏好的生物群系标签，影响环境分。空集表示不挑。 */
    private final Set<BiomeDictionary.Type> likedBiomes;
    /** 完整生长一轮的时长（tick）。{@code <= 0} 表示按 tier 推导。 */
    private final int growthDuration;
    /** 参与杂交所需的最低生长进度（0~1）；{@code < 0} 表示禁止杂交。 */
    private final float crossingThreshold;
    /** 作为亲本被其它作物杂交所需的最低生长进度（0~1）；{@code < 0} 表示禁止。 */
    private final float breedingThreshold;

    private CropType(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.texturePath = builder.texturePath;
        this.seedColor = builder.seedColor;
        this.seedIcon = builder.seedIcon;
        this.tier = builder.tier;
        this.maxGrowthStage = builder.maxGrowthStage;
        this.harvestStage = builder.harvestStage;
        this.stageRequirement = builder.stageRequirement;
        this.drops = new ArrayList<>(builder.drops);          // 可变副本
        this.chanceDrops = new ArrayList<>(builder.chanceDrops); // 可变副本
        this.lootTable = builder.lootTable;
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
        this.soilTypes = builder.soilTypes;
        this.subSoilRequirement = builder.subSoilRequirement;
        this.likedBiomes = Collections.unmodifiableSet(new HashSet<>(builder.likedBiomes));
        this.growthDuration = builder.growthDuration;
        this.crossingThreshold = builder.crossingThreshold;
        this.breedingThreshold = builder.breedingThreshold;
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
    /**
     * 显示名。
     *
     * <p>存进去的通常是一个 lang key（生成的作物用 {@code cropqt.crop.<id>.name}），
     * 这里查一次翻译。查不到会原样返回，所以直接塞中文名的手写作物也不受影响。
     *
     * <p>用 {@code net.minecraft.util.text.translation.I18n} 而不是 client 包那个 ——
     * TOP 的 provider 跑在服务端，用 client 版会 {@code NoClassDefFoundError}。
     */
    public String getDisplayName() {
        return net.minecraft.util.text.translation.I18n.translateToLocal(displayName);
    }
    /** 作物贴图目录路径，未设定则返回id */
    public String getTexturePath() { return texturePath != null ? texturePath : id; }
    /** 种子袋染色RGB，未设定返回0xFFFFFF(不着色) */
    public int getSeedColor() { return seedColor; }
    /** 种子材质；未设定返回 null（用默认种子模型）。 */
    @Nullable public MaterialIconType getSeedIcon() { return seedIcon; }
    public int getTier() { return tier; }
    public int getMaxGrowthStage() { return maxGrowthStage; }
    public int getHarvestStage() { return harvestStage; }
    public int getStageRequirement() { return stageRequirement; }
    public List<ItemStack> getDrops() { return drops; }
    public List<ChanceDrop> getChanceDrops() { return chanceDrops; }
    public String getLootTable() { return lootTable; }
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

    // ==================== 种植条件 ====================

    /**
     * 允许的土壤组。
     *
     * <p>返回 {@code null} 表示<b>不限土壤</b>——这是默认值，也是迁移期的安全默认：
     * 一个作物没显式声明土壤就不该被限制，否则老存档里的农场会突然种不下去。
     */
    @Nullable
    public ISoilList getSoilTypes() { return soilTypes; }

    /** 底土要求；没有则返回 {@code null}。 */
    public SubSoilRequirement getSubSoilRequirement() { return subSoilRequirement; }

    /** 是否要求特定底土。 */
    public boolean hasSubSoilRequirement() { return subSoilRequirement != null; }

    /** 偏好的生物群系标签，只读。 */
    public Set<BiomeDictionary.Type> getLikedBiomes() { return likedBiomes; }

    /**
     * 完整生长一轮的时长（tick）。
     *
     * <p>没显式设定时按 {@code 600 * tier} 推导——机器（育种机 / 工业农场）用它算周期，
     * 世界里的作物架走的是 {@link #getStageRequirement()} 那套逐阶段进度。
     */
    public int getGrowthDuration() {
        return growthDuration > 0 ? growthDuration : 600 * tier;
    }

    /** 参与杂交所需的最低生长进度（0~1）；返回负数表示该作物不能主动杂交。 */
    public float getCrossingThreshold() { return crossingThreshold; }

    /** 作为亲本被杂交所需的最低生长进度（0~1）；返回负数表示不能被杂交。 */
    public float getBreedingThreshold() { return breedingThreshold; }

    /**
     * 光照与湿度是否达标。
     *
     * <p><b>底土要求不在这里判</b>——底土要看真实方块，需要 {@link World} 与坐标，
     * 见 {@link SubSoilRequirement#isMet} 与 {@code TileCropStick} 的生长逻辑。
     * 本方法只需要光照与湿度两个标量。
     */
    public boolean canGrowAt(float light, float humidity) {
        if (!checkValue(light, lightRequirement, lightRequirementMax, lightCompare)) return false;
        return checkValue(humidity, waterRequirement, waterRequirementMax, humidityCompare);
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
        private String texturePath = null;    // 自定义贴图目录
        private int seedColor = 0xFFFFFF;    // 种子袋染色RGB
        private MaterialIconType seedIcon = null; // 种子材质

        private ISoilList soilTypes = null;   // null = 不限土壤
        private SubSoilRequirement subSoilRequirement = null;
        private Set<BiomeDictionary.Type> likedBiomes = new HashSet<>();
        private int growthDuration = -1;         // <=0 表示按 tier 推导
        private float crossingThreshold = 0.8f;
        private float breedingThreshold = 0.8f;

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
        /** 接受Block实例(自动取defaultState转带meta的方块ID) */
        public Builder addBlockDrop(Block block, ItemStack item) {
            return addBlockDrop(block.getDefaultState(), item);
        }
        /** 接受IBlockState实例，自动转为带meta的方块ID */
        public Builder addBlockDrop(IBlockState state, ItemStack item) {
            String id = blockStateToId(state);
            if (!id.isEmpty()) this.blockDrops.computeIfAbsent(id, k -> new ArrayList<>()).add(item);
            return this;
        }

        /** 底下放特定方块时的概率掉落 */
        public Builder addBlockChanceDrop(String blockId, ItemStack item, float chance) {
            this.blockChanceDrops.computeIfAbsent(blockId, k -> new ArrayList<>())
                    .add(new ChanceDrop(item, chance));
            return this;
        }
        /** 接受Block实例的概率掉落 */
        public Builder addBlockChanceDrop(Block block, ItemStack item, float chance) {
            return addBlockChanceDrop(block.getDefaultState(), item, chance);
        }
        /** 接受IBlockState实例的概率掉落 */
        public Builder addBlockChanceDrop(IBlockState state, ItemStack item, float chance) {
            String id = blockStateToId(state);
            if (!id.isEmpty()) this.blockChanceDrops.computeIfAbsent(id, k -> new ArrayList<>())
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

        private static String blockStateToId(IBlockState state) {
            Block block = state.getBlock();
            ResourceLocation rl = Block.REGISTRY.getNameForObject(block);
            if (rl == null) return "";
            int meta = block.getMetaFromState(state);
            return rl.toString() + ":" + meta;
        }
        public Builder lightRequirement(float l) { this.lightRequirement = l; return this; }
        public Builder waterRequirement(float w) { this.waterRequirement = w; return this; }
        public Builder renderType(CropRenderType t) { this.renderType = t; return this; }
        /** 自定义作物贴图目录名(默认使用cropId) — 影响作物架TESR */
        public Builder texturePath(String path) { this.texturePath = path; return this; }
        /** 自定义种子袋贴图分组键(如 "oreberry")，指向 models/item/crop_seed_<key>.json，多个作物可共享 */
        /** 种子染色(如 Materials.Silver.materialRGB)，给灰度种子模型上色 */
        public Builder seedColor(int rgb) { this.seedColor = rgb; return this; }

        /**
         * 种子材质。
         *
         * <p>设了就指向材质模型 {@code material_sets/<图标集>/<类型名>}，
         * 不用再维护一份中间模型。
         */
        public Builder seedIcon(MaterialIconType icon) { this.seedIcon = icon; return this; }
        /** 设为false则不允许通过杂交产出此作物 */
        public Builder canBeBreedResult(boolean v) { this.canBeBreedResult = v; return this; }

        // ==================== 种植条件 ====================

        /**
         * 允许的土壤组（作物架直接踩着的那一格）。
         * 不设表示不限土壤。
         */
        public Builder soil(ISoilList soil) { this.soilTypes = soil; return this; }

        /**
         * 底土要求（再往下一格）。
         * 例：{@code .subSoil(SubSoilRequirements.copper)} 要求下方第二格是铜。
         */
        public Builder subSoil(SubSoilRequirement requirement) { this.subSoilRequirement = requirement; return this; }

        /** 追加一个偏好的生物群系标签。 */
        public Builder likedBiome(BiomeDictionary.Type type) { this.likedBiomes.add(type); return this; }

        /** 批量设置偏好的生物群系标签。 */
        public Builder likedBiomes(BiomeDictionary.Type... types) {
            this.likedBiomes.addAll(Arrays.asList(types));
            return this;
        }

        /** 完整生长一轮的时长（tick）。不设则按 {@code 600 * tier} 推导。 */
        public Builder growthDuration(int ticks) { this.growthDuration = ticks; return this; }

        /** 参与杂交所需的最低生长进度（0~1）；传负数表示禁止主动杂交。 */
        public Builder crossingThreshold(float v) { this.crossingThreshold = v; return this; }

        /** 作为亲本被杂交所需的最低生长进度（0~1）；传负数表示不能被杂交。 */
        public Builder breedingThreshold(float v) { this.breedingThreshold = v; return this; }
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
