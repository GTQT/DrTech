package com.drppp.drtech.hooked;

public enum EnumHookStatus {
    EXTENDING(true),
    PLANTED(true),
    TO_RETRACT(false),
    RETRACTING(false),
    DEAD(false);

    public final boolean active;

    EnumHookStatus(boolean active) {
        this.active = active;
    }
}
