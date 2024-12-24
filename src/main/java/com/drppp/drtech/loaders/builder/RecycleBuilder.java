package com.drppp.drtech.loaders.builder;

import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import com.drppp.drtech.loaders.DrtechReceipes;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RecycleBuilder {
    public static void initRecycleRecipe(){
        for (Item item : ForgeRegistries.ITEMS) {
            DrtechReceipes.RECYCLE_RECIPE.recipeBuilder()
                    .input(item,1)
                    .chancedOutput(MyMetaItems.SCRAP,500,1000)
                    .EUt(30)
                    .duration(100)
                    .buildAndRegister();
        }
    }
}
