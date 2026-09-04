package com.drppp.drtech.loaders;

import com.drppp.drtech.api.utils.Mods;
import com.drppp.drtech.loaders.recipes.chain.FluidStoreRecpie;
import com.drppp.drtech.loaders.recipes.chain.ChorusFruitChainRecipes;
import com.drppp.drtech.loaders.recipes.chain.LaserBending;
import com.drppp.drtech.loaders.recipes.MachineRecipes;
import com.drppp.drtech.loaders.recipes.LightsaberRecipes;
import com.drppp.drtech.loaders.recipes.misc.BinneRecipes;

public class DrTechReceipeManager {
    public static void init()
    {
        MachineRecipes.load();
        LightsaberRecipes.init();
        FluidStoreRecpie.init();
        ChorusFruitChainRecipes.init();
        LaserBending.init();
        if (Mods.Genetics.isModLoaded())
            BinneRecipes.init();
    }
}
