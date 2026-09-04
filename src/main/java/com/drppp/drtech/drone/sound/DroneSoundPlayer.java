package com.drppp.drtech.drone.sound;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.UUID;

/** Authoritative semantic sound dispatcher with explicit per-listener linear attenuation. */
public final class DroneSoundPlayer {
    private static final DroneSoundLimiter LIMITER = new DroneSoundLimiter();

    private DroneSoundPlayer() { }

    public static boolean play(World world, UUID sourceId, DroneSoundCue cue,
            double x, double y, double z, float baseVolume, float pitch) {
        if (!(world instanceof WorldServer) || sourceId == null || cue == null
                || !LIMITER.shouldPlay(sourceId, cue, world.getTotalWorldTime())) return false;
        WorldServer server = (WorldServer) world;
        for (EntityPlayerMP player : server.getMinecraftServer().getPlayerList().getPlayers()) {
            if (player.world != world) continue;
            double distance = player.getDistance(x, y, z);
            float volume = Math.max(0.0F, baseVolume) * DroneSoundLimiter.volume(cue, distance);
            if (volume <= 0.0F) continue;
            player.connection.sendPacket(new SPacketSoundEffect(cue.getDefaultSound(), SoundCategory.BLOCKS,
                    x, y, z, volume, Math.max(0.5F, Math.min(2.0F, pitch))));
        }
        return true;
    }
}
