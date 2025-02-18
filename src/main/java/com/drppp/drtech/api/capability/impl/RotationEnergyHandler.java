package com.drppp.drtech.api.capability.impl;

import com.drppp.drtech.api.capability.IRotationEnergy;

public class RotationEnergyHandler implements IRotationEnergy {
    private int outputRu=0;
    private boolean isOutput=true;
    @Override
    public int getEnergyOutput() {
        return outputRu;
    }

    @Override
    public boolean isOutPut() {
        return this.isOutput;
    }

    @Override
    public void setOutPut(boolean b) {
        this.isOutput = b;
    }

    @Override
    public void setRuEnergy(int energy) {
        this.outputRu = energy;
    }

    @Override
    public void changeRuEnergy(int energy) {
        this.outputRu += energy;
        if(this.outputRu<0)
            this.outputRu=0;
    }
}
