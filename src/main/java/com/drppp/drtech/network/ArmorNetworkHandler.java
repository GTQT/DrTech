package com.drppp.drtech.network;

import com.drppp.drtech.network.packets.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ArmorNetworkHandler {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("drtech_armor");
    private static int packetId = 0;

    public static void init() {
        registerC2S(CModularArmorSwitchModuleMode.class);
        registerC2S(CModularArmorDrainEnergy.class);
    }

    private static void registerC2S(Class<? extends IPacket> clazz) {
        CHANNEL.registerMessage(C2SHandler, clazz, packetId++, Side.SERVER);
    }

    private static void registerS2C(Class<? extends IPacket> clazz) {
        CHANNEL.registerMessage(S2CHandler, clazz, packetId++, Side.CLIENT);
    }

    public static void sendToServer(IPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToWorld(IPacket packet, World world) {
        CHANNEL.sendToDimension(packet, world.provider.getDimension());
    }

    public static void sendToPlayer(IPacket packet, EntityPlayerMP player) {
        CHANNEL.sendTo(packet, player);
    }

    public static void sendToAllAround(IPacket packet, NetworkRegistry.TargetPoint targetPoint) {
        CHANNEL.sendToAllAround(packet, targetPoint);
    }

    public static void sendToAllTracking(IMessage message, NetworkRegistry.TargetPoint point) {
        CHANNEL.sendToAllTracking(message, point);
    }

    final static IMessageHandler<IPacket, IPacket> S2CHandler = (message, ctx) -> {
        NetHandlerPlayClient handler = ctx.getClientHandler();
        IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
        if (threadListener.isCallingFromMinecraftThread()) {
            return message.executeClient(handler);
        } else {
            threadListener.addScheduledTask(() -> message.executeClient(handler));
        }
        return null;
    };
    final static IMessageHandler<IPacket, IPacket> C2SHandler = (message, ctx) -> {
        NetHandlerPlayServer handler = ctx.getServerHandler();
        IThreadListener threadListener = FMLCommonHandler.instance().getWorldThread(handler);
        if (threadListener.isCallingFromMinecraftThread()) {
            return message.executeServer(handler);
        } else {
            threadListener.addScheduledTask(() -> message.executeServer(handler));
        }
        return null;
    };
}
