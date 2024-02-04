package com.drppp.drtech.Load;

import com.drppp.drtech.Items.ItemsInit;
import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.recipes.recipeproperties.ScanProperty;
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
import net.minecraft.nbt.NBTTagCompound;

import static com.drppp.drtech.Load.DrtechReceipes.EIMPLOSION_RECIPES;
import static com.drppp.drtech.Load.DrtechReceipes.UU_RECIPES;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.Carbon;
import static gregtech.api.unification.material.Materials.NetherStar;
import static gregtech.api.unification.material.info.MaterialFlags.EXPLOSIVE;
import static gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.ingot;
import static gregtech.api.util.AssemblyLineManager.writeResearchToNBT;

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
        EIMPLOSION_RECIPES.recipeBuilder()
                .input(OrePrefix.dust,Carbon,63)
                .output(Items.DIAMOND)
                .explosivesAmount(OreDictUnifier.get(OrePrefix.dust, Carbon))
                .buildAndRegister();
        OrePrefix.dust.addProcessingHandler(PropertyKey.DUST, MachineReceipe::processDust);
        //160秒
        UU_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Air.getFluid(10))
                .fluidOutputs(Materials.UUMatter.getFluid(1))
                .EUt(256)
                .duration(3200)
                .buildAndRegister();
        UU_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Air.getFluid(1))
                .input(MyMetaItems.SCRAP)
                .fluidOutputs(Materials.UUMatter.getFluid(1))
                .EUt(256)
                .duration(1600)
                .buildAndRegister();
        ItemStack is = MetaItems.TOOL_DATA_STICK.getStackForm();
        NBTTagCompound compound = GTUtility.getOrCreateNbtCompound(MetaItems.ELECTRIC_MOTOR_LV.getStackForm());
        writeResearchToNBT(compound, "1xmetaitem.electric.motor.lv@127");
        is.setTagCompound(compound);
        RecipeBuilder<?> builder= SCANNER_RECIPES.recipeBuilder()
                 .inputNBT(is.getItem(), 1,is.getMetadata(),NBTMatcher.ANY, NBTCondition.ANY)
                 .input(MetaItems.ELECTRIC_MOTOR_LV)
                 .outputs(is)
                 .duration(100)
                 .EUt(2);
        builder.applyProperty(ScanProperty.getInstance(), true);
        builder.buildAndRegister();

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
            }
        }
    }
}
