package com.drppp.drtech.common.Items;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Items.ItemCropSeed.ItemFluStoneCropSeed;
import com.drppp.drtech.common.Items.ItemCropSeed.ItemLapisCropSeed;
import com.drppp.drtech.common.Items.ItemCropSeed.ItemRedStoneCropSeed;
import com.drppp.drtech.common.Items.ItemCropSeed.ItemXjcCropSeed;
import gregtech.api.block.VariantItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

import static gregtech.common.blocks.MetaBlocks.statePropertiesToString;

public class ItemsInit {
    public static final Item ITEM_BLOCK_GRAVITATIONAL_ANOMALY = new ItemBlock(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY).setRegistryName(Tags.MODID, "gravitational_anomaly");
    public static final Item ITEM_BLOCK_WATER_MILL = new ItemBlock(BlocksInit.BLOCK_WATER_MILL).setRegistryName(Tags.MODID, "water_mill");
    public static final Item ITEM_BLOCK_WOOD_AXLE = new ItemBlock(BlocksInit.BLOCK_WOOD_AXLE).setRegistryName(Tags.MODID, "wood_axle");
    public static final Item ITEM_CONNECTOR1 = new ItemBlock(BlocksInit.BLOCK_CONNECTOR1).setRegistryName(Tags.MODID, "connector_1");
    public static final Item ITEM_CONNECTOR2 = new ItemBlock(BlocksInit.BLOCK_CONNECTOR2).setRegistryName(Tags.MODID, "connector_2");
    public static final Item ITEM_CONNECTOR3 = new ItemBlock(BlocksInit.BLOCK_CONNECTOR3).setRegistryName(Tags.MODID, "connector_3");
    public static final Item ITEM_BLOCK_GOLDEN_SEA = new ItemBlock(BlocksInit.BLOCK_GOLDEN_SEA).setRegistryName(Tags.MODID, "golden_sea");
    public static final Item ITEM_BLOCK_PEACEFUL_TABLE = new ItemBlock(BlocksInit.BLOCK_PEACEFUL_TABLE).setRegistryName(Tags.MODID, "peaceful_table");
    public static final Item ITEM_BLOCK_STORAGE_PAIL = new ItemBlock(BlocksInit.BLOCK_STORAGE_PAIL).setRegistryName(Tags.MODID, BlocksInit.BLOCK_STORAGE_PAIL.getRegistryName().getPath());
    public static final Item ITEM_BLOCK_WASTE_DIRT = new ItemBlock(BlocksInit.BLOCK_WASTE_DIRT).setRegistryName(Tags.MODID, BlocksInit.BLOCK_WASTE_DIRT.getRegistryName().getPath());
    public static final Item ITEM_BLOCK_SAP_BAG = new ItemBlock(BlocksInit.BLOCK_SAP_BAG).setRegistryName(Tags.MODID, BlocksInit.BLOCK_SAP_BAG.getRegistryName().getPath());
    public static final Item ITEM_RED_STONE_SEED = new ItemRedStoneCropSeed("red_stone_seed");
    public static final Item ITEM_LAPIS_SEED = new ItemLapisCropSeed("lapis_seed");
    public static final Item ITEM_FLU_SEED = new ItemFluStoneCropSeed("flu_seed");
    public static final Item ITEM_XJC_SEED = new ItemXjcCropSeed("xjc_seed");
    public static final Item ITEM_BLOCK_ADVANCED_CAULDRON = new ItemBlock(BlocksInit.BLOCK_ADVANCED_CAULDRON).setRegistryName(Tags.MODID, BlocksInit.BLOCK_ADVANCED_CAULDRON.getRegistryName().getPath());
    public static final Item ITEM_BLOCK_TIME_TABLE = new ItemBlock(BlocksInit.BLOCK_TIME_TABLE).setRegistryName(Tags.MODID, BlocksInit.BLOCK_TIME_TABLE.getRegistryName().getPath());

    public static void init(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ITEM_BLOCK_GRAVITATIONAL_ANOMALY);
        event.getRegistry().register(ITEM_BLOCK_WATER_MILL);
        event.getRegistry().register(ITEM_BLOCK_WOOD_AXLE);
        event.getRegistry().register(ITEM_RED_STONE_SEED);
        event.getRegistry().register(ITEM_LAPIS_SEED);
        event.getRegistry().register(ITEM_FLU_SEED);
        event.getRegistry().register(ITEM_XJC_SEED);
        event.getRegistry().register(ITEM_CONNECTOR1);
        event.getRegistry().register(ITEM_CONNECTOR2);
        event.getRegistry().register(ITEM_CONNECTOR3);
        event.getRegistry().register(ITEM_BLOCK_GOLDEN_SEA);
        event.getRegistry().register(ITEM_BLOCK_PEACEFUL_TABLE);
        event.getRegistry().register(ITEM_BLOCK_STORAGE_PAIL);
        event.getRegistry().register(ITEM_BLOCK_WASTE_DIRT);
        event.getRegistry().register(ITEM_BLOCK_SAP_BAG);
        event.getRegistry().register(ITEM_BLOCK_ADVANCED_CAULDRON);
        event.getRegistry().register(ITEM_BLOCK_TIME_TABLE);
        event.getRegistry().register(createItemBlock(BlocksInit.TRANSPARENT_CASING1, VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.COMMON_CASING, VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.COMMON_CASING1, VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.YOT_TANK, VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.TFFT_TANK, VariantItemBlock::new));
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(BlocksInit.TRANSPARENT_CASING1);
        registerItemModel(BlocksInit.COMMON_CASING);
        registerItemModel(BlocksInit.COMMON_CASING1);
        registerItemModel(BlocksInit.YOT_TANK);
        registerItemModel(BlocksInit.TFFT_TANK);

        ModelLoader.setCustomModelResourceLocation(ITEM_RED_STONE_SEED, 0, new ModelResourceLocation(ITEM_RED_STONE_SEED.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_LAPIS_SEED, 0, new ModelResourceLocation(ITEM_LAPIS_SEED.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_FLU_SEED, 0, new ModelResourceLocation(ITEM_FLU_SEED.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_XJC_SEED, 0, new ModelResourceLocation(ITEM_XJC_SEED.getRegistryName(), "inventory"));

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
