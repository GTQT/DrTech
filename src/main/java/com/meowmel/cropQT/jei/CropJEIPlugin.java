package com.meowmel.cropQT.jei;

import com.drppp.drtech.Tags;
import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.api.ISoilList;
import com.meowmel.cropQT.api.SoilRegistry;
import com.meowmel.cropQT.api.SubSoilRequirement;
import com.meowmel.cropQT.api.mutation.CropMutation;
import com.meowmel.cropQT.api.mutation.MutationPool;
import com.meowmel.cropQT.api.mutation.MutationRegistry;
import com.meowmel.cropQT.item.ItemCropSeed;
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
    public static final String SOIL_UID = Tags.MODID + ".soil";
    public static final String SUB_SOIL_UID = Tags.MODID + ".sub_soil";
    public static final String MUTATION_POOL_UID = Tags.MODID + ".mutation_pool";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
                new CropOutputCategory(registry.getJeiHelpers().getGuiHelper()),
                new CrossBreedingCategory(registry.getJeiHelpers().getGuiHelper()),
                new SoilCategory(registry.getJeiHelpers().getGuiHelper()),
                new SubSoilCategory(registry.getJeiHelpers().getGuiHelper()),
                new MutationPoolCategory(registry.getJeiHelpers().getGuiHelper())
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

        // === 杂交配方（支持 2~4 父本）===
        List<CrossBreedingRecipeWrapper> crossRecipes = new ArrayList<>();
        for (CropMutation mutation : MutationRegistry.getAllMutations()) {
            CropType result = CropRegistry.get(mutation.getResult());
            if (result == null) continue;

            List<CropType> parents = new ArrayList<>(mutation.getParents().size());
            boolean allResolved = true;
            for (String parentId : mutation.getParents()) {
                CropType parent = CropRegistry.get(parentId);
                if (parent == null) {
                    allResolved = false;
                    break;
                }
                parents.add(parent);
            }
            if (!allResolved) continue;

            crossRecipes.add(new CrossBreedingRecipeWrapper(mutation, result, parents));
        }
        registry.addRecipes(crossRecipes, CROSS_BREEDING_UID);

        // 5 台作物机器不摆 JEI 页——它们的配方是逐条查作物表的动态逻辑，摆几页假示例
        // 教不会任何东西。用法写在机器物品自己的 tooltip 里。

        // === 土壤组 ===
        List<SoilRecipeWrapper> soilRecipes = new ArrayList<>();
        for (ISoilList soil : SoilRegistry.getAll()) {
            soilRecipes.add(new SoilRecipeWrapper(soil));
        }
        registry.addRecipes(soilRecipes, SOIL_UID);

        // === 底土要求 ===
        List<SubSoilRecipeWrapper> subSoilRecipes = new ArrayList<>();
        for (SubSoilRequirement requirement : SubSoilRequirement.getAll()) {
            if (requirement.isEmpty()) continue;
            subSoilRecipes.add(new SubSoilRecipeWrapper(requirement));
        }
        registry.addRecipes(subSoilRecipes, SUB_SOIL_UID);

        // === 变异池 ===
        List<MutationPoolRecipeWrapper> poolRecipes = new ArrayList<>();
        for (MutationPool pool : MutationRegistry.getAllPools()) {
            poolRecipes.add(new MutationPoolRecipeWrapper(pool));
        }
        registry.addRecipes(poolRecipes, MUTATION_POOL_UID);

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
