package com.drppp.drtech.api.capability.impl;

import com.drppp.drtech.api.capability.IRotationEnergy;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;

public class RecipeLogicRU  extends AbstractRecipeLogic {

    final IRotationEnergy ru;
    public RecipeLogicRU(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,IRotationEnergy ru) {
        super(tileEntity, recipeMap);
        this.ru = ru;
        this.hasPerfectOC=true;
    }

    @Override
    protected long getEnergyInputPerSecond() {
        if(ru==null)
            return 0;
        return ru.getEnergyOutput();
    }

    @Override
    protected long getEnergyStored() {
        if(ru==null)
            return 0;
        return ru.getEnergyOutput();
    }

    @Override
    protected long getEnergyCapacity() {
        if(ru==null)
            return 0;
        return ru.getEnergyOutput();
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean b) {
        if(ru==null)
            return false;
        if(ru.getEnergyOutput()>=recipeEUt)
            return true;
        return false;
    }

    @Override
    public long getMaxVoltage() {
        return GTValues.V[2];
    }
}
