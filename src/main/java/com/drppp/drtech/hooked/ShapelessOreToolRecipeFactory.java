package com.drppp.drtech.hooked;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.concurrent.ThreadLocalRandom;

public class ShapelessOreToolRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");
        NonNullList<Ingredient> ingredients = NonNullList.create();
        NonNullList<Ingredient> tools = NonNullList.create();
        JsonUtils.getJsonArray(json, "ingredients").forEach(element -> ingredients.add(CraftingHelper.getIngredient(element, context)));
        JsonUtils.getJsonArray(json, "tools").forEach(element -> tools.add(CraftingHelper.getIngredient(element, context)));
        if (ingredients.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }
        ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
        return new ShapelessOreToolRecipe(group.isEmpty() ? null : new ResourceLocation(group), ingredients, tools, result);
    }

    public static class ShapelessOreToolRecipe extends ShapelessOreRecipe {
        private final NonNullList<Ingredient> tools;

        public ShapelessOreToolRecipe(ResourceLocation group, NonNullList<Ingredient> input, NonNullList<Ingredient> tools, ItemStack result) {
            super(group, input, result);
            this.tools = tools;
        }

        @Override
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            NonNullList<ItemStack> list = super.getRemainingItems(inv);
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && matchesTool(stack)) {
                    ItemStack newStack = stack.copy();
                    newStack.attemptDamageItem(1, ThreadLocalRandom.current(), null);
                    if (newStack.getItemDamage() <= newStack.getMaxDamage()) {
                        list.set(i, newStack);
                    }
                }
            }
            return list;
        }

        private boolean matchesTool(ItemStack stack) {
            for (Ingredient ingredient : tools) {
                if (ingredient.apply(stack)) {
                    return true;
                }
            }
            return false;
        }
    }
}
