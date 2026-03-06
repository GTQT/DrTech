package com.brachy84.mechtech.network.packets;

import com.brachy84.mechtech.api.armor.ModularArmor;
import com.brachy84.mechtech.network.IPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public class CModularArmorSwitchModuleMode implements IPacket {

    private EntityEquipmentSlot slot;
    private String modeName;

    public CModularArmorSwitchModuleMode()  {}

    public CModularArmorSwitchModuleMode(final EntityEquipmentSlot slot, final String modeName)  {
        this.slot = slot;
        this.modeName = modeName;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeByte(slot.ordinal());
        buf.writeByte(modeName.length());
        buf.writeString(modeName);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.slot = EntityEquipmentSlot.values()[buf.readByte()];
        byte length = buf.readByte();
        this.modeName = buf.readString(length);
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        EntityPlayer player = handler.player;
        ItemStack stack = player.getItemStackFromSlot(this.slot);
        if (stack != null) {
            NBTTagCompound nbt = ModularArmor.getArmorData(stack);
            boolean mode = false;
            if (nbt.hasKey(this.modeName)) {
                mode = nbt.getBoolean(this.modeName);
            }
            nbt.setBoolean(this.modeName, !mode);
        }
        player.inventoryContainer.detectAndSendChanges();
        return null;
    }
}
