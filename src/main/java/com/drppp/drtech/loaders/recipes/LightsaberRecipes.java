package com.drppp.drtech.loaders.recipes;

import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.lightsaber.FocusingCrystal;
import com.drppp.drtech.common.Items.lightsaber.ItemDoubleLightsaber;
import com.drppp.drtech.common.Items.lightsaber.LightsaberColor;
import com.drppp.drtech.common.Items.lightsaber.LightsaberHilt;
import com.drppp.drtech.common.MetaTileEntities.DrTechMetaTileEntities;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.item.ItemStack;

public final class LightsaberRecipes {
    private LightsaberRecipes() {
    }

    public static void init() {
        registerComponents();
        registerAssemblyExamples();
        registerMachineRecipe();
    }

    private static void registerComponents() {
        DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.LV)
                .input(OrePrefix.wireFine, Materials.Electrum, 4)
                .input(OrePrefix.plate, Materials.Gold)
                .output(ItemsInit.LIGHTSABER_CIRCUITRY)
                .duration(100)
                .EUt(GTValues.VA[GTValues.LV])
                .buildAndRegister();

        for (LightsaberHilt hilt : LightsaberHilt.values()) {
            int circuit = hilt.getMetadata() + 1;
            DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, Materials.StainlessSteel, 2)
                    .input(OrePrefix.ring, Materials.Titanium)
                    .circuitMeta(circuit)
                    .outputs(new ItemStack(ItemsInit.LIGHTSABER_EMITTER, 1, hilt.getMetadata()))
                    .duration(80)
                    .EUt(GTValues.VA[GTValues.LV])
                    .buildAndRegister();
            DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, Materials.StainlessSteel)
                    .input(OrePrefix.wireFine, Materials.Copper, 2)
                    .circuitMeta(circuit)
                    .outputs(new ItemStack(ItemsInit.LIGHTSABER_SWITCH, 1, hilt.getMetadata()))
                    .duration(80)
                    .EUt(GTValues.VA[GTValues.LV])
                    .buildAndRegister();
            DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, Materials.Steel, 2)
                    .input(OrePrefix.ring, Materials.Rubber, 2)
                    .circuitMeta(circuit)
                    .outputs(new ItemStack(ItemsInit.LIGHTSABER_GRIP, 1, hilt.getMetadata()))
                    .duration(80)
                    .EUt(GTValues.VA[GTValues.LV])
                    .buildAndRegister();
            DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, Materials.Steel)
                    .input(OrePrefix.screw, Materials.StainlessSteel, 2)
                    .circuitMeta(circuit)
                    .outputs(new ItemStack(ItemsInit.LIGHTSABER_POMMEL, 1, hilt.getMetadata()))
                    .duration(80)
                    .EUt(GTValues.VA[GTValues.LV])
                    .buildAndRegister();
        }

        for (LightsaberColor color : LightsaberColor.values()) {
            DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.gem, Materials.NetherQuartz)
                    .input(OrePrefix.dust, Materials.Redstone)
                    .circuitMeta(color.getMetadata() + 1)
                    .outputs(new ItemStack(ItemsInit.LIGHTSABER_CRYSTAL, 1, color.getMetadata()))
                    .duration(120)
                    .EUt(GTValues.VA[GTValues.LV])
                    .buildAndRegister();
        }

        for (FocusingCrystal crystal : FocusingCrystal.values()) {
            DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.gem, Materials.Diamond)
                    .input(OrePrefix.dust, Materials.CertusQuartz)
                    .circuitMeta(crystal.getMetadata() + 1)
                    .outputs(new ItemStack(ItemsInit.FOCUSING_CRYSTAL, 1, crystal.getMetadata()))
                    .duration(160)
                    .EUt(GTValues.VA[GTValues.LV])
                    .buildAndRegister();
        }

        ItemStack mauler = new ItemStack(ItemsInit.getLightsaber(LightsaberHilt.MAULER));
        DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(mauler.copy())
                .inputs(mauler.copy())
                .outputs(ItemDoubleLightsaber.create(mauler, mauler))
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV])
                .buildAndRegister();
    }

    private static void registerAssemblyExamples() {
        for (LightsaberHilt hilt : LightsaberHilt.values()) {
            LightsaberColor color = hilt.getDefaultColor();
            DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(ItemsInit.LIGHTSABER_EMITTER, 1, hilt.getMetadata()))
                    .inputs(new ItemStack(ItemsInit.LIGHTSABER_SWITCH, 1, hilt.getMetadata()))
                    .inputs(new ItemStack(ItemsInit.LIGHTSABER_GRIP, 1, hilt.getMetadata()))
                    .inputs(new ItemStack(ItemsInit.LIGHTSABER_POMMEL, 1, hilt.getMetadata()))
                    .input(ItemsInit.LIGHTSABER_CIRCUITRY)
                    .inputs(new ItemStack(ItemsInit.LIGHTSABER_CRYSTAL, 1, color.getMetadata()))
                    .outputs(new ItemStack(ItemsInit.getLightsaber(hilt)))
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.LV])
                    .buildAndRegister();
        }
    }

    private static void registerMachineRecipe() {
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaTileEntities.HULL[GTValues.LV])
                .input(OrePrefix.circuit, MarkerMaterials.Tier.LV, 2)
                .input(MetaItems.ROBOT_ARM_LV)
                .input(MetaItems.CONVEYOR_MODULE_LV)
                .outputs(DrTechMetaTileEntities.LIGHTSABER_ASSEMBLER[GTValues.LV].getStackForm())
                .circuitMeta(8)
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV])
                .buildAndRegister();
    }
}
