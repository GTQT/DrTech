package com.drppp.drtech.Network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class SyncInit {
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("drtech_channel");
    public static final SimpleNetworkWrapper NETWORK_CLIENT = NetworkRegistry.INSTANCE.newSimpleChannel("dr_c_c");
    public static final SimpleNetworkWrapper ROCKET_NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("dr_c_ro");
    public static final SimpleNetworkWrapper PRESSED_NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("dr_c_pre");
    public static void init() {
        // 第一个参数是数据包ID
        NETWORK.registerMessage(UpdateTileEntityPacketHandler.class, UpdateTileEntityPacket.class, 0, Side.SERVER);
        NETWORK_CLIENT.registerMessage(UpdateTileEntityPacketClinetHandler.class, UpdateTileEntityPacket.class, 1, Side.CLIENT);
        ROCKET_NETWORK.registerMessage(PacketSyncDimension.Handler.class,PacketSyncDimension.class,2,Side.CLIENT);
        PRESSED_NETWORK.registerMessage(PacketJumpKey.Handler.class, PacketJumpKey.class, 3, Side.SERVER);
    }
}
