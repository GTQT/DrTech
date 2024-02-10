package com.drppp.drtech.Linkage;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import static com.drppp.drtech.Load.DrtechReceipes.LOG_CREATE;

public class GtqtCoreLinkage {
    public static final String GTQTCORE_ID = "gtqtcore";
    public static void MachineRecipeInit()
    {
        LOG_CREATE.recipeBuilder()
                .notConsumable(new ItemStack(Item.getByNameOrId("gtqtcore:pine_sapling"),1))
                .outputs(new ItemStack(Item.getByNameOrId("gtqtcore:pine_log"),1))
                .EUt(30)
                .duration(100)
                .buildAndRegister();
    }
}
