package com.drppp.drtech.Items.MetaItems;

import gregtech.api.items.metaitem.MetaItem;

public final class MyMetaItems {
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_1;
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_2;
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_3;
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_4;
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_5;
    public static MetaItem<?>.MetaValueItem GRAVITY_SHIELD;
    public static MetaItem<?>.MetaValueItem SKULL_DUST;
    public static MetaItem<?>.MetaValueItem REDSTONE_SEED;
    public static void MetaItemsInit()
    {
       MetaItems1 metaItem = new MetaItems1();
        metaItem.setRegistryName("meta_items_mymod");
    }
}
