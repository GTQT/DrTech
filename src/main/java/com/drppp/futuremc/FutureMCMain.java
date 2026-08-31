package com.drppp.futuremc;

import com.drppp.futuremc.blocks.TileEntityComposter;
import com.drppp.futuremc.blocks.BlockAmethystCluster;
import com.drppp.futuremc.blocks.BlockBuddingAmethyst;
import com.drppp.futuremc.blocks.BlockBubbleColumn;
import com.drppp.futuremc.blocks.BlockChain;
import com.drppp.futuremc.blocks.BlockComposter;
import com.drppp.futuremc.blocks.BlockDriedGhast;
import com.drppp.futuremc.blocks.BlockLantern;
import com.drppp.futuremc.blocks.BlockSimpleDrTech;
import com.drppp.futuremc.blocks.TileEntityDriedGhast;
import com.drppp.futuremc.client.render.entity.RenderHappyGhast;
import com.drppp.futuremc.entity.EntityHappyGhast;
import com.drppp.futuremc.items.ItemHappyGhastHarness;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
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
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Vanilla blocks introduced after 1.12.2, exposed under a stable standalone namespace. */
@Mod(modid = FutureMCMain.MODID, name = "Future MC", version = "1.0.0", acceptedMinecraftVersions = "[1.12.2]")
@Mod.EventBusSubscriber(modid = FutureMCMain.MODID)
public final class FutureMCMain {
    public static final String MODID = "futuremc";
    public static final CreativeTabs TAB = new FutureMCCreativeTabs();
    @Mod.Instance(MODID)
    public static FutureMCMain instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new com.drppp.futuremc.world.AmethystGeodeWorldGenerator(), 0);
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

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        BlockComposter.registerDispenseBehaviors();
    }

    private static Block id(Block block, String name) {
        block.setCreativeTab(TAB);
        block.setTranslationKey(MODID + "." + name);
        return block;
    }

    public static final Item AMETHYST_SHARD = new Item().setCreativeTab(TAB).setRegistryName(MODID, "amethyst_shard").setTranslationKey(MODID + ".amethyst_shard");
    public static final Block SMOOTH_BASALT = id(new BlockSimpleDrTech("smooth_basalt", Material.ROCK, SoundType.STONE, 1.25F, 4.2F), "smooth_basalt");
    public static final Block CALCITE = id(new BlockSimpleDrTech("calcite", Material.ROCK, SoundType.STONE, 0.75F, 0.75F), "calcite");
    public static final Block AMETHYST_BLOCK = id(new BlockSimpleDrTech("amethyst_block", Material.ROCK, SoundType.GLASS, 1.5F, 1.5F), "amethyst_block");
    public static final Block BUDDING_AMETHYST = id(new BlockBuddingAmethyst(), "budding_amethyst");
    public static final Block SMALL_AMETHYST_BUD = id(new BlockAmethystCluster("small_amethyst_bud", 6, 3, 1, false), "small_amethyst_bud");
    public static final Block MEDIUM_AMETHYST_BUD = id(new BlockAmethystCluster("medium_amethyst_bud", 5, 4, 2, false), "medium_amethyst_bud");
    public static final Block LARGE_AMETHYST_BUD = id(new BlockAmethystCluster("large_amethyst_bud", 4, 5, 4, false), "large_amethyst_bud");
    public static final Block AMETHYST_CLUSTER = id(new BlockAmethystCluster("amethyst_cluster", 3, 7, 5, true), "amethyst_cluster");
    public static final Block LANTERN = id(new BlockLantern("lantern", 15), "lantern");
    public static final Block SOUL_LANTERN = id(new BlockLantern("soul_lantern", 10), "soul_lantern");
    public static final Block CHAIN = id(new BlockChain(), "chain");
    public static final Block COMPOSTER = id(new BlockComposter(), "composter");
    public static final Block BUBBLE_COLUMN = new BlockBubbleColumn();
    public static final Block DRIED_GHAST = id(new BlockDriedGhast(), "dried_ghast");
    public static final Item HAPPY_GHAST_HARNESS = new ItemHappyGhastHarness();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(SMOOTH_BASALT, CALCITE, AMETHYST_BLOCK, BUDDING_AMETHYST,
                SMALL_AMETHYST_BUD, MEDIUM_AMETHYST_BUD, LARGE_AMETHYST_BUD, AMETHYST_CLUSTER,
                LANTERN, SOUL_LANTERN, CHAIN, COMPOSTER, BUBBLE_COLUMN, DRIED_GHAST);
        GameRegistry.registerTileEntity(TileEntityComposter.class, new ResourceLocation(MODID, "composter"));
        GameRegistry.registerTileEntity(TileEntityDriedGhast.class, new ResourceLocation(MODID, "dried_ghast"));
        GameRegistry.registerTileEntity(LegacyComposterTile.class, new ResourceLocation("drtech", "composter"));
        GameRegistry.registerTileEntity(LegacyDriedGhastTile.class, new ResourceLocation("drtech", "dried_ghast"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Block block : new Block[] {SMOOTH_BASALT, CALCITE, AMETHYST_BLOCK, BUDDING_AMETHYST,
                SMALL_AMETHYST_BUD, MEDIUM_AMETHYST_BUD, LARGE_AMETHYST_BUD, AMETHYST_CLUSTER,
                LANTERN, SOUL_LANTERN, CHAIN, COMPOSTER, DRIED_GHAST}) {
            event.getRegistry().register(new ItemBlock(block).setCreativeTab(TAB).setRegistryName(block.getRegistryName()));
        }
        event.getRegistry().registerAll(AMETHYST_SHARD, HAPPY_GHAST_HARNESS);
        OreDictionary.registerOre("stoneBasalt", SMOOTH_BASALT);
        OreDictionary.registerOre("stoneCalcite", CALCITE);
        OreDictionary.registerOre("gemAmethyst", AMETHYST_SHARD);
        OreDictionary.registerOre("gemChippedAmethyst", AMETHYST_SHARD);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new ShapedOreRecipe(new ResourceLocation(MODID, "composter"),
                new ItemStack(COMPOSTER), "S S", "S S", "SSS",
                'S', new ItemStack(Blocks.WOODEN_SLAB, 1, 0))
                .setRegistryName(MODID, "composter"));
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
            case "smooth_basalt": return SMOOTH_BASALT;
            case "calcite": return CALCITE;
            case "amethyst_block": return AMETHYST_BLOCK;
            case "budding_amethyst": return BUDDING_AMETHYST;
            case "small_amethyst_bud": return SMALL_AMETHYST_BUD;
            case "medium_amethyst_bud": return MEDIUM_AMETHYST_BUD;
            case "large_amethyst_bud": return LARGE_AMETHYST_BUD;
            case "amethyst_cluster": return AMETHYST_CLUSTER;
            case "lantern": return LANTERN;
            case "soul_lantern": return SOUL_LANTERN;
            case "chain": return CHAIN;
            case "composter": return COMPOSTER;
            case "bubble_column": return BUBBLE_COLUMN;
            case "dried_ghast": return DRIED_GHAST;
            default: return null;
        }
    }

    private static Item legacyItemTarget(String path) {
        String normalized = path.startsWith("future_") ? path.substring("future_".length()) : path;
        if ("amethyst_shard".equals(normalized)) {
            return AMETHYST_SHARD;
        }
        if ("happy_ghast_harness".equals(normalized)) {
            return HAPPY_GHAST_HARNESS;
        }
        Block block = legacyBlockTarget(normalized);
        if (block == null || block == BUBBLE_COLUMN) {
            return null;
        }
        Item item = Item.getItemFromBlock(block);
        return item == Items.AIR ? null : item;
    }

    /** Tile aliases allow old DrTech worlds to load after the namespace move. */
    public static final class LegacyComposterTile extends TileEntityComposter {}
    public static final class LegacyDriedGhastTile extends TileEntityDriedGhast {}
}
