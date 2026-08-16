package com.drppp.drtech.common.drone.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import com.drppp.drtech.common.drone.hardware.DroneChassisTier;
import com.drppp.drtech.common.drone.hardware.DroneHardwareStats;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeDataCodec;
import com.drppp.drtech.common.drone.filter.DroneItemFilterSpec;
import com.drppp.drtech.common.drone.firmware.DroneSafetyFirmware;
import com.drppp.drtech.common.drone.program.codec.DroneProgramMigrator;
import com.drppp.drtech.common.drone.program.codec.DroneProgramFormatException;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/** Shared item/entity payload keys for lossless deployment and recall. */
public final class DroneItemData {

    public static final int CURRENT_DATA_VERSION = 9;

    public static final String PROGRAM_TAG = "DrTechDroneProgram";
    public static final String INVENTORY_TAG = "DrTechDroneInventory";
    public static final String WEAPONS_TAG = "DrTechDroneWeapons";
    public static final String FLUID_TAG = "DrTechDroneFluid";
    public static final String DOCK_TAG = "DrTechDroneDock";
    public static final String RUNTIME_TAG = "DrTechDroneRuntime";
    public static final String UPGRADES_TAG = "DrTechDroneUpgrades";
    public static final String DATA_VERSION_TAG = "DrTechDroneDataVersion";
    public static final String PROGRAM_VERSION_TAG = "DrTechDroneProgramVersion";
    public static final String UPGRADE_VERSION_TAG = "DrTechDroneUpgradeVersion";
    public static final String DRONE_ID_TAG = "DrTechDroneId";
    public static final String OWNER_TAG = "DrTechDroneOwner";
    public static final String CHASSIS_TAG = "DrTechDroneChassis";
    public static final String SAFETY_FIRMWARE_TAG = "DrTechDroneSafetyFirmware";
    public static final String AUTO_PICKUP_MODE_TAG = "DrTechDroneAutoPickupMode";
    public static final String AUTO_PICKUP_FILTER_TAG = "DrTechDroneAutoPickupFilter";
    public static final String FALLBACK_DOCKS_TAG = "DrTechDroneFallbackDocks";
    public static final String LOADED_ENTITY_TAG = "DrTechDroneLoadedEntity";
    public static final String LOADED_ENTITY_UUID_TAG = "DrTechDroneLoadedEntityUuid";
    public static final String STATUS_LABEL_TAG = "DrTechDroneStatusLabel";
    public static final String ROTORS_ACTIVE_TAG = "DrTechDroneRotorsActive";
    public static final String STATUS_LIGHT_MODE_TAG = "DrTechDroneStatusLightMode";
    public static final String FOLLOW_TARGET_TAG = "DrTechDroneFollowTarget";
    public static final String AVOID_TARGET_TAG = "DrTechDroneAvoidTarget";
    public static final String ATTACK_TARGET_TAG = "DrTechDroneAttackTarget";

    private DroneItemData() {}

    public static void setAttackTargetLock(ItemStack stack, @Nullable UUID targetId, @Nullable BlockPos anchor) {
        NBTTagCompound root = getOrCreateRoot(stack);
        if (targetId == null || anchor == null) {
            root.removeTag(ATTACK_TARGET_TAG);
        } else {
            NBTTagCompound lock = new NBTTagCompound();
            lock.setString("Target", targetId.toString());
            lock.setLong("Anchor", anchor.toLong());
            root.setTag(ATTACK_TARGET_TAG, lock);
        }
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    @Nullable
    public static UUID getAttackTargetId(ItemStack stack) {
        NBTTagCompound lock = getAttackTargetLock(stack);
        if (lock == null || !lock.hasKey("Target", 8)) return null;
        try { return UUID.fromString(lock.getString("Target")); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    @Nullable
    public static BlockPos getAttackTargetAnchor(ItemStack stack) {
        NBTTagCompound lock = getAttackTargetLock(stack);
        return lock != null && lock.hasKey("Anchor", 4) ? BlockPos.fromLong(lock.getLong("Anchor")) : null;
    }

    @Nullable
    private static NBTTagCompound getAttackTargetLock(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root != null && root.hasKey(ATTACK_TARGET_TAG, 10)
                ? root.getCompoundTag(ATTACK_TARGET_TAG) : null;
    }

    public static void setEntityTargetLock(ItemStack stack, boolean following,
            @Nullable UUID targetId, @Nullable BlockPos anchor) {
        NBTTagCompound root = getOrCreateRoot(stack);
        String key = following ? FOLLOW_TARGET_TAG : AVOID_TARGET_TAG;
        if (targetId == null || anchor == null) {
            root.removeTag(key);
        } else {
            NBTTagCompound lock = new NBTTagCompound();
            lock.setString("Target", targetId.toString());
            lock.setLong("Anchor", anchor.toLong());
            root.setTag(key, lock);
        }
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    @Nullable
    public static UUID getEntityTargetId(ItemStack stack, boolean following) {
        NBTTagCompound lock = getEntityTargetLock(stack, following);
        if (lock == null || !lock.hasKey("Target", 8)) return null;
        try { return UUID.fromString(lock.getString("Target")); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    @Nullable
    public static BlockPos getEntityTargetAnchor(ItemStack stack, boolean following) {
        NBTTagCompound lock = getEntityTargetLock(stack, following);
        return lock != null && lock.hasKey("Anchor", 4) ? BlockPos.fromLong(lock.getLong("Anchor")) : null;
    }

    @Nullable
    private static NBTTagCompound getEntityTargetLock(ItemStack stack, boolean following) {
        NBTTagCompound root = stack.getTagCompound();
        String key = following ? FOLLOW_TARGET_TAG : AVOID_TARGET_TAG;
        return root != null && root.hasKey(key, 10) ? root.getCompoundTag(key) : null;
    }

    public static String getStatusLabel(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root == null ? "" : root.getString(STATUS_LABEL_TAG);
    }

    public static void setStatusLabel(ItemStack stack, @Nullable String label) {
        NBTTagCompound root = getOrCreateRoot(stack);
        if (label == null || label.isEmpty()) root.removeTag(STATUS_LABEL_TAG);
        else root.setString(STATUS_LABEL_TAG, label);
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    public static boolean areRotorsActive(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root == null || !root.hasKey(ROTORS_ACTIVE_TAG) || root.getBoolean(ROTORS_ACTIVE_TAG);
    }

    public static void setRotorsActive(ItemStack stack, boolean active) {
        NBTTagCompound root = getOrCreateRoot(stack);
        root.setBoolean(ROTORS_ACTIVE_TAG, active);
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    public static int getStatusLightMode(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root == null ? 0 : Math.max(0, Math.min(4, root.getInteger(STATUS_LIGHT_MODE_TAG)));
    }

    public static void setStatusLightMode(ItemStack stack, int mode) {
        NBTTagCompound root = getOrCreateRoot(stack);
        root.setInteger(STATUS_LIGHT_MODE_TAG, Math.max(0, Math.min(4, mode)));
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    @Nullable
    public static NBTTagCompound getLoadedEntity(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root != null && root.hasKey(LOADED_ENTITY_TAG, 10)
                ? root.getCompoundTag(LOADED_ENTITY_TAG).copy() : null;
    }

    public static void setLoadedEntity(ItemStack stack, @Nullable NBTTagCompound entity, @Nullable UUID uuid) {
        NBTTagCompound root = getOrCreateRoot(stack);
        if (entity == null) {
            root.removeTag(LOADED_ENTITY_TAG);
            root.removeTag(LOADED_ENTITY_UUID_TAG);
        } else {
            root.setTag(LOADED_ENTITY_TAG, entity.copy());
            if (uuid != null) root.setString(LOADED_ENTITY_UUID_TAG, uuid.toString());
        }
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    @Nullable
    public static UUID getLoadedEntityUuid(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(LOADED_ENTITY_UUID_TAG, 8)) return null;
        try { return UUID.fromString(root.getString(LOADED_ENTITY_UUID_TAG)); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    public static NBTTagCompound getProgram(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root != null && root.hasKey(PROGRAM_TAG, 10) ? root.getCompoundTag(PROGRAM_TAG).copy() : null;
    }

    public static void setProgram(ItemStack stack, NBTTagCompound program) {
        if (program == null) {
            return;
        }
        NBTTagCompound root = getOrCreateRoot(stack);
        root.setTag(PROGRAM_TAG, program.copy());
        root.setInteger(PROGRAM_VERSION_TAG, program.getInteger("Schema"));
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
        root.removeTag(RUNTIME_TAG);
    }

    @Nullable
    public static NBTTagCompound getRuntime(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return copyRuntimePayload(root != null && root.hasKey(RUNTIME_TAG, 10)
                ? root.getCompoundTag(RUNTIME_TAG) : null);
    }

    public static void setRuntime(ItemStack stack, @Nullable NBTTagCompound runtime) {
        NBTTagCompound root = getOrCreateRoot(stack);
        if (runtime == null) root.removeTag(RUNTIME_TAG);
        else root.setTag(RUNTIME_TAG, copyRuntimePayload(runtime));
    }

    @Nullable
    public static NBTTagCompound getSafetyFirmware(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root != null && root.hasKey(SAFETY_FIRMWARE_TAG, 10)
                ? copySafetyFirmwarePayload(root.getCompoundTag(SAFETY_FIRMWARE_TAG)) : null;
    }

    public static void setSafetyFirmware(ItemStack stack, @Nullable NBTTagCompound firmware) {
        NBTTagCompound root = getOrCreateRoot(stack);
        if (firmware == null) root.removeTag(SAFETY_FIRMWARE_TAG);
        else root.setTag(SAFETY_FIRMWARE_TAG, copySafetyFirmwarePayload(firmware));
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    public static String getAutoPickupMode(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root == null ? "ALL" : root.getString(AUTO_PICKUP_MODE_TAG);
    }

    public static void setAutoPickupMode(ItemStack stack, String mode) {
        NBTTagCompound root = getOrCreateRoot(stack);
        root.setString(AUTO_PICKUP_MODE_TAG, mode == null ? "ALL" : mode);
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    public static DroneItemFilterSpec getAutoPickupFilter(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return root == null || !root.hasKey(AUTO_PICKUP_FILTER_TAG, 10)
                ? DroneItemFilterSpec.ANY
                : DroneItemFilterSpec.readFromNbt(root.getCompoundTag(AUTO_PICKUP_FILTER_TAG));
    }

    public static void setAutoPickupFilter(ItemStack stack, @Nullable DroneItemFilterSpec filter) {
        NBTTagCompound root = getOrCreateRoot(stack);
        if (filter == null) root.removeTag(AUTO_PICKUP_FILTER_TAG);
        else root.setTag(AUTO_PICKUP_FILTER_TAG, filter.writeToNbt());
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    static NBTTagCompound copySafetyFirmwarePayload(NBTTagCompound firmware) {
        return firmware == null ? new NBTTagCompound() : firmware.copy();
    }

    @Nullable
    static NBTTagCompound copyRuntimePayload(@Nullable NBTTagCompound runtime) {
        return runtime == null ? null : runtime.copy();
    }

    public static NBTTagCompound getInventory(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return copyInventoryPayload(root != null && root.hasKey(INVENTORY_TAG, 10)
                ? root.getCompoundTag(INVENTORY_TAG) : null);
    }

    public static void setInventory(ItemStack stack, NBTTagCompound inventory) {
        NBTTagCompound root = getOrCreateRoot(stack);
        root.setTag(INVENTORY_TAG, copyInventoryPayload(inventory));
    }

    public static NBTTagCompound getWeapons(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        NBTTagCompound weapons = root != null && root.hasKey(WEAPONS_TAG, 10)
                ? root.getCompoundTag(WEAPONS_TAG).copy() : new NBTTagCompound();
        weapons.setInteger("Size", 2);
        return weapons;
    }

    public static void setWeapons(ItemStack stack, NBTTagCompound weapons) {
        NBTTagCompound root = getOrCreateRoot(stack);
        NBTTagCompound value = weapons == null ? new NBTTagCompound() : weapons.copy();
        value.setInteger("Size", 2);
        root.setTag(WEAPONS_TAG, value);
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    public static NBTTagCompound getFluid(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return copyFluidPayload(root != null && root.hasKey(FLUID_TAG, 10)
                ? root.getCompoundTag(FLUID_TAG) : null);
    }

    public static void setFluid(ItemStack stack, @Nullable NBTTagCompound fluid) {
        NBTTagCompound root = getOrCreateRoot(stack);
        if (fluid == null || fluid.getKeySet().isEmpty()) root.removeTag(FLUID_TAG);
        else root.setTag(FLUID_TAG, fluid.copy());
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    static NBTTagCompound copyFluidPayload(@Nullable NBTTagCompound fluid) {
        return fluid == null ? new NBTTagCompound() : fluid.copy();
    }

    static NBTTagCompound copyInventoryPayload(NBTTagCompound inventory) {
        NBTTagCompound value = inventory == null ? new NBTTagCompound() : inventory.copy();
        value.setInteger("Size", DroneHardwareStats.MAX_CARGO_SLOTS);
        return value;
    }

    public static NBTTagCompound getUpgrades(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return DroneUpgradeDataCodec.migrate(root != null && root.hasKey(UPGRADES_TAG, 10)
                ? root.getCompoundTag(UPGRADES_TAG) : null);
    }

    public static void setUpgrades(ItemStack stack, NBTTagCompound upgrades) {
        NBTTagCompound root = getOrCreateRoot(stack);
        root.setTag(UPGRADES_TAG, DroneUpgradeDataCodec.migrate(upgrades));
        root.setInteger(UPGRADE_VERSION_TAG, DroneUpgradeDataCodec.CURRENT_VERSION);
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    public static void setUpgrades(ItemStack stack, IItemHandler upgrades) {
        setUpgrades(stack, DroneUpgradeDataCodec.write(upgrades));
    }

    static NBTTagCompound copyUpgradesPayload(@Nullable NBTTagCompound upgrades) {
        return DroneUpgradeDataCodec.migrate(upgrades);
    }

    public static void migrateInPlace(ItemStack stack, @Nullable UUID claimingOwner) {
        NBTTagCompound root = getOrCreateRoot(stack);
        migratePayload(root, stack.getMetadata(), claimingOwner);
    }

    static void migratePayload(NBTTagCompound root, int chassisMetadata, @Nullable UUID claimingOwner) {
        UUID droneId = readUuid(root, DRONE_ID_TAG);
        if (droneId == null) root.setString(DRONE_ID_TAG, UUID.randomUUID().toString());
        if (readUuid(root, OWNER_TAG) == null && claimingOwner != null) {
            root.setString(OWNER_TAG, claimingOwner.toString());
        }
        DroneChassisTier chassis = DroneChassisTier.fromId(root.getString(CHASSIS_TAG),
                DroneChassisTier.fromMetadata(chassisMetadata));
        root.setString(CHASSIS_TAG, chassis.getId().toString());
        if (root.hasKey(UPGRADES_TAG, 10)) {
            root.setTag(UPGRADES_TAG, DroneUpgradeDataCodec.migrate(root.getCompoundTag(UPGRADES_TAG)));
        } else {
            root.setTag(UPGRADES_TAG, DroneUpgradeDataCodec.migrate(null));
        }
        root.setInteger(UPGRADE_VERSION_TAG, DroneUpgradeDataCodec.CURRENT_VERSION);
        if (root.hasKey(PROGRAM_TAG, 10)) {
            try {
                NBTTagCompound migrated = DroneProgramMigrator.migrate(root.getCompoundTag(PROGRAM_TAG));
                root.setTag(PROGRAM_TAG, migrated);
                root.setInteger(PROGRAM_VERSION_TAG, DroneProgramMigrator.CURRENT_SCHEMA);
            } catch (DroneProgramFormatException ignored) {
                root.setInteger(PROGRAM_VERSION_TAG, root.getCompoundTag(PROGRAM_TAG).getInteger("Schema"));
            }
        }
        if (!root.hasKey(SAFETY_FIRMWARE_TAG, 10)) {
            root.setTag(SAFETY_FIRMWARE_TAG, new DroneSafetyFirmware().writeToNbt());
        }
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    public static UUID getOrCreateDroneId(ItemStack stack) {
        migrateInPlace(stack, null);
        return readUuid(stack.getTagCompound(), DRONE_ID_TAG);
    }

    @Nullable
    public static UUID getOwnerId(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return readUuid(root, OWNER_TAG);
    }

    public static void setIdentity(ItemStack stack, UUID droneId, @Nullable UUID ownerId) {
        NBTTagCompound root = getOrCreateRoot(stack);
        root.setString(DRONE_ID_TAG, droneId.toString());
        if (ownerId == null) root.removeTag(OWNER_TAG);
        else root.setString(OWNER_TAG, ownerId.toString());
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    public static DroneChassisTier getChassis(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        return DroneChassisTier.fromId(root == null ? "" : root.getString(CHASSIS_TAG),
                DroneChassisTier.fromMetadata(stack.getMetadata()));
    }

    public static void setChassis(ItemStack stack, DroneChassisTier chassis) {
        NBTTagCompound root = getOrCreateRoot(stack);
        root.setString(CHASSIS_TAG, chassis.getId().toString());
        root.setInteger(DATA_VERSION_TAG, CURRENT_DATA_VERSION);
    }

    @Nullable
    public static BlockPos getDock(ItemStack stack, int dimension) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(DOCK_TAG, 10)) return null;
        NBTTagCompound dock = root.getCompoundTag(DOCK_TAG);
        if (dock.getInteger("Dimension") != dimension) return null;
        return new BlockPos(dock.getInteger("X"), dock.getInteger("Y"), dock.getInteger("Z"));
    }

    public static void setDock(ItemStack stack, @Nullable BlockPos position, int dimension) {
        NBTTagCompound root = getOrCreateRoot(stack);
        if (position == null) {
            root.removeTag(DOCK_TAG);
            return;
        }
        NBTTagCompound dock = new NBTTagCompound();
        dock.setInteger("Dimension", dimension);
        dock.setInteger("X", position.getX());
        dock.setInteger("Y", position.getY());
        dock.setInteger("Z", position.getZ());
        root.setTag(DOCK_TAG, dock);
    }

    public static List<UUID> getFallbackDocks(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(FALLBACK_DOCKS_TAG, 9)) return Collections.emptyList();
        NBTTagList list = root.getTagList(FALLBACK_DOCKS_TAG, 8);
        List<UUID> result = new ArrayList<>();
        for (int index = 0; index < list.tagCount() && result.size() < 8; index++) {
            try {
                UUID id = UUID.fromString(list.getStringTagAt(index));
                if (!result.contains(id)) result.add(id);
            } catch (IllegalArgumentException ignored) {
                // Ignore damaged or legacy entries while preserving the remaining preference order.
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static void setFallbackDocks(ItemStack stack, @Nullable List<UUID> dockIds) {
        NBTTagCompound root = getOrCreateRoot(stack);
        NBTTagList list = new NBTTagList();
        if (dockIds != null) {
            int count = 0;
            for (UUID id : new LinkedHashSet<>(dockIds)) {
                if (id == null) continue;
                list.appendTag(new NBTTagString(id.toString()));
                if (++count >= 8) break;
            }
        }
        if (list.tagCount() == 0) root.removeTag(FALLBACK_DOCKS_TAG);
        else root.setTag(FALLBACK_DOCKS_TAG, list);
    }

    private static NBTTagCompound getOrCreateRoot(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }
        return root;
    }

    @Nullable
    private static UUID readUuid(@Nullable NBTTagCompound root, String key) {
        if (root == null || !root.hasKey(key, 8)) return null;
        try {
            return UUID.fromString(root.getString(key));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
