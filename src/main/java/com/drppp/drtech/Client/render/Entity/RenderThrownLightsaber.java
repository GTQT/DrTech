package com.drppp.drtech.Client.render.Entity;

import com.drppp.drtech.Client.render.Items.LightsaberRenderHelper;
import com.drppp.drtech.common.Entity.EntityThrownLightsaber;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderThrownLightsaber extends Render<EntityThrownLightsaber> {
    public RenderThrownLightsaber(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityThrownLightsaber entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.03F, (float) z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate((entity.ticksExisted + partialTicks) * 40.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.2F, 0.2F, 0.2F);
        LightsaberRenderHelper.renderLightsaber(stack, true, false);
        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityThrownLightsaber entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
