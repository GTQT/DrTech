package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.api.Utils.RewardBoxManager;
import com.drppp.drtech.api.Utils.RewardEntry;
import com.drppp.drtech.common.Items.MetaItems.MetaItemLootTable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootTableJeiWrapper implements IRecipeWrapper {

    private final String tableName;
    private final ItemStack input;
    private final List<RewardEntry> entries;
    private final List<ItemStack> outputs;

    public LootTableJeiWrapper(String tableName) {
        this.tableName = tableName;
        this.input = MetaItemLootTable.LOOT_BAG_ITEMS.getOrDefault(tableName, ItemStack.EMPTY);
        List<RewardEntry> table = RewardBoxManager.getRewardTable(tableName);
        this.entries = table != null ? table : Collections.emptyList();
        this.outputs = new ArrayList<>();
        for (RewardEntry entry : entries) {
            outputs.add(entry.getItemStack().copy());
        }
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, input);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    public String getTableName() {
        return tableName;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public int getEntryChance(int index) {
        if (index >= 0 && index < entries.size()) {
            return entries.get(index).getProbability();
        }
        return -1;
    }
}