package com.meowmel.cropQT.api;

import gregtech.api.unification.material.Material;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * {@link ISoilList} 的标准实现：按方块、矿物词典或 GT 材料登记成员。
 *
 * <p>匹配逻辑全在 {@link BlockMatcher} 里，本类只负责挂上「保水 / 保肥上限」这两个数值。
 *
 * <p>登记新成员会自动让 {@link SoilRegistry} 的反查缓存失效。
 *
 * @see SoilTypes 预定义好的常用土壤组
 */
public class SoilList implements ISoilList {

    private final String name;
    private final BlockMatcher matcher = new BlockMatcher();

    private int waterCapacity = 100;
    private int fertilizerCapacity = 100;
    private float baseNutrients = 0.2f;

    /**
     * 每生长周期的基础耗水量（tier 0 时）。
     *
     * <p>配 {@link SoilTypes} 里 250 ~ 4000 的保水上限，落在「沙地 1 分钟就得浇一次、
     * 耕地能撑二十来分钟」这个区间。1 个生长周期 = {@code GROWTH_CYCLE} = 256 tick ≈ 12.8 秒。
     */
    public static final int BASE_WATER_USAGE = 40;
    /** 每生长周期的基础耗肥量。比水慢——肥料是消耗品，不该逼玩家一直喂。 */
    public static final int BASE_FERTILIZER_USAGE = 10;
    /** tier 每高一级，消耗增加的比例。 */
    private static final float TIER_USAGE_STEP = 0.1f;

    public SoilList(String name) {
        this.name = name;
    }

    // ==================== 登记（全部返回 this 以便链式书写） ====================

    /** 登记一个方块，忽略其元数据（例如石头会同时覆盖花岗岩/闪长岩/安山岩）。 */
    public SoilList registerBlock(Block block) {
        matcher.addBlock(block);
        SoilRegistry.invalidateCache();
        return this;
    }

    /** 登记一个具体的「方块 + 元数据」组合。 */
    public SoilList registerBlock(Block block, int meta) {
        matcher.addBlock(block, meta);
        SoilRegistry.invalidateCache();
        return this;
    }

    /** {@link #registerBlock(Block, int)} 的便捷重载。 */
    public SoilList registerBlock(IBlockState state) {
        matcher.addBlock(state);
        SoilRegistry.invalidateCache();
        return this;
    }

    /** 登记一个矿物词典条目，例如 {@code "oreCopper"}。 */
    public SoilList registerOreDict(String oreDictName) {
        matcher.addOreDict(oreDictName);
        SoilRegistry.invalidateCache();
        return this;
    }

    /** 登记一种 GT 材料，命中该材料的任意形态。 */
    public SoilList registerMaterial(Material material) {
        matcher.addMaterial(material);
        SoilRegistry.invalidateCache();
        return this;
    }

    /** 保水上限。值越大，一次浇水撑得越久，储量加成也越容易吃满。 */
    public SoilList setWaterCapacity(int capacity) {
        this.waterCapacity = capacity;
        return this;
    }

    /** 保肥上限。 */
    public SoilList setFertilizerCapacity(int capacity) {
        this.fertilizerCapacity = capacity;
        return this;
    }

    /** 基础营养值（0~1）。不设则按贫瘠的 0.2 处理。 */
    public SoilList setBaseNutrients(float nutrients) {
        this.baseNutrients = nutrients;
        return this;
    }

    // ==================== ISoilList ====================

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean contains(Block block, int meta) {
        return matcher.matches(block, meta);
    }

    @Override
    public int getWaterCapacity() {
        return waterCapacity;
    }

    @Override
    public int getFertilizerCapacity() {
        return fertilizerCapacity;
    }

    @Override
    public float getBaseNutrients() {
        return baseNutrients;
    }

    @Override
    public int getWaterUsage(int cropTier) {
        return usage(BASE_WATER_USAGE, cropTier);
    }

    @Override
    public int getFertilizerUsage(int cropTier) {
        return usage(BASE_FERTILIZER_USAGE, cropTier);
    }

    private static int usage(int base, int cropTier) {
        return Math.max(1, Math.round(base * (1f + Math.max(0, cropTier) * TIER_USAGE_STEP)));
    }

    @Override
    public List<ItemStack> getDisplayItems() {
        return matcher.getDisplayItems();
    }

    @Override
    public String toString() {
        return "SoilList[" + name + ", water=" + waterCapacity + ", fert=" + fertilizerCapacity + "]";
    }
}
