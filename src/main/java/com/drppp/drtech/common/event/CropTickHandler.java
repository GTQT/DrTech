package com.drppp.drtech.common.event;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * 额外tick处理器，可扩展季节系统、天气影响等
 */
public class CropTickHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;
        // 全局作物事件扩展点
    }
}
