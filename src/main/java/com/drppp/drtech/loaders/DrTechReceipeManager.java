package com.drppp.drtech.loaders;

import com.drppp.drtech.api.Utils.Mods;
import com.drppp.drtech.loaders.chain.FluidStoreRecpie;
import com.drppp.drtech.loaders.chain.MobsDropsRecipe;
import com.drppp.drtech.loaders.chain.NuclearRecipe;
import com.drppp.drtech.loaders.chain.UURecipes;
import com.drppp.drtech.loaders.misc.BinneRecipes;

public class DrTechReceipeManager {
    public static void init()
    {
        MachineReceipe.load();
        MobsDropsRecipe.load();
        FluidStoreRecpie.init();
        UURecipes.init();
        if (Mods.Genetics.isModLoaded()) BinneRecipes.init();
        NuclearRecipe.load();
    }
}
