package com.drppp.drtech;

import codechicken.lib.texture.TextureUtils;
import com.drppp.drtech.Client.ClientProxy;
import com.drppp.drtech.Client.TesrTimeTable;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Client.render.TileEntityRendererConnector;
import com.drppp.drtech.Client.render.TileEntityRendererGravitationalAnomaly;
import com.drppp.drtech.Network.SyncInit;
import com.drppp.drtech.Tile.*;
import com.drppp.drtech.api.ItemHandler.TileEntityUIFactory;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.api.capability.DrtechCapInit;
import com.drppp.drtech.common.Blocks.BlockComposter;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.CommonProxy;
import com.drppp.drtech.common.Items.DrtToolItems;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.common.MetaTileEntities.DrTechMetaTileEntities;
import com.drppp.drtech.common.drtMetaEntities;
import com.drppp.drtech.common.event.CommonHandler;
import com.drppp.drtech.common.world.AmethystGeodeWorldGenerator;
import com.drppp.drtech.common.world.DriedGhastWorldGenerator;
import com.drppp.drtech.hooked.HookCapability;
import com.drppp.drtech.hooked.HookClientHooks;
import com.drppp.drtech.hooked.HookNetwork;
import com.drppp.drtech.hooked.HookTickHandler;
import com.drppp.drtech.intergations.top.TopInit;
import com.meowmel.cropQT.api.CropInitHandler;
import com.meowmel.cropQT.client.CropStickTESR;
import com.meowmel.cropQT.gtfo.TileCropFarmerMode;
import com.meowmel.cropQT.tile.TileCropStick;
import com.drppp.drtech.loaders.recipes.CraftingReceipe;
import com.drppp.drtech.loaders.DrTechReceipeManager;
import com.drppp.drtech.loaders.ModRewardBoxes;
import com.drppp.drtech.loaders.recipes.builder.DisassemblyHandler;
import com.drppp.drtech.lootgames.LootGames;
import com.drppp.drtech.lootgames.api.minigame.GameManager;
import com.drppp.drtech.lootgames.api.task.TaskCreateExplosion;
import com.drppp.drtech.lootgames.api.task.TaskRegistry;
import com.drppp.drtech.lootgames.loot.ModLootTables;
import com.drppp.drtech.lootgames.world.gen.LootGamesWorldGen;
import com.drppp.drtech.lootgames.minigame.gameoflight.GameOfLight;
import com.drppp.drtech.lootgames.minigame.minesweeper.GameMineSweeper;
import com.drppp.drtech.lootgames.minigame.minesweeper.client.TESRMSMaster;
import com.drppp.drtech.lootgames.minigame.minesweeper.tileentity.TileEntityMSMaster;
import com.drppp.drtech.lootgames.minigame.gameoflight.client.TESRGOLMaster;
import com.drppp.drtech.lootgames.minigame.gameoflight.TileEntityGOLMaster;
import com.drppp.drtech.lootgames.packets.NetworkHandler;
import com.drppp.drtech.lootgames.registry.ModBlocks;
import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import gregtechfoodoption.common.machines.farmer.FarmerModeRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import static com.drppp.drtech.Tags.MODID;

@Mod(modid = MODID, version = Tags.VERSION, name = Tags.MODNAME,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:gregtech@[1.9.0,);"
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
        DrTechTab = new DrTechCreativeTabs("drtech");
        DrMetaItems.MetaItemsInit();
        DrtechCapInit.init();
        HookCapability.init();
        Textures.init();
        drtMetaEntities.init();
        TileEntityUIFactory.INSTANCE.init();
        DrtToolItems.init();
        DrTechMetaTileEntities.initialization();
        CropInitHandler.preInit();
        HookNetwork.init();
        HookTickHandler.init();
        MinecraftForge.EVENT_BUS.register(new CommonHandler());
        GameRegistry.registerWorldGenerator(new DriedGhastWorldGenerator(), 0);
        GameRegistry.registerWorldGenerator(new AmethystGeodeWorldGenerator(), 0);

        // LootGames init
        ModBlocks.registerTileEntities();
        NetworkHandler.registerPackets();
        LootGames.gameManager = new GameManager();
        ModLootTables.init();
        ModRewardBoxes.init();
        GameRegistry.registerWorldGenerator(new LootGamesWorldGen(), 0);

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
        HookClientHooks.init();

    }

    @SideOnly(Side.CLIENT)
    public void TexturesInit() {
        try {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGravitationalAnomaly.class, new TileEntityRendererGravitationalAnomaly());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnector.class, new TileEntityRendererConnector());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTimeTable.class, new TesrTimeTable());
            ClientRegistry.bindTileEntitySpecialRenderer(TileCropStick.class, new CropStickTESR());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMSMaster.class, new TESRMSMaster());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGOLMaster.class, new TESRGOLMaster());
        TextureUtils.addIconRegister(Textures::register);
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    }



    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        BlocksInit.init(event);
        ModBlocks.registerBlocks(event);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        ItemsInit.init(event);
        ModBlocks.registerItemBlocks(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        CraftingReceipe.load();
        DrTechReceipeManager.init();
        SyncInit.init();
        TopInit.init();
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            OBJLoader.INSTANCE.addDomain(MODID);
        }
        CropInitHandler.init();
        BlockComposter.registerDispenseBehaviors();

        // LootGames game & task registration
        LootGames.gameManager.registerGame(GameOfLight.class, new GameOfLight.Factory());
        LootGames.gameManager.registerGame(GameMineSweeper.class, new GameMineSweeper.Factory());
        TaskRegistry.registerTask(TaskCreateExplosion.class);
        TaskRegistry.registerTask(com.drppp.drtech.lootgames.minigame.minesweeper.task.TaskMSCreateExplosion.class);
    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void Clientinit(FMLInitializationEvent event) {
        SyncInit.init();
        drtMetaEntities.initRenderers();
        CropInitHandler.clienInit();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (DrtConfig.EnableDisassembly)
            DisassemblyHandler.buildDisassemblerRecipes();
        FarmerModeRegistry.registerFarmerMode(new TileCropFarmerMode());
        DrtechUtils.initCropsList();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
    }
}
