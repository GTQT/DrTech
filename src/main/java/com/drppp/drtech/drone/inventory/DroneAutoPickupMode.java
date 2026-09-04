package com.drppp.drtech.drone.inventory;

/** Automatic dropped-item pickup policy. Program pickup actions are unaffected. */
public enum DroneAutoPickupMode {
    OFF,
    ALL,
    FILTER_MATCHING,
    PROGRAM_ONLY;

    public static DroneAutoPickupMode fromName(String name) {
        if (name == null || name.isEmpty()) return ALL;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return ALL;
        }
    }
}
