package com.drppp.drtech.Items;

import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Client.render.LaserPipeRenderer;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Items.ItemCropSeed.ItemFluStoneCropSeed;
import com.drppp.drtech.Items.ItemCropSeed.ItemLapisCropSeed;
import com.drppp.drtech.Items.ItemCropSeed.ItemRedStoneCropSeed;
import com.drppp.drtech.Tags;
import gregtech.api.block.VariantItemBlock;
import gregtech.client.model.SimpleStateMapper;
import gregtech.common.pipelike.laser.BlockLaserPipe;
import gregtech.common.pipelike.laser.ItemBlockLaserPipe;
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
    public static  final Item ITEM_BLOCK_GRAVITATIONAL_ANOMALY = new  ItemBlock(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY).setRegistryName(Tags.MODID,"gravitational_anomaly");
    public static  final Item ITEM_RED_STONE_SEED = new ItemRedStoneCropSeed("red_stone_seed");
    public static  final Item ITEM_LAPIS_SEED = new ItemLapisCropSeed("lapis_seed");
    public static  final Item ITEM_FLU_SEED = new ItemFluStoneCropSeed("flu_seed");

    public static void init(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITEM_BLOCK_GRAVITATIONAL_ANOMALY);
        event.getRegistry().register(ITEM_RED_STONE_SEED);
        event.getRegistry().register(ITEM_LAPIS_SEED);
        event.getRegistry().register(ITEM_FLU_SEED);
        event.getRegistry().register(createItemBlock(BlocksInit.TRANSPARENT_CASING,  VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.COMMON_CASING,  VariantItemBlock::new));
        event.getRegistry().register(new ItemBlockLaserPipe(BlocksInit.MY_LASER_PIPE).setRegistryName(BlocksInit.MY_LASER_PIPE.getRegistryName()));
    }
    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(BlocksInit.TRANSPARENT_CASING);
        registerItemModel(BlocksInit.COMMON_CASING);
            BlockLaserPipe pipe = BlocksInit.MY_LASER_PIPE;
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(pipe), (stack) -> {
                return LaserPipeRenderer.INSTANCE.getModelLocation();
            });
        var normalStateMapper = new SimpleStateMapper(LaserPipeRenderer.INSTANCE.getModelLocation());
            ModelLoader.setCustomStateMapper(pipe, normalStateMapper);
        ModelLoader.setCustomModelResourceLocation(ITEM_RED_STONE_SEED, 0, new ModelResourceLocation(ITEM_RED_STONE_SEED.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_LAPIS_SEED, 0, new ModelResourceLocation(ITEM_LAPIS_SEED.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(ITEM_FLU_SEED, 0, new ModelResourceLocation(ITEM_FLU_SEED.getRegistryName(), "inventory"));


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
