package com.drppp.drtech.common.Items.MetaItems;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.common.Items.Behavior.*;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.items.metaitem.stats.IItemColorProvider;
import gregtech.common.items.behaviors.TooltipBehavior;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class MetaItemsReactorReg extends StandardMetaItem {
    public IItemColorProvider fuelColor = new IItemColorProvider() {
        @Override
        public int getItemStackColor(ItemStack itemStack, int i) {
            return 0xdb6ab7;
        }
    };
    public MetaItemsReactorReg() {
    }

    @Override
    public void registerSubItems() {
        MetaItemsReactor.U_FUEL_ROD_1X_EX = this.addItem(4,"u_fuel_rod_1x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(fuelColor)
                .setMaxStackSize(64);
        MetaItemsReactor.U_FUEL_ROD_2X_EX = this.addItem(5,"u_fuel_rod_2x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(fuelColor)
                .setMaxStackSize(64);
        MetaItemsReactor.U_FUEL_ROD_4X_EX = this.addItem(6,"u_fuel_rod_4x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(fuelColor)
                .setMaxStackSize(64);
        //U MOX
        MetaItemsReactor.U_MOX_FUEL_ROD_1X_EX = this.addItem(26,"u_mox_fuel_rod_1x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(fuelColor)
                .setMaxStackSize(64);
        MetaItemsReactor.U_MOX_FUEL_ROD_2X_EX = this.addItem(27,"u_mox_fuel_rod_2x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(fuelColor)
                .setMaxStackSize(64);
        MetaItemsReactor.U_MOX_FUEL_ROD_4X_EX = this.addItem(28,"u_mox_fuel_rod_4x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(fuelColor)
                .setMaxStackSize(64);
        //TH
        MetaItemsReactor.Th_FUEL_ROD_1X_EX = this.addItem(29,"th_fuel_rod_1x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(fuelColor)
                .setMaxStackSize(64);
        MetaItemsReactor.Th_FUEL_ROD_2X_EX = this.addItem(30,"th_fuel_rod_2x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(fuelColor)
                .setMaxStackSize(64);
        MetaItemsReactor.Th_FUEL_ROD_4X_EX = this.addItem(31,"th_fuel_rod_4x_ex")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(fuelColor)
                .setMaxStackSize(64);

        MetaItemsReactor.U_FUEL_ROD_1X = this.addItem(1,"u_fuel_rod_1x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64)
                .addComponents(new FuelRodBehavior(75,2,1,false,0,400000, MetaItemsReactor.U_FUEL_ROD_1X_EX.getStackForm(),75));
        MetaItemsReactor.U_FUEL_ROD_2X = this.addItem(2,"u_fuel_rod_2x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64)
                .addComponents(new FuelRodBehavior(300,4,2,false,0,400000, MetaItemsReactor.U_FUEL_ROD_2X_EX.getStackForm(),75));
        MetaItemsReactor.U_FUEL_ROD_4X = this.addItem(3,"u_fuel_rod_4x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64)
                .addComponents(new FuelRodBehavior(900,8,4,false,0,400000, MetaItemsReactor.U_FUEL_ROD_4X_EX.getStackForm(),75));
        MetaItemsReactor.HEAT_VENT = this.addItem(7,"heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatVentBehavior(0,6,1000,false,false));
        MetaItemsReactor.COMPONENT_HEAT_VENT = this.addItem(8,"component_heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatVentBehavior(0,4,0,false,true));
        MetaItemsReactor.OVERCLOCKED_HEAT_VENT = this.addItem(9,"overclocked_heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatVentBehavior(36,20,1000,true,false));
        MetaItemsReactor.REACTOR_HEAT_VENT = this.addItem(10,"reactor_heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatVentBehavior(5,5,1000,true,false));
        MetaItemsReactor.ADVANCED_HEAT_VENT = this.addItem(11,"advanced_heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatVentBehavior(0,12,1000,false,false));

        MetaItemsReactor.HEAT_EXCHANGER = this.addItem(12,"heat_exchanger").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatExchangerBehavior(4,12,2500,true,true));
        MetaItemsReactor.ADVANCED_HEAT_EXCHANGER = this.addItem(13,"advanced_heat_exchanger").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatExchangerBehavior(8,24,10000,true,true));
        MetaItemsReactor.REACTOR_HEAT_EXCHANGER = this.addItem(14,"reactor_heat_exchanger").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatExchangerBehavior(72,0,5000,true,false));
        MetaItemsReactor.COMPONENT_HEAT_EXCHANGER = this.addItem(15,"component_heat_exchanger").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatExchangerBehavior(0,36,5000,false,true));

        MetaItemsReactor.NEUTRON_REFLECTOR1 =  this.addItem(16,"neutron_reflector1").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new NeutronReflectorBehavior(30000));
        MetaItemsReactor.THICK_NEUTRON_REFLECTOR =  this.addItem(17,"thick_neutron_reflector").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new NeutronReflectorBehavior(120000));
        MetaItemsReactor.IRIDIUM_NEUTRON_REFLECTOR =  this.addItem(18,"iridium_neutron_reflector").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new NeutronReflectorBehavior(999999999));

        MetaItemsReactor.HE_COOLANT_CELL_60K =  this.addItem(19,"he_coolant_cell_60k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new CoolantCellBehavior(60000));
        MetaItemsReactor.HE_COOLANT_CELL_180K =  this.addItem(20,"he_coolant_cell_180k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new CoolantCellBehavior(180000));
        MetaItemsReactor.HE_COOLANT_CELL_360K =  this.addItem(21,"he_coolant_cell_360k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new CoolantCellBehavior(360000));
        MetaItemsReactor.NAK_COOLANT_CELL_60K =  this.addItem(22,"nak_coolant_cell_60k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new CoolantCellBehavior(60000));
        MetaItemsReactor.NAK_COOLANT_CELL_180K =  this.addItem(23,"nak_coolant_cell_180k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new CoolantCellBehavior(180000));
        MetaItemsReactor.NAK_COOLANT_CELL_360K =  this.addItem(24,"nak_coolant_cell_360k").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new CoolantCellBehavior(360000));
        MetaItemsReactor.ADVANCED_COMPONENT_HEAT_VENT = this.addItem(25,"advanced_component_heat_vent").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                .addComponents(new HeatVentBehavior(0,8,0,false,true));

        //U -MOX
        MetaItemsReactor.U_MOX_FUEL_ROD_1X = this.addItem(32,"u_mox_fuel_rod_1x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64)
                .addComponents(new FuelRodBehavior(75,2,1,true,4,200000, MetaItemsReactor.U_MOX_FUEL_ROD_1X_EX.getStackForm(),75));
        MetaItemsReactor.U_MOX_FUEL_ROD_2X = this.addItem(33,"u_mox_fuel_rod_2x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64)
                .addComponents(new FuelRodBehavior(300,4,2,true,4,200000, MetaItemsReactor.U_MOX_FUEL_ROD_2X_EX.getStackForm(),75));
        MetaItemsReactor.U_MOX_FUEL_ROD_4X = this.addItem(34,"u_mox_fuel_rod_4x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64)
                .addComponents(new FuelRodBehavior(900,8,4,true,4,200000, MetaItemsReactor.U_MOX_FUEL_ROD_4X_EX.getStackForm(),75));
        //Th
        MetaItemsReactor.Th_FUEL_ROD_1X = this.addItem(35,"th_fuel_rod_1x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64)
                .addComponents(new FuelRodBehavior(20,0.5f,1,false,0,1000000, MetaItemsReactor.Th_FUEL_ROD_1X_EX.getStackForm(),20));
        MetaItemsReactor.Th_FUEL_ROD_2X = this.addItem(36,"th_fuel_rod_2x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64)
                .addComponents(new FuelRodBehavior(80,1,2,false,0,1000000, MetaItemsReactor.Th_FUEL_ROD_2X_EX.getStackForm(),20));
        MetaItemsReactor.Th_FUEL_ROD_4X = this.addItem(37,"th_fuel_rod_4x")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64)
                .addComponents(new FuelRodBehavior(240,2,4,false,0,1000000, MetaItemsReactor.Th_FUEL_ROD_4X_EX.getStackForm(),20));
        MetaItemsReactor.COOLANT_NULL_CELL_1 = this.addItem(38,"coolant_null_cell_1")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64);
        MetaItemsReactor.COOLANT_NULL_CELL_2 = this.addItem(39,"coolant_null_cell_2")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64);
        MetaItemsReactor.COOLANT_NULL_CELL_3 = this.addItem(40,"coolant_null_cell_3")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64);
        MetaItemsReactor.UPGRADE_IO = this.addItem(41,"upgrade_io").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.nuclear.upgrade.io", new Object[0]));
                }));
        MetaItemsReactor.UPGRADE_STOP = this.addItem(42,"upgrade_stop").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.nuclear.upgrade.stop", new Object[0]));
                }));
        MetaItemsReactor.UPGRADE_CATCH = this.addItem(43,"upgrade_catch").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.nuclear.upgrade.catch", new Object[0]));
                }));
        MetaItemsReactor.UPGRADE_REFLECT = this.addItem(44,"upgrade_reflect").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.nuclear.upgrade.reflect", new Object[0]));
                }));
    }


}
