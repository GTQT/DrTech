package com.drppp.drtech.loaders;


import com.drppp.drtech.api.recipes.builder.DronePadRecipeBuilder;
import com.drppp.drtech.api.recipes.builder.NoEnergyRecipeBuilder;
import com.drppp.drtech.loaders.builder.EImplosionRecipeBuilder;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.init.SoundEvents;

public final class DrtechReceipes {
    public static final RecipeMap<EImplosionRecipeBuilder> EIMPLOSION_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> LOG_CREATE ;
    public static final RecipeMap<SimpleRecipeBuilder> MOLECULAR_RECOMBINATION ;
    public static final RecipeMap<SimpleRecipeBuilder> MOB_KILLER ;
    public static final RecipeMap<SimpleRecipeBuilder> SOLAR_TOWER ;
    public static final RecipeMap<SimpleRecipeBuilder> DISASSEMBLER_RECIPES = new RecipeMap<>("disassembler", 1, 9, 0, 0,new SimpleRecipeBuilder(),false)
            .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL);


    public static final RecipeMap<NoEnergyRecipeBuilder> JET_WINGPACK_FUELS = new RecipeMap<>("jet_wingpack_fuels", 0, 0, 1, 0, new NoEnergyRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.DARK_CANISTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressWidget.MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.TURBINE)
            .allowEmptyOutput();

    public static final RecipeMap<DronePadRecipeBuilder> DRONE_PAD = new RecipeMap<>("drone_pad", 3, 9, 3, 3, new DronePadRecipeBuilder(), false);
    public static final RecipeMap<SimpleRecipeBuilder> COMBS_PRODUCT ;

    private DrtechReceipes() {
    }

    static {

             EIMPLOSION_RECIPES = (new RecipeMap
                     ("eimplosion_compressor", 3, 2, 0, 0,
                             (EImplosionRecipeBuilder)((EImplosionRecipeBuilder)(new EImplosionRecipeBuilder())
                                     .duration(20))
                                     .EUt(GTValues.VA[4]), false))
                     .setSlotOverlay(false, false, true, GuiTextures.IMPLOSION_OVERLAY_1)
                     .setSlotOverlay(false, false, false, GuiTextures.IMPLOSION_OVERLAY_2)
                     .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                     .setSound(SoundEvents.ENTITY_GENERIC_EXPLODE);
        LOG_CREATE = new RecipeMap<>("log_create",2,2,1,1,(new SimpleRecipeBuilder()),false);
        MOLECULAR_RECOMBINATION = new RecipeMap<>("molecular_recombination",1,1,1,1,(new SimpleRecipeBuilder()),false);
        MOB_KILLER = new RecipeMap<>("mob_killer",2,6,0,1,(new SimpleRecipeBuilder()),false);
        SOLAR_TOWER = new RecipeMap<>("solar_tower",0,0,1,1,(new SimpleRecipeBuilder()),false);
        COMBS_PRODUCT = new RecipeMap<>("combs_product",3,9,2,3,(new SimpleRecipeBuilder()),false);
    }
}
