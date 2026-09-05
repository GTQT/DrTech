package com.brachy84.mechtech.network.packets;

import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.network.IPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public class CTeslaCoilModeSwitch implements IPacket {

    private boolean isHelmet;

    public CTeslaCoilModeSwitch() {
    }

    public CTeslaCoilModeSwitch(final boolean isHelmet) {
        this.isHelmet = isHelmet;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBoolean(isHelmet);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.isHelmet = buf.readBoolean();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        EntityPlayer player = handler.player;
        ItemStack stack;
        if (this.isHelmet) {
            stack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        } else {
            stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        }

        NBTTagCompound nbt = ModularArmor.getArmorData(stack);
        byte mode = 1;
        if (nbt.hasKey("tesla_mode")) {
            mode = nbt.getByte("tesla_mode");
        }

        /* 0 - Off
           1 - Only Monsters
           2 - Only Animals
           3 - Animals & Monsters
         */
        mode = switch (mode) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            default -> 0;
        };
        nbt.setByte("tesla_mode", mode);
        ModularArmor.setArmorData(stack, nbt);
        player.inventoryContainer.detectAndSendChanges();
        return null;
    }
}
