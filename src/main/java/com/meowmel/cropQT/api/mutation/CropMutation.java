package com.meowmel.cropQT.api.mutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 一条确定性杂交配方：列出的父本都在场时，就能产出 {@link #getResult()}。
 *
 * <p><b>匹配语义是「父本都在场」而不是「父本集合完全相等」</b>——四株邻作物同时在场时，
 * 其中任意两三株的配方都能参与。这与旧实现的「两两组合查表」一致，
 * 也是搬配方时能保证「同样父本组合 → 同样产物」的前提。
 *
 * <p>父本允许重复：旧表里就有 {@code stickreed × stickreed → ferru} 这种同种配方。
 * 匹配按<b>多重集</b>判定，所以两株粘性甘蔗能命中它、一株不行。
 *
 * <p>{@link #getWeight()} 参与选择时的轮盘权重。源端（CropsNH）的选择是等概率的，
 * 这里保留权重是为了不改变现有配方表的手感——把所有权重都设成同一个值即等价于等概率。
 */
public class CropMutation {

    /** 父本数量的上下界。 */
    public static final int MIN_PARENTS = 2;
    public static final int MAX_PARENTS = 4;

    /** 未显式指定时的权重。 */
    public static final int DEFAULT_WEIGHT = 1;

    private final String result;
    private final List<String> parents;
    private int weight = DEFAULT_WEIGHT;

    /**
     * @param result  产物作物 id
     * @param parents 父本作物 id，2~4 个
     */
    public CropMutation(String result, String... parents) {
        if (parents.length < MIN_PARENTS || parents.length > MAX_PARENTS) {
            throw new IllegalArgumentException(
                    "杂交配方的父本数量必须在 " + MIN_PARENTS + "~" + MAX_PARENTS + " 之间，收到 " + parents.length);
        }
        this.result = result;
        this.parents = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(parents)));
    }

    /** 设置权重（链式）。 */
    public CropMutation weight(int weight) {
        this.weight = Math.max(1, weight);
        return this;
    }

    /**
     * 这组参与者能否触发本配方。
     *
     * <p>按多重集判定：每个父本都要在参与者里找到对应的那一份。
     */
    public boolean matches(List<String> participants) {
        if (participants.size() < parents.size()) {
            return false;
        }
        List<String> remaining = new ArrayList<>(participants);
        for (String parent : parents) {
            if (!remaining.remove(parent)) {
                return false;
            }
        }
        return true;
    }

    public String getResult() {
        return result;
    }

    public List<String> getParents() {
        return parents;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return String.join(" × ", parents) + " → " + result + " (w=" + weight + ")";
    }
}
