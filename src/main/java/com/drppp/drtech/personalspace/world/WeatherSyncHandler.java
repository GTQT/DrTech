package com.drppp.drtech.personalspace.world;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class WeatherSyncHandler {

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
            if (player.world.provider instanceof PWWorldProvider) {
                syncWeatherToClient(player);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) {
            if (event.world.provider instanceof PWWorldProvider) {
                PWWorldProvider provider = (PWWorldProvider) event.world.provider;
                provider.updateWeather();
            }
        }
    }

    private static void syncWeatherToClient(EntityPlayerMP player) {
        WorldServer worldServer = player.getServerWorld();
        worldServer.getEntityTracker().sendToTracking(player,
                new SPacketChangeGameState(7, worldServer.isRaining() ? 1.0f : 0.0f));
        worldServer.getEntityTracker().sendToTracking(player,
                new SPacketChangeGameState(8, worldServer.isThundering() ? 1.0f : 0.0f));
    }
}
