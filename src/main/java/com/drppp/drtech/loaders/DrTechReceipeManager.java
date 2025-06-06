package com.drppp.drtech.loaders;

import com.drppp.drtech.api.Utils.Mods;
import com.drppp.drtech.intergations.HarvestcraftFishChain;
import com.drppp.drtech.intergations.HarvestcraftLinkage;
import com.drppp.drtech.loaders.chain.*;
import com.drppp.drtech.loaders.misc.BinneRecipes;
import net.minecraftforge.fml.common.Loader;

public class DrTechReceipeManager {
    public static void init()
    {
        MachineReceipe.load();
        MobsDropsRecipe.load();
        FluidStoreRecpie.init();
        LaserBending.init();
        if (Mods.Genetics.isModLoaded()) BinneRecipes.init();
        NuclearRecipe.load();
        if (Loader.isModLoaded(HarvestcraftLinkage.CRAFT_ID)) {
            HarvestcraftFishChain.MachineRecipeInit();
        }
    }
}
