package com.drppp.drtech.wings;

import me.paulf.wings.util.CubicBezier;
import net.minecraft.nbt.NBTTagCompound;

public final class WingsFlightData {
    private static final CubicBezier FLY_AMOUNT_CURVE = new CubicBezier(0.37F, 0.13F, 0.3F, 1.12F);

    private boolean flying;
    private int timeFlying;
    private int previousTimeFlying;
    private int durabilityTimer;

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public int getTimeFlying() {
        return timeFlying;
    }

    public void setTimeFlying(int timeFlying) {
        this.timeFlying = timeFlying;
    }

    public int getPreviousTimeFlying() {
        return previousTimeFlying;
    }

    public void setPreviousTimeFlying(int previousTimeFlying) {
        this.previousTimeFlying = previousTimeFlying;
    }

    public int incrementDurabilityTimer() {
        return ++durabilityTimer;
    }

    public void resetDurabilityTimer() {
        durabilityTimer = 0;
    }

    public float getFlyingAmount(float partialTicks) {
        float progress = (previousTimeFlying + (timeFlying - previousTimeFlying) * partialTicks) / 20.0F;
        return FLY_AMOUNT_CURVE.eval(Math.max(0.0F, Math.min(1.0F, progress)));
    }

    public void copyFrom(WingsFlightData other) {
        flying = other.flying;
        timeFlying = other.timeFlying;
        previousTimeFlying = other.previousTimeFlying;
        durabilityTimer = other.durabilityTimer;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("Flying", flying);
        tag.setInteger("TimeFlying", timeFlying);
        tag.setInteger("PreviousTimeFlying", previousTimeFlying);
        tag.setInteger("DurabilityTimer", durabilityTimer);
        return tag;
    }

    public void deserializeNBT(NBTTagCompound tag) {
        flying = tag.getBoolean("Flying");
        timeFlying = tag.getInteger("TimeFlying");
        previousTimeFlying = tag.getInteger("PreviousTimeFlying");
        durabilityTimer = tag.getInteger("DurabilityTimer");
    }
}
