package com.drppp.drtech.intergations.jei;

import com.drppp.drtech.api.Utils.CustomeRecipe;
import gregicality.multiblocks.common.metatileentities.GCYMMetaTileEntities;
import gregtech.api.GTValues;
import gregtech.common.metatileentities.MetaTileEntities;
import keqing.gtqtcore.common.metatileentities.GTQTMetaTileEntities;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.*;


public class CanDoWorkMachineJei implements IRecipeWrapper {

    private final List<List<ItemStack>> groupedInputsAsItemStacks = new ArrayList<>();

    public CanDoWorkMachineJei()
    {
        for (var item : CustomeRecipe.CAN_DO_WORK_MACHINES)
        {
            List<ItemStack> list = new ArrayList<>();
            list.add(item);
            groupedInputsAsItemStacks.add(list);
        }
    }
    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, groupedInputsAsItemStacks);
        ingredients.setOutputLists(VanillaTypes.ITEM, groupedInputsAsItemStacks);
    }
    public int getInputCount() {
        return groupedInputsAsItemStacks.size();
    }
}
