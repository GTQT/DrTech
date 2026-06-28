package com.drppp.drtech.loaders.recipes.chain;

import com.drppp.drtech.api.unification.Materials.DrtechMaterials;
import net.minecraft.init.Items;

import static gregtech.api.GTValues.EV;
import static gregtech.api.GTValues.HV;
import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.V;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_RECIPES;
import static gregtech.api.recipes.RecipeMaps.COMBUSTION_GENERATOR_FUELS;
import static gregtech.api.recipes.RecipeMaps.DISTILLATION_RECIPES;
import static gregtech.api.recipes.RecipeMaps.DISTILLERY_RECIPES;
import static gregtech.api.recipes.RecipeMaps.ELECTROLYZER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.MACERATOR_RECIPES;
import static gregtech.api.recipes.RecipeMaps.MIXER_RECIPES;
import static gregtech.api.unification.material.Materials.Blaze;
import static gregtech.api.unification.material.Materials.CetaneBoostedDiesel;
import static gregtech.api.unification.material.Materials.EnderPearl;
import static gregtech.api.unification.material.Materials.Endstone;
import static gregtech.api.unification.material.Materials.HighOctaneGasoline;
import static gregtech.api.unification.material.Materials.SiliconDioxide;
import static gregtech.api.unification.material.Materials.SodiumHydroxide;
import static gregtech.api.unification.material.Materials.Talc;
import static gregtech.api.unification.material.Materials.Water;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.dustSmall;

public final class ChorusFruitChainRecipes {

    private ChorusFruitChainRecipes() {
    }

    public static void init() {
        registerDemulsifierRecipes();
        registerChorusExtractionRecipes();
        registerFuelRecipes();
    }

    private static void registerDemulsifierRecipes() {
        MIXER_RECIPES.recipeBuilder()
                .input(dust, SodiumHydroxide)
                .input(dust, Talc)
                .fluidInputs(Water.getFluid(1000))
                .fluidOutputs(DrtechMaterials.Demulsifier.getFluid(1000))
                .duration(120)
                .EUt(VA[LV])
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, EnderPearl)
                .input(dust, Blaze)
                .fluidInputs(DrtechMaterials.Demulsifier.getFluid(1000))
                .fluidOutputs(DrtechMaterials.HighEnergyDemulsifier.getFluid(1000))
                .duration(200)
                .EUt(VA[EV])
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .input(dust, EnderPearl)
                .fluidInputs(DrtechMaterials.Demulsifier.getFluid(1000))
                .fluidOutputs(DrtechMaterials.EnderExtractant.getFluid(1000))
                .duration(160)
                .EUt(VA[HV])
                .buildAndRegister();
    }

    private static void registerChorusExtractionRecipes() {
        MACERATOR_RECIPES.recipeBuilder()
                .input(Items.CHORUS_FRUIT)
                .output(dust, DrtechMaterials.Chorus)
                .duration(80)
                .EUt(VA[ULV])
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .input(dust, DrtechMaterials.Chorus)
                .fluidInputs(DrtechMaterials.HighEnergyDemulsifier.getFluid(1000))
                .output(dust, DrtechMaterials.EnderPrecipitate)
                .fluidOutputs(DrtechMaterials.HighEnergyChorusSolution.getFluid(1000))
                .duration(200)
                .EUt(VA[EV])
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .input(dust, DrtechMaterials.EnderPrecipitate)
                .chancedOutput(dustSmall, SiliconDioxide, 5000, 500)
                .chancedOutput(dust, Endstone, 2500, 250)
                .duration(160)
                .EUt(VA[HV])
                .buildAndRegister();

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(DrtechMaterials.HighEnergyChorusSolution.getFluid(1000))
                .fluidInputs(DrtechMaterials.EnderExtractant.getFluid(1000))
                .fluidOutputs(DrtechMaterials.ExtractedChorusSolvent.getFluid(1000))
                .fluidOutputs(DrtechMaterials.ChorusWaste.getFluid(1000))
                .duration(240)
                .EUt(VA[EV])
                .buildAndRegister();

        DISTILLERY_RECIPES.recipeBuilder()
                .fluidInputs(DrtechMaterials.ChorusWaste.getFluid(1000))
                .fluidOutputs(DrtechMaterials.Demulsifier.getFluid(250))
                .duration(120)
                .EUt(VA[HV])
                .buildAndRegister();

        DISTILLATION_RECIPES.recipeBuilder()
                .fluidInputs(DrtechMaterials.ExtractedChorusSolvent.getFluid(1000))
                .fluidOutputs(DrtechMaterials.EnderEnergyFactor.getFluid(100))
                .fluidOutputs(DrtechMaterials.EnderExtractant.getFluid(1000))
                .disableDistilleryRecipes()
                .duration(240)
                .EUt(VA[EV])
                .buildAndRegister();
    }

    private static void registerFuelRecipes() {
        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(DrtechMaterials.EnderEnergyFactor.getFluid(100))
                .fluidInputs(CetaneBoostedDiesel.getFluid(1000))
                .fluidOutputs(DrtechMaterials.HighEnergyDiesel.getFluid(1000))
                .duration(120)
                .EUt(VA[EV])
                .buildAndRegister();

        MIXER_RECIPES.recipeBuilder()
                .fluidInputs(DrtechMaterials.EnderEnergyFactor.getFluid(100))
                .fluidInputs(HighOctaneGasoline.getFluid(1000))
                .fluidOutputs(DrtechMaterials.HighEnergyGasoline.getFluid(1000))
                .duration(120)
                .EUt(VA[EV])
                .buildAndRegister();

        COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(DrtechMaterials.HighEnergyDiesel.getFluid(2))
                .duration(180)
                .EUt(V[LV])
                .buildAndRegister();

        COMBUSTION_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(DrtechMaterials.HighEnergyGasoline.getFluid(1))
                .duration(400)
                .EUt(V[LV])
                .buildAndRegister();
    }
}
