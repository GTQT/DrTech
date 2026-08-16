package com.drppp.drtech.common.drone.machine;

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
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

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
        for (DroneJob job : DroneFleetState.get(getWorld()).getJobsForOwner(player.getUniqueID())) {
            if (count++ >= 3) break;
            if (jobs.length() > 0) jobs.append('\n');
            jobs.append(job.getJobId().toString(), 0, 8).append(" | ")
                    .append(job.getState().name()).append(" | P").append(job.getPriority());
        }
        state.setString("Jobs", jobs.length() == 0 ? "-" : jobs.toString());
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
}
