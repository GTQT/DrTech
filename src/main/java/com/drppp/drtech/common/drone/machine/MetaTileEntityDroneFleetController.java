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
import com.drppp.drtech.common.drone.network.DroneReservation;
import com.drppp.drtech.common.drone.network.DroneRegistry;
import com.drppp.drtech.common.drone.network.DroneRegistryRecord;
import com.drppp.drtech.common.drone.network.DroneEndpointNetwork;
import com.drppp.drtech.common.drone.network.DroneEndpointRoute;
import com.drppp.drtech.common.drone.network.DroneEndpointRoutePlanner;
import com.drppp.drtech.common.drone.network.DroneEndpointWorldLink;
import com.drppp.drtech.client.drone.DroneWorldPreviewRenderer;
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
    private static final String JOB_ACTION = "fleet_controller_job";
    private static final int MAX_CONTROL_DISTANCE_SQUARED = 64;
    private List<ClientDrone> clientDrones = Collections.emptyList();
    private int clientDroneIndex;
    private List<ClientJob> clientJobs = Collections.emptyList();
    private int clientJobIndex;

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
            DroneFleetState.get(getWorld()).tick(getWorld());
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
        syncManager.registerSyncedAction(JOB_ACTION, false, true,
                packet -> receiveJobAction(guiData.getPlayer(), packet.readString(36), packet.readString(16)));
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
                .child(cancelJobButton(syncManager))
                .child(IKey.dynamic(this::clientJobPage).asWidget().pos(190, 124).size(34, 10))
                .child(jobButton("<", 226, () -> moveJob(-1)))
                .child(jobButton(">", 250, () -> moveJob(1)))
                .child(IKey.dynamic(this::clientJobDetails).asWidget().pos(7, 139).size(266, 42));
    }

    private ButtonWidget<?> button(String label, int x, int y, Runnable action) {
        return new ButtonWidget<>().pos(x, y).size(32, 16).overlay(IKey.str(label))
                .onMousePressed(mouse -> { action.run(); return true; });
    }

    private ButtonWidget<?> jobButton(String label, int x, Runnable action) {
        return new ButtonWidget<>().pos(x, 121).size(22, 13).overlay(IKey.str(label))
                .setEnabledIf(widget -> clientJobs.size() > 1)
                .onMousePressed(mouse -> { action.run(); return true; });
    }

    private ButtonWidget<?> cancelJobButton(PanelSyncManager syncManager) {
        return new ButtonWidget<>().pos(130, 121).size(56, 13)
                .overlay(IKey.lang("drtech.drone.fleet_controller.cancel_job"))
                .setEnabledIf(widget -> currentJob() != null && currentJob().cancellable)
                .onMousePressed(mouse -> {
                    ClientJob job = currentJob();
                    if (job != null && job.cancellable) syncManager.callSyncedAction(JOB_ACTION, packet -> {
                        packet.writeString(job.id.toString());
                        packet.writeString("CANCEL");
                    });
                    return true;
                });
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
            tag.setString("Chassis", record.getChassis());
            tag.setString("Status", record.getStatus());
            tag.setInteger("Dimension", record.getDimension());
            tag.setLong("Position", record.getPosition().toLong());
            tag.setLong("Energy", record.getEnergyStored());
            tag.setLong("EnergyCapacity", record.getEnergyCapacity());
            tag.setInteger("Cargo", record.getCargoOccupiedSlots());
            tag.setInteger("CargoCapacity", record.getCargoCapacitySlots());
            drones.appendTag(tag);
        }
        state.setTag("Drones", drones);
        NBTTagList jobs = new NBTTagList();
        List<DroneJob> ownedJobs = new ArrayList<>(DroneFleetState.get(getWorld())
                .getJobsForOwner(player.getUniqueID()));
        ownedJobs.sort(java.util.Comparator.comparingInt(MetaTileEntityDroneFleetController::displayOrder)
                .thenComparing(java.util.Comparator.comparingLong(DroneJob::getSubmittedTick).reversed())
                .thenComparing(job -> job.getJobId().toString()));
        for (DroneJob job : ownedJobs) {
            if (jobs.tagCount() >= 128) break;
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", job.getJobId().toString());
            tag.setBoolean("Cancellable", !isTerminal(job.getState()));
            tag.setBoolean("Logistics", job.isLogisticsJob());
            tag.setString("State", job.getState().name());
            tag.setInteger("Priority", job.getPriority());
            tag.setInteger("Attempts", job.getAttempts());
            tag.setInteger("MaxAttempts", job.getMaxAttempts());
            tag.setString("Failure", job.getLastFailure());
            tag.setLong("RetryTicks", job.getState() == DroneJob.State.RETRY_WAIT
                    ? Math.max(0L, job.getNextEligibleTick() - getWorld().getTotalWorldTime()) : 0L);
            if (job.isLogisticsJob()) {
                DroneReservation reservation = DroneFleetState.get(getWorld())
                        .getReservationForJob(player.getUniqueID(), job.getJobId()).orElse(null);
                tag.setString("Kind", job.getResourceKind().name());
                tag.setString("Stage", job.getLogisticsStage().name());
                tag.setString("Resource", job.getResourceId());
                tag.setLong("Delivered", job.getDeliveredAmount());
                tag.setLong("Picked", job.getPickedAmount());
                tag.setLong("Requested", job.getRequestedAmount());
                tag.setLong("Reserved", reservation == null ? 0L : reservation.getAmount());
                tag.setString("Source", job.getSourceEndpointId().toString());
                tag.setString("Target", job.getTargetEndpointId().toString());
                if (job.getAssignedDroneId() != null) tag.setString("Drone", job.getAssignedDroneId().toString());
            }
            jobs.appendTag(tag);
        }
        NBTTagList links = new NBTTagList();
        for (DroneJob job : ownedJobs) {
            if (links.tagCount() >= 128) break;
            if (!job.isLogisticsJob() || isTerminal(job.getState())
                    && job.getLogisticsStage() != DroneJob.LogisticsStage.RETURNING) continue;
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
        state.setTag("Jobs", jobs);
        state.setTag("Links", links);
        return state;
    }

    private void receiveState(NBTTagCompound state) {
        List<ClientDrone> drones = new ArrayList<>();
        NBTTagList list = state.getTagList("Drones", 10);
        for (int index = 0; index < list.tagCount(); index++) {
            NBTTagCompound tag = list.getCompoundTagAt(index);
            try { drones.add(new ClientDrone(UUID.fromString(tag.getString("Id")), tag.getBoolean("Online"),
                    droneSummary(tag))); }
            catch (IllegalArgumentException ignored) { }
        }
        clientDrones = Collections.unmodifiableList(drones);
        if (!clientDrones.isEmpty()) clientDroneIndex = Math.floorMod(clientDroneIndex, clientDrones.size());
        else clientDroneIndex = 0;
        List<ClientJob> jobs = new ArrayList<>();
        NBTTagList jobTags = state.getTagList("Jobs", 10);
        for (int index = 0; index < jobTags.tagCount() && jobs.size() < 128; index++) {
            NBTTagCompound tag = jobTags.getCompoundTagAt(index);
            try {
                jobs.add(new ClientJob(UUID.fromString(tag.getString("Id")), jobSummary(tag),
                        tag.getBoolean("Cancellable")));
            } catch (IllegalArgumentException ignored) { }
        }
        clientJobs = Collections.unmodifiableList(jobs);
        if (!clientJobs.isEmpty()) clientJobIndex = Math.floorMod(clientJobIndex, clientJobs.size());
        else clientJobIndex = 0;
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

    /** Keeps actionable and payload-recovery work visible when the bounded history is full. */
    private static int displayOrder(DroneJob job) {
        if (!isTerminal(job.getState())) return 0;
        if (job.isLogisticsJob() && job.getLogisticsStage() == DroneJob.LogisticsStage.RETURNING) return 1;
        return 2;
    }

    private ClientDrone currentDrone() {
        return clientDrones.isEmpty() ? null : clientDrones.get(Math.floorMod(clientDroneIndex, clientDrones.size()));
    }

    private void moveDrone(int delta) {
        if (!clientDrones.isEmpty()) clientDroneIndex = Math.floorMod(clientDroneIndex + delta, clientDrones.size());
    }

    private void moveJob(int delta) {
        if (!clientJobs.isEmpty()) clientJobIndex = Math.floorMod(clientJobIndex + delta, clientJobs.size());
    }

    private ClientJob currentJob() {
        return clientJobs.isEmpty() ? null : clientJobs.get(Math.floorMod(clientJobIndex, clientJobs.size()));
    }

    private String clientDronePage() {
        return clientDrones.isEmpty() ? "0/0" : (clientDroneIndex + 1) + "/" + clientDrones.size();
    }

    private String clientDroneDetails() {
        ClientDrone drone = currentDrone();
        return drone == null ? "-" : drone.summary + "\n" + drone.id.toString().substring(0, 8);
    }

    private String clientJobPage() {
        return clientJobs.isEmpty() ? "0/0" : (clientJobIndex + 1) + "/" + clientJobs.size();
    }

    private String clientJobDetails() {
        ClientJob job = currentJob();
        return job == null ? "-" : job.summary;
    }

    private static String droneSummary(NBTTagCompound tag) {
        BlockPos position = BlockPos.fromLong(tag.getLong("Position"));
        return net.minecraft.client.resources.I18n.format("drtech.drone.fleet_controller.drone_line1",
                tag.getString("Chassis"), localizeToken("status", tag.getString("Status")),
                tag.getBoolean("Online") ? localizeToken("online", "online") : localizeToken("online", "offline"),
                tag.getInteger("Dimension"), position.getX(), position.getY(), position.getZ())
                + "\n" + net.minecraft.client.resources.I18n.format(
                "drtech.drone.fleet_controller.drone_line2", tag.getLong("Energy"),
                tag.getLong("EnergyCapacity"), tag.getInteger("Cargo"), tag.getInteger("CargoCapacity"));
    }

    private static String jobSummary(NBTTagCompound tag) {
        String id = shortId(readUuid(tag, "Id"));
        String state = localizeToken("state", tag.getString("State"));
        String failure = localizeFailure(tag.getString("Failure"));
        if (!tag.getBoolean("Logistics")) {
            return net.minecraft.client.resources.I18n.format("drtech.drone.fleet_controller.job_generic",
                    id, state, tag.getInteger("Priority"), tag.getInteger("Attempts"),
                    tag.getInteger("MaxAttempts"), bounded(failure, 72));
        }
        String assigned = shortId(readUuid(tag, "Drone"));
        String route = shortId(readUuid(tag, "Source")) + ">" + shortId(readUuid(tag, "Target"));
        String suffix = failure.isEmpty() ? "" : net.minecraft.client.resources.I18n.format(
                "drtech.drone.fleet_controller.job_failure", bounded(failure, 38));
        long retryTicks = Math.max(0L, tag.getLong("RetryTicks"));
        if (retryTicks > 0L) suffix += net.minecraft.client.resources.I18n.format(
                "drtech.drone.fleet_controller.job_wait", (retryTicks + 19L) / 20L);
        return net.minecraft.client.resources.I18n.format("drtech.drone.fleet_controller.job_line1",
                id, localizeToken("kind", tag.getString("Kind")), state,
                localizeToken("stage", tag.getString("Stage")))
                + "\n" + net.minecraft.client.resources.I18n.format(
                "drtech.drone.fleet_controller.job_line2", bounded(tag.getString("Resource"), 40),
                tag.getLong("Delivered"), tag.getLong("Picked"), tag.getLong("Requested"),
                tag.getLong("Reserved"))
                + "\n" + net.minecraft.client.resources.I18n.format(
                "drtech.drone.fleet_controller.job_line3", route, assigned, tag.getInteger("Attempts"),
                tag.getInteger("MaxAttempts"), suffix);
    }

    private static String shortId(UUID id) {
        return id == null ? "-" : id.toString().substring(0, 8);
    }

    private static String bounded(String value, int maximum) {
        String safe = value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
        return safe.length() <= maximum ? safe : safe.substring(0, Math.max(1, maximum - 1)) + "…";
    }

    private static String localizeToken(String group, String value) {
        String normalized = value == null ? "" : value.toLowerCase(java.util.Locale.ROOT);
        String key = "drtech.drone.logistics." + group + "." + normalized;
        String localized = net.minecraft.client.resources.I18n.format(key);
        return key.equals(localized) ? value : localized;
    }

    private static String localizeFailure(String value) {
        if (value == null || value.isEmpty()) return "";
        String key = "drtech.drone.logistics.failure." + value.toLowerCase(java.util.Locale.ROOT);
        String localized = net.minecraft.client.resources.I18n.format(key);
        if (!key.equals(localized)) return localized;
        String normalized = value.toLowerCase(java.util.Locale.ROOT);
        String side = normalized.startsWith("source_") ? "source"
                : normalized.startsWith("target_") ? "target" : "";
        if (!side.isEmpty()) {
            String detail = normalized.substring(side.length() + 1);
            String detailKey = "drtech.drone.logistics.failure_detail." + detail;
            String detailText = net.minecraft.client.resources.I18n.format(detailKey);
            if (!detailKey.equals(detailText)) {
                return net.minecraft.client.resources.I18n.format(
                        "drtech.drone.logistics.failure_side." + side, detailText);
            }
        }
        return value;
    }

    private static UUID readUuid(NBTTagCompound tag, String key) {
        try { return tag.hasKey(key, 8) ? UUID.fromString(tag.getString(key)) : null; }
        catch (IllegalArgumentException ignored) { return null; }
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

    private void receiveJobAction(EntityPlayer player, String rawJobId, String command) {
        if (getWorld() == null || getWorld().isRemote || player == null || !"CANCEL".equals(command)
                || player.getDistanceSq(getPos()) > MAX_CONTROL_DISTANCE_SQUARED) return;
        try {
            DroneFleetState.get(getWorld()).cancelJob(player.getUniqueID(), UUID.fromString(rawJobId));
        } catch (IllegalArgumentException ignored) { }
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

    private static final class ClientJob {
        private final UUID id;
        private final String summary;
        private final boolean cancellable;

        private ClientJob(UUID id, String summary, boolean cancellable) {
            this.id = id;
            this.summary = summary;
            this.cancellable = cancellable;
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
        com.drppp.drtech.client.Textures.DRONE_CONTROLLER_CASING.render(
                renderState, translation, pipeline);
        Textures.INFINITE_EMITTER_FACE.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.INFINITE_EMITTER_FACE.renderSided(net.minecraft.util.EnumFacing.UP,
                renderState, translation, pipeline);
        com.drppp.drtech.client.Textures.DRONE_CONTROLLER_OVERLAY.renderSided(
                getFrontFacing(), renderState, translation, pipeline);
    }
}
