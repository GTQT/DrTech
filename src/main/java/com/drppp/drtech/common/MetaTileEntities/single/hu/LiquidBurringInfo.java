package com.drppp.drtech.common.MetaTileEntities.single.hu;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.*;


public class LiquidBurringInfo {

    public static List<LiquidAndHu> listFuel = new ArrayList<>();
    public static void init()
    {
        Collection<RecipeMap> Recipes = new ArrayList<>();
        Recipes.add(RecipeMaps.COMBUSTION_GENERATOR_FUELS);
        Recipes.add(RecipeMaps.GAS_TURBINE_FUELS);
        for(RecipeMap map:Recipes)
        {
            Collection<Recipe> rc = map.getRecipeList();
            rc.forEach(LiquidBurringInfo::initFuelsDta);
        }
    }

    private static void initFuelsDta(Recipe recipe) {
        if(recipe!=null)
        {
            int eut = recipe.getEUt();
            int time = recipe.getDuration();
            var fluid = recipe.getFluidInputs();
            for (var item : fluid)
            {
                if(!ContainsFuel(item.getInputFluidStack()))
                {
                    int amount = item.getAmount();
                    LiquidBurringInfo.listFuel.add(new LiquidBurringInfo.LiquidAndHu(item.getInputFluidStack(),eut*time/amount));
                }
            }
        }
    }

    public static boolean ContainsFuel(FluidStack source)
    {
        for (int i = 0; i < listFuel.size(); i++)
        {
            if(listFuel.get(i).fuel.isFluidEqual(source))
                return true;
        }
        return false;
    }
    public static boolean ContainsFuel(ItemStack source)
    {
        if(source.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,null))
        {
            var item = source.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,null);
            FluidStack fluidStack = item.drain(Integer.MAX_VALUE, false);
            return ContainsFuel(fluidStack);
        }
        return false;
    }
    public static int getMlHu(FluidStack fuel)
    {
        for (var s: LiquidBurringInfo.listFuel)
        {
            if(s.fuel.equals(fuel))
                return s.mlHu;
        }
        return -1;
    }
    private static class LiquidAndHu{
        public LiquidAndHu(FluidStack fuel, int mlHu) {
            this.fuel = fuel;
            this.mlHu = mlHu;
        }

        public FluidStack fuel;
        public int mlHu;

    }
}
