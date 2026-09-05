package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.Tags;
import com.drppp.drtech.api.Utils.RewardBoxManager;
import com.drppp.drtech.common.Items.MetaItems.MetaItemLootTable;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JEIPlugin
public class LootTableJeiPlugin implements IModPlugin {

    public static final String LOOT_TABLE_UID = Tags.MODID + ".loot_table";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
                new LootTableJeiCategory(registry.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void register(IModRegistry registry) {
        List<LootTableJeiWrapper> recipes = new ArrayList<>();
        for (String name : RewardBoxManager.getAllRewardBoxes().keySet()) {
            recipes.add(new LootTableJeiWrapper(name));
        }
        registry.addRecipes(recipes, LOOT_TABLE_UID);

        // 为每个奖励袋物品注册催化剂，点击可查看所有奖励袋配方
        for (Map.Entry<String, ItemStack> entry : MetaItemLootTable.LOOT_BAG_ITEMS.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                registry.addRecipeCatalyst(entry.getValue(), LOOT_TABLE_UID);
            }
        }
    }
}
