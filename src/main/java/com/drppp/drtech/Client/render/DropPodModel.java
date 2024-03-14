package com.drppp.drtech.Client.render;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.EntityDropPod;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class DropPodModel extends AnimatedGeoModel<EntityDropPod> {

    private static final ResourceLocation modelResource = new ResourceLocation("drtech", "geo/drop_pod.geo.json");
    private static final ResourceLocation textureResource = new ResourceLocation("drtech", "textures/entities/drop_pod.png");
    private static final ResourceLocation animationResource = new ResourceLocation("drtech", "animations/drop_pod.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityDropPod entityDropPod) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityDropPod entityDropPod) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityDropPod entityDropPod) {
        return animationResource;
    }
}