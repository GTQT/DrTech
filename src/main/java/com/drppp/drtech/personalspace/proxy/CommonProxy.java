package com.drppp.drtech.personalspace.proxy;

import com.drppp.drtech.Tags;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import codechicken.lib.packet.ICustomPacketHandler;
import codechicken.lib.packet.PacketCustom;
import gnu.trove.map.hash.TIntObjectHashMap;
import com.drppp.drtech.personalspace.PWConfig;
import com.drppp.drtech.personalspace.PersonalSpaceModule;
import com.drppp.drtech.personalspace.blocks.BlockPersonalPortal;
import com.drppp.drtech.personalspace.blocks.tile.TilePersonalPortal;
import com.drppp.drtech.personalspace.command.PWCommand;
import com.drppp.drtech.personalspace.packet.Packets;
import com.drppp.drtech.personalspace.world.DimensionConfig;
import com.drppp.drtech.personalspace.world.PWWorldProvider;
import com.drppp.drtech.personalspace.world.WeatherSyncHandler;

public class CommonProxy {

    public static final BlockPersonalPortal blockPersonalPortal = new BlockPersonalPortal();
    public static ItemBlock itemBlockPersonalPortal;

    private final TIntObjectHashMap<DimensionConfig> clientDimensionConfigs = new TIntObjectHashMap<>();
    private final TIntObjectHashMap<DimensionConfig> serverDimensionConfigs = new TIntObjectHashMap<>();

    public static TIntObjectHashMap<DimensionConfig> getDimensionConfigs(boolean isClient) {
        if (isClient) {
            return PersonalSpaceModule.proxy.clientDimensionConfigs;
        }
        return PersonalSpaceModule.proxy.serverDimensionConfigs;
    }

    public void onPreInit(FMLPreInitializationEvent e) {
        PacketCustom.assignHandler(Tags.MODID,
                (ICustomPacketHandler.IServerPacketHandler) Packets.INSTANCE::handleServerPacket);
    }

    public void onInit(FMLInitializationEvent e) {
        PWConfig.Values.presets = PWConfig.presets;
        PWConfig.Values.allowedBiomes = PWConfig.allowedBiomes;
        PWConfig.Values.allowedBlocks = PWConfig.allowedBlocks;
        PWConfig.Values.allowedBoundaryBlocks = PWConfig.allowedBoundaryBlocks;
        PWConfig.Values.allowedGapBlocks = PWConfig.allowedGapBlocks;
        PWConfig.Values.allowedCenterBlocks = PWConfig.allowedCenterBlocks;
        MinecraftForge.EVENT_BUS.register(new WeatherSyncHandler());
    }

    public void onServerStarting(FMLServerStartingEvent e) {
        e.registerServerCommand(new PWCommand());
        loadDimensionConfigs(DimensionManager.getCurrentSaveRootDirectory());
    }

    public void onServerStopping(FMLServerStoppingEvent event) {
        TIntObjectHashMap<DimensionConfig> configs = CommonProxy.getDimensionConfigs(false);
        configs.forEachEntry((dimID, dimCFG) -> {
            if (dimCFG == null || !dimCFG.needsSaving()) {
                return true;
            }
            dimCFG.update();
            return true;
        });
    }

    public void serverStopped(FMLServerStoppedEvent event) {
        unregisterDims(false);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            unregisterDims(true);
            synchronized (CommonProxy.getDimensionConfigs(true)) {
                CommonProxy.getDimensionConfigs(true).clear();
            }
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> e) {
        itemBlockPersonalPortal = new ItemBlock(blockPersonalPortal);
        e.getRegistry().register(itemBlockPersonalPortal.setRegistryName(blockPersonalPortal.getRegistryName()));
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> e) {
        e.getRegistry().register(blockPersonalPortal);
        GameRegistry.registerTileEntity(TilePersonalPortal.class,
                new ResourceLocation("drtech:personal_portal"));
    }

    @SubscribeEvent
    public void initRecipes(RegistryEvent.Register<IRecipe> r) {
        GameRegistry.addShapedRecipe(new ResourceLocation("portal_block"), new ResourceLocation(Tags.MODID),
                new ItemStack(CommonProxy.itemBlockPersonalPortal), "QBQ", "SQS", "OOO", 'Q',
                Blocks.QUARTZ_BLOCK, 'S', Blocks.QUARTZ_STAIRS, 'O', Blocks.OBSIDIAN, 'B',
                Ingredient.fromItems(Items.BOOK));
    }

    @SubscribeEvent
    public void worldSave(WorldEvent.Save event) {
        if (!(event.getWorld().provider instanceof PWWorldProvider provider)) {
            return;
        }
        DimensionConfig cfg = provider.getConfig();
        if (cfg == null || !cfg.needsSaving()) {
            return;
        }
        cfg.update();
    }

    @SubscribeEvent
    public void netEventHandler(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        event.getManager().sendPacket(Packets.INSTANCE.sendWorldList().toPacket());
    }

    private void loadDimensionConfigs(File path) {
        unregisterDims(false);
        File file = new File(path + "/PWWorlds.dat");
        if (file.exists()) {
            NBTTagCompound configNBT = null;
            try {
                configNBT = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
            } catch (IOException ex) {
                PersonalSpaceModule.log.error("Could not read PWWorlds.dat!", ex);
            }
            if (configNBT != null) {
                int[] dimensions = configNBT.getIntArray("dimensions");
                Arrays.stream(dimensions).forEach(dimID -> {
                    try {
                        DimensionConfig dimCFG = new DimensionConfig(dimID);
                        dimCFG.registerWithDimManager(false, false);
                    } catch (Exception e) {
                        PersonalSpaceModule.log.error("Couldn't load personal dimension data!", e);
                    }
                });
            }
        }
    }

    public static void unregisterDims(boolean isClient) {
        if (CommonProxy.getDimensionConfigs(isClient).isEmpty()) {
            return;
        }
        synchronized (CommonProxy.getDimensionConfigs(isClient)) {
            CommonProxy.getDimensionConfigs(isClient).forEachEntry((dimID, dimCFG) -> {
                if (DimensionManager.isDimensionRegistered(dimID)) {
                    PersonalSpaceModule.log.info("unregistering PersonalWorld dimension {}", dimID);
                    DimensionManager.unregisterDimension(dimID);
                }
                return true;
            });
        }
        CommonProxy.getDimensionConfigs(isClient).clear();
    }

    public static class oreGenBusListener {

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void onOreGenerate(OreGenEvent.GenerateMinable event) {
            if (event.getWorld().provider instanceof PWWorldProvider) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    public static class BiomeBusListener {

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void onBiomeDecorate(DecorateBiomeEvent.Decorate event) {
            if (event.getWorld().provider instanceof PWWorldProvider provider) {
                if (!event.getType().equals(DecorateBiomeEvent.Decorate.EventType.TREE)) {
                    if (event.getType().equals(DecorateBiomeEvent.Decorate.EventType.FOSSIL) ||
                            event.getType().equals(DecorateBiomeEvent.Decorate.EventType.CUSTOM) ||
                            !provider.getConfig().vegetationEnabled()) {
                        event.setResult(Event.Result.DENY);
                    }
                } else if (!provider.getConfig().generateTrees()) {
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }
}
