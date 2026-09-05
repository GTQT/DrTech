package com.drppp.drtech.common.MetaTileEntities.single;

import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.Function;

public class MetaTileEntityLightsaberAssembler extends SimpleMachineMetaTileEntity {
    public MetaTileEntityLightsaberAssembler(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                             ICubeRenderer renderer, int tier, boolean hasFrontFacing,
                                             Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction);
    }

    @Override
    protected AbstractRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new RecipeLogicEnergy(this, recipeMap, () -> energyContainer) {
            @Override
            protected boolean checkPreviousRecipe() {
                // A double saber's upper/lower order is positional, unlike normal GTCEu shapeless recipes.
                if (containsTwoSingleLightsabers(getInputInventory())) {
                    return false;
                }
                return super.checkPreviousRecipe();
            }
        };
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLightsaberAssembler(metaTileEntityId, recipeMap, renderer, getTier(),
                hasFrontFacing(), getTankScalingFunction());
    }

    private static boolean containsTwoSingleLightsabers(IItemHandlerModifiable inventory) {
        int lightsaberCount = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemLightsaber) {
                lightsaberCount++;
                if (lightsaberCount == 2) {
                    return true;
                }
            }
        }
        return false;
    }
}
