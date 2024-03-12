package com.drppp.drtech.Items.MetaItems;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;

public class ItemCombs {
    public static  Item ITEM_COMBS;
    public static void init()
    {
        if(Loader.isModLoaded("forestry"))
        {
            ITEM_COMBS =  new com.drppp.drtech.Linkage.Forestry.DrtCombItem();
        }
    }
}
