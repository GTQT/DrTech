package com.drppp.drtech.loaders;


import com.drppp.drtech.api.recipes.builder.DronePadRecipeBuilder;
import com.drppp.drtech.api.recipes.builder.NoEnergyRecipeBuilder;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;

public final class DrtechReceipes {
    public static final RecipeMap<SimpleRecipeBuilder> LOG_CREATE ;
    public static final RecipeMap<SimpleRecipeBuilder> MOLECULAR_RECOMBINATION ;
    public static final RecipeMap<SimpleRecipeBuilder> MOB_KILLER ;
    public static final RecipeMap<SimpleRecipeBuilder> SOLAR_TOWER ;
    public static final RecipeMap<SimpleRecipeBuilder> DISASSEMBLER_RECIPES = new RecipeMap<>("disassembler", new SimpleRecipeBuilder(),RecipeMap::getRecipeMapUI,1, 9, 0, 0)
            .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);


    public static final RecipeMap<NoEnergyRecipeBuilder> JET_WINGPACK_FUELS = new RecipeMap<>("jet_wingpack_fuels", new NoEnergyRecipeBuilder(),RecipeMap::getRecipeMapUI, 0, 0, 1, 0)
            .setSlotOverlay(false, false, GuiTextures.DARK_CANISTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressWidget.MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.TURBINE)
            .allowEmptyOutput();

    public static final RecipeMap<DronePadRecipeBuilder> DRONE_PAD = new RecipeMap<>("drone_pad",new DronePadRecipeBuilder(), RecipeMap::getRecipeMapUI, 3, 9, 3, 3);
    public static final RecipeMap<SimpleRecipeBuilder> COMBS_PRODUCT ;

    private DrtechReceipes() {
    }

    static {
        LOG_CREATE = new RecipeMap<>("log_create",new SimpleRecipeBuilder(),RecipeMap::getRecipeMapUI,2,2,1,1);
        MOLECULAR_RECOMBINATION = new RecipeMap<>("molecular_recombination",new SimpleRecipeBuilder(),RecipeMap::getRecipeMapUI,1,1,1,1);
        MOB_KILLER = new RecipeMap<>("mob_killer",(new SimpleRecipeBuilder()),RecipeMap::getRecipeMapUI,2,6,0,1);
        SOLAR_TOWER = new RecipeMap<>("solar_tower",new SimpleRecipeBuilder(),RecipeMap::getRecipeMapUI,0,0,1,1);
        COMBS_PRODUCT = new RecipeMap<>("combs_product",(new SimpleRecipeBuilder()),RecipeMap::getRecipeMapUI,3,9,2,3);
    }
}
