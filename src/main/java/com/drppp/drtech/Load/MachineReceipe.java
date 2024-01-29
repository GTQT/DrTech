package com.drppp.drtech.Load;

import com.drppp.drtech.Items.ItemsInit;
import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItem1;
import gregtech.common.items.MetaItems;
import gregtech.loaders.recipe.handlers.MaterialRecipeHandler;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import  com.drppp.drtech.Blocks.BlocksInit;

import static com.drppp.drtech.Load.DrtechReceipes.EIMPLOSION_RECIPES;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.NetherStar;
import static gregtech.api.unification.material.info.MaterialFlags.EXPLOSIVE;
import static gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE;
import static gregtech.api.unification.ore.OrePrefix.dust;

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
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(MetaBlocks.TRANSPARENT_CASING,1,2))
                .input(MetaBlocks.TRANSPARENT_CASING)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(144))
                .output(BlocksInit.MY_LASER_PIPE)
                .duration(100)
                .EUt(7680)
                .buildAndRegister();
        OrePrefix.dust.addProcessingHandler(PropertyKey.DUST, MachineReceipe::processDust);
    }
    public static void processDust(OrePrefix dustPrefix, Material mat, DustProperty property)
    {
        ItemStack dustStack = OreDictUnifier.get(dustPrefix, mat);
        if (mat.hasProperty(PropertyKey.GEM))
        {
            ItemStack gemStack = OreDictUnifier.get(OrePrefix.gem, mat);
            if (!mat.hasFlag(EXPLOSIVE) && !mat.hasFlag(FLAMMABLE)) {
               EIMPLOSION_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copy(3, dustStack))
                        .outputs(GTUtility.copy(3, gemStack))
                        .chancedOutput(dust, Materials.DarkAsh, 2500, 0)
                       .explosivesAmount(dustStack)
                        .buildAndRegister();

                EIMPLOSION_RECIPES.recipeBuilder()
                        .inputs(GTUtility.copy(3, dustStack))
                        .outputs(GTUtility.copy(3, gemStack))
                        .chancedOutput(dust, Materials.DarkAsh, 2500, 0)
                        .explosivesAmount(MetaItems.DYNAMITE.getStackForm())
                        .buildAndRegister();
            }
        }
    }
}
