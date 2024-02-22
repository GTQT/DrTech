package com.drppp.drtech;

import codechicken.lib.texture.TextureUtils;
import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.Crops.CropsInit;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Client.render.LaserPipeRenderer;
import com.drppp.drtech.Client.render.TileEntityRendererGravitationalAnomaly;
import com.drppp.drtech.Items.ItemsInit;
import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import com.drppp.drtech.Load.CraftingReceipe;
import com.drppp.drtech.Load.DrTechReceipeManager;
import com.drppp.drtech.Materials.DrtechMaterials;
import com.drppp.drtech.MetaTileEntities.MetaTileEntities;
import com.drppp.drtech.Sync.SyncInit;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]")
public class DrTechMain {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    public static CreativeTabs Mytab;
    @EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc. (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        // register to the event bus so that we can listen to events
        MinecraftForge.EVENT_BUS.register(this);
        Mytab = new MyCreativeTabs("mytab");
        MyMetaItems.MetaItemsInit();
    }
    @EventHandler
    @SideOnly(Side.CLIENT)
    // preInit "Run before anything else. Read your config, create blocks, items, etc. (Remove if not needed)
    public void ClientpreInit(FMLPreInitializationEvent event) {
        TexturesInit();
    }
    @SideOnly(Side.CLIENT)
    public void TexturesInit()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGravitationalAnomaly.class, new TileEntityRendererGravitationalAnomaly());
        TextureUtils.addIconRegister(Textures::register);
        Textures.init();
        LaserPipeRenderer.INSTANCE.preInit();
    }
    @SubscribeEvent
    // Register recipes here (Remove if not needed)
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        DrTechReceipeManager.init();
    }

    @SubscribeEvent
    // Register items here (Remove if not needed)
    public void registerItems(RegistryEvent.Register<Item> event) {
        ItemsInit.init(event);

    }
    @SubscribeEvent
    // Register blocks here (Remove if not needed)
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        BlocksInit.init(event);
        CropsInit.init(event);
    }
    @EventHandler
    // load "Do your mod setup. Build whatever data structures you care about." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        MetaTileEntities.Init();
        CraftingReceipe.load();
        SyncInit.init();
    }

    @EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
    }

    @EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
    }



}
