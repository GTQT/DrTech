package com.drppp.drtech.personalspace.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;

import com.drppp.drtech.personalspace.world.DimensionConfig;
import com.drppp.drtech.personalspace.world.RoadGrid;

/**
 * 2D 俯瞰预览：用 {@link RoadGrid} 的同一套规则，把地块边界 / 道路 / 路缘 / 虚线 / 中心标记
 * 画成一张 128x128 的贴图。颜色取自方块模型的平均色，不需要额外美术资源。
 */
@SideOnly(Side.CLIENT)
public class WorldPreviewWidget extends Widget<WorldPreviewWidget> {

    private static final int RES = 128;
    private static final Map<Long, Integer> COLOR_CACHE = new HashMap<>();
    private static final int FALLBACK_COLOR = 0xFF808080;

    private final DimensionConfig config;
    private DynamicTexture texture;
    private ResourceLocation location;
    private int lastHash = Integer.MIN_VALUE;
    private int pixelSize = RES;

    public WorldPreviewWidget(DimensionConfig config) {
        this.config = config;
    }

    @Override
    public WorldPreviewWidget size(int width, int height) {
        this.pixelSize = Math.min(width, height);
        return super.size(width, height);
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        updatePreview();
        if (location == null) {
            return;
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, pixelSize, pixelSize, RES, RES);
    }

    private void updatePreview() {
        int hash = computeHash();
        if (hash == lastHash) {
            return;
        }
        lastHash = hash;
        if (texture == null) {
            texture = new DynamicTexture(RES, RES);
            location = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("drtech_personal_preview", texture);
        }

        RoadGrid grid = RoadGrid.of(config);
        int[] data = texture.getTextureData();
        int periodX = Math.max(grid.periodX, 1);
        int periodZ = Math.max(grid.periodZ, 1);
        float scale = Math.max(Math.max(periodX, periodZ) * 2.0F / RES, 1.0F);
        int fallback = mainColor();

        for (int py = 0; py < RES; py++) {
            int worldZ = (int) (py * scale);
            for (int px = 0; px < RES; px++) {
                int worldX = (int) (px * scale);
                IBlockState state = grid.stateAt(worldX, worldZ);
                int color = state == null ? fallback : blockColor(state, fallback);
                data[py * RES + px] = color;
            }
        }
        texture.updateDynamicTexture();
    }

    private int computeHash() {
        int hash = config.getBoundaryChunkIntervalX() * 31 + config.getBoundaryChunkIntervalZ();
        hash = hash * 31 + config.getGapWidth();
        hash = hash * 31 + (config.getGapPreset() == null ? 0 : config.getGapPreset().ordinal());
        hash = hash * 31 + (config.isCenterEnabled() ? 1 : 0);
        hash = hash * 31 + (config.getCenterDirection() == null ? 0 : config.getCenterDirection().ordinal());
        hash = hash * 31 + specHash(config.getBoundaryBlockA(), config.getBoundaryMetaA());
        hash = hash * 31 + specHash(config.getBoundaryBlockB(), config.getBoundaryMetaB());
        hash = hash * 31 + specHash(config.getGapBlockA(), config.getGapMetaA());
        hash = hash * 31 + specHash(config.getGapBlockB(), config.getGapMetaB());
        hash = hash * 31 + specHash(config.getGapBlockC(), config.getGapMetaC());
        hash = hash * 31 + specHash(config.getCenterBlock(), config.getCenterMeta());
        hash = hash * 31 + config.getLayersAsString().hashCode();
        return hash;
    }

    private static int specHash(String spec, int meta) {
        return (spec == null ? 0 : spec.hashCode()) * 31 + meta;
    }

    private int mainColor() {
        List<FlatLayerInfo> layers = config.getLayers();
        if (layers != null) {
            for (int i = layers.size() - 1; i >= 0; i--) {
                IBlockState state = layers.get(i).getLayerMaterial();
                if (state != null && state.getBlock() != net.minecraft.init.Blocks.AIR) {
                    return blockColor(state, FALLBACK_COLOR);
                }
            }
        }
        return FALLBACK_COLOR;
    }

    private static int blockColor(IBlockState state, int fallback) {
        Block block = state.getBlock();
        if (block == null) {
            return fallback;
        }
        long key = ((long) Block.getIdFromBlock(block) << 32) ^ (state.hashCode() & 0xFFFFFFFFL);
        Integer cached = COLOR_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        int color = fallback;
        try {
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher()
                    .getBlockModelShapes().getTexture(state);
            if (sprite != null) {
                int[][] frames = sprite.getFrameTextureData(0);
                if (frames != null && frames.length > 0) {
                    color = averageColor(frames[0], fallback);
                }
            }
        } catch (Throwable ignored) {
        }
        COLOR_CACHE.put(key, color);
        return color;
    }

    private static int averageColor(int[] pixels, int fallback) {
        if (pixels == null || pixels.length == 0) {
            return fallback;
        }
        long r = 0;
        long g = 0;
        long b = 0;
        long weightSum = 0;
        for (int pixel : pixels) {
            int a = (pixel >>> 24) & 0xFF;
            if (a < 128) {
                continue;
            }
            int pr = (pixel >> 16) & 0xFF;
            int pg = (pixel >> 8) & 0xFF;
            int pb = pixel & 0xFF;
            int weight = Math.max(1, (pr + pg + pb) / 3);
            r += (long) pr * weight;
            g += (long) pg * weight;
            b += (long) pb * weight;
            weightSum += weight;
        }
        if (weightSum == 0) {
            return fallback;
        }
        int ar = (int) (r / weightSum);
        int ag = (int) (g / weightSum);
        int ab = (int) (b / weightSum);
        return 0xFF000000 | (ar << 16) | (ag << 8) | ab;
    }
}
