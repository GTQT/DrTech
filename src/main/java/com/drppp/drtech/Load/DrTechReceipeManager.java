package com.drppp.drtech.Load;

import com.drppp.drtech.Load.chain.MobsDropsRecipe;

public class DrTechReceipeManager {
    public static void init()
    {
        MachineReceipe.load();
        CraftingReceipe.load();
        MobsDropsRecipe.load();
    }
}
