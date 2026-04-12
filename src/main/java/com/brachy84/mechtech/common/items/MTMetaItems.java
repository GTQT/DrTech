package com.brachy84.mechtech.common.items;

import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.Material;

import java.util.HashMap;
import java.util.Map;

public class MTMetaItems {

    public static MetaItem<?>.MetaValueItem WIRELESS_RECEIVER;
    public static MetaItem<?>.MetaValueItem WIRELESS_BINDER;

    // Modules
    public static MetaItem<?>.MetaValueItem SHOCK_ABSORBER;
    public static MetaItem<?>.MetaValueItem THICK_INSULATOR;
    public static MetaItem<?>.MetaValueItem BINOCULARS;
    public static MetaItem<?>.MetaValueItem AUTO_FEEDER;
    public static MetaItem<?>.MetaValueItem TESLA_COIL;
    public static MetaItem<?>.MetaValueItem OXYGEN_MASK;
    public static MetaItem<?>.MetaValueItem ANTI_GRAVITY;
    public static MetaItem<?>.MetaValueItem SPRINT_MODULE;
    public static MetaItem<?>.MetaValueItem ENERGY_SHIELD;
    public static MetaItem<?>.MetaValueItem HEALING_MODULE;
    public static MetaItem<?>.MetaValueItem APIARIST_SHIELD;
    public static MetaItem<?>.MetaValueItem REVEALING_GOGGLES;
    public static MetaItem<?>.MetaValueItem VIS_OPTIMIZER;
    // Armor
    public static ArmorMetaItem<?>.ArmorMetaValueItem MODULAR_HELMET;
    public static ArmorMetaItem<?>.ArmorMetaValueItem MODULAR_CHESTPLATE;
    public static ArmorMetaItem<?>.ArmorMetaValueItem MODULAR_LEGGINGS;
    public static ArmorMetaItem<?>.ArmorMetaValueItem MODULAR_BOOTS;

    public static final Map<Material, MetaItem<?>.MetaValueItem> MATERIAL_ARMOR_PLATINGS = new HashMap<>();

    public static void init() {
        MTMetaItem item = new MTMetaItem();
        MTArmorItem armorItem = new MTArmorItem();
        item.setRegistryName("meta_item");
        armorItem.setRegistryName("meta_armor");
    }
}
