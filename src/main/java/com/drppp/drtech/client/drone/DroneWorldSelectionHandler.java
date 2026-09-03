package com.drppp.drtech.client.drone;

import com.drppp.drtech.network.SyncInit;
import com.drppp.drtech.network.UpdateTileEntityPacket;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.UUID;

/** Client-side two-click world picker started from the programmer coordinate inspector. */
public final class DroneWorldSelectionHandler {

    private static BlockPos programmerPos;
    private static UUID nodeId;
    private static boolean areaMode;
    private static BlockPos firstCorner;
    private static int dimension = Integer.MIN_VALUE;

    public static void begin(BlockPos programmer, UUID selectedNode, boolean area) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null || programmer == null || selectedNode == null) return;
        programmerPos = programmer.toImmutable();
        nodeId = selectedNode;
        areaMode = area;
        firstCorner = null;
        dimension = minecraft.world.provider.getDimension();
        DroneWorldPreviewRenderer.clearProjectedArea();
        DroneWorldPreviewRenderer.clearSelectionArea();
        DroneWorldPreviewRenderer.clearExecutionCoordinate();
        minecraft.player.sendMessage(new TextComponentString(I18n.format(area
                ? "drtech.drone.world_select.area_start" : "drtech.drone.world_select.coordinate_start")));
        minecraft.addScheduledTask(() -> minecraft.displayGuiScreen(null));
    }

    public static boolean isActive() { return nodeId != null; }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!isActive() || !event.getWorld().isRemote || event.getEntityPlayer() != Minecraft.getMinecraft().player) return;
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        // Forge may emit the same physical click for both hands. Only MAIN_HAND advances the
        // picker, otherwise one click could fill both area corners with the same block.
        if (event.getHand() != EnumHand.MAIN_HAND) return;
        if (event.getEntityPlayer().isSneaking()) {
            cancel("drtech.drone.world_select.cancelled");
            return;
        }
        BlockPos selected = event.getPos().toImmutable();
        if (!areaMode) {
            sendSelection(selected, null);
            finish("drtech.drone.world_select.coordinate_done", selected);
            return;
        }
        if (firstCorner == null) {
            firstCorner = selected;
            DroneWorldPreviewRenderer.setExecutionCoordinate(firstCorner);
            event.getEntityPlayer().sendMessage(new TextComponentString(I18n.format(
                    "drtech.drone.world_select.first_done", selected.getX(), selected.getY(), selected.getZ())));
            return;
        }
        BlockPos first = firstCorner;
        sendSelection(first, selected);
        finish("drtech.drone.world_select.area_done", selected);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isActive() || !areaMode || firstCorner == null) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.world.provider.getDimension() != dimension) {
            cancel("drtech.drone.world_select.cancelled");
            return;
        }
        RayTraceResult hit = minecraft.objectMouseOver;
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK && hit.getBlockPos() != null) {
            DroneWorldPreviewRenderer.setSelectionArea(DroneArea.between(firstCorner, hit.getBlockPos()));
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote && isActive()) clear();
    }

    private static void sendSelection(BlockPos first, BlockPos second) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("DroneWorldSelection", true);
        tag.setString("NodeId", nodeId.toString());
        tag.setLong("First", first.toLong());
        if (second != null) tag.setLong("Second", second.toLong());
        SyncInit.NETWORK.sendToServer(new UpdateTileEntityPacket(programmerPos, tag));
    }

    private static void finish(String key, BlockPos selected) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player != null) minecraft.player.sendMessage(new TextComponentString(I18n.format(
                key, selected.getX(), selected.getY(), selected.getZ())));
        clear();
    }

    private static void cancel(String key) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player != null) minecraft.player.sendMessage(new TextComponentString(I18n.format(key)));
        clear();
    }

    private static void clear() {
        programmerPos = null;
        nodeId = null;
        firstCorner = null;
        areaMode = false;
        dimension = Integer.MIN_VALUE;
        DroneWorldPreviewRenderer.clearProjectedArea();
        DroneWorldPreviewRenderer.clearSelectionArea();
        DroneWorldPreviewRenderer.clearExecutionCoordinate();
    }
}
