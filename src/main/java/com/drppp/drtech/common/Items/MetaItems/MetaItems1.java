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
                })
                );
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
                MyMetaItems.SCRAP = this.addItem(7,"scrap").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(BluePrintBehavior.INSTANCE);
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
                .addComponents(new FilteredFluidStats(128000, 2600, true, true, false, false, true))
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1);
        MyMetaItems.GRASS_KILLER = this.addItem(27,"grass_killer")
                .addComponents(new KillGrassBehavior())
                .setCreativeTabs(DrTechMain.Mytab)
                .setMaxStackSize(1);
        MyMetaItems.UPGRADE_NULL = this.addItem(28,"upgrade_null").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64);
        MyMetaItems.UPGRADE_SPEED1 = this.addItem(29,"upgrade_speed1").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加速"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.speed","2"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+1A-LV"));
                }));
        MyMetaItems.UPGRADE_SPEED2 = this.addItem(30,"upgrade_speed2").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加速"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.speed","4"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+1A-MV"));
                }));
        MyMetaItems.UPGRADE_SPEED3 = this.addItem(31,"upgrade_speed3").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加速"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.speed","8"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+1A-HV"));
                }));
        MyMetaItems.UPGRADE_SPEED4 = this.addItem(32,"upgrade_speed4").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加速"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.speed","16"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+1A-EV"));
                }));
        MyMetaItems.UPGRADE_SPEED5 = this.addItem(33,"upgrade_speed5").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加速"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.speed","32"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+1A-IV"));
                }));
        MyMetaItems.UPGRADE_SPEED6  = this.addItem(34,"upgrade_speed6").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加速"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.speed","64"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+1A-LuV"));
                }));
        MyMetaItems.UPGRADE_SPEED7  = this.addItem(35,"upgrade_speed7").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加速"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.speed","128"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+1A-ZPM"));
                }));
        MyMetaItems.UPGRADE_SPEED8  = this.addItem(36,"upgrade_speed8").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加速"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.speed","256"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+1A-UV"));
                }));
        MyMetaItems.UPGRADE_SPEED8P  = this.addItem(37,"upgrade_speed8p").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加速"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.speed","256"));
                    lines.add(I18n.format("metaitem.upgrade.product", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+1A-UV"));
                }));
        MyMetaItems.UPGRADE_PRODUCTION  = this.addItem(38,"upgrade_production").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","产量"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","8"));
                    lines.add(I18n.format("metaitem.upgrade.production.function", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+40%"));
                }));
        MyMetaItems.UPGRADE_PLAIN  = this.addItem(39,"upgrade_plain").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","平原环境"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.biomes","平原"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+40%"));
                }));
        MyMetaItems.UPGRADE_LIGHT  = this.addItem(40,"upgrade_light").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","内部光照"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.light", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+5%"));
                }));
        MyMetaItems.UPGRADE_FLOWERING  = this.addItem(41,"upgrade_flowering").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","授粉"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","8S"));
                    lines.add(I18n.format("metaitem.upgrade.flowering", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+10%"));
                }));
        MyMetaItems.UPGRADE_WINTER_EMULATION  = this.addItem(42,"upgrade_winter_emulation").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","冰原环境"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.biomes","冰原"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+50%"));
                }));
        MyMetaItems.UPGRADE_DRYER  = this.addItem(43,"upgrade_dryer").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","干燥"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","16"));
                    lines.add(I18n.format("metaitem.upgrade.dry", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+2.5%"));
                }));
        MyMetaItems.UPGRADE_AUTOMATION  = this.addItem(44,"upgrade_automation").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","自动化"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+10%"));
                }));
        MyMetaItems.UPGRADE_HUMIDIFIER  = this.addItem(45,"upgrade_humidifier").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","干燥"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","16"));
                    lines.add(I18n.format("metaitem.upgrade.humidifier", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+2.5%"));
                }));
        MyMetaItems.UPGRADE_HELL_EMULATION  = this.addItem(46,"upgrade_hell_emulation").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","地狱环境"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.biomes","地狱"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+50%"));
                }));
        MyMetaItems.UPGRADE_POLLEN_SCRUBBER  = this.addItem(47,"upgrade_pollen_scrubber").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","花粉洗涤"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.pollen_scrubber", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+30%"));
                }));
        MyMetaItems.UPGRADE_DESERT_EMULATION  = this.addItem(48,"upgrade_desert_emulation").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","沙漠环境"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.biomes","沙漠"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+20%"));
                }));
        MyMetaItems.UPGRADE_COOLER  = this.addItem(49,"upgrade_cooler").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","冷却器"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","16"));
                    lines.add(I18n.format("metaitem.upgrade.cooler", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+2.5%"));
                }));
        MyMetaItems.UPGRADE_LIFESPAN  = this.addItem(50,"upgrade_lifespan").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","寿命"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","4"));
                    lines.add(I18n.format("metaitem.upgrade.lifespan", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+5%"));
                }));
        MyMetaItems.UPGRADE_SEAL  = this.addItem(51,"upgrade_seal").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","气密性"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.seal", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+5%"));
                }));
        MyMetaItems.UPGRADE_GENETIC_STABILIZER  = this.addItem(52,"upgrade_genetic_stabilizer").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","基因稳定"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.genetic_stabilizer", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+150%"));
                }));
        MyMetaItems.UPGRADE_JUNGLE_EMULATION  = this.addItem(53,"upgrade_jungle_emulation").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","丛林环境"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.biomes","丛林"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+20%"));
                }));
        MyMetaItems.UPGRADE_TERRITORY  = this.addItem(54,"upgrade_territory").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","范围"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","4"));
                    lines.add(I18n.format("metaitem.upgrade.territory", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+5%"));
                }));
        MyMetaItems.UPGRADE_OCEAN_EMULATION  = this.addItem(55,"upgrade_ocean_emulation").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","海洋环境"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.biomes","海洋"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+20%"));
                }));
        MyMetaItems.UPGRADE_OPEN_SKY  = this.addItem(56,"upgrade_open_sky").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","露天环境"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+5%"));
                }));
        MyMetaItems.UPGRADE_HEATER  = this.addItem(57,"upgrade_heater").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","加热器"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.heater", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+2.5%"));
                }));
        MyMetaItems.UPGRADE_SIEVE  = this.addItem(58,"upgrade_sieve").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","筛网"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.sieve", new Object[0]));
                    lines.add(I18n.format("metaitem.upgrade.energy","+25%"));
                }));
        MyMetaItems.UPGRADE_T  = this.addItem(59,"upgrade_t").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(64).addComponents(
                new TooltipBehavior((lines) -> {
                    lines.add(I18n.format("metaitem.upgrade.info","黑夜环境"));
                    lines.add(I18n.format("metaitem.upgrade.maxnum","1"));
                    lines.add(I18n.format("metaitem.upgrade.energy","+5%"));
                }));
        MyMetaItems.TOOL_BOX = this.addItem(60,"tool_box").setCreativeTabs(DrTechMain.Mytab).setMaxStackSize(1)
                .addComponents(
                        new TooltipBehavior((lines) -> {
                            lines.add(I18n.format("metaitem.upgrade.tool_box.1", new Object[0]));
                            lines.add(I18n.format("metaitem.upgrade.tool_box.2", new Object[0]));
                        }));
        MyMetaItems.ENERGY_LINK_PANEL = this.addItem(61,"cover.energy_link_panel").setCreativeTabs(DrTechMain.Mytab);
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
