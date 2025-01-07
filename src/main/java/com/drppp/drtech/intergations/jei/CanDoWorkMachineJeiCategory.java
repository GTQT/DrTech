package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.Tags;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class CanDoWorkMachineJeiCategory implements IRecipeCategory<CanDoWorkMachineJei> {
    @Nonnull
    public String getModName() {
        return Tags.MODID;
    }
    @Nonnull
    private final IDrawable background;
    public CanDoWorkMachineJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(144, 120);
    }
    @Nonnull
    public String getUid() {
        return Tags.MODID+":CanDoWorkMachines";
    }

    @Nonnull
    public String getTitle() {
        return "配方联和器支持列表";
    }

    @Nonnull
    public IDrawable getBackground() {
        return this.background;
    }

    @Nullable
    public IDrawable getIcon() {
        return null;
    }

    public void setRecipe(IRecipeLayout recipeLayout, CanDoWorkMachineJei recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        for(int i = 0; i < recipeWrapper.getInputCount(); ++i) {
            int xPos = 18*(i%8);
            itemStackGroup.init(i, true, xPos, 10+ i/8*18);
        }

        itemStackGroup.set(ingredients);
    }
    public void drawExtras(Minecraft minecraft) {
        minecraft.fontRenderer.drawString("支持机器：", 0, 0, 0x111111);
    }

}
