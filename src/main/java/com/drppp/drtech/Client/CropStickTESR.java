package com.drppp.drtech.Client;

import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileCropStick;
import com.drppp.drtech.api.crop.CropRenderType;
import com.drppp.drtech.api.crop.CropType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * 作物架TESR - 根据CropRenderType渲染植物
 *
 * CROSS模式: 两个45度对角面 (原版小麦风格)
 *   \  /
 *    \/
 *    /\
 *   /  \
 *
 * HASH模式: 四个面沿方块四边排列 (原版甘蔗/地狱疣风格)
 *   +--+
 *   |  |
 *   +--+
 */
@SideOnly(Side.CLIENT)
public class CropStickTESR extends TileEntitySpecialRenderer<TileCropStick> {

    @Override
    public void render(TileCropStick te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.hasCrop()) return;

        CropType type = te.getCropType();
        if (type == null) return;

        int stage = Math.min(te.getGrowthStage(), type.getMaxGrowthStage());

        // 获取贴图
        String texPath = Tags.MODID + ":blocks/crop/" + te.getCropId() + "/stage_" + stage;
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texPath);

        TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        if (sprite == missing) {
            // fallback到stage_0
            texPath = Tags.MODID + ":blocks/crop/" + te.getCropId() + "/stage_0";
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texPath);
            if (sprite == missing) return;
        }

        // 植物高度
        int maxStage = type.getMaxGrowthStage();
        float plantHeight = maxStage > 0 ? (0.2f + 0.75f * ((float) stage / maxStage)) : 0.5f;

        // 光照
        int combinedLight = te.getWorld().getCombinedLight(te.getPos().up(), 0);
        int sky = (combinedLight >> 16) & 0xFFFF;
        int block = combinedLight & 0xFFFF;

        // UV
        float u0 = sprite.getMinU();
        float u1 = sprite.getMaxU();
        float v0 = sprite.getMinV();
        float v1 = sprite.getMaxV();
        // 根据植物高度裁剪UV顶部
        float vCrop = v0 + (1.0f - plantHeight) * (v1 - v0);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        CropRenderType renderType = type.getRenderType();

        if (renderType == CropRenderType.HASH) {
            drawHash(buf, plantHeight, u0, u1, vCrop, v1, sky, block);
        } else {
            drawCross(buf, plantHeight, u0, u1, vCrop, v1, sky, block);
        }

        tess.draw();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    /**
     * CROSS模式 - 两个45°对角面
     */
    private void drawCross(BufferBuilder buf, float h,
                           float u0, float u1, float v0, float v1,
                           int sky, int block) {
        // 面1: 从(0.15, 0, 0.15)到(0.85, h, 0.85)
        quad(buf, 0.15f, 0, 0.15f, 0.85f, h, 0.85f, u0, u1, v0, v1, sky, block);
        // 面1反面
        quad(buf, 0.85f, 0, 0.85f, 0.15f, h, 0.15f, u0, u1, v0, v1, sky, block);
        // 面2: 从(0.85, 0, 0.15)到(0.15, h, 0.85)
        quad(buf, 0.85f, 0, 0.15f, 0.15f, h, 0.85f, u0, u1, v0, v1, sky, block);
        // 面2反面
        quad(buf, 0.15f, 0, 0.85f, 0.85f, h, 0.15f, u0, u1, v0, v1, sky, block);
    }

    /**
     * HASH模式 - 四个面沿方块四边排列
     * 类似 #字型，从上方看:
     *
     *   ----  (北面, z=0.25)
     *  |    |
     *  |    | (西面x=0.25, 东面x=0.75)
     *  |    |
     *   ----  (南面, z=0.75)
     */
    private void drawHash(BufferBuilder buf, float h,
                          float u0, float u1, float v0, float v1,
                          int sky, int block) {
        float inset = 0.25f;
        float outer = 1.0f - inset;

        // 北面 (沿X轴, z=inset) 正反
        quadFlat(buf, 0, 0, inset, 1, h, inset, u0, u1, v0, v1, sky, block, 'z');
        quadFlat(buf, 1, 0, inset, 0, h, inset, u0, u1, v0, v1, sky, block, 'z');
        // 南面 (沿X轴, z=outer) 正反
        quadFlat(buf, 0, 0, outer, 1, h, outer, u0, u1, v0, v1, sky, block, 'z');
        quadFlat(buf, 1, 0, outer, 0, h, outer, u0, u1, v0, v1, sky, block, 'z');
        // 西面 (沿Z轴, x=inset) 正反
        quadFlat(buf, inset, 0, 0, inset, h, 1, u0, u1, v0, v1, sky, block, 'x');
        quadFlat(buf, inset, 0, 1, inset, h, 0, u0, u1, v0, v1, sky, block, 'x');
        // 东面 (沿Z轴, x=outer) 正反
        quadFlat(buf, outer, 0, 0, outer, h, 1, u0, u1, v0, v1, sky, block, 'x');
        quadFlat(buf, outer, 0, 1, outer, h, 0, u0, u1, v0, v1, sky, block, 'x');
    }

    /**
     * 绘制一个对角四边形(CROSS用)
     * 从(x0,y0,z0)底部到(x1,y1,z1)顶部
     */
    private void quad(BufferBuilder buf,
                      float x0, float y0, float z0,
                      float x1, float y1, float z1,
                      float u0, float u1, float v0, float v1,
                      int sky, int block) {
        buf.pos(x0, y0, z0).tex(u0, v1).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
        buf.pos(x0, y1, z0).tex(u0, v0).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
        buf.pos(x1, y1, z1).tex(u1, v0).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
        buf.pos(x1, y0, z1).tex(u1, v1).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
    }

    /**
     * 绘制一个平行于某轴的四边形(HASH用)
     * axis='z': 面朝南北(沿X轴展开)
     * axis='x': 面朝东西(沿Z轴展开)
     */
    private void quadFlat(BufferBuilder buf,
                          float x0, float y0, float z0,
                          float x1, float y1, float z1,
                          float u0, float u1, float v0, float v1,
                          int sky, int block, char axis) {
        if (axis == 'z') {
            // 沿X轴展开, Z固定
            float z = z0;
            buf.pos(x0, y0, z).tex(u0, v1).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
            buf.pos(x0, y1, z).tex(u0, v0).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
            buf.pos(x1, y1, z).tex(u1, v0).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
            buf.pos(x1, y0, z).tex(u1, v1).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
        } else {
            // 沿Z轴展开, X固定
            float x = x0;
            buf.pos(x, y0, z0).tex(u0, v1).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
            buf.pos(x, y1, z0).tex(u0, v0).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
            buf.pos(x, y1, z1).tex(u1, v0).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
            buf.pos(x, y0, z1).tex(u1, v1).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
        }
    }
}
