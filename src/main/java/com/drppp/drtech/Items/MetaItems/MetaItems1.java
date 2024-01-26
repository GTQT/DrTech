package com.drppp.drtech.Items.MetaItems;

import com.drppp.drtech.DrTechMain;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.common.items.behaviors.TooltipBehavior;
import net.minecraft.client.resources.I18n;


public  class MetaItems1 extends StandardMetaItem {
    public MetaItems1() {
    }
    public void registerSubItems() {
        MyMetaItems.ENERGY_ELEMENT_1 = this.addItem(0,"energy_element_1").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.1", new Object[0]));
                }));
        MyMetaItems.ENERGY_ELEMENT_2 = this.addItem(1,"energy_element_2").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.2", new Object[0]));
                }));
        MyMetaItems.ENERGY_ELEMENT_3 = this.addItem(2,"energy_element_3").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.3", new Object[0]));
                }));
        MyMetaItems.ENERGY_ELEMENT_4 = this.addItem(3,"energy_element_4").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.4", new Object[0]));
                }));
        MyMetaItems.ENERGY_ELEMENT_5 = this.addItem(4,"energy_element_5").setMaxStackSize(64).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.weight.tooltip.5", new Object[0]));
                }));
    }
}
