package com.brachy84.mechtech.common.recipes;

import com.brachy84.mechtech.api.armor.MaterialArmorModuleBuilder;
import com.brachy84.mechtech.api.armor.Modules;
import com.brachy84.mechtech.common.MTConfig;
import com.brachy84.mechtech.common.items.MTMetaItems;
import com.brachy84.mechtech.common.machines.MTTileEntities;
import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.item.ItemStack;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class Recipes {

    public static void init() {
        modularArmor();
        modules();
        metaTileEntities();
        for (MaterialArmorModuleBuilder builder : Modules.getArmorModules().values()) {
            if (builder.isRegistered()) {
                if (builder.doGenerateRecipe && builder.material != null && builder.material.hasFlag(MaterialFlags.GENERATE_PLATE)) {
                    generateArmorPlatingRecipe(builder);
                }
            }
        }
    }

    private static void modularArmor() {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.StainlessSteel, 5)
                .input(MetaItems.CARBON_MESH, 3)
                .input(OrePrefix.plate, Materials.BlackBronze)
                .input(circuit, MarkerMaterials.Tier.MV)
                .output(MTMetaItems.MODULAR_HELMET)
                .EUt(400)
                .duration(300)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.StainlessSteel, 8)
                .input(MetaItems.CARBON_MESH, 6)
                .input(OrePrefix.plate, Materials.Tungsten)
                .input(circuit, MarkerMaterials.Tier.HV)
                .output(MTMetaItems.MODULAR_CHESTPLATE)
                .EUt(400)
                .duration(400)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.StainlessSteel, 7)
                .input(MetaItems.CARBON_MESH, 5)
                .input(OrePrefix.plate, Materials.NiobiumNitride)
                .input(circuit, MarkerMaterials.Tier.MV)
                .output(MTMetaItems.MODULAR_LEGGINGS)
                .EUt(400)
                .duration(350)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.StainlessSteel, 4)
                .input(MetaItems.CARBON_MESH, 2)
                .input(OrePrefix.plate, Materials.Nichrome)
                .input(circuit, MarkerMaterials.Tier.MV)
                .output(MTMetaItems.MODULAR_BOOTS)
                .EUt(400)
                .duration(250)
                .buildAndRegister();
    }

    private static void generateArmorPlatingRecipe(MaterialArmorModuleBuilder builder) {
        ItemStack result = builder.getItemStack();
        if (result.isEmpty()) {
            throw new IllegalStateException("Result item can not be empty. Module was not properly registered");
        }
        Material material = builder.material;
        if (material.hasProperty(PropertyKey.INGOT)) {
            ModHandler.addShapedRecipe("armor_plating_" + material, result, "PPh", "PP ", "h  ", 'P', new UnificationEntry(OrePrefix.plate, material));
            RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                    .input(plate, material, 2)
                    .input(plate, material, 2)
                    .outputs(result)
                    .duration(120)
                    .EUt(44)
                    .buildAndRegister();
        } else if (material.hasProperty(PropertyKey.GEM)) {

            // TODO gem plating recipe
            ModHandler.addShapedRecipe("armor_plating_" + material, result, "PPh", "PP ", "h  ", 'P', new UnificationEntry(OrePrefix.plate, material));
            RecipeMaps.FORMING_PRESS_RECIPES.recipeBuilder()
                    .input(plate, material, 2)
                    .input(plate, material, 2)
                    .outputs(result)
                    .duration(120)
                    .EUt(44)
                    .buildAndRegister();
        }
    }

    private static void modules() {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItems.SENSOR_LuV)
                .input(plate, Osmium)
                .input(cableGtSingle, NiobiumNitride)
                .circuitMeta(3)
                .output(MTMetaItems.WIRELESS_RECEIVER)
                .duration(200)
                .EUt(2048)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItems.FLUID_CELL_LARGE_ALUMINIUM, 2)
                .input(plate, Tin, 3)
                .input(stick, Steel)
                .circuitMeta(3)
                .output(MTMetaItems.AUTO_FEEDER)
                .duration(140)
                .EUt(96)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItems.VOLTAGE_COIL_LuV, 8)
                .input(plate, IndiumTinBariumTitaniumCuprate, 16)
                .input(stick, PolyvinylChloride, 4)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.IV, 4)
                .circuitMeta(3)
                .output(MTMetaItems.TESLA_COIL)
                .duration(300)
                .EUt(2048)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItems.SENSOR_LV, 1)
                .input(lens, Glass, 2)
                .input(MetaItems.DUCT_TAPE, 2)
                .circuitMeta(3)
                .output(MTMetaItems.BINOCULARS)
                .duration(150)
                .EUt(32)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Steel, 2)
                .input(springSmall, Tungsten, 3)
                .circuitMeta(3)
                .output(MTMetaItems.SHOCK_ABSORBER)
                .duration(120)
                .EUt(480)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, Polycaprolactam, 64)
                .input(MetaItems.DUCT_TAPE, 8)
                .circuitMeta(3)
                .output(MTMetaItems.THICK_INSULATOR)
                .duration(100)
                .EUt(48)
                .buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(foil, PolyvinylChloride, 8)
                .input(MetaItems.DUCT_TAPE, 8)
                .input(craftingLens, Glass, 4)
                .circuitMeta(3)
                .output(MTMetaItems.OXYGEN_MASK)
                .duration(180)
                .EUt(480)
                .buildAndRegister();
    }

    private static void metaTileEntities() {
        ModHandler.addShapedRecipe("armor_workbench", MTTileEntities.ARMOR_WORKBENCH.getStackForm(), " S ", "SWS", "hSw", 'S', new UnificationEntry(OrePrefix.stick, Materials.StainlessSteel), 'W', MetaTileEntities.WORKBENCH.getStackForm());

        if (MTConfig.teslaTower.enable)
            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(plate, IndiumTinBariumTitaniumCuprate, 32)
                    .input(MetaItems.VOLTAGE_COIL_ZPM, 16)
                    .input(stick, PolyvinylChloride, 20)
                    .inputs(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE, 4))
                    .input(MTMetaItems.TESLA_COIL, 4)
                    .input("circuitZpm", 4)
                    .input("circuitUv", 1)
                    .input(MetaItems.EMITTER_ZPM, 8)
                    .input(MetaItems.FIELD_GENERATOR_ZPM, 2)
                    .input(MetaItems.WIRELESS, 6)
                    .input(MetaItems.ENERGY_LAPOTRONIC_ORB_CLUSTER)
                    .output(MTTileEntities.TESLA_TOWER)
                    .stationResearch(b -> b
                            .CWUt(32)
                            .EUt(GTValues.VA[GTValues.ZPM])
                            .researchStack(MTMetaItems.TESLA_COIL.getStackForm()))
                    .duration(2000)
                    .EUt(120000)
                    .buildAndRegister();
    }
}
