package com.meowmel.cropQT.api;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Random;

/**
 * 作物三维属性: Growth(生长速度), Gain(产量), Resistance(抗性)
 * 范围1-31
 *
 * <p>附带一个 {@code analyzed} 标志：未分析的种子看不到具体属性，也不能进种子生成机。
 * 用分析仪右键种子袋即可分析，见 {@code ItemCropAnalyzer}。
 *
 * <p>它放在这里而不是单独一个类，是因为它和三维属性共用同一份 NBT——
 * 种子袋的 NBT 里就是 {@code cropId} + 这几个属性键。
 */
public class CropStats {
    public static final int MIN_STAT = 1;
    public static final int MAX_STAT = 31;
    public static final int WEED_THRESHOLD = 24;

    private int growth;
    private int gain;
    private int resistance;
    /** 是否已被分析。新品种子、杂交子代默认未分析。 */
    private boolean analyzed;

    public CropStats(int growth, int gain, int resistance) {
        this(growth, gain, resistance, false);
    }

    public CropStats(int growth, int gain, int resistance, boolean analyzed) {
        this.growth = clamp(growth);
        this.gain = clamp(gain);
        this.resistance = clamp(resistance);
        this.analyzed = analyzed;
    }

    public CropStats() { this(1, 1, 1); }

    /**
     * 多亲本属性遗传: 所有参与杂交的亲本取算术平均 ±2
     */
    public static CropStats inheritFrom(CropStats[] parents, Random rand) {
        int totalG = 0, totalGa = 0, totalR = 0;
        for (CropStats p : parents) {
            totalG += p.growth;
            totalGa += p.gain;
            totalR += p.resistance;
        }
        int n = parents.length;
        int g = clamp(totalG / n + rand.nextInt(5) - 2);
        int ga = clamp(totalGa / n + rand.nextInt(5) - 2);
        int r = clamp(totalR / n + rand.nextInt(5) - 2);
        return new CropStats(g, ga, r);
    }

    /**
     * 计算本生长轮的进度增量: random(0~3) + growth属性
     */
    public int rollGrowthIncrement(Random rand) {
        return rand.nextInt(4) + growth;
    }

    /**
     * 计算杂交参与概率(%)
     * 基础20%, gr>10则25%, gr>16则30%
     * re>16时每超1点扣5%
     */
    public int getCrossBreedChance() {
        int chance;
        if (growth > 16) chance = 30;
        else if (growth > 10) chance = 25;
        else chance = 20;

        if (resistance > 16) {
            chance -= (resistance - 16) * 5;
        }
        return Math.max(5, chance);
    }

    /**
     * 抵抗杂草: re越高越能抵抗，但无法完全阻止
     * 返回true表示本次抵抗成功
     */
    public boolean tryResistWeed(Random rand) {
        // 抵抗率 = re * 3%, 最高90%
        int resistChance = Math.min(90, resistance * 3);
        return rand.nextInt(100) < resistChance;
    }

    public int getYieldBonus(Random rand) {
        int bonus = gain / 8;
        if (rand.nextInt(MAX_STAT) < gain % 8) bonus++;
        return bonus;
    }

    public float getGrowthRateMultiplier() {
        return 0.5f + (growth / (float) MAX_STAT) * 1.5f;
    }

    // ==================== NBT ====================

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("statGrowth", growth);
        nbt.setInteger("statGain", gain);
        nbt.setInteger("statResistance", resistance);
        nbt.setBoolean("statAnalyzed", analyzed);
        return nbt;
    }

    public static CropStats readFromNBT(NBTTagCompound nbt) {
        return new CropStats(
                nbt.getInteger("statGrowth"),
                nbt.getInteger("statGain"),
                nbt.getInteger("statResistance"),
                nbt.getBoolean("statAnalyzed"));
    }

    public int getGrowth() { return growth; }
    public int getGain() { return gain; }
    public int getResistance() { return resistance; }

    /** 这袋种子是否已被分析过。未分析的种子不显示属性，也不能进种子生成机。 */
    public boolean isAnalyzed() { return analyzed; }

    /** 标记为已分析（就地修改并返回自身，便于链式调用）。 */
    public CropStats analyze() {
        this.analyzed = true;
        return this;
    }

    private static int clamp(int v) { return Math.max(MIN_STAT, Math.min(MAX_STAT, v)); }

    @Override
    public String toString() { return String.format("Gr:%d Ga:%d Re:%d", growth, gain, resistance); }
}
