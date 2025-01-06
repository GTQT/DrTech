package com.drppp.drtech.Client.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.uv.IconTransformation;
import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Tags;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.laser.LaserPipeType;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class LaserPipeRenderer extends PipeRenderer {
    public static final LaserPipeRenderer INSTANCE = new LaserPipeRenderer();
    private final EnumMap<LaserPipeType, TextureAtlasSprite> pipeTextures = new EnumMap(LaserPipeType.class);
    private boolean active = false;

    public LaserPipeRenderer() {
        super("gt_laser_pipe", new ResourceLocation(Tags.MODID, "laser_pipe"));
    }

    public void registerIcons(TextureMap map) {
        this.pipeTextures.put(LaserPipeType.NORMAL, Textures.LASER_PIPE_IN);
    }

    public void buildRenderer(PipeRenderer.PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, @Nullable IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (pipeType instanceof LaserPipeType) {
            renderContext.addOpenFaceRender(new IconTransformation(this.pipeTextures.get(pipeType))).addSideRender(false, new IconTransformation(Textures.LASER_PIPE_SIDE));
            if (pipeTile != null && pipeTile.isPainted()) {
                renderContext.addSideRender(new IconTransformation(Textures.LASER_PIPE_OVERLAY));
            }

            boolean var10001;
            label21:
            {
                if (!ConfigHolder.client.preventAnimatedCables && pipeTile instanceof TileEntityLaserPipe laserPipe) {
                    if (laserPipe.isActive()) {
                        var10001 = true;
                        break label21;
                    }
                }

                var10001 = false;
            }

            this.active = var10001;
        }

    }

    protected void renderOtherLayers(BlockRenderLayer layer, CCRenderState renderState, PipeRenderer.PipeRenderContext renderContext) {
        if (this.active && layer == BloomEffectUtil.getEffectiveBloomLayer() && (renderContext.getConnections() & 63) != 0) {
            Cuboid6 innerCuboid = BlockPipe.getSideBox(null, renderContext.getPipeThickness());
            if ((renderContext.getConnections() & 63) != 0) {
                EnumFacing[] var5 = EnumFacing.VALUES;
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    EnumFacing side = var5[var7];
                    if ((renderContext.getConnections() & 1 << side.getIndex()) == 0) {
                        int oppositeIndex = side.getOpposite().getIndex();
                        if ((renderContext.getConnections() & 1 << oppositeIndex) <= 0 || (renderContext.getConnections() & 63 & ~(1 << oppositeIndex)) != 0) {
                            IVertexOperation[] ops = new IVertexOperation[0];
                            ops = ArrayUtils.addAll(ops, new IVertexOperation[]{new IconTransformation(Textures.LASER_PIPE_OVERLAY_EMISSIVE)});
                            this.renderFace(renderState, ops, side, innerCuboid);
                        }
                    } else {
                        Cuboid6 sideCuboid = BlockPipe.getSideBox(side, renderContext.getPipeThickness());
                        EnumFacing[] var10 = EnumFacing.VALUES;
                        int var11 = var10.length;

                        for (int var12 = 0; var12 < var11; ++var12) {
                            EnumFacing connectionSide = var10[var12];
                            if (connectionSide.getAxis() != side.getAxis()) {
                                IVertexOperation[] ops = new IVertexOperation[0];
                                ops = ArrayUtils.addAll(ops, new IVertexOperation[]{new IconTransformation(Textures.LASER_PIPE_OVERLAY_EMISSIVE)});
                                this.renderFace(renderState, ops, connectionSide, sideCuboid);
                            }
                        }
                    }
                }

            }
        }

    }


    protected boolean canRenderInLayer(BlockRenderLayer layer) {
        return super.canRenderInLayer(layer) || layer == BloomEffectUtil.getEffectiveBloomLayer();
    }

    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        return Textures.LASER_PIPE_SIDE;
    }
}
