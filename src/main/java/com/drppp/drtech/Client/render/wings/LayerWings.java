package com.drppp.drtech.Client.render.wings;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

/** Renders inside RenderPlayer's active model matrix, matching Wings' original layer placement. */
final class LayerWings implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer renderer;

    LayerWings(RenderPlayer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        WingsClientHandler.renderWings(player, renderer, partialTicks);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
