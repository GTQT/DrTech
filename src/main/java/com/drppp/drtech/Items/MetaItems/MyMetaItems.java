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
    public static MetaItem<?>.MetaValueItem SCRAP;
    public static MetaItem<?>.MetaValueItem CD_ROM;
    public static MetaItem<?>.MetaValueItem UU_MATER;
    public static MetaItem<?>.MetaValueItem PIPIE_1;
    public static MetaItem<?>.MetaValueItem PIPIE_5;
    public static MetaItem<?>.MetaValueItem PIPIE_10;
    public static MetaItem<?>.MetaValueItem POS_CARD;
    public static MetaItem<?>.MetaValueItem GOLD_COIN;
    public static MetaItem<?>.MetaValueItem FLY_RING;
    public static MetaItem<?>.MetaValueItem LIFE_SUPPORT_RING;
    public static MetaItem<?>.MetaValueItem TACTICAL_LASER_SUBMACHINE_GUN ;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PLASMA_GUN ;
    public static MetaItem<?>.MetaValueItem ADVANCED_TACHINO_DISRUPTOR ;
    public static MetaItem<?>.MetaValueItem NULL_FUEL_ROD ;
    public static MetaItem<?>.MetaValueItem NUCLEAR_BATTERY_LV ;
    public static MetaItem<?>.MetaValueItem NUCLEAR_BATTERY_MV ;
    public static MetaItem<?>.MetaValueItem NUCLEAR_BATTERY_HV ;

    public static void MetaItemsInit()
    {
       MetaItems1 metaItem = new MetaItems1();
        metaItem.setRegistryName("meta_items_mymod");
    }
}
