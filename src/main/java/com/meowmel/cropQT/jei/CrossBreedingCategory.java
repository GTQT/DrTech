package com.meowmel.cropQT.jei;

import com.drppp.drtech.Tags;
import com.meowmel.cropQT.item.ItemCropSeed;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 杂交配方页。
 *
 * <p>父本槽位按数量排开（2~4 个），中间用 "+" 连、产物在右侧。
 */
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

        // 父本：槽位 0..n-1
        for (int i = 0; i < recipe.getParentCount(); i++) {
            stacks.init(i, true, CrossBreedingRecipeWrapper.slotX(i), CrossBreedingRecipeWrapper.slotY());
            stacks.set(i, recipe.getParentSeeds().get(i));
        }

        // 产物：槽位 n
        int resultIndex = recipe.getParentCount();
        stacks.init(resultIndex, false,
                CrossBreedingRecipeWrapper.resultSlotX(), CrossBreedingRecipeWrapper.slotY());
        stacks.set(resultIndex, recipe.getResultSeed());
    }

    // 分隔符（父本间的 "+" 与产物前的 "→"）画在 CrossBreedingRecipeWrapper#drawInfo 里——
    // JEI 的 drawExtras 拿不到当前配方，只有 wrapper 知道这一页有几个父本。
}
