package com.drppp.drtech.loaders;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.common.MetaTileEntities.MetaTileEntities;
import forestry.apiculture.ModuleApiculture;
import forestry.apiculture.blocks.BlockAlvearyType;
import gregtech.api.GTValues;
import gregtech.api.items.OreDictNames;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import keqing.gtqtcore.api.unification.GTQTMaterials;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static com.drppp.drtech.common.MetaTileEntities.MetaTileEntities.BENEATH_TRANS;
import static gregicality.multiblocks.api.unification.GCYMMaterials.MolybdenumDisilicide;
import static gregtech.api.GTValues.*;
import static gregtech.api.GTValues.MV;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;
import static gregtech.loaders.recipe.CraftingComponent.*;
import static gregtech.loaders.recipe.CraftingComponent.HULL;
import static gregtech.loaders.recipe.CraftingComponent.PUMP;
import static keqing.gtqtcore.common.items.GTQTMetaItems.DISK_8;

public class CraftingReceipe {
    public static void load()
    {
        GameRegistry.addSmelting(new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.LEATHER), 0.1F);
        ModHandler.addShapedRecipe("log_factory", MetaTileEntities.LOG_FACTORY.getStackForm(),
                "WMW", "EFE", "WMW",
                'W', MetaItems.FIELD_GENERATOR_IV,
                'E', new UnificationEntry(OrePrefix.plate, Materials.Iridium),
                'F', new ItemStack(BlocksInit.COMMON_CASING,1,2),
                'M', MetaItems.CONVEYOR_MODULE_IV);

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(gregtech.common.metatileentities.MetaTileEntities.HULL[3],16)
                .inputs(MetaItems.FIELD_GENERATOR_MV.getStackForm(16))
                .inputs(MetaItems.SENSOR_MV.getStackForm(16))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.HV, 4)
                .input(OrePrefix.pipeSmallFluid, Aluminium, 4)
                .input(OrePrefix.plate, StainlessSteel, 4)
                .fluidInputs(Polyethylene.getFluid(GTValues.L * 4))
                .scannerResearch(b -> b
                        .researchStack(DISK_8.getStackForm())
                        .duration(1200)
                        .EUt(VA[MV]))
                .output(BENEATH_TRANS)
                .duration(800).EUt(VA[MV]).buildAndRegister();

        ModHandler.addShapedRecipe("drone_pad", MetaTileEntities.DRONE_PAD.getStackForm(),
                "WMW", "EFE", "WMW",
                'W', MetaItems.ELECTRIC_PISTON_EV,
                'E', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel),
                'F', gregtech.common.metatileentities.MetaTileEntities.HULL[4].getStackForm(),
                'M', new UnificationEntry(circuit,MarkerMaterials.Tier.EV));

        ModHandler.addShapedRecipe("yot_tank", MetaTileEntities.YOUT_TANK.getStackForm(),
                "WAW", "EFE", "MBM",
                'W', new UnificationEntry(OrePrefix.screw,Materials.TungstenSteel),
                'E', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'F', new ItemStack(BlocksInit.COMMON_CASING,1,3),
                'M', MetaItems.FIELD_GENERATOR_LV,
                'A', new UnificationEntry(OrePrefix.plate,Materials.Polytetrafluoroethylene),
                'B',  new UnificationEntry(OrePrefix.rotor,Materials.StainlessSteel)
        );
        ModHandler.addShapedRecipe("tfft_tank", MetaTileEntities.TFFT.getStackForm(),
                "WAW", "EFE", "MBM",
                'W', new UnificationEntry(OrePrefix.screw,Materials.TungstenSteel),
                'E', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'F', new ItemStack(BlocksInit.COMMON_CASING,1,4),
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
            ModHandler.addShapedRecipe("electric_flight_ring", DrMetaItems.FLY_RING.getStackForm(),
                    "WSW", "SCS", "WSW",
                    'W', MetaItems.FIELD_GENERATOR_HV,
                    'C', MetaItems.ELECTRIC_JETPACK_ADVANCED,
                    'S', MetaItems.ENERGIUM_CRYSTAL
            );
            ModHandler.addShapedRecipe("electric_life_support_ring", DrMetaItems.LIFE_SUPPORT_RING.getStackForm(),
                    "WSW", "SCS", "WSW",
                    'W', MetaItems.FIELD_GENERATOR_HV,
                    'C', Items.NETHER_STAR,
                    'S', MetaItems.ENERGIUM_CRYSTAL
            );
        }
        ModHandler.addShapedRecipe(true, "electric_plasma_gun",DrMetaItems.ELECTRIC_PLASMA_GUN.getStackForm(),
                "JTG", "LSB", "DSB",
                        'D', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                        'S', MetaItems.ENERGIUM_CRYSTAL,
                        'B', new UnificationEntry(OrePrefix.plate,Materials.Titanium),
                        'L', MetaItems.FIELD_GENERATOR_HV,
                        'J',new UnificationEntry(OrePrefix.plate,Materials.Titanium),
                        'T',MetaItems.POWER_THRUSTER_ADVANCED,
                        'G',MetaItems.EMITTER_EV
        );
        ModHandler.addShapedRecipe(true, "tactical_laser_submachine_gun",DrMetaItems.TACTICAL_LASER_SUBMACHINE_GUN.getStackForm(),
                "JTG", "LSB", "DSB",
                        'D', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                        'S', MetaItems.ENERGIUM_CRYSTAL,
                        'B', new UnificationEntry(OrePrefix.plate,Materials.Iridium),
                        'L', MetaItems.FIELD_GENERATOR_HV,
                        'J',new UnificationEntry(OrePrefix.toolHeadDrill,Materials.Titanium),
                        'T',MetaItems.POWER_THRUSTER_ADVANCED,
                        'G',MetaItems.EMITTER_EV
                );
        ModHandler.addShapedRecipe(true, "advanced_tachino_disruptor",DrMetaItems.ADVANCED_TACHINO_DISRUPTOR.getStackForm(),
                "JBG", "DSB", "DSB",
                        'D', new UnificationEntry(circuit, MarkerMaterials.Tier.LuV),
                        'S', MetaItems.LAPOTRON_CRYSTAL,
                        'B', new UnificationEntry(OrePrefix.plate,Materials.Iridium),
                        'J',MetaItems.NANO_SABER,
                        'G',MetaItems.EMITTER_IV
                );
        ModHandler.addShapelessRecipe("peaceful_table", new ItemStack(ItemsInit.ITEM_BLOCK_PEACEFUL_TABLE),Blocks.CRAFTING_TABLE,Items.IRON_SWORD);

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

        ModHandler.addShapedRecipe("grass_killer", DrMetaItems.GRASS_KILLER.getStackForm(),
                "WSW", "SSS", "WSW",
                'W', Blocks.GRASS,
                'S', Items.IRON_INGOT
        );
        if(DrtConfig.MachineSwitch.EnableIndustrialMachines)
        {
            ModHandler.addShapedRecipe(true, "large_extruder", MetaTileEntities.LARGE_EXTRUDER.getStackForm(),
                    "LCL", "PSP", "OWO",
                    'L', new UnificationEntry(pipeLargeItem, Ultimet),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                    'S', gregtech.common.metatileentities.MetaTileEntities.EXTRUDER[EV].getStackForm(),
                    'P', MetaItems.ELECTRIC_PISTON_EV.getStackForm(),
                    'O', new UnificationEntry(spring, MolybdenumDisilicide),
                    'W', new UnificationEntry(cableGtSingle, Platinum));
        }
        ModHandler.addShapedRecipe(true, "storage_pail", new ItemStack(ItemsInit.ITEM_BLOCK_STORAGE_PAIL),
                "XXX", "XCX", "XXX",
                'X', Blocks.CHEST,'C',MetaItems.ELECTRIC_PISTON_LV);
        ModHandler.addShapelessRecipe("advanced_cauldron",new ItemStack(ItemsInit.ITEM_BLOCK_ADVANCED_CAULDRON),Items.CAULDRON);

        ModHandler.addShapedRecipe("type_filter", MetaTileEntities.TYPE_FILTER.getStackForm(),
                "WBW", "WFW", "WWW",
                'B',ToolItems.WRENCH,
                'W', Blocks.IRON_BARS,
                'F', gregtech.common.metatileentities.MetaTileEntities.HULL[0].getStackForm()
        );
        for (int i = 0; i < 10; i++) {
            ModHandler.addShapelessRecipe("huancun_energy_hatch"+i,MetaTileEntities.BATTERY_INPUT_ENERGY_HATCH[i].getStackForm(), gregtech.common.metatileentities.MetaTileEntities.ENERGY_INPUT_HATCH[i].getStackForm(), gregtech.common.metatileentities.MetaTileEntities.BATTERY_BUFFER[0][i].getStackForm());

        }
        ModHandler.addShapedRecipe("industrial_apiary", MetaTileEntities.INDUSTRIAL_APIARY.getStackForm(),
                "KBK",
                        "JSJ",
                        "RRR",
                'S', getItemStack("forestry:sturdy_machine",0),
                'J', MetaItems.ROBOT_ARM_HV,
                'K', ModuleApiculture.getBlocks().getAlvearyBlock(BlockAlvearyType.PLAIN),
                'R', getItemStack("extrabees:alveary",3),
                'B', getItemStack("forestry:chipsets",1)
        );
        ItemStack[] upgrade = {
                DrMetaItems.UPGRADE_SPEED1.getStackForm(),
                DrMetaItems.UPGRADE_SPEED2.getStackForm(),
                DrMetaItems.UPGRADE_SPEED3.getStackForm(),
                DrMetaItems.UPGRADE_SPEED4.getStackForm(),
                DrMetaItems.UPGRADE_SPEED5.getStackForm(),
                DrMetaItems.UPGRADE_SPEED6.getStackForm(),
                DrMetaItems.UPGRADE_SPEED7.getStackForm(),
                DrMetaItems.UPGRADE_SPEED8.getStackForm(),
                DrMetaItems.UPGRADE_SPEED8P.getStackForm(),
                DrMetaItems.UPGRADE_PRODUCTION.getStackForm(),
        };
        for (int i = 0; i < 8; i++) {
            ModHandler.addShapedRecipe("upgrade_speed"+i, upgrade[i].copy(),
                    "ACA",
                            "CSC",
                            "BCB",
                    'S', WORLD_ACCELERATOR[i].getStackForm(),
                    'C', DrMetaItems.UPGRADE_NULL,
                    'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                    'B',  new UnificationEntry(gearSmall, Materials.Steel)
            );
        }
        ModHandler.addShapedRecipe("upgrade_speed_ppp", upgrade[8].copy(),
                "AAA",
                "ASA",
                "AAA",
                'S',  upgrade[7],
                'A', DrMetaItems.UPGRADE_PRODUCTION
        );
        ModHandler.addShapedRecipe("upgrade_speed_production", DrMetaItems.UPGRADE_PRODUCTION.getStackForm(),
                "ACA",
                "DSD",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', Items.SUGAR,
                'D', getItemStack("forestry:royal_jelly",0),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_pingyuan", DrMetaItems.UPGRADE_PLAIN.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', getItemStack("gendustry:env_processor",0),
                'D', Blocks.GRASS,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_shamo", DrMetaItems.UPGRADE_DESERT_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', getItemStack("gendustry:env_processor",0),
                'D', Blocks.SAND,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_yanhan", DrMetaItems.UPGRADE_WINTER_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', getItemStack("gendustry:env_processor",0),
                'D', Blocks.SNOW,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_haiyang", DrMetaItems.UPGRADE_OCEAN_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', getItemStack("gendustry:env_processor",0),
                'D', Items.WATER_BUCKET,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_diyu", DrMetaItems.UPGRADE_HELL_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', getItemStack("gendustry:env_processor",0),
                'D', Blocks.NETHER_BRICK,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_conglin", DrMetaItems.UPGRADE_JUNGLE_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', getItemStack("gendustry:env_processor",0),
                'D', new ItemStack(Items.DYE,1,3),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_light", DrMetaItems.UPGRADE_LIGHT.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', Blocks.GLASS,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_shoufende", DrMetaItems.UPGRADE_FLOWERING.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', Blocks.RED_FLOWER,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_ganzao", DrMetaItems.UPGRADE_DRYER.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', Items.LAVA_BUCKET,
                'D', Blocks.SAND,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_jaishi", DrMetaItems.UPGRADE_HUMIDIFIER.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', Items.WATER_BUCKET,
                'D', Blocks.CACTUS,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_auto", DrMetaItems.UPGRADE_AUTOMATION.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', MetaItems.ROBOT_ARM_LV,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_huafen", DrMetaItems.UPGRADE_POLLEN_SCRUBBER.getStackForm(),
                "ADA",
                "CSC",
                "BDB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', new UnificationEntry(rotor, Materials.StainlessSteel),
                'D', MetaItems.ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_jiangwen", DrMetaItems.UPGRADE_COOLER.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', Blocks.ICE,
                'D', Blocks.SNOW,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_shengwen", DrMetaItems.UPGRADE_HEATER.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module",0),
                'E', Blocks.NETHERRACK,
                'D', Items.LAVA_BUCKET,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_lifespan", DrMetaItems.UPGRADE_LIFESPAN.getStackForm(),
                "ADA",
                "CSC",
                "BDB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', Blocks.CACTUS,
                'D', Items.FERMENTED_SPIDER_EYE,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_jiyiwending", DrMetaItems.UPGRADE_GENETIC_STABILIZER.getStackForm(),
                "ADA",
                "CSC",
                "BDB",
                'S', DrMetaItems.UPGRADE_NULL,
                'D', getItemStack("gendustry:genetics_processor",0),
                'C', new UnificationEntry(plate, RedAlloy),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_fanwei", DrMetaItems.UPGRADE_TERRITORY.getStackForm(),
                "ADA",
                "CSC",
                "BDB",
                'S', DrMetaItems.UPGRADE_NULL,
                'D', new UnificationEntry(plate, Iron),
                'C', new UnificationEntry(plate, EnderPearl),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_sky", DrMetaItems.UPGRADE_OPEN_SKY.getStackForm(),
                "ADA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'D', Blocks.REDSTONE_LAMP,
                'C', Blocks.GLASS_PANE,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_lvwang", DrMetaItems.UPGRADE_SIEVE.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("forestry:crafting_material",3),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_dark", DrMetaItems.UPGRADE_T.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', new UnificationEntry(plate, Steel),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_qimi", DrMetaItems.UPGRADE_SEAL.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', new UnificationEntry(plate, Rubber),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B',  new UnificationEntry(gearSmall, Materials.Steel)
        );
        if(DrtConfig.MachineSwitch.EnableIndustrialMachines)
        {
            ModHandler.addShapedRecipe("large_mixer_drt", MetaTileEntities.INDUSTRIAL_MIXER.getStackForm(),
                    "ACA",
                    "BSB",
                    "ACA",
                    'S', MIXER[IV].getStackForm(),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                    'A', new UnificationEntry(plate, GTQTMaterials.Staballoy),
                    'B',  new UnificationEntry(plate, GTQTMaterials.ZirconiumCarbide)
            );

            ModHandler.addShapedRecipe("larger_roller_press", MetaTileEntities.INDUSTRIAL_ROLLER_PRESS.getStackForm(),
                    "ACA",
                    "BSB",
                    "ACA",
                    'S', FORMING_PRESS[EV].getStackForm(),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                    'A', new UnificationEntry(plate, Titanium),
                    'B', BENDER[EV].getStackForm()

            );
            ModHandler.addShapedRecipe("larger_cable_press", MetaTileEntities.INDUSTRIAL_CABLE_PRESS.getStackForm(),
                    "ACA",
                    "BSB",
                    "ACA",
                    'S', WIREMILL[IV].getStackForm(),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                    'A', new UnificationEntry(plate, BlueSteel),
                    'B', gregtech.common.metatileentities.MetaTileEntities.HULL[IV].getStackForm()

            );
            ModHandler.addShapedRecipe("larger_sieve_drt", MetaTileEntities.INDUSTRIAL_SIEVE.getStackForm(),
                    "ACA",
                    "BSB",
                    "ACA",
                    'S', SIFTER[GTValues.HV].getStackForm(),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                    'A', new UnificationEntry(plate, GTQTMaterials.EglinSteel),
                    'B', new UnificationEntry(cableGtQuadruple,Gold)

            );
            ModHandler.addShapedRecipe("larger_centrifuge_drt", MetaTileEntities.INDUSTRIAL_CENTRIFUGE.getStackForm(),
                    "ACA",
                    "BSB",
                    "DCD",
                    'S', CENTRIFUGE[GTValues.EV].getStackForm(),
                    'A', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                    'B', new UnificationEntry(plate, GTQTMaterials.Inconel792),
                    'C', gregtech.common.metatileentities.MetaTileEntities.HULL[EV].getStackForm(),
                    'D', new UnificationEntry(plate, GTQTMaterials.MaragingSteel250)

            );
        }
        ModHandler.addShapedRecipe("water_mill", new ItemStack(BlocksInit.BLOCK_WATER_MILL),
                "ABA",
                "BCB",
                "ABA",
                'C', new UnificationEntry(screw, Iron),
                'A', new UnificationEntry(frameGt, TreatedWood),
                'B', new UnificationEntry(plate,TreatedWood)

        );
        ModHandler.addShapedRecipe("wood_axle", new ItemStack(BlocksInit.BLOCK_WOOD_AXLE),
                "AAA", "BCB", "AAA",
                'C', new UnificationEntry(stick, Iron),
                'B', new UnificationEntry(screw, Iron),
                'A', new UnificationEntry(plate,TreatedWood)
        );
        ModHandler.addShapedRecipe("ru_macerator", MetaTileEntities.RU_MACERATOR.getStackForm(),
                "DXD", "XMX", "PXP",
                'M', Blocks.BRICK_BLOCK,
                'X', new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Copper),
                'P', OreDictNames.craftingPiston,
                'D', new UnificationEntry(OrePrefix.gem, Materials.Diamond));

        ModHandler.addShapedRecipe("ru_mixer", MetaTileEntities.RU_MIXER.getStackForm(),
                "AGA", "BCB", "AGA",
                'C', Blocks.BRICK_BLOCK,
                'G', new UnificationEntry(rotor, Iron),
                'B', new UnificationEntry(screw, Iron),
                'A', new UnificationEntry(plate,Copper));

        ModHandler.addShapedRecipe( "ru_compressor", MetaTileEntities.RU_COMPRESSOR.getStackForm(),
                "XXX", "PMP", "XXX",
                'M', Blocks.BRICK_BLOCK,
                'X',  new UnificationEntry(plate, Materials.Iron),
                'P', OreDictNames.craftingPiston);

        ModHandler.addShapedRecipe( "ru_extruder", MetaTileEntities.RU_EXTRUDER.getStackForm(),
                "GPG", "SMS", "GXG",
                'M', Blocks.BRICK_BLOCK,
                'X',  new UnificationEntry(plate, Materials.Iron),
                'S', new UnificationEntry(screw, Materials.Tin),
                'P', OreDictNames.craftingPiston,
                'G', new UnificationEntry(spring, Materials.Copper));

        ModHandler.addShapedRecipe( "ru_sifter", MetaTileEntities.RU_SIFTER.getStackForm(),
                "GPG", "SMS", "GXG",
                'M', Blocks.BRICK_BLOCK,
                'X',  new UnificationEntry(plate, Materials.Iron),
                'S', new UnificationEntry(spring, Materials.Copper),
                'P', OreDictNames.craftingPiston,
                'G', new UnificationEntry(OrePrefix.pipeSmallFluid, Materials.Copper));

        ModHandler.addShapedRecipe("ru_hammer", MetaTileEntities.RU_HAMMER.getStackForm(),
                "GPG", "SMS", "GXG",
                'M', Blocks.BRICK_BLOCK,
                'X',  new UnificationEntry(plate, Materials.Iron),
                'S', new UnificationEntry(spring, Materials.Copper),
                'P', OreDictNames.craftingPiston,
                'G', new UnificationEntry(screw, Materials.Tin));

        ModHandler.addShapedRecipe( "ru_wiremill", MetaTileEntities.RU_WIREMILL.getStackForm(),
                "XXX", "PMG", "XXX",
                'M', Blocks.BRICK_BLOCK,
                'X',  new UnificationEntry(stick, Materials.Tin),
                'P', OreDictNames.craftingPiston,
                'G', new UnificationEntry(screw, Materials.Iron));

        ModHandler.addShapedRecipe("ru_bender", MetaTileEntities.RU_BENDER.getStackForm(),
                "XXX", "PMG", "XXX",
                'M', Blocks.BRICK_BLOCK,
                'X',  new UnificationEntry(plate, Materials.Iron),
                'P', OreDictNames.craftingPiston,
                'G', new UnificationEntry(ring, Materials.Iron));

        ModHandler.addShapedRecipe("ru_centrifuge", MetaTileEntities.RU_CENTRIFUGE.getStackForm(),
                "XSX", "PMP", "XSX",
                'M', Blocks.BRICK_BLOCK,
                'X',  new UnificationEntry(plate, Materials.Iron),
                'S', new UnificationEntry(spring, Materials.Copper),
                'P', OreDictNames.craftingPiston);

        ModHandler.addShapedRecipe("ru_generator", MetaTileEntities.RU_GENERATOR.getStackForm(),
                "PCP",
                "RMR",
                "EWE",
                'M', gregtech.common.metatileentities.MetaTileEntities.HULL[GTValues.LV].getStackForm(),
                'E', MetaItems.ELECTRIC_MOTOR_LV,
                'R', new UnificationEntry(gear, Tin),
                'C', new UnificationEntry(OrePrefix.cableGtSingle, Tin),
                'W', new UnificationEntry(OrePrefix.cableGtSingle, Tin),
                'P', new UnificationEntry(OrePrefix.gear, Materials.Bronze));

        ModHandler.removeRecipeByName("gregtech:dust_bronze");
        ModHandler.removeRecipeByName("gregtech:dust_brass");
        ModHandler.removeRecipeByName("gregtech:potin_dust");
    }
    public static ItemStack getItemStack(String itemstr)
    {
        return  getItemStack(itemstr, 0);
    }
    public static ItemStack getItemStack(String itemstr,long num)
    {
        var item = getItemStack(itemstr, 0);
        item.setCount((int)num);
        return item;
    }
    public static ItemStack getItemStack(String itemstr, int meta)
    {
        if(itemstr.startsWith("<") && itemstr.endsWith(">"))
            itemstr = itemstr.substring(1,itemstr.length()-1);
        String[] str = itemstr.split(":");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(str[0], str[1]));
        if (item != null) {
            if(str.length==3)
                return new ItemStack(item, 1,Integer.parseInt(str[2]));
            return new ItemStack(item, 1,meta);
        } else {
            return ItemStack.EMPTY;
        }
    }
}
