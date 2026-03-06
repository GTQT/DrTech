
package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.AbsorbResult;
import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.ISpecialArmorModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.common.items.MTMetaItems;
import com.brachy84.mechtech.network.NetworkHandler;
import com.brachy84.mechtech.network.packets.CModularArmorSwitchModuleMode;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class ShockAbsorber implements IModule, ISpecialArmorModule {

    private byte toggleTimer = 0;

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.FEET;
    }

    @Override
    public void onClientTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        if (toggleTimer == 0 && KeyBind.ARMOR_CANCEL_INERTIA.isKeyDown(player)) {
            toggleTimer = 5;
            NetworkHandler.sendToServer(new CModularArmorSwitchModuleMode(ModularArmor.get(modularArmorPiece).getSlot(), "cancel_inertia"));
        }

        if (toggleTimer > 0) {
            toggleTimer--;
        }
    }

    @Override
    public void onUnequip(World world, EntityLivingBase player, ItemStack modularArmorPiece, ItemStack newStack) {
        if (!world.isRemote) {
            NBTTagCompound nbt = ModularArmor.getArmorData(modularArmorPiece);
            nbt.setBoolean("cancel_inertia", false);
            ModularArmor.setArmorData(modularArmorPiece, nbt);
            ((EntityPlayer)player).inventoryContainer.detectAndSendChanges();
        }
    }

    @Override
    public String getModuleId() {
        return "shock_absorber";
    }

    @Override
    public AbsorbResult getArmorProperties(EntityLivingBase entity, ItemStack modularArmorPiece, NBTTagCompound moduleData, DamageSource source, double damage, EntityEquipmentSlot slot) {
        if (slot == EntityEquipmentSlot.FEET && source == DamageSource.FALL) {
            return new AbsorbResult(0.9, 40, 10);
        }
        return new AbsorbResult();
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.SHOCK_ABSORBER;
    }

    @Override
    public void addHUDInfo(ItemStack armorPiece, NBTTagCompound armorData, List<String> hudStrings) {
        if (armorData != null) {
            String status = I18n.format("metaarmor.hud.status.disabled");
            if (armorData.hasKey("cancel_inertia")) {
                status = (armorData.getBoolean("cancel_inertia") ? I18n.format("metaarmor.hud.status.enabled") :
                        I18n.format("metaarmor.hud.status.disabled"));
            }
            String result = I18n.format("metaarmor.hud.cancel_inertia_mode", status);
            hudStrings.add(result);
        }
    }
}
