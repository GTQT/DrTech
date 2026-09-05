package com.drppp.drtech.common.Blocks;

import com.drppp.drtech.Tags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class BubbleColumnHandler {
    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        refresh(event.getWorld(), event.getPos());
    }

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        refresh(event.getWorld(), event.getPos());
    }

    private static void refresh(World world, BlockPos pos) {
        BlockBubbleColumn.refreshAround(world, pos);
    }
}
