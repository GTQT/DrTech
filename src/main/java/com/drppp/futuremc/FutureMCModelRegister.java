package com.drppp.futuremc;

import com.drppp.futuremc.blocks.BlockDriedGhast;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Client-side item model registration for blocks owned by Future MC. */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = FutureMCMain.MODID)
public final class FutureMCModelRegister {
    private FutureMCModelRegister() {}

    @SubscribeEvent
    public static void register(ModelRegistryEvent event) {
        for (Block block : new Block[] {FutureMCMain.SMOOTH_BASALT, FutureMCMain.CALCITE,
                FutureMCMain.AMETHYST_BLOCK, FutureMCMain.BUDDING_AMETHYST,
                FutureMCMain.SMALL_AMETHYST_BUD, FutureMCMain.MEDIUM_AMETHYST_BUD,
                FutureMCMain.LARGE_AMETHYST_BUD, FutureMCMain.AMETHYST_CLUSTER,
                FutureMCMain.LANTERN, FutureMCMain.SOUL_LANTERN, FutureMCMain.CHAIN,
                FutureMCMain.COMPOSTER, FutureMCMain.DRIED_GHAST}) {
            Item item = Item.getItemFromBlock(block);
            if (item != null) {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                        new ModelResourceLocation(block.getRegistryName(), "inventory"));
            }
        }
        ModelLoader.setCustomModelResourceLocation(FutureMCMain.AMETHYST_SHARD, 0,
                new ModelResourceLocation(FutureMCMain.AMETHYST_SHARD.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(FutureMCMain.HAPPY_GHAST_HARNESS, 0,
                new ModelResourceLocation(FutureMCMain.HAPPY_GHAST_HARNESS.getRegistryName(), "inventory"));
        ModelLoader.setCustomStateMapper(FutureMCMain.DRIED_GHAST, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(FutureMCMain.DRIED_GHAST.getRegistryName(),
                        "facing=" + state.getValue(BlockDriedGhast.FACING).getName()
                                + ",hydration=" + state.getValue(BlockDriedGhast.HYDRATION));
            }
        });
    }
}
