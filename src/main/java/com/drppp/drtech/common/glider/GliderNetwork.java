package com.drppp.drtech.common.glider;

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

public final class GliderNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID + "_glider");

    private GliderNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage(ToggleHandler.class, TogglePacket.class, 0, Side.SERVER);
        if (FMLCommonHandler.instance().getSide().isClient()) {
            CHANNEL.registerMessage(SyncHandler.class, SyncPacket.class, 1, Side.CLIENT);
        }
    }

    public static void sync(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        GliderFlightData data = GliderFlightCapability.get(player);
        if (data == null) {
            return;
        }
        SyncPacket packet = new SyncPacket(player.getEntityId(), data.isDeployed());
        CHANNEL.sendTo(packet, (EntityPlayerMP) player);
        CHANNEL.sendToAllTracking(packet, player);
    }

    public static final class TogglePacket implements IMessage {
        private boolean deployed;

        public TogglePacket() {
        }

        public TogglePacket(boolean deployed) {
            this.deployed = deployed;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            deployed = buffer.readBoolean();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeBoolean(deployed);
        }
    }

    public static final class ToggleHandler implements IMessageHandler<TogglePacket, IMessage> {
        @Override
        public IMessage onMessage(TogglePacket message, MessageContext context) {
            context.getServerHandler().player.getServerWorld().addScheduledTask(() ->
                    GliderFlightHandler.setDeployed(context.getServerHandler().player, message.deployed));
            return null;
        }
    }

    public static final class SyncPacket implements IMessage {
        private int entityId;
        private boolean deployed;

        public SyncPacket() {
        }

        public SyncPacket(int entityId, boolean deployed) {
            this.entityId = entityId;
            this.deployed = deployed;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            entityId = buffer.readInt();
            deployed = buffer.readBoolean();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(entityId);
            buffer.writeBoolean(deployed);
        }
    }

    public static final class SyncHandler implements IMessageHandler<SyncPacket, IMessage> {
        @Override
        public IMessage onMessage(SyncPacket message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Entity entity = Minecraft.getMinecraft().world == null ? null
                        : Minecraft.getMinecraft().world.getEntityByID(message.entityId);
                if (entity instanceof EntityPlayer) {
                    GliderFlightData data = GliderFlightCapability.get((EntityPlayer) entity);
                    if (data != null) {
                        data.setDeployed(message.deployed);
                    }
                }
            });
            return null;
        }
    }
}
