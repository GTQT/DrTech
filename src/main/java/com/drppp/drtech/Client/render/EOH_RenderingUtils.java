package com.drppp.drtech.Client.render;

import com.drppp.drtech.Tags;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import static com.drppp.drtech.Client.render.EOH_TESR.*;

public class EOH_RenderingUtils {
    public static void renderStar(int type, Color color) {
        GL11.glPushMatrix();

        if (type == 1) GL11.glRotated(180, 0, 1, 0);
        else if (type == 2
                || type == 3) {
            GL11.glTranslated(0.5, 0.5, 0.5);
            if (type == 4) GL11.glRotated(90, 0, 1, 0);
        }

        // Render star stuff.
        renderStarLayer(0, STAR_LAYER_0, color, 1.0f);
        renderStarLayer(1, STAR_LAYER_1, color, 0.4f);
        renderStarLayer(2, STAR_LAYER_2, color, 0.2f);

        GL11.glPopMatrix();
    }

    public static void renderStar(int type) {
        renderStar(type, new Color(1.0f, 0.4f, 0.05f, 1.0f));
    }
    private static void renderStarLayer(int layer, ResourceLocation texture, Color color, float alpha) {

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        if (alpha < 1.0f) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
        float scale = 0.01f;
        scale *= Math.pow(1.04f, layer);
        GL11.glScalef(scale, scale, scale);
        switch (layer) {
            case 0 -> GL11.glRotatef(130 + (System.currentTimeMillis() / 64) % 360, 0F, 1F, 1F);
            case 1 -> GL11.glRotatef(-49 + (System.currentTimeMillis() / 64) % 360, 1F, 1F, 0F);
            case 2 -> GL11.glRotatef(67 + (System.currentTimeMillis() / 64) % 360, 1F, 0F, 1F);
        }
        final float red = color.getRed() / 255.0f;
        final float green = color.getGreen() / 255.0f;
        final float blue = color.getBlue() / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
        starModel.renderAll();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPopMatrix();
    }

    public static void beginRenderingBlocksInWorld(final float blockSize) {
        final Tessellator tes = Tessellator.getInstance();
        final BufferBuilder bufferBuilder = tes.getBuffer();
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        GL11.glScalef(blockSize, blockSize, blockSize);
    }

    public static void endRenderingBlocksInWorld() {
        Tessellator.getInstance().draw();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    static final double[] BLOCK_X = { -0.5, -0.5, +0.5, +0.5, +0.5, +0.5, -0.5, -0.5 };
    static final double[] BLOCK_Y = { +0.5, -0.5, -0.5, +0.5, +0.5, -0.5, -0.5, +0.5 };
    static final double[] BLOCK_Z = { +0.5, +0.5, +0.5, +0.5, -0.5, -0.5, -0.5, -0.5 };

    public static void addRenderedBlockInWorld(final Block block, final int meta, final double x, final double y,
                                               final double z) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferBuilder = tessellator.getBuffer();
        TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite texture= textureMap.getAtlasSprite("drtech:blocks/grass_top");
        switch (meta)
        {
            case 1:
                texture = textureMap.getAtlasSprite("drtech:blocks/connector_1");
                break;
            case 2:
                texture = textureMap.getAtlasSprite("drtech:blocks/connector_2");
                break;
            case 3:
                texture = textureMap.getAtlasSprite("drtech:blocks/connector_3");
                break;
            case 4:
                texture = textureMap.getAtlasSprite("drtech:blocks/grass_top");
                break;
            case 5:
                texture = textureMap.getAtlasSprite("drtech:blocks/cauldron_inner");
                break;
            case 6:
                texture = textureMap.getAtlasSprite("drtech:blocks/solar_reflection_casing_side");
                break;
            case 7:
                texture = textureMap.getAtlasSprite("drtech:blocks/resonator_casing");
                break;
            case 0:
                texture = textureMap.getAtlasSprite("drtech:blocks/grass_top");
                break;


        }
        double minU;
        double maxU;
        double minV;
        double maxV;

        {
            minU = texture.getMinU();
            maxU = texture.getMaxU();
            minV = texture.getMinV();
            maxV = texture.getMaxV();

            bufferBuilder.pos(x + BLOCK_X[1], y + BLOCK_Y[1], z + BLOCK_Z[1]).tex( maxU, maxV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[0], y + BLOCK_Y[0], z + BLOCK_Z[0]).tex(  maxU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[7], y + BLOCK_Y[7], z + BLOCK_Z[7]).tex(  minU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[6], y + BLOCK_Y[6], z + BLOCK_Z[6]).tex(  minU, maxV).endVertex();
        }
        {
            minU = texture.getMinU();
            maxU = texture.getMaxU();
            minV = texture.getMinV();
            maxV = texture.getMaxV();

            bufferBuilder.pos(x + BLOCK_X[5], y + BLOCK_Y[5], z + BLOCK_Z[5]).tex( maxU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[2], y + BLOCK_Y[2], z + BLOCK_Z[2]).tex( maxU, maxV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[1], y + BLOCK_Y[1], z + BLOCK_Z[1]).tex(minU, maxV) .endVertex() ;
            bufferBuilder.pos(x + BLOCK_X[6], y + BLOCK_Y[6], z + BLOCK_Z[6]).tex( minU, minV).endVertex();
        }

        {
            minU = texture.getMinU();
            maxU = texture.getMaxU();
            minV = texture.getMinV();
            maxV = texture.getMaxV();

            bufferBuilder.pos(x + BLOCK_X[6], y + BLOCK_Y[6], z + BLOCK_Z[6]).tex( maxU, maxV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[7], y + BLOCK_Y[7], z + BLOCK_Z[7]).tex( maxU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[4], y + BLOCK_Y[4], z + BLOCK_Z[4]).tex( minU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[5], y + BLOCK_Y[5], z + BLOCK_Z[5]).tex( minU, maxV).endVertex();
        }

        {
            minU = texture.getMinU();
            maxU = texture.getMaxU();
            minV = texture.getMinV();
            maxV = texture.getMaxV();

            bufferBuilder.pos(x + BLOCK_X[5], y + BLOCK_Y[5], z + BLOCK_Z[5]).tex( maxU, maxV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[4], y + BLOCK_Y[4], z + BLOCK_Z[4]).tex( maxU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[3], y + BLOCK_Y[3], z + BLOCK_Z[3]).tex( minU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[2], y + BLOCK_Y[2], z + BLOCK_Z[2]).tex( minU, maxV).endVertex();
        }

        {
            minU = texture.getMinU();
            maxU = texture.getMaxU();
            minV = texture.getMinV();
            maxV = texture.getMaxV();

            bufferBuilder.pos(x + BLOCK_X[3], y + BLOCK_Y[3], z + BLOCK_Z[3]).tex( maxU, maxV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[4], y + BLOCK_Y[4], z + BLOCK_Z[4]).tex( maxU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[7], y + BLOCK_Y[7], z + BLOCK_Z[7]).tex( minU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[0], y + BLOCK_Y[0], z + BLOCK_Z[0]).tex( minU, maxV).endVertex();
        }

        {
            minU = texture.getMinU();
            maxU = texture.getMaxU();
            minV = texture.getMinV();
            maxV = texture.getMaxV();

            bufferBuilder.pos(x + BLOCK_X[2], y + BLOCK_Y[2], z + BLOCK_Z[2]).tex( maxU, maxV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[3], y + BLOCK_Y[3], z + BLOCK_Z[3]).tex( maxU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[0], y + BLOCK_Y[0], z + BLOCK_Z[0]).tex( minU, minV).endVertex();
            bufferBuilder.pos(x + BLOCK_X[1], y + BLOCK_Y[1], z + BLOCK_Z[1]).tex( minU, maxV).endVertex();
        }
    }

    public static void renderBlockInWorld(final Block block, final int meta, final float blockSize) {
        beginRenderingBlocksInWorld(blockSize);

        addRenderedBlockInWorld(block, meta, 0, 0, 0);

        endRenderingBlocksInWorld();
    }

    public static void renderOuterSpaceShell() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        FMLClientHandler.instance().getClient().getTextureManager()
                .bindTexture(new ResourceLocation(Tags.MODID ,"models/spacelayer.png"));
        final float scale = 0.01f * 17.5f;
        GL11.glScalef(scale, scale, scale);
        GL11.glColor4f(1, 1, 1, 1);
        spaceModel.renderAll();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
