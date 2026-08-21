package com.drppp.drtech.Client.multiblock.mover;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Network.mover.StartMoverPreviewPacket;
import com.drppp.drtech.common.Items.MetaItems.DrMetaItems;
import com.drppp.drtech.common.multiblock.mover.PreviewBlockData;
import com.drppp.drtech.common.multiblock.mover.MoverTargeting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MultiblockMoverPreviewRenderer {
    private static UUID sessionId;
    private static int dimension = Integer.MIN_VALUE;
    private static BlockPos sourceController;
    private static List<PreviewBlockData> blocks = Collections.emptyList();
    private static Set<BlockPos> sourcePositions = Collections.emptySet();
    private static List<CachedPreviewBlock> cachedPreview = Collections.emptyList();
    private static BlockPos cachedTargetController;
    private static long lastCollisionRefreshTick = Long.MIN_VALUE;

    public static void start(StartMoverPreviewPacket packet) {
        sessionId = packet.getSessionId();
        dimension = packet.getDimension();
        sourceController = packet.getSourceController();
        blocks = Collections.unmodifiableList(new ArrayList<>(packet.getBlocks()));
        Set<BlockPos> positions = new HashSet<>();
        for (PreviewBlockData block : blocks) {
            positions.add(sourceController.add(
                    packet.getRotation().inverse(block.getRelativePos())));
        }
        sourcePositions = Collections.unmodifiableSet(positions);
        invalidateCollisionCache();
        MoverGhostModelRenderer.rebuild(blocks);
    }

    public static void clear(UUID requestedSession) {
        if (sessionId == null || requestedSession == null || sessionId.equals(requestedSession)) {
            clearAll();
        }
    }

    public static boolean isActive() {
        return sessionId != null;
    }

    public static UUID getSessionId() {
        return sessionId;
    }

    private static void clearAll() {
        sessionId = null;
        dimension = Integer.MIN_VALUE;
        sourceController = null;
        blocks = Collections.emptyList();
        sourcePositions = Collections.emptySet();
        invalidateCollisionCache();
        MoverGhostModelRenderer.release();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (sessionId == null || minecraft.world == null || minecraft.player == null
                || minecraft.world.provider.getDimension() != dimension || !isHoldingMover(minecraft)) {
            return;
        }

        RayTraceResult hit = minecraft.objectMouseOver;
        BlockPos targetController = MoverTargeting.resolve(
                minecraft.player, hit, event.getPartialTicks());
        refreshCollisionCache(minecraft, targetController);

        Entity camera = minecraft.getRenderViewEntity();
        if (camera == null) return;
        double partial = event.getPartialTicks();
        double cameraX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partial;
        double cameraY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partial;
        double cameraZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partial;

        MoverGhostModelRenderer.render(targetController, cameraX, cameraY, cameraZ);

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.glLineWidth(2.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (CachedPreviewBlock block : cachedPreview) {
            BlockPos destination = block.destination;
            float red;
            float green;
            float blue;
            if (block.status == CachedPreviewBlock.CONTROLLER) {
                red = 0.15F;
                green = 0.55F;
                blue = 1.0F;
            } else if (block.status == CachedPreviewBlock.OVERLAP) {
                red = 1.0F;
                green = 0.78F;
                blue = 0.12F;
            } else if (block.status == CachedPreviewBlock.REPLACEABLE) {
                red = 0.18F;
                green = 1.0F;
                blue = 0.32F;
            } else {
                red = 1.0F;
                green = 0.18F;
                blue = 0.18F;
            }
            appendBox(buffer, destination, cameraX, cameraY, cameraZ, red, green, blue, 0.9F);
        }
        tessellator.draw();

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private static void refreshCollisionCache(Minecraft minecraft, BlockPos targetController) {
        long worldTick = minecraft.world.getTotalWorldTime();
        int refreshTicks = Math.max(1, DrtConfig.MultiblockMover.previewCollisionRefreshTicks);
        if (targetController.equals(cachedTargetController)
                && worldTick >= lastCollisionRefreshTick
                && worldTick - lastCollisionRefreshTick < refreshTicks) {
            return;
        }
        List<CachedPreviewBlock> refreshed = new ArrayList<>(blocks.size());
        double maxDistance = DrtConfig.MultiblockMover.maxDistance;
        boolean globallyInvalid = targetController.equals(sourceController)
                || minecraft.player.getDistanceSqToCenter(targetController) > maxDistance * maxDistance;
        for (PreviewBlockData block : blocks) {
            BlockPos destination = targetController.add(block.getRelativePos());
            byte status;
            if (globallyInvalid || !isDestinationInWorld(minecraft, destination)) {
                status = CachedPreviewBlock.BLOCKED;
            } else if (sourcePositions.contains(destination)) {
                status = CachedPreviewBlock.OVERLAP;
            } else if (canReplace(minecraft, destination)) {
                status = block.isController()
                        ? CachedPreviewBlock.CONTROLLER
                        : CachedPreviewBlock.REPLACEABLE;
            } else {
                status = CachedPreviewBlock.BLOCKED;
            }
            refreshed.add(new CachedPreviewBlock(destination, status));
        }
        cachedPreview = Collections.unmodifiableList(refreshed);
        cachedTargetController = targetController.toImmutable();
        lastCollisionRefreshTick = worldTick;
    }

    private static void invalidateCollisionCache() {
        cachedPreview = Collections.emptyList();
        cachedTargetController = null;
        lastCollisionRefreshTick = Long.MIN_VALUE;
    }

    private static boolean isHoldingMover(Minecraft minecraft) {
        if (DrMetaItems.MULTIBLOCK_MOVER == null) return false;
        ItemStack main = minecraft.player.getHeldItemMainhand();
        ItemStack off = minecraft.player.getHeldItemOffhand();
        return DrMetaItems.MULTIBLOCK_MOVER.isItemEqual(main)
                || DrMetaItems.MULTIBLOCK_MOVER.isItemEqual(off);
    }

    private static boolean canReplace(Minecraft minecraft, BlockPos pos) {
        if (minecraft.world.getTileEntity(pos) != null) return false;
        if (minecraft.world.isAirBlock(pos)) return true;
        IBlockState state = minecraft.world.getBlockState(pos);
        return state.getBlock().isReplaceable(minecraft.world, pos);
    }

    private static boolean isDestinationInWorld(Minecraft minecraft, BlockPos pos) {
        return pos.getY() >= 0 && pos.getY() < minecraft.world.getActualHeight()
                && minecraft.world.isBlockLoaded(pos);
    }

    private static void appendBox(BufferBuilder buffer, BlockPos pos,
                                  double cameraX, double cameraY, double cameraZ,
                                  float red, float green, float blue, float alpha) {
        double x0 = pos.getX() - cameraX + 0.002D;
        double y0 = pos.getY() - cameraY + 0.002D;
        double z0 = pos.getZ() - cameraZ + 0.002D;
        double x1 = pos.getX() - cameraX + 0.998D;
        double y1 = pos.getY() - cameraY + 0.998D;
        double z1 = pos.getZ() - cameraZ + 0.998D;

        line(buffer, x0, y0, z0, x1, y0, z0, red, green, blue, alpha);
        line(buffer, x1, y0, z0, x1, y0, z1, red, green, blue, alpha);
        line(buffer, x1, y0, z1, x0, y0, z1, red, green, blue, alpha);
        line(buffer, x0, y0, z1, x0, y0, z0, red, green, blue, alpha);
        line(buffer, x0, y1, z0, x1, y1, z0, red, green, blue, alpha);
        line(buffer, x1, y1, z0, x1, y1, z1, red, green, blue, alpha);
        line(buffer, x1, y1, z1, x0, y1, z1, red, green, blue, alpha);
        line(buffer, x0, y1, z1, x0, y1, z0, red, green, blue, alpha);
        line(buffer, x0, y0, z0, x0, y1, z0, red, green, blue, alpha);
        line(buffer, x1, y0, z0, x1, y1, z0, red, green, blue, alpha);
        line(buffer, x1, y0, z1, x1, y1, z1, red, green, blue, alpha);
        line(buffer, x0, y0, z1, x0, y1, z1, red, green, blue, alpha);
    }

    private static void line(BufferBuilder buffer,
                             double x0, double y0, double z0,
                             double x1, double y1, double z1,
                             float red, float green, float blue, float alpha) {
        buffer.pos(x0, y0, z0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) clearAll();
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Post event) {
        if (sessionId != null) MoverGhostModelRenderer.rebuild(blocks);
    }

    private static final class CachedPreviewBlock {
        private static final byte CONTROLLER = 0;
        private static final byte OVERLAP = 1;
        private static final byte REPLACEABLE = 2;
        private static final byte BLOCKED = 3;

        private final BlockPos destination;
        private final byte status;

        private CachedPreviewBlock(BlockPos destination, byte status) {
            this.destination = destination.toImmutable();
            this.status = status;
        }
    }
}
