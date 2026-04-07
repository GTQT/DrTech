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

public class CropOutputCategory implements IRecipeCategory<CropOutputRecipeWrapper> {

    private final IDrawable background;
    private final IDrawable icon;

    public CropOutputCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 65);
        this.icon = guiHelper.createDrawableIngredient(ItemCropSeed.createSeedBag("wheat"));
    }

    @Override
    public String getUid() { return CropJEIPlugin.CROP_OUTPUT_UID; }

    @Override
    public String getTitle() { return "作物产出"; }

    @Override
    public String getModName() { return "DRTech"; }

    @Override
    public IDrawable getBackground() { return background; }

    @Nullable
    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayout layout, CropOutputRecipeWrapper recipe, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();

        // 左侧: 种子袋 (slot 0)
        stacks.init(0, true, 7, 24);
        stacks.set(0, recipe.getInput());

        // 右侧: 产出物品 (slot 1~4, 最多4种)
        for (int i = 0; i < Math.min(recipe.getAllOutputs().size(), 4); i++) {
            stacks.init(1 + i, false, 85 + i * 18, 24);
            stacks.set(1 + i, recipe.getAllOutputs().get(i));
        }
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        minecraft.fontRenderer.drawString("\u2192", 38, 28, 0x808080);
    }
}

