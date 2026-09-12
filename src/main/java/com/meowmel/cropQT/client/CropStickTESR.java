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

        // 形状可以被配置全局覆盖——出问题时不用逐个改作物定义
        CropRenderType renderType = CropConfig.resolveRenderTypeOrDefault(type.getRenderType());

        if (renderType == CropRenderType.HASH) {
            drawHash(buf, plantHeight, u0, u1, vCrop, v1, sky, block);
        } else if (renderType == CropRenderType.FLOWER) {
            drawFlower(buf, plantHeight, u0, u1, vCrop, v1, sky, block);
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
     * FLOWER模式 - 四个面沿方块中线排成 # 字，向四边各外扩 2/16。
     *
     * <p>和 HASH 的区别在位置与尺寸：HASH 的面贴在离边 1/4 处、宽刚好 1 格；
     * FLOWER 的面在 1/4 与 3/4 处，并且两端各出界 2/16，让花比作物架宽一圈。
     * 布局取自源端的 FlowerPlantRenderer。
     */
    private void drawFlower(BufferBuilder buf, float h,
                            float u0, float u1, float v0, float v1,
                            int sky, int block) {
        // 面沿轴展开时两端各出界 2/16
        float out = -0.125f;
        float far = 1.125f;
        float near = 0.25f;
        float away = 0.75f;

        // 南北两个面 (沿X轴展开, Z 固定在 1/4 与 3/4)
        quadFlat(buf, out, 0, near, far, h, near, u0, u1, v0, v1, sky, block, 'z');
        quadFlat(buf, far, 0, near, out, h, near, u0, u1, v0, v1, sky, block, 'z');
        quadFlat(buf, out, 0, away, far, h, away, u0, u1, v0, v1, sky, block, 'z');
        quadFlat(buf, far, 0, away, out, h, away, u0, u1, v0, v1, sky, block, 'z');

        // 东西两个面 (沿Z轴展开, X 固定在 1/4 与 3/4)
        quadFlat(buf, near, 0, out, near, h, far, u0, u1, v0, v1, sky, block, 'x');
        quadFlat(buf, near, 0, far, near, h, out, u0, u1, v0, v1, sky, block, 'x');
        quadFlat(buf, away, 0, out, away, h, far, u0, u1, v0, v1, sky, block, 'x');
        quadFlat(buf, away, 0, far, away, h, out, u0, u1, v0, v1, sky, block, 'x');
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
