package com.drppp.drtech.Client;

import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileCropStick;
import com.drppp.drtech.common.Blocks.Crops.CropType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * 作物架TESR渲染器
 *
 * 渲染逻辑:
 * 1. 作物架框架由blockstate模型处理(单层/双层)
 * 2. 此TESR只负责渲染植物部分
 * 3. 贴图路径: textures/blocks/crop/作物名/stage_N.png
 * 4. 植物渲染为两个十字交叉的面(类似原版小麦)
 */
@SideOnly(Side.CLIENT)
public class CropStickTESR extends TileEntitySpecialRenderer<TileCropStick> {

    @Override
    public void render(TileCropStick te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.hasCrop()) return;

        String cropId = te.getCropId();
        int stage = te.getGrowthStage();
        CropType type = te.getCropType();
        if (type == null) return;

        // 限制stage范围
        int maxStage = type.getMaxGrowthStage();
        if (stage > maxStage) stage = maxStage;

        // 获取贴图: drtech:blocks/crop/作物名/stage_N
        String texturePath = Tags.MODID + ":blocks/crop/" + cropId + "/stage_" + stage;
        TextureAtlasSprite sprite = Minecraft.getMinecraft()
                .getTextureMapBlocks().getAtlasSprite(texturePath);

        // 如果找不到，尝试用stage 0
        if (sprite == null || sprite == Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
            texturePath = Tags.MODID + ":blocks/crop/" + cropId + "/stage_0";
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texturePath);
        }

        // 仍找不到，跳过渲染
        if (sprite == null || sprite == Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
            return;
        }

        // 计算植物高度 (随生长阶段变化)
        float plantHeight;
        if (maxStage > 0) {
            plantHeight = 0.2f + 0.75f * ((float) stage / maxStage);
        } else {
            plantHeight = 0.5f;
        }

        // 绘制
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 设置光照
        int light = te.getWorld().getCombinedLight(te.getPos().up(), 0);
        int skyLight = light >> 16 & 0xFFFF;
        int blockLight = light & 0xFFFF;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float u0 = sprite.getMinU();
        float u1 = sprite.getMaxU();
        float v0 = sprite.getMinV();
        float v1 = sprite.getMaxV();

        // 调整UV: 只显示植物高度对应的部分
        float vOffset = (1.0f - plantHeight) * (v1 - v0);
        float vStart = v0 + vOffset;

        // 绘制两个十字交叉面
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        // 面1: 沿对角线 (NW-SE)
        drawCrossPlane(buf, 0.5f, 0, 0.5f, plantHeight, 0.5f,
                u0, u1, vStart, v1, skyLight, blockLight, 0);

        // 面2: 沿对角线 (NE-SW) 旋转90度
        drawCrossPlane(buf, 0.5f, 0, 0.5f, plantHeight, 0.5f,
                u0, u1, vStart, v1, skyLight, blockLight, 90);

        tess.draw();

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * 绘制一个十字面(正反两面)
     */
    private void drawCrossPlane(BufferBuilder buf,
                                 float cx, float cy, float cz,
                                 float height, float halfWidth,
                                 float u0, float u1, float v0, float v1,
                                 int skyLight, int blockLight,
                                 float rotationDeg) {
        // 计算旋转后的偏移
        double rad = Math.toRadians(rotationDeg);
        float dx = (float) (Math.cos(rad) * halfWidth);
        float dz = (float) (Math.sin(rad) * halfWidth);

        float x0 = cx - dx;
        float z0 = cz - dz;
        float x1 = cx + dx;
        float z1 = cz + dz;

        // 正面
        buf.pos(x0, cy, z0).tex(u0, v1).lightmap(skyLight, blockLight).color(255, 255, 255, 255).endVertex();
        buf.pos(x0, cy + height, z0).tex(u0, v0).lightmap(skyLight, blockLight).color(255, 255, 255, 255).endVertex();
        buf.pos(x1, cy + height, z1).tex(u1, v0).lightmap(skyLight, blockLight).color(255, 255, 255, 255).endVertex();
        buf.pos(x1, cy, z1).tex(u1, v1).lightmap(skyLight, blockLight).color(255, 255, 255, 255).endVertex();

        // 反面
        buf.pos(x1, cy, z1).tex(u1, v1).lightmap(skyLight, blockLight).color(255, 255, 255, 255).endVertex();
        buf.pos(x1, cy + height, z1).tex(u1, v0).lightmap(skyLight, blockLight).color(255, 255, 255, 255).endVertex();
        buf.pos(x0, cy + height, z0).tex(u0, v0).lightmap(skyLight, blockLight).color(255, 255, 255, 255).endVertex();
        buf.pos(x0, cy, z0).tex(u0, v1).lightmap(skyLight, blockLight).color(255, 255, 255, 255).endVertex();
    }
}
