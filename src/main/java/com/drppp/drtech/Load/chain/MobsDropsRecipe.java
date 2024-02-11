package com.drppp.drtech.Load.chain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.drppp.drtech.Load.DrtechReceipes.MOB_KILLER;

public class MobsDropsRecipe {
    public static void load()
    {
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(1))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(2))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(3))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(4))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(5))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(6))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(7))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(8))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(9))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(10))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(11))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(12))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(13))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
        MOB_KILLER.recipeBuilder()
                .notConsumable(new IntCircuitIngredient(14))
                .chancedOutput(new ItemStack(Items.EXPERIENCE_BOTTLE,1),10,10)
                .EUt(2000)
                .duration(200).buildAndRegister();
    }
}
