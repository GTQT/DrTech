package com.drppp.drtech.common.drone.program.runtime.service;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/** Immutable result returned by entity sensing operations. */
public final class DroneEntitySensorResult {
    public static final DroneEntitySensorResult EMPTY = new DroneEntitySensorResult(0, null, 0F, 0F, false, false, 0F);
    private final int count;
    private final BlockPos nearestPosition;
    private final float health;
    private final float maxHealth;
    private final String name;
    private final String entityId;
    private final String entityUuid;
    private final boolean owner;
    private final boolean owned;
    private final boolean hostile;
    private final float droneDamage;

    public DroneEntitySensorResult(int count, @Nullable BlockPos nearestPosition, float health, float maxHealth,
            boolean owner, boolean hostile, float droneDamage) {
        this(count, nearestPosition, health, maxHealth, owner, false, hostile, droneDamage);
    }

    public DroneEntitySensorResult(int count, @Nullable BlockPos nearestPosition, float health, float maxHealth,
            boolean owner, boolean owned, boolean hostile, float droneDamage) {
        this(count, nearestPosition, health, maxHealth, "", "", "", owner, owned, hostile, droneDamage);
    }

    public DroneEntitySensorResult(int count, @Nullable BlockPos nearestPosition, float health, float maxHealth,
            String name, String entityId, String entityUuid, boolean owner, boolean owned,
            boolean hostile, float droneDamage) {
        this.count = Math.max(0, count);
        this.nearestPosition = nearestPosition == null ? null : nearestPosition.toImmutable();
        this.health = Math.max(0F, health);
        this.maxHealth = Math.max(0F, maxHealth);
        this.name = name == null ? "" : name;
        this.entityId = entityId == null ? "" : entityId;
        this.entityUuid = entityUuid == null ? "" : entityUuid;
        this.owner = owner;
        this.owned = owned;
        this.hostile = hostile;
        this.droneDamage = Math.max(0F, Math.min(1F, droneDamage));
    }
    public int getCount() { return count; }
    @Nullable public BlockPos getNearestPosition() { return nearestPosition; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public String getName() { return name; }
    public String getEntityId() { return entityId; }
    public String getEntityUuid() { return entityUuid; }
    public boolean isOwner() { return owner; }
    public boolean isOwned() { return owned; }
    public boolean isHostile() { return hostile; }
    public float getDroneDamage() { return droneDamage; }
}
