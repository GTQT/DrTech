package com.drppp.drtech.Client.render.Entity;

import com.drppp.drtech.Client.render.Entity.model.ModelHappyGhast;
import com.drppp.drtech.common.Entity.moster.EntityHappyGhast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerHappyGhastHarness implements LayerRenderer<EntityHappyGhast> {
    private static final ResourceLocation ROPES_TEXTURE = new ResourceLocation("drtech", "textures/entity/happy_ghast/happy_ghast_ropes.png");
    private static final ResourceLocation HARNESS_TEXTURE = new ResourceLocation("drtech", "textures/entity/happy_ghast/green_harness_body.png");
    private final RenderHappyGhast renderer;
    private final ModelHappyGhast model = new ModelHappyGhast();

    public LayerHappyGhastHarness(RenderHappyGhast renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityHappyGhast entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.isChildForm()) {
            return;
        }
        this.renderer.bindTexture(ROPES_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        if (entitylivingbaseIn.getHarnessed()) {
            this.renderer.bindTexture(HARNESS_TEXTURE);
            this.model.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
