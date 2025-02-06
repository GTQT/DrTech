package com.drppp.drtech.intergations;

import gregtech.api.recipes.RecipeMaps;
import gregtechfoodoption.GTFOMaterialHandler;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.dustTiny;
import static gregtechfoodoption.GTFOMaterialHandler.BeerBatter;
import static gregtechfoodoption.item.GTFOMetaItem.FRIED_FISH;

public class HarvestcraftFishChain {
    public static void MachineRecipeInit() {
        RecipeMaps.BREWING_RECIPES.recipeBuilder().EUt(16).duration(400)
                .fluidInputs(BeerBatter.getFluid(40))
                .input("listAllfishfresh")
                .outputs(FRIED_FISH.getStackForm())
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder().duration(500).EUt(16)
                .input("listAllfishfresh")
                .fluidInputs(Water.getFluid(400))
                .fluidOutputs(GTFOMaterialHandler.Sludge.getFluid(100))
                .buildAndRegister();
        MIXER_RECIPES.recipeBuilder().duration(250).EUt(16)
                .input("listAllfishfresh")
                .fluidInputs(SulfuricAcid.getFluid(200))
                .fluidOutputs(GTFOMaterialHandler.Sludge.getFluid(200))
                .buildAndRegister();

        EXTRACTOR_RECIPES.recipeBuilder().duration(16).EUt(4)
                .input("listAllfishfresh")
                .fluidOutputs(FishOil.getFluid(40))
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input("listAllfishfresh")
                .output(dust, Meat)
                .chancedOutput(dust, Meat, 5000, 0)
                .output(dustTiny, Bone)
                .duration(102).buildAndRegister();
    }
}
