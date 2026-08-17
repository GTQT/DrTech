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

    /** Native logistics costs are charged for both pickup and delivery. */
    public static long logisticsItems(long items) {
        return items <= 0L ? 0L : 8L + 8L * divideRoundUp(items, 8L);
    }

    public static long logisticsFluid(long milliBuckets) {
        return milliBuckets <= 0L ? 0L : 8L + 4L * divideRoundUp(milliBuckets, 250L);
    }

    /** EU payload remains separate from flight energy; cost scales by transferred voltage packets. */
    public static long logisticsEu(long eu, long chassisVoltage) {
        return eu <= 0L ? 0L : 8L + 4L * divideRoundUp(eu, Math.max(1L, chassisVoltage));
    }

    private static long divideRoundUp(long value, long divisor) {
        return 1L + (value - 1L) / divisor;
    }

    private DroneEnergyCosts() {}
}
