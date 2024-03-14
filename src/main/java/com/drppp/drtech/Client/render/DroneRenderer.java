package com.drppp.drtech.Client.render;


import com.drppp.drtech.common.Entity.EntityDrone;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class DroneRenderer extends GeoEntityRenderer<EntityDrone> {

    public DroneRenderer(RenderManager manager) {
        super(manager, new DroneModel());
    }

}