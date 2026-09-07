package com.drppp.drtech.common.event;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.ItemsInit;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class CommonHandler {
    private int tickCounter = 0;
    @SubscribeEvent
    public void onPlayerUpdate(LivingEvent.LivingUpdateEvent event) {

    }

    @SubscribeEvent
    public void onGrassBroken(BlockEvent.HarvestDropsEvent event) {

    }
}