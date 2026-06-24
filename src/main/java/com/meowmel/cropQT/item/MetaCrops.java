package com.meowmel.cropQT.item;

import gregtech.api.items.metaitem.StandardMetaItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.drppp.drtech.common.Items.MetaItems.DrMetaItems.*;

public class MetaCrops extends StandardMetaItem {
    public static CreativeTabs LootCropTab = new CreativeTabs("loot_crop_tab") {
        @Override
        public @NotNull ItemStack createIcon() {
            return ARGENTIA_LEAF.getStackForm();
        }
    };

    public MetaCrops() {
        this.setCreativeTabs(LootCropTab);
    }

    public void registerSubItems() {
        ARGENTIA_LEAF = this.addItem(0, "argentia_leaf");
        AURONIA_LEAF = this.addItem(1, "auronia_leaf");
        BAUXIA_LEAF = this.addItem(2, "bauxia_leaf");
        BOBS_YER_UNCLE_BERRY = this.addItem(3, "bobs_yer_uncle_berry");
        CANOLA_FLOWER = this.addItem(4, "canola_flower");
        COPPON_FIBER = this.addItem(5, "coppon_fiber");
        FERROFERN_LEAF = this.addItem(6, "ferrofern_leaf");
        GALVANIA_LEAF = this.addItem(7, "galvania_leaf");
        HEMP_STEM = this.addItem(8, "hemp_stem");
        HOPS = this.addItem(9, "hops");
        INDIGO_BLOSSOM = this.addItem(10, "indigo_blossom");
        IRIDINE_FLOWER = this.addItem(11, "iridine_flower");
        MAGIC_ESSENCE = this.addItem(12, "magic_essence");
        MICADIA_FLOWER = this.addItem(13, "micadia_flower");
        MILK_WART = this.addItem(14, "milk_wart");
        NICKELBACK_LEAF = this.addItem(15, "nickelback_leaf");
        OIL_BERRY = this.addItem(16, "oil_berry");
        OSMIANTH_FLOWER = this.addItem(17, "osmianth_flower");
        PLATINA_LEAF = this.addItem(18, "platina_leaf");
        PLUMBILIA_LEAF = this.addItem(19, "plumbilia_leaf");
        PYROLUSIUM_LEAF_0 = this.addItem(20, "pyrolusium_leaf.0");
        PYROLUSIUM_LEAF_1 = this.addItem(21, "pyrolusium_leaf.1");
        PYROLUSIUM_LEAF_2 = this.addItem(22, "pyrolusium_leaf.2");
        PYROLUSIUM_LEAF_3 = this.addItem(23, "pyrolusium_leaf.3");
        PYROLUSIUM_LEAF_BANANA = this.addItem(24, "pyrolusium_leaf.banana");
        PYROLUSIUM_LEAF_CANADA = this.addItem(25, "pyrolusium_leaf.canada");
        PYROLUSIUM_LEAF_NO_EGG = this.addItem(26, "pyrolusium_leaf.no_egg");
        REACTORIA_LEAF = this.addItem(27, "reactoria_leaf");
        REACTORIA_STEM = this.addItem(28, "reactoria_stem");
        SALTY_ROOT = this.addItem(29, "salty_root");
        SCHEELINIUM_LEAF = this.addItem(30, "scheelinium_leaf");
        SPACE_FLOWER = this.addItem(31, "space_flower");
        STAR_WART = this.addItem(32, "star_wart");
        STARGATIUM_LEAF = this.addItem(33, "stargatium_leaf");
        THIOSULFINE_FLOWER = this.addItem(34, "thiosulfine_flower");
        THUNDER_FLOWER = this.addItem(35, "thunder_flower");
        TINE_TWIG = this.addItem(36, "tine_twig");
        TITANIA_LEAF = this.addItem(37, "titania_leaf");
        UUA_BERRY = this.addItem(38, "uua_berry");
        UUM_BERRY = this.addItem(39, "uum_berry");
    }
}
