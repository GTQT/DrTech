package com.drppp.drtech.common.Items.MetaItems;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;

public class ItemCombs {
    public static  Item ITEM_COMBS;
    public static void init()
    {
        if(Loader.isModLoaded("forestry"))
        {
            ITEM_COMBS =  new com.drppp.drtech.intergations.Forestry.DrtCombItem();
        }
    }
}
