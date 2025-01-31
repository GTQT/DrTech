package com.drppp.drtech.Client.render.Items;


import com.drppp.drtech.common.Items.GeoItems.GeoItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class GeoItemRender extends GeoItemRenderer<GeoItem> {

    public GeoItemRender() {
        super(new GeoItemModel());
    }

}