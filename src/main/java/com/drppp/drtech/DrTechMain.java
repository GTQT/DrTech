package com.drppp.drtech;

import codechicken.lib.texture.TextureUtils;
import com.drppp.drtech.Client.ClientProxy;
import com.drppp.drtech.Client.render.EOH_TESR;
import com.drppp.drtech.Client.render.Items.NuclearItemsRender;
import com.drppp.drtech.Tile.TileEntityHomoEye;
import com.drppp.drtech.World.DrtDimensionType.DrtDimType;
import com.drppp.drtech.World.WordStruct.StructUtil;
import com.drppp.drtech.World.WorldRegisterHandler;
import com.drppp.drtech.api.ItemHandler.TileEntityUIFactory;
import com.drppp.drtech.api.WirelessNetwork.GlobalEnergyWorldSavedData;
import com.drppp.drtech.api.sound.SusySounds;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.Crops.CropsInit;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Client.render.LaserPipeRenderer;
import com.drppp.drtech.Client.render.TileEntityRendererGravitationalAnomaly;
import com.drppp.drtech.common.CommonProxy;
import com.drppp.drtech.common.Items.GeoItemsInit;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.MetaItems.ItemCombs;
import com.drppp.drtech.common.Items.MetaItems.MyMetaItems;
import com.drppp.drtech.common.command.CommandHordeBase;
import com.drppp.drtech.common.command.CommandHordeStart;
import com.drppp.drtech.common.command.CommandHordeStatus;
import com.drppp.drtech.common.command.CommandHordeStop;
import com.drppp.drtech.common.drtMetaEntities;
import com.drppp.drtech.common.enent.PollutionEffectHandler;
import com.drppp.drtech.intergations.Forestry.CombRecipes;
import com.drppp.drtech.intergations.Forestry.DRTAlleleBeeSpecies;
import com.drppp.drtech.intergations.Forestry.DrtBeeDefinition;
import com.drppp.drtech.intergations.top.TopInit;
import com.drppp.drtech.loaders.CraftingReceipe;
import com.drppp.drtech.loaders.DrTechReceipeManager;
import com.drppp.drtech.common.MetaTileEntities.MetaTileEntities;
import com.drppp.drtech.Sync.SyncInit;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import com.drppp.drtech.api.capability.DrtechCapInit;
import com.drppp.drtech.loaders.OrePrefixRecipes;
import com.drppp.drtech.loaders.builder.DisassemblyHandler;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

import static com.drppp.drtech.common.Items.MetaItems.MetaItemsReactor.FuelRodInit;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]")
public class DrTechMain {


    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    public static CreativeTabs Mytab;

    @Mod.Instance(DrTechMain.MODID)
    public static DrTechMain instance;


    public static final String MODID = "drtech";
    public static final String NAME = "drtech";
    public static final String VERSION = "1.0";
    @SidedProxy(modId = MODID, clientSide = "com.drppp.drtech.Client.ClientProxy", serverSide = "com.drppp.drtech.common.CommonProxy")

    public static CommonProxy proxy;
    public static ClientProxy cproxy;
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // register to the event bus so that we can listen to events
        OBJLoader.INSTANCE.addDomain(Tags.MODID);
        MinecraftForge.EVENT_BUS.register(this);
        Mytab = new MyCreativeTabs("mytab");
        MyMetaItems.MetaItemsInit();
        FuelRodInit();
        DrtechCapInit.init();
        if(Loader.isModLoaded("forestry"))
        {
             ItemCombs.init();
        }
        GeckoLib.initialize();
        Textures.init();
        drtMetaEntities.init();
        SusySounds.registerSounds();
        TileEntityUIFactory.INSTANCE.init();
        DrtDimType.init();
        WorldRegisterHandler.init();
    }
    @EventHandler
    @SideOnly(Side.CLIENT)
    // preInit "Run before anything else. Read your config, create blocks, items, etc. (Remove if not needed)
    public void ClientpreInit(FMLPreInitializationEvent event) {
        OBJLoader.INSTANCE.addDomain(Tags.MODID);
        TexturesInit();
        drtMetaEntities.initRenderers();

    }

    @SideOnly(Side.CLIENT)
    public void TexturesInit()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGravitationalAnomaly.class, new TileEntityRendererGravitationalAnomaly());
        try {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHomoEye.class, new EOH_TESR());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        TextureUtils.addIconRegister(Textures::register);
        LaserPipeRenderer.INSTANCE.preInit();
    }
    @SubscribeEvent
    // Register recipes here (Remove if not needed)
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        OrePrefixRecipes.init();
    }

    @SubscribeEvent
    // Register items here (Remove if not needed)
    public void registerItems(RegistryEvent.Register<Item> event) {
        ItemsInit.init(event);
        GeoItemsInit.init(event);
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
        DrTechReceipeManager.init();
        SyncInit.init();
        TopInit.init();
        MinecraftForge.EVENT_BUS.register(new GlobalEnergyWorldSavedData());
        MinecraftForge.EVENT_BUS.register(new PollutionEffectHandler());
        MinecraftForge.EVENT_BUS.register(new NuclearItemsRender());
        StructUtil.init();

        DRTAlleleBeeSpecies.setupAlleles();
        CombRecipes.initDRTCombs();
    }
    @SideOnly(Side.CLIENT)
    @EventHandler
    // load "Do your mod setup. Build whatever data structures you care about." (Remove if not needed)
    public void Clientinit(FMLInitializationEvent event) {
        DrtechEventHandler.Keybinds.registerKeybinds();
        SyncInit.init();
        if(Loader.isModLoaded("forestry"))
        {
            ItemCombs.ClientInit();
        }
    }
    @EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        if(DrtConfig.EnableDisassembly)
            DisassemblyHandler.buildDisassemblerRecipes();
        DrtBeeDefinition.initBees();

    }

    @EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
    }
    @Mod.EventHandler
    public void onInit( FMLInitializationEvent event) {
        proxy.load();

    }
    @Mod.EventHandler
    public void onServerStarting( FMLServerStartingEvent event) {
        CommandHordeBase hordeCommand = new CommandHordeBase();
        event.registerServerCommand(hordeCommand);

        hordeCommand.addSubcommand(new CommandHordeStart());
        hordeCommand.addSubcommand(new CommandHordeStop());
        hordeCommand.addSubcommand(new CommandHordeStatus());
    }


}
