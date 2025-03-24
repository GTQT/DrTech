package com.drppp.drtech.api.capability.impl;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.api.capability.IHeatEnergy;
import com.drppp.drtech.api.capability.IRotationEnergy;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import org.jetbrains.annotations.NotNull;

public class RecipeLogicHU extends AbstractRecipeLogic {

    final IHeatEnergy hu;
    public RecipeLogicHU(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, IHeatEnergy hu) {
        super(tileEntity, recipeMap);
        this.hu = hu;
        this.hasPerfectOC=true;
    }

    @Override
    protected long getEnergyInputPerSecond() {
        if(hu==null)
            return 0;
        return hu.getHeat()/2;
    }

    @Override
    protected long getEnergyStored() {
        if(hu==null)
            return 0;
        return hu.getHeat()/2;
    }

    @Override
    protected long getEnergyCapacity() {
        if(hu==null)
            return 0;
        return hu.getHeat();
    }

    @Override
    protected boolean drawEnergy(long recipeEUt, boolean b) {
        if(hu==null)
            return false;
        if(getEnergyStored()>=recipeEUt)
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
    protected void updateRecipeProgress() {
        if (this.canRecipeProgress && this.drawEnergy(this.recipeEUt, true)) {
            this.drawEnergy(this.recipeEUt, false);
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
        return this.maxProgressTime/2;
    }
}
