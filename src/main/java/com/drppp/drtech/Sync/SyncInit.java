package com.drppp.drtech.Sync;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class SyncInit {
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("my_mod_channel");

    public static void init() {
        // 第一个参数是数据包ID
        NETWORK.registerMessage(UpdateTileEntityPacketHandler.class, UpdateTileEntityPacket.class, 0, Side.SERVER);
    }
}
