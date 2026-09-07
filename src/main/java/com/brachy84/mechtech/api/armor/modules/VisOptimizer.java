package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.AbstractModule;
import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.common.items.MTMetaItems;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Vis Optimizer Module — reduces Thaumcraft vis consumption.
 * Slot: All armor slots
 *
 * Discount per module: 5%, max 20% across all 4 slots.
 * The actual IVisDiscountGear interface is on MTArmorItem,
 * which delegates to getDiscount().
 */
public class VisOptimizer extends AbstractModule {

    public static final int DISCOUNT_PER_MODULE = 5;

    public VisOptimizer() {
        super("vis_optimizer");
    }

    @Override
    public int maxModules() {
        return 1;
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.HEAD
                || slot == EntityEquipmentSlot.CHEST
                || slot == EntityEquipmentSlot.LEGS
                || slot == EntityEquipmentSlot.FEET;
    }

    /**
     * Called by MTArmorItem's IVisDiscountGear.getVisDiscount() delegation.
     */
    public static int getDiscount(ItemStack armorPiece) {
        if (!Loader.isModLoaded("thaumcraft")) {
            return 0;
        }
        List<IModule> modules = ModularArmor.getModulesOf(armorPiece);
        for (IModule module : modules) {
            if (module instanceof VisOptimizer) {
                return DISCOUNT_PER_MODULE;
            }
        }
        return 0;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        if (Loader.isModLoaded("thaumcraft")) {
            lines.add(I18n.format("mechtech.vis_optimizer.tooltip.1", DISCOUNT_PER_MODULE));
            lines.add(I18n.format("mechtech.vis_optimizer.tooltip.2"));
        } else {
            lines.add(I18n.format("mechtech.vis_optimizer.tooltip.disabled"));
        }
        lines.add(I18n.format("mechtech.modular_armor.usable"));
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.VIS_OPTIMIZER;
    }
}
