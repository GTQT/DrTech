package com.meowmel.cropQT.client;

import com.drppp.drtech.Tags;
import com.meowmel.cropQT.tile.TileCropStick;
import com.meowmel.cropQT.api.CropRenderType;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.handler.CropConfig;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // ==================== 帧解析 ====================

    /** 一个目录最多探多少帧。目前最长的是 8 帧（trollplant），留足余量。 */
    private static final int MAX_FRAMES = 16;

    /** 目录名 → 该目录实际存在的帧（按阶段升序）。 */
    private static final Map<String, TextureAtlasSprite[]> FRAME_CACHE = new HashMap<>();

    /** 上次缓存对应的贴图图集；资源包重载会换一个新实例，靠它作废缓存。 */
    private static TextureMap cachedAtlas;

    /**
     * 取某个作物在第 {@code stage} 阶段的贴图；一帧都没有时返回 {@code null}。
     *
     * <p>素材里存在<b>两套命名</b>：新的 {@code stage_N.png}（N 从 0 开始），
     * 以及一批老的 {@code N.png}（N 从 1 开始，且往往少几帧）。
     * 这里把整个目录探一遍拿到实际帧数，再把请求的阶段<b>夹进可用区间</b> ——
     * 素材少几帧时最坏也只是「成熟期用倒数第二帧」，不会整株不显示。
     */
    @Nullable
    private static TextureAtlasSprite frameOf(String texturePath, int stage) {
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
        if (atlas != cachedAtlas) {
            // 资源包重载过——旧的 sprite 已经失效，整个缓存作废
            cachedAtlas = atlas;
            FRAME_CACHE.clear();
        }
        TextureAtlasSprite[] frames = FRAME_CACHE.computeIfAbsent(texturePath, p -> probeFrames(atlas, p));
        if (frames.length == 0) {
            return null;
        }
        return frames[Math.min(Math.max(stage, 0), frames.length - 1)];
    }

    /** 把一个目录的帧按顺序探出来。两种命名都认，谁先探到用谁。 */
    private static TextureAtlasSprite[] probeFrames(TextureMap atlas, String texturePath) {
        TextureAtlasSprite missing = atlas.getMissingSprite();
        List<TextureAtlasSprite> frames = new ArrayList<>();
        String base = Tags.MODID + ":blocks/crop/" + texturePath + "/";
        for (int i = 0; i < MAX_FRAMES; i++) {
            TextureAtlasSprite sprite = atlas.getAtlasSprite(base + "stage_" + i);
            if (sprite == missing) {
                sprite = atlas.getAtlasSprite(base + i);
            }
            // 不能中途 break：老命名从 1 开始，第 0 帧本来就缺
            if (sprite != missing) {
                frames.add(sprite);
            }
        }
        return frames.toArray(new TextureAtlasSprite[0]);
    }

    @Override
    public void render(TileCropStick te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.hasCrop()) return;

        CropType type = te.getCropType();
        if (type == null) return;

        int stage = Math.min(te.getGrowthStage(), type.getMaxGrowthStage());

        TextureAtlasSprite sprite = frameOf(type.getTexturePath(), stage);
        if (sprite == null) return;

        // 光照
        int combinedLight = te.getWorld().getCombinedLight(te.getPos().up(), 0);
        int sky = (combinedLight >> 16) & 0xFFFF;
        int block = combinedLight & 0xFFFF;

        // 源端三种株型都是固定几何 + 整图 UV：阶段只决定用哪一帧，
        // 不再按进度缩放高度或裁剪 UV（贴图本身已经画满整张图）。
        float u0 = sprite.getMinU();
        float u1 = sprite.getMaxU();
        float v0 = sprite.getMinV();
        float v1 = sprite.getMaxV();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        // 形状可以被配置全局覆盖——出问题时不用逐个改作物定义
        CropRenderType renderType = CropConfig.resolveRenderTypeOrDefault(type.getRenderType());

        if (renderType == CropRenderType.HASH) {
            drawHash(buf, u0, u1, v0, v1, sky, block);
        } else if (renderType == CropRenderType.FLOWER) {
            drawFlower(buf, u0, u1, v0, v1, sky, block);
        } else {
            drawCross(buf, u0, u1, v0, v1, sky, block);
        }

        tess.draw();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    /**
     * CROSS / X 模式 - 两个 45° 对角面。
     *
     * <p>几何逐字取自源端 {@code XPlantRenderer}：高 12/16，四角内缩 2/16，
     * UV 用整张图，四个面各画正反两面。
     */
    private void drawCross(BufferBuilder buf,
                           float u0, float u1, float v0, float v1,
                           int sky, int block) {
        final float lo = 2.0f / 16.0f;
        final float hi = 14.0f / 16.0f;
        final float h = 12.0f / 16.0f;

        // plane 1 front
        quadV(buf, lo, 0, lo, u1, v1,
            lo, h, lo, u1, v0,
            hi, h, hi, u0, v0,
            hi, 0, hi, u0, v1, sky, block);
        // plane 1 back
        quadV(buf, hi, 0, hi, u1, v1,
            hi, h, hi, u1, v0,
            lo, h, lo, u0, v0,
            lo, 0, lo, u0, v1, sky, block);
        // plane 2 front
        quadV(buf, lo, 0, hi, u0, v1,
            hi, 0, lo, u1, v1,
            hi, h, lo, u1, v0,
            lo, h, hi, u0, v0, sky, block);
        // plane 2 back
        quadV(buf, hi, 0, lo, u0, v1,
            lo, 0, hi, u1, v1,
            lo, h, hi, u1, v0,
            hi, h, lo, u0, v0, sky, block);
    }

    /**
     * HASH 模式 - 四个面沿方块四边排列，整格高。
     *
     * <p>几何逐字取自源端 {@code HashPlantRenderer}：面固定在 x=4/16、z=4/16、
     * z=12/16、x=12/16，沿轴从 0 铺到 1，各自正反两面。
     */
    private void drawHash(BufferBuilder buf,
                          float u0, float u1, float v0, float v1,
                          int sky, int block) {
        final float in = 4.0f / 16.0f;
        final float out = 12.0f / 16.0f;
        final float h = 1.0f;

        // plane 1 front / back（z = 4/16，沿 X 展开）
        quadV(buf, 0, 0, in, u1, v1,
            0, h, in, u1, v0,
            1, h, in, u0, v0,
            1, 0, in, u0, v1, sky, block);
        quadV(buf, 0, 0, in, u1, v1,
            1, 0, in, u0, v1,
            1, h, in, u0, v0,
            0, h, in, u1, v0, sky, block);
        // plane 2 front / back（x = 4/16，沿 Z 展开）
        quadV(buf, in, 0, 0, u0, v1,
            in, 0, 1, u1, v1,
            in, h, 1, u1, v0,
            in, h, 0, u0, v0, sky, block);
        quadV(buf, in, 0, 0, u0, v1,
            in, h, 0, u0, v0,
            in, h, 1, u1, v0,
            in, 0, 1, u1, v1, sky, block);
        // plane 3 front / back（z = 12/16，沿 X 展开）
        quadV(buf, 0, 0, out, u0, v1,
            1, 0, out, u1, v1,
            1, h, out, u1, v0,
            0, h, out, u0, v0, sky, block);
        quadV(buf, 0, 0, out, u0, v1,
            0, h, out, u0, v0,
            1, h, out, u1, v0,
            1, 0, out, u1, v1, sky, block);
        // plane 4 front / back（x = 12/16，沿 Z 展开）
        quadV(buf, out, 0, 1, u0, v1,
            out, 0, 0, u1, v1,
            out, h, 0, u1, v0,
            out, h, 1, u0, v0, sky, block);
        quadV(buf, out, 0, 1, u0, v1,
            out, h, 1, u0, v0,
            out, h, 0, u1, v0,
            out, 0, 0, u1, v1, sky, block);
    }

    /**
     * FLOWER 模式 - 花比作物架宽一圈，同一面用两段错位贴图拼出来。
     *
     * <p>几何逐字取自源端 {@code FlowerPlantRenderer}：高 12/16，四个方向各两段
     * （6..18 与 -2..10，单位 1/16），并用 0.002/16 的深度错位避免 z-fighting。
     */
    private void drawFlower(BufferBuilder buf,
                            float u0, float u1, float v0, float v1,
                            int sky, int block) {
        final float h = 12.0f / 16.0f;

        final float z4a = 4.001f / 16.0f;
        final float z4b = 3.999f / 16.0f;
        final float x4a = 3.999f / 16.0f;
        final float x4b = 4.001f / 16.0f;
        final float z12a = 11.999f / 16.0f;
        final float z12b = 12.001f / 16.0f;
        final float x12a = 11.999f / 16.0f;
        final float x12b = 12.001f / 16.0f;

        final float left = -2.0f / 16.0f;
        final float midStart = 6.0f / 16.0f;
        final float midEnd = 10.0f / 16.0f;
        final float right = 18.0f / 16.0f;

        // plane 1 front / back（z ≈ 4/16）
        quadV(buf, midStart, 0, z4a, u1, v1,
            midStart, h, z4a, u1, v0,
            right, h, z4a, u0, v0,
            right, 0, z4a, u0, v1, sky, block);
        quadV(buf, left, 0, z4b, u1, v1,
            left, h, z4b, u1, v0,
            midEnd, h, z4b, u0, v0,
            midEnd, 0, z4b, u0, v1, sky, block);
        quadV(buf, midStart, 0, z4a, u1, v1,
            right, 0, z4a, u0, v1,
            right, h, z4a, u0, v0,
            midStart, h, z4a, u1, v0, sky, block);
        quadV(buf, left, 0, z4b, u1, v1,
            midEnd, 0, z4b, u0, v1,
            midEnd, h, z4b, u0, v0,
            left, h, z4b, u1, v0, sky, block);

        // plane 2 front / back（x ≈ 4/16）
        quadV(buf, x4a, 0, midStart, u0, v1,
            x4a, 0, right, u1, v1,
            x4a, h, right, u1, v0,
            x4a, h, midStart, u0, v0, sky, block);
        quadV(buf, x4b, 0, left, u0, v1,
            x4b, 0, midEnd, u1, v1,
            x4b, h, midEnd, u1, v0,
            x4b, h, left, u0, v0, sky, block);
        quadV(buf, x4a, 0, midStart, u0, v1,
            x4a, h, midStart, u0, v0,
            x4a, h, right, u1, v0,
            x4a, 0, right, u1, v1, sky, block);
        quadV(buf, x4b, 0, left, u0, v1,
            x4b, h, left, u0, v0,
            x4b, h, midEnd, u1, v0,
            x4b, 0, midEnd, u1, v1, sky, block);

        // plane 3 front / back（z ≈ 12/16）
        quadV(buf, midStart, 0, z12a, u0, v1,
            right, 0, z12a, u1, v1,
            right, h, z12a, u1, v0,
            midStart, h, z12a, u0, v0, sky, block);
        quadV(buf, left, 0, z12b, u0, v1,
            midEnd, 0, z12b, u1, v1,
            midEnd, h, z12b, u1, v0,
            left, h, z12b, u0, v0, sky, block);
        quadV(buf, midStart, 0, z12a, u0, v1,
            midStart, h, z12a, u0, v0,
            right, h, z12a, u1, v0,
            right, 0, z12a, u1, v1, sky, block);
        quadV(buf, left, 0, z12b, u0, v1,
            left, h, z12b, u0, v0,
            midEnd, h, z12b, u1, v0,
            midEnd, 0, z12b, u1, v1, sky, block);

        // plane 4 front / back（x ≈ 12/16）
        quadV(buf, x12a, 0, right, u0, v1,
            x12a, 0, midStart, u1, v1,
            x12a, h, midStart, u1, v0,
            x12a, h, right, u0, v0, sky, block);
        quadV(buf, x12b, 0, midEnd, u0, v1,
            x12b, 0, left, u1, v1,
            x12b, h, left, u1, v0,
            x12b, h, midEnd, u0, v0, sky, block);
        quadV(buf, x12a, 0, right, u0, v1,
            x12a, h, right, u0, v0,
            x12a, h, midStart, u1, v0,
            x12a, 0, midStart, u1, v1, sky, block);
        quadV(buf, x12b, 0, midEnd, u0, v1,
            x12b, h, midEnd, u0, v0,
            x12b, h, left, u1, v0,
            x12b, 0, left, u1, v1, sky, block);
    }

    /** 四个顶点各带自己的 UV——株型的正反面 UV 会镜像，不能共用同一组角。 */
    private void quadV(BufferBuilder buf,
                       float x0, float y0, float z0, float uA, float vA,
                       float x1, float y1, float z1, float uB, float vB,
                       float x2, float y2, float z2, float uC, float vC,
                       float x3, float y3, float z3, float uD, float vD,
                       int sky, int block) {
        vertex(buf, x0, y0, z0, uA, vA, sky, block);
        vertex(buf, x1, y1, z1, uB, vB, sky, block);
        vertex(buf, x2, y2, z2, uC, vC, sky, block);
        vertex(buf, x3, y3, z3, uD, vD, sky, block);
    }

    private void vertex(BufferBuilder buf, float x, float y, float z,
                        float u, float v, int sky, int block) {
        buf.pos(x, y, z).tex(u, v).lightmap(sky, block).color(255, 255, 255, 255).endVertex();
    }
}
