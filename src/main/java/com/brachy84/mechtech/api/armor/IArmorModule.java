package com.brachy84.mechtech.api.armor;

import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * Simple armor. Will never absorb unblockable damage
 */
public interface IArmorModule extends IModule {

    double getArmor(EntityEquipmentSlot slot);

    double getToughness(EntityEquipmentSlot slot);
}
