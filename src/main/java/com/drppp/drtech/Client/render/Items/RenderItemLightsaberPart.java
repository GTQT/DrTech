package com.drppp.drtech.Client.render.Items;

import com.drppp.drtech.Client.render.Items.lightsaber.LightsaberModelRegistry;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaberPart;
import com.drppp.drtech.common.Items.lightsaber.LightsaberHilt;
import com.drppp.drtech.common.Items.lightsaber.LightsaberPartType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public final class RenderItemLightsaberPart extends TileEntityItemStackRenderer {
    private static final ThreadLocal<ItemCameraTransforms.TransformType> TRANSFORM_TYPE =
            ThreadLocal.withInitial(() -> ItemCameraTransforms.TransformType.NONE);

    public static void setTransformType(ItemCameraTransforms.TransformType type) {
        TRANSFORM_TYPE.set(type == null ? ItemCameraTransforms.TransformType.NONE : type);
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        if (!(stack.getItem() instanceof ItemLightsaberPart)) {
            return;
        }
        ItemLightsaberPart partItem = (ItemLightsaberPart) stack.getItem();
        LightsaberPartType partType = partItem.getPartType();
        LightsaberHilt hilt = partItem.getHilt(stack);
        float height = hilt.getHeight(partType);
        float scale = 0.4F;
        float offset = height * (partType == LightsaberPartType.BODY || partType == LightsaberPartType.POMMEL ? -1.0F : 1.0F)
                / 2.0F * 0.0625F;

        GlStateManager.pushMatrix();
        try {
            ItemCameraTransforms.TransformType transform = TRANSFORM_TYPE.get();
            if (transform == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
                    || transform == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
                applyLegacyFirstPersonTransform();
                GL11.glRotatef(-100.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-150.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(5.0F, 0.0F, 0.0F, 1.0F);
                GL11.glTranslatef(0.0F, 0.15F, 0.9F);
            } else if (transform == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
                    || transform == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
                applyLegacyEquippedTransform();
                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-150.0F, 1.0F, 0.0F, 0.0F);
                GL11.glTranslatef(0.1F, 0.15F, 0.475F);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 1.0F);
                GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
            } else if (transform == ItemCameraTransforms.TransformType.GROUND) {
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                scale = 0.3F;
            } else if (transform == ItemCameraTransforms.TransformType.GUI) {
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-45.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GL11.glTranslatef(0.0F, 0.05F, 0.0F);
                GL11.glRotatef(-110.0F, 0.0F, 1.0F, 0.0F);
                scale = partType == LightsaberPartType.POMMEL && height <= 4.0F ? 2.0F : 1.0F;
                if (height * scale > 14.0F) {
                    scale = 14.0F / height;
                }
            } else {
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                scale = 0.3F;
            }

            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(0.0F, offset, 0.0F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(LightsaberModelRegistry.getTexture(hilt, partType));
            LightsaberModelRegistry.getModel(hilt, partType)
                    .render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        } finally {
            GlStateManager.popMatrix();
            TRANSFORM_TYPE.remove();
        }
    }

    private static void applyLegacyEquippedTransform() {
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glTranslatef(0.0F, -0.3F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 1.5F);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
    }

    private static void applyLegacyFirstPersonTransform() {
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glScalef(0.4F, 0.4F, 0.4F);
        GL11.glTranslatef(0.0F, -0.3F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 1.5F);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
    }
}
