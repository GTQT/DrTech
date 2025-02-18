package com.drppp.drtech.api.capability;

public interface IRotationEnergy {
    int getEnergyOutput();
    boolean isOutPut();
    void setOutPut(boolean b);
    void setRuEnergy(int energy);
    void changeRuEnergy(int energy);
}