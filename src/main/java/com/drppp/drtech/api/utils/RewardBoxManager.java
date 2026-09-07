package com.drppp.drtech.api.Utils;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 奖励箱管理类。
 * <p>
 * 维护一个静态的 Map，将奖励箱名称映射到对应的奖励表。
 * 奖励表中的每个条目包含一个 {@link ItemStack} 和一个概率值 (0-100)。
 * 提供方法按概率随机抽取一个物品。
 */
public class RewardBoxManager {

    private static final Map<String, List<RewardEntry>> REWARD_BOXES = new ConcurrentHashMap<>();

    private RewardBoxManager() {
    }

    // ========== 注册 / 查询 ==========

    /**
     * 注册一个奖励表。若 name 已存在则覆盖。
     */
    public static void registerRewardBox(@Nonnull String name, @Nonnull List<RewardEntry> entries) {
        REWARD_BOXES.put(name, new ArrayList<>(entries));
    }

    /**
     * 获取指定奖励表的不可修改视图，若不存在返回 null。
     */
    @Nullable
    public static List<RewardEntry> getRewardTable(@Nonnull String name) {
        List<RewardEntry> entries = REWARD_BOXES.get(name);
        return entries != null ? Collections.unmodifiableList(entries) : null;
    }

    /**
     * 判断指定名称的奖励表是否已注册。
     */
    public static boolean hasRewardBox(@Nonnull String name) {
        return REWARD_BOXES.containsKey(name);
    }

    /**
     * 获取所有已注册的奖励箱名称。
     */
    @Nonnull
    public static Map<String, List<RewardEntry>> getAllRewardBoxes() {
        return Collections.unmodifiableMap(REWARD_BOXES);
    }

    // ========== 随机抽取 ==========

    /**
     * 从指定奖励表中按概率随机抽取一个物品。
     *
     * @param name 奖励表名称
     * @return 随机抽取到的物品副本，若表不存在或没有可抽取的物品则返回 {@link ItemStack#EMPTY}
     */
    @Nonnull
    public static ItemStack roll(@Nonnull String name) {
        List<RewardEntry> table = REWARD_BOXES.get(name);
        if (table == null || table.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return roll(table);
    }

    /**
     * 从给定的奖励列表中按概率随机抽取一个物品。
     *
     * @param entries 奖励列表
     * @return 随机抽取到的物品副本，若列表为空则返回 {@link ItemStack#EMPTY}
     */
    @Nonnull
    public static ItemStack roll(@Nonnull List<RewardEntry> entries) {
        if (entries.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int totalWeight = entries.stream().mapToInt(RewardEntry::getProbability).sum();
        if (totalWeight <= 0) {
            return ItemStack.EMPTY;
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        for (RewardEntry entry : entries) {
            cumulative += entry.getProbability();
            if (roll < cumulative) {
                return entry.getItemStack().copy();
            }
        }
        return entries.get(entries.size() - 1).getItemStack().copy();
    }

    /**
     * 从指定奖励表中按概率随机抽取多个物品（每轮独立抽取）。
     *
     * @param name 奖励表名称
     * @param times 抽取次数
     * @return 抽取到的物品列表
     */
    @Nonnull
    public static List<ItemStack> roll(@Nonnull String name, int times) {
        List<ItemStack> results = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            ItemStack stack = roll(name);
            if (!stack.isEmpty()) {
                results.add(stack);
            }
        }
        return results;
    }

    /**
     * 清空所有注册的奖励表（供 reload 等场景使用）。
     */
    public static void clear() {
        REWARD_BOXES.clear();
    }
}
