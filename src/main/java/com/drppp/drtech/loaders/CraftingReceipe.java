package com.drppp.drtech.loaders;

import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.MetaItems.MetaItems1;
import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import com.drppp.drtech.common.MetaTileEntities.MetaTileEntities;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import keqing.gtqtcore.common.metatileentities.GTQTMetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import static gregicality.multiblocks.api.unification.GCYMMaterials.MolybdenumDisilicide;
import static gregtech.api.GTValues.EV;
import static gregtech.api.GTValues.IV;
import static gregtech.api.unification.material.Materials.Platinum;
import static gregtech.api.unification.material.Materials.Ultimet;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.metatileentities.MetaTileEntities.ELECTRIC_BLAST_FURNACE;
import static gregtech.common.metatileentities.MetaTileEntities.FISHER;
import static gregtech.loaders.recipe.CraftingComponent.*;

public class CraftingReceipe {
    public static void load()
    {
        ModHandler.addShapedRecipe("log_factory", MetaTileEntities.LOG_FACTORY.getStackForm(),
                "WMW", "EFE", "WMW",
                'W', MetaItems.FIELD_GENERATOR_IV,
                'E', new UnificationEntry(OrePrefix.plate, Materials.Iridium),
                'F', new ItemStack(BlocksInit.COMMON_CASING,1,8),
                'M', MetaItems.CONVEYOR_MODULE_IV);
        ModHandler.addShapedRecipe("yot_tank", MetaTileEntities.YOUT_TANK.getStackForm(),
                "WAW", "EFE", "MBM",
                'W', new UnificationEntry(OrePrefix.screw,Materials.TungstenSteel),
                'E', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'F', new ItemStack(BlocksInit.COMMON_CASING,1,9),
                'M', MetaItems.FIELD_GENERATOR_LV,
                'A', new UnificationEntry(OrePrefix.plate,Materials.Polytetrafluoroethylene),
                'B',  new UnificationEntry(OrePrefix.rotor,Materials.StainlessSteel)
        );
        ModHandler.addShapedRecipe("tfft_tank", MetaTileEntities.TFFT.getStackForm(),
                "WAW", "EFE", "MBM",
                'W', new UnificationEntry(OrePrefix.screw,Materials.TungstenSteel),
                'E', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'F', new ItemStack(BlocksInit.COMMON_CASING,1,10),
                'M', MetaItems.FIELD_GENERATOR_LV,
                'A', new UnificationEntry(OrePrefix.plate,Materials.Polytetrafluoroethylene),
                'B',  new UnificationEntry(OrePrefix.rotor,Materials.StainlessSteel)
        );
        ModHandler.addShapedRecipe("mob_killer", MetaTileEntities.MOB_KILLER.getStackForm(),
                "WAW", "AFA", "SSS",
                'W',  MetaItems.ROBOT_ARM_HV,
                'A', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'F', new ItemStack(MetaBlocks.MACHINE,1,988),
                'S', Items.DIAMOND_SWORD
        );
        ModHandler.addShapedRecipe("advanced_process_array", MetaTileEntities.ADVANCED_PROCESS_ARRAY.getStackForm(),
                "WWW", "WSW", "WWW",
                'W',  MetaBlocks.TRANSPARENT_CASING,
                'S', new ItemStack(MetaBlocks.MACHINE,1,1031)
        );
        ModHandler.addShapedRecipe("trans_tower", MetaTileEntities.TRANS_TOWER.getStackForm(),
                "WAW", "DSD", "WAW",
                'W', new UnificationEntry(OrePrefix.plateDouble,Materials.Copper),
                'D', new UnificationEntry(OrePrefix.plateDouble,Materials.Silver),
                'S', new ItemStack(MetaBlocks.MACHINE,1,986),
                'A', new UnificationEntry(circuit, MarkerMaterials.Tier.LV)
        );
        ModHandler.addShapedRecipe("connector_1", new ItemStack(BlocksInit.BLOCK_CONNECTOR1),
                "WAW", "WSW", "WAW",
                'W',  new UnificationEntry(OrePrefix.plate,Materials.Lead),
                'S', new ItemStack(MetaBlocks.MACHINE,1,986),
                'A', new UnificationEntry(OrePrefix.plate,Materials.Polyethylene)
        );
        ModHandler.addShapedRecipe("connector_2", new ItemStack(BlocksInit.BLOCK_CONNECTOR2),
                "WAW", "WSW", "WAW",
                'W',  new UnificationEntry(OrePrefix.plate,Materials.Aluminium),
                'S', new ItemStack(MetaBlocks.MACHINE,1,987),
                'A', new UnificationEntry(OrePrefix.plate,Materials.Polyethylene)
        );
        ModHandler.addShapedRecipe("connector_3", new ItemStack(BlocksInit.BLOCK_CONNECTOR3),
                "WAW", "WSW", "WAW",
                'W',  new UnificationEntry(OrePrefix.plate,Materials.Electrum),
                'S', new ItemStack(MetaBlocks.MACHINE,1,988),
                'A', new UnificationEntry(OrePrefix.plate,Materials.Polyethylene)
        );
        ModHandler.addShapedRecipe("golden_sea", new ItemStack(BlocksInit.BLOCK_GOLDEN_SEA),
                "WWW", "WSW", "WWW",
                'W', Blocks.GOLD_BLOCK,
                'S', new UnificationEntry(OrePrefix.plate,Materials.Gold)
        );
        ModHandler.addShapedRecipe(true, "electric_implosion_compressor", MetaTileEntities.LARGE_LARGE.getStackForm(),
                new Object[]{"PCP", "FSF", "PCP", 'C',
                        new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                        'S', gregtech.common.metatileentities.MetaTileEntities.IMPLOSION_COMPRESSOR.getStackForm(),
                        'P', MetaItems.ELECTRIC_PISTON_EV.getStackForm(),
                        'F', MetaItems.FIELD_GENERATOR_EV.getStackForm()});

        if (Loader.isModLoaded("baubles"))
        {
            ModHandler.addShapedRecipe("electric_flight_ring", MyMetaItems.FLY_RING.getStackForm(),
                    "WSW", "SCS", "WSW",
                    'W', MetaItems.FIELD_GENERATOR_HV,
                    'C', MetaItems.ELECTRIC_JETPACK_ADVANCED,
                    'S', MetaItems.ENERGIUM_CRYSTAL
            );
            ModHandler.addShapedRecipe("electric_life_support_ring", MyMetaItems.LIFE_SUPPORT_RING.getStackForm(),
                    "WSW", "SCS", "WSW",
                    'W', MetaItems.FIELD_GENERATOR_HV,
                    'C', Items.NETHER_STAR,
                    'S', MetaItems.ENERGIUM_CRYSTAL
            );
        }
        ModHandler.addShapedRecipe(true, "electric_plasma_gun",MyMetaItems.ELECTRIC_PLASMA_GUN.getStackForm(),
                "JTG", "LSB", "DSB",
                        'D', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                        'S', MetaItems.ENERGIUM_CRYSTAL,
                        'B', new UnificationEntry(OrePrefix.plate,Materials.Titanium),
                        'L', MetaItems.FIELD_GENERATOR_HV,
                        'J',new UnificationEntry(OrePrefix.plate,Materials.Titanium),
                        'T',MetaItems.POWER_THRUSTER_ADVANCED,
                        'G',MetaItems.EMITTER_EV
        );
        ModHandler.addShapedRecipe(true, "tactical_laser_submachine_gun",MyMetaItems.TACTICAL_LASER_SUBMACHINE_GUN.getStackForm(),
                "JTG", "LSB", "DSB",
                        'D', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                        'S', MetaItems.ENERGIUM_CRYSTAL,
                        'B', new UnificationEntry(OrePrefix.plate,Materials.Iridium),
                        'L', MetaItems.FIELD_GENERATOR_HV,
                        'J',new UnificationEntry(OrePrefix.toolHeadDrill,Materials.Titanium),
                        'T',MetaItems.POWER_THRUSTER_ADVANCED,
                        'G',MetaItems.EMITTER_EV
                );
        ModHandler.addShapedRecipe(true, "advanced_tachino_disruptor",MyMetaItems.ADVANCED_TACHINO_DISRUPTOR.getStackForm(),
                "JBG", "DSB", "DSB",
                        'D', new UnificationEntry(circuit, MarkerMaterials.Tier.LuV),
                        'S', MetaItems.LAPOTRON_CRYSTAL,
                        'B', new UnificationEntry(OrePrefix.plate,Materials.Iridium),
                        'J',MetaItems.NANO_SABER,
                        'G',MetaItems.EMITTER_IV
                );
        ModHandler.addShapelessRecipe("peaceful_table", new ItemStack(ItemsInit.ITEM_BLOCK_PEACEFUL_TABLE),Blocks.CRAFTING_TABLE,Items.IRON_SWORD);
        gregtech.loaders.recipe.MetaTileEntityLoader.registerMachineRecipe(true, MetaTileEntities.UU_PRODUCTER,
                "WSW", "XCX", "WSW",
                'W',CIRCUIT,
                'C', CASING,
                'S', FIELD_GENERATOR,
                'X',CABLE_QUAD);
        gregtech.loaders.recipe.MetaTileEntityLoader.registerMachineRecipe(true, MetaTileEntities.DUPLICATOR,
                "WSW", "XCX", "WDW",
                'W', EMITTER,
                'C', CASING,
                'D',CABLE_QUAD,
                'S', FIELD_GENERATOR,
                'X',CIRCUIT);


        ModHandler.addShapedRecipe("sap_bag", new ItemStack(BlocksInit.BLOCK_SAP_BAG),
                "WWW", "SCS", "SSS",
                'W', Items.LEATHER,
                'S', Blocks.HARDENED_CLAY,
                'C', ToolItems.SAW
        );
        gregtech.loaders.recipe.MetaTileEntityLoader.registerMachineRecipe(MetaTileEntities.DISASSEMBLY, "AVA", "VMV", "WCW", 'M', HULL, 'V', CONVEYOR, 'A', ROBOT_ARM,
                'C', CIRCUIT, 'W', CABLE);
        gregtech.loaders.recipe.MetaTileEntityLoader.registerMachineRecipe(MetaTileEntities.UNIVERSAL_COLLECTORS, "WFW", "VHP", "WCW", 'W', Blocks.IRON_BARS, 'F',
                MetaItems.FLUID_FILTER, 'P', PUMP, 'H', HULL, 'C', CIRCUIT,'V', CONVEYOR);

        ModHandler.addShapedRecipe(true,"large_alloy_smelter", MetaTileEntities.LARGE_ALLOY_SMELTER.getStackForm(),
                "ADA", "WSW", "WWW",
                'W',  new ItemStack(MetaBlocks.METAL_CASING,1,2),
                'A',   new UnificationEntry(circuit, MarkerMaterials.Tier.MV),
                'S', ELECTRIC_BLAST_FURNACE.getStackForm(),
                'D',new UnificationEntry(OrePrefix.plate, Materials.Aluminium)
        );


        ModHandler.addShapedRecipe(true,"large_alloy_smelter", MetaTileEntities.DRONE_PAD.getStackForm(),
                "ADA", "WSW", "WWW",
                'W',  new ItemStack(MetaBlocks.METAL_CASING,1,3),
                'A',   new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'S', FISHER[3].getStackForm(),
                'D',new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel)
        );

        ModHandler.addShapedRecipe("grass_killer", MyMetaItems.GRASS_KILLER.getStackForm(),
                "WSW", "SSS", "WSW",
                'W', Blocks.GRASS,
                'S', Items.IRON_INGOT
        );
        ModHandler.addShapedRecipe(true, "large_extruder", MetaTileEntities.LARGE_EXTRUDER.getStackForm(),
                "LCL", "PSP", "OWO",
                'L', new UnificationEntry(pipeLargeItem, Ultimet),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'S', gregtech.common.metatileentities.MetaTileEntities.EXTRUDER[EV].getStackForm(),
                'P', MetaItems.ELECTRIC_PISTON_EV.getStackForm(),
                'O', new UnificationEntry(spring, MolybdenumDisilicide),
                'W', new UnificationEntry(cableGtSingle, Platinum));
        ModHandler.addShapedRecipe(true, "storage_pail", new ItemStack(ItemsInit.ITEM_BLOCK_STORAGE_PAIL),
                "XXX", "XCX", "XXX",
                'X', Blocks.CHEST,'C',MetaItems.ELECTRIC_PISTON_LV);
    }
}
