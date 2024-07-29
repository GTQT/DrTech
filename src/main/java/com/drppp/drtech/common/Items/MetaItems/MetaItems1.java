package com.drppp.drtech.common.Items.MetaItems;

import baubles.api.BaubleType;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.common.Items.Baubles.ElectricFlightRingBehavior;
import com.drppp.drtech.common.Items.Baubles.ElectricLifeSupportRingBehavior;
import com.drppp.drtech.common.Items.Behavior.*;
import gregtech.api.GTValues;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.FilteredFluidStats;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.common.items.behaviors.TooltipBehavior;
import gregtech.integration.baubles.BaubleBehavior;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;


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
        MyMetaItems.GRAVITY_SHIELD = this.addItem(5,"gravity_shield").setMaxStackSize(1).setCreativeTabs(DrTechMain.Mytab)
                .addComponents(new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.gravity_shield.tooltip.1", new Object[0]));
                })).addComponents(new ChunkRemoveBehavior());
                MyMetaItems.SKULL_DUST = this.addItem(6,"skull_dust").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64);
                MyMetaItems.SCRAP = this.addItem(7,"scrap").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64);
                MyMetaItems.CD_ROM = this.addItem(8,"cd_rom").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(16).addComponents(new DataItemBehavior(true));
                MyMetaItems.UU_MATER = this.addItem(9,"uu_mater").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64);
                MyMetaItems.PIPIE_1 = this.addItem(10,"pipe_1").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                        .addComponents(new TooltipBehavior((lines) -> {
                            lines.add(I18n.format("metaitem.pipe_1.tooltip.1", new Object[0]));
                        }));
                MyMetaItems.PIPIE_5 = this.addItem(11,"pipe_5").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                        .addComponents(new TooltipBehavior((lines) -> {
                            lines.add(I18n.format("metaitem.pipe_5.tooltip.1", new Object[0]));
                        }));
                MyMetaItems.PIPIE_10 = this.addItem(12,"pipe_10").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64)
                        .addComponents(new TooltipBehavior((lines) -> {
                            lines.add(I18n.format("metaitem.pipe_10.tooltip.1", new Object[0]));
                        }));
        MyMetaItems.POS_CARD = this.addItem(13,"pos_card").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1).addComponents(new DataItemBehavior(true));
        MyMetaItems.GOLD_COIN = this.addItem(14,"gold_coin").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.gold_coin.tooltip.1", new Object[0]));
                })
        );
        if (Loader.isModLoaded("baubles"))
        {
            MyMetaItems.FLY_RING = this.addItem(15,"electric_flight_ring")
                    .setCreativeTabs(DrTechMain.Mytab)
                    .setMaxStackSize(1).addComponents(ElectricStats.createElectricItem(25600000,GTValues.HV))
                    .addComponents(new ElectricFlightRingBehavior());

            MyMetaItems.LIFE_SUPPORT_RING = this.addItem(16,"electric_life_support_ring")
                    .setCreativeTabs(DrTechMain.Mytab)
                    .setMaxStackSize(1).addComponents(ElectricStats.createElectricItem(25600000,GTValues.HV))
                    .addComponents(new ElectricLifeSupportRingBehavior());
        MyMetaItems.TELEPATHIC_NECKLACE = this.addItem(24,"telepathic_necklace")
                    .setCreativeTabs(DrTechMain.Mytab)
                    .setMaxStackSize(1)
                    .addComponents(new BaubleBehavior(BaubleType.HEAD));
        }
        MyMetaItems.TACTICAL_LASER_SUBMACHINE_GUN = this.addItem(17,"tactical_laser_submachine_gun")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1)
                .addComponents(ElectricStats.createElectricItem(12800000,GTValues.HV));
        MyMetaItems.ELECTRIC_PLASMA_GUN = this.addItem(18,"electric_plasma_gun")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1)
                .addComponents(ElectricStats.createElectricItem(12800000,GTValues.HV));
        MyMetaItems.ADVANCED_TACHINO_DISRUPTOR = this.addItem(19,"advanced_tachino_disruptor")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1)
                .addComponents(ElectricStats.createElectricItem(51200000,GTValues.IV));
        MyMetaItems.NULL_FUEL_ROD = this.addItem(20,"null_fuel_rod")
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(64);
        MyMetaItems.NUCLEAR_BATTERY_LV = this.addItem(21,"nuclear_battery_lv")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(ElectricStatsNuclear.createBattery(1200000,GTValues.LV,true))
                .setMaxStackSize(1);
        MyMetaItems.NUCLEAR_BATTERY_MV = this.addItem(22,"nuclear_battery_mv")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(ElectricStatsNuclear.createBattery(4200000,GTValues.MV,true))
                .setMaxStackSize(1);
        MyMetaItems.NUCLEAR_BATTERY_HV = this.addItem(23,"nuclear_battery_hv")
                .setCreativeTabs(DrTechMain.Mytab)
                .addComponents(ElectricStatsNuclear.createBattery(32000000,GTValues.HV,true))
                .setMaxStackSize(1);
        MyMetaItems.WIRELESS_NETWORK_CONTROL_PANEL = this.addItem(25,"wireless_network_control_panel")
                .addComponents(new WirelessPanelBehavior())
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1);
        MyMetaItems.HAND_PUMP = this.addItem(26,"hand_pump")
                .addComponents(new HandPumpBehavior())
                .addComponents(new FilteredFluidStats(128000, 2600, true, false, false, false, true))
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1);
        MyMetaItems.GRASS_KILLER = this.addItem(27,"grass_killer")
                .addComponents(new KillGrassBehavior())
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1);

    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, EntityPlayer player, @NotNull EnumHand hand) {
        MetItemsEvent.onItemRightClick(world,player,hand);
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public @NotNull EnumActionResult onItemUse(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        MetItemsEvent.onItemUse(player,world,pos,hand,facing,hitX,hitY,hitZ);
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        MetItemsEvent.hitEntity(stack,target,attacker);
        return super.hitEntity(stack, target, attacker);
    }
}
