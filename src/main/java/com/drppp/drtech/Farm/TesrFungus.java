package com.drppp.drtech.Farm;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class TesrFungus extends TileEntitySpecialRenderer<TileFungus> {
    @Override
    public void render(TileFungus te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();

        TextureManager texturemanager = this.rendererDispatcher.renderEngine;
        texturemanager.bindTexture(te.type.RL.get(0));

        // 渲染交叉的平面
        GlStateManager.pushMatrix();
        renderCrossedSquares();
        GlStateManager.popMatrix();

        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);

        GlStateManager.pushMatrix();
        renderCrossedSquares();
        GlStateManager.popMatrix();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
    private void renderCrossedSquares() {
        // 开始渲染交叉的正方形（一个面）
        GlStateManager.glBegin(GL11.GL_QUADS);
        // 这里你需要根据需要设置顶点坐标和纹理坐标
        // ...
        GlStateManager.glEnd();
    }
}
