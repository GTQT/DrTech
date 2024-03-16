package com.drppp.drtech.Client.render;


import com.drppp.drtech.common.Entity.EntityWindRotor;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class WindRotorRender extends GeoEntityRenderer<EntityWindRotor> {

    public WindRotorRender(RenderManager manager) {
        super(manager, new WindRotorModel());
    }

}