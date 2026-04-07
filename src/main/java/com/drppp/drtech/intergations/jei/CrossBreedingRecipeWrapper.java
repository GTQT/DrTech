package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.common.Blocks.Crops.CropType;
import com.drppp.drtech.common.Blocks.Crops.CrossBreedingRegistry;
import com.drppp.drtech.common.Items.ItemCropSeed;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class CrossBreedingRecipeWrapper implements IRecipeWrapper {

    private final CrossBreedingRegistry.CrossRecipe recipe;
    private final CropType parent1Type;
    private final CropType parent2Type;
    private final CropType resultType;
    private final ItemStack parent1Seed;
    private final ItemStack parent2Seed;
    private final ItemStack resultSeed;

    public CrossBreedingRecipeWrapper(CrossBreedingRegistry.CrossRecipe recipe,
                                      CropType parent1, CropType parent2, CropType result) {
        this.recipe = recipe;
        this.parent1Type = parent1;
        this.parent2Type = parent2;
        this.resultType = result;
        this.parent1Seed = ItemCropSeed.createSeedBag(parent1.getId());
        this.parent2Seed = ItemCropSeed.createSeedBag(parent2.getId());
        this.resultSeed = ItemCropSeed.createSeedBag(result.getId());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(parent1Seed, parent2Seed));
        ingredients.setOutput(VanillaTypes.ITEM, resultSeed);
    }

    public ItemStack getParent1Seed() { return parent1Seed; }
    public ItemStack getParent2Seed() { return parent2Seed; }
    public ItemStack getResultSeed() { return resultSeed; }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        // 标题: 亲本名称
        String p1 = parent1Type.getDisplayName();
        String p2 = parent2Type.getDisplayName();
        String title = p1 + " \u00d7 " + p2;
        // 如果太长就缩短
        if (minecraft.fontRenderer.getStringWidth(title) > 155) {
            title = p1.substring(0, Math.min(p1.length(), 4)) + " \u00d7 " + p2.substring(0, Math.min(p2.length(), 4));
        }
        minecraft.fontRenderer.drawString(title, 4, 4, 0x333333);

        // 结果名
        String resultName = TextFormatting.DARK_GREEN + resultType.getDisplayName();
        minecraft.fontRenderer.drawString(resultName, 105, 8, 0x336633);

        // 底部: 权重 + Tier
        String info = TextFormatting.GRAY + "权重:" + recipe.weight
                + " T" + resultType.getTier();
        minecraft.fontRenderer.drawString(info, 4, 52, 0x888888);

    }
}
