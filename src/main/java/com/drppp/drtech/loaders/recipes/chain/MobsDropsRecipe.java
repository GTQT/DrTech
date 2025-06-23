package com.drppp.drtech.loaders.recipes.chain;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static com.drppp.drtech.loaders.recipes.DrtechReceipes.MOB_KILLER;

public class MobsDropsRecipe {
    public static void load()
    {
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(4))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(5))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(6))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(7))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(8))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(9))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(10))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(11))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(12))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(13))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(14))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(300).buildAndRegister();
    }
}
