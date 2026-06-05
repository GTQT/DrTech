package com.drppp.drtech.lootgames.packets;

import com.drppp.drtech.Tags;
import com.drppp.drtech.lootgames.api.packet.SMessageGameUpdate;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID + "_games");

    private static int id = 0;

    public static void registerPackets() {
        INSTANCE.registerMessage(SMessageGameUpdate.Handler.class, SMessageGameUpdate.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(CMessageGOLFeedback.Handler.class, CMessageGOLFeedback.class, id++, Side.SERVER);
        INSTANCE.registerMessage(SMessageGOLDrawStuff.Handler.class, SMessageGOLDrawStuff.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(SMessageGOLParticle.Handler.class, SMessageGOLParticle.class, id++, Side.CLIENT);
    }
}
