package com.drppp.drtech.compat.opencomputers;

/** Stable OC component names reserved by the optional compatibility layer. */
public final class OpenComputersComponentIds {
    public static final String DRONE_DOCK = "drtech_drone_dock";
    public static final String DRONE_PROGRAMMER = "drtech_drone_programmer";
    public static final String DRONE_FLEET = "drtech_drone_fleet";
    private OpenComputersComponentIds() { }

    public static boolean isKnown(String value) {
        return DRONE_DOCK.equals(value) || DRONE_PROGRAMMER.equals(value) || DRONE_FLEET.equals(value);
    }
}
