package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Persisted identity and location for one native logistics endpoint. */
public final class DroneEndpoint {
    private static final long DEFAULT_ITEM_CAPACITY = 9L * 64L;
    private static final long DEFAULT_FLUID_CAPACITY = 64_000L;
    public enum Kind { ITEM, FLUID, EU }

    private final UUID endpointId;
    private final Kind kind;
    private final int dimension;
    private final BlockPos position;
    private final UUID ownerId;
    private final long lastHeartbeat;
    private final boolean loaded;
    private final long requestAmount;
    private final long provideAmount;
    private final int priority;
    private final List<String> whitelist;
    private final long minimumReserve;
    private final long maximumInventory;
    private final List<DroneEndpointResource> resources;

    public DroneEndpoint(UUID endpointId, Kind kind, int dimension, BlockPos position, @Nullable UUID ownerId,
            long lastHeartbeat, boolean loaded) {
        this(endpointId, kind, dimension, position, ownerId, lastHeartbeat, loaded, 0L, 0L, 0);
    }

    public DroneEndpoint(UUID endpointId, Kind kind, int dimension, BlockPos position, @Nullable UUID ownerId,
            long lastHeartbeat, boolean loaded, long requestAmount, long provideAmount, int priority) {
        this(endpointId, kind, dimension, position, ownerId, lastHeartbeat, loaded, requestAmount, provideAmount,
                priority, Collections.emptyList(), 0L, 0L, Collections.emptyList());
    }

    public DroneEndpoint(UUID endpointId, Kind kind, int dimension, BlockPos position, @Nullable UUID ownerId,
            long lastHeartbeat, boolean loaded, long requestAmount, long provideAmount, int priority,
            List<String> whitelist, long minimumReserve, long maximumInventory) {
        this(endpointId, kind, dimension, position, ownerId, lastHeartbeat, loaded, requestAmount, provideAmount,
                priority, whitelist, minimumReserve, maximumInventory, Collections.emptyList());
    }

    public DroneEndpoint(UUID endpointId, Kind kind, int dimension, BlockPos position, @Nullable UUID ownerId,
            long lastHeartbeat, boolean loaded, long requestAmount, long provideAmount, int priority,
            List<String> whitelist, long minimumReserve, long maximumInventory,
            List<DroneEndpointResource> resources) {
        if (endpointId == null || kind == null || position == null) {
            throw new IllegalArgumentException("Endpoint id, kind and position are required");
        }
        this.endpointId = endpointId;
        this.kind = kind;
        this.dimension = dimension;
        this.position = position.toImmutable();
        this.ownerId = ownerId;
        this.lastHeartbeat = Math.max(0L, lastHeartbeat);
        this.loaded = loaded;
        this.requestAmount = Math.max(0L, requestAmount);
        this.provideAmount = Math.max(0L, provideAmount);
        this.priority = Math.max(-100, Math.min(100, priority));
        List<String> normalized = new ArrayList<>();
        if (whitelist != null) {
            for (String value : whitelist) {
                if (value != null && !value.trim().isEmpty() && normalized.size() < 64) {
                    normalized.add(value.trim().substring(0, Math.min(128, value.trim().length())));
                }
            }
        }
        this.whitelist = Collections.unmodifiableList(normalized);
        this.minimumReserve = Math.max(0L, minimumReserve);
        this.maximumInventory = Math.max(0L, maximumInventory);
        List<DroneEndpointResource> snapshots = new ArrayList<>();
        if (resources != null) for (DroneEndpointResource resource : resources) {
            if (resource != null && snapshots.size() < 64) snapshots.add(resource);
        }
        this.resources = Collections.unmodifiableList(snapshots);
    }

    public UUID getEndpointId() { return endpointId; }
    public Kind getKind() { return kind; }
    public int getDimension() { return dimension; }
    public BlockPos getPosition() { return position; }
    @Nullable public UUID getOwnerId() { return ownerId; }
    public long getLastHeartbeat() { return lastHeartbeat; }
    public boolean isLoaded() { return loaded; }
    public long getRequestAmount() { return requestAmount; }
    public long getProvideAmount() { return provideAmount; }
    public int getPriority() { return priority; }
    public List<String> getWhitelist() { return whitelist; }
    public long getMinimumReserve() { return minimumReserve; }
    public long getMaximumInventory() { return maximumInventory; }
    public List<DroneEndpointResource> getResources() { return resources; }
    public long getStoredAmount() {
        long total = 0L;
        for (DroneEndpointResource resource : resources) {
            total = resource.getAmount() > Long.MAX_VALUE - total ? Long.MAX_VALUE : total + resource.getAmount();
        }
        return total;
    }
    public long getStorageCapacity() {
        long capacity = 0L;
        for (DroneEndpointResource resource : resources) capacity = Math.max(capacity, resource.getCapacity());
        if (capacity > 0L) return capacity;
        return kind == Kind.ITEM ? DEFAULT_ITEM_CAPACITY : kind == Kind.FLUID ? DEFAULT_FLUID_CAPACITY : 0L;
    }
    public boolean canStoreResource(String resourceId) {
        if (!matchesResource(resourceId)) return false;
        if (kind != Kind.FLUID || resources.isEmpty()) return true;
        return getResource(resourceId) != null;
    }
    @Nullable public DroneEndpointResource getResource(String resourceId) {
        if (resourceId == null) return null;
        for (DroneEndpointResource resource : resources) {
            if (resourceId.equals(resource.getResourceId())) return resource;
        }
        return null;
    }
    public boolean matchesResource(String resourceId) {
        return DroneEndpointPolicy.matchesResource(this, resourceId);
    }
    public long availableToProvide(long currentAmount, long alreadyReserved) {
        return DroneEndpointPolicy.availableToProvide(this, currentAmount, alreadyReserved);
    }
    public long requestCapacity(long currentAmount, long alreadyReserved) {
        return DroneEndpointPolicy.requestCapacity(this, currentAmount, alreadyReserved);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("EndpointId", endpointId.toString());
        tag.setString("Kind", kind.name());
        tag.setInteger("Dimension", dimension);
        tag.setLong("Position", position.toLong());
        if (ownerId != null) tag.setString("Owner", ownerId.toString());
        tag.setLong("LastHeartbeat", lastHeartbeat);
        tag.setBoolean("Loaded", loaded);
        tag.setLong("RequestAmount", requestAmount);
        tag.setLong("ProvideAmount", provideAmount);
        tag.setInteger("Priority", priority);
        net.minecraft.nbt.NBTTagList allowed = new net.minecraft.nbt.NBTTagList();
        for (String value : whitelist) {
            net.minecraft.nbt.NBTTagCompound entry = new net.minecraft.nbt.NBTTagCompound();
            entry.setString("Value", value);
            allowed.appendTag(entry);
        }
        tag.setTag("Whitelist", allowed);
        tag.setLong("MinimumReserve", minimumReserve);
        tag.setLong("MaximumInventory", maximumInventory);
        net.minecraft.nbt.NBTTagList resourceList = new net.minecraft.nbt.NBTTagList();
        for (DroneEndpointResource resource : resources) resourceList.appendTag(resource.writeToNbt());
        tag.setTag("Resources", resourceList);
        return tag;
    }

    @Nullable
    public static DroneEndpoint readFromNbt(NBTTagCompound tag) {
        UUID endpointId = readUuid(tag, "EndpointId");
        if (endpointId == null || !tag.hasKey("Position", 4)) return null;
        Kind kind;
        try { kind = Kind.valueOf(tag.getString("Kind")); }
        catch (IllegalArgumentException ignored) { return null; }
        return new DroneEndpoint(endpointId, kind, tag.getInteger("Dimension"),
                BlockPos.fromLong(tag.getLong("Position")), readUuid(tag, "Owner"),
                tag.getLong("LastHeartbeat"), tag.getBoolean("Loaded"),
                tag.hasKey("RequestAmount", 4) ? tag.getLong("RequestAmount") : 0L,
                tag.hasKey("ProvideAmount", 4) ? tag.getLong("ProvideAmount") : 0L,
                tag.hasKey("Priority", 3) ? tag.getInteger("Priority") : 0,
                readWhitelist(tag), tag.hasKey("MinimumReserve", 4) ? tag.getLong("MinimumReserve") : 0L,
                tag.hasKey("MaximumInventory", 4) ? tag.getLong("MaximumInventory") : 0L,
                readResources(tag));
    }

    private static List<String> readWhitelist(NBTTagCompound tag) {
        List<String> values = new ArrayList<>();
        net.minecraft.nbt.NBTTagList list = tag.getTagList("Whitelist", 10);
        for (int i = 0; i < list.tagCount() && values.size() < 64; i++) {
            String value = list.getCompoundTagAt(i).getString("Value");
            if (!value.trim().isEmpty()) values.add(value.trim().substring(0, Math.min(128, value.trim().length())));
        }
        return values;
    }

    private static List<DroneEndpointResource> readResources(NBTTagCompound tag) {
        List<DroneEndpointResource> values = new ArrayList<>();
        net.minecraft.nbt.NBTTagList list = tag.getTagList("Resources", 10);
        for (int i = 0; i < list.tagCount() && values.size() < 64; i++) {
            DroneEndpointResource value = DroneEndpointResource.readFromNbt(list.getCompoundTagAt(i));
            if (value != null) values.add(value);
        }
        return values;
    }

    @Nullable
    private static UUID readUuid(NBTTagCompound tag, String key) {
        try { return tag.hasKey(key, 8) ? UUID.fromString(tag.getString(key)) : null; }
        catch (IllegalArgumentException ignored) { return null; }
    }
}
