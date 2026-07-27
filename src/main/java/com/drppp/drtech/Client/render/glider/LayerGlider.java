package com.drppp.drtech.Client.render.glider;

import com.drppp.drtech.glider.GliderFlightHandler;
import com.drppp.drtech.glider.ItemHangGlider;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public final class LayerGlider implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer renderer;
    private final ModelGlider model = new ModelGlider();

    public LayerGlider(RenderPlayer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack stack = GliderFlightHandler.getGlider(player);
        if (player.isInvisible() || !GliderFlightHandler.isDeployed(player) || !(stack.getItem() instanceof ItemHangGlider)) {
            return;
        }
        renderer.bindTexture(texture(stack));
        GlStateManager.pushMatrix();
        model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
        model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();
    }

    static ResourceLocation texture(ItemStack stack) {
        return new ResourceLocation("drtech", stack.getItem().getRegistryName().getPath().equals("advanced_hang_glider")
                ? "textures/models/hang_glider_advanced.png" : "textures/models/hang_glider.png");
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
