package com.drppp.drtech.intergations.jei;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 单个物品的掉落信息（语义对应 JER 的 LootDrop，由 MobLootAnalyzer 确定性分析战利品表得出）。
 * 注意本类在分析过程中是可变的（min/max/chance/item 会被 conditions/functions 改写）。
 */
public class LootDropInfo implements Comparable<LootDropInfo> {

    public ItemStack item;
    public ItemStack smeltedItem;
    public int minDrop = 1, maxDrop = 1;
    /** 掉落概率（0~1 权重占比，RandomChance 类条件会直接改写它） */
    public float chance = 1F;
    public boolean enchanted;
    private final EnumSet<DropCondition> conditions = EnumSet.noneOf(DropCondition.class);
    private final float sortIndex;

    public LootDropInfo(ItemStack item, float chance) {
        this.item = item;
        this.chance = chance;
        // 与 JER 的 LootDrop(item, chance) 构造一致：无数量函数时 min=floor(chance)、max=ceil(chance)
        this.minDrop = (int) Math.floor(chance);
        this.maxDrop = (int) Math.ceil(chance);
        this.sortIndex = Math.min(chance, 1F) * (float) (minDrop + maxDrop);
    }

    public void addCondition(DropCondition condition) {
        this.conditions.add(condition);
    }

    public EnumSet<DropCondition> getConditions() {
        return conditions;
    }

    public boolean canBeCooked() {
        return smeltedItem != null;
    }

    /** 参与 JEI 网格展示的物品（本体 + 烤制产物，若存在） */
    public List<ItemStack> getDrops() {
        List<ItemStack> list = new ArrayList<>(2);
        if (item != null) list.add(item);
        if (smeltedItem != null) list.add(smeltedItem);
        return list;
    }

    /**
     * tooltip 首行文本，格式照 JER LootDrop.toString：
     * "1-3" 或 "1-3 (8.1%)"；<10% 保留一位小数，>=10% 取整。
     */
    public String toStringLine() {
        String base = minDrop == maxDrop ? String.valueOf(minDrop) : minDrop + "-" + maxDrop;
        if (chance < 1F) {
            float pct = chance * 100F;
            String s = pct < 10 ? String.format("%.1f", pct) : String.format("%2d", (int) pct);
            return base + " (" + s + "%)";
        }
        return base;
    }

    /** 排序键（越大排越前）：min(chance,1) * (min+max)，与 JER 相同 */
    private float getSortIndex() {
        return sortIndex;
    }

    @Override
    public int compareTo(@Nonnull LootDropInfo o) {
        if (ItemStack.areItemStacksEqual(item, o.item)) {
            return 0;
        }
        int cmp = Float.compare(o.getSortIndex(), getSortIndex());
        return cmp != 0 ? cmp : item.getDisplayName().compareTo(o.item.getDisplayName());
    }
}
