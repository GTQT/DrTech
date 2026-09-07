package com.drppp.drtech.Client.render.Items;

import com.drppp.drtech.common.Items.lightsaber.FocusingCrystal;
import com.drppp.drtech.common.Items.lightsaber.ItemDoubleLightsaber;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.Items.lightsaber.LightsaberPartType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class RenderItemDoubleLightsaber extends TileEntityItemStackRenderer {
    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        ItemCameraTransforms.TransformType type = RenderItemLightsaber.getTransformType();
        float renderTick = Minecraft.getMinecraft().getRenderPartialTicks();
        GlStateManager.pushMatrix();
        try {
            if (type == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                    || type == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
                applyLegacyFirstPersonTransform();
                GL11.glRotatef(-100.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-150.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(95.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(-0.1F, -0.2F, 1.1F);
                applyFirstPersonAnimation(RenderItemLightsaber.getRenderEntity(), renderTick);
                GL11.glScalef(0.2F, 0.2F, 0.2F);
                renderLightsaber(stack, true, false);
            } else if (type == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND
                    || type == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
                applyLegacyEquippedTransform();
                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-150.0F, 1.0F, 0.0F, 0.0F);
                GL11.glTranslatef(-0.15F + getHiltHeight(stack) * 0.0015F, 0.14F, 0.75F);
                EntityLivingBase entity = RenderItemLightsaber.getRenderEntity();
                if (entity != null) {
                    applyThirdPersonAnimation(entity, stack, renderTick);
                }
                GL11.glScalef(0.175F, 0.175F, 0.175F);
                renderLightsaber(stack, true, false);
            } else if (type == ItemCameraTransforms.TransformType.GROUND) {
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScalef(0.3F, 0.3F, 0.3F);
                renderHilt(stack);
            } else if (type == ItemCameraTransforms.TransformType.GUI) {
                renderInventoryPreview(stack);
            } else {
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                GL11.glScalef(0.22F, 0.22F, 0.22F);
                renderHilt(stack);
            }
        } finally {
            GlStateManager.popMatrix();
            RenderItemLightsaber.clearRenderContext();
        }
    }

    public static void renderLightsaber(ItemStack stack, boolean inWorld, boolean forceBlade) {
        ItemStack[] blades = {ItemDoubleLightsaber.getUpper(stack), ItemDoubleLightsaber.getLower(stack)};
        boolean active = forceBlade || ItemDoubleLightsaber.isActive(stack);
        for (int i = 0; i < blades.length; i++) {
            GL11.glPushMatrix();
            if (i == 1) {
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }
            GL11.glTranslatef(0.0F, -LightsaberRenderHelper.getHiltHeight(blades[i]) / 32.0F, 0.0F);
            LightsaberRenderHelper.renderLightsaber(blades[i], inWorld, active);
            GL11.glPopMatrix();
        }
    }

    public static void renderHilt(ItemStack stack) {
        ItemStack[] blades = {ItemDoubleLightsaber.getUpper(stack), ItemDoubleLightsaber.getLower(stack)};
        for (int i = 0; i < blades.length; i++) {
            GL11.glPushMatrix();
            if (i == 1) {
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }
            GL11.glTranslatef(0.0F, -LightsaberRenderHelper.getHiltHeight(blades[i]) / 32.0F, 0.0F);
            LightsaberRenderHelper.renderLightsaberHilt(blades[i]);
            GL11.glPopMatrix();
        }
    }

    public static float getHiltHeight(ItemStack stack) {
        return LightsaberRenderHelper.getHiltHeight(ItemDoubleLightsaber.getUpper(stack))
                + LightsaberRenderHelper.getHiltHeight(ItemDoubleLightsaber.getLower(stack));
    }

    private void renderInventoryPreview(ItemStack stack) {
        renderCrystalMarkers(stack);
        GL11.glTranslatef(0.50F, 0.50F, 0.625F);
        GL11.glScalef(1.0F, 1.0F, -1.0F);
        GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-45.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(-110.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, 0.05F, 0.05F);
        GL11.glScalef(0.1875F, 0.1875F, 0.1875F);
        renderHilt(stack);
    }

    private void renderCrystalMarkers(ItemStack stack) {
        ItemStack upper = ItemDoubleLightsaber.getUpper(stack);
        ItemStack lower = ItemDoubleLightsaber.getLower(stack);
        float size = 0.25F;
        drawMarkerTriangle(upper, size, true);
        drawMarkerTriangle(lower, size, false);
    }

    private void drawMarkerTriangle(ItemStack blade, float size, boolean upper) {
        float[] rgb = ItemLightsaber.getColor(blade).getRgb();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(rgb[0], rgb[1], rgb[2], 1.0F);
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        if (upper) {
            buffer.pos(size / 2.0F, size / 2.0F, 0.0F).endVertex();
            buffer.pos(size, 0.0F, 0.0F).endVertex();
            buffer.pos(0.0F, 0.0F, 0.0F).endVertex();
        } else {
            buffer.pos(0.0F, size, 0.0F).endVertex();
            buffer.pos(size / 2.0F, size / 2.0F, 0.0F).endVertex();
            buffer.pos(0.0F, 0.0F, 0.0F).endVertex();
        }
        tessellator.draw();
        if ((ItemLightsaber.getFocusingCrystalMask(blade) & FocusingCrystal.INVERTING.getMask()) != 0) {
            float inner = size / 1.5F;
            GL11.glPushMatrix();
            GL11.glTranslatef(inner / 8.0F, inner / 8.0F, 0.0F);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
            if (upper) {
                buffer.pos(inner / 2.0F, inner / 2.0F, 0.0F).endVertex();
                buffer.pos(inner, 0.0F, 0.0F).endVertex();
                buffer.pos(0.0F, 0.0F, 0.0F).endVertex();
            } else {
                buffer.pos(0.0F, inner, 0.0F).endVertex();
                buffer.pos(inner / 2.0F, inner / 2.0F, 0.0F).endVertex();
                buffer.pos(0.0F, 0.0F, 0.0F).endVertex();
            }
            tessellator.draw();
            GL11.glPopMatrix();
        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void applyFirstPersonAnimation(EntityLivingBase entity, float partialTicks) {
        if (entity == null) {
            return;
        }
        float limbSwing = entity.prevLimbSwingAmount
                - (entity.prevLimbSwingAmount - entity.limbSwingAmount) * partialTicks;
        float swingProgress = entity.getSwingProgress(partialTicks);
        float swingPeak = (swingProgress > 0.5F ? 1.0F - swingProgress : swingProgress) * 2.0F;
        GL11.glRotatef(90.0F * limbSwing, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(0.2F * swingPeak + 0.8F * limbSwing,
                0.5F * swingPeak, 0.4F * limbSwing);
        GL11.glRotatef(30.0F * swingPeak, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(360.0F * swingProgress, 0.0F, 0.0F, 1.0F);
    }

    private void applyThirdPersonAnimation(EntityLivingBase entity, ItemStack stack, float partialTicks) {
        float limbSwing = entity.prevLimbSwingAmount
                - (entity.prevLimbSwingAmount - entity.limbSwingAmount) * partialTicks;
        float gait = MathHelper.cos((entity.limbSwing
                - entity.limbSwingAmount * (1.0F - partialTicks)) * 0.6662F) * 1.4F * limbSwing;
        float swingProgress = entity.getSwingProgress(partialTicks);
        float idle = 1.0F - limbSwing;

        GL11.glRotatef(-10.0F * gait * limbSwing, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.15F * idle, 0.0F, 0.0F);
        GL11.glRotatef(82.0F * idle, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-5.0F * idle, 1.0F, 0.0F, 0.0F);
        if (swingProgress > 0.0F) {
            GL11.glRotatef(-360.0F * swingProgress, 0.0F, 0.0F, 1.0F);
        }

        ItemStack upper = ItemDoubleLightsaber.getUpper(stack);
        ItemStack lower = ItemDoubleLightsaber.getLower(stack);
        if (ItemLightsaber.getPart(upper, LightsaberPartType.EMITTER).getCrossguard() != null
                || ItemLightsaber.getPart(lower, LightsaberPartType.EMITTER).getCrossguard() != null) {
            GL11.glRotatef(60.0F * idle, 0.0F, 1.0F, 0.0F);
        }
    }

    private void applyLegacyEquippedTransform() {
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glTranslatef(0.0F, -0.3F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 1.5F);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
    }

    private void applyLegacyFirstPersonTransform() {
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glScalef(0.4F, 0.4F, 0.4F);
        GL11.glTranslatef(0.0F, -0.3F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 1.5F);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
    }
}
