package com.drppp.drtech;

import codechicken.lib.texture.TextureUtils;
import com.drppp.drtech.Client.ClientProxy;
import com.drppp.drtech.Client.TesrTimeTable;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Client.render.TesrStoneAxle;
import com.drppp.drtech.Client.render.TesrWaterMill;
import com.drppp.drtech.Client.render.LaserPipeRenderer;
import com.drppp.drtech.Client.render.TileEntityRendererGravitationalAnomaly;
import com.drppp.drtech.Network.SyncInit;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import com.drppp.drtech.Tile.TileEntityStoneAxle;
import com.drppp.drtech.Tile.TileEntityWaterMill;
import com.drppp.drtech.Tile.TileEntityTimeTable;
import com.drppp.drtech.World.DrtDimensionType.DrtDimType;
import com.drppp.drtech.World.WordStruct.StructUtil;
import com.drppp.drtech.World.WorldRegisterHandler;
import com.drppp.drtech.api.ItemHandler.TileEntityUIFactory;
import com.drppp.drtech.api.Utils.CustomeRecipe;
import com.drppp.drtech.api.WirelessNetwork.GlobalEnergyWorldSavedData;
import com.drppp.drtech.api.capability.DrtechCapInit;
import com.drppp.drtech.command.CommandTeleportDimension;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.Crops.CropsInit;
import com.drppp.drtech.common.CommonProxy;
import com.drppp.drtech.common.Items.DrtToolItems;
import com.drppp.drtech.common.Items.GeoItemsInit;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.MetaItems.ItemCombs;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.common.MetaTileEntities.MetaTileEntities;
import com.drppp.drtech.common.covers.DrtCoverReg;
import com.drppp.drtech.common.drtMetaEntities;
import com.drppp.drtech.common.event.PollutionEffectHandler;
import com.drppp.drtech.common.event.RocketFuelHUD;
import com.drppp.drtech.intergations.Forestry.CombRecipes;
import com.drppp.drtech.intergations.Forestry.DRTAlleleBeeSpecies;
import com.drppp.drtech.intergations.Forestry.DrtBeeDefinition;
import com.drppp.drtech.intergations.top.TopInit;
import com.drppp.drtech.loaders.CraftingReceipe;
import com.drppp.drtech.loaders.DrTechReceipeManager;
import com.drppp.drtech.loaders.OrePrefixRecipes;
import com.drppp.drtech.loaders.builder.DisassemblyHandler;
import com.drppp.drtech.loaders.misc.GendustryRecipes;
import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverDefinition;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
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
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

import static com.drppp.drtech.Tags.MODID;
import static com.drppp.drtech.common.Items.MetaItems.MetaItemsReactor.FuelRodInit;

@Mod(modid = MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required:genetics@[2.5.1.203,);")
public class DrTechMain {
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static CreativeTabs DrTechTab;
    @Mod.Instance(MODID)
    public static DrTechMain instance;
    @SidedProxy(modId = MODID, clientSide = "com.drppp.drtech.Client.ClientProxy", serverSide = "com.drppp.drtech.common.CommonProxy")
    public static CommonProxy proxy;
    public static ClientProxy cproxy;

    @SubscribeEvent
    public static void registerCoverBehavior(GregTechAPI.RegisterEvent<CoverDefinition> event) {
        DrtCoverReg.init();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        DrTechTab = new MyCreativeTabs("mytab");
        DrMetaItems.MetaItemsInit();
        FuelRodInit();
        DrtechCapInit.init();
        GeckoLib.initialize();
        Textures.init();
        drtMetaEntities.init();
        TileEntityUIFactory.INSTANCE.init();
        DrtDimType.init();
        WorldRegisterHandler.init();
        DrtToolItems.init();
        if (Loader.isModLoaded("forestry")) {
            ItemCombs.init();
        }
    }

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void ClientpreInit(FMLPreInitializationEvent event) {
        TexturesInit();
        drtMetaEntities.initRenderers();

    }

    @SideOnly(Side.CLIENT)
    public void TexturesInit() {
        try {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGravitationalAnomaly.class, new TileEntityRendererGravitationalAnomaly());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTimeTable.class, new TesrTimeTable());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWaterMill.class, new TesrWaterMill());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStoneAxle.class, new TesrStoneAxle());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        TextureUtils.addIconRegister(Textures::register);
        LaserPipeRenderer.INSTANCE.preInit();
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        OrePrefixRecipes.init();
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        ItemsInit.init(event);
        GeoItemsInit.init(event);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        BlocksInit.init(event);
        CropsInit.init(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MetaTileEntities.Init();
        CraftingReceipe.load();
        DrTechReceipeManager.init();
        SyncInit.init();
        TopInit.init();
        MinecraftForge.EVENT_BUS.register(new GlobalEnergyWorldSavedData());
        MinecraftForge.EVENT_BUS.register(new PollutionEffectHandler());
        StructUtil.init();
        DRTAlleleBeeSpecies.setupAlleles();
        CombRecipes.initDRTCombs();
        CustomeRecipe.InitCanDoWorkMachines();

        if (FMLLaunchHandler.side() == Side.CLIENT) {
            OBJLoader.INSTANCE.addDomain(MODID);
        }

    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void Clientinit(FMLInitializationEvent event) {
        DrtechEventHandler.Keybinds.registerKeybinds();
        SyncInit.init();
        if (Loader.isModLoaded("forestry")) {
            ItemCombs.ClientInit();
        }
        drtMetaEntities.initRenderers();
        MinecraftForge.EVENT_BUS.register(new RocketFuelHUD());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (DrtConfig.MachineSwitch.EnableDisassembly)
            DisassemblyHandler.buildDisassemblerRecipes();
        DrtBeeDefinition.initBees();
        GendustryRecipes.init();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTeleportDimension());
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
    }

}
