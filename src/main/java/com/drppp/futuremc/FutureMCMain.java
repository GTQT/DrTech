package com.drppp.futuremc;

import com.drppp.futuremc.blocks.BlockDriedGhast;
import com.drppp.futuremc.blocks.TileEntityDriedGhast;
import com.drppp.futuremc.client.render.entity.RenderHappyGhast;
import com.drppp.futuremc.entity.EntityHappyGhast;
import com.drppp.futuremc.items.ItemHappyGhastHarness;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Vanilla blocks introduced after 1.12.2, exposed under a stable standalone namespace. */
@Mod(modid = FutureMCMain.MODID, name = "Future MC", version = "1.0.0", acceptedMinecraftVersions = "[1.12.2]")
@Mod.EventBusSubscriber(modid = FutureMCMain.MODID)
public final class FutureMCMain {
    public static final String MODID = "qtfuturemc";
    public static final CreativeTabs TAB = new FutureMCCreativeTabs();
    @Mod.Instance(MODID)
    public static FutureMCMain instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new com.drppp.futuremc.world.DriedGhastWorldGenerator(), 0);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "happy_ghast"), EntityHappyGhast.class,
                "happy_ghast", 1, instance, 96, 3, true);
        EntityRegistry.registerEgg(new ResourceLocation(MODID, "happy_ghast"), 0xF4F4F4, 0x7ED6E7);
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void clientPreInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityHappyGhast.class, RenderHappyGhast::new);
    }

    private static Block id(Block block, String name) {
        block.setCreativeTab(TAB);
        block.setTranslationKey(MODID + "." + name);
        return block;
    }

    public static final Block DRIED_GHAST = id(new BlockDriedGhast(), "dried_ghast");
    public static final Item HAPPY_GHAST_HARNESS = new ItemHappyGhastHarness();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(DRIED_GHAST);
        GameRegistry.registerTileEntity(TileEntityDriedGhast.class, new ResourceLocation(MODID, "dried_ghast"));
        GameRegistry.registerTileEntity(LegacyDriedGhastTile.class, new ResourceLocation("drtech", "dried_ghast"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Block block : new Block[] {DRIED_GHAST}) {
            event.getRegistry().register(new ItemBlock(block).setCreativeTab(TAB).setRegistryName(block.getRegistryName()));
        }
        event.getRegistry().register(HAPPY_GHAST_HARNESS);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new ShapedOreRecipe(new ResourceLocation(MODID, "happy_ghast_harness"),
                new ItemStack(HAPPY_GHAST_HARNESS), "LGL", "LWL", " L ",
                'L', Items.LEATHER, 'G', Blocks.GLASS, 'W', new ItemStack(Blocks.WOOL, 1, 0))
                .setRegistryName(MODID, "happy_ghast_harness"));
    }

    @SubscribeEvent
    public static void remapLegacyBlocks(RegistryEvent.MissingMappings<Block> event) {
        for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()) {
            if ("drtech".equals(mapping.key.getNamespace())) {
                Block target = legacyBlockTarget(mapping.key.getPath());
                if (target != null) {
                    mapping.remap(target);
                }
            }
        }
    }

    @SubscribeEvent
    public static void remapLegacyItems(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
            if ("drtech".equals(mapping.key.getNamespace())) {
                Item target = legacyItemTarget(mapping.key.getPath());
                if (target != null) {
                    mapping.remap(target);
                }
            }
        }
    }

    @SubscribeEvent
    public static void remapLegacyEntities(RegistryEvent.MissingMappings<EntityEntry> event) {
        for (RegistryEvent.MissingMappings.Mapping<EntityEntry> mapping : event.getAllMappings()) {
            if (new ResourceLocation("drtech", "happy_ghast").equals(mapping.key)) {
                EntityEntry target = event.getRegistry().getValue(new ResourceLocation(MODID, "happy_ghast"));
                if (target != null) {
                    mapping.remap(target);
                }
            }
        }
    }

    private static Block legacyBlockTarget(String path) {
        String normalized = path.startsWith("future_") ? path.substring("future_".length()) : path;
        switch (normalized) {
            case "dried_ghast": return DRIED_GHAST;
            default: return null;
        }
    }

    private static Item legacyItemTarget(String path) {
        String normalized = path.startsWith("future_") ? path.substring("future_".length()) : path;
        if ("happy_ghast_harness".equals(normalized)) {
            return HAPPY_GHAST_HARNESS;
        }
        Block block = legacyBlockTarget(normalized);
        if (block == null) {
            return null;
        }
        Item item = Item.getItemFromBlock(block);
        return item == Items.AIR ? null : item;
    }

    /** Tile aliases allow old DrTech worlds to load after the namespace move. */
    public static final class LegacyDriedGhastTile extends TileEntityDriedGhast {}
}
