package com.drppp.drtech.intergations;

import com.drppp.drtech.common.Items.MetaItems.MetaItemsReactor;
import gregtech.api.fluids.store.FluidStorageKeys;
import keqing.gtqtcore.api.recipes.GTQTcoreRecipeMaps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static com.drppp.drtech.loaders.DrtechReceipes.LOG_CREATE;
import static gregtech.api.unification.material.Materials.Helium;


public class GtqtCoreLinkage {
    public static final String GTQTCORE_ID = "gtqtcore";
    public static void MachineRecipeInit()
    {
        LOG_CREATE.recipeBuilder()
                .notConsumable(new ItemStack(Item.getByNameOrId("gtqtcore:pine_sapling"),1))
                .outputs(new ItemStack(Item.getByNameOrId("gtqtcore:pine_log"),1))
                .EUt(30)
                .duration(100)
                .buildAndRegister();

        GTQTcoreRecipeMaps.FLUID_CANNER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.COOLANT_NULL_CELL_1)
                .fluidInputs(Helium.getFluid(FluidStorageKeys.LIQUID, 1000))
                .output(MetaItemsReactor.HE_COOLANT_CELL_60K)
                .EUt(1)
                .duration(10)
                .buildAndRegister();
        GTQTcoreRecipeMaps.FLUID_CANNER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.COOLANT_NULL_CELL_2)
                .fluidInputs(Helium.getFluid(FluidStorageKeys.LIQUID, 3000))
                .output(MetaItemsReactor.HE_COOLANT_CELL_180K)
                .EUt(1)
                .duration(30)
                .buildAndRegister();
        GTQTcoreRecipeMaps.FLUID_CANNER_RECIPES.recipeBuilder()
                .input(MetaItemsReactor.COOLANT_NULL_CELL_3)
                .fluidInputs(Helium.getFluid(FluidStorageKeys.LIQUID, 6000))
                .output(MetaItemsReactor.HE_COOLANT_CELL_360K)
                .EUt(1)
                .duration(60)
                .buildAndRegister();
    }
}
