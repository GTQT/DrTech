package com.drppp.drtech.common.MetaTileEntities.muti.electric.standard;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityAssemblyLine;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityAdvancedAssemblyLine extends MetaTileEntityAssemblyLine {
    public MetaTileEntityAdvancedAssemblyLine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    protected class AssemblyLineRecipeLogic extends MultiblockRecipeLogic {

        public AssemblyLineRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
            super(tileEntity, hasPerfectOC);
        }

        @Override
        protected boolean drawEnergy(int recipeEUt, boolean simulate) {
            long resultEnergy = this.getEnergyStored() - (long) recipeEUt / 2;
            if (resultEnergy >= 0L && resultEnergy <= this.getEnergyCapacity()) {
                if (!simulate) {
                    this.getEnergyContainer().changeEnergy(-recipeEUt);
                }

                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getParallelLimit() {
            return 2;
        }
    }
}
