package com.drppp.drtech.drone.entity;

import com.mojang.authlib.GameProfile;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Server-configurable identity policy used by all drone world interactions. */
public enum DroneFakePlayerIdentity {
    PER_DRONE,
    OWNER,
    SHARED;

    private static final UUID SHARED_ID = derived("drtech:drone:shared");

    public GameProfile profile(UUID droneId, @Nullable UUID ownerId, @Nullable String ownerName) {
        UUID safeDroneId = droneId == null ? SHARED_ID : droneId;
        return switch (this) {
            case OWNER -> ownerId == null
                    ? new GameProfile(derived("drtech:drone:" + safeDroneId), "[DrTechDrone]")
                    : new GameProfile(ownerId, validName(ownerName) ? ownerName : "[DrTechOwner]");
            case SHARED -> new GameProfile(SHARED_ID, "[DrTechDrone]");
            default -> new GameProfile(derived("drtech:drone:" + safeDroneId), "[DrTechDrone]");
        };
    }

    private static UUID derived(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean validName(String name) {
        return name != null && !name.isEmpty() && name.length() <= 16;
    }
}
