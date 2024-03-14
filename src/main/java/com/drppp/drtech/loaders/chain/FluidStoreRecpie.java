package com.drppp.drtech.loaders.chain;

import com.drppp.drtech.common.Blocks.BlocksInit;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;

public class FluidStoreRecpie {
    public static void init()
    {
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Steel)
                .inputs(new ItemStack(MetaBlocks.HERMETIC_CASING,1,0))
                .input(OrePrefix.plateDouble,Materials.Steel,2)
                .input(MetaItems.ELECTRIC_PUMP_HV,6)
                .input(OrePrefix.pipeNormalFluid,Materials.Steel,2)
                .circuitMeta(3)
                .fluidInputs(Materials.Polyethylene.getFluid(144))
                .outputs(new ItemStack(BlocksInit.YOT_TANK,1,0))
                .duration(400)
                .EUt(480)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.TungstenSteel)
                .inputs(new ItemStack(MetaBlocks.HERMETIC_CASING,1,4))
                .input(OrePrefix.plateDouble,Materials.TungstenSteel,2)
                .input(MetaItems.ELECTRIC_PUMP_EV,6)
                .input(MetaItems.FIELD_GENERATOR_HV,2)
                .input(OrePrefix.pipeNormalFluid,Materials.TungstenSteel,4)
                .fluidInputs(Materials.Polytetrafluoroethylene.getFluid(288))
                .circuitMeta(3)
                .outputs(new ItemStack(BlocksInit.YOT_TANK,1,1))
                .duration(400)
                .EUt(7680)
                .buildAndRegister();



        //TFFT
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Steel)
                .inputs(new ItemStack(MetaBlocks.HERMETIC_CASING,1,0))
                .input(OrePrefix.plateDouble,Materials.Steel,2)
                .input(MetaItems.ELECTRIC_PUMP_HV,3)
                .input(OrePrefix.pipeNormalFluid,Materials.Steel,2)
                .circuitMeta(6)
                .fluidInputs(Materials.Polyethylene.getFluid(144))
                .outputs(new ItemStack(BlocksInit.TFFT_TANK,1,0))
                .duration(400)
                .EUt(480)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.StainlessSteel)
                .inputs(new ItemStack(MetaBlocks.HERMETIC_CASING,1,0))
                .input(OrePrefix.plateDouble,Materials.StainlessSteel,2)
                .input(MetaItems.ELECTRIC_PUMP_HV,6)
                .input(OrePrefix.pipeNormalFluid,Materials.StainlessSteel,2)
                .circuitMeta(6)
                .fluidInputs(Materials.Polytetrafluoroethylene.getFluid(144))
                .outputs(new ItemStack(BlocksInit.TFFT_TANK,1,1))
                .duration(600)
                .EUt(480)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Titanium)
                .inputs(new ItemStack(MetaBlocks.HERMETIC_CASING,1,2))
                .input(OrePrefix.plateDouble,Materials.Titanium,2)
                .input(MetaItems.ELECTRIC_PUMP_EV,3)
                .input(OrePrefix.pipeNormalFluid,Materials.Titanium,2)
                .input(MetaItems.FIELD_GENERATOR_LV,1)
                .circuitMeta(6)
                .fluidInputs(Materials.Polytetrafluoroethylene.getFluid(144))
                .outputs(new ItemStack(BlocksInit.TFFT_TANK,1,2))
                .duration(400)
                .EUt(1920)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.TungstenSteel)
                .inputs(new ItemStack(MetaBlocks.HERMETIC_CASING,1,4))
                .input(OrePrefix.plateDouble,Materials.TungstenSteel,2)
                .input(MetaItems.ELECTRIC_PUMP_IV,3)
                .input(OrePrefix.pipeNormalFluid,Materials.TungstenSteel,2)
                .input(MetaItems.FIELD_GENERATOR_HV,1)
                .circuitMeta(6)
                .fluidInputs(Materials.Polytetrafluoroethylene.getFluid(144))
                .outputs(new ItemStack(BlocksInit.TFFT_TANK,1,3))
                .duration(400)
                .EUt(7680)
                .buildAndRegister();
        //T5
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.RhodiumPlatedPalladium)
                .inputs(new ItemStack(MetaBlocks.HERMETIC_CASING,1,5))
                .input(OrePrefix.plateDouble,Materials.RhodiumPlatedPalladium,2)
                .input(MetaItems.ELECTRIC_PUMP_LuV,3)
                .input(OrePrefix.pipeNormalFluid,Materials.NiobiumTitanium,2)
                .input(MetaItems.FIELD_GENERATOR_IV,1)
                .circuitMeta(6)
                .fluidInputs(Materials.Epoxy.getFluid(288))
                .outputs(new ItemStack(BlocksInit.TFFT_TANK,1,4))
                .duration(400)
                .EUt(32000)
                .buildAndRegister();
    }
}
