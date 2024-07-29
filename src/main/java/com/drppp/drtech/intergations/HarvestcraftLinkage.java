package com.drppp.drtech.intergations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import forestry.api.core.ForestryAPI;
import forestry.api.farming.IFarmable;
import forestry.api.recipes.RecipeManagers;
import forestry.core.utils.Log;
import forestry.farming.logic.farmables.FarmableAgingCrop;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static gregtech.api.recipes.RecipeMaps.EXTRACTOR_RECIPES;

public class HarvestcraftLinkage {
    public static final String CRAFT_ID = "harvestcraft";

    public static void MachineRecipeInit()
    {
        ImmutableList<String> oilItems = ImmutableList.of("cranberry", "blackberry", "blueberry", "raspberry", "strawberry","pineapple", "cactusfruit", "cantaloupe", "grape", "kiwi", "chilipepper",
                "asparagus", "bean", "beet", "broccoli", "cauliflower", "celery", "leek", "lettuce", "onion", "parsnip", "radish", "rutabaga","scallion", "soybean", "sweetpotato", "turnip",
                "whitemushroom", "artichoke", "bellpepper", "brusselsprout", "cabbage", "corn", "cucumber", "eggplant", "okra", "peas", "rhubarb", "seaweed",
                "tomato", "wintersquash", "zucchini", "bambooshoot", "spinach","barley", "oats", "rye","cotton", "rice", "tealeaf", "coffeebean", "candleberry","garlic","ginger", "spiceleaf"
        );
        ImmutableList<String> cropNuts = ImmutableList.of("peanut");
        ImmutableList<String> nuts = ImmutableList.of("walnut", "almond", "cashew", "chestnut", "pecan", "pistachio", "cherry");
        Fluid seedOil = FluidRegistry.getFluid("seed.oil");
        int seedamount = ForestryAPI.activeMode.getIntegerSetting("squeezer.liquid.seed");
        UnmodifiableIterator var24;
        String Name;
        ItemStack mustardFruit;
        for(var24 = oilItems.iterator(); var24.hasNext();) {
            Name = (String)var24.next();
            mustardFruit = HarvestcraftLinkage.getItemStack(Name + "seeditem");
            if (mustardFruit != null && seedOil != null) {
                EXTRACTOR_RECIPES.recipeBuilder()
                        .inputs(mustardFruit)
                        .fluidOutputs(new FluidStack(seedOil, seedamount))
                        .EUt(2)
                        .duration(32)
                        .buildAndRegister();
            }
        }
        UnmodifiableIterator var42;
        ItemStack hcBeeswaxItem;
        String nutName;
        var42 = cropNuts.iterator();
        while(var42.hasNext()) {
            nutName = (String)var42.next();
            hcBeeswaxItem = HarvestcraftLinkage.getItemStack(nutName + "item");
            if (hcBeeswaxItem != null && seedOil != null) {
                EXTRACTOR_RECIPES.recipeBuilder()
                        .inputs(hcBeeswaxItem)
                        .fluidOutputs(new FluidStack(seedOil, 12*seedamount))
                        .EUt(2)
                        .duration(32)
                        .buildAndRegister();
            }
        }
        var42 = nuts.iterator();
        while(var42.hasNext()) {
            nutName = (String)var42.next();
            hcBeeswaxItem = HarvestcraftLinkage.getItemStack(nutName + "item");
            if (hcBeeswaxItem != null && seedOil != null) {
                EXTRACTOR_RECIPES.recipeBuilder()
                        .inputs(hcBeeswaxItem)
                        .fluidOutputs(new FluidStack(seedOil, 15*seedamount))
                        .EUt(2)
                        .duration(32)
                        .buildAndRegister();
            }
        }
    }
    @Nullable
    protected static ItemStack getItemStack(@Nonnull String itemName) {
        return HarvestcraftLinkage.getItemStack(itemName, 0);
    }
    @Nullable
    protected static ItemStack getItemStack(@Nonnull String itemName, int meta) {
        Item item = HarvestcraftLinkage.getItem(itemName);
        return item == null ? null : new ItemStack(item, 1, meta);
    }
    @Nullable
    protected static Item getItem(String itemName) {
        ResourceLocation key = new ResourceLocation(CRAFT_ID, itemName);
        if (ForgeRegistries.ITEMS.containsKey(key)) {
            return (Item)ForgeRegistries.ITEMS.getValue(key);
        } else {
            Log.debug("Could not find item {}", new Object[]{key});
            return null;
        }
    }
}
