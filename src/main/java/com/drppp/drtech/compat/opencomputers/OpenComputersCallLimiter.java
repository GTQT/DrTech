package com.drppp.drtech.compat.opencomputers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-side token bucket guard for OC callbacks. */
public final class OpenComputersCallLimiter {
    private final int capacity;
    private final long refillTicks;
    private final Map<UUID, Bucket> buckets = new HashMap<>();
    public OpenComputersCallLimiter(int capacity, long refillTicks) { this.capacity = Math.max(1, capacity); this.refillTicks = Math.max(1L, refillTicks); }
    public synchronized boolean tryAcquire(UUID caller, long worldTime) {
        if (caller == null) return false;
        Bucket bucket = buckets.get(caller);
        if (bucket == null || worldTime - bucket.lastRefill >= refillTicks) { buckets.put(caller, new Bucket(worldTime, capacity - 1)); return true; }
        if (bucket.tokens <= 0) return false;
        bucket.tokens--; return true;
    }
    private static final class Bucket { long lastRefill; int tokens; Bucket(long tick, int tokens) { this.lastRefill = tick; this.tokens = tokens; } }
}
