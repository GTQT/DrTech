package com.drppp.drtech.loaders.misc;

import binnie.core.Binnie;
import binnie.core.Mods;
import binnie.core.liquid.ManagerLiquid;
import binnie.genetics.item.GeneticsItems;
import binnie.genetics.machine.AdvGeneticMachine;
import binnie.genetics.machine.GeneticMachine;
import binnie.genetics.machine.LaboratoryMachine;
import forestry.api.recipes.RecipeManagers;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static binnie.genetics.item.GeneticsItems.LaboratoryCasing;
import static binnie.genetics.modules.ModuleCore.*;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.MarkerMaterials.Tier.EV;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.BlockGlassCasing.CasingType.TEMPERED_GLASS;
import static gregtech.common.items.MetaItems.*;
import static keqing.gtqtcore.api.unification.GTQTMaterials.PPB;
import static keqing.gtqtcore.api.unification.GTQTMaterials.Zylon;


public class BinneRecipes {
    public static void init() {
        //基因工程-分包 配方修改！

        //机器
        ModHandler.addShapedRecipe("incubator", LaboratoryMachine.Incubator.get(1), "GFG", "XCX", "APA", 'C', GeneticsItems.LaboratoryCasing.get(1), 'F', MetaTileEntities.ELECTRIC_FURNACE[5].getStackForm(), 'X', "circuitEv", 'G', MetaBlocks.TRANSPARENT_CASING.getItemVariant(TEMPERED_GLASS), 'P', ELECTRIC_PISTON_IV, 'A', ROBOT_ARM_IV);

        ModHandler.addShapedRecipe("analyzer", LaboratoryMachine.Analyser.get(1), "GBG", "XCX", "APA", 'C', GeneticsItems.LaboratoryCasing.get(1), 'B', Mods.Forestry.item("portable_alyzer"), 'X', "circuitEv", 'G', MetaBlocks.TRANSPARENT_CASING.getItemVariant(TEMPERED_GLASS), 'P', ELECTRIC_PISTON_IV, 'A', GeneticsItems.DNADye.get(1));
        ModHandler.addShapedRecipe("genepool", LaboratoryMachine.Genepool.get(1), "GBG", "XCX", "APA", 'C', GeneticsItems.LaboratoryCasing.get(1), 'B', ELECTRIC_PISTON_IV, 'X', "circuitEv", 'G', MetaBlocks.TRANSPARENT_CASING.getItemVariant(TEMPERED_GLASS), 'P', ELECTRIC_PISTON_IV, 'A', MetaBlocks.TRANSPARENT_CASING.getItemVariant(TEMPERED_GLASS));
        ModHandler.addShapedRecipe("acclimatizer", LaboratoryMachine.Acclimatiser.get(1), "GBG", "XCX", "APA", 'C', GeneticsItems.LaboratoryCasing.get(1), 'B', VOLTAGE_COIL_IV, 'X', "circuitEv", 'G', MetaBlocks.TRANSPARENT_CASING.getItemVariant(TEMPERED_GLASS), 'P', ELECTRIC_PISTON_IV, 'A', FLUID_REGULATOR_EV);

        ModHandler.addShapedRecipe("isolator", GeneticMachine.Isolator.get(1), "GBG", "XCX", "APA", 'C', GeneticsItems.LaboratoryCasing.get(1), 'B', ELECTRIC_PUMP_IV, 'X', GeneticsItems.IntegratedCircuit.get(1), 'G', MetaTileEntities.CHEMICAL_REACTOR[5].getStackForm(), 'P', ELECTRIC_PISTON_IV, 'A', GeneticsItems.Enzyme.get(1));
        ModHandler.addShapedRecipe("polymeriser", GeneticMachine.Polymeriser.get(1), "GBG", "XCX", "GPG", 'C', GeneticsItems.LaboratoryCasing.get(1), 'B', ROBOT_ARM_IV, 'X', GeneticsItems.IntegratedCircuit.get(1), 'G', MetaTileEntities.CHEMICAL_REACTOR[5].getStackForm(), 'P', ELECTRIC_PISTON_IV);
        ModHandler.addShapedRecipe("sequencer", GeneticMachine.Sequencer.get(1), "GBG", "XCX", "APA", 'C', GeneticsItems.LaboratoryCasing.get(1), 'B', ELECTRIC_PISTON_IV, 'X', GeneticsItems.IntegratedCircuit.get(1), 'G', MetaTileEntities.CHEMICAL_REACTOR[5].getStackForm(), 'P', ELECTRIC_PISTON_IV, 'A', GeneticsItems.FluorescentDye.get(1));
        ModHandler.addShapedRecipe("inoculator", GeneticMachine.Inoculator.get(1), "GBG", "XCX", "APA", 'C', GeneticsItems.LaboratoryCasing.get(1), 'B', CONVEYOR_MODULE_IV, 'X', GeneticsItems.IntegratedCircuit.get(1), 'G', MetaTileEntities.CHEMICAL_REACTOR[5].getStackForm(), 'P', ELECTRIC_PISTON_IV, 'A', Items.EMERALD);

        ModHandler.addShapedRecipe("splicer", AdvGeneticMachine.Splicer.get(1), "GBG", "XCX", "APA", 'C', GeneticsItems.IntegratedCasing.get(1), 'B', CONVEYOR_MODULE_IV, 'X', GeneticsItems.IntegratedCPU.get(1), 'G', MetaTileEntities.CHEMICAL_REACTOR[5].getStackForm(), 'P', ELECTRIC_PISTON_IV, 'A', Items.BLAZE_ROD);
        ModHandler.addShapedRecipe("lab_machine", LaboratoryMachine.LabMachine.get(1), "IGI", "GCG", "IGI", 'C', GeneticsItems.LaboratoryCasing.get(1), 'I', "plateTungstenSteel", 'G', MetaBlocks.TRANSPARENT_CASING.getItemVariant(TEMPERED_GLASS));


        //物品
        //laboratory casing id 0
        ModHandler.addShapedRecipe(true, "laboratory_casing", LaboratoryCasing.get(1), "PYP", "YSY", "PYP", 'S', MetaTileEntities.HULL[5].getStackForm(), 'P', new UnificationEntry(screw, RhodiumPlatedPalladium), 'Y', new UnificationEntry(plate, Iridium));

        //id 5 6 7
        ASSEMBLER_RECIPES.recipeBuilder().inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(TEMPERED_GLASS,4)).input(Items.PAPER).input(screw, PPB, 2).input(plate, Iridium, 8).input(wireFine, Platinum, 4).outputs(GeneticsItems.EmptySequencer.get(itemGenetics, 1)).circuitMeta(20).EUt(VA[5]).duration(200).fluidInputs(Zylon.getFluid(144)).cleanroom(CleanroomType.CLEANROOM).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().inputs(MetaBlocks.TRANSPARENT_CASING.getItemVariant(TEMPERED_GLASS,6)).outputs(GeneticsItems.EMPTY_SERUM.get(itemGenetics, 1)).circuitMeta(21).fluidInputs(Zylon.getFluid(144)).cleanroom(CleanroomType.CLEANROOM).EUt(VA[5]).duration(200).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().inputs(GeneticsItems.EMPTY_SERUM.get(itemGenetics, 6)).input(stick, Iridium, 2).input(rotor, RhodiumPlatedPalladium, 1).outputs(GeneticsItems.EMPTY_GENOME.get(itemGenetics, 1)).fluidInputs(Zylon.getFluid(576)).cleanroom(CleanroomType.CLEANROOM).EUt(VA[5]).duration(200).buildAndRegister();

        ModHandler.addSmeltingRecipe(new ItemStack(itemSequencer), GeneticsItems.EmptySequencer.get(itemGenetics, 1), 0.0f);
        ModHandler.addSmeltingRecipe(new ItemStack(itemSerum), GeneticsItems.EMPTY_SERUM.get(itemGenetics, 1), 0.0f);
        ModHandler.addSmeltingRecipe(new ItemStack(itemSerumArray), GeneticsItems.EMPTY_GENOME.get(itemGenetics, 1), 0.0f);

        //8  基板
        ASSEMBLER_RECIPES.recipeBuilder().input(MetaItems.ELITE_CIRCUIT_BOARD).input(wireFine, Platinum, 16).input(screw, PPB, 8).input(rotor, RhodiumPlatedPalladium, 8).input(circuit, EV, 4).input(pipeNormalFluid, TungstenSteel, 1).fluidInputs(Zylon.getFluid(576)).outputs(GeneticsItems.IntegratedCircuit.get(itemGenetics, 1)).EUt(VA[5]).duration(400).buildAndRegister();

        //9  电路
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().inputs(GeneticsItems.IntegratedCircuit.get(itemGenetics, 1)).input(CRYSTAL_CENTRAL_PROCESSING_UNIT).input(ADVANCED_SMD_CAPACITOR, 6).input(ADVANCED_SMD_DIODE, 4).input(ADVANCED_SMD_TRANSISTOR, 6).input(wireFine, NiobiumTitanium, 8).outputs(GeneticsItems.IntegratedCPU.get(itemGenetics, 1)).cleanroom(CleanroomType.CLEANROOM).EUt(VA[5]).duration(400).buildAndRegister();

        //10 外壳
        ASSEMBLER_RECIPES.recipeBuilder().inputs(GeneticsItems.LaboratoryCasing.get(itemGenetics, 1)).inputs(GeneticsItems.IntegratedCPU.get(itemGenetics, 8)).input(screw, PPB, 8).input(stick, RhodiumPlatedPalladium, 8).input(wireFine, Platinum, 16).outputs(GeneticsItems.IntegratedCasing.get(itemGenetics, 1)).fluidInputs(Zylon.getFluid(1440)).cleanroom(CleanroomType.CLEANROOM).EUt(VA[5]).duration(800).buildAndRegister();

        RecipeManagers.carpenterManager.addRecipe(100, Binnie.LIQUID.getFluidStack(ManagerLiquid.WATER, 2000), ItemStack.EMPTY,
                new ItemStack(database),
                "X#X",
                "YEY",
                "RDR",
                '#', Blocks.GLASS_PANE, 'X', Items.DIAMOND, 'Y', Items.DIAMOND, 'R', Items.REDSTONE, 'D', Items.ENDER_EYE, 'E', Blocks.OBSIDIAN
        );

        ModHandler.addShapedRecipe("analyst", new ItemStack(analyst),
                " c ",
                "cac",
                " d ",
                'c', GeneticsItems.IntegratedCircuit.get(itemGenetics, 1), 'a', Mods.Forestry.item("portable_alyzer"), 'd', new ItemStack(Items.DIAMOND)
        );
    }
}
