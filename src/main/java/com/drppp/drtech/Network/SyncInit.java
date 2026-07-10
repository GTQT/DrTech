package com.drppp.drtech.Network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class SyncInit {
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("drtech_channel");

    public static void init() {
        NETWORK.registerMessage(UpdateTileEntityPacketHandler.class, UpdateTileEntityPacket.class, 0, Side.SERVER);
    }
}
