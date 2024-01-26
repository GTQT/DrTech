package com.drppp.drtech.Client.render;

import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.util.glu.Sphere;

public class TileEntityRendererGravitationalAnomaly extends TileEntitySpecialRenderer<TileEntityGravitationalAnomaly> {
   // public static final ResourceLocation core = new ResourceLocation("drtech:textures/blocks/gravitational_anomaly.png");

    private final Sphere sphere_model = new Sphere();

    public TileEntityRendererGravitationalAnomaly() {
    }

    public void render(TileEntityGravitationalAnomaly tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tileEntity.shouldRender()) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            long time = Minecraft.getMinecraft().world.getWorldTime();
            float speed = 1.0F;
            double resonateSpeed = 0.1;
            double radius;
            radius = 1d;
            radius = radius * Math.sin((double)time * resonateSpeed) * 0.1 + radius * 0.9;
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            GlStateManager.scale(radius, radius, radius);
            GlStateManager.disableCull();
            GlStateManager.disableTexture2D();
            GlStateManager.color(0.0F, 0.0F, 0.0F, 1.0F);
            this.sphere_model.draw(0.33F, 8, 8);
            GlStateManager.enableTexture2D();
            GlStateManager.enableCull();
            GlStateManager.enableBlend();
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            GlStateManager.rotate(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks, 0.0F, -1.0F, 0.0F);
            GlStateManager.rotate(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate((float)time * speed, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(-0.5, -0.5, 0.0);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            GlStateManager.blendFunc(770, 771);
            //this.bindTexture(core);
            //RenderUtils.drawPlane(1.0);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
}
