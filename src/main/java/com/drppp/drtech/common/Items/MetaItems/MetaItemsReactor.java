package com.drppp.drtech.common.Items.MetaItems;

import gregtech.api.items.metaitem.MetaItem;

public class MetaItemsReactor {
    public static MetaItem<?>.MetaValueItem U_FUEL_ROD_1X ;
    public static MetaItem<?>.MetaValueItem U_FUEL_ROD_2X ;
    public static MetaItem<?>.MetaValueItem U_FUEL_ROD_4X ;
    public static MetaItem<?>.MetaValueItem U_FUEL_ROD_1X_EX ;
    public static MetaItem<?>.MetaValueItem U_FUEL_ROD_2X_EX ;
    public static MetaItem<?>.MetaValueItem U_FUEL_ROD_4X_EX ;
    public static MetaItem<?>.MetaValueItem Th_FUEL_ROD_1X ;
    public static MetaItem<?>.MetaValueItem Th_FUEL_ROD_2X ;
    public static MetaItem<?>.MetaValueItem Th_FUEL_ROD_4X ;
    public static MetaItem<?>.MetaValueItem Th_FUEL_ROD_1X_EX ;
    public static MetaItem<?>.MetaValueItem Th_FUEL_ROD_2X_EX ;
    public static MetaItem<?>.MetaValueItem Th_FUEL_ROD_4X_EX ;
    public static MetaItem<?>.MetaValueItem U_MOX_FUEL_ROD_1X ;
    public static MetaItem<?>.MetaValueItem U_MOX_FUEL_ROD_2X ;
    public static MetaItem<?>.MetaValueItem U_MOX_FUEL_ROD_4X ;
    public static MetaItem<?>.MetaValueItem U_MOX_FUEL_ROD_1X_EX ;
    public static MetaItem<?>.MetaValueItem U_MOX_FUEL_ROD_2X_EX ;
    public static MetaItem<?>.MetaValueItem U_MOX_FUEL_ROD_4X_EX ;
    public static MetaItem<?>.MetaValueItem HEAT_VENT;
    public static MetaItem<?>.MetaValueItem ADVANCED_HEAT_VENT;
    public static MetaItem<?>.MetaValueItem REACTOR_HEAT_VENT;
    public static MetaItem<?>.MetaValueItem COMPONENT_HEAT_VENT;
    public static MetaItem<?>.MetaValueItem OVERCLOCKED_HEAT_VENT;
    public static MetaItem<?>.MetaValueItem HEAT_EXCHANGER;
    public static MetaItem<?>.MetaValueItem ADVANCED_HEAT_EXCHANGER;
    public static MetaItem<?>.MetaValueItem COMPONENT_HEAT_EXCHANGER;
    public static MetaItem<?>.MetaValueItem REACTOR_HEAT_EXCHANGER;
    public static MetaItem<?>.MetaValueItem NEUTRON_REFLECTOR1;
    public static MetaItem<?>.MetaValueItem THICK_NEUTRON_REFLECTOR;
    public static MetaItem<?>.MetaValueItem IRIDIUM_NEUTRON_REFLECTOR;
    public static MetaItem<?>.MetaValueItem HE_COOLANT_CELL_60K;
    public static MetaItem<?>.MetaValueItem HE_COOLANT_CELL_180K;
    public static MetaItem<?>.MetaValueItem HE_COOLANT_CELL_360K;
    public static MetaItem<?>.MetaValueItem NAK_COOLANT_CELL_60K;
    public static MetaItem<?>.MetaValueItem NAK_COOLANT_CELL_180K;
    public static MetaItem<?>.MetaValueItem NAK_COOLANT_CELL_360K;
    public static MetaItem<?>.MetaValueItem ADVANCED_COMPONENT_HEAT_VENT;
    public static MetaItem<?>.MetaValueItem COOLANT_NULL_CELL_1;
    public static MetaItem<?>.MetaValueItem COOLANT_NULL_CELL_2;
    public static MetaItem<?>.MetaValueItem COOLANT_NULL_CELL_3;
    public static void FuelRodInit()
    {
        MetaItemsReactorReg metaItemFuelRod = new MetaItemsReactorReg();
        metaItemFuelRod.setRegistryName("meta_items_fuel_rod");
    }
}
