package com.drppp.drtech.Items.MetaItems;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Items.Behavior.*;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.items.metaitem.stats.IItemColorProvider;
import net.minecraft.item.ItemStack;

public class MetaItemsReactorReg extends StandardMetaItem {
    public MetaItemsReactorReg() {
    }

    @Override
    public void registerSubItems() {
        MetaItemsReactor.Th_FUEL_ROD_1X_EX = this.addItem(4,"u_fuel_rod_1x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new IItemColorProvider() {
                    @Override
                    public int getItemStackColor(ItemStack itemStack, int i) {
                        return 0xdb6ab7;
                    }
                })
                .setMaxStackSize(64);
        MetaItemsReactor.Th_FUEL_ROD_2X_EX = this.addItem(5,"u_fuel_rod_2x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new IItemColorProvider() {
                    @Override
                    public int getItemStackColor(ItemStack itemStack, int i) {
                        return 0xdb6ab7;
                    }
                })
                .setMaxStackSize(64);
        MetaItemsReactor.Th_FUEL_ROD_4X_EX = this.addItem(6,"u_fuel_rod_4x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new IItemColorProvider() {
                    @Override
                    public int getItemStackColor(ItemStack itemStack, int i) {
                        return 0xdb6ab7;
                    }
                })
                .setMaxStackSize(64);
        MetaItemsReactor.U_FUEL_ROD_1X = this.addItem(1,"u_fuel_rod_1x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1)
                .addComponents(new FuelRodBehavior(50,2,2,false,400000, MetaItemsReactor.Th_FUEL_ROD_1X_EX.getStackForm(),50));
        MetaItemsReactor.U_FUEL_ROD_2X = this.addItem(2,"u_fuel_rod_2x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1)
                .addComponents(new FuelRodBehavior(200,4,2,false,400000, MetaItemsReactor.Th_FUEL_ROD_2X_EX.getStackForm(),50));
        MetaItemsReactor.U_FUEL_ROD_4X = this.addItem(3,"u_fuel_rod_4x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1)
                .addComponents(new FuelRodBehavior(600,8,4,false,400000, MetaItemsReactor.Th_FUEL_ROD_4X_EX.getStackForm(),50));
        MetaItemsReactor.HEAT_VENT = this.addItem(7,"heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new HeatVentBehavior(0,6,1000,false,false));
        MetaItemsReactor.COMPONENT_HEAT_VENT = this.addItem(8,"component_heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new HeatVentBehavior(0,4,0,false,true));
        MetaItemsReactor.OVERCLOCKED_HEAT_VENT = this.addItem(9,"overclocked_heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new HeatVentBehavior(36,20,1000,true,false));
        MetaItemsReactor.REACTOR_HEAT_VENT = this.addItem(10,"reactor_heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new HeatVentBehavior(5,5,1000,true,false));
        MetaItemsReactor.ADVANCED_HEAT_VENT = this.addItem(11,"advanced_heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new HeatVentBehavior(0,12,1000,false,false));

        MetaItemsReactor.HEAT_EXCHANGER = this.addItem(12,"heat_exchanger").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new HeatExchangerBehavior(4,12,2500,true,true));
        MetaItemsReactor.ADVANCED_HEAT_EXCHANGER = this.addItem(13,"advanced_heat_exchanger").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new HeatExchangerBehavior(8,24,10000,true,true));
        MetaItemsReactor.REACTOR_HEAT_EXCHANGER = this.addItem(14,"reactor_heat_exchanger").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new HeatExchangerBehavior(72,0,5000,true,false));
        MetaItemsReactor.COMPONENT_HEAT_EXCHANGER = this.addItem(15,"component_heat_exchanger").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new HeatExchangerBehavior(0,36,5000,false,true));

        MetaItemsReactor.NEUTRON_REFLECTOR1 =  this.addItem(16,"neutron_reflector1").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new NeutronReflectorBehavior(30000));
        MetaItemsReactor.THICK_NEUTRON_REFLECTOR =  this.addItem(17,"thick_neutron_reflector").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new NeutronReflectorBehavior(120000));
        MetaItemsReactor.IRIDIUM_NEUTRON_REFLECTOR =  this.addItem(18,"iridium_neutron_reflector").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new NeutronReflectorBehavior(999999999));

        MetaItemsReactor.HE_COOLANT_CELL_60K =  this.addItem(19,"he_coolant_cell_60k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new CoolantCellBehavior(60000));
        MetaItemsReactor.HE_COOLANT_CELL_180K =  this.addItem(20,"he_coolant_cell_180k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new CoolantCellBehavior(180000));
        MetaItemsReactor.HE_COOLANT_CELL_360K =  this.addItem(21,"he_coolant_cell_360k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new CoolantCellBehavior(360000));
        MetaItemsReactor.NAK_COOLANT_CELL_60K =  this.addItem(22,"nak_coolant_cell_60k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new CoolantCellBehavior(60000));
        MetaItemsReactor.NAK_COOLANT_CELL_180K =  this.addItem(23,"nak_coolant_cell_180k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new CoolantCellBehavior(180000));
        MetaItemsReactor.NAK_COOLANT_CELL_360K =  this.addItem(24,"nak_coolant_cell_360k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new CoolantCellBehavior(360000));
    }


}
