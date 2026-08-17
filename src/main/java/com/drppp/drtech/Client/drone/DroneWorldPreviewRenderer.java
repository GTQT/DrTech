package com.drppp.drtech.Client.drone;

import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.model.DroneWorldMarkerStyle;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneDock;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneEndpoint;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneFleetController;
import com.drppp.drtech.common.drone.network.DroneEndpoint;
import com.drppp.drtech.common.drone.network.DroneEndpointWorldLink;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Client-only world overlay for editor area projections and live loop coordinates. */
public final class DroneWorldPreviewRenderer {

    private static DroneArea projectedArea;
    private static int projectedDimension = Integer.MIN_VALUE;
    private static DroneArea selectionArea;
    private static int selectionDimension = Integer.MIN_VALUE;
    private static BlockPos executionCoordinate;
    private static int executionDimension = Integer.MIN_VALUE;
    private static List<BlockPos> navigationPath = Collections.emptyList();
    private static int navigationDimension = Integer.MIN_VALUE;
    private static int trackedEntityId = -1;
    private static int trackedDimension = Integer.MIN_VALUE;
    private static boolean trackExecutionCoordinate;
    private static boolean trackNavigationPath;
    private static List<DroneEndpointWorldLink> logisticsLinks = Collections.emptyList();

    public static void setLogisticsLinks(List<DroneEndpointWorldLink> links) {
        logisticsLinks = links == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(links.subList(0,
                Math.min(DroneWorldMarkerStyle.MAX_LINKS_PER_VIEW, links.size()))));
    }

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

    public static void setSelectionArea(DroneArea area) {
        Minecraft minecraft = Minecraft.getMinecraft();
        selectionArea = area;
        selectionDimension = minecraft.world == null ? Integer.MIN_VALUE
                : minecraft.world.provider.getDimension();
    }

    public static void clearSelectionArea() {
        selectionArea = null;
        selectionDimension = Integer.MIN_VALUE;
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
        boolean drawSelection = selectionArea != null && selectionDimension == dimension;
        boolean drawCoordinate = executionCoordinate != null && executionDimension == dimension;
        boolean drawPath = !navigationPath.isEmpty() && navigationDimension == dimension;
        boolean drawLinks = !logisticsLinks.isEmpty();

        double partialTicks = event.getPartialTicks();
        Vec3d cameraPosition = new Vec3d(
                camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks,
                camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks,
                camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        drawLoadedDeviceModels(minecraft, cameraPosition);
        GlStateManager.disableDepth();
        if (drawArea) drawArea(projectedArea);
        if (drawSelection) drawAreaBounds(selectionArea);
        if (drawPath) drawPath(navigationPath);
        if (drawCoordinate) drawBox(executionCoordinate, 1.01D, 1.0F, 0.78F, 0.16F, 0.95F, 3.0F);
        drawPlacementHolograms(minecraft);
        drawLoadedDeviceMarkers(minecraft);
        if (drawLinks) drawLogisticsLinks(logisticsLinks, dimension);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    /** Draws the machine-specific upper assemblies as depth-tested world geometry. */
    private static void drawLoadedDeviceModels(Minecraft minecraft, Vec3d cameraPosition) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int rendered = 0;
        for (TileEntity tile : minecraft.world.loadedTileEntityList) {
            if (!(tile instanceof IGregTechTileEntity) || rendered >= 96
                    || tile.getPos().distanceSq(cameraPosition.x, cameraPosition.y, cameraPosition.z) > 9216.0D) {
                continue;
            }
            MetaTileEntity meta = ((IGregTechTileEntity) tile).getMetaTileEntity();
            if (meta instanceof MetaTileEntityDroneEndpoint) {
                appendEndpointModel(buffer, tile.getPos(), ((MetaTileEntityDroneEndpoint) meta).getKind());
                rendered++;
            } else if (meta instanceof MetaTileEntityDroneFleetController) {
                appendFleetControllerModel(buffer, tile.getPos());
                rendered++;
            }
        }
        if (rendered > 0) {
            GlStateManager.disableCull();
            tessellator.draw();
            GlStateManager.enableCull();
        } else {
            tessellator.draw();
        }
    }

    private static void appendEndpointModel(BufferBuilder buffer, BlockPos pos, DroneEndpoint.Kind kind) {
        double x = pos.getX();
        double y = pos.getY() + 1.0D;
        double z = pos.getZ();
        float red = kind == DroneEndpoint.Kind.ITEM ? 0.96F : kind == DroneEndpoint.Kind.FLUID ? 0.16F : 1.0F;
        float green = kind == DroneEndpoint.Kind.ITEM ? 0.52F : kind == DroneEndpoint.Kind.FLUID ? 0.62F : 0.78F;
        float blue = kind == DroneEndpoint.Kind.ITEM ? 0.12F : kind == DroneEndpoint.Kind.FLUID ? 0.98F : 0.12F;
        appendSolidBox(buffer, x + 0.17D, y, z + 0.17D, x + 0.83D, y + 0.10D, z + 0.83D,
                0.18F, 0.20F, 0.24F, 1.0F);
        appendSolidBox(buffer, x + 0.23D, y + 0.10D, z + 0.23D, x + 0.77D, y + 0.64D, z + 0.77D,
                red, green, blue, kind == DroneEndpoint.Kind.FLUID ? 0.68F : 0.94F);
        appendSolidBox(buffer, x + 0.17D, y + 0.64D, z + 0.17D, x + 0.83D, y + 0.74D, z + 0.83D,
                0.23F, 0.25F, 0.30F, 1.0F);
        if (kind == DroneEndpoint.Kind.ITEM) {
            appendSolidBox(buffer, x + 0.34D, y + 0.34D, z + 0.76D, x + 0.66D, y + 0.42D, z + 0.84D,
                    1.0F, 0.82F, 0.22F, 1.0F);
        } else if (kind == DroneEndpoint.Kind.FLUID) {
            appendSolidBox(buffer, x + 0.44D, y + 0.74D, z + 0.44D, x + 0.56D, y + 0.92D, z + 0.56D,
                    0.22F, 0.72F, 1.0F, 0.92F);
        } else {
            appendSolidBox(buffer, x + 0.44D, y + 0.74D, z + 0.44D, x + 0.56D, y + 1.02D, z + 0.56D,
                    1.0F, 0.88F, 0.20F, 1.0F);
            appendSolidBox(buffer, x + 0.28D, y + 0.83D, z + 0.47D, x + 0.72D, y + 0.91D, z + 0.53D,
                    1.0F, 0.88F, 0.20F, 1.0F);
        }
    }

    private static void appendFleetControllerModel(BufferBuilder buffer, BlockPos pos) {
        double x = pos.getX();
        double y = pos.getY() + 1.0D;
        double z = pos.getZ();
        appendSolidBox(buffer, x + 0.12D, y, z + 0.16D, x + 0.88D, y + 0.12D, z + 0.84D,
                0.17F, 0.18F, 0.24F, 1.0F);
        appendSolidBox(buffer, x + 0.18D, y + 0.12D, z + 0.22D, x + 0.82D, y + 0.40D, z + 0.78D,
                0.42F, 0.16F, 0.72F, 0.96F);
        appendSolidBox(buffer, x + 0.22D, y + 0.40D, z + 0.62D, x + 0.78D, y + 0.82D, z + 0.76D,
                0.28F, 0.10F, 0.50F, 1.0F);
        appendSolidBox(buffer, x + 0.28D, y + 0.48D, z + 0.60D, x + 0.72D, y + 0.72D, z + 0.61D,
                0.66F, 0.38F, 1.0F, 0.95F);
        appendSolidBox(buffer, x + 0.46D, y + 0.82D, z + 0.67D, x + 0.54D, y + 1.20D, z + 0.75D,
                0.72F, 0.38F, 1.0F, 1.0F);
        appendSolidBox(buffer, x + 0.35D, y + 1.14D, z + 0.67D, x + 0.65D, y + 1.22D, z + 0.75D,
                0.84F, 0.58F, 1.0F, 1.0F);
    }

    private static void appendSolidBox(BufferBuilder buffer, double x0, double y0, double z0,
            double x1, double y1, double z1, float red, float green, float blue, float alpha) {
        quad(buffer, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, red, green, blue, alpha);
        quad(buffer, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, red, green, blue, alpha);
        quad(buffer, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, red, green, blue, alpha);
        quad(buffer, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, red, green, blue, alpha);
        quad(buffer, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, red, green, blue, alpha);
        quad(buffer, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, red, green, blue, alpha);
    }

    private static void quad(BufferBuilder buffer,
            double x0, double y0, double z0, double x1, double y1, double z1,
            double x2, double y2, double z2, double x3, double y3, double z3,
            float red, float green, float blue, float alpha) {
        buffer.pos(x0, y0, z0).color(red, green, blue, alpha).endVertex();
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.pos(x3, y3, z3).color(red, green, blue, alpha).endVertex();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) return;
        clearProjectedArea();
        clearSelectionArea();
        clearExecutionCoordinate();
        clearNavigationPath();
        trackedEntityId = -1;
        trackedDimension = Integer.MIN_VALUE;
        trackExecutionCoordinate = false;
        trackNavigationPath = false;
        logisticsLinks = Collections.emptyList();
    }

    private static void drawPlacementHolograms(Minecraft minecraft) {
        int rendered = 0;
        for (Entity entity : minecraft.world.loadedEntityList) {
            if (!(entity instanceof EntityProgrammableDrone) || rendered >= 64) continue;
            NBTTagCompound state = ((EntityProgrammableDrone) entity).getWorldPreviewState();
            if (!state.hasKey("PlacementTarget", 4)) continue;
            drawBox(BlockPos.fromLong(state.getLong("PlacementTarget")), 1.025D,
                    0.22F, 0.92F, 1.0F, 0.72F, 2.0F);
            rendered++;
        }
    }

    private static void drawLoadedDeviceMarkers(Minecraft minecraft) {
        int rendered = 0;
        for (TileEntity tile : minecraft.world.loadedTileEntityList) {
            if (!(tile instanceof IGregTechTileEntity) || rendered >= 64) continue;
            MetaTileEntity meta = ((IGregTechTileEntity) tile).getMetaTileEntity();
            if (meta instanceof MetaTileEntityDroneDock) {
                drawBeacon(tile.getPos(), 0.12F, 0.82F, 1.0F, 0.62F,
                        DroneWorldMarkerStyle.MARKER_HEIGHT);
                rendered++;
            } else if (meta instanceof MetaTileEntityDroneFleetController) {
                drawBeacon(tile.getPos(), 0.72F, 0.32F, 1.0F, 0.72F,
                        DroneWorldMarkerStyle.MARKER_HEIGHT + 1);
                rendered++;
            }
        }
    }

    private static void drawBeacon(BlockPos position, float red, float green, float blue, float alpha, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.glLineWidth(DroneWorldMarkerStyle.MARKER_LINE_WIDTH);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        double x = position.getX() + 0.5D;
        double y = position.getY() + 1.02D;
        double z = position.getZ() + 0.5D;
        line(buffer, x, y, z, x, y + height, z, red, green, blue, alpha);
        tessellator.draw();
        drawBox(position.up(height), 0.34D, red, green, blue, alpha, DroneWorldMarkerStyle.MARKER_LINE_WIDTH);
    }

    private static void drawLogisticsLinks(List<DroneEndpointWorldLink> links, int dimension) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.glLineWidth(DroneWorldMarkerStyle.ROUTE_LINE_WIDTH);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (DroneEndpointWorldLink link : links) {
            if (link.getDimension() != dimension) continue;
            BlockPos source = link.getSourcePosition();
            BlockPos target = link.getTargetPosition();
            line(buffer, source.getX() + 0.5D, source.getY() + 1.1D, source.getZ() + 0.5D,
                    target.getX() + 0.5D, target.getY() + 1.1D, target.getZ() + 0.5D,
                    1.0F, 0.78F, 0.18F, 0.82F);
        }
        tessellator.draw();
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
        GlStateManager.glLineWidth(DroneWorldMarkerStyle.MARKER_LINE_WIDTH);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int index = 0; index < area.getVolume(); index++) {
            appendBox(buffer, area.positionAt(index), 1.002D, 0.20F, 0.78F, 0.95F, 0.42F);
        }
        tessellator.draw();
    }

    /** Lightweight two-corner selection outline; never iterates the selected volume. */
    private static void drawAreaBounds(DroneArea area) {
        BlockPos min = area.getMin();
        BlockPos max = area.getMax();
        double x0 = min.getX();
        double y0 = min.getY();
        double z0 = min.getZ();
        double x1 = max.getX() + 1.0D;
        double y1 = max.getY() + 1.0D;
        double z1 = max.getZ() + 1.0D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.glLineWidth(3.0F);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        float r = 0.22F, g = 0.92F, b = 1.0F, a = 0.92F;
        line(buffer,x0,y0,z0,x1,y0,z0,r,g,b,a); line(buffer,x1,y0,z0,x1,y0,z1,r,g,b,a);
        line(buffer,x1,y0,z1,x0,y0,z1,r,g,b,a); line(buffer,x0,y0,z1,x0,y0,z0,r,g,b,a);
        line(buffer,x0,y1,z0,x1,y1,z0,r,g,b,a); line(buffer,x1,y1,z0,x1,y1,z1,r,g,b,a);
        line(buffer,x1,y1,z1,x0,y1,z1,r,g,b,a); line(buffer,x0,y1,z1,x0,y1,z0,r,g,b,a);
        line(buffer,x0,y0,z0,x0,y1,z0,r,g,b,a); line(buffer,x1,y0,z0,x1,y1,z0,r,g,b,a);
        line(buffer,x1,y0,z1,x1,y1,z1,r,g,b,a); line(buffer,x0,y0,z1,x0,y1,z1,r,g,b,a);
        tessellator.draw();
    }

    private static void drawPath(List<BlockPos> path) {
        if (path.size() < 2) return;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.glLineWidth(DroneWorldMarkerStyle.MARKER_LINE_WIDTH + 1.0F);
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
