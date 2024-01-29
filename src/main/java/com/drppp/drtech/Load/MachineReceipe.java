package com.drppp.drtech.Load;

import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItem1;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.MACERATOR_RECIPES;
import static gregtech.api.recipes.RecipeMaps.MIXER_RECIPES;
import static gregtech.api.unification.material.Materials.NetherStar;

public class MachineReceipe {
    public static void load()
    {
        MACERATOR_RECIPES.recipeBuilder()
                .input(Items.SKULL,1,1)
                .output(MyMetaItems.SKULL_DUST,2)
                .duration(200)
                .EUt(1920)
                .buildAndRegister();
        MIXER_RECIPES.recipeBuilder()
                .input(MyMetaItems.SKULL_DUST,1)
                .input(Blocks.SOUL_SAND,2)
                .output(OrePrefix.dust,NetherStar,1)
                .duration(300)
                .EUt(1920)
                .buildAndRegister();
    }
}
