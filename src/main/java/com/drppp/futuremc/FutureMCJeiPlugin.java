package com.drppp.futuremc;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.IModPlugin;
import net.minecraft.item.ItemStack;

/** Ensures Future MC ingredients are explicitly indexed by JEI. */
@JEIPlugin
public final class FutureMCJeiPlugin implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        for (net.minecraft.block.Block block : new net.minecraft.block.Block[] {
                FutureMCMain.DRIED_GHAST}) {
            registry.addIngredientInfo(new ItemStack(block), mezz.jei.api.ingredients.VanillaTypes.ITEM,
                    "Future MC block");
        }
        registry.addIngredientInfo(new ItemStack(FutureMCMain.HAPPY_GHAST_HARNESS),
                mezz.jei.api.ingredients.VanillaTypes.ITEM, "Future MC item");
    }
}
