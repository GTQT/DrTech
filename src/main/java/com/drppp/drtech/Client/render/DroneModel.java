package com.drppp.drtech.Client.render;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.EntityDrone;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class DroneModel extends AnimatedGeoModel<EntityDrone> {

    private static final ResourceLocation modelResource = new ResourceLocation("drtech", "geo/gatherer_drone.geo.json");
    private static final ResourceLocation textureResource = new ResourceLocation("drtech", "textures/entities/drone.png");
    private static final ResourceLocation animationResource = new ResourceLocation("drtech", "animations/drone.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityDrone entityDrone) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityDrone entityDrone) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityDrone entityDrone) {
        return animationResource;
    }

}