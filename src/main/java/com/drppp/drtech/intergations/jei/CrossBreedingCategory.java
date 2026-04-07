package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.ItemCropSeed;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
public class CrossBreedingCategory implements IRecipeCategory<CrossBreedingRecipeWrapper> {

    private final IDrawable background;
    private final IDrawable icon;

    public CrossBreedingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 65);
        this.icon = guiHelper.createDrawableIngredient(ItemCropSeed.createSeedBag("ferru"));
    }

    @Override
    public String getUid() { return CropJEIPlugin.CROSS_BREEDING_UID; }

    @Override
    public String getTitle() { return "作物杂交"; }

    @Override
    public String getModName() { return "DRTech"; }

    @Override
    public IDrawable getBackground() { return background; }

    @Nullable
    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayout layout, CrossBreedingRecipeWrapper recipe, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();

        // 左: 亲本A (slot 0)
        stacks.init(0, true, 10, 24);
        stacks.set(0, recipe.getParent1Seed());

        // 中: 亲本B (slot 1)
        stacks.init(1, true, 50, 24);
        stacks.set(1, recipe.getParent2Seed());

        // 右: 产物 (slot 2)
        stacks.init(2, false, 120, 24);
        stacks.set(2, recipe.getResultSeed());
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        // "+" 号
        minecraft.fontRenderer.drawString("+", 35, 28, 0x808080);
        // 箭头
        minecraft.fontRenderer.drawString("\u2192", 80, 28, 0x808080);
    }
}
