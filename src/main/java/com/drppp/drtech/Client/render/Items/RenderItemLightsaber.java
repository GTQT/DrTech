package com.drppp.drtech.Client.render.Items;

import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderItemLightsaber extends TileEntityItemStackRenderer {
    private static final ThreadLocal<ItemCameraTransforms.TransformType> TRANSFORM_TYPE =
            ThreadLocal.withInitial(() -> ItemCameraTransforms.TransformType.NONE);
    private static final ThreadLocal<EntityLivingBase> RENDER_ENTITY = new ThreadLocal<>();

    public static void setTransformType(ItemCameraTransforms.TransformType type) {
        TRANSFORM_TYPE.set(type == null ? ItemCameraTransforms.TransformType.NONE : type);
    }

    public static void setRenderEntity(@Nullable EntityLivingBase entity) {
        RENDER_ENTITY.set(entity);
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        ItemCameraTransforms.TransformType type = TRANSFORM_TYPE.get();
        GlStateManager.pushMatrix();
        try {
            if (type == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND || type == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
                applyLegacyFirstPersonTransform();
                GL11.glRotatef(-100.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-150.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(5.0F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(0.0F, 0.275F, 0.85F);
                GL11.glScalef(0.2F, 0.2F, 0.2F);
                LightsaberRenderHelper.renderLightsaber(stack, false, false);
            } else if (type == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND || type == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
                applyLegacyEquippedTransform();
                GL11.glTranslatef(0.7F, 0.3F, 0.0F);
                GL11.glRotatef(-150.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-85.0F, 0.0F, 1.0F, 0.0F);

                EntityLivingBase entity = RENDER_ENTITY.get();
                if (entity != null) {
                    applyLightsaberItemRotation(entity, stack, Minecraft.getMinecraft().getRenderPartialTicks());
                }

                GL11.glScalef(0.175F, 0.175F, 0.175F);
                LightsaberRenderHelper.renderLightsaber(stack, true, false);
            } else if (type == ItemCameraTransforms.TransformType.GROUND) {
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(0.3F, 0.3F, 0.3F);
                GL11.glTranslatef(0.0F, -LightsaberRenderHelper.getHiltHeight() / 48.0F, 0.0F);
                applyNameFlip(stack, 180.0F);
                LightsaberRenderHelper.renderLightsaberHilt();
            } else if (type == ItemCameraTransforms.TransformType.GUI) {
                renderInventoryPreview(stack);
            } else {
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                GL11.glScalef(0.22F, 0.22F, 0.22F);
                LightsaberRenderHelper.renderLightsaberHilt();
            }
        } finally {
            GlStateManager.popMatrix();
            TRANSFORM_TYPE.remove();
            RENDER_ENTITY.remove();
        }
    }

    private void renderInventoryPreview(ItemStack stack) {
        renderCrystalMarker(stack);
        GL11.glTranslatef(0.50F, 0.50F, 0.625F);
        GL11.glScalef(1.0F, 1.0F, -1.0F);
        GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-45.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(-110.0F, 0.0F, 1.0F, 0.0F);
        applyNameFlip(stack, 180.0F);
        GL11.glTranslatef(0.0F, 0.05F, 0.0F);
        GL11.glScalef(0.3125F, 0.3125F, 0.3125F);
        LightsaberRenderHelper.renderLightsaberHilt();
    }

    private void applyLegacyEquippedTransform() {
        // Cancel RenderItem's TEISR centering, then reproduce Forge 1.7.10's equipped-item transform.
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glTranslatef(0.0F, -0.3F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 1.5F);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
    }

    private void applyLegacyFirstPersonTransform() {
        // 1.7's first-person ItemRenderer scales items by 0.4 before Forge applies its equipped transform.
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glScalef(0.4F, 0.4F, 0.4F);
        GL11.glTranslatef(0.0F, -0.3F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 1.5F);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
    }

    private void renderCrystalMarker(ItemStack stack) {
        float size = 0.25F;
        float[] rgb = ItemLightsaber.getColor(stack).getRgb();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(rgb[0], rgb[1], rgb[2], 1.0F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(size / 2.0F, size / 2.0F, 0.0F).endVertex();
        buffer.pos(size, 0.0F, 0.0F).endVertex();
        buffer.pos(0.0F, 0.0F, 0.0F).endVertex();
        buffer.pos(0.0F, size, 0.0F).endVertex();
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void applyLightsaberItemRotation(EntityLivingBase entity, ItemStack stack, float partialTicks) {
        float limbSwing = entity.prevLimbSwingAmount
                - (entity.prevLimbSwingAmount - entity.limbSwingAmount) * partialTicks;
        float swingProgress = entity.getSwingProgress(partialTicks);
        float swingPeak = (swingProgress > 0.5F ? 1.0F - swingProgress : swingProgress) * 2.0F;
        float idle = 1.0F - limbSwing;

        if (!entity.isSneaking()) {
            GL11.glRotatef(-30.0F * idle, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-10.0F * idle, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(0.05F * idle, 0.05F * idle, 0.0F);
            GL11.glRotatef(-140.0F * swingPeak, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-80.0F * swingPeak, 1.0F, 0.0F, 0.0F);
        }

        if (isNameFlipped(stack)) {
            GL11.glRotatef(-180.0F * limbSwing * (1.0F - swingPeak), 0.0F, 0.0F, 1.0F);
        }
    }

    private void applyNameFlip(ItemStack stack, float angle) {
        if (isNameFlipped(stack)) {
            GL11.glRotatef(angle, 1.0F, 0.0F, 0.0F);
        }
    }

    private boolean isNameFlipped(ItemStack stack) {
        return stack.hasDisplayName()
                && ("Dinnerbone".equals(stack.getDisplayName()) || "Grumm".equals(stack.getDisplayName()));
    }
}
