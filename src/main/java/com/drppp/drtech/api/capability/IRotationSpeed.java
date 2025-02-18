package com.drppp.drtech.api.capability;

import net.minecraft.util.EnumFacing;

public interface IRotationSpeed {
    int getSpeed();
    void setSpeed(int speed);
    IRotationEnergy getEnergy();
    EnumFacing getFacing();
}
