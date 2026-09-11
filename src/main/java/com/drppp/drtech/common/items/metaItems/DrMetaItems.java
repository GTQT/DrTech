package com.drppp.drtech.common.items.metaItems;

import com.meowmel.cropQT.item.MetaCrops;
import com.meowmel.cropQT.item.MetaItemCropTools;
import gregtech.api.items.metaitem.MetaItem;

public final class DrMetaItems {
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_1;
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_2;
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_3;
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_4;
    public static MetaItem<?>.MetaValueItem ENERGY_ELEMENT_5;
    public static MetaItem<?>.MetaValueItem GRAVITY_SHIELD;
    public static MetaItem<?>.MetaValueItem SKULL_DUST;
    public static MetaItem<?>.MetaValueItem PIPIE_1;
    public static MetaItem<?>.MetaValueItem PIPIE_5;
    public static MetaItem<?>.MetaValueItem PIPIE_10;

    public static MetaItem<?>.MetaValueItem GOLD_COIN;
    public static MetaItem<?>.MetaValueItem FLY_RING;
    public static MetaItem<?>.MetaValueItem LIFE_SUPPORT_RING;
    public static MetaItem<?>.MetaValueItem TACTICAL_LASER_SUBMACHINE_GUN;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PLASMA_GUN;
    public static MetaItem<?>.MetaValueItem ADVANCED_TACHINO_DISRUPTOR;
    public static MetaItem<?>.MetaValueItem NUCLEAR_BATTERY_LV;
    public static MetaItem<?>.MetaValueItem NUCLEAR_BATTERY_MV;
    public static MetaItem<?>.MetaValueItem NUCLEAR_BATTERY_HV;
    public static MetaItem<?>.MetaValueItem HAND_PUMP;
    public static MetaItem<?>.MetaValueItem GRASS_KILLER;
    public static MetaItem<?>.MetaValueItem UPGRADE_NULL;

    // 工业蜂箱升级：声明顺序与 GT_ApiaryUpgrade 的枚举顺序一致。
    // 行尾数字是物品元数据，它们并不连续（为兼容已有存档而固定），不要按声明顺序推断。

    // 加速（互斥，只能装一件）
    public static MetaItem<?>.MetaValueItem UPGRADE_SPEED1;             // 29
    public static MetaItem<?>.MetaValueItem UPGRADE_SPEED2;             // 30
    public static MetaItem<?>.MetaValueItem UPGRADE_SPEED3;             // 31
    public static MetaItem<?>.MetaValueItem UPGRADE_SPEED4;             // 32
    public static MetaItem<?>.MetaValueItem UPGRADE_SPEED5;             // 33
    public static MetaItem<?>.MetaValueItem UPGRADE_SPEED6;             // 34
    public static MetaItem<?>.MetaValueItem UPGRADE_SPEED7;             // 35
    public static MetaItem<?>.MetaValueItem UPGRADE_SPEED8;             // 36
    public static MetaItem<?>.MetaValueItem UPGRADE_SPEED8P;            // 37

    // 产量
    public static MetaItem<?>.MetaValueItem UPGRADE_PRODUCTION;         // 38

    // 环境模拟
    public static MetaItem<?>.MetaValueItem UPGRADE_PLAIN;              // 39 平原
    public static MetaItem<?>.MetaValueItem UPGRADE_WINTER_EMULATION;   // 42 冰原
    public static MetaItem<?>.MetaValueItem UPGRADE_HELL_EMULATION;     // 46 地狱
    public static MetaItem<?>.MetaValueItem UPGRADE_DESERT_EMULATION;   // 48 沙漠
    public static MetaItem<?>.MetaValueItem UPGRADE_JUNGLE_EMULATION;   // 53 丛林
    public static MetaItem<?>.MetaValueItem UPGRADE_OCEAN_EMULATION;    // 55 海洋
    public static MetaItem<?>.MetaValueItem UPGRADE_DRYER;              // 43 除湿
    public static MetaItem<?>.MetaValueItem UPGRADE_HUMIDIFIER;         // 45 加湿
    public static MetaItem<?>.MetaValueItem UPGRADE_COOLER;             // 49 降温
    public static MetaItem<?>.MetaValueItem UPGRADE_HEATER;             // 57 升温

    // 行为开关
    public static MetaItem<?>.MetaValueItem UPGRADE_LIGHT;              // 40 内部光照
    public static MetaItem<?>.MetaValueItem UPGRADE_T;                  // 59 内部黑暗
    public static MetaItem<?>.MetaValueItem UPGRADE_OPEN_SKY;           // 56 露天模拟
    public static MetaItem<?>.MetaValueItem UPGRADE_SEAL;               // 51 气密
    public static MetaItem<?>.MetaValueItem UPGRADE_AUTOMATION;         // 44 自动回填

    // 属性强化
    public static MetaItem<?>.MetaValueItem UPGRADE_FLOWERING;          // 41
    public static MetaItem<?>.MetaValueItem UPGRADE_POLLEN_SCRUBBER;    // 47
    public static MetaItem<?>.MetaValueItem UPGRADE_LIFESPAN;           // 50
    public static MetaItem<?>.MetaValueItem UPGRADE_GENETIC_STABILIZER; // 52
    public static MetaItem<?>.MetaValueItem UPGRADE_TERRITORY;          // 54
    public static MetaItem<?>.MetaValueItem UPGRADE_SIEVE;              // 58
    public static MetaItem<?>.MetaValueItem TOOL_BOX;
    public static MetaItem<?>.MetaValueItem MATRIX_GEMS;
    public static MetaItem<?>.MetaValueItem XJC;
    public static MetaItem<?>.MetaValueItem INULIN;
    public static MetaItem<?>.MetaValueItem NATURAL_RUBBER;
    public static MetaItem<?>.MetaValueItem RAPESEED_FLOWER;
    public static MetaItem<?>.MetaValueItem SNAPDRAGON;
    public static MetaItem<?>.MetaValueItem LUCKY_CLOVER;

    //  Fluid Cores
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T1;
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T2;
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T3;
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T4;
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T5;
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T6;
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T7;
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T8;
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T9;
    public static MetaItem<?>.MetaValueItem FLUID_CORE_T10;

    // Fusion reactor parts
    public static MetaItem<?>.MetaValueItem FUSION_FIRST_WALL_PLATE;
    public static MetaItem<?>.MetaValueItem FUSION_MAGNET_COIL;
    public static MetaItem<?>.MetaValueItem FUSION_COOLING_CHANNEL;
    public static MetaItem<?>.MetaValueItem TRITIUM_BREEDING_CELL;
    public static MetaItem<?>.MetaValueItem NEUTRON_CAPTURE_CORE;
    public static MetaItem<?>.MetaValueItem MAGNETIC_FIELD_STORAGE_CELL;
    public static MetaItem<?>.MetaValueItem DT_FUEL_INJECTOR;
    public static MetaItem<?>.MetaValueItem RF_GENERATOR_CORE;
    public static MetaItem<?>.MetaValueItem RF_WAVEGUIDE;
    public static MetaItem<?>.MetaValueItem RF_PHASE_SYNCHRONIZER;
    public static MetaItem<?>.MetaValueItem CERAMIC_RF_WINDOW;
    public static MetaItem<?>.MetaValueItem LOW_VOLTAGE_WIRE;
    public static MetaItem<?>.MetaValueItem MEDIUM_VOLTAGE_WIRE;
    public static MetaItem<?>.MetaValueItem HIGH_VOLTAGE_WIRE;

    // Loot Table
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_STONE_AGE;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_STEAM_AGE;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_LV;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_MV;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_HV;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_EV;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_IV;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_LUV;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_ZPM;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_UV;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_UHV;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_AE1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_AE2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_BEE1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_BEE2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_BEE3;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_BM1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_BM2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_BM3;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_COMPUTER1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_CROPS;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_FLOPPIES;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_FOOD1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_FOOD2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_FOOD3;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_FOOD4;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_FOREST1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_FOREST2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_FOREST3;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_GARDENS;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_HEE1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_HEE2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_LEGENDARY;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_MAGIC1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_MAGIC2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_MAGIC3;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_MAGIC4;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_MAGIC5;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_RAIL1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_RAIL2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_RAIL3;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_SEEDS;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_SPACE1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_SPACE2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_SPACE3;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_WITCH1;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_WITCH2;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_WITCH3;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_WITCH4;
    public static MetaItem<?>.MetaValueItem LOOT_TABLE_WITCH5;

    public static MetaItem<?>.MetaValueItem ARGENTIA_LEAF;
    public static MetaItem<?>.MetaValueItem AURONIA_LEAF;
    public static MetaItem<?>.MetaValueItem BAUXIA_LEAF;
    public static MetaItem<?>.MetaValueItem BOBS_YER_UNCLE_BERRY;
    public static MetaItem<?>.MetaValueItem CANOLA_FLOWER;
    public static MetaItem<?>.MetaValueItem COPPON_FIBER;
    public static MetaItem<?>.MetaValueItem FERROFERN_LEAF;
    public static MetaItem<?>.MetaValueItem GALVANIA_LEAF;
    public static MetaItem<?>.MetaValueItem HEMP_STEM;
    public static MetaItem<?>.MetaValueItem HOPS;
    public static MetaItem<?>.MetaValueItem INDIGO_BLOSSOM;
    public static MetaItem<?>.MetaValueItem IRIDINE_FLOWER;
    public static MetaItem<?>.MetaValueItem MAGIC_ESSENCE;
    public static MetaItem<?>.MetaValueItem MICADIA_FLOWER;
    public static MetaItem<?>.MetaValueItem MILK_WART;
    public static MetaItem<?>.MetaValueItem NICKELBACK_LEAF;
    public static MetaItem<?>.MetaValueItem OIL_BERRY;
    public static MetaItem<?>.MetaValueItem OSMIANTH_FLOWER;
    public static MetaItem<?>.MetaValueItem PLATINA_LEAF;
    public static MetaItem<?>.MetaValueItem PLUMBILIA_LEAF;
    public static MetaItem<?>.MetaValueItem PYROLUSIUM_LEAF_0;
    public static MetaItem<?>.MetaValueItem PYROLUSIUM_LEAF_1;
    public static MetaItem<?>.MetaValueItem PYROLUSIUM_LEAF_2;
    public static MetaItem<?>.MetaValueItem PYROLUSIUM_LEAF_3;
    public static MetaItem<?>.MetaValueItem PYROLUSIUM_LEAF_BANANA;
    public static MetaItem<?>.MetaValueItem PYROLUSIUM_LEAF_CANADA;
    public static MetaItem<?>.MetaValueItem PYROLUSIUM_LEAF_NO_EGG;
    public static MetaItem<?>.MetaValueItem REACTORIA_LEAF;
    public static MetaItem<?>.MetaValueItem REACTORIA_STEM;
    public static MetaItem<?>.MetaValueItem SALTY_ROOT;
    public static MetaItem<?>.MetaValueItem SCHEELINIUM_LEAF;
    public static MetaItem<?>.MetaValueItem SPACE_FLOWER;
    public static MetaItem<?>.MetaValueItem STAR_WART;
    public static MetaItem<?>.MetaValueItem STARGATIUM_LEAF;
    public static MetaItem<?>.MetaValueItem THIOSULFINE_FLOWER;
    public static MetaItem<?>.MetaValueItem THUNDER_FLOWER;
    public static MetaItem<?>.MetaValueItem TINE_TWIG;
    public static MetaItem<?>.MetaValueItem TITANIA_LEAF;
    public static MetaItem<?>.MetaValueItem UUA_BERRY;
    public static MetaItem<?>.MetaValueItem UUM_BERRY;

    public static void MetaItemsInit() {
        MetaItems1 metaItem = new MetaItems1();
        metaItem.setRegistryName("meta_items");

        MetaCrops metaItemCrops = new MetaCrops();
        metaItemCrops.setRegistryName("meta_crops");

        MetaItemLootTable metaItemLootTable = new MetaItemLootTable();
        metaItemLootTable.setRegistryName("loot_table");

        MetaItemCropTools cropTools = new MetaItemCropTools();
        cropTools.setRegistryName("crop_tools");
    }
}
