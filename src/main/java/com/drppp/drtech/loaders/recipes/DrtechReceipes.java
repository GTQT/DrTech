package com.drppp.drtech.loaders.recipes;


import com.drppp.drtech.api.recipes.builder.DronePadRecipeBuilder;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMapBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;

public final class DrtechReceipes {

    public static final RecipeMap<SimpleRecipeBuilder> MOB_KILLER = new RecipeMapBuilder<>("mob_killer",
            new SimpleRecipeBuilder())
            .itemInputs(2)
            .itemOutputs(6)
            .fluidInputs(0)
            .fluidOutputs(1)
            .sound(GTSoundEvents.CHEMICAL_REACTOR)
            .build();

    public static final RecipeMap<SimpleRecipeBuilder> SOLAR_TOWER = new RecipeMapBuilder<>("solar_tower",
            new SimpleRecipeBuilder())
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

    public static final RecipeMap<SimpleRecipeBuilder> LOG_CREATE = new RecipeMapBuilder<>("log_create",
            new SimpleRecipeBuilder())
            .itemInputs(2)
            .itemOutputs(2)
            .fluidInputs(1)
            .fluidOutputs(1)
            .sound(GTSoundEvents.CHEMICAL_REACTOR)
            .build();

    public static final RecipeMap<SimpleRecipeBuilder> DISASSEMBLER_RECIPES = new RecipeMapBuilder<>("disassembler",
            new SimpleRecipeBuilder())
            .itemInputs(1)
            .itemOutputs(9)
            .uiBuilder(builder -> builder
                    .itemSlotOverlay(GTGuiTextures.CIRCUIT_OVERLAY, false)
                    .progressBar(GTGuiTextures.PROGRESS_BAR_CIRCUIT))
            .sound(GTSoundEvents.ASSEMBLER)
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

    public static final RecipeMap<SimpleRecipeBuilder> COMBS_PRODUCT = new RecipeMapBuilder<>("combs_product",
            new SimpleRecipeBuilder())
            .itemInputs(3)
            .itemOutputs(9)
            .fluidInputs(2)
            .fluidOutputs(3)
            .sound(GTSoundEvents.MOTOR)
            .build();

    private DrtechReceipes() {
    }
}
