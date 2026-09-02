package com.drppp.drtech.Network.packets;

import com.drppp.drtech.api.armor.ModularArmor;
import com.drppp.drtech.Network.IPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public class CModularArmorDrainEnergy implements IPacket {
    private EntityEquipmentSlot slot;
    private long amount;

    public CModularArmorDrainEnergy()  {}

    public CModularArmorDrainEnergy(final EntityEquipmentSlot slot, final long amount)  {
        this.slot = slot;
        this.amount = amount;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeByte(slot.ordinal());
        buf.writeLong(this.amount);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.slot = EntityEquipmentSlot.values()[buf.readByte()];
        this.amount = buf.readLong();
    }

    @Override
    public IPacket executeServer(NetHandlerPlayServer handler) {
        EntityPlayer player = handler.player;
        ItemStack stack = player.getItemStackFromSlot(this.slot);
        if (stack != null) {
            ModularArmor armor = ModularArmor.get(stack);
            if (armor != null) {
                ModularArmor.drain(stack, this.amount, Integer.MAX_VALUE, false);
            }
        }
        player.inventoryContainer.detectAndSendChanges();
        return null;
    }
}
