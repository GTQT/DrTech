package com.drppp.drtech.common.drone.network;

import javax.annotation.Nullable;
import java.util.List;

/** Pure policy checks shared by endpoint UIs and the server-side logistics task layer. */
public final class DroneEndpointPolicy {
    private DroneEndpointPolicy() { }

    /** Empty whitelist means unrestricted; otherwise resource ids must match exactly. */
    public static boolean matchesResource(@Nullable DroneEndpoint endpoint, @Nullable String resourceId) {
        if (endpoint == null || resourceId == null || resourceId.trim().isEmpty()) return false;
        List<String> whitelist = endpoint.getWhitelist();
        if (whitelist.isEmpty()) return true;
        String normalized = resourceId.trim();
        for (String allowed : whitelist) if (normalized.equals(allowed)) return true;
        return false;
    }

    /** Amount that can be provided without violating reserve or maximum inventory policy. */
    public static long availableToProvide(@Nullable DroneEndpoint endpoint, long currentAmount,
            long alreadyReserved) {
        if (endpoint == null || currentAmount <= 0L) return 0L;
        long safeCurrent = Math.max(0L, currentAmount);
        long reserved = Math.max(0L, alreadyReserved);
        long reserve = endpoint.getMinimumReserve();
        long protectedAmount = reserve >= Long.MAX_VALUE - reserved ? Long.MAX_VALUE : reserve + reserved;
        long available = safeCurrent > protectedAmount ? safeCurrent - protectedAmount : 0L;
        long cap = endpoint.getProvideAmount();
        return cap > 0L ? Math.min(available, cap) : available;
    }

    /** Amount that can be requested without exceeding the configured inventory ceiling. */
    public static long requestCapacity(@Nullable DroneEndpoint endpoint, long currentAmount,
            long alreadyReserved) {
        if (endpoint == null) return 0L;
        long current = Math.max(0L, currentAmount);
        long reserved = Math.max(0L, alreadyReserved);
        long maximum = endpoint.getMaximumInventory();
        long occupied = current >= Long.MAX_VALUE - reserved ? Long.MAX_VALUE : current + reserved;
        long available = maximum > 0L && occupied < maximum
                ? maximum - occupied : (maximum == 0L ? Long.MAX_VALUE : 0L);
        long requested = endpoint.getRequestAmount();
        return requested > 0L ? Math.min(available, requested) : available;
    }
}
