package com.drppp.drtech.common.drone.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import com.drppp.drtech.common.drone.hardware.DroneChassisTier;
import com.drppp.drtech.common.drone.hardware.DroneHardwareStats;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeDataCodec;
import com.drppp.drtech.common.drone.program.codec.DroneProgramMigrator;
import com.drppp.drtech.common.drone.program.codec.DroneProgramFormatException;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.UUID;

/** Shared item/entity payload keys for lossless deployment and recall. */
public final class DroneItemData {

    public static final int CURRENT_DATA_VERSION = 4;

    public static final String PROGRAM_TAG = "DrTechDroneProgram";
    public static final String INVENTORY_TAG = "DrTechDroneInventory";
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

    private DroneItemData() {}

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
