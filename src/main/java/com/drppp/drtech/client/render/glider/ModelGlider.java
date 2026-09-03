package com.drppp.drtech.client.render.glider;

import com.drppp.drtech.common.glider.GliderFlightHandler;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public final class ModelGlider extends ModelBase {
    private static final float QUAD_HALF_SIZE = 2.4F;

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.glNormal3f(0.0F, -1.0F, 0.0F);
        quad(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        quad(false);
        GlStateManager.disableBlend();
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        quad(true);
    }

    private static void quad(boolean top) {
        GlStateManager.glBegin(GL11.GL_QUADS);
        if (top) {
            vertex(1.0F, 0.0F, QUAD_HALF_SIZE, -QUAD_HALF_SIZE);
            vertex(0.0F, 0.0F, -QUAD_HALF_SIZE, -QUAD_HALF_SIZE);
            vertex(0.0F, 1.0F, -QUAD_HALF_SIZE, QUAD_HALF_SIZE);
            vertex(1.0F, 1.0F, QUAD_HALF_SIZE, QUAD_HALF_SIZE);
        } else {
            vertex(1.0F, 1.0F, QUAD_HALF_SIZE, QUAD_HALF_SIZE);
            vertex(0.0F, 1.0F, -QUAD_HALF_SIZE, QUAD_HALF_SIZE);
            vertex(0.0F, 0.0F, -QUAD_HALF_SIZE, -QUAD_HALF_SIZE);
            vertex(1.0F, 0.0F, QUAD_HALF_SIZE, -QUAD_HALF_SIZE);
        }
        GlStateManager.glEnd();
    }

    private static void vertex(float u, float v, float x, float z) {
        GlStateManager.glTexCoord2f(u, v);
        GlStateManager.glVertex3f(x, 0.0F, z);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        EntityPlayer player = (EntityPlayer) entity;
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 2.0F, 0.0F);
        GlStateManager.translate(0.0F, player.isSneaking() ? -0.5F : -0.35F, 0.0F);
        if (!GliderFlightHandler.shouldBeGliding(player)) {
            GlStateManager.scale(0.9F, 0.9F, 0.8F);
            GlStateManager.translate(0.0F, 0.0F, -0.5F);
        }
    }
}
