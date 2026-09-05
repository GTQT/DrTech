package com.drppp.drtech.glider;

import net.minecraft.nbt.NBTTagCompound;

public final class GliderFlightData {
    private boolean deployed;

    public boolean isDeployed() {
        return deployed;
    }

    public void setDeployed(boolean deployed) {
        this.deployed = deployed;
    }

    public void copyFrom(GliderFlightData other) {
        deployed = other.deployed;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("Deployed", deployed);
        return tag;
    }

    public void deserializeNBT(NBTTagCompound tag) {
        deployed = tag.getBoolean("Deployed");
    }
}
