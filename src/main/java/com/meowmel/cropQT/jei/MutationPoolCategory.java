package com.meowmel.cropQT.jei;

import com.meowmel.cropQT.item.ItemCropSeed;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import org.jetbrains.annotations.Nullable;

/**
 * JEI「变异池」页签。
 */
public class MutationPoolCategory implements IRecipeCategory<MutationPoolRecipeWrapper> {

    private static final int SLOT_X = 4;
    private static final int SLOT_Y = 24;
    private static final int COLUMNS = 6;

    private final IDrawable background;
    private final IDrawable icon;

    public MutationPoolCategory(IGuiHelper guiHelper) {
        // 3 行 × 6 列
        this.background = guiHelper.createBlankDrawable(160, 82);
        this.icon = guiHelper.createDrawableIngredient(ItemCropSeed.createSeedBag("dandelion"));
    }

    @Override
    public String getUid() {
        return CropJEIPlugin.MUTATION_POOL_UID;
    }

    @Override
    public String getTitle() {
        return "变异池";
    }

    @Override
    public String getModName() {
        return "DRTech";
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout layout, MutationPoolRecipeWrapper recipe, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        for (int i = 0; i < recipe.getMembers().size(); i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            stacks.init(i, true, SLOT_X + col * 18, SLOT_Y + row * 18);
            stacks.set(i, recipe.getMembers().get(i));
        }
    }
}
