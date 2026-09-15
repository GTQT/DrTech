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
 * JEI「底土」页签。
 *
 * <p>只做展示，所以每个成员槽都是输入槽——玩家照着页签去找方块就行。
 */
public class SubSoilCategory implements IRecipeCategory<SubSoilRecipeWrapper> {

    private static final int SLOT_X = 4;
    private static final int SLOT_Y = 22;
    private static final int COLUMNS = 4;

    private final IDrawable background;
    private final IDrawable icon;

    public SubSoilCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 92);
        this.icon = guiHelper.createDrawableIngredient(
                new ItemStack(Blocks.IRON_ORE));
    }

    @Override
    public String getUid() {
        return CropJEIPlugin.SUB_SOIL_UID;
    }

    @Override
    public String getTitle() {
        return "底土";
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
    public void setRecipe(IRecipeLayout layout, SubSoilRecipeWrapper recipe, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        for (int i = 0; i < recipe.getDisplayItems().size(); i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            stacks.init(i, true, SLOT_X + col * 18, SLOT_Y + row * 18);
            stacks.set(i, recipe.getDisplayItems().get(i));
        }
    }
}
