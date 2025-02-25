package com.drppp.drtech.api.capability.impl;

import com.drppp.drtech.api.capability.IHeatEnergy;

public class HeatEnergyHandler implements IHeatEnergy {
    private int heat=0;
    @Override
    public int getHeat() {
        return heat;
    }

    @Override
    public void setHuEnergy(int energy) {
        this.heat  = energy;
    }

    @Override
    public void changeHuEnergy(int energy) {
        this.heat += energy;
        if(this.heat<0)
            this.heat=0;
    }
}
