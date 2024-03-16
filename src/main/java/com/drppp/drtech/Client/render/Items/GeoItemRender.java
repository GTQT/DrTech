package com.drppp.drtech.Client.render.Items;


import com.drppp.drtech.Client.render.WindRotorModel;
import com.drppp.drtech.common.Entity.EntityWindRotor;
import com.drppp.drtech.common.Items.GeoItems.GeoItem;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class GeoItemRender extends GeoItemRenderer<GeoItem> {

    public GeoItemRender() {
        super(new GeoItemModel());
    }

}