package com.drppp.drtech.Network;

import com.drppp.drtech.common.Entity.EntityAdvancedRocket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketJumpKey implements IMessage {
    public boolean isJumping;

    public PacketJumpKey() {}

    public PacketJumpKey(boolean isJumping) {
        this.isJumping = isJumping;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isJumping = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.isJumping);
    }

    public static class Handler implements IMessageHandler<PacketJumpKey, IMessage> {
        @Override
        public IMessage onMessage(PacketJumpKey message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            if (player.getRidingEntity() instanceof EntityAdvancedRocket) {
                EntityAdvancedRocket rocket = (EntityAdvancedRocket) player.getRidingEntity();
                rocket.setCanStartfly(true);
            }
            return null;
        }
    }
}