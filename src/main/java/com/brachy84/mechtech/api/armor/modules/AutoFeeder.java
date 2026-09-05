package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.MechTech;
import com.brachy84.mechtech.api.armor.AbstractModule;
import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.common.items.MTMetaItems;
import com.brachy84.mechtech.network.NetworkHandler;
import com.brachy84.mechtech.network.packets.CModularArmorSwitchModuleMode;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IFoodBehavior;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class AutoFeeder extends AbstractModule {

    private byte toggleTimer = 0;

    public AutoFeeder() {
        super("auto_feeder");
    }

    @Override
    public boolean canPlaceIn(EntityEquipmentSlot slot, ItemStack modularArmorPiece, IItemHandler modularSlots) {
        return slot == EntityEquipmentSlot.HEAD;
    }

    @Override
    public void onClientTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        if (toggleTimer == 0 && MechTech.keys.AUTO_FEEDER_MODE_SWITCH.isKeyDown(player) && MechTech.keys.AUTO_FEEDER_MODE_SWITCH.isPressed(player)) {
            toggleTimer = 5;
            NetworkHandler.sendToServer(new CModularArmorSwitchModuleMode(ModularArmor.get(modularArmorPiece).getEquipmentSlot(modularArmorPiece), "feeder_enabled"));
        }
        if (toggleTimer > 0) {
            toggleTimer--;
        }
    }

    @Override
    public void onServerTick(World world, EntityPlayer player, ItemStack modularArmorPiece, NBTTagCompound armorData) {
        boolean enabled = true;
        if (armorData.hasKey("feeder_enabled")) {
            enabled = armorData.getBoolean("feeder_enabled");
        }

        if (enabled) {
            int needed = 20 - player.getFoodStats().getFoodLevel();
            if (needed == 0)
                return;
            byte food = armorData.getByte("food");
            // try to feed stored food
            if (food > 0) {
                int toFeed = Math.min(food, needed);
                player.getFoodStats().addStats(toFeed, 0);
                armorData.setByte("food", (byte) (food - toFeed));
                return;
            }

            // find food item in inventory
            int hunger;
            float saturation;
            outer:
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                ItemStack stack = player.inventory.mainInventory.get(i);
                if (stack.isEmpty())
                    continue;
                if (stack.getItem() instanceof ItemFood) {
                    hunger = ((ItemFood) stack.getItem()).getHealAmount(stack);
                    ItemStack remainder = stack.getItem().onItemUseFinish(stack, world, player);
                    if (hunger > needed)
                        armorData.setByte("food", (byte) (hunger - needed));
                    player.inventory.mainInventory.set(i, remainder);
                    return;
                }
                if (stack.getItem() instanceof MetaItem) {
                    for (IItemComponent component : ((MetaItem<?>) stack.getItem()).getItem(stack).getAllStats()) {
                        if (component instanceof IFoodBehavior) {
                            hunger = ((IFoodBehavior) component).getFoodLevel(stack, player);
                            saturation = ((IFoodBehavior) component).getSaturation(stack, player);
                            int toFeed = Math.min(needed, hunger);
                            player.getFoodStats().addStats(toFeed, saturation);
                            if (toFeed < hunger) {
                                armorData.setByte("food", (byte) (hunger - toFeed));
                            }
                            ((IFoodBehavior) component).onFoodEaten(stack, player);
                            stack.shrink(1);
                            break outer;
                        }
                    }
                }
            }
        }
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MTMetaItems.AUTO_FEEDER;
    }

    @Override
    public void addHUDInfo(ItemStack armorPiece, NBTTagCompound armorData, List<String> hudStrings) {
        if (armorData != null) {
            String status = I18n.format("metaarmor.hud.status.disabled");
            if (armorData.hasKey("feeder_enabled")) {
                status = (armorData.getBoolean("feeder_enabled") ? I18n.format("metaarmor.hud.status.enabled") :
                        I18n.format("metaarmor.hud.status.disabled"));
            }
            String result = I18n.format("mechtech.auto_feeder.mode", status);
            hudStrings.add(result);
        }
    }
}
