package com.drppp.drtech;

import com.drppp.drtech.api.unification.Materials.DrtechMaterials;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import keqing.gtqtcore.GTQTCore;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.drppp.drtech.Tags.MODID;


@Mod.EventBusSubscriber(modid = Tags.MODID)
public class DrtechEventHandler {
    public static int ctrlflag = 0;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMaterials(MaterialEvent event) {
        DrtechMaterials.init();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMTERegistry(MTEManager.MTERegistryEvent event) {
        GregTechAPI.mteManager.createRegistry(MODID);
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void createMaterialRegistry(MaterialRegistryEvent event) {
        GregTechAPI.materialManager.createRegistry(MODID);
    }

}
