package com.drppp.drtech;

import com.drppp.drtech.api.unification.Materials.DrtechMaterials;
import gregtech.api.unification.material.event.MaterialEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public class DrtechEventHandler {
    public static int ctrlflag = 0;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMaterials(MaterialEvent event) {
        DrtechMaterials.init();
    }
}
