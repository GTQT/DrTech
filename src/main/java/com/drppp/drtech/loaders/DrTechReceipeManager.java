package com.drppp.drtech.loaders;

import com.drppp.drtech.api.Utils.Mods;
import com.drppp.drtech.loaders.chain.*;
import com.drppp.drtech.loaders.misc.BinneRecipes;

public class DrTechReceipeManager {
    public static void init()
    {
        MachineReceipe.load();
        MobsDropsRecipe.load();
        FluidStoreRecpie.init();
        UURecipes.init();
        LaserBending.init();
        if (Mods.Genetics.isModLoaded()) BinneRecipes.init();
        NuclearRecipe.load();
    }
}
