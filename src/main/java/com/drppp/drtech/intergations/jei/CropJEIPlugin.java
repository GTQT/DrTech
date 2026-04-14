package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.Tags;
import com.drppp.drtech.api.crop.CropRegistry;
import com.drppp.drtech.api.crop.CropType;
import com.drppp.drtech.api.crop.CrossBreedingRegistry;
import com.drppp.drtech.common.Items.ItemCropSeed;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JEIPlugin
public class CropJEIPlugin implements IModPlugin {

    public static final String CROP_OUTPUT_UID = Tags.MODID + ".crop_output";
    public static final String CROSS_BREEDING_UID = Tags.MODID + ".cross_breeding";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
                new CropOutputCategory(registry.getJeiHelpers().getGuiHelper()),
                new CrossBreedingCategory(registry.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void register(IModRegistry registry) {
        // === 种子产出配方 ===
// === 种子产出配方 ===
        List<CropOutputRecipeWrapper> outputRecipes = new ArrayList<>();
        for (Map.Entry<String, CropType> entry : CropRegistry.getAll().entrySet()) {
            CropType type = entry.getValue();
            if (type.getId().equals("weed")) continue;
            // 有任何一种掉落方式就显示
            if (type.getDrops().isEmpty()
                    && type.getChanceDrops().isEmpty()
                    && type.getLootTable() == null
                    && type.getBlockDrops().isEmpty()) continue;
            outputRecipes.add(new CropOutputRecipeWrapper(type));
        }
        registry.addRecipes(outputRecipes, CROP_OUTPUT_UID);

        // === 杂交配方 ===
        List<CrossBreedingRecipeWrapper> crossRecipes = new ArrayList<>();
        for (CrossBreedingRegistry.CrossRecipe recipe : CrossBreedingRegistry.getAllRecipes()) {
            CropType parent1 = CropRegistry.get(recipe.parent1);
            CropType parent2 = CropRegistry.get(recipe.parent2);
            CropType result = CropRegistry.get(recipe.result);
            if (parent1 == null || parent2 == null || result == null) continue;
            crossRecipes.add(new CrossBreedingRecipeWrapper(recipe, parent1, parent2, result));
        }
        registry.addRecipes(crossRecipes, CROSS_BREEDING_UID);

        // === 催化剂 ===
        for (Map.Entry<String, CropType> entry : CropRegistry.getAll().entrySet()) {
            CropType type = entry.getValue();
            if (type.getId().equals("weed")) continue;
            ItemStack seedBag = ItemCropSeed.createSeedBag(type.getId());
            if (!seedBag.isEmpty()) {
                registry.addRecipeCatalyst(seedBag, CROP_OUTPUT_UID);
                registry.addRecipeCatalyst(seedBag, CROSS_BREEDING_UID);
            }
        }

        // === 配方处理器 ===
        registry.handleRecipes(CropOutputRecipeWrapper.class, r -> r, CROP_OUTPUT_UID);
        registry.handleRecipes(CrossBreedingRecipeWrapper.class, r -> r, CROSS_BREEDING_UID);
    }
}
