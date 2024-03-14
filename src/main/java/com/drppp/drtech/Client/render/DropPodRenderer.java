package com.drppp.drtech.Client.render;

import com.drppp.drtech.common.Entity.EntityDropPod;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class DropPodRenderer extends GeoEntityRenderer<EntityDropPod> {

    public DropPodRenderer(RenderManager renderManager) {
        super(renderManager, new DropPodModel());
    }

}