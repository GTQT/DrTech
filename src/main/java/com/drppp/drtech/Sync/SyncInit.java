package com.drppp.drtech.Sync;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class SyncInit {
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("drtech_channel");
    public static final SimpleNetworkWrapper NETWORK_CLIENT = NetworkRegistry.INSTANCE.newSimpleChannel("drtech_channe_client");

    public static void init() {
        // 第一个参数是数据包ID
        NETWORK.registerMessage(UpdateTileEntityPacketHandler.class, UpdateTileEntityPacket.class, 0, Side.SERVER);
        NETWORK_CLIENT.registerMessage(UpdateTileEntityPacketClinetHandler.class, UpdateTileEntityPacket.class, 1, Side.CLIENT);
    }
}
