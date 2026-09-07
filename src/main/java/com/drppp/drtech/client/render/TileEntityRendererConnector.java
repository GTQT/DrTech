package com.drppp.drtech.Client.render;

import com.drppp.drtech.Tile.TileEntityConnector;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class TileEntityRendererConnector extends TileEntitySpecialRenderer<TileEntityConnector> {
    private static final int SEGMENTS = 24;

    @Override
    public void render(TileEntityConnector tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tileEntity == null || !tileEntity.shouldRender()) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GL11.glLineWidth(2.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        for (TileEntityConnector.WireConnection connection : tileEntity.getConnections()) {
            if (isConnectorTarget(tileEntity, connection.target) && tileEntity.getPos().compareTo(connection.target) >= 0) {
                continue;
            }
            drawWire(tileEntity.getPos(), connection, buffer, tessellator);
        }

        GL11.glLineWidth(1.0F);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static boolean isConnectorTarget(TileEntityConnector tileEntity, BlockPos target) {
        if (tileEntity.getWorld() == null || !tileEntity.getWorld().isBlockLoaded(target)) {
            return false;
        }
        TileEntity targetTile = tileEntity.getWorld().getTileEntity(target);
        return targetTile instanceof TileEntityConnector;
    }

    private static void drawWire(BlockPos source, TileEntityConnector.WireConnection connection, BufferBuilder buffer, Tessellator tessellator) {
        double dx = connection.target.getX() - source.getX();
        double dy = connection.target.getY() - source.getY();
        double dz = connection.target.getZ() - source.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double sag = Math.min(1.5D, distance * 0.06D);

        int color = TileEntityConnector.getWireColor(connection.wireTier);
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;

        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= SEGMENTS; i++) {
            double progress = i / (double) SEGMENTS;
            double wireX = 0.5D + dx * progress;
            double wireY = 0.5D + dy * progress - Math.sin(Math.PI * progress) * sag;
            double wireZ = 0.5D + dz * progress;
            buffer.pos(wireX, wireY, wireZ).color(red, green, blue, 255).endVertex();
        }
        tessellator.draw();
    }
}
