package com.drppp.drtech.loaders;


import com.drppp.drtech.api.recipes.builder.DronePadRecipeBuilder;
import com.drppp.drtech.loaders.builder.EImplosionRecipeBuilder;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import net.minecraft.init.SoundEvents;

public final class DrtechReceipes {
    public static final RecipeMap<EImplosionRecipeBuilder> EIMPLOSION_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> UU_RECIPES ;
    public static final RecipeMap<SimpleRecipeBuilder> COPY_RECIPES ;
    public static final RecipeMap<SimpleRecipeBuilder> LOG_CREATE ;
    public static final RecipeMap<SimpleRecipeBuilder> MOLECULAR_RECOMBINATION ;
    public static final RecipeMap<SimpleRecipeBuilder> MOB_KILLER ;
    public static final RecipeMap<SimpleRecipeBuilder> DRRP_GROUND_PUMP ;
    public static final RecipeMap<SimpleRecipeBuilder> SOLAR_TOWER ;


    public static final RecipeMap<DronePadRecipeBuilder> DRONE_PAD = new RecipeMap<>("drone_pad", 4, 9, 0, 0, new DronePadRecipeBuilder(), false);


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
        UU_RECIPES = new RecipeMap<>("uu_producter",2,0,1,1,(new SimpleRecipeBuilder()),false);
        COPY_RECIPES = new RecipeMap<>("uu_copy",2,1,1,1,(new SimpleRecipeBuilder()),false);
        LOG_CREATE = new RecipeMap<>("log_create",2,2,1,1,(new SimpleRecipeBuilder()),false);
        MOLECULAR_RECOMBINATION = new RecipeMap<>("molecular_recombination",1,1,1,1,(new SimpleRecipeBuilder()),false);
        MOB_KILLER = new RecipeMap<>("mob_killer",2,6,0,1,(new SimpleRecipeBuilder()),false);
        DRRP_GROUND_PUMP = new RecipeMap<>("deep_ground_pump",1,0,1,4,(new SimpleRecipeBuilder()),false);
        SOLAR_TOWER = new RecipeMap<>("solar_tower",0,0,1,1,(new SimpleRecipeBuilder()),false);

    }
}
