package com.drppp.drtech.loaders.misc;

import binnie.core.Binnie;
import binnie.core.Mods;
import binnie.core.liquid.ManagerLiquid;
import binnie.genetics.item.GeneticsItems;
import binnie.genetics.machine.AdvGeneticMachine;
import binnie.genetics.machine.GeneticMachine;
import binnie.genetics.machine.LaboratoryMachine;
import com.drppp.drtech.loaders.CraftingReceipe;
import forestry.api.recipes.RecipeManagers;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.ore.OrePrefix;
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
import static gregtech.api.unification.material.MarkerMaterials.Tier.HV;
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
        ModHandler.addShapedRecipe("incubator", LaboratoryMachine.Incubator.get(1),
                "GFG",
                        "XCX",
                        "APA",
                'C', GeneticsItems.LaboratoryCasing.get(1),
                'F', Blocks.FURNACE,
                'X', GeneticsItems.IntegratedCircuit.get(1),
                'G', CraftingReceipe.getItemStack("gregtech:machine", 1611),
                'P', ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall,StainlessSteel));

        ModHandler.addShapedRecipe("analyzer", LaboratoryMachine.Analyser.get(1),
                "GFG",
                "XCX",
                "APA",
                'C', GeneticsItems.LaboratoryCasing.get(1),
                'F', Blocks.CHEST,
                'X', GeneticsItems.IntegratedCircuit.get(1),
                'G', GeneticsItems.DNADye.get(1),
                'P', ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall,StainlessSteel));
        ModHandler.addShapedRecipe("genepool", LaboratoryMachine.Genepool.get(1),
                "GFG",
                "XCX",
                "APA",
                'C', GeneticsItems.LaboratoryCasing.get(1),
                'F', Blocks.CHEST,
                'X', GeneticsItems.IntegratedCircuit.get(1),
                'G', CraftingReceipe.getItemStack("gregtech:machine", 1611),
                'P', ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall,StainlessSteel));
        ModHandler.addShapedRecipe("acclimatizer", LaboratoryMachine.Acclimatiser.get(1),
                "GFG",
                "XCX",
                "APA",
                'C', GeneticsItems.LaboratoryCasing.get(1),
                'F', Items.LAVA_BUCKET,
                'X', GeneticsItems.IntegratedCircuit.get(1),
                'G', Items.WATER_BUCKET,
                'P', ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall,StainlessSteel));

        ModHandler.addShapedRecipe("isolator", GeneticMachine.Isolator.get(1),
                "GFG",
                "XCX",
                "APA",
                'C', GeneticsItems.LaboratoryCasing.get(1),
                'F', Blocks.CHEST,
                'X', GeneticsItems.IntegratedCircuit.get(1),
                'G', CraftingReceipe.getItemStack("genetics:misc", 3),
                'P', ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall,StainlessSteel));
        ModHandler.addShapedRecipe("polymeriser", GeneticMachine.Polymeriser.get(1),
                "GFG",
                "XCX",
                "APA",
                'C', GeneticsItems.LaboratoryCasing.get(1),
                'F', Blocks.CHEST,
                'X', GeneticsItems.IntegratedCircuit.get(1),
                'G', CraftingReceipe.getItemStack("genetics:misc", 7),
                'P', ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall,StainlessSteel));
        ModHandler.addShapedRecipe("sequencer", GeneticMachine.Sequencer.get(1),
                "GFG",
                "XCX",
                "APA",
                'C', GeneticsItems.LaboratoryCasing.get(1),
                'F', Blocks.CHEST,
                'X', GeneticsItems.IntegratedCircuit.get(1),
                'G', CraftingReceipe.getItemStack("genetics:misc", 2),
                'P', ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall,StainlessSteel));
        ModHandler.addShapedRecipe("inoculator", CraftingReceipe.getItemStack("<genetics:adv_machine>"),
                "GFG",
                "XCX",
                "APA",
                'C', GeneticsItems.LaboratoryCasing.get(1),
                'F', Blocks.CHEST,
                'X', GeneticsItems.IntegratedCircuit.get(1),
                'G', new UnificationEntry(plate,Emerald),
                'P', ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall,StainlessSteel));

        ModHandler.addShapedRecipe("splicer", CraftingReceipe.getItemStack("genetics:adv_machine"),
                "GBG",
                        "XCX",
                        "APA",
                'C', GeneticsItems.IntegratedCasing.get(1),
                'X', GeneticsItems.IntegratedCPU.get(1),
                'B', CraftingReceipe.getItemStack("gregtech:machine", 1629),
                'G',  new UnificationEntry(plate,Diamond),
                'P', ELECTRIC_MOTOR_HV,
                'A', new UnificationEntry(gearSmall,Diamond));
        ModHandler.addShapedRecipe("lab_machine", LaboratoryMachine.LabMachine.get(1),
                "IGI",
                        "GCG",
                        "IGI",
                'C', GeneticsItems.LaboratoryCasing.get(1),
                'I', Blocks.GLASS_PANE,
                'G', new UnificationEntry(plate,StainlessSteel));
        //物品
        //laboratory casing id 0
         ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(CraftingReceipe.getItemStack("forestry:sturdy_machine"))
                .input(plate,Aluminium,4)
                 .outputs(CraftingReceipe.getItemStack("<genetics:misc>"))
                 .EUt(120)
                 .duration(1200)
                 .buildAndRegister();
        ;
        //id 5 6 7
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(Blocks.GLASS_PANE,2)
                .outputs(GeneticsItems.EmptySequencer.get(itemGenetics, 1))
                .circuitMeta(20)
                .EUt(24)
                .duration(200)
                .fluidInputs(Gold.getFluid(288))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(Blocks.GLASS_PANE,4)
                .outputs(GeneticsItems.EMPTY_SERUM.get(itemGenetics, 1))
                .circuitMeta(21)
                .fluidInputs(Gold.getFluid(144))
                .EUt(48)
                .duration(200)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(GeneticsItems.EMPTY_SERUM.get(itemGenetics, 10))
                .outputs(GeneticsItems.EMPTY_GENOME.get(itemGenetics, 1))
                .fluidInputs(Gold.getFluid(576))
                .EUt(96)
                .duration(200)
                .buildAndRegister();

        ModHandler.addSmeltingRecipe(new ItemStack(itemSequencer), GeneticsItems.EmptySequencer.get(itemGenetics, 1), 0.0f);
        ModHandler.addSmeltingRecipe(new ItemStack(itemSerum), GeneticsItems.EMPTY_SERUM.get(itemGenetics, 1), 0.0f);
        ModHandler.addSmeltingRecipe(new ItemStack(itemSerumArray), GeneticsItems.EMPTY_GENOME.get(itemGenetics, 1), 0.0f);

        //8  基板
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(CraftingReceipe.getItemStack("forestry:chipsets",1))
                .input(circuit, HV, 2)
                .fluidInputs(StainlessSteel.getFluid(576))
                .outputs(GeneticsItems.IntegratedCircuit.get(itemGenetics, 1))
                .EUt(64).duration(400).buildAndRegister();

        //9  电路
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(CraftingReceipe.getItemStack("gregtech:meta_item_1", 403))
                .inputs(CraftingReceipe.getItemStack("forestry:thermionic_tubes", 5))
                .fluidInputs(Gold.getFluid(144))
                .circuitMeta(23)
                .outputs(GeneticsItems.IntegratedCPU.get(itemGenetics, 1))
                .EUt(20)
                .duration(400)
                .buildAndRegister();

        //10 外壳
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(GeneticsItems.LaboratoryCasing.get(itemGenetics, 1))
                .inputs(GeneticsItems.IntegratedCPU.get(itemGenetics, 2))
                .outputs(GeneticsItems.IntegratedCasing.get(itemGenetics, 1))
                .fluidInputs(Glowstone.getFluid(144*2))
                .circuitMeta(23)
                .EUt(480)
                .duration(800)
                .buildAndRegister();

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
