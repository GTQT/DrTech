package com.drppp.drtech.wings;

import com.drppp.drtech.Tags;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class WingsNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID + "_wings");

    private WingsNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage(ToggleHandler.class, TogglePacket.class, 0, Side.SERVER);
        if (FMLCommonHandler.instance().getSide().isClient()) {
            CHANNEL.registerMessage(SyncHandler.class, SyncPacket.class, 1, Side.CLIENT);
        }
    }

    public static void sync(EntityPlayer player) {
        WingsFlightData data = WingsFlightCapability.get(player);
        if (data == null || !(player instanceof EntityPlayerMP)) {
            return;
        }
        SyncPacket packet = new SyncPacket(player.getEntityId(), data.isFlying(), data.getTimeFlying());
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        CHANNEL.sendTo(packet, serverPlayer);
        CHANNEL.sendToAllTracking(packet, player);
    }

    public static final class TogglePacket implements IMessage {
        private boolean flying;

        public TogglePacket() {
        }

        public TogglePacket(boolean flying) {
            this.flying = flying;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            flying = buffer.readBoolean();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeBoolean(flying);
        }
    }

    public static final class ToggleHandler implements IMessageHandler<TogglePacket, IMessage> {
        @Override
        public IMessage onMessage(TogglePacket message, MessageContext context) {
            context.getServerHandler().player.getServerWorld().addScheduledTask(() ->
                    WingsFlightHandler.setFlying(context.getServerHandler().player, message.flying));
            return null;
        }
    }

    public static final class SyncPacket implements IMessage {
        private int entityId;
        private boolean flying;
        private int timeFlying;

        public SyncPacket() {
        }

        public SyncPacket(int entityId, boolean flying, int timeFlying) {
            this.entityId = entityId;
            this.flying = flying;
            this.timeFlying = timeFlying;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            entityId = buffer.readInt();
            flying = buffer.readBoolean();
            timeFlying = buffer.readInt();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(entityId);
            buffer.writeBoolean(flying);
            buffer.writeInt(timeFlying);
        }
    }

    public static final class SyncHandler implements IMessageHandler<SyncPacket, IMessage> {
        @Override
        public IMessage onMessage(SyncPacket message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Entity entity = Minecraft.getMinecraft().world == null ? null
                        : Minecraft.getMinecraft().world.getEntityByID(message.entityId);
                if (entity instanceof EntityPlayer) {
                    WingsFlightData data = WingsFlightCapability.get((EntityPlayer) entity);
                    if (data != null) {
                        data.setFlying(message.flying);
                        data.setPreviousTimeFlying(message.timeFlying);
                        data.setTimeFlying(message.timeFlying);
                    }
                }
            });
            return null;
        }
    }
}
