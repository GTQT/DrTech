package com.drppp.drtech.api.capability.impl;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.api.capability.IRotationEnergy;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import org.jetbrains.annotations.NotNull;

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
    protected boolean drawEnergy(long recipeEUt, boolean b) {
        if(ru==null)
            return false;
        if(ru.getEnergyOutput()/2>=recipeEUt)
            return true;
        return false;
    }
    protected boolean hasEnoughPower(int @NotNull [] resultOverclock) {
        int recipeEUt = resultOverclock[0];
        if (recipeEUt >= 0) {
            return this.getEnergyStored() >= (long)recipeEUt;
        } else {
            return this.getEnergyStored() - (long)recipeEUt <= this.getEnergyCapacity();
        }
    }
    @Override
    public long getMaxVoltage() {
        return GTValues.V[8];
    }

    @Override
    public void setParallelRecipesPerformed(int amount) {
        super.setParallelRecipesPerformed(amount);
    }

    @Override
    protected void updateRecipeProgress() {
        if (this.canRecipeProgress && this.drawEnergy(this.recipeEUt, true)) {
            if (++this.progressTime > this.getMaxProgress()) {
                this.completeRecipe();
            }
            if (this.hasNotEnoughEnergy && this.getEnergyInputPerSecond() >(long)this.recipeEUt) {
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
