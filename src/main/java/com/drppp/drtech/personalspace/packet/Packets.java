package com.drppp.drtech.personalspace.packet;

import com.drppp.drtech.Tags;
import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import com.cleanroommc.modularui.factory.ClientGUI;

import codechicken.lib.packet.PacketCustom;
import com.drppp.drtech.personalspace.PWConfig;
import com.drppp.drtech.personalspace.blocks.tile.TilePersonalPortal;
import com.drppp.drtech.personalspace.gui.PWGuiMUI;
import com.drppp.drtech.personalspace.proxy.CommonProxy;
import com.drppp.drtech.personalspace.world.DimensionConfig;

public enum Packets {

    INSTANCE;

    public enum PacketIds {

        DUMMY,
        UPDATE_WORLDLIST,
        CHANGE_WORLD_SETTINGS,
        OPEN_GUI,
    }

    public PacketCustom sendOpenGui(TilePersonalPortal tpp) {
        PacketCustom pkt = new PacketCustom(Tags.MODID, PacketIds.OPEN_GUI.ordinal());
        pkt.writeVarInt(tpp.getTargetID());
        pkt.writeVarInt(tpp.getWorld().provider.getDimension());
        pkt.writeVarInt(tpp.getPos().getX());
        pkt.writeVarInt(tpp.getPos().getY());
        pkt.writeVarInt(tpp.getPos().getZ());
        pkt.writeString(tpp.getCustomName());
        return pkt;
    }

    public PacketCustom sendChangeWorldSettings(int dimID, BlockPos blockPos, String name,
                                                DimensionConfig dimensionConfig) {
        PacketCustom pkt = new PacketCustom(Tags.MODID, PacketIds.CHANGE_WORLD_SETTINGS.ordinal());
        pkt.writeVarInt(dimID);
        pkt.writeVarInt(blockPos.getX());
        pkt.writeVarInt(blockPos.getY());
        pkt.writeVarInt(blockPos.getZ());
        pkt.writeString(name);
        dimensionConfig.writeToPacket(pkt);
        return pkt;
    }

    public void handleClientPacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient handler) {
        int id = packet.getType();
        if (id >= PacketIds.values().length || id < 0) {
            return;
        }

        switch (PacketIds.values()[id]) {
            case UPDATE_WORLDLIST -> handleWorldList(packet);
            case CHANGE_WORLD_SETTINGS -> {}
            case OPEN_GUI -> handleOpenGUI(packet);
        }
    }

    public void handleServerPacket(PacketCustom packet, EntityPlayerMP player, INetHandlerPlayServer handler) {
        int id = packet.getType();
        if (id >= PacketIds.values().length || id < 0) {
            return;
        }

        switch (PacketIds.values()[id]) {
            case UPDATE_WORLDLIST -> {}
            case CHANGE_WORLD_SETTINGS -> {
                int dim = packet.readVarInt();
                int x = packet.readVarInt();
                int y = packet.readVarInt();
                int z = packet.readVarInt();
                String name = packet.readString();
                DimensionConfig conf = DimensionConfig.readFromPacket(packet);
                if (player != null && player.getServerWorld() != null &&
                        player.getServerWorld().provider.getDimension() == dim) {
                    TileEntity te = player.getServerWorld().getTileEntity(new BlockPos(x, y, z));
                    if (te instanceof TilePersonalPortal tpp) {
                        tpp.updateSettings(player, conf, name);
                        tpp.sendToClient();
                    }
                }
            }
        }
    }

    public void handleOpenGUI(PacketCustom pkt) {
        int targetDim = pkt.readVarInt();
        int dimID = pkt.readVarInt();
        int x = pkt.readVarInt();
        int y = pkt.readVarInt();
        int z = pkt.readVarInt();
        String name = pkt.readString();
        ClientGUI.open(new PWGuiMUI(targetDim, dimID, x, y, z, name).createGUI());
    }

    public PacketCustom sendWorldList() {
        PacketCustom pkt = new PacketCustom(Tags.MODID, PacketIds.UPDATE_WORLDLIST.ordinal());
        synchronized (CommonProxy.getDimensionConfigs(false)) {
            pkt.writeVarInt(CommonProxy.getDimensionConfigs(false).size());
            CommonProxy.getDimensionConfigs(false).forEachEntry((dimID, dimCfg) -> {
                dimCfg.writeToPacket(pkt);
                return true;
            });
        }
        writeStringArray(pkt, PWConfig.Values.allowedBlocks);
        writeStringArray(pkt, PWConfig.Values.allowedBoundaryBlocks);
        writeStringArray(pkt, PWConfig.Values.allowedGapBlocks);
        writeStringArray(pkt, PWConfig.Values.allowedCenterBlocks);
        writeStringArray(pkt, PWConfig.Values.allowedBiomes);
        writeStringArray(pkt, PWConfig.Values.presets);
        return pkt;
    }

    private static void writeStringArray(PacketCustom pkt, String[] values) {
        pkt.writeVarInt(values.length);
        Arrays.stream(values).forEach(pkt::writeString);
    }

    private static String[] readStringArray(PacketCustom pkt) {
        int amount = pkt.readVarInt();
        ArrayList<String> tmpList = new ArrayList<>(amount);
        for (int i = 0; i < amount; ++i) {
            tmpList.add(pkt.readString());
        }
        return tmpList.toArray(new String[0]);
    }

    private static void handleWorldList(PacketCustom pkt) {
        int dimConfigs = pkt.readVarInt();
        for (int i = 0; i < dimConfigs; i++) {
            DimensionConfig cfg = DimensionConfig.readFromPacket(pkt);
            cfg.registerWithDimManager(true, false);
        }

        PWConfig.Values.allowedBlocks = readStringArray(pkt);
        PWConfig.Values.allowedBoundaryBlocks = readStringArray(pkt);
        PWConfig.Values.allowedGapBlocks = readStringArray(pkt);
        PWConfig.Values.allowedCenterBlocks = readStringArray(pkt);
        PWConfig.Values.allowedBiomes = readStringArray(pkt);
        PWConfig.Values.presets = readStringArray(pkt);
    }
}
