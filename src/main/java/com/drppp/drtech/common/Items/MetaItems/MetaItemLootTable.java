package com.drppp.drtech.common.Items.MetaItems;

import com.drppp.drtech.common.Items.MetaItems.behaviors.LootTableBehavior;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.drppp.drtech.common.Items.MetaItems.DrMetaItems.*;

public class MetaItemLootTable extends StandardMetaItem {

    /** name → ItemStack 映射，供 JEI 等模块查找奖励袋物品 */
    public static final Map<String, ItemStack> LOOT_BAG_ITEMS = new HashMap<>();

    public MetaItemLootTable() {
    }

    public static CreativeTabs LootTab = new CreativeTabs("loot_table") {
        @Override
        public @NotNull ItemStack createIcon() {
            return LOOT_TABLE_TEST.getStackForm();
        }
    };

    private int metaId = 0;

    private MetaItem<?>.MetaValueItem register(String name) {
        MetaItem<?>.MetaValueItem item = this.addItem(metaId++, "loot_table." + name)
                .addComponents(new LootTableBehavior(name))
                .setCreativeTabs(LootTab);
        LOOT_BAG_ITEMS.put(name, item.getStackForm());
        return item;
    }

    public void registerSubItems() {
        LOOT_TABLE_TEST = register("test");
        LOOT_TABLE_STONE_AGE = register("stone_age");
        LOOT_TABLE_STEAM_AGE = register("steam_age");
        LOOT_TABLE_LV = register("lv");
        LOOT_TABLE_MV = register("mv");
        LOOT_TABLE_HV = register("hv");
        LOOT_TABLE_EV = register("ev");
        LOOT_TABLE_IV = register("iv");
        LOOT_TABLE_LUV = register("luv");
        LOOT_TABLE_ZPM = register("zpm");
        LOOT_TABLE_UV = register("uv");
        LOOT_TABLE_UHV = register("uhv");
        LOOT_TABLE_AE1 = register("ae1");
        LOOT_TABLE_AE2 = register("ae2");
        LOOT_TABLE_BEE1 = register("bee1");
        LOOT_TABLE_BEE2 = register("bee2");
        LOOT_TABLE_BEE3 = register("bee3");
        LOOT_TABLE_BM1 = register("bm1");
        LOOT_TABLE_BM2 = register("bm2");
        LOOT_TABLE_BM3 = register("bm3");
        LOOT_TABLE_COMPUTER1 = register("computer1");
        LOOT_TABLE_CROPS = register("crops");
        LOOT_TABLE_FLOPPIES = register("floppies");
        LOOT_TABLE_FOOD1 = register("food1");
        LOOT_TABLE_FOOD2 = register("food2");
        LOOT_TABLE_FOOD3 = register("food3");
        LOOT_TABLE_FOOD4 = register("food4");
        LOOT_TABLE_FOREST1 = register("forest1");
        LOOT_TABLE_FOREST2 = register("forest2");
        LOOT_TABLE_FOREST3 = register("forest3");
        LOOT_TABLE_GARDENS = register("gardens");
        LOOT_TABLE_HEE1 = register("hee1");
        LOOT_TABLE_HEE2 = register("hee2");
        LOOT_TABLE_LEGENDARY = register("legendary");
        LOOT_TABLE_MAGIC1 = register("magic1");
        LOOT_TABLE_MAGIC2 = register("magic2");
        LOOT_TABLE_MAGIC3 = register("magic3");
        LOOT_TABLE_MAGIC4 = register("magic4");
        LOOT_TABLE_MAGIC5 = register("magic5");
        LOOT_TABLE_RAIL1 = register("rail1");
        LOOT_TABLE_RAIL2 = register("rail2");
        LOOT_TABLE_RAIL3 = register("rail3");
        LOOT_TABLE_SEEDS = register("seeds");
        LOOT_TABLE_SPACE1 = register("space1");
        LOOT_TABLE_SPACE2 = register("space2");
        LOOT_TABLE_SPACE3 = register("space3");
        LOOT_TABLE_WITCH1 = register("witch1");
        LOOT_TABLE_WITCH2 = register("witch2");
        LOOT_TABLE_WITCH3 = register("witch3");
        LOOT_TABLE_WITCH4 = register("witch4");
        LOOT_TABLE_WITCH5 = register("witch5");
    }
}
