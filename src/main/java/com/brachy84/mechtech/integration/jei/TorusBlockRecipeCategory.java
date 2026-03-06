package com.brachy84.mechtech.integration.jei;


import com.brachy84.mechtech.MechTech;
import com.brachy84.mechtech.common.machines.MTTileEntities;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nullable;

public class TorusBlockRecipeCategory implements IRecipeCategory<TorusBlockRecipeWrapper> {

    private final IDrawable background;
    private IDrawable icon;

    public TorusBlockRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(176, 50);
        icon = helper.createDrawableIngredient(MTTileEntities.TESLA_TOWER.getStackForm());
    }

    @Override
    public String getUid() {
        return JEIMTPlugin.TORUS_RECIPES;
    }

    @Override
    public String getTitle() {
        return I18n.format("mechtech.jei_category.torus_block");
    }

    @Override
    public String getModName() {
        return MechTech.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, TorusBlockRecipeWrapper recipeWrapper, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().set(ingredients);

    }
}
