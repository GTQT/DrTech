package com.brachy84.mechtech.integration.jei;

import com.brachy84.mechtech.MechTech;
import com.brachy84.mechtech.api.ToroidBlock;
import com.brachy84.mechtech.common.machines.MTTileEntities;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMap;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

import java.util.stream.Collectors;

@JEIPlugin
public class JEIMTPlugin implements IModPlugin {

    public JEIMTPlugin() {
    }

    public static final String TORUS_RECIPES = MechTech.MODID + ":torus_block";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
                new TorusBlockRecipeCategory(registry.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipes(ToroidBlock.getAll().stream().map((TorusBlockRecipeWrapper::new)).collect(Collectors.toList()), TORUS_RECIPES);
        registry.addRecipeCatalyst(MTTileEntities.TESLA_TOWER.getStackForm(), TORUS_RECIPES);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        IRecipeRegistry registry = jeiRuntime.getRecipeRegistry();

        /*if (MTConfig.multis.tokamak.enableTokamak) {
            IFocus<ItemStack> focus = registry.createFocus(IFocus.Mode.OUTPUT, GATileEntities.ADVANCED_FUSION_REACTOR.getStackForm());
            IRecipeCategory category = registry.getRecipeCategory(MUTLIBLOCK_INFOS);
            if(category == null) return;
            registry.getRecipeWrappers(category, focus).forEach(wrapper -> {
                if(!multiblockInfos.contains(wrapper)) {
                    registry.hideRecipe((IRecipeWrapper) wrapper, MUTLIBLOCK_INFOS);
                }
            });
            //IRecipeWrapper recipe = registry.getRecipeWrapper(new FusionReactor4Info(), MUTLIBLOCK_INFOS);
            //if(recipe == null) return;
            //registry.hideRecipe(recipe, MUTLIBLOCK_INFOS);
        }*/
    }

    public String getRecipeMapJeiCategory(RecipeMap<?> recipeMap) {
        return GTValues.MODID + ":" + recipeMap.unlocalizedName;
    }
}
