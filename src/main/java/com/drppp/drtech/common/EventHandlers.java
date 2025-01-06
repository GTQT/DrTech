package com.drppp.drtech.common;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.EntityDropPod;
import com.drppp.drtech.common.enent.MobHordeWorldData;
import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.drppp.drtech.DrtConfig.onPlayerLoggedAtTheBetweenLand;
import static com.drppp.drtech.DrtConfig.onPlayerLoggedInEvent;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class EventHandlers {

    private static final String FIRST_SPAWN = Tags.MODID + ".first_spawn";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!onPlayerLoggedInEvent) return;

        NBTTagCompound playerData = event.player.getEntityData();
        NBTTagCompound data = playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ? playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();

        if (!event.player.getEntityWorld().isRemote && !data.getBoolean(FIRST_SPAWN)) {

            data.setBoolean(FIRST_SPAWN, true);
            playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            if (onPlayerLoggedAtTheBetweenLand && Loader.isModLoaded("thebetweenlands")) {
                try {
                    Class<?> betweenlandsConfigClass = Class.forName("thebetweenlands.common.config.BetweenlandsConfig");
                    Field worldAndDimensionField = betweenlandsConfigClass.getField("WORLD_AND_DIMENSION");
                    Object worldAndDimension = worldAndDimensionField.get(null);
                    Field dimensionIdField = worldAndDimension.getClass().getField("dimensionId");
                    int dimensionId = dimensionIdField.getInt(worldAndDimension);
                    Field startInPortalField = worldAndDimension.getClass().getField("startInPortal");
                    boolean startInPortal = startInPortalField.getBoolean(worldAndDimension);
                    MinecraftServer server = event.player.world.getMinecraftServer();
                    if (server != null) {
                        WorldServer blWorld = server.getWorld(dimensionId);
                        Class<?> teleporterHandlerClass = Class.forName("thebetweenlands.common.world.teleporter.TeleporterHandler");
                        Method transferToDimMethod = teleporterHandlerClass.getMethod("transferToDim", Entity.class, World.class, boolean.class, boolean.class);
                        transferToDimMethod.invoke(null, event.player, blWorld, startInPortal, true);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            EntityDropPod dropPod = new EntityDropPod(event.player.getEntityWorld(), event.player.posX, event.player.posY + 256, event.player.posZ);
            GTTeleporter teleporter = new GTTeleporter((WorldServer) event.player.world, event.player.posX, event.player.posY + 256, event.player.posZ);
            TeleportHandler.teleport(event.player, event.player.dimension, teleporter, event.player.posX, event.player.posY + 256, event.player.posZ);

            event.player.getEntityWorld().spawnEntity(dropPod);
            event.player.startRiding(dropPod);
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