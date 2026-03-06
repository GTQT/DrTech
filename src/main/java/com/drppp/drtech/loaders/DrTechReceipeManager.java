package com.drppp.drtech.loaders;

import com.drppp.drtech.api.Utils.Mods;
import com.drppp.drtech.loaders.recipes.chain.FluidStoreRecpie;
import com.drppp.drtech.loaders.recipes.chain.LaserBending;
import com.drppp.drtech.loaders.recipes.chain.MobsDropsRecipe;
import com.drppp.drtech.loaders.recipes.MachineReceipe;
import com.drppp.drtech.loaders.recipes.misc.BinneRecipes;

public class DrTechReceipeManager {
    public static void init()
    {
        MachineReceipe.load();
        MobsDropsRecipe.load();
        FluidStoreRecpie.init();
        LaserBending.init();
        if (Mods.Genetics.isModLoaded())
            BinneRecipes.init();
    }
}
