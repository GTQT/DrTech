package com.drppp.drtech.Client.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.common.multiblock.mover.PreviewBlockData;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.util.BlockInfo;
import gregtech.client.renderer.godforge.util.FaceCulledRenderBlocks;
import gregtech.client.renderer.godforge.util.FaceVisibility;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;
import gregtech.client.renderer.handler.PreviewRenderUtils;
import gregtech.client.utils.TrackedDummyWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Uses the same DummyWorld, layer VBO and dedicated MTE path as GTCEu's structure preview. */
final class MoverGhostModelRenderer {
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;
    private static final int GL_CONSTANT_ALPHA = 32771;
    private static final int GL_ONE_MINUS_CONSTANT_ALPHA = 32772;
    private static final FaceVisibility FULL_VISIBILITY = new FaceVisibility();
    private static final VertexBuffer[] VBO_BY_LAYER =
            new VertexBuffer[BlockRenderLayer.values().length];
    private static boolean built;

    private MoverGhostModelRenderer() {
    }

    static void rebuild(List<PreviewBlockData> blocks) {
        release();
        if (!DrtConfig.MultiblockMover.enableBlockModelPreview
                || blocks.isEmpty()
                || blocks.size() > DrtConfig.MultiblockMover.maxModelPreviewBlocks
                || !OpenGlHelper.useVbo()) {
            return;
        }

        Map<BlockPos, BlockInfo> blockInfo = new LinkedHashMap<>();
        for (PreviewBlockData block : blocks) {
            blockInfo.put(block.getRelativePos(), createBlockInfo(block));
        }

        TrackedDummyWorld dummyWorld = new TrackedDummyWorld();
        dummyWorld.addBlocks(blockInfo);
        FaceCulledRenderBlocks blockRenderer = new FaceCulledRenderBlocks(dummyWorld);
        PreviewRenderUtils.OffsetBlockAccess offsetAccess =
                new PreviewRenderUtils.OffsetBlockAccess(dummyWorld);
        BlockRenderLayer previousLayer = MinecraftForgeClient.getRenderLayer();
        try {
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                ForgeHooksClient.setRenderLayer(layer);
                buildLayer(layer, blockInfo, dummyWorld, blockRenderer, offsetAccess);
            }
            built = true;
        } catch (Throwable error) {
            DrTechMain.LOGGER.warn("Unable to build multiblock mover ghost model; using outline fallback", error);
            release();
        } finally {
            ForgeHooksClient.setRenderLayer(previousLayer);
        }
    }

    private static BlockInfo createBlockInfo(PreviewBlockData block) {
        IBlockState state = block.getBlockState();
        ResourceLocation metaId = block.getMetaTileEntityId();
        if (metaId == null) return new BlockInfo(state);
        try {
            MTERegistry registry = MTEManager.getInstance().getRegistry(metaId.getNamespace());
            MetaTileEntity prototype = registry == null ? null : registry.getObject(metaId);
            if (prototype == null) return new BlockInfo(state);
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            MetaTileEntity metaTileEntity = holder.setMetaTileEntity(prototype, null, null);
            metaTileEntity.setFrontFacing(EnumFacing.byIndex(block.getFrontFacing()));
            if (block.getPaintingColor() >= 0) {
                metaTileEntity.setPaintingColor(block.getPaintingColor());
            }
            return new BlockInfo(state, holder);
        } catch (Throwable error) {
            DrTechMain.LOGGER.debug("Unable to create mover preview MTE {}", metaId, error);
            return new BlockInfo(state);
        }
    }

    private static void buildLayer(BlockRenderLayer layer, Map<BlockPos, BlockInfo> blocks,
                                   TrackedDummyWorld dummyWorld,
                                   FaceCulledRenderBlocks blockRenderer,
                                   PreviewRenderUtils.OffsetBlockAccess offsetAccess) {
        BufferBuilder buffer = new BufferBuilder(BUFFER_SIZE);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        for (Map.Entry<BlockPos, BlockInfo> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            IBlockState state = entry.getValue().getBlockState();
            if (!state.getBlock().canRenderInLayer(state, layer)
                    || state.getRenderType() == EnumBlockRenderType.INVISIBLE) {
                continue;
            }
            if (state.getRenderType() == MetaTileEntityRenderer.BLOCK_RENDER_TYPE) {
                if (!(dummyWorld.getTileEntity(pos) instanceof MetaTileEntityHolder)) continue;
                offsetAccess.setPos(pos, pos, true);
                MetaTileEntityRenderer.INSTANCE.renderBlock(offsetAccess, pos, state, buffer);
            } else if (state.getRenderType() == EnumBlockRenderType.MODEL
                    || state.getRenderType() == EnumBlockRenderType.LIQUID) {
                blockRenderer.renderBlockScaled(state, pos, pos,
                        0.92F, 0.04F, FULL_VISIBILITY, buffer);
            }
        }
        if (buffer.getVertexCount() == 0) {
            buffer.finishDrawing();
            return;
        }
        buffer.finishDrawing();
        VertexBuffer vbo = new VertexBuffer(DefaultVertexFormats.BLOCK);
        vbo.bufferData(buffer.getByteBuffer());
        VBO_BY_LAYER[layer.ordinal()] = vbo;
    }

    static void render(BlockPos targetController, double cameraX, double cameraY, double cameraZ) {
        if (!built) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.translate(targetController.getX() - cameraX,
                targetController.getY() - cameraY, targetController.getZ() - cameraZ);
        minecraft.entityRenderer.enableLightmap();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.38F);
        GlStateManager.tryBlendFuncSeparate(GL_CONSTANT_ALPHA,
                GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        renderVbos();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        minecraft.entityRenderer.disableLightmap();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderVbos() {
        BlockRenderLayer previousLayer = MinecraftForgeClient.getRenderLayer();
        try {
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                VertexBuffer vbo = VBO_BY_LAYER[layer.ordinal()];
                if (vbo == null) continue;
                ForgeHooksClient.setRenderLayer(layer);
                vbo.bindBuffer();
                GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
                GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
                GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12);
                GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                vbo.drawArrays(GL11.GL_QUADS);
                GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                vbo.unbindBuffer();
            }
        } finally {
            ForgeHooksClient.setRenderLayer(previousLayer);
        }
    }

    static void release() {
        for (int i = 0; i < VBO_BY_LAYER.length; i++) {
            if (VBO_BY_LAYER[i] != null) {
                VBO_BY_LAYER[i].deleteGlBuffers();
                VBO_BY_LAYER[i] = null;
            }
        }
        built = false;
    }
}
