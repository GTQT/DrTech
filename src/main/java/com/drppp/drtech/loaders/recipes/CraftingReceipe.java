package com.drppp.drtech.loaders.recipes;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorCasing;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorTieredCasing;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFusionReactorTieredCasing2;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.common.drone.hardware.DroneChassisTier;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeType;
import com.drppp.drtech.common.MetaTileEntities.DrTechMetaTileEntities;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;


import static gregtech.api.GTValues.EV;
import static gregtech.api.GTValues.IV;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;
import static gregtech.loaders.recipe.CraftingComponent.HULL;
import static gregtech.loaders.recipe.CraftingComponent.PUMP;
import static gregtech.loaders.recipe.CraftingComponent.*;

public class CraftingReceipe {
    public static void load() {
        updateChip();
        multiblock();

        GameRegistry.addSmelting(new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.LEATHER), 0.1F);

        ModHandler.addShapedRecipe("drone_pad", DrTechMetaTileEntities.DRONE_PAD.getStackForm(),
                "WMW", "EFE", "WMW",
                'W', MetaItems.ELECTRIC_PISTON_EV,
                'E', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel),
                'F', gregtech.common.metatileentities.MetaTileEntities.HULL[4].getStackForm(),
                'M', new UnificationEntry(circuit, MarkerMaterials.Tier.EV));

        ModHandler.addShapedRecipe("drtech_drone_program_card", new ItemStack(ItemsInit.DRONE_PROGRAM_CARD),
                "PCP", "RGR", "PWP",
                'P', new UnificationEntry(plate, Polyethylene),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.MV),
                'R', new UnificationEntry(wireFine, RedAlloy),
                'G', Blocks.GLASS_PANE,
                'W', Items.PAPER);

        ModHandler.addShapedRecipe("drtech_programmable_drone", new ItemStack(ItemsInit.PROGRAMMABLE_DRONE),
                "RMR", "ECE", "PBP",
                'R', new UnificationEntry(rotor, Titanium),
                'M', MetaItems.ELECTRIC_MOTOR_HV,
                'E', MetaItems.EMITTER_HV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'P', new UnificationEntry(plate, Titanium),
                'B', MetaItems.ENERGIUM_CRYSTAL);

        ModHandler.addShapedRecipe("drtech_programmable_drone_ev",
                new ItemStack(ItemsInit.PROGRAMMABLE_DRONE, 1, DroneChassisTier.EV.getMetadata()),
                "RMR", "EDE", "PCP",
                'R', new UnificationEntry(rotor, TungstenSteel),
                'M', MetaItems.ELECTRIC_MOTOR_EV,
                'E', MetaItems.EMITTER_EV,
                'D', new ItemStack(ItemsInit.PROGRAMMABLE_DRONE, 1, DroneChassisTier.HV.getMetadata()),
                'P', new UnificationEntry(plate, TungstenSteel),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV));

        ModHandler.addShapedRecipe("drtech_programmable_drone_iv",
                new ItemStack(ItemsInit.PROGRAMMABLE_DRONE, 1, DroneChassisTier.IV.getMetadata()),
                "RMR", "EDE", "PCP",
                'R', new UnificationEntry(rotor, Osmiridium),
                'M', MetaItems.ELECTRIC_MOTOR_IV,
                'E', MetaItems.EMITTER_IV,
                'D', new ItemStack(ItemsInit.PROGRAMMABLE_DRONE, 1, DroneChassisTier.EV.getMetadata()),
                'P', new UnificationEntry(plate, Osmiridium),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.IV));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_battery", droneUpgrade(DroneUpgradeType.BATTERY),
                "PCP", "EBE", "PCP",
                'P', new UnificationEntry(plate, Titanium),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'E', MetaItems.ENERGIUM_CRYSTAL,
                'B', new UnificationEntry(wireFine, Platinum));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_propulsion", droneUpgrade(DroneUpgradeType.PROPULSION),
                "PRP", "RMR", "PCP",
                'P', new UnificationEntry(plate, Titanium),
                'R', new UnificationEntry(rotor, Titanium),
                'M', MetaItems.ELECTRIC_MOTOR_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_efficiency", droneUpgrade(DroneUpgradeType.EFFICIENCY),
                "WCW", "SES", "WCW",
                'W', new UnificationEntry(wireFine, Platinum),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'S', MetaItems.SENSOR_EV,
                'E', MetaItems.EMITTER_EV);

        ModHandler.addShapedRecipe("drtech_drone_upgrade_cargo", droneUpgrade(DroneUpgradeType.CARGO),
                "PHP", "RCR", "PMP",
                'P', new UnificationEntry(plate, Titanium),
                'H', Blocks.CHEST,
                'R', MetaItems.ROBOT_ARM_HV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'M', MetaItems.ELECTRIC_MOTOR_HV);

        ModHandler.addShapedRecipe("drtech_drone_upgrade_wireless", droneUpgrade(DroneUpgradeType.WIRELESS),
                "WEW", "SCS", "WPW",
                'W', new UnificationEntry(wireFine, Electrum),
                'E', MetaItems.EMITTER_EV,
                'S', MetaItems.SENSOR_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'P', new UnificationEntry(plate, Titanium));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_fluid_cargo",
                droneUpgrade(DroneUpgradeType.FLUID_CARGO),
                "PBP", "UCU", "PPP",
                'P', new UnificationEntry(plate, Titanium),
                'B', Items.BUCKET,
                'U', MetaItems.ELECTRIC_PUMP_HV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_crafting",
                droneUpgrade(DroneUpgradeType.CRAFTING),
                "PGP", "RCR", "PMP",
                'P', new UnificationEntry(plate, Titanium),
                'G', Blocks.CRAFTING_TABLE,
                'R', MetaItems.ROBOT_ARM_HV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'M', MetaItems.ELECTRIC_MOTOR_HV);

        ModHandler.addShapedRecipe("drtech_drone_upgrade_advanced_navigation",
                droneUpgrade(DroneUpgradeType.ADVANCED_NAVIGATION),
                "SES", "ECE", "PIP",
                'S', MetaItems.SENSOR_EV,
                'E', MetaItems.EMITTER_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'P', new UnificationEntry(plate, Titanium),
                'I', Items.ENDER_EYE);

        ModHandler.addShapedRecipe("drtech_drone_upgrade_eu_interface",
                droneUpgrade(DroneUpgradeType.EU_INTERFACE),
                "WCW", "EPE", "WCW",
                'W', new UnificationEntry(cableGtSingle, Aluminium),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'E', MetaItems.EMITTER_EV,
                'P', new UnificationEntry(plate, Titanium));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_tool_arm",
                droneUpgrade(DroneUpgradeType.TOOL_ARM),
                "PRP", "MCM", "PAP",
                'P', new UnificationEntry(plate, Titanium),
                'R', MetaItems.ROBOT_ARM_EV,
                'M', MetaItems.ELECTRIC_MOTOR_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'A', MetaItems.ROBOT_ARM_HV);

        ModHandler.addShapedRecipe("drtech_drone_upgrade_entity_scanner",
                droneUpgrade(DroneUpgradeType.ENTITY_SCANNER),
                "WSW", "ECE", "WPW",
                'W', new UnificationEntry(wireFine, Electrum),
                'S', MetaItems.SENSOR_EV,
                'E', MetaItems.EMITTER_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'P', new UnificationEntry(plate, Titanium));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_combat",
                droneUpgrade(DroneUpgradeType.COMBAT),
                "PSP", "RCR", "PMP",
                'P', new UnificationEntry(plate, Titanium),
                'S', Items.DIAMOND_SWORD,
                'R', MetaItems.ROBOT_ARM_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'M', MetaItems.ELECTRIC_MOTOR_EV);

        ModHandler.addShapedRecipe("drtech_drone_upgrade_entity_containment",
                droneUpgrade(DroneUpgradeType.ENTITY_CONTAINMENT),
                "PEP", "SCS", "PEP",
                'P', new UnificationEntry(plate, Titanium),
                'E', Items.ENDER_PEARL,
                'S', MetaItems.SENSOR_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_waterproof",
                droneUpgrade(DroneUpgradeType.WATERPROOF),
                "RPR", "PBP", "RCR",
                'R', new UnificationEntry(plate, SiliconeRubber),
                'P', new UnificationEntry(plate, Titanium),
                'B', Items.WATER_BUCKET,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_self_repair",
                droneUpgrade(DroneUpgradeType.SELF_REPAIR),
                "PRP", "ACA", "PMP",
                'P', new UnificationEntry(plate, Titanium),
                'R', MetaItems.ROBOT_ARM_EV,
                'A', MetaItems.ELECTRIC_PISTON_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'M', MetaItems.ELECTRIC_MOTOR_EV);

        ModHandler.addShapedRecipe("drtech_drone_upgrade_secure_access",
                droneUpgrade(DroneUpgradeType.SECURE_ACCESS),
                "SES", "ECE", "PIP",
                'S', MetaItems.SENSOR_EV,
                'E', MetaItems.EMITTER_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'P', new UnificationEntry(plate, Titanium),
                'I', Items.ENDER_EYE);

        ModHandler.addShapedRecipe("drtech_drone_upgrade_advanced_item_handling",
                droneUpgrade(DroneUpgradeType.ADVANCED_ITEM_HANDLING),
                "RCR", "PMP", "RCR",
                'R', MetaItems.ROBOT_ARM_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'P', new UnificationEntry(plate, Titanium),
                'M', MetaItems.ELECTRIC_MOTOR_EV);

        ModHandler.addShapedRecipe("drtech_drone_upgrade_fleet_communication",
                droneUpgrade(DroneUpgradeType.FLEET_COMMUNICATION),
                "AEA", "ECE", "PWP",
                'A', MetaItems.SENSOR_EV,
                'E', MetaItems.EMITTER_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'P', new UnificationEntry(plate, Titanium),
                'W', new UnificationEntry(wireFine, Electrum));

        ModHandler.addShapedRecipe("drtech_drone_upgrade_fishing",
                droneUpgrade(DroneUpgradeType.FISHING),
                "PRP", "FCF", "PMP",
                'P', new UnificationEntry(plate, Titanium),
                'R', MetaItems.ROBOT_ARM_EV,
                'F', Items.FISHING_ROD,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'M', MetaItems.ELECTRIC_MOTOR_EV);

        if (Loader.isModLoaded("thaumcraft")) {
            Item thaumcraftFilter = ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", "filter"));
            Item thaumcraftPhial = ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", "phial"));
            if (thaumcraftFilter != null && thaumcraftPhial != null) {
                ModHandler.addShapedRecipe("drtech_drone_upgrade_thaumcraft_alchemy",
                        droneUpgrade(DroneUpgradeType.THAUMCRAFT_ALCHEMY),
                        "PFP", "RCR", "PHP",
                        'P', new UnificationEntry(plate, Titanium),
                        'F', thaumcraftFilter,
                        'R', MetaItems.ROBOT_ARM_EV,
                        'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                        'H', thaumcraftPhial);
            }
        }


        ModHandler.addShapedRecipe("drtech_drone_programmer", DrTechMetaTileEntities.DRONE_PROGRAMMER.getStackForm(),
                "SCS", "RHR", "WCW",
                'S', MetaItems.SENSOR_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'R', MetaItems.ROBOT_ARM_EV,
                'H', gregtech.common.metatileentities.MetaTileEntities.HULL[EV].getStackForm(),
                'W', new UnificationEntry(cableGtSingle, Aluminium));

        ModHandler.addShapedRecipe("drtech_drone_fleet_controller",
                DrTechMetaTileEntities.DRONE_FLEET_CONTROLLER.getStackForm(),
                "SES", "CHC", "WCW",
                'S', MetaItems.SENSOR_EV,
                'E', MetaItems.EMITTER_EV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'H', gregtech.common.metatileentities.MetaTileEntities.HULL[EV].getStackForm(),
                'W', new UnificationEntry(cableGtSingle, Aluminium));

        ModHandler.addShapedRecipe("drtech_drone_item_endpoint", DrTechMetaTileEntities.DRONE_ITEM_ENDPOINT.getStackForm(),
                "PCP", "RHR", "WCW",
                'P', new UnificationEntry(plate, Titanium),
                'C', Blocks.CHEST,
                'R', MetaItems.ROBOT_ARM_EV,
                'H', gregtech.common.metatileentities.MetaTileEntities.HULL[EV].getStackForm(),
                'W', new UnificationEntry(cableGtSingle, Aluminium));

        ModHandler.addShapedRecipe("drtech_drone_fluid_endpoint", DrTechMetaTileEntities.DRONE_FLUID_ENDPOINT.getStackForm(),
                "PBP", "UHU", "WBW",
                'P', new UnificationEntry(plate, Titanium),
                'B', Items.BUCKET,
                'U', MetaItems.ELECTRIC_PUMP_EV,
                'H', gregtech.common.metatileentities.MetaTileEntities.HULL[EV].getStackForm(),
                'W', new UnificationEntry(cableGtSingle, Aluminium));

        ModHandler.addShapedRecipe("drtech_drone_eu_endpoint", DrTechMetaTileEntities.DRONE_EU_ENDPOINT.getStackForm(),
                "PCP", "EHE", "WCW",
                'P', new UnificationEntry(plate, Titanium),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'E', MetaItems.ENERGIUM_CRYSTAL,
                'H', gregtech.common.metatileentities.MetaTileEntities.HULL[EV].getStackForm(),
                'W', new UnificationEntry(cableGtSingle, Aluminium));

        ModHandler.addShapedRecipe("drtech_drone_dock", DrTechMetaTileEntities.DRONE_DOCK.getStackForm(),
                "PWP", "CHC", "PWP",
                'P', new UnificationEntry(plate, StainlessSteel),
                'W', new UnificationEntry(cableGtSingle, Gold),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'H', gregtech.common.metatileentities.MetaTileEntities.HULL[gregtech.api.GTValues.HV].getStackForm());

        ModHandler.addShapedRecipe("drtech_drone_dock_ev", DrTechMetaTileEntities.DRONE_DOCK_EV.getStackForm(),
                "PWP", "CDC", "PWP",
                'P', new UnificationEntry(plate, Titanium),
                'W', new UnificationEntry(cableGtSingle, Aluminium),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                'D', DrTechMetaTileEntities.DRONE_DOCK.getStackForm());

        ModHandler.addShapedRecipe("drtech_drone_dock_iv", DrTechMetaTileEntities.DRONE_DOCK_IV.getStackForm(),
                "PWP", "CDC", "PWP",
                'P', new UnificationEntry(plate, TungstenSteel),
                'W', new UnificationEntry(cableGtSingle, Platinum),
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                'D', DrTechMetaTileEntities.DRONE_DOCK_EV.getStackForm());

        ModHandler.addShapedRecipe("drtech_drone_redstone_emitter",
                DrTechMetaTileEntities.DRONE_REDSTONE_EMITTER.getStackForm(),
                "RER", "CHC", "RWR",
                'R', new UnificationEntry(dust, Redstone),
                'E', MetaItems.EMITTER_HV,
                'C', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'H', gregtech.common.metatileentities.MetaTileEntities.HULL[gregtech.api.GTValues.HV].getStackForm(),
                'W', new UnificationEntry(wireFine, RedAlloy));

        ModHandler.addShapedRecipe("yot_tank", DrTechMetaTileEntities.YOUT_TANK.getStackForm(),
        "WAW", "EFE", "MBM",
        'W', new UnificationEntry(OrePrefix.screw, Materials.TungstenSteel),
        'E', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
        'F', new ItemStack(BlocksInit.COMMON_CASING, 1, 3),
        'M', MetaItems.FIELD_GENERATOR_LV,
        'A', new UnificationEntry(OrePrefix.plate, Materials.Polytetrafluoroethylene),
        'B', new UnificationEntry(OrePrefix.rotor, Materials.StainlessSteel)
        );

        ModHandler.addShapedRecipe("tfft_tank", DrTechMetaTileEntities.TFFT.getStackForm(),
        "WAW", "EFE", "MBM",
        'W', new UnificationEntry(OrePrefix.screw, Materials.TungstenSteel),
        'E', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
        'F', new ItemStack(BlocksInit.COMMON_CASING, 1, 4),
        'M', MetaItems.FIELD_GENERATOR_LV,
        'A', new UnificationEntry(OrePrefix.plate, Materials.Polytetrafluoroethylene),
        'B', new UnificationEntry(OrePrefix.rotor, Materials.StainlessSteel)
        );

        ModHandler.addShapedRecipe("mob_killer", DrTechMetaTileEntities.MOB_KILLER.getStackForm(),
                "WAW", "AFA", "SSS",
                'W', MetaItems.ROBOT_ARM_HV,
                'A', new UnificationEntry(circuit, MarkerMaterials.Tier.HV),
                'F', gregtech.common.metatileentities.MetaTileEntities.HULL[3].getStackForm(),
                'S', Items.DIAMOND_SWORD
        );

        ModHandler.addShapedRecipe("trans_tower", DrTechMetaTileEntities.TRANS_TOWER.getStackForm(),
                "WAW", "DSD", "WAW",
                'W', new UnificationEntry(OrePrefix.plateDouble, Materials.Copper),
                'D', new UnificationEntry(OrePrefix.plateDouble, Materials.Silver),
                'S', gregtech.common.metatileentities.MetaTileEntities.HULL[1].getStackForm(),
                'A', new UnificationEntry(circuit, MarkerMaterials.Tier.LV)
        );

        ModHandler.addShapedRecipe("connector_1", new ItemStack(BlocksInit.BLOCK_CONNECTOR1),
                "WAW", "WSW", "WAW",
                'W', new UnificationEntry(OrePrefix.plate, Materials.Lead),
                'S', gregtech.common.metatileentities.MetaTileEntities.HULL[1].getStackForm(),
                'A', new UnificationEntry(OrePrefix.plate, Materials.Polyethylene)
        );

        ModHandler.addShapedRecipe("connector_2", new ItemStack(BlocksInit.BLOCK_CONNECTOR2),
                "WAW", "WSW", "WAW",
                'W', new UnificationEntry(OrePrefix.plate, Materials.Aluminium),
                'S', gregtech.common.metatileentities.MetaTileEntities.HULL[2].getStackForm(),
                'A', new UnificationEntry(OrePrefix.plate, Materials.Polyethylene)
        );

        ModHandler.addShapedRecipe("connector_3", new ItemStack(BlocksInit.BLOCK_CONNECTOR3),
                "WAW", "WSW", "WAW",
                'W', new UnificationEntry(OrePrefix.plate, Materials.Electrum),
                'S', gregtech.common.metatileentities.MetaTileEntities.HULL[3].getStackForm(),
                'A', new UnificationEntry(OrePrefix.plate, Materials.Polyethylene)
        );

        ModHandler.addShapedRecipe("golden_sea", new ItemStack(BlocksInit.BLOCK_GOLDEN_SEA),
                "WWW", "WSW", "WWW",
                'W', Blocks.GOLD_BLOCK,
                'S', new UnificationEntry(OrePrefix.plate, Materials.Gold)
        );

        if (Loader.isModLoaded("baubles")) {
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

        ModHandler.addShapedRecipe(true, "electric_plasma_gun", DrMetaItems.ELECTRIC_PLASMA_GUN.getStackForm(),
                "JTG", "LSB", "DSB",
                'D', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                'S', MetaItems.ENERGIUM_CRYSTAL,
                'B', new UnificationEntry(OrePrefix.plate, Materials.Titanium),
                'L', MetaItems.FIELD_GENERATOR_HV,
                'J', new UnificationEntry(OrePrefix.plate, Materials.Titanium),
                'T', MetaItems.POWER_THRUSTER_ADVANCED,
                'G', MetaItems.EMITTER_EV
        );
        ModHandler.addShapedRecipe(true, "tactical_laser_submachine_gun", DrMetaItems.TACTICAL_LASER_SUBMACHINE_GUN.getStackForm(),
                "JTG", "LSB", "DSB",
                'D', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                'S', MetaItems.ENERGIUM_CRYSTAL,
                'B', new UnificationEntry(OrePrefix.plate, Materials.Iridium),
                'L', MetaItems.FIELD_GENERATOR_HV,
                'J', new UnificationEntry(OrePrefix.toolHeadDrill, Materials.Titanium),
                'T', MetaItems.POWER_THRUSTER_ADVANCED,
                'G', MetaItems.EMITTER_EV
        );
        ModHandler.addShapedRecipe(true, "advanced_tachino_disruptor", DrMetaItems.ADVANCED_TACHINO_DISRUPTOR.getStackForm(),
                "JBG", "DSB", "DSB",
                'D', new UnificationEntry(circuit, MarkerMaterials.Tier.LuV),
                'S', MetaItems.LAPOTRON_CRYSTAL,
                'B', new UnificationEntry(OrePrefix.plate, Materials.Iridium),
                'J', MetaItems.NANO_SABER,
                'G', MetaItems.EMITTER_IV
        );
        ModHandler.addShapelessRecipe("peaceful_table", new ItemStack(ItemsInit.ITEM_BLOCK_PEACEFUL_TABLE), Blocks.CRAFTING_TABLE, Items.IRON_SWORD);

        gregtech.loaders.recipe.MetaTileEntityLoader.registerMachineRecipe(DrTechMetaTileEntities.DISASSEMBLY, "AVA", "VMV", "WCW", 'M', HULL, 'V', CONVEYOR, 'A', ROBOT_ARM,
                'C', CIRCUIT, 'W', CABLE);

        gregtech.loaders.recipe.MetaTileEntityLoader.registerMachineRecipe(DrTechMetaTileEntities.UNIVERSAL_COLLECTORS, "WFW", "VHP", "WCW", 'W', Blocks.IRON_BARS, 'F',
                MetaItems.FLUID_FILTER, 'P', PUMP, 'H', HULL, 'C', CIRCUIT, 'V', CONVEYOR);

        ModHandler.addShapedRecipe("grass_killer", DrMetaItems.GRASS_KILLER.getStackForm(),
                "WSW", "SSS", "WSW",
                'W', Blocks.GRASS,
                'S', Items.IRON_INGOT
        );

        ModHandler.addShapedRecipe(true, "storage_pail", new ItemStack(ItemsInit.ITEM_BLOCK_STORAGE_PAIL),
                "XXX", "XCX", "XXX",
                'X', Blocks.CHEST, 'C', MetaItems.ELECTRIC_PISTON_LV);
        ModHandler.addShapelessRecipe("advanced_cauldron", new ItemStack(ItemsInit.ITEM_BLOCK_ADVANCED_CAULDRON), Items.CAULDRON);
        ModHandler.addShapedRecipe("happy_ghast_harness", new ItemStack(ItemsInit.HAPPY_GHAST_HARNESS),
                "LGL", "LWL", " L ",
                'L', Items.LEATHER,
                'G', Blocks.GLASS,
                'W', new ItemStack(Blocks.WOOL, 1, 0));

        ModHandler.addShapedRecipe("composter", new ItemStack(BlocksInit.BLOCK_COMPOSTER),
                "S S", "S S", "SSS",
                'S', new ItemStack(Blocks.WOODEN_SLAB, 1, 0));

        fusion();

    }

    private static ItemStack droneUpgrade(DroneUpgradeType type) {
        return new ItemStack(ItemsInit.DRONE_UPGRADE_MODULE, 1, type.getMetadata());
    }

    private static void multiblock() {


        ModHandler.addShapedRecipe(true, "large_alloy_smelter", DrTechMetaTileEntities.LARGE_ALLOY_SMELTER.getStackForm(),
                "ADA", "WSW", "WWW",
                'W', new ItemStack(MetaBlocks.METAL_CASING, 1, 2),
                'A', new UnificationEntry(circuit, MarkerMaterials.Tier.MV),
                'S', ELECTRIC_BLAST_FURNACE.getStackForm(),
                'D', new UnificationEntry(OrePrefix.plate, Materials.Aluminium)
        );

        if (DrtConfig.MachineSwitch.EnableIndustrialMachines) {
            ModHandler.addShapedRecipe(true, "large_extruder", DrTechMetaTileEntities.LARGE_EXTRUDER.getStackForm(),
                    "LCL", "PSP", "OWO",
                    'L', new UnificationEntry(pipeLargeItem, Ultimet),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                    'S', gregtech.common.metatileentities.MetaTileEntities.EXTRUDER[EV].getStackForm(),
                    'P', MetaItems.ELECTRIC_PISTON_EV.getStackForm(),
                    'O', new UnificationEntry(spring, MolybdenumDisilicide),
                    'W', new UnificationEntry(cableGtSingle, Platinum));



            ModHandler.addShapedRecipe("larger_roller_press", DrTechMetaTileEntities.INDUSTRIAL_ROLLER_PRESS.getStackForm(),
                    "ACA",
                    "BSB",
                    "ACA",
                    'S', FORMING_PRESS[EV].getStackForm(),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.EV),
                    'A', new UnificationEntry(plate, Titanium),
                    'B', BENDER[EV].getStackForm()

            );
            ModHandler.addShapedRecipe("larger_cable_press", DrTechMetaTileEntities.INDUSTRIAL_CABLE_PRESS.getStackForm(),
                    "ACA",
                    "BSB",
                    "ACA",
                    'S', WIREMILL[IV].getStackForm(),
                    'C', new UnificationEntry(circuit, MarkerMaterials.Tier.IV),
                    'A', new UnificationEntry(plate, BlueSteel),
                    'B', gregtech.common.metatileentities.MetaTileEntities.HULL[IV].getStackForm()

            );

        }
    }

    private static void updateChip() {
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
            ModHandler.addShapedRecipe("upgrade_speed" + i, upgrade[i].copy(),
                    "ACA",
                    "CSC",
                    "BCB",
                    'S', WORLD_ACCELERATOR[i].getStackForm(),
                    'C', DrMetaItems.UPGRADE_NULL,
                    'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                    'B', new UnificationEntry(gearSmall, Materials.Steel)
            );
        }
        ModHandler.addShapedRecipe("upgrade_speed_ppp", upgrade[8].copy(),
                "AAA",
                "ASA",
                "AAA",
                'S', upgrade[7],
                'A', DrMetaItems.UPGRADE_PRODUCTION
        );
        ModHandler.addShapedRecipe("upgrade_speed_production", DrMetaItems.UPGRADE_PRODUCTION.getStackForm(),
                "ACA",
                "DSD",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', Items.SUGAR,
                'D', getItemStack("forestry:royal_jelly", 0),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_pingyuan", DrMetaItems.UPGRADE_PLAIN.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', getItemStack("gendustry:env_processor", 0),
                'D', Blocks.GRASS,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_shamo", DrMetaItems.UPGRADE_DESERT_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', getItemStack("gendustry:env_processor", 0),
                'D', Blocks.SAND,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_yanhan", DrMetaItems.UPGRADE_WINTER_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', getItemStack("gendustry:env_processor", 0),
                'D', Blocks.SNOW,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_haiyang", DrMetaItems.UPGRADE_OCEAN_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', getItemStack("gendustry:env_processor", 0),
                'D', Items.WATER_BUCKET,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_diyu", DrMetaItems.UPGRADE_HELL_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', getItemStack("gendustry:env_processor", 0),
                'D', Blocks.NETHER_BRICK,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_conglin", DrMetaItems.UPGRADE_JUNGLE_EMULATION.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', getItemStack("gendustry:env_processor", 0),
                'D', new ItemStack(Items.DYE, 1, 3),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_light", DrMetaItems.UPGRADE_LIGHT.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', Blocks.GLASS,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_flowering", DrMetaItems.UPGRADE_FLOWERING.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', Blocks.RED_FLOWER,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_dry", DrMetaItems.UPGRADE_DRYER.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', Items.LAVA_BUCKET,
                'D', Blocks.SAND,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_humidifier", DrMetaItems.UPGRADE_HUMIDIFIER.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', Items.WATER_BUCKET,
                'D', Blocks.CACTUS,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_auto", DrMetaItems.UPGRADE_AUTOMATION.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', MetaItems.ROBOT_ARM_LV,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_scrubber", DrMetaItems.UPGRADE_POLLEN_SCRUBBER.getStackForm(),
                "ADA",
                "CSC",
                "BDB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', new UnificationEntry(rotor, Materials.StainlessSteel),
                'D', MetaItems.ELECTRIC_MOTOR_MV,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_cooler", DrMetaItems.UPGRADE_COOLER.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', Blocks.ICE,
                'D', Blocks.SNOW,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_heater", DrMetaItems.UPGRADE_HEATER.getStackForm(),
                "ACA",
                "DSD",
                "BEB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("gendustry:climate_module", 0),
                'E', Blocks.NETHERRACK,
                'D', Items.LAVA_BUCKET,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_lifespan", DrMetaItems.UPGRADE_LIFESPAN.getStackForm(),
                "ADA",
                "CSC",
                "BDB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', Blocks.CACTUS,
                'D', Items.FERMENTED_SPIDER_EYE,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_stabilized", DrMetaItems.UPGRADE_GENETIC_STABILIZER.getStackForm(),
                "ADA",
                "CSC",
                "BDB",
                'S', DrMetaItems.UPGRADE_NULL,
                'D', getItemStack("gendustry:genetics_processor", 0),
                'C', new UnificationEntry(plate, RedAlloy),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_territory", DrMetaItems.UPGRADE_TERRITORY.getStackForm(),
                "ADA",
                "CSC",
                "BDB",
                'S', DrMetaItems.UPGRADE_NULL,
                'D', new UnificationEntry(plate, Iron),
                'C', new UnificationEntry(plate, EnderPearl),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_sky", DrMetaItems.UPGRADE_OPEN_SKY.getStackForm(),
                "ADA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'D', Blocks.REDSTONE_LAMP,
                'C', Blocks.GLASS_PANE,
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_sieve", DrMetaItems.UPGRADE_SIEVE.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', getItemStack("forestry:crafting_material", 3),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_dark", DrMetaItems.UPGRADE_T.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', new UnificationEntry(plate, Steel),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
        ModHandler.addShapedRecipe("upgrade_speed_seal", DrMetaItems.UPGRADE_SEAL.getStackForm(),
                "ACA",
                "CSC",
                "BCB",
                'S', DrMetaItems.UPGRADE_NULL,
                'C', new UnificationEntry(plate, Rubber),
                'A', new UnificationEntry(gearSmall, Materials.StainlessSteel),
                'B', new UnificationEntry(gearSmall, Materials.Steel)
        );
    }



    public static ItemStack getItemStack(String itemstr) {
        return getItemStack(itemstr, 0);
    }

    public static ItemStack getItemStack(String itemstr, long num) {
        ItemStack item = getItemStack(itemstr, 0);
        item.setCount((int) num);
        return item;
    }
    public static ItemStack getItemStack(String itemstr, int meta) {
        if (itemstr.startsWith("<") && itemstr.endsWith(">"))
            itemstr = itemstr.substring(1, itemstr.length() - 1);
        if(itemstr.startsWith("item"))
        {
            String content = itemstr.substring(itemstr.indexOf('(') + 1, itemstr.lastIndexOf(')'));
            String[] parts = content.split("\\s*,\\s*");
            itemstr=parts[0].replace('\'',' ').trim();
            meta=Integer.parseInt(parts[1]);
        }
        String[] str = itemstr.split(":");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(str[0], str[1]));
        if (item != null) {
            if (str.length == 3)
                return new ItemStack(item, 1, Integer.parseInt(str[2]));
            return new ItemStack(item, 1, meta);
        } else {
            return ItemStack.EMPTY;
        }
    }

    // ============ 鍙戠數鑱氬彉鍫嗛厤鏂?============

    private static ItemStack fusionCasing(BlockFusionReactorCasing.CasingType type) {
        return new ItemStack(BlocksInit.FUSION_REACTOR_CASING, 1, type.ordinal());
    }

    private static ItemStack fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType type) {
        return new ItemStack(BlocksInit.FUSION_REACTOR_TIERED_CASING, 1, type.ordinal());
    }

    private static ItemStack fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType type) {
        return new ItemStack(BlocksInit.FUSION_REACTOR_TIERED_CASING2, 1, type.ordinal());
    }

    private static void fusion() {
        ItemStack firstWall1 = fusionCasing(BlockFusionReactorCasing.CasingType.FIRST_WALL_CASING);
        ItemStack magnet1 = fusionCasing(BlockFusionReactorCasing.CasingType.SUPERCONDUCTING_MAGNET_CASING);
        ItemStack coolant1 = fusionCasing(BlockFusionReactorCasing.CasingType.COOLING_CHANNEL_CASING);
        ItemStack breeding1 = fusionCasing(BlockFusionReactorCasing.CasingType.TRITIUM_BREEDING_BLANKET_CASING);
        ItemStack neutron1 = fusionCasing(BlockFusionReactorCasing.CasingType.NEUTRON_CAPTURE_CASING);
        ItemStack radiation = fusionCasing(BlockFusionReactorCasing.CasingType.RADIATION_SHIELDING_CASING);
        ItemStack plasma = fusionCasing(BlockFusionReactorCasing.CasingType.PLASMA_CONTAINMENT_CASING);
        ItemStack rfDevice = fusionCasing(BlockFusionReactorCasing.CasingType.RF_DEVICE_CASING);
        ItemStack rfWaveguide = fusionCasing(BlockFusionReactorCasing.CasingType.RF_WAVEGUIDE_CASING);
        ItemStack rfCapacitor = fusionCasing(BlockFusionReactorCasing.CasingType.RF_CAPACITOR_CASING);
        ItemStack rfPhase = fusionCasing(BlockFusionReactorCasing.CasingType.RF_PHASE_SYNCHRONIZER_CASING);
        ItemStack rfWindow = fusionCasing(BlockFusionReactorCasing.CasingType.RF_CERAMIC_WINDOW_CASING);

        // ---- 鍩虹澶栧３锛? 妗ｏ級 ----
        ModHandler.addShapedRecipe("fusion_casing_first_wall_1", firstWall1,
                "PPP", "TWT", "PPP",
                'P', DrMetaItems.FUSION_FIRST_WALL_PLATE,
                'T', new UnificationEntry(plate, Tungsten),
                'W', new UnificationEntry(plate, TungstenSteel));

        ModHandler.addShapedRecipe("fusion_casing_magnet_1", magnet1,
                "CCC", "NMN", "CCC",
                'C', DrMetaItems.FUSION_MAGNET_COIL,
                'N', new UnificationEntry(plate, NiobiumTitanium),
                'M', DrMetaItems.MAGNETIC_FIELD_STORAGE_CELL);

        ModHandler.addShapedRecipe("fusion_casing_coolant_1", coolant1,
                "CCC", "PFP", "CCC",
                'C', DrMetaItems.FUSION_COOLING_CHANNEL,
                'P', new UnificationEntry(pipeNormalFluid, Copper),
                'F', new UnificationEntry(plate, StainlessSteel));

        ModHandler.addShapedRecipe("fusion_casing_breeding_1", breeding1,
                "LLL", "TBT", "LLL",
                'L', new UnificationEntry(plate, Lithium),
                'T', DrMetaItems.TRITIUM_BREEDING_CELL,
                'B', new UnificationEntry(plate, Steel));

        ModHandler.addShapedRecipe("fusion_casing_neutron_1", neutron1,
                "BBB", "NCN", "BBB",
                'B', new UnificationEntry(plate, Boron),
                'N', DrMetaItems.NEUTRON_CAPTURE_CORE,
                'C', new UnificationEntry(plate, Cadmium));

        ModHandler.addShapedRecipe("fusion_casing_radiation", radiation,
                "LTL", "TST", "LTL",
                'L', new UnificationEntry(plate, Lead),
                'T', new UnificationEntry(plate, Tungsten),
                'S', new UnificationEntry(plate, Steel));

        ModHandler.addShapedRecipe("fusion_casing_plasma", plasma,
                "WWW", "WGW", "WWW",
                'W', new UnificationEntry(plate, TungstenSteel),
                'G', DrMetaItems.FUSION_FIRST_WALL_PLATE);

        ModHandler.addShapedRecipe("fusion_casing_rf_device", rfDevice,
                "SRS", "RGR", "SRS",
                'R', DrMetaItems.RF_GENERATOR_CORE,
                'G', DrMetaItems.FUSION_MAGNET_COIL,
                'S', new UnificationEntry(plate, Steel));

        ModHandler.addShapedRecipe("fusion_casing_rf_waveguide", rfWaveguide,
                "CWC", "CWC", "CWC",
                'C', new UnificationEntry(plate, Copper),
                'W', DrMetaItems.RF_WAVEGUIDE);

        ModHandler.addShapedRecipe("fusion_casing_rf_capacitor", rfCapacitor,
                "WNW", "NRN", "WNW",
                'N', new UnificationEntry(plate, NaquadahAlloy),
                'R', DrMetaItems.RF_GENERATOR_CORE,
                'W', DrMetaItems.LOW_VOLTAGE_WIRE);

        ModHandler.addShapedRecipe("fusion_casing_rf_phase", rfPhase,
                "PPP", "RSR", "PPP",
                'P', DrMetaItems.RF_PHASE_SYNCHRONIZER,
                'R', new UnificationEntry(plate, RhodiumPlatedPalladium),
                'S', DrMetaItems.HIGH_VOLTAGE_WIRE);

        ModHandler.addShapedRecipe("fusion_casing_rf_window", rfWindow,
                "AAA", "CWC", "AAA",
                'A', new UnificationEntry(plate, Aluminium),
                'C', DrMetaItems.CERAMIC_RF_WINDOW,
                'W', DrMetaItems.FUSION_COOLING_CHANNEL);

        // ---- 鑱氬彉鍫嗘帶鍒跺櫒 ----
        ModHandler.addShapedRecipe("fusion_reactor", DrTechMetaTileEntities.FUSION_REACTOR.getStackForm(),
                "RRR", "RIR", "RRR",
                'R', radiation,
                'I', DrMetaItems.DT_FUEL_INJECTOR);

        // ---- 绗竴澹佸崌绾э紙2~5 妗ｏ級 ----
        ModHandler.addShapedRecipe("fusion_casing_first_wall_2",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.FIRST_WALL_2),
                "CCC", "CFC", "CCC",
                'C', new UnificationEntry(plate, TungstenCarbide),
                'F', firstWall1);

        ModHandler.addShapedRecipe("fusion_casing_first_wall_3",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.FIRST_WALL_3),
                "VVV", "TFT", "VVV",
                'V', new UnificationEntry(plate, VanadiumSteel),
                'T', new UnificationEntry(plate, Titanium),
                'F', fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.FIRST_WALL_2));

        ModHandler.addShapedRecipe("fusion_casing_first_wall_4",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.FIRST_WALL_4),
                "NON", "OFO", "NON",
                'N', new UnificationEntry(plate, NaquadahAlloy),
                'O', new UnificationEntry(plate, Osmium),
                'F', fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.FIRST_WALL_3));

        ModHandler.addShapedRecipe("fusion_casing_first_wall_5",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.FIRST_WALL_5),
                "NQN", "QFQ", "NQN",
                'N', new UnificationEntry(plate, Naquadria),
                'Q', new UnificationEntry(plate, Neutronium),
                'F', fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.FIRST_WALL_4));

        // ---- 鍐峰嵈鍓傚洖璺崌绾э紙2~5 妗ｏ級 ----
        ModHandler.addShapedRecipe("fusion_casing_coolant_2",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.COOLING_CHANNEL_2),
                "SPS", "PCP", "SPS",
                'S', new UnificationEntry(plate, Sodium),
                'P', new UnificationEntry(plate, Potassium),
                'C', coolant1);

        ModHandler.addShapedRecipe("fusion_casing_coolant_3",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.COOLING_CHANNEL_3),
                "SSS", "SAS", "SSS",
                'S', new UnificationEntry(plate, Salt),
                'A', fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.COOLING_CHANNEL_2));

        ModHandler.addShapedRecipe("fusion_casing_coolant_4",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.COOLING_CHANNEL_4),
                "LPL", "PCP", "LPL",
                'L', new UnificationEntry(plate, Lithium),
                'P', new UnificationEntry(plate, Lead),
                'C', fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.COOLING_CHANNEL_3));

        ModHandler.addShapedRecipe("fusion_casing_coolant_5",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.COOLING_CHANNEL_5),
                "RRR", "RCR", "RRR",
                'R', new UnificationEntry(plate, Ruridit),
                'C', fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.COOLING_CHANNEL_4));

        // ---- 涓瓙鎹曡幏鍗囩骇锛?~5 妗ｏ級 ----
        ModHandler.addShapedRecipe("fusion_casing_neutron_2",
                fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.NEUTRON_CAPTURE_2),
                "CCC", "CAC", "CCC",
                'C', new UnificationEntry(plate, Cadmium),
                'A', neutron1);

        ModHandler.addShapedRecipe("fusion_casing_neutron_3",
                fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.NEUTRON_CAPTURE_3),
                "BBB", "BAB", "BBB",
                'B', new UnificationEntry(plate, Boron),
                'A', fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.NEUTRON_CAPTURE_2));

        ModHandler.addShapedRecipe("fusion_casing_neutron_4",
                fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.NEUTRON_CAPTURE_4),
                "LPL", "PAP", "LPL",
                'L', new UnificationEntry(plate, Lithium),
                'P', new UnificationEntry(plate, Lead),
                'A', fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.NEUTRON_CAPTURE_3));

        ModHandler.addShapedRecipe("fusion_casing_neutron_5",
                fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.NEUTRON_CAPTURE_5),
                "III", "IAI", "III",
                'I', new UnificationEntry(plate, Indium),
                'A', fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.NEUTRON_CAPTURE_4));

        // ---- 姘氬娈栧寘灞傚崌绾э紙2~5 妗ｏ級 ----
        ModHandler.addShapedRecipe("fusion_casing_breeding_2",
                fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.TRITIUM_BREEDING_BLANKET_2),
                "LLL", "LBL", "LLL",
                'L', new UnificationEntry(plate, Lithium),
                'B', breeding1);

        ModHandler.addShapedRecipe("fusion_casing_breeding_3",
                fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.TRITIUM_BREEDING_BLANKET_3),
                "AAA", "ABA", "AAA",
                'A', new UnificationEntry(plate, Aluminium),
                'B', fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.TRITIUM_BREEDING_BLANKET_2));

        ModHandler.addShapedRecipe("fusion_casing_breeding_4",
                fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.TRITIUM_BREEDING_BLANKET_4),
                "LPL", "PBP", "LPL",
                'L', new UnificationEntry(plate, Lithium),
                'P', new UnificationEntry(plate, Lead),
                'B', fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.TRITIUM_BREEDING_BLANKET_3));

        ModHandler.addShapedRecipe("fusion_casing_breeding_5",
                fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.TRITIUM_BREEDING_BLANKET_5),
                "MMM", "MBM", "MMM",
                'M', new UnificationEntry(plate, ManganesePhosphide),
                'B', fusionTieredCasing2(BlockFusionReactorTieredCasing2.CasingType.TRITIUM_BREEDING_BLANKET_4));

        // ---- 瓒呭纾佷綋鍗囩骇锛?~4 妗ｏ級 ----
        ModHandler.addShapedRecipe("fusion_casing_magnet_2",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.SUPERCONDUCTING_MAGNET_2),
                "NNN", "NMN", "NNN",
                'N', new UnificationEntry(plate, NiobiumTitanium),
                'M', magnet1);

        ModHandler.addShapedRecipe("fusion_casing_magnet_3",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.SUPERCONDUCTING_MAGNET_3),
                "VVV", "VAV", "VVV",
                'V', new UnificationEntry(plate, VanadiumSteel),
                'A', fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.SUPERCONDUCTING_MAGNET_2));

        ModHandler.addShapedRecipe("fusion_casing_magnet_4",
                fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.SUPERCONDUCTING_MAGNET_4),
                "OOO", "OAO", "OOO",
                'O', new UnificationEntry(plate, Osmium),
                'A', fusionTieredCasing(BlockFusionReactorTieredCasing.CasingType.SUPERCONDUCTING_MAGNET_3));
    }
}
