package com.drppp.drtech.common.drone.filter;

public enum DroneFilterMode {
    WHITELIST,
    BLACKLIST;

    public boolean apply(boolean matched) { return this == WHITELIST ? matched : !matched; }

    public static DroneFilterMode fromName(String name) {
        try {
            return valueOf(name);
        } catch (RuntimeException ignored) {
            return WHITELIST;
        }
    }
}
