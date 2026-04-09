package com.drppp.drtech;

import codechicken.lib.texture.TextureUtils;
import com.drppp.drtech.Client.ClientProxy;
import com.drppp.drtech.Client.CropStickTESR;
import com.drppp.drtech.Client.TesrTimeTable;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Client.render.TileEntityRendererGravitationalAnomaly;
import com.drppp.drtech.Network.SyncInit;
import com.drppp.drtech.Tile.*;
import com.drppp.drtech.api.ItemHandler.TileEntityUIFactory;
import com.drppp.drtech.api.Utils.CustomeRecipe;
import com.drppp.drtech.api.capability.DrtechCapInit;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.Crops.CropInitHandler;
import com.drppp.drtech.common.CommonProxy;
import com.drppp.drtech.common.Items.DrtToolItems;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.common.MetaTileEntities.DrTechMetaTileEntities;
import com.drppp.drtech.common.drtMetaEntities;
import com.drppp.drtech.common.event.CommonHandler;
import com.drppp.drtech.intergations.gtfo.TileCropFarmerMode;
import com.drppp.drtech.intergations.top.TopInit;
import com.drppp.drtech.loaders.recipes.CraftingReceipe;
import com.drppp.drtech.loaders.DrTechReceipeManager;
import com.drppp.drtech.loaders.recipes.builder.DisassemblyHandler;
import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import gregtechfoodoption.machines.farmer.FarmerModeRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.drppp.drtech.Tags.MODID;

@Mod(modid = MODID, version = Tags.VERSION, name = Tags.MODNAME,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:gregtech@[2.9.0-beta,);"
)
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

    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        DrTechTab = new DrTechCreativeTabs("mytab");
        DrMetaItems.MetaItemsInit();
        DrtechCapInit.init();
        Textures.init();
        drtMetaEntities.init();
        TileEntityUIFactory.INSTANCE.init();
        DrtToolItems.init();
        DrTechMetaTileEntities.initialization();
        CropInitHandler.preInit();
        MinecraftForge.EVENT_BUS.register(new CommonHandler());

    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMTERegistry(MTEManager.MTERegistryEvent event) {
        GregTechAPI.mteManager.createRegistry(Tags.MODID);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void createMaterialRegistry(MaterialRegistryEvent event) {
        GregTechAPI.materialManager.createRegistry(Tags.MODID);
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
            ClientRegistry.bindTileEntitySpecialRenderer(TileCropStick.class, new CropStickTESR());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        TextureUtils.addIconRegister(Textures::register);
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        ItemsInit.init(event);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        BlocksInit.init(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        CraftingReceipe.load();
        DrTechReceipeManager.init();
        SyncInit.init();
        TopInit.init();
        CustomeRecipe.InitCanDoWorkMachines();
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            OBJLoader.INSTANCE.addDomain(MODID);
        }
        CropInitHandler.init();
    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void Clientinit(FMLInitializationEvent event) {
        SyncInit.init();
        drtMetaEntities.initRenderers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (DrtConfig.EnableDisassembly)
            DisassemblyHandler.buildDisassemblerRecipes();
        FarmerModeRegistry.registerFarmerMode(new TileCropFarmerMode());
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
    }
}
