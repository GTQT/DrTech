package com.brachy84.mechtech.common.items;

import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.api.armor.ModularArmorStats;
import com.brachy84.mechtech.common.MTConfig;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.brachy84.mechtech.common.items.MTMetaItems.*;

public class MTArmorItem extends ArmorMetaItem<ArmorMetaItem<?>.ArmorMetaValueItem> {

    @Override
    public void registerSubItems() {
        MODULAR_HELMET = addItem(0, "modular_helmet").setArmorLogic(new ModularArmor(EntityEquipmentSlot.HEAD, MTConfig.modularArmor.helmetSlots, 8000));
        MODULAR_CHESTPLATE = addItem(1, "modular_chestplate").setArmorLogic(new ModularArmor(EntityEquipmentSlot.CHEST, MTConfig.modularArmor.chestPlateSlots, 512000));
        MODULAR_LEGGINGS = addItem(2, "modular_leggings").setArmorLogic(new ModularArmor(EntityEquipmentSlot.LEGS, MTConfig.modularArmor.leggingsSlots, 64000));
        MODULAR_BOOTS = addItem(3, "modular_boots").setArmorLogic(new ModularArmor(EntityEquipmentSlot.FEET, MTConfig.modularArmor.bootsSlot, 0));
    }

    @Override
    public void addInformation(@Nonnull ItemStack itemStack, @Nullable World worldIn, @Nonnull List<String> lines, @Nonnull ITooltipFlag tooltipFlag) {
        ArmorMetaItem<?>.ArmorMetaValueItem item = this.getItem(itemStack);
        if (item != null) {
            if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
                lines.add(I18n.format("metaitem.modular_armor.tooltip.sneak"));
            } else {
                lines.add(I18n.format("metaitem.modular_armor.tooltip.unsneak"));
            }
            Collection<IModule> modules = ModularArmor.getModulesOf(itemStack);
            ModularArmor modularArmor = ModularArmor.get(itemStack);
            String unlocalizedTooltip = "metaitem." + item.unlocalizedName + ".tooltip";
            if (I18n.hasKey(unlocalizedTooltip)) {
                lines.addAll(Arrays.asList(I18n.format(unlocalizedTooltip, new Object[0]).split("/n")));
            }

            if (modules.size() > 0) {
                lines.add(I18n.format("metaitem.modular_armor.installed_modules", modules.size(), modularArmor.getModuleSlots()));
                for (IModule module : modules) {
                    lines.add(" - " + module.getLocalizedName());
                }
                for (IModule module : modules) {
                    module.addTooltip(itemStack, worldIn, lines, tooltipFlag);
                }
            } else {
                lines.add(I18n.format("metaitem.modular_armor.no_modules"));
            }

            IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electricItem != null) {
                if (electricItem.getMaxCharge() == 0) {
                    lines.add(I18n.format("metaitem.modular_armor.no_battery"));
                } else {
                    lines.add(I18n.format("metaitem.generic.electric_item.tooltip", electricItem.getCharge(), electricItem.getMaxCharge(), "Unspecified"));
                }
            }

            if (modularArmor.getMaxFluidSize() > 0) {
                lines.add(I18n.format("metaitem.modular_armor.max_fluid", modularArmor.getMaxFluidSize() + "mb"));
                IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    IFluidTankProperties[] properties = fluidHandler.getTankProperties();
                    if (properties[0] != ModularArmorStats.DEFAULT_TANK) {
                        for (IFluidTankProperties property : properties) {
                            if (property.getContents() == null || property.getContents().amount <= 0) continue;
                            FluidStack fluid = property.getContents();
                            lines.add(I18n.format("metaitem.generic.fluid_container.tooltip",
                                    fluid == null ? 0 : fluid.amount,
                                    property.getCapacity(),
                                    fluid == null ? "" : fluid.getLocalizedName()));
                        }
                    }
                }
            }

            for (IItemBehaviour behaviour : this.getBehaviours(itemStack)) {
                behaviour.addInformation(itemStack, lines);
            }
        }
    }
}
