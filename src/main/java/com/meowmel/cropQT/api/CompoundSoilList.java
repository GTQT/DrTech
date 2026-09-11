package com.meowmel.cropQT.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 若干土壤组的并集（只读）。
 *
 * <p>用途：
 * <ul>
 *     <li>{@link SoilRegistry#getAllSoils()} —— 「任意土壤」，给不需要挑土的作物用</li>
 *     <li>把几个组打包成一个语义组，例如「所有石头类」= 石头 + 下界岩 + 末地石</li>
 * </ul>
 *
 * <p>容量取成员中的<b>最大值</b>：并集不该比它的任何成员更差。
 */
public class CompoundSoilList implements ISoilList {

    private final String name;
    private final List<ISoilList> members;

    public CompoundSoilList(String name, ISoilList... members) {
        this(name, Arrays.asList(members));
    }

    public CompoundSoilList(String name, Collection<? extends ISoilList> members) {
        this.name = name;
        this.members = new ArrayList<>(members);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean contains(Block block, int meta) {
        for (ISoilList member : members) {
            if (member.contains(block, meta)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getWaterCapacity() {
        int max = 0;
        for (ISoilList member : members) {
            max = Math.max(max, member.getWaterCapacity());
        }
        return max;
    }

    @Override
    public int getFertilizerCapacity() {
        int max = 0;
        for (ISoilList member : members) {
            max = Math.max(max, member.getFertilizerCapacity());
        }
        return max;
    }

    @Override
    public float getBaseNutrients() {
        float max = 0f;
        for (ISoilList member : members) {
            max = Math.max(max, member.getBaseNutrients());
        }
        return max;
    }

    @Override
    public int getWaterUsage(int cropTier) {
        return minUsage(cropTier, true);
    }

    @Override
    public int getFertilizerUsage(int cropTier) {
        return minUsage(cropTier, false);
    }

    /** 消耗取成员中的<b>最小值</b>：并集不该比它的任何成员更费水费肥。 */
    private int minUsage(int cropTier, boolean water) {
        int min = Integer.MAX_VALUE;
        for (ISoilList member : members) {
            int usage = water ? member.getWaterUsage(cropTier) : member.getFertilizerUsage(cropTier);
            min = Math.min(min, usage);
        }
        return min == Integer.MAX_VALUE ? 1 : min;
    }

    @Override
    public List<ItemStack> getDisplayItems() {
        List<ItemStack> items = new ArrayList<>();
        for (ISoilList member : members) {
            for (ItemStack stack : member.getDisplayItems()) {
                if (!containsStack(items, stack)) {
                    items.add(stack);
                }
            }
        }
        return Collections.unmodifiableList(items);
    }

    @Override
    public String toString() {
        return "CompoundSoilList[" + name + " x" + members.size() + "]";
    }

    private static boolean containsStack(List<ItemStack> items, ItemStack target) {
        for (ItemStack stack : items) {
            if (ItemStack.areItemsEqual(stack, target)) {
                return true;
            }
        }
        return false;
    }
}
