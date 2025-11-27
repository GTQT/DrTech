package com.drppp.drtech.common.event;

import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class CommonHandler {
    private int tickCounter = 0;
    @SubscribeEvent
    public void onPlayerUpdate(LivingEvent.LivingUpdateEvent event) {

    }
    @SubscribeEvent
    public void onGrassBroken(BlockEvent.HarvestDropsEvent event) {
        World world = event.getWorld();
        IBlockState state = event.getState();
        Block block = state.getBlock();
        if ((block == Blocks.TALLGRASS || block == Blocks.GRASS) && !world.isRemote) {
            Random rand = world.rand;
            if (rand.nextFloat() < 0.1F) {
                ItemStack seedStack = new ItemStack(ItemsInit.ITEM_XJC_SEED, 1);
                event.getDrops().add(seedStack);
            }
        }
    }
}