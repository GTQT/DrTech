package com.drppp.drtech.drone.inventory;

/** Owner-selected response when an item pickup/import cannot fit in the active cargo slots. */
public enum DroneCargoFullPolicy {
    STOP,
    DROP_ONE_STACK,
    RETURN_TO_DOCK;

    public DroneCargoFullPolicy next() {
        DroneCargoFullPolicy[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static DroneCargoFullPolicy fromName(String name) {
        if (name == null || name.isEmpty()) return STOP;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return STOP;
        }
    }
}
