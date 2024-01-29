package com.drppp.drtech.Load;


import com.drppp.drtech.Load.builder.EImplosionRecipeBuilder;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.builders.ImplosionRecipeBuilder;
import gregtech.api.recipes.machines.RecipeMapAssemblyLine;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.init.SoundEvents;

public final class DrtechReceipes {
    public static final RecipeMap<EImplosionRecipeBuilder> EIMPLOSION_RECIPES;

    private DrtechReceipes() {
    }

    static {
             EIMPLOSION_RECIPES = (new RecipeMap
                     ("eimplosion_compressor", 3, 2, 0, 0,
                             (EImplosionRecipeBuilder)((EImplosionRecipeBuilder)(new EImplosionRecipeBuilder())
                                     .duration(20))
                                     .EUt(GTValues.VA[4]), false))
                     .setSlotOverlay(false, false, true,
                             GuiTextures.IMPLOSION_OVERLAY_1)
                     .setSlotOverlay(false, false, false, GuiTextures.IMPLOSION_OVERLAY_2)
                     .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                     .setSound(SoundEvents.ENTITY_GENERIC_EXPLODE);

    }
}
