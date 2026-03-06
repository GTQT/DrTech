package com.brachy84.mechtech.integration.jei;

//import gregicadditions.GAValues;

import com.brachy84.mechtech.api.ToroidBlock;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class TorusBlockRecipeWrapper implements IRecipeWrapper {

    private final ToroidBlock torusBlock;

    public TorusBlockRecipeWrapper(ToroidBlock torusBlock) {
        this.torusBlock = torusBlock;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, torusBlock.getItemForm());
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        drawLine(minecraft, "mechtech.recipe.torus_block.dmg", 0, 18, 0x111111, torusBlock.getDmgModifier());
        drawLine(minecraft, "mechtech.recipe.torus_block.amps", 0, 27, 0x111111, torusBlock.getAmpsPerBlock());
        drawLine(minecraft, "mechtech.recipe.torus_block.range", 0, 36, 0x111111, torusBlock.getRangeModifier());
    }

    public void drawLine(Minecraft minecraft, String langKey, int x, int y, int color, Object... obj) {
        minecraft.fontRenderer.drawString(I18n.format(langKey, obj), x, y, color);
    }

}
