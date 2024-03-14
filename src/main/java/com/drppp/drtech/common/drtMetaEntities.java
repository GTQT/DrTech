package com.drppp.drtech.common;

import com.drppp.drtech.Client.render.DroneRenderer;
import com.drppp.drtech.Client.render.DropPodRenderer;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.EntityDrone;
import com.drppp.drtech.common.Entity.EntityDropPod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class drtMetaEntities {

    public static void init() {
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MODID, "drop_pod"), EntityDropPod.class, "Drop Pod", 1, DrTechMain.instance, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Tags.MODID, "drone"), EntityDrone.class, "Drone", 2, DrTechMain.instance, 64, 3, true);
    }

    @SideOnly(Side.CLIENT)
    public static void initRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityDropPod.class, DropPodRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityDrone.class, DroneRenderer::new);
    }

}