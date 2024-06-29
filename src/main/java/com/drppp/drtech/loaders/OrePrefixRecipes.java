package com.drppp.drtech.loaders;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

import static com.drppp.drtech.loaders.DrtechReceipes.EIMPLOSION_RECIPES;
import static gregtech.api.unification.material.info.MaterialFlags.EXPLOSIVE;
import static gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class OrePrefixRecipes {
    public static void init()
    {
        OrePrefix.dust.addProcessingHandler(PropertyKey.DUST, OrePrefixRecipes::processDust);
    }

    public static void processDust(OrePrefix dustPrefix, Material mat, DustProperty property)
    {
        ItemStack dustStack = OreDictUnifier.get(dustPrefix, mat);
        if (!mat.hasFlag(EXPLOSIVE) && !mat.hasFlag(FLAMMABLE))
        {
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, mat);
            EIMPLOSION_RECIPES.recipeBuilder()
                    .inputs(GTUtility.copy(3, dustStack))
                    .outputs(GTUtility.copy(3, gemStack))
                    .chancedOutput(dust, Materials.DarkAsh, 2500, 0)
                    .explosivesAmount(dustStack)
                    .buildAndRegister();
        }
    }
}
