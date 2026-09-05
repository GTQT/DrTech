package com.brachy84.mechtech.api.armor;

import net.minecraft.nbt.NBTTagCompound;

public class AbsorbResult implements Comparable<AbsorbResult> {

    public static final AbsorbResult ZERO = new AbsorbResult();

    protected double ratio;
    protected int max;
    protected int priority;

    protected double armor = 0;
    protected double toughness = 0;

    protected NBTTagCompound moduleData;
    protected IModule module;

    public AbsorbResult(double ratio, int maxAbsorb, int priority) {
        this.ratio = ratio;
        this.max = maxAbsorb;
        this.priority = priority;
    }

    public AbsorbResult(double ratio, int maxAbsorb) {
        this(ratio, maxAbsorb, 0);
    }

    public AbsorbResult() {
        this(0, 0, 0);
    }

    public boolean isZero() {
        return this == ZERO || (armor <= 0 && toughness <= 0 && ratio <= 0);
    }

    public boolean useRatio() {
        return ratio > 0;
    }

    public AbsorbResult setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public AbsorbResult setMax(int max) {
        this.max = max;
        return this;
    }

    public AbsorbResult setRatio(double ratio) {
        this.ratio = ratio;
        return this;
    }

    protected void setModule(IModule module, NBTTagCompound data) {
        this.module = module;
        this.moduleData = data;
    }

    @Override
    public int compareTo(AbsorbResult o) {
        if (o.priority != priority) {
            return o.priority - priority;
        }
        double left =  (  ratio == 0 ? 0 :   max * 100.0D /   ratio);
        double right = (o.ratio == 0 ? 0 : o.max * 100.0D / o.ratio);
        return (int)(left - right);
    }

    @Override
    public String toString() {
        return "AbsorbResult{" +
                "ratio=" + ratio +
                ", max=" + max +
                ", priority=" + priority +
                ", armor=" + armor +
                ", toughness=" + toughness +
                '}';
    }
}

