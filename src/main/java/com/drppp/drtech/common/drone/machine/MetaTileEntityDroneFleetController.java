package com.drppp.drtech.common.drone.machine;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.common.drone.network.DroneFleetState;
import com.drppp.drtech.common.drone.network.DroneJob;
import com.drppp.drtech.common.drone.network.DroneRegistry;
import com.drppp.drtech.common.drone.network.DroneRegistryRecord;
import com.drppp.drtech.common.drone.network.DroneEndpointNetwork;
import com.drppp.drtech.common.drone.network.DroneEndpointRoute;
import com.drppp.drtech.common.drone.network.DroneEndpointRoutePlanner;
import com.drppp.drtech.common.drone.network.DroneEndpointWorldLink;
import com.drppp.drtech.Client.drone.DroneWorldPreviewRenderer;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Dedicated EV fleet console; all synchronized records are filtered by the viewing player's UUID. */
public final class MetaTileEntityDroneFleetController extends TieredMetaTileEntity {
    private static final String CONTROL_ACTION = "fleet_controller_control";
    private static final int MAX_CONTROL_DISTANCE_SQUARED = 64;
    private List<ClientDrone> clientDrones = Collections.emptyList();
    private int clientDroneIndex;
    private String clientJobs = "";

    public MetaTileEntityDroneFleetController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.EV);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDroneFleetController(metaTileEntityId);
    }

    @Override
    public boolean usesMui2() { return true; }

    @Override
    public void update() {
        super.update();
        if (getWorld() != null && !getWorld().isRemote && getOffsetTimer() % 20 == 0) {
            DroneFleetState.get(getWorld()).tick(getWorld().getTotalWorldTime());
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        GenericSyncValue<NBTTagCompound> state = GenericSyncValue.builder(NBTTagCompound.class)
                .getter(() -> createState(guiData.getPlayer()))
                .setter(this::receiveState)
                .adapter(ByteBufAdapters.NBT)
                .copy(NBTTagCompound::copy)
                .build();
        syncManager.syncValue("fleet_controller_state", state);
        syncManager.registerSyncedAction(CONTROL_ACTION, false, true,
                packet -> receiveControl(guiData.getPlayer(), packet.readString(36), packet.readString(16)));
        return GTGuis.createPanel(this, 280, 190)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(7, 6))
                .child(IKey.lang("drtech.drone.fleet_controller.drones").asWidget().pos(7, 24))
                .child(IKey.dynamic(this::clientDronePage).asWidget().pos(210, 24).size(60, 10))
                .child(IKey.dynamic(this::clientDroneDetails).asWidget().pos(7, 40).size(266, 54))
                .child(button("<", 7, 99, () -> moveDrone(-1)))
                .child(button(">", 43, 99, () -> moveDrone(1)))
                .child(controlButton("drtech.drone.controller.start", "START", 83, syncManager))
                .child(controlButton("drtech.drone.controller.stop", "STOP", 145, syncManager))
                .child(controlButton("drtech.drone.programmer.fleet.recall", "RECALL", 207, syncManager))
                .child(IKey.lang("drtech.drone.fleet_controller.jobs").asWidget().pos(7, 124))
                .child(IKey.dynamic(() -> clientJobs).asWidget().pos(7, 139).size(266, 42));
    }

    private ButtonWidget<?> button(String label, int x, int y, Runnable action) {
        return new ButtonWidget<>().pos(x, y).size(32, 16).overlay(IKey.str(label))
                .onMousePressed(mouse -> { action.run(); return true; });
    }

    private ButtonWidget<?> controlButton(String key, String command, int x, PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(x, 99).size(58, 16).overlay(IKey.lang(key))
                .setEnabledIf(widget -> currentDrone() != null && currentDrone().online)
                .onMousePressed(mouse -> {
                    ClientDrone drone = currentDrone();
                    if (drone != null && drone.online) syncManager.callSyncedAction(CONTROL_ACTION, packet -> {
                        packet.writeString(drone.id.toString());
                        packet.writeString(command);
                    });
                    return true;
                });
    }

    private NBTTagCompound createState(EntityPlayer player) {
        NBTTagCompound state = new NBTTagCompound();
        if (getWorld() == null || getWorld().isRemote || player == null) return state;
        NBTTagList drones = new NBTTagList();
        for (DroneRegistryRecord record : DroneRegistry.get(getWorld()).listForOwner(player.getUniqueID())) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", record.getDroneId().toString());
            tag.setBoolean("Online", DroneRegistry.isOnline(record, getWorld().getTotalWorldTime()));
            tag.setString("Summary", record.getChassis() + " | " + record.getStatus() + " | DIM "
                    + record.getDimension() + " | " + record.getPosition().getX() + ", "
                    + record.getPosition().getY() + ", " + record.getPosition().getZ() + "\nEU "
                    + record.getEnergyStored() + "/" + record.getEnergyCapacity() + " | Cargo "
                    + record.getCargoOccupiedSlots() + "/" + record.getCargoCapacitySlots());
            drones.appendTag(tag);
        }
        state.setTag("Drones", drones);
        StringBuilder jobs = new StringBuilder();
        int count = 0;
        java.util.Collection<DroneJob> ownedJobs = DroneFleetState.get(getWorld())
                .getJobsForOwner(player.getUniqueID());
        for (DroneJob job : ownedJobs) {
            if (count++ >= 3) break;
            if (jobs.length() > 0) jobs.append('\n');
            jobs.append(job.getJobId().toString(), 0, 8).append(" | ")
                    .append(job.getState().name()).append(" | P").append(job.getPriority());
            if (job.isLogisticsJob()) {
                jobs.append(" | ").append(job.getLogisticsStage().name())
                        .append(" | ").append(job.getResourceId()).append(' ')
                        .append(job.getDeliveredAmount()).append('/').append(job.getRequestedAmount());
                if (job.getAssignedDroneId() != null) {
                    jobs.append(" | D:").append(job.getAssignedDroneId().toString(), 0, 8);
                }
            }
        }
        NBTTagList links = new NBTTagList();
        for (DroneJob job : ownedJobs) {
            if (links.tagCount() >= 128) break;
            if (!job.isLogisticsJob() || isTerminal(job.getState())) continue;
            DroneEndpointRoute route = DroneEndpointRoutePlanner.plan(DroneEndpointNetwork.get(getWorld()),
                    player.getUniqueID(), job.getSourceEndpointId(), job.getTargetEndpointId(),
                    getWorld().getTotalWorldTime()).orElse(null);
            if (route != null) {
                NBTTagCompound link = new NBTTagCompound();
                link.setInteger("Dimension", route.getSource().getDimension());
                link.setLong("Source", route.getSource().getPosition().toLong());
                link.setLong("Target", route.getTarget().getPosition().toLong());
                link.setLong("Distance", route.getDistance());
                links.appendTag(link);
            }
        }
        state.setString("Jobs", jobs.length() == 0 ? "-" : jobs.toString());
        state.setTag("Links", links);
        return state;
    }

    private void receiveState(NBTTagCompound state) {
        List<ClientDrone> drones = new ArrayList<>();
        NBTTagList list = state.getTagList("Drones", 10);
        for (int index = 0; index < list.tagCount(); index++) {
            NBTTagCompound tag = list.getCompoundTagAt(index);
            try { drones.add(new ClientDrone(UUID.fromString(tag.getString("Id")), tag.getBoolean("Online"),
                    tag.getString("Summary"))); }
            catch (IllegalArgumentException ignored) { }
        }
        clientDrones = Collections.unmodifiableList(drones);
        if (!clientDrones.isEmpty()) clientDroneIndex = Math.floorMod(clientDroneIndex, clientDrones.size());
        else clientDroneIndex = 0;
        clientJobs = state.getString("Jobs");
        List<DroneEndpointWorldLink> links = new ArrayList<>();
        NBTTagList linkTags = state.getTagList("Links", 10);
        for (int index = 0; index < linkTags.tagCount() && links.size() < 128; index++) {
            NBTTagCompound tag = linkTags.getCompoundTagAt(index);
            if (!tag.hasKey("Source", 4) || !tag.hasKey("Target", 4)) continue;
            int dimension = tag.getInteger("Dimension");
            links.add(new DroneEndpointWorldLink(dimension, BlockPos.fromLong(tag.getLong("Source")),
                    BlockPos.fromLong(tag.getLong("Target")), tag.getLong("Distance")));
        }
        DroneWorldPreviewRenderer.setLogisticsLinks(links);
    }

    private static boolean isTerminal(DroneJob.State state) {
        return state == DroneJob.State.COMPLETED || state == DroneJob.State.FAILED
                || state == DroneJob.State.CANCELLED;
    }

    private ClientDrone currentDrone() {
        return clientDrones.isEmpty() ? null : clientDrones.get(Math.floorMod(clientDroneIndex, clientDrones.size()));
    }

    private void moveDrone(int delta) {
        if (!clientDrones.isEmpty()) clientDroneIndex = Math.floorMod(clientDroneIndex + delta, clientDrones.size());
    }

    private String clientDronePage() {
        return clientDrones.isEmpty() ? "0/0" : (clientDroneIndex + 1) + "/" + clientDrones.size();
    }

    private String clientDroneDetails() {
        ClientDrone drone = currentDrone();
        return drone == null ? "-" : drone.summary + "\n" + drone.id.toString().substring(0, 8);
    }

    private void receiveControl(EntityPlayer player, String rawDroneId, String command) {
        if (getWorld() == null || getWorld().isRemote || player == null
                || player.getDistanceSq(getPos()) > MAX_CONTROL_DISTANCE_SQUARED
                || (!"START".equals(command) && !"STOP".equals(command) && !"RECALL".equals(command))) return;
        UUID droneId;
        try { droneId = UUID.fromString(rawDroneId); }
        catch (IllegalArgumentException ignored) { return; }
        DroneRegistryRecord record = DroneRegistry.get(getWorld()).getRecord(droneId).orElse(null);
        if (record == null || !player.getUniqueID().equals(record.getOwnerId())) return;
        MinecraftServer server = getWorld().getMinecraftServer();
        WorldServer targetWorld = server == null ? null : server.getWorld(record.getDimension());
        if (targetWorld == null || !DroneRegistry.isOnline(record, targetWorld.getTotalWorldTime())) return;
        for (Entity entity : targetWorld.loadedEntityList) {
            if (entity instanceof EntityProgrammableDrone
                    && droneId.equals(((EntityProgrammableDrone) entity).getDroneId())) {
                ((EntityProgrammableDrone) entity).handleFleetControl(player.getUniqueID(), command);
                return;
            }
        }
    }

    private static final class ClientDrone {
        private final UUID id;
        private final boolean online;
        private final String summary;

        private ClientDrone(UUID id, boolean online, String summary) {
            this.id = id;
            this.online = online;
            this.summary = summary;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation,
            IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[GTValues.EV].render(renderState, translation, colouredPipeline);
        com.drppp.drtech.Client.Textures.DRONE_CONTROLLER_CASING.render(
                renderState, translation, pipeline);
        Textures.INFINITE_EMITTER_FACE.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.INFINITE_EMITTER_FACE.renderSided(net.minecraft.util.EnumFacing.UP,
                renderState, translation, pipeline);
        com.drppp.drtech.Client.Textures.DRONE_CONTROLLER_OVERLAY.renderSided(
                getFrontFacing(), renderState, translation, pipeline);
    }
}
