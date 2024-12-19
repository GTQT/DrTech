package com.drppp.drtech.loaders.chain;

import gregtech.api.unification.material.MarkerMaterials;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItem1;
import gregtech.common.items.MetaItems;

import static com.drppp.drtech.common.Blocks.BlocksInit.COMMON_CASING;
import static com.drppp.drtech.common.Blocks.BlocksInit.TRANSPARENT_CASING1;
import static com.drppp.drtech.common.Blocks.MetaBlocks.MetaCasing.MetalCasingType.*;
import static com.drppp.drtech.common.Blocks.MetaBlocks.MetaGlasses1.CasingType.COPY_GALSS;
import static com.drppp.drtech.common.Blocks.MetaBlocks.MetaGlasses1.CasingType.UU_GALSS;
import static com.drppp.drtech.common.MetaTileEntities.MetaTileEntities.*;
import static gregicality.multiblocks.api.unification.GCYMMaterials.Zeron100;
import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;
import static gregtech.loaders.recipe.CraftingComponent.*;
import static gregtech.loaders.recipe.MetaTileEntityLoader.registerMachineRecipe;
import static keqing.gtqtcore.api.recipes.GTQTcoreRecipeMaps.PRECISE_ASSEMBLER_RECIPES;
import static keqing.gtqtcore.api.unification.GCYSMaterials.KaptonK;
import static keqing.gtqtcore.api.unification.GTQTMaterials.*;
import static keqing.gtqtcore.api.utils.GTQTUtil.CWT;
import static keqing.gtqtcore.common.items.GTQTMetaItems.CIRCUIT_GOOD_III;

public class UURecipes {
    public static void init() {
        //多方块外壳配方
        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_CASING_MK2, 4))
                .input(CIRCUIT_GOOD_III, 4)
                .input(FIELD_GENERATOR_UV, 16)
                .input(frameGt, Tritanium, 8)
                .input(circuit, MarkerMaterials.Tier.UV, 4)
                .input(plateDouble, HG1223, 16)
                .input(plateDouble, Staballoy, 16)
                .input(plate, Zeron100, 16)
                .input(gearSmall, Stellite, 16)
                .fluidInputs(KaptonK.getFluid(L * 32))
                .fluidInputs(Polybenzimidazole.getFluid(L * 16))
                .fluidInputs(UUMatter.getFluid(4000))
                .fluidInputs(NaquadahAlloy.getFluid(L * 4))
                .outputs(COMMON_CASING.getItemVariant(ELEMENT_CONSTRAINS_MACHINE_CASING, 4))
                .stationResearch(b -> b
                        .researchStack(COMMON_CASING.getItemVariant(RESONATOR_CASING))
                        .CWUt(CWT[ZPM])
                        .EUt(VA[UV]))
                .EUt(VA[UV])
                .duration(1200)
                .buildAndRegister();

        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(CIRCUIT_GOOD_III, 1)
                .input(frameGt, Inconel690, 2)
                .input(FIELD_GENERATOR_UV, 4)
                .input(plate, Zeron100, 8)
                .fluidInputs(UUMatter.getFluid(1000))
                .fluidInputs(Polybenzimidazole.getFluid(L * 8))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputs(COMMON_CASING.getItemVariant(RESONATOR_CASING))
                .EUt(VA[ZPM])
                .duration(600)
                .Tier(3)
                .CWUt(CWT[ZPM])
                .buildAndRegister();

        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(CIRCUIT_GOOD_III, 1)
                .input(frameGt, Inconel690, 2)
                .input(EMITTER_ZPM, 4)
                .input(plate, Zeron100, 8)
                .fluidInputs(UUMatter.getFluid(1000))
                .fluidInputs(Polybenzimidazole.getFluid(L * 8))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputs(COMMON_CASING.getItemVariant(BUNCHER_CASING))
                .EUt(VA[ZPM])
                .duration(600)
                .Tier(3)
                .CWUt(CWT[ZPM])
                .buildAndRegister();

        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(CIRCUIT_GOOD_III, 1)
                .input(VOLTAGE_COIL_ZPM, 8)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .input(plate, Zeron100, 8)
                .fluidInputs(UUMatter.getFluid(1000))
                .fluidInputs(Polybenzimidazole.getFluid(L * 8))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputs(COMMON_CASING.getItemVariant(HIGH_VOLTAGE_CAPACITOR_BLOCK_CASING))
                .EUt(VA[ZPM])
                .duration(600)
                .Tier(3)
                .CWUt(CWT[ZPM])
                .buildAndRegister();

        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(CIRCUIT_GOOD_III, 1)
                .input(frameGt, Inconel690, 2)
                .input(stick, Americium, 2)
                .input(screw, Tritanium, 2)
                .fluidInputs(UUMatter.getFluid(1000))
                .fluidInputs(Polybenzimidazole.getFluid(L * 8))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputs(COMMON_CASING.getItemVariant(MASS_GENERATION_CASING))
                .EUt(VA[ZPM])
                .duration(600)
                .Tier(3)
                .CWUt(CWT[ZPM])
                .buildAndRegister();

        PRECISE_ASSEMBLER_RECIPES.recipeBuilder()
                .input(CIRCUIT_GOOD_III, 1)
                .input(frameGt, Inconel690, 2)
                .input(stick, Pikyonium64B, 2)
                .input(plate, Zeron100, 2)
                .fluidInputs(UUMatter.getFluid(1000))
                .fluidInputs(Polybenzimidazole.getFluid(L * 8))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputs(COMMON_CASING.getItemVariant(MASS_GENERATION_COIL_CASING))
                .EUt(VA[ZPM])
                .duration(600)
                .Tier(3)
                .CWUt(CWT[ZPM])
                .buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.FUSION_GLASS, 8))
                .input(CIRCUIT_GOOD_III, 4)
                .input(FIELD_GENERATOR_UV, 16)
                .input(frameGt, HMS1J22Alloy, 8)
                .input(circuit, MarkerMaterials.Tier.UHV, 2)
                .input(circuit, MarkerMaterials.Tier.UV, 4)
                .input(circuit, MarkerMaterials.Tier.ZPM, 8)
                .input(VOLTAGE_COIL_ZPM, 16)
                .input(plateDouble, HG1223, 16)
                .input(plateDouble, Staballoy, 16)
                .input(gear, MaragingSteel250, 16)
                .input(gearSmall, Stellite, 16)
                .input(cableGtQuadruple, VanadiumGallium, 64)
                .input(cableGtQuadruple, VanadiumGallium, 64)
                .fluidInputs(KaptonK.getFluid(L * 32))
                .fluidInputs(Polybenzimidazole.getFluid(L * 16))
                .fluidInputs(UUMatter.getFluid(4000))
                .fluidInputs(NaquadahAlloy.getFluid(L * 4))
                .outputs(TRANSPARENT_CASING1.getItemVariant(UU_GALSS, 8))
                .stationResearch(b -> b
                        .researchStack(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.FUSION_GLASS))
                        .CWUt(CWT[ZPM])
                        .EUt(VA[UV]))
                .EUt(VA[UV])
                .duration(1200)
                .buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.FUSION_GLASS, 8))
                .input(CIRCUIT_GOOD_III, 4)
                .input(FIELD_GENERATOR_UV, 16)
                .input(frameGt, Tritanium, 8)
                .input(circuit, MarkerMaterials.Tier.UHV, 2)
                .input(circuit, MarkerMaterials.Tier.UV, 4)
                .input(circuit, MarkerMaterials.Tier.ZPM, 8)
                .input(VOLTAGE_COIL_ZPM, 16)
                .input(plate, Zeron100, 16)
                .input(plateDouble, Staballoy, 16)
                .input(gearSmall, Americium, 16)
                .input(gearSmall, Stellite, 16)
                .fluidInputs(KaptonK.getFluid(L * 32))
                .fluidInputs(Polybenzimidazole.getFluid(L * 16))
                .fluidInputs(UUMatter.getFluid(4000))
                .fluidInputs(NaquadahAlloy.getFluid(L * 4))
                .outputs(TRANSPARENT_CASING1.getItemVariant(COPY_GALSS, 8))
                .stationResearch(b -> b
                        .researchStack(MetaBlocks.TRANSPARENT_CASING.getItemVariant(BlockGlassCasing.CasingType.FUSION_GLASS))
                        .CWUt(CWT[ZPM])
                        .EUt(VA[UV]))
                .EUt(VA[UV])
                .duration(1200)
                .buildAndRegister();
        //多方块控制器配方
        //大UU
        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(UU_PRODUCTER[8].getStackForm(4))
                .input(CIRCUIT_GOOD_III, 8)
                .input(ELECTRIC_PUMP_UV, 32)
                .input(FIELD_GENERATOR_UV, 32)
                .input(frameGt, HMS1J22Alloy, 16)
                .input(circuit, MarkerMaterials.Tier.UHV, 4)
                .input(circuit, MarkerMaterials.Tier.UV, 16)
                .input(circuit, MarkerMaterials.Tier.ZPM, 32)
                .input(plateDouble, HG1223, 4)
                .input(plateDouble, Staballoy, 4)
                .input(gear, MaragingSteel250, 4)
                .input(gearSmall, Stellite, 16)
                .fluidInputs(KaptonK.getFluid(L * 32))
                .fluidInputs(Polybenzimidazole.getFluid(L * 16))
                .fluidInputs(UUMatter.getFluid(4000))
                .fluidInputs(NaquadahAlloy.getFluid(L * 4))
                .outputs(LARGE_UU_PRODUCTER.getStackForm())
                .stationResearch(b -> b
                        .researchStack(UU_PRODUCTER[8].getStackForm())
                        .CWUt(CWT[ZPM])
                        .EUt(VA[UV]))
                .EUt(VA[UV])
                .duration(1200)
                .buildAndRegister();

        //大复制
        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(DUPLICATOR[8].getStackForm(4))
                .input(CIRCUIT_GOOD_III, 8)
                .input(ELECTRIC_PUMP_UV, 32)
                .input(FIELD_GENERATOR_UV, 32)
                .input(frameGt, Pikyonium64B, 16)
                .input(circuit, MarkerMaterials.Tier.UHV, 4)
                .input(circuit, MarkerMaterials.Tier.UV, 16)
                .input(circuit, MarkerMaterials.Tier.ZPM, 32)
                .input(plate, Zeron100, 4)
                .input(plateDouble, Staballoy, 4)
                .input(screw, Naquadria, 4)
                .input(gearSmall, Stellite, 16)
                .input(cableGtQuadruple, VanadiumGallium, 64)
                .input(cableGtQuadruple, VanadiumGallium, 64)
                .fluidInputs(KaptonK.getFluid(L * 32))
                .fluidInputs(Polybenzimidazole.getFluid(L * 16))
                .fluidInputs(UUMatter.getFluid(4000))
                .fluidInputs(NaquadahAlloy.getFluid(L * 4))
                .outputs(LARGE_ELEMENT_DUPLICATOR.getStackForm())
                .stationResearch(b -> b
                        .researchStack(DUPLICATOR[8].getStackForm())
                        .CWUt(CWT[ZPM])
                        .EUt(VA[UV]))
                .EUt(VA[UV])
                .duration(1200)
                .buildAndRegister();
        //机器配方
        registerMachineRecipe(DUPLICATOR, "PGP", "CMC", "PFP",
                'M', HULL,
                'P', EMITTER,
                'F', CABLE_HEX,
                'C', CIRCUIT,
                'G', FIELD_GENERATOR);

        registerMachineRecipe(UU_PRODUCTER, "CGC", "FMF", "CGC",
                'M', HULL,
                'F', CABLE_HEX,
                'C', CIRCUIT,
                'G', FIELD_GENERATOR);
    }
}
