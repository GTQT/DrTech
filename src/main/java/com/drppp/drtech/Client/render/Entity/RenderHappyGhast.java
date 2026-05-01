package com.drppp.drtech.Client.render.Entity;

import com.drppp.drtech.Client.render.Entity.model.ModelHappyGhast;
import com.drppp.drtech.common.Entity.moster.EntityHappyGhast;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.util.ResourceLocation;

public class RenderHappyGhast extends RenderLiving<EntityHappyGhast> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("drtech", "textures/entity/happy_ghast/happy_ghast.png");
    private static final ResourceLocation BABY_TEXTURE = new ResourceLocation("drtech", "textures/entity/happy_ghast/happy_ghast_baby.png");

    public RenderHappyGhast(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelHappyGhast(), 0.5F);
        this.addLayer(new LayerHappyGhastHarness(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityHappyGhast entity) {
        return entity.isChildForm() ? BABY_TEXTURE : TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityHappyGhast entitylivingbaseIn, float partialTickTime) {
        float scale = entitylivingbaseIn.isChildForm() ? 1.06875F : 4.5F;
        this.shadowSize = entitylivingbaseIn.isChildForm() ? 0.2F : 0.5F;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
