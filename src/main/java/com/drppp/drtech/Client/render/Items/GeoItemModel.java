package com.drppp.drtech.Client.render.Items;


import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.EntityWindRotor;
import com.drppp.drtech.common.Items.GeoItems.GeoItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GeoItemModel extends AnimatedGeoModel<GeoItem> {

    private static final ResourceLocation modelResource = new ResourceLocation("drtech", "geo/windrotor.geo.json");
    private static final ResourceLocation textureResource = new ResourceLocation("drtech", "textures/entities/windrotor.png");
    private static final ResourceLocation animationResource = new ResourceLocation("drtech", "animations/windrotor.animation.json");

    @Override
    public ResourceLocation getModelLocation(GeoItem object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureLocation(GeoItem object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(GeoItem object) {
        return animationResource;
    }
}
