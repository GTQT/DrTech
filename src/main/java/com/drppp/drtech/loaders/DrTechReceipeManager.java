package com.drppp.drtech.loaders;

import com.drppp.drtech.loaders.chain.FluidStoreRecpie;
import com.drppp.drtech.loaders.chain.MobsDropsRecipe;
import com.drppp.drtech.loaders.chain.NuclearRecipe;

public class DrTechReceipeManager {
    public static void init()
    {
        MachineReceipe.load();
        MobsDropsRecipe.load();
        FluidStoreRecpie.init();
        NuclearRecipe.load();
    }
}
