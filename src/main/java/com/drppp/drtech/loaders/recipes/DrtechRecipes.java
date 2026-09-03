package com.drppp.drtech.loaders.recipes;


import com.drppp.drtech.api.recipes.builder.DronePadRecipeBuilder;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMapBuilder;
import gregtech.api.recipes.builders.PrimitiveRecipeBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;

public final class DrtechRecipes {

    public static final RecipeMap<PrimitiveRecipeBuilder> SOLAR_TOWER = new RecipeMapBuilder<>("solar_tower",
            new PrimitiveRecipeBuilder())
            .itemInputs(0)
            .itemOutputs(0)
            .fluidInputs(1)
            .fluidOutputs(1)
            .sound(GTSoundEvents.CHEMICAL_REACTOR)
            .build();

    public static final RecipeMap<SimpleRecipeBuilder> MOLECULAR_RECOMBINATION = new RecipeMapBuilder<>("molecular_recombination",
            new SimpleRecipeBuilder())
            .itemInputs(1)
            .itemOutputs(1)
            .fluidInputs(1)
            .fluidOutputs(1)
            .sound(GTSoundEvents.CHEMICAL_REACTOR)
            .build();

    public static final RecipeMap<DronePadRecipeBuilder> DRONE_PAD = new RecipeMapBuilder<>("drone_pad",
            new DronePadRecipeBuilder())
            .itemInputs(3)
            .itemOutputs(9)
            .fluidInputs(3)
            .fluidOutputs(3)
            .uiBuilder(b -> b
                    .itemSlotOverlay(GTGuiTextures.BENDER_OVERLAY, false, false)
                    .itemSlotOverlay(GTGuiTextures.INT_CIRCUIT_OVERLAY, false, true)
                    .progressBar(GTGuiTextures.PROGRESS_BAR_BENDING))
            .sound(GTSoundEvents.MOTOR)
            .build();

    public static final RecipeMap<SimpleRecipeBuilder> DISASSEMBLER_RECIPES =
            new LightsaberDisassemblerRecipeMap();

    public static final RecipeMap<SimpleRecipeBuilder> LIGHTSABER_ASSEMBLER_RECIPES =
            new LightsaberAssemblerRecipeMap();


    private DrtechRecipes() {
    }
}
