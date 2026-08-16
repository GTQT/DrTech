package com.drppp.drtech.Client.drone;

import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Client-only world overlay for editor area projections and live loop coordinates. */
public final class DroneWorldPreviewRenderer {

    private static DroneArea projectedArea;
    private static int projectedDimension = Integer.MIN_VALUE;
    private static BlockPos executionCoordinate;
    private static int executionDimension = Integer.MIN_VALUE;
    private static List<BlockPos> navigationPath = Collections.emptyList();
    private static int navigationDimension = Integer.MIN_VALUE;
    private static int trackedEntityId = -1;
    private static int trackedDimension = Integer.MIN_VALUE;
    private static boolean trackExecutionCoordinate;
    private static boolean trackNavigationPath;

    public static void trackLiveDrone(int entityId, boolean coordinateEnabled, boolean pathEnabled) {
        Minecraft minecraft = Minecraft.getMinecraft();
        trackedEntityId = coordinateEnabled || pathEnabled ? entityId : -1;
        trackedDimension = minecraft.world == null ? Integer.MIN_VALUE : minecraft.world.provider.getDimension();
        trackExecutionCoordinate = coordinateEnabled;
        trackNavigationPath = pathEnabled;
        if (!coordinateEnabled) clearExecutionCoordinate();
        if (!pathEnabled) clearNavigationPath();
    }

    public static void setProjectedArea(DroneArea area) {
        Minecraft minecraft = Minecraft.getMinecraft();
        projectedArea = area;
        projectedDimension = minecraft.world == null ? Integer.MIN_VALUE
                : minecraft.world.provider.getDimension();
    }

    public static void clearProjectedArea() {
        projectedArea = null;
        projectedDimension = Integer.MIN_VALUE;
    }

    public static void setExecutionCoordinate(BlockPos coordinate) {
        Minecraft minecraft = Minecraft.getMinecraft();
        executionCoordinate = coordinate;
        executionDimension = minecraft.world == null ? Integer.MIN_VALUE
                : minecraft.world.provider.getDimension();
    }

    public static void clearExecutionCoordinate() {
        executionCoordinate = null;
        executionDimension = Integer.MIN_VALUE;
    }

    public static void setNavigationPath(List<BlockPos> path) {
        Minecraft minecraft = Minecraft.getMinecraft();
        navigationPath = path == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(path));
        navigationDimension = minecraft.world == null ? Integer.MIN_VALUE
                : minecraft.world.provider.getDimension();
    }

    public static void clearNavigationPath() {
        navigationPath = Collections.emptyList();
        navigationDimension = Integer.MIN_VALUE;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        Entity camera = minecraft.getRenderViewEntity();
        if (minecraft.world == null || camera == null) return;
        int dimension = minecraft.world.provider.getDimension();
        refreshTrackedDrone(minecraft, dimension);
        boolean drawArea = projectedArea != null && projectedDimension == dimension;
        boolean drawCoordinate = executionCoordinate != null && executionDimension == dimension;
        boolean drawPath = !navigationPath.isEmpty() && navigationDimension == dimension;
        if (!drawArea && !drawCoordinate && !drawPath) return;

        double partialTicks = event.getPartialTicks();
        Vec3d cameraPosition = new Vec3d(
                camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks,
                camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks,
                camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        if (drawArea) drawArea(projectedArea);
        if (drawPath) drawPath(navigationPath);
        if (drawCoordinate) drawBox(executionCoordinate, 1.01D, 1.0F, 0.78F, 0.16F, 0.95F, 3.0F);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private static void refreshTrackedDrone(Minecraft minecraft, int dimension) {
        if (trackedEntityId < 0 || trackedDimension != dimension) return;
        Entity entity = minecraft.world.getEntityByID(trackedEntityId);
        if (!(entity instanceof EntityProgrammableDrone)) {
            if (trackExecutionCoordinate) clearExecutionCoordinate();
            if (trackNavigationPath) clearNavigationPath();
            return;
        }
        NBTTagCompound state = ((EntityProgrammableDrone) entity).getWorldPreviewState();
        if (trackExecutionCoordinate) {
            executionCoordinate = state.hasKey("Coordinate", 4)
                    ? BlockPos.fromLong(state.getLong("Coordinate")) : null;
            executionDimension = executionCoordinate == null ? Integer.MIN_VALUE : dimension;
        }
        if (trackNavigationPath) {
            List<BlockPos> path = new ArrayList<>();
            NBTTagList points = state.getTagList("Path", 10);
            for (int index = 0; index < points.tagCount() && path.size() < 256; index++) {
                NBTTagCompound point = points.getCompoundTagAt(index);
                if (point.hasKey("Position", 4)) path.add(BlockPos.fromLong(point.getLong("Position")));
            }
            navigationPath = Collections.unmodifiableList(path);
            navigationDimension = navigationPath.isEmpty() ? Integer.MIN_VALUE : dimension;
        }
    }

    private static void drawArea(DroneArea area) {
        if (!area.isWithinRuntimeLimits()) return;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.glLineWidth(1.5F);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int index = 0; index < area.getVolume(); index++) {
            appendBox(buffer, area.positionAt(index), 1.002D, 0.20F, 0.78F, 0.95F, 0.42F);
        }
        tessellator.draw();
    }

    private static void drawPath(List<BlockPos> path) {
        if (path.size() < 2) return;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.glLineWidth(3.0F);
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (BlockPos position : path) {
            buffer.pos(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D)
                    .color(0.35F, 1.0F, 0.48F, 0.9F).endVertex();
        }
        tessellator.draw();
    }

    private static void drawBox(BlockPos position, double size, float red, float green, float blue,
            float alpha, float lineWidth) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.glLineWidth(lineWidth);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        appendBox(buffer, position, size, red, green, blue, alpha);
        tessellator.draw();
    }

    private static void appendBox(BufferBuilder buffer, BlockPos position, double size,
            float red, float green, float blue, float alpha) {
        double inset = (1.0D - size) * 0.5D;
        double x0 = position.getX() + inset;
        double y0 = position.getY() + inset;
        double z0 = position.getZ() + inset;
        double x1 = x0 + size;
        double y1 = y0 + size;
        double z1 = z0 + size;
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

    private static void line(BufferBuilder buffer, double x0, double y0, double z0,
            double x1, double y1, double z1, float red, float green, float blue, float alpha) {
        buffer.pos(x0, y0, z0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
    }
}
