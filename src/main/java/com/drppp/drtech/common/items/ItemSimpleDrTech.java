package com.drppp.drtech.common.Items;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import net.minecraft.item.Item;

public class ItemSimpleDrTech extends Item {
    public ItemSimpleDrTech(String name) {
        setRegistryName(Tags.MODID, name);
        setTranslationKey(Tags.MODID + "." + name);
        setCreativeTab(DrTechMain.DrTechTab);
    }
}
