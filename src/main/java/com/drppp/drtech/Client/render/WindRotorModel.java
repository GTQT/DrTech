package com.drppp.drtech.Client.render;

import com.drppp.drtech.common.Entity.EntityDrone;
import com.drppp.drtech.common.Entity.EntityWindRotor;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WindRotorModel extends AnimatedGeoModel<EntityWindRotor> {

    private static final ResourceLocation modelResource = new ResourceLocation("drtech", "geo/windrotor.geo.json");
    private static final ResourceLocation textureResource = new ResourceLocation("drtech", "textures/entities/windrotor.png");
    private static final ResourceLocation animationResource = new ResourceLocation("drtech", "animations/windrotor.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityWindRotor entityDrone) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityWindRotor entityDrone) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityWindRotor entityDrone) {
        return animationResource;
    }

}