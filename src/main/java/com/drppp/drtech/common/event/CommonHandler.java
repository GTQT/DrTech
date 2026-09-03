package com.drppp.drtech.common.event;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommonHandler {
    private int tickCounter = 0;
    @SubscribeEvent
    public void onPlayerUpdate(LivingEvent.LivingUpdateEvent event) {

    }

    @SubscribeEvent
    public void onGrassBroken(BlockEvent.HarvestDropsEvent event) {

    }
}