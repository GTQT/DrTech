package com.drppp.drtech.Items;

import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Tags;
import gregtech.api.block.VariantItemBlock;
import gregtech.common.pipelike.laser.BlockLaserPipe;
import gregtech.common.pipelike.laser.LaserPipeType;
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

import static com.drppp.drtech.Blocks.BlocksInit.MY_LASER_PIPES;
import static gregtech.common.blocks.MetaBlocks.statePropertiesToString;

public class ItemsInit {
    public static  final Item ITEM_BLOCK_GRAVITATIONAL_ANOMALY = new  ItemBlock(BlocksInit.BLOCK_GRAVITATIONAL_ANOMALY).setRegistryName(Tags.MODID,"gravitational_anomaly");
    public static void init(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITEM_BLOCK_GRAVITATIONAL_ANOMALY);
        event.getRegistry().register(createItemBlock(BlocksInit.TRANSPARENT_CASING,  VariantItemBlock::new));
        event.getRegistry().register(createItemBlock(BlocksInit.COMMON_CASING,  VariantItemBlock::new));



        //未生效
        LaserPipeType[] var11 = LaserPipeType.values();
        for(int i = 0; i < var11.length; ++i) {
            LaserPipeType type = var11[i];
            event.getRegistry().register(Item.getItemFromBlock(MY_LASER_PIPES[type.ordinal()]));
        }
    }
    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(BlocksInit.TRANSPARENT_CASING);
        registerItemModel(BlocksInit.COMMON_CASING);
        BlockLaserPipe[] var7 = MY_LASER_PIPES;
        for(int i = 0; i < var7.length; ++i) {
            registerItemModel(var7[i]);
        }
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
