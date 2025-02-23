package com.drppp.drtech.loaders.misc;

import binnie.genetics.item.GeneticsItems;
import binnie.genetics.machine.LaboratoryMachine;
import com.drppp.drtech.loaders.CraftingReceipe;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.ToolItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;


public class GendustryRecipes {
    public static void init() {
        ItemStack chacao = CraftingReceipe.getItemStack("<gendustry:bee_receptacle>");
        ItemStack nengliang = CraftingReceipe.getItemStack("<gendustry:power_module>");
        ItemStack jiyin = CraftingReceipe.getItemStack("<gendustry:genetics_processor>");
        ItemStack huanjing = CraftingReceipe.getItemStack("<gendustry:env_processor>");
        ItemStack qihou = CraftingReceipe.getItemStack("<gendustry:climate_module>");
        ItemStack bujian = CraftingReceipe.getItemStack("<forestry:hardened_machine>");
        UnificationEntry chilun =new UnificationEntry(gear,Osmiridium);

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Tin,4)
                .input(dust, RedAlloy,1)
                .fluidInputs(Glass.getFluid(144))
                .outputs(CraftingReceipe.getItemStack("gendustry:gene_sample_blank"))
                .circuitMeta(24)
                .EUt(480)
                .duration(20)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Diamond,1)
                .input(ring, Steel,1)
                .fluidInputs(Glass.getFluid(576))
                .outputs(CraftingReceipe.getItemStack("gendustry:labware"))
                .circuitMeta(24)
                .EUt(480)
                .duration(40)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(plateDouble, Palladium,1)
                .inputs(CraftingReceipe.getItemStack("gregtech:machine",1612))
                .fluidInputs(Plutonium239.getFluid(576))
                .outputs(CraftingReceipe.getItemStack("gendustry:mutagen_tank"))
                .circuitMeta(24)
                .EUt(1920)
                .duration(40)
                .buildAndRegister();
        ModHandler.addShapedRecipe("bee_cachao", CraftingReceipe.getItemStack("gendustry:bee_receptacle"),
                "AFA",
                        "XCX",
                        "AGA",
                'C', CraftingReceipe.getItemStack("<gregtech:meta_shell:75>"),
                'X', CraftingReceipe.getItemStack("<gregtech:meta_shell:50>"),
                'G', ToolItems.WRENCH,
                'F', CraftingReceipe.getItemStack("<gregtech:gt.comb:56>"),
                'A', new UnificationEntry(gearSmall,StainlessSteel));
        ModHandler.addShapedRecipe("bee_nengliangmokuai", CraftingReceipe.getItemStack("<gendustry:power_module>"),
                "ADA",
                        "BEB",
                        "CFC",
                'A', new UnificationEntry(cableGtSingle,VanadiumGallium),
                'B', CraftingReceipe.getItemStack("<gregtech:meta_shell:50>"),
                'C', new UnificationEntry(gearSmall,Osmiridium),
                'D', CraftingReceipe.getItemStack("<gregtech:gt.comb:57>"),
                'E', new UnificationEntry(circuit, MarkerMaterials.Tier.LuV),
                'F', ELECTRIC_MOTOR_LuV
        );
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.IV,1)
                .input(ROBOT_ARM_IV)
                .inputs(CraftingReceipe.getItemStack("<gregtech:meta_item_1:701>"))
                .outputs(CraftingReceipe.getItemStack("<gendustry:genetics_processor>"))
                .circuitMeta(24)
                .EUt(1920)
                .duration(400)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.IV,1)
                .input(plate, Diamond,1)
                .input(ROBOT_ARM_IV)
                .inputs(CraftingReceipe.getItemStack("<gregtech:meta_item_1:701>"))
                .outputs(CraftingReceipe.getItemStack("<gendustry:env_processor>"))
                .circuitMeta(23)
                .EUt(1920)
                .duration(400)
                .buildAndRegister();
        ModHandler.addShapedRecipe("bee_qihou_contral", CraftingReceipe.getItemStack("<gendustry:climate_module>"),
                "ADA",
                "BEB",
                "CFC",
                'A', CraftingReceipe.getItemStack("<gregtech:gt.comb:56>"),
                'B', new UnificationEntry(stick,Iridium),
                'C', CraftingReceipe.getItemStack("<gregtech:meta_item_1:701>"),
                'D', CraftingReceipe.getItemStack("<gregtech:meta_item_1:98>"),
                'E', new UnificationEntry(rotor, Osmiridium),
                'F', ELECTRIC_MOTOR_LuV
        );
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(Items.PAPER)
                .input(Items.STRING)
                .inputs(CraftingReceipe.getItemStack("<gregtech:meta_item_1:79>"))
                .outputs(CraftingReceipe.getItemStack("<gendustry:pollen_kit>"))
                .circuitMeta(24)
                .EUt(32)
                .duration(200)
                .buildAndRegister();
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ROBOT_ARM_LuV,4)
                .inputs(CraftingReceipe.getItemStack("<gregtech:metal_casing:2>", 4L))
                .inputs(CraftingReceipe.getItemStack("<extrabees:alveary:5>", 4L))
                .inputs(CraftingReceipe.getItemStack("<extrabees:alveary:4>", 2L))
                .inputs(CraftingReceipe.getItemStack("gendustry:mutagen_tank",2L))
                .inputs(CraftingReceipe.getItemStack("<gregtech:gt.comb:57>",2L))
                .inputs(CraftingReceipe.getItemStack("<forestry:hardened_machine>"))
                .outputs(CraftingReceipe.getItemStack("<gendustry:mutatron>"))
                .circuitMeta(24)
                .EUt(32000)
                .duration(400)
                .buildAndRegister();

        ModHandler.addShapedRecipe("drt_transposer", CraftingReceipe.getItemStack("<gendustry:transposer>"),
                "ADA",
                "BEB",
                "CFC",
                'A', ROBOT_ARM_LuV,
                'B', chacao,
                'C', chilun,
                'D', chilun,
                'E', bujian,
                'F', nengliang
        );
        ModHandler.addShapedRecipe("drt_imprinter", CraftingReceipe.getItemStack("<gendustry:imprinter>"),
                "ADA",
                "BEB",
                "CFC",
                'A', jiyin,
                'B', chacao,
                'C', chilun,
                'D', chilun,
                'E', bujian,
                'F', nengliang
        );
        ModHandler.addShapedRecipe("drt_sampler", CraftingReceipe.getItemStack("<gendustry:sampler>"),
                "ADA",
                "BEB",
                "CFC",
                'A', jiyin,
                'B', new UnificationEntry(plate,Diamond),
                'C', chilun,
                'D', chacao,
                'E', bujian,
                'F', nengliang
        );
        ModHandler.addShapedRecipe("drt_liquifier", CraftingReceipe.getItemStack("<gendustry:liquifier>"),
                "ADA",
                "BEB",
                "CFC",
                'A', chilun,
                'B', ELECTRIC_MOTOR_LuV,
                'C', chilun,
                'D', Blocks.FURNACE,
                'E', bujian,
                'F', nengliang
        );
        ModHandler.addShapedRecipe("drt_jiyintiqu", CraftingReceipe.getItemStack("<gendustry:extractor>"),
                "ADA",
                "BEB",
                "CFC",
                'A', jiyin,
                'B', ELECTRIC_PISTON_LUV,
                'C', chilun,
                'D', chacao,
                'E', bujian,
                'F', nengliang
        );
        ModHandler.addShapedRecipe("drt_replicator", CraftingReceipe.getItemStack("<gendustry:replicator>"),
                "ADA",
                "BEB",
                "CFC",
                'A', ROBOT_ARM_LuV,
                'B', nengliang,
                'C', chilun,
                'D', jiyin,
                'E', bujian,
                'F', jiyin
        );
    }
}
