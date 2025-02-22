package com.drppp.drtech.api.capability.impl;

import com.drppp.drtech.DrtConfig;
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
        return ru.getEnergyOutput()/2;
    }

    @Override
    protected long getEnergyStored() {
        if(ru==null)
            return 0;
        return ru.getEnergyOutput()/2;
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
        return GTValues.V[1];
    }
    @Override
    protected void updateRecipeProgress() {
        if (this.canRecipeProgress && this.drawEnergy(this.recipeEUt, true)) {
            this.drawEnergy(this.recipeEUt, false);
            if (++this.progressTime > this.getMaxProgress()) {
                this.completeRecipe();
            }

            if (this.hasNotEnoughEnergy && this.getEnergyInputPerSecond() > 19L * (long)this.recipeEUt) {
                this.hasNotEnoughEnergy = false;
            }
        } else if (this.recipeEUt > 0) {
            this.hasNotEnoughEnergy = true;
            this.decreaseProgress();
        }

    }
    @Override
    public int getMaxProgress() {
        if(ru.getEnergyOutput()>this.recipeEUt)
        {
            int max = this.maxProgressTime;
            double reductionRatio = ((double) ru.getEnergyOutput() - ((double)recipeEUt*2)) / ((double)DrtConfig.MaxRu - ((double)recipeEUt*2));
            return Math.max(1,(int)(max - (max - max/2) * reductionRatio));
        }
        return super.getMaxProgress();
    }
}
