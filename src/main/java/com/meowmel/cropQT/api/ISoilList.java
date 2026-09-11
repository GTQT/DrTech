package com.meowmel.cropQT.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * 一个具名土壤组。
 *
 * <p>土壤是作物架<b>直接踩着的那一格</b>（y-1）。它决定三件事：
 * 作物能不能种在这里、保水上限、保肥上限。
 *
 * <p>别和「底土」搞混——底土是再往下一格（y-2，见 {@link SubSoilRequirement}）。
 * 两者是独立概念：<b>土壤管生存条件，底土管矿物来源</b>。
 *
 * @see SoilRegistry
 * @see SoilTypes
 */
public interface ISoilList {

    /** 内部名，同时是 lang key 的一部分（{@code cropqt.soil.<name>}）。 */
    String getName();

    /** 该方块（含元数据）是否属于本土壤组。 */
    boolean contains(Block block, int meta);

    /** {@link #contains(Block, int)} 的便捷重载。 */
    default boolean contains(IBlockState state) {
        Block block = state.getBlock();
        return contains(block, block.getMetaFromState(state));
    }

    /** 保水上限，0 表示完全不保水。 */
    int getWaterCapacity();

    /** 保肥上限，0 表示完全不保肥。 */
    int getFertilizerCapacity();

    /**
     * 基础营养值（0~1）。作物架脚下的土壤有多肥。
     *
     * <p>直接进环境综合分（权重 0.35），所以它是「这块地天生好不好」，
     * 与保水/保肥那种「能存多少」是两回事。
     */
    float getBaseNutrients();

    /**
     * 每生长周期消耗的水量。
     *
     * <p>由作物 tier 决定消耗速度；土壤的保水上限决定「一箱水能撑几轮」。
     * 两者相乘才是玩家实际感受到的「多久浇一次水」。
     *
     * @param cropTier 作物 tier，越高喝得越多
     */
    int getWaterUsage(int cropTier);

    /** 每生长周期消耗的肥料量，语义同 {@link #getWaterUsage}。 */
    int getFertilizerUsage(int cropTier);

    /** 用于 JEI 展示的代表性物品。 */
    List<ItemStack> getDisplayItems();
}
