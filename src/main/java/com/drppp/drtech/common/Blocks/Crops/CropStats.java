package com.drppp.drtech.common.Blocks.Crops;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Random;

/**
 * 作物三维属性: Growth(生长速度), Gain(产量), Resistance(抗性)
 * 范围1-31
 */
public class CropStats {
    public static final int MIN_STAT = 1;
    public static final int MAX_STAT = 31;
    public static final int WEED_THRESHOLD = 24;

    private int growth;
    private int gain;
    private int resistance;

    public CropStats(int growth, int gain, int resistance) {
        this.growth = clamp(growth);
        this.gain = clamp(gain);
        this.resistance = clamp(resistance);
    }

    public CropStats() {
        this(1, 1, 1);
    }

    // ==================== 属性遗传 ====================

    public static CropStats inherit(CropStats parentA, CropStats parentB, Random rand) {
        int g = inheritStat(parentA.growth, parentB.growth, rand);
        int ga = inheritStat(parentA.gain, parentB.gain, rand);
        int r = inheritStat(parentA.resistance, parentB.resistance, rand);
        return new CropStats(g, ga, r);
    }

    public static CropStats inheritMultiple(CropStats[] parents, Random rand) {
        int totalG = 0, totalGa = 0, totalR = 0;
        for (CropStats p : parents) {
            totalG += p.growth;
            totalGa += p.gain;
            totalR += p.resistance;
        }
        int count = parents.length;
        int g = clamp(totalG / count + rand.nextInt(3) - 1);
        int ga = clamp(totalGa / count + rand.nextInt(3) - 1);
        int r = clamp(totalR / count + rand.nextInt(3) - 1);
        return new CropStats(g, ga, r);
    }

    private static int inheritStat(int a, int b, Random rand) {
        int base = (a + b) / 2;
        int variance = rand.nextInt(5) - 2;
        if (Math.abs(variance) == 2 && rand.nextInt(3) != 0) {
            variance = variance > 0 ? 1 : -1;
        }
        return clamp(base + variance);
    }

    // ==================== 杂草判定 ====================

    public boolean hasWeedRisk(Random rand) {
        if (growth <= WEED_THRESHOLD) return false;
        int riskFactor = growth - resistance;
        return rand.nextInt(32) < riskFactor;
    }

    public float getGrowthRateMultiplier() {
        return 0.5f + (growth / (float) MAX_STAT) * 1.5f;
    }

    public int getYieldBonus(Random rand) {
        int bonus = gain / 8;
        if (rand.nextInt(MAX_STAT) < gain % 8) bonus++;
        return bonus;
    }

    public boolean resistWeedSpread(Random rand) {
        return rand.nextInt(MAX_STAT) < resistance;
    }

    // ==================== NBT持久化 ====================

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("statGrowth", growth);
        nbt.setInteger("statGain", gain);
        nbt.setInteger("statResistance", resistance);
        return nbt;
    }

    public static CropStats readFromNBT(NBTTagCompound nbt) {
        return new CropStats(
            nbt.getInteger("statGrowth"),
            nbt.getInteger("statGain"),
            nbt.getInteger("statResistance")
        );
    }

    // ==================== Getter/Setter ====================

    public int getGrowth() { return growth; }
    public int getGain() { return gain; }
    public int getResistance() { return resistance; }

    public void setGrowth(int growth) { this.growth = clamp(growth); }
    public void setGain(int gain) { this.gain = clamp(gain); }
    public void setResistance(int resistance) { this.resistance = clamp(resistance); }

    private static int clamp(int value) {
        return Math.max(MIN_STAT, Math.min(MAX_STAT, value));
    }

    @Override
    public String toString() {
        return String.format("Gr:%d Ga:%d Re:%d", growth, gain, resistance);
    }
}
