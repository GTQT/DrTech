package com.meowmel.cropQT.api.mutation;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 确定性杂交配方 + 随机变异池的全局注册表。
 *
 * <p>杂交时的查找顺序（照 CropsNH）：
 * <ol>
 *     <li>{@link #getPossibleDeterministicMutations} —— 命中的配方里按权重挑一个</li>
 *     <li>没命中就 {@link #getPossiblePools} —— 命中的池里随机挑一个，再从池里随机挑成员</li>
 *     <li>都没有就什么都不发生</li>
 * </ol>
 *
 * <p>查找是线性扫描。配方量级在几百条、每株作物每 256 tick 才查一次，
 * 建前缀树省下来的时间还不够维护它的成本——等真有几千条再谈优化。
 */
public final class MutationRegistry {

    private static final List<CropMutation> MUTATIONS = new ArrayList<>();
    private static final List<MutationPool> POOLS = new ArrayList<>();

    private MutationRegistry() {
    }

    // ==================== 登记 ====================

    public static void register(CropMutation mutation) {
        MUTATIONS.add(mutation);
    }

    /** 便捷写法：{@code register("reed", 80, "wheat", "pumpkin")}。 */
    public static void register(String result, int weight, String... parents) {
        register(new CropMutation(result, parents).weight(weight));
    }

    public static void register(MutationPool pool) {
        POOLS.add(pool);
    }

    public static List<CropMutation> getAllMutations() {
        return Collections.unmodifiableList(MUTATIONS);
    }

    public static List<MutationPool> getAllPools() {
        return Collections.unmodifiableList(POOLS);
    }

    public static boolean isEmpty() {
        return MUTATIONS.isEmpty() && POOLS.isEmpty();
    }

    // ==================== 查找 ====================

    /**
     * 所有父本都在场的配方，按权重挑一个。
     *
     * <p>权重用轮盘抽取。源端（CropsNH）是等概率挑，这里保留权重是为了不改动
     * 现有配方表的产出概率——把权重全设成同一个值即等价于等概率。
     *
     * @return 挑中的配方；没有命中的返回 {@code null}
     */
    @Nullable
    public static CropMutation pickDeterministic(List<String> participants, Random rand) {
        List<CropMutation> matched = new ArrayList<>();
        int totalWeight = 0;
        for (CropMutation mutation : MUTATIONS) {
            if (mutation.matches(participants)) {
                matched.add(mutation);
                totalWeight += mutation.getWeight();
            }
        }
        if (matched.isEmpty()) {
            return null;
        }

        int roll = rand.nextInt(totalWeight);
        for (CropMutation mutation : matched) {
            roll -= mutation.getWeight();
            if (roll < 0) {
                return mutation;
            }
        }
        return matched.get(matched.size() - 1);
    }

    /**
     * 所有命中的变异池。
     *
     * @return 命中的池；一个都没有时返回空表
     */
    public static List<MutationPool> getPossiblePools(List<String> participants) {
        List<MutationPool> matched = new ArrayList<>();
        for (MutationPool pool : POOLS) {
            if (pool.matches(participants)) {
                matched.add(pool);
            }
        }
        return matched;
    }

    /**
     * 从命中的池里随机挑一个成员。
     *
     * @return 作物 id；没有命中的池、或池是空的时返回 {@code null}
     */
    @Nullable
    public static String pickFromPools(List<String> participants, Random rand) {
        List<MutationPool> pools = getPossiblePools(participants);
        if (pools.isEmpty()) {
            return null;
        }
        return pools.get(rand.nextInt(pools.size())).pickRandom(rand);
    }
}
