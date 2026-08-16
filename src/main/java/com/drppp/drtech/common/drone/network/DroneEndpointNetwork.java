package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Owner-isolated directory for native item, fluid, and EU endpoints. */
public final class DroneEndpointNetwork extends WorldSavedData {
    public static final String DATA_NAME = "drtech_drone_endpoints";
    public static final long ONLINE_TIMEOUT_TICKS = 100L;
    private static final int MAX_RECORDS = 2048;
    private final Map<UUID, DroneEndpoint> endpoints = new LinkedHashMap<>();

    public DroneEndpointNetwork() { this(DATA_NAME); }
    public DroneEndpointNetwork(String name) { super(name); }

    public static DroneEndpointNetwork get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        DroneEndpointNetwork network = (DroneEndpointNetwork) storage.getOrLoadData(
                DroneEndpointNetwork.class, DATA_NAME);
        if (network == null) {
            network = new DroneEndpointNetwork();
            storage.setData(DATA_NAME, network);
        }
        return network;
    }

    public void heartbeat(DroneEndpoint endpoint) {
        if (endpoint == null || (!endpoints.containsKey(endpoint.getEndpointId()) && endpoints.size() >= MAX_RECORDS)) return;
        endpoints.put(endpoint.getEndpointId(), endpoint);
        markDirty();
    }

    public List<DroneEndpoint> listForOwner(@Nullable UUID ownerId, @Nullable DroneEndpoint.Kind kind) {
        List<DroneEndpoint> result = new ArrayList<>();
        for (DroneEndpoint endpoint : endpoints.values()) {
            if (Objects.equals(ownerId, endpoint.getOwnerId()) && (kind == null || kind == endpoint.getKind())) {
                result.add(endpoint);
            }
        }
        result.sort(Comparator.comparingInt(DroneEndpoint::getDimension)
                .thenComparing(endpoint -> endpoint.getPosition().toLong())
                .thenComparing(endpoint -> endpoint.getEndpointId().toString()));
        return Collections.unmodifiableList(result);
    }

    public static boolean isOnline(DroneEndpoint endpoint, long worldTime) {
        return endpoint != null && endpoint.isLoaded() && worldTime >= endpoint.getLastHeartbeat()
                && worldTime - endpoint.getLastHeartbeat() <= ONLINE_TIMEOUT_TICKS;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        endpoints.clear();
        NBTTagList list = compound.getTagList("Endpoints", 10);
        for (int index = 0; index < Math.min(MAX_RECORDS, list.tagCount()); index++) {
            DroneEndpoint endpoint = DroneEndpoint.readFromNbt(list.getCompoundTagAt(index));
            if (endpoint != null) endpoints.put(endpoint.getEndpointId(), new DroneEndpoint(endpoint.getEndpointId(),
                    endpoint.getKind(), endpoint.getDimension(), endpoint.getPosition(), endpoint.getOwnerId(),
                    endpoint.getLastHeartbeat(), false, endpoint.getRequestAmount(), endpoint.getProvideAmount(),
                    endpoint.getPriority(), endpoint.getWhitelist(), endpoint.getMinimumReserve(),
                    endpoint.getMaximumInventory()));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (DroneEndpoint endpoint : endpoints.values()) list.appendTag(endpoint.writeToNbt());
        compound.setTag("Endpoints", list);
        return compound;
    }
}
