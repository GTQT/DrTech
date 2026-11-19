package com.meowmel.cropQT.api.potency;

/**
 * {@link IPotencyData} 的不可变默认实现。
 *
 * <p>三个构造器分别对应「只要点数」「点数 + 存储上限」「点数 + 上限 + 每次消耗量」三种登记方式，
 * 单位数默认 1。负值不会在这里被拒绝 —— 由注册表在登记时校验，这样构造器可以自由用于
 * 内部计算/测试。
 */
public final class PotencyData implements IPotencyData {

    private final int potency;
    private final int maxStorage;
    private final int unitsConsumedPerApplication;

    /** 无额外上限，每次消耗 1 单位。 */
    public PotencyData(int potency) {
        this(potency, 0, 1);
    }

    /** 指定存储上限，每次消耗 1 单位。 */
    public PotencyData(int potency, int maxStorage) {
        this(potency, maxStorage, 1);
    }

    /** 完整参数。 */
    public PotencyData(int potency, int maxStorage, int unitsConsumedPerApplication) {
        this.potency = potency;
        this.maxStorage = maxStorage;
        this.unitsConsumedPerApplication = unitsConsumedPerApplication;
    }

    @Override
    public int getPotency() {
        return potency;
    }

    @Override
    public int getMaxStorage() {
        return maxStorage;
    }

    @Override
    public int getUnitsConsumedPerApplication() {
        return unitsConsumedPerApplication;
    }

    @Override
    public String toString() {
        return "PotencyData{" + potency + ", max=" + maxStorage + ", units=" + unitsConsumedPerApplication + '}';
    }
}
