package com.drppp.drtech.loaders.recipes.chain;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

import static com.drppp.drtech.common.blocks.BlocksInit.TFFT_TANK;
import static com.drppp.drtech.common.blocks.metaBlocks.BlockFTTFPart.TFFTPartType.*;
import static com.drppp.drtech.common.items.metaItems.DrMetaItems.*;
import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.Materials.Tritanium;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.api.unification.ore.OrePrefix.pipeNormalFluid;
import static gregtech.common.blocks.BlockHermeticCasing.HermeticCasingsType.*;
import static gregtech.common.blocks.MetaBlocks.HERMETIC_CASING;
import static gregtech.common.items.MetaItems.*;


public class FluidStoreRecpie {
    public static void init()
    {
        FluidCores();

        //TFFT
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Steel)
                .inputs(HERMETIC_CASING.getItemVariant(HERMETIC_LV))
                .input(OrePrefix.plateDouble,Materials.Steel,2)
                .input(MetaItems.ELECTRIC_PUMP_HV,3)
                .input(OrePrefix.pipeNormalFluid,Materials.Steel,2)
                .circuitMeta(6)
                .fluidInputs(Materials.Polyethylene.getFluid(144))
                .outputs(TFFT_TANK.getItemVariant(TFFT_PART_TIER_T1))
                .duration(400)
                .EUt(480)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.StainlessSteel)
                .inputs(HERMETIC_CASING.getItemVariant(HERMETIC_MV))
                .input(OrePrefix.plateDouble,Materials.StainlessSteel,2)
                .input(MetaItems.ELECTRIC_PUMP_HV,6)
                .input(OrePrefix.pipeNormalFluid,Materials.StainlessSteel,2)
                .circuitMeta(6)
                .fluidInputs(Materials.Polytetrafluoroethylene.getFluid(144))
                .outputs(TFFT_TANK.getItemVariant(TFFT_PART_TIER_T2))
                .duration(600)
                .EUt(480)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Titanium)
                .inputs(HERMETIC_CASING.getItemVariant(HERMETIC_HV))
                .input(OrePrefix.plateDouble,Materials.Titanium,2)
                .input(MetaItems.ELECTRIC_PUMP_EV,3)
                .input(OrePrefix.pipeNormalFluid,Materials.Titanium,2)
                .input(MetaItems.FIELD_GENERATOR_LV,1)
                .circuitMeta(6)
                .fluidInputs(Materials.Polytetrafluoroethylene.getFluid(144))
                .outputs(TFFT_TANK.getItemVariant(TFFT_PART_TIER_T3))
                .duration(400)
                .EUt(1920)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.TungstenSteel)
                .inputs(HERMETIC_CASING.getItemVariant(HERMETIC_EV))
                .input(OrePrefix.plateDouble,Materials.TungstenSteel,2)
                .input(MetaItems.ELECTRIC_PUMP_IV,3)
                .input(OrePrefix.pipeNormalFluid,Materials.TungstenSteel,2)
                .input(MetaItems.FIELD_GENERATOR_HV,1)
                .circuitMeta(6)
                .fluidInputs(Materials.Polytetrafluoroethylene.getFluid(144))
                .outputs(TFFT_TANK.getItemVariant(TFFT_PART_TIER_T4))
                .duration(400)
                .EUt(7680)
                .buildAndRegister();
        //T5
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.RhodiumPlatedPalladium)
                .inputs(new ItemStack(HERMETIC_CASING,1,5))
                .inputs(HERMETIC_CASING.getItemVariant(HERMETIC_IV))
                .input(OrePrefix.plateDouble,Materials.RhodiumPlatedPalladium,2)
                .input(MetaItems.ELECTRIC_PUMP_LuV,3)
                .input(OrePrefix.pipeNormalFluid,Materials.NiobiumTitanium,2)
                .input(MetaItems.FIELD_GENERATOR_IV,1)
                .circuitMeta(6)
                .fluidInputs(Materials.Epoxy.getFluid(288))
                .outputs(TFFT_TANK.getItemVariant(TFFT_PART_TIER_T5))
                .duration(400)
                .EUt(32000)
                .buildAndRegister();
    }

    private static void FluidCores() {

        //  T1
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CELL_LARGE_STEEL, 12)
                .input(plateDouble, Tin, 8)
                .circuitMeta(8)
                .fluidInputs(Polyethylene.getFluid(L * 4))
                .output(FLUID_CORE_T1)
                .EUt(VA[HV])
                .duration(200)
                .buildAndRegister();

        //  T2
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CELL_LARGE_TUNGSTEN_STEEL, 18)
                .input(plateDouble, BlackSteel, 8)
                .circuitMeta(8)
                .fluidInputs(Polyethylene.getFluid(L * 16))
                .output(FLUID_CORE_T2)
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();
/*
        //  T3
        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CORE_T2)
                .input(FIELD_GENERATOR_HV)
                .input(QUANTUM_TANK[0])
                .input(foil, PolyvinylChloride, 4)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 16))
                .output(FLUID_CORE_T3)
                .EUt(VA[LuV])
                .duration(400)
                .Tier(1) // LuV
                .buildAndRegister();

        //  T4
        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CORE_T3)
                .input(FIELD_GENERATOR_EV)
                .input(QUANTUM_TANK[1])
                .input(foil, PolyvinylChloride, 8)
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 32))
                .fluidInputs(TinAlloy.getFluid(L * 4))
                .output(FLUID_CORE_T4)
                .EUt(VA[ZPM])
                .duration(400)
                .Tier(2) // ZPM
                .buildAndRegister();

        //  T5
        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CORE_T4)
                .input(FIELD_GENERATOR_IV)
                .input(QUANTUM_TANK[2])
                .input(foil, Polycaprolactam, 16)
                .fluidInputs(Polybenzimidazole.getFluid(L * 32))
                .fluidInputs(TinAlloy.getFluid(L * 16))
                .fluidInputs(Lubricant.getFluid(1000))
                .output(FLUID_CORE_T5)
                .EUt(VA[UV])
                .duration(400)
                .Tier(3) // UV
                .buildAndRegister();

        //  T6
        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CORE_T5)
                .input(FIELD_GENERATOR_LuV)
                .input(QUANTUM_TANK[3])
                .input(foil, Polycaprolactam, 32)
                .fluidInputs(Polybenzimidazole.getFluid(L * 64))
                .fluidInputs(TinAlloy.getFluid(L * 32))
                .fluidInputs(Lubricant.getFluid(3000))
                .fluidInputs(Vibranium.getFluid(L))
                .output(FLUID_CORE_T6)
                .EUt(VA[UHV])
                .duration(400)
                .Tier(4) // UHV
                .buildAndRegister();

        //  T7
        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CORE_T6)
                .input(FIELD_GENERATOR_ZPM)
                .input(QUANTUM_TANK[4])
                .input(foil, StyreneButadieneRubber, 64)
                .fluidInputs(Polyetheretherketone.getFluid(L * 64))
                .fluidInputs(TinAlloy.getFluid(L * 64))
                .fluidInputs(Lubricant.getFluid(5000))
                .fluidInputs(Neutronium.getFluid(L * 2))
                .output(FLUID_CORE_T7)
                .EUt(VA[UEV])
                .duration(400)
                .Tier(5) // UEV
                .buildAndRegister();

        //  T8
        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CORE_T7)
                .input(FIELD_GENERATOR_UV)
                .input(QUANTUM_TANK[5])
                .input(foil, PolyPhosphonitrileFluoroRubber, 64)
                .fluidInputs(Polyetheretherketone.getFluid(L * 128))
                .fluidInputs(TinAlloy.getFluid(L * 128))
                .fluidInputs(Lubricant.getFluid(7000))
                .fluidInputs(CelestialTungsten.getFluid(L * 4))
                .output(FLUID_CORE_T8)
                .EUt(VA[UIV])
                .duration(400)
                .Tier(5) // UIV
                .buildAndRegister();

        //  T9
        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CORE_T8)
                .input(FIELD_GENERATOR_UHV)
                .input(QUANTUM_TANK[6])
                .input(foil, Zylon, 64)
                .fluidInputs(Kevlar.getFluid(L * 128))
                .fluidInputs(TinAlloy.getFluid(L * 256))
                .fluidInputs(Lubricant.getFluid(9000))
                .fluidInputs(Spacetime.getFluid(L * 8))
                .output(FLUID_CORE_T9)
                .EUt(VA[UXV])
                .duration(400)
                .Tier(6) // UXV
                .buildAndRegister();

        /*
        //  T10
        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(FLUID_CORE_T9)
                .input(FIELD_GENERATOR_UEV)
                .input(QUANTUM_TANK[7])
                .input(foil, FullerenePolymerMatrix, 64)
                .fluidInputs(CosmicFabric.getFluid(L * 256))
                .fluidInputs(TinAlloy.getFluid(L * 512))
                .fluidInputs(Lubricant.getFluid(11000))
                .fluidInputs(Shirabon.getFluid(L * 16))
                .output(FLUID_CORE_T10)
                .EUt(VA[OpV])
                .duration(400)
                .Tier(6) // OpV
                .buildAndRegister();

         */
    }
}
