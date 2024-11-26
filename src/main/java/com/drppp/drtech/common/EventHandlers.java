package com.drppp.drtech.common;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.EntityDropPod;
import com.drppp.drtech.common.enent.MobHordeWorldData;
import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import thebetweenlands.common.config.BetweenlandsConfig;
import thebetweenlands.common.world.teleporter.TeleporterHandler;

import static com.drppp.drtech.DrtConfig.onPlayerLoggedInEvent;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class EventHandlers {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.player.world.isRemote && BetweenlandsConfig.WORLD_AND_DIMENSION.startInBetweenlands && event.player.world.provider.getDimension() != BetweenlandsConfig.WORLD_AND_DIMENSION.dimensionId && event.player.world instanceof WorldServer) {
            NBTTagCompound dataNbt = event.player.getEntityData();
            NBTTagCompound persistentNbt = dataNbt.getCompoundTag("PlayerPersisted");
            boolean isFirstTimeSpawning = !persistentNbt.hasKey("thebetweenlands.not_first_spawn", 1) || !persistentNbt.getBoolean("thebetweenlands.not_first_spawn");
            if (isFirstTimeSpawning) {
                persistentNbt.setBoolean("thebetweenlands.not_first_spawn", true);
                dataNbt.setTag("PlayerPersisted", persistentNbt);
                WorldServer blWorld = event.player.world.getMinecraftServer().getWorld(BetweenlandsConfig.WORLD_AND_DIMENSION.dimensionId);
                TeleporterHandler.transferToDim(event.player, blWorld, !onPlayerLoggedInEvent, true);
                if (onPlayerLoggedInEvent) {
                    EntityDropPod dropPod = new EntityDropPod(event.player.getEntityWorld(), event.player.posX, event.player.posY + 256, event.player.posZ);
                    GTTeleporter teleporter = new GTTeleporter((WorldServer) event.player.world, event.player.posX, event.player.posY + 256, event.player.posZ);
                    TeleportHandler.teleport(event.player, event.player.dimension, teleporter, event.player.posX, event.player.posY + 256, event.player.posZ);

                    event.player.getEntityWorld().spawnEntity(dropPod);
                    event.player.startRiding(dropPod);
                }
            }
        }
    }


    @SubscribeEvent
    public static void on(TickEvent.WorldTickEvent event) {

        World world = event.world;

        if (world.isRemote) {
            return;
        }

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (world.provider.getDimension() != 0) {
            return;
        }

        if (world instanceof WorldServer server) {
            PlayerList list = server.getMinecraftServer().getPlayerList();
            MobHordeWorldData mobHordeWorldData = MobHordeWorldData.get(world);
            list.getPlayers().forEach(p -> mobHordeWorldData.getPlayerData(p.getPersistentID()).update(p));
            mobHordeWorldData.markDirty();
        }
    }

}