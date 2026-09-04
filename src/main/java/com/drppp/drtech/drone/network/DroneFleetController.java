package com.drppp.drtech.drone.network;

import com.drppp.drtech.drone.hardware.DroneChassisTier;
import com.drppp.drtech.drone.hardware.DroneUpgradeType;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

/** Deterministic server-side fleet candidate selection used by automated jobs. */
public final class DroneFleetController {
    private DroneFleetController() {}

    public static Optional<DroneRegistryRecord> selectNearestAvailable(DroneRegistry registry, UUID ownerId,
            int dimension, BlockPos origin, DroneChassisTier minimumChassis, int requiredUpgradeMask, long worldTime) {
        if (registry == null || ownerId == null || origin == null) return Optional.empty();
        DroneChassisTier requiredTier = minimumChassis == null ? DroneChassisTier.HV : minimumChassis;
        return registry.listForOwner(ownerId).stream()
                .filter(record -> record.getDimension() == dimension)
                .filter(record -> DroneRegistry.isOnline(record, worldTime))
                .filter(record -> "READY".equals(record.getStatus()))
                .filter(record -> chassisTier(record).getMetadata() >= requiredTier.getMetadata())
                .filter(record -> (record.getUpgradeMask() & requiredUpgradeMask) == requiredUpgradeMask)
                .min(Comparator.<DroneRegistryRecord>comparingDouble(record -> record.getPosition().distanceSq(origin))
                        .thenComparing(record -> record.getDroneId().toString()));
    }

    public static int requiredUpgradeMask(@Nullable DroneUpgradeType... requiredTypes) {
        int mask = 0;
        if (requiredTypes == null) return mask;
        for (DroneUpgradeType type : requiredTypes) {
            if (type != null) mask |= 1 << type.getMetadata();
        }
        return mask;
    }

    private static DroneChassisTier chassisTier(DroneRegistryRecord record) {
        try { return DroneChassisTier.valueOf(record.getChassis()); }
        catch (IllegalArgumentException ignored) { return DroneChassisTier.HV; }
    }
}
