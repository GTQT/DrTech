package com.drppp.drtech.common.drone.energy;

/** Initial balance constants. Keeping costs centralized makes later config migration deterministic. */
public final class DroneEnergyCosts {

    public static final long IDLE_PER_TICK = 1L;
    public static final long MOVE_PER_TICK = 4L;
    public static final long PATHFIND = 32L;
    public static final long BLOCK_INTERACTION = 128L;
    public static final long ENTITY_INTERACTION = 128L;
    public static final long SELF_REPAIR = 512L;
    public static final long TELEPORT = 32_768L;

    private DroneEnergyCosts() {}
}
