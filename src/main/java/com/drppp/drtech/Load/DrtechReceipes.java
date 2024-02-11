package com.drppp.drtech.Load;


import com.drppp.drtech.Load.builder.EImplosionRecipeBuilder;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.builders.ImplosionRecipeBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.machines.RecipeMapAssemblyLine;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.init.SoundEvents;

public final class DrtechReceipes {
    public static final RecipeMap<EImplosionRecipeBuilder> EIMPLOSION_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> UU_RECIPES ;
    public static final RecipeMap<SimpleRecipeBuilder> COPY_RECIPES ;
    public static final RecipeMap<SimpleRecipeBuilder> LOG_CREATE ;
    public static final RecipeMap<SimpleRecipeBuilder> MOLECULAR_RECOMBINATION ;
    public static final RecipeMap<SimpleRecipeBuilder> MOB_KILLER ;

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
        UU_RECIPES = new RecipeMap<>("uu_producter",1,0,1,1,(new SimpleRecipeBuilder()),false);
        COPY_RECIPES = new RecipeMap<>("uu_copy",1,1,1,1,(new SimpleRecipeBuilder()),false);
        LOG_CREATE = new RecipeMap<>("log_create",1,1,1,1,(new SimpleRecipeBuilder()),false);
        MOLECULAR_RECOMBINATION = new RecipeMap<>("molecular_recombination",1,1,1,1,(new SimpleRecipeBuilder()),false);
        MOB_KILLER = new RecipeMap<>("mob_killer",2,6,0,1,(new SimpleRecipeBuilder()),false);

    }
}
