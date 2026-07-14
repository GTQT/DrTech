package com.drppp.drtech.common.Items;

import com.drppp.drtech.Tags;
import com.drppp.drtech.Client.render.Items.RenderItemLightsaber;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.Items.lightsaber.LightsaberColor;
import com.drppp.drtech.common.Items.foods.ItemSoarXpBerry;
import com.drppp.drtech.common.Items.foods.ItemXpBerry;
import com.drppp.drtech.hooked.HookComponentType;
import com.drppp.drtech.hooked.HookRegistry;
import com.drppp.drtech.hooked.HookType;
import com.meowmel.cropQT.item.ItemCropAnalyzer;
import com.meowmel.cropQT.item.ItemCropSeed;
import com.meowmel.cropQT.item.ItemWeedingShears;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

import static com.drppp.drtech.common.Blocks.BlocksInit.CROP_STICK;
import static gregtech.common.blocks.MetaBlocks.statePropertiesToString;

public class ItemsInit {
    public static final Item ITEM_BLOCK_GRAVITATIONAL_ANOMALY = new ItemBlock(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY).setRegistryName(Tags.MODID, "gravitational_anomaly");
    public static final Item ITEM_CONNECTOR1 = new ItemBlock(BlocksInit.BLOCK_CONNECTOR1).setRegistryName(Tags.MODID, "connector_1");
    public static final Item ITEM_CONNECTOR2 = new ItemBlock(BlocksInit.BLOCK_CONNECTOR2).setRegistryName(Tags.MODID, "connector_2");
    public static final Item ITEM_CONNECTOR3 = new ItemBlock(BlocksInit.BLOCK_CONNECTOR3).setRegistryName(Tags.MODID, "connector_3");
    public static final Item ITEM_BLOCK_GOLDEN_SEA = new ItemBlock(BlocksInit.BLOCK_GOLDEN_SEA).setRegistryName(Tags.MODID, "golden_sea");
    public static final Item ITEM_BLOCK_DRIED_GHAST = new ItemBlock(BlocksInit.BLOCK_DRIED_GHAST).setRegistryName(Tags.MODID, "dried_ghast");
    public static final Item ITEM_BLOCK_SMOOTH_BASALT = new ItemBlock(BlocksInit.BLOCK_SMOOTH_BASALT).setRegistryName(Tags.MODID, "smooth_basalt");
    public static final Item ITEM_BLOCK_CALCITE = new ItemBlock(BlocksInit.BLOCK_CALCITE).setRegistryName(Tags.MODID, "calcite");
    public static final Item ITEM_BLOCK_AMETHYST_BLOCK = new ItemBlock(BlocksInit.BLOCK_AMETHYST_BLOCK).setRegistryName(Tags.MODID, "amethyst_block");
    public static final Item ITEM_BLOCK_BUDDING_AMETHYST = new ItemBlock(BlocksInit.BLOCK_BUDDING_AMETHYST).setRegistryName(Tags.MODID, "budding_amethyst");
    public static final Item ITEM_BLOCK_SMALL_AMETHYST_BUD = new ItemBlock(BlocksInit.BLOCK_SMALL_AMETHYST_BUD).setRegistryName(Tags.MODID, "small_amethyst_bud");
    public static final Item ITEM_BLOCK_MEDIUM_AMETHYST_BUD = new ItemBlock(BlocksInit.BLOCK_MEDIUM_AMETHYST_BUD).setRegistryName(Tags.MODID, "medium_amethyst_bud");
    public static final Item ITEM_BLOCK_LARGE_AMETHYST_BUD = new ItemBlock(BlocksInit.BLOCK_LARGE_AMETHYST_BUD).setRegistryName(Tags.MODID, "large_amethyst_bud");
    public static final Item ITEM_BLOCK_AMETHYST_CLUSTER = new ItemBlock(BlocksInit.BLOCK_AMETHYST_CLUSTER).setRegistryName(Tags.MODID, "amethyst_cluster");
    public static final Item ITEM_BLOCK_LANTERN = new ItemBlock(BlocksInit.BLOCK_LANTERN).setRegistryName(Tags.MODID, "lantern");
    public static final Item ITEM_BLOCK_SOUL_LANTERN = new ItemBlock(BlocksInit.BLOCK_SOUL_LANTERN).setRegistryName(Tags.MODID, "soul_lantern");
    public static final Item ITEM_BLOCK_CHAIN = new ItemBlock(BlocksInit.BLOCK_CHAIN).setRegistryName(Tags.MODID, "chain");
    public static final Item ITEM_BLOCK_COMPOSTER = new ItemBlock(BlocksInit.BLOCK_COMPOSTER).setRegistryName(Tags.MODID, "composter");
    public static final Item ITEM_BLOCK_PEACEFUL_TABLE = new ItemBlock(BlocksInit.BLOCK_PEACEFUL_TABLE).setRegistryName(Tags.MODID, "peaceful_table");
    public static final Item ITEM_BLOCK_STORAGE_PAIL = new ItemBlock(BlocksInit.BLOCK_STORAGE_PAIL).setRegistryName(Tags.MODID, BlocksInit.BLOCK_STORAGE_PAIL.getRegistryName().getPath());
    public static final Item ITEM_BLOCK_WASTE_DIRT = new ItemBlock(BlocksInit.BLOCK_WASTE_DIRT).setRegistryName(Tags.MODID, BlocksInit.BLOCK_WASTE_DIRT.getRegistryName().getPath());
    public static final Item ITEM_BLOCK_ADVANCED_CAULDRON = new ItemBlock(BlocksInit.BLOCK_ADVANCED_CAULDRON).setRegistryName(Tags.MODID, BlocksInit.BLOCK_ADVANCED_CAULDRON.getRegistryName().getPath());
    public static final Item ITEM_BLOCK_TIME_TABLE = new ItemBlock(BlocksInit.BLOCK_TIME_TABLE).setRegistryName(Tags.MODID, BlocksInit.BLOCK_TIME_TABLE.getRegistryName().getPath());
    public static final ItemSimpleDrTech AMETHYST_SHARD = new ItemSimpleDrTech("amethyst_shard");
    public static ItemCropSeed CROP_SEED = new ItemCropSeed();
    public static ItemCropAnalyzer CROP_ANALYZER = new ItemCropAnalyzer();
    public static ItemWeedingShears ITEM_WEEDING_SHEARS = new ItemWeedingShears();
    public static ItemXpBerry ITEM_XP_BERRY = new ItemXpBerry();
    public static ItemSoarXpBerry ITEM_SOAR_XP_BERRY = new ItemSoarXpBerry();
    public static ItemHappyGhastHarness HAPPY_GHAST_HARNESS = new ItemHappyGhastHarness();
    public static final ItemLightsaber LIGHTSABER = new ItemLightsaber();
    public static final Item HOOK_ITEM = HookRegistry.HOOK_ITEM;
    public static final Item HOOK_COMPONENT_ITEM = HookRegistry.COMPONENT_ITEM;

    public static void init(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ITEM_BLOCK_GRAVITATIONAL_ANOMALY);
        event.getRegistry().register(ITEM_CONNECTOR1);
        event.getRegistry().register(ITEM_CONNECTOR2);
        event.getRegistry().register(ITEM_CONNECTOR3);
        event.getRegistry().register(ITEM_BLOCK_GOLDEN_SEA);
        event.getRegistry().register(ITEM_BLOCK_DRIED_GHAST);
        event.getRegistry().register(ITEM_BLOCK_SMOOTH_BASALT);
        event.getRegistry().register(ITEM_BLOCK_CALCITE);
        event.getRegistry().register(ITEM_BLOCK_AMETHYST_BLOCK);
        event.getRegistry().register(ITEM_BLOCK_BUDDING_AMETHYST);
        event.getRegistry().register(ITEM_BLOCK_SMALL_AMETHYST_BUD);
        event.getRegistry().register(ITEM_BLOCK_MEDIUM_AMETHYST_BUD);
        event.getRegistry().register(ITEM_BLOCK_LARGE_AMETHYST_BUD);
        event.getRegistry().register(ITEM_BLOCK_AMETHYST_CLUSTER);
        event.getRegistry().register(ITEM_BLOCK_LANTERN);
        event.getRegistry().register(ITEM_BLOCK_SOUL_LANTERN);
        event.getRegistry().register(ITEM_BLOCK_CHAIN);
        event.getRegistry().register(ITEM_BLOCK_COMPOSTER);
        event.getRegistry().register(ITEM_BLOCK_PEACEFUL_TABLE);
        event.getRegistry().register(ITEM_BLOCK_STORAGE_PAIL);
        event.getRegistry().register(ITEM_BLOCK_WASTE_DIRT);
        event.getRegistry().register(ITEM_BLOCK_ADVANCED_CAULDRON);
        event.getRegistry().register(ITEM_BLOCK_TIME_TABLE);
        event.getRegistry().register(createItemBlock(BlocksInit.TRANSPARENT_CASING1, VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.COMMON_CASING, VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.COMMON_CASING1, VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.FUSION_REACTOR_CASING, VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.YOT_TANK, VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.TFFT_TANK, VariantItemBlock::new));
        event.getRegistry().register(AMETHYST_SHARD);
        event.getRegistry().register(CROP_SEED);
        event.getRegistry().register(CROP_ANALYZER);
        event.getRegistry().register(ITEM_WEEDING_SHEARS);
        event.getRegistry().register(ITEM_XP_BERRY);
        event.getRegistry().register(ITEM_SOAR_XP_BERRY);
        event.getRegistry().register(HAPPY_GHAST_HARNESS);
        event.getRegistry().register(LIGHTSABER);
        event.getRegistry().register(HOOK_ITEM);
        event.getRegistry().register(HOOK_COMPONENT_ITEM);
        event.getRegistry().register(new ItemBlock(CROP_STICK).setRegistryName(CROP_STICK.getRegistryName()));
        registerOreDicts();
    }

    private static void registerOreDicts() {
        OreDictUnifier.registerOre(new ItemStack(ITEM_BLOCK_SMOOTH_BASALT), OrePrefix.stone, Materials.Basalt);
        OreDictUnifier.registerOre(new ItemStack(ITEM_BLOCK_CALCITE), OrePrefix.stone, Materials.Calcite);
        OreDictUnifier.registerOre(new ItemStack(AMETHYST_SHARD), OrePrefix.gem, Materials.Amethyst);
        OreDictUnifier.registerOre(new ItemStack(AMETHYST_SHARD), OrePrefix.gemChipped, Materials.Amethyst);
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(BlocksInit.TRANSPARENT_CASING1);
        registerItemModel(BlocksInit.COMMON_CASING);
        registerItemModel(BlocksInit.COMMON_CASING1);
        registerItemModel(BlocksInit.FUSION_REACTOR_CASING);
        registerItemModel(BlocksInit.YOT_TANK);
        registerItemModel(BlocksInit.TFFT_TANK);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CROP_STICK), 0, new ModelResourceLocation(CROP_STICK.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(AMETHYST_SHARD, 0, new ModelResourceLocation(AMETHYST_SHARD.getRegistryName(), "inventory"));
        ModelLoader.setCustomMeshDefinition(CROP_SEED, new ItemCropSeed.SeedMeshDefinition());
        // 种子袋变体模型(含默认 + 8个自定义，硬编码避免依赖CropRegistry时序)
        ModelLoader.registerItemVariants(CROP_SEED,
                new ModelResourceLocation(CROP_SEED.getRegistryName(), "inventory"),
                new ModelResourceLocation(Tags.MODID + ":crop_seed_oreberry", "inventory"),
                new ModelResourceLocation(Tags.MODID + ":crop_seed_flower", "inventory"),
                new ModelResourceLocation(Tags.MODID + ":crop_seed_grains", "inventory"),
                new ModelResourceLocation(Tags.MODID + ":crop_seed_magic", "inventory"),
                new ModelResourceLocation(Tags.MODID + ":crop_seed_spore", "inventory"),
                new ModelResourceLocation(Tags.MODID + ":crop_seed_bonsai", "inventory"),
                new ModelResourceLocation(Tags.MODID + ":crop_seed_botania", "inventory"),
                new ModelResourceLocation(Tags.MODID + ":crop_seed_vanilla", "inventory"));
        ModelLoader.setCustomModelResourceLocation(CROP_ANALYZER, 0, new ModelResourceLocation(CROP_ANALYZER.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_WEEDING_SHEARS, 0, new ModelResourceLocation(ITEM_WEEDING_SHEARS.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_XP_BERRY, 0, new ModelResourceLocation(ITEM_XP_BERRY.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_SOAR_XP_BERRY, 0, new ModelResourceLocation(ITEM_SOAR_XP_BERRY.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(HAPPY_GHAST_HARNESS, 0, new ModelResourceLocation(HAPPY_GHAST_HARNESS.getRegistryName(), "inventory"));
        LIGHTSABER.setTileEntityItemStackRenderer(new RenderItemLightsaber());
        for (LightsaberColor color : LightsaberColor.values()) {
            ModelLoader.setCustomModelResourceLocation(LIGHTSABER, color.getMetadata(),
                    new ModelResourceLocation(LIGHTSABER.getRegistryName(), "inventory"));
        }
        for (HookType type : HookType.values()) {
            ModelLoader.setCustomModelResourceLocation(HOOK_ITEM, type.ordinal(),
                    new ModelResourceLocation(Tags.MODID + ":hook_" + type.name().toLowerCase(), "inventory"));
        }
        for (HookComponentType type : HookComponentType.values()) {
            ModelLoader.setCustomModelResourceLocation(HOOK_COMPONENT_ITEM, type.ordinal(),
                    new ModelResourceLocation(Tags.MODID + ":" + type.name().toLowerCase(), "inventory"));
        }
    }

    /** 在CropRegistry.registerAll()之后调用，注册染色处理器 */
    @SideOnly(Side.CLIENT)
    public static void registerSeedModelsLate() {
        net.minecraft.client.Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                new ItemCropSeed.SeedColorHandler(), CROP_SEED);
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(@Nonnull Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(state.getProperties())));
        }
    }

    private static <T extends Block> ItemBlock createItemBlock(@Nonnull T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        return itemBlock;
    }
}
