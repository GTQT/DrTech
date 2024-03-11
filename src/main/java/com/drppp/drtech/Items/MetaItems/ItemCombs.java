package com.drppp.drtech.Items.MetaItems;

import com.drppp.drtech.Linkage.Forestry.DrtCombItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ItemCombs {
    public static  DrtCombItem ITEM_COMBS;
    public static void init()
    {
        if(Loader.isModLoaded("forestry"))
        {
            ITEM_COMBS =  new DrtCombItem();
        }
    }
}
