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
                FutureMCMain.SMOOTH_BASALT, FutureMCMain.CALCITE, FutureMCMain.AMETHYST_BLOCK,
                FutureMCMain.BUDDING_AMETHYST, FutureMCMain.SMALL_AMETHYST_BUD,
                FutureMCMain.MEDIUM_AMETHYST_BUD, FutureMCMain.LARGE_AMETHYST_BUD,
                FutureMCMain.AMETHYST_CLUSTER, FutureMCMain.LANTERN, FutureMCMain.SOUL_LANTERN,
                FutureMCMain.CHAIN, FutureMCMain.COMPOSTER, FutureMCMain.DRIED_GHAST}) {
            registry.addIngredientInfo(new ItemStack(block), mezz.jei.api.ingredients.VanillaTypes.ITEM,
                    "Future MC block");
        }
        registry.addIngredientInfo(new ItemStack(FutureMCMain.AMETHYST_SHARD),
                mezz.jei.api.ingredients.VanillaTypes.ITEM, "Future MC item");
        registry.addIngredientInfo(new ItemStack(FutureMCMain.HAPPY_GHAST_HARNESS),
                mezz.jei.api.ingredients.VanillaTypes.ITEM, "Future MC item");
    }
}
