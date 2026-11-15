package com.meowmel.cropQT.api.potency;

/**
 * 「一份水 / 肥 / 除草剂能提供多少点数」的描述。
 *
 * <p>对应 CropsNH 的 {@code IPotencyData}。把三件事拆开：
 * <ol>
 *     <li>{@link #getPotency()} —— 每次施用加多少点</li>
 *     <li>{@link #getMaxStorage()} —— 这种来源允许把作物/机器的存储顶到多高（≤0 表示用机制默认值）</li>
 *     <li>{@link #getUnitsConsumedPerApplication()} —— 每次施用消耗几个单位（mB 或物品个数）</li>
 * </ol>
 *
 * <p>典型例子：普通水 {@code (1, 0, 1)} 表示 1 mB 加 1 点、无额外上限；
 * 骨粉 {@code (5, 30, 1)} 表示 1 个加 5 点、最多把存储顶到 30，与默认 200 取小后是 30。
 * 骨粉这类"低上限"来源是源端刻意做的：不能靠堆骨粉把作物喂到满。
 */
public interface IPotencyData extends Comparable<IPotencyData> {

    /** 每次施用增加的点数。 */
    int getPotency();

    /**
     * 这种来源允许的存储上限；≤0 表示沿用机制默认值（水 200、肥 200、WeedEX 150）。
     */
    int getMaxStorage();

    /**
     * 在机制已有上限 {@code defaultMax} 的前提下，实际可用的上限。
     * 自身上限 ≤0 时返回 {@code defaultMax}，否则取两者较小值。
     */
    default int getMaxStorage(int defaultMax) {
        if (getMaxStorage() <= 0) {
            return defaultMax;
        }
        return Math.min(defaultMax, getMaxStorage());
    }

    /** 每次施用消耗的单位数（mB 或物品个数），必须 &gt; 0。 */
    int getUnitsConsumedPerApplication();

    @Override
    default int compareTo(IPotencyData other) {
        int comp = Integer.compare(getPotency(), other.getPotency());
        if (comp != 0) {
            return comp;
        }
        comp = Integer.compare(getMaxStorage(), other.getMaxStorage());
        if (comp != 0) {
            return comp;
        }
        return Integer.compare(getUnitsConsumedPerApplication(), other.getUnitsConsumedPerApplication());
    }
}
