package com.brachy84.mechtech.common.items;

import com.brachy84.mechtech.api.armor.MaterialArmorModuleBuilder;
import com.brachy84.mechtech.api.armor.Modules;
import com.brachy84.mechtech.api.armor.modules.MaterialArmorModule;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import static com.brachy84.mechtech.common.items.MTMetaItems.*;

public class MTMetaItem extends StandardMetaItem {

    public MTMetaItem() {
        super((short) 0);
    }

    @Override
    public void registerSubItems() {
        WIRELESS_RECEIVER = addItem(0, "wireless_receiver");
        // Modules
        SHOCK_ABSORBER = addItem(1, "shock_absorber");
        THICK_INSULATOR = addItem(2, "thick_insulator");
        BINOCULARS = addItem(3, "binoculars");
        AUTO_FEEDER = addItem(4, "auto_feeder");
        TESLA_COIL = addItem(5, "tesla_coil");
        OXYGEN_MASK = addItem(6, "oxygen_mask");

        // Armor Platings
        for (Int2ObjectMap.Entry<MaterialArmorModuleBuilder> entry : Modules.getArmorModules().int2ObjectEntrySet()) {
            MaterialArmorModuleBuilder builder = entry.getValue();
            if (!builder.isRegistered())
                continue;
            MetaItem<?>.MetaValueItem metaValueItem = addItem(entry.getIntKey(), "armor_plating_" + builder.material.toString())
                    .addComponents();
            ((MaterialArmorModule) Modules.getModule(entry.getIntKey())).init(metaValueItem);
            MATERIAL_ARMOR_PLATINGS.put(builder.material, metaValueItem);
        }
        //MetaItems.TOOL_DATA_STICK.addComponents(new DataStickBehavior());
    }

    @Override
    protected String formatModelPath(MetaItem<?>.MetaValueItem metaValueItem) {
        if (metaValueItem.unlocalizedName.startsWith("armor_plating_")) {
            return "metaitems/armor_plating";
        }
        return super.formatModelPath(metaValueItem);
    }
}
