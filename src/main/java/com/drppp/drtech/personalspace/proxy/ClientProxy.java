package com.drppp.drtech.personalspace.proxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import com.drppp.drtech.Tags;
import com.drppp.drtech.personalspace.PWConfig;
import com.drppp.drtech.personalspace.blocks.tile.TilePersonalPortal;
import com.drppp.drtech.personalspace.blocks.tile.TilePersonalPortalSpecialRender;
import com.drppp.drtech.personalspace.packet.Packets;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MODID)
public class ClientProxy extends CommonProxy {

    @Override
    public void onPreInit(FMLPreInitializationEvent e) {
        super.onPreInit(e);
        PacketCustom.assignHandler(Tags.MODID,
                (ICustomPacketHandler.IClientPacketHandler) Packets.INSTANCE::handleClientPacket);
    }

    @Override
    public void onInit(FMLInitializationEvent e) {
        super.onInit(e);
        ClientRegistry.bindTileEntitySpecialRenderer(TilePersonalPortal.class, new TilePersonalPortalSpecialRender());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemBlockPersonalPortal, 0,
                new ModelResourceLocation(blockPersonalPortal.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public static void clientDisconnectionHandler(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        PWConfig.Values.presets = PWConfig.presets;
        PWConfig.Values.allowedBiomes = PWConfig.allowedBiomes;
        PWConfig.Values.allowedBlocks = PWConfig.allowedBlocks;
        PWConfig.Values.allowedBoundaryBlocks = PWConfig.allowedBoundaryBlocks;
        PWConfig.Values.allowedCenterBlocks = PWConfig.allowedCenterBlocks;
        PWConfig.Values.allowedGapBlocks = PWConfig.allowedGapBlocks;
        unregisterDims(true);
    }
}
