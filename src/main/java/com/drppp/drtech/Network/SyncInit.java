package com.drppp.drtech.Network;

import com.drppp.drtech.Network.mover.ClearMoverPreviewPacket;
import com.drppp.drtech.Network.mover.StartMoverPreviewPacket;
import com.drppp.drtech.Network.mover.RotateMoverPreviewPacket;
import com.drppp.drtech.common.multiblock.mover.MoverSessionManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class SyncInit {
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("drtech_channel");
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        NETWORK.registerMessage(UpdateTileEntityPacketHandler.class, UpdateTileEntityPacket.class, 0, Side.SERVER);
        NETWORK.registerMessage(StartMoverPreviewPacket.Handler.class, StartMoverPreviewPacket.class, 1, Side.CLIENT);
        NETWORK.registerMessage(ClearMoverPreviewPacket.Handler.class, ClearMoverPreviewPacket.class, 2, Side.CLIENT);
        NETWORK.registerMessage(RotateMoverPreviewPacket.Handler.class, RotateMoverPreviewPacket.class, 3, Side.SERVER);
        MoverSessionManager.init();
        initialized = true;
    }
}
