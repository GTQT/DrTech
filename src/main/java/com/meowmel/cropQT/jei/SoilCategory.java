package com.meowmel.cropQT.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * JEI「土壤」页签。
 *
 * <p>布局：上半是代表方块（最多 8 个，4 列 2 行），下半是水肥数值。
 */
public class SoilCategory implements IRecipeCategory<SoilRecipeWrapper> {

    private static final int SLOT_X = 4;
    private static final int SLOT_Y = SoilRecipeWrapper.SLOTS_Y;
    private static final int COLUMNS = 4;

    private final IDrawable background;
    private final IDrawable icon;

    public SoilCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 122);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.FARMLAND));
    }

    @Override
    public String getUid() {
        return CropJEIPlugin.SOIL_UID;
    }

    @Override
    public String getTitle() {
        return "土壤";
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
    public void setRecipe(IRecipeLayout layout, SoilRecipeWrapper recipe, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        for (int i = 0; i < recipe.getDisplayItems().size(); i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            stacks.init(i, true, SLOT_X + col * 18, SLOT_Y + row * 18);
            stacks.set(i, recipe.getDisplayItems().get(i));
        }
    }
}
