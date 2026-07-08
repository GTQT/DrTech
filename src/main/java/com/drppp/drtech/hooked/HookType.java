package com.drppp.drtech.hooked;

public enum HookType {
    WOOD(1, 8, 8, 4, 0.5),
    IRON(2, 16, 16, 8, 0.5),
    DIAMOND(4, 24, 24, 20, 0.5),
    RED(4, 24, 24, 20, 0.5),
    ENDER(1, 64, -1, 45, 0.5);

    public final int count;
    public final int range;
    public final double speed;
    public final int rangeSq;
    public final double pullStrength;
    public final double hookLength;

    HookType(int count, int range, int blocksPerSecond, int reelSpeed, double hookLength) {
        this.count = count;
        this.range = range;
        this.speed = blocksPerSecond == -1 ? range : blocksPerSecond / 20.0;
        this.rangeSq = range * range;
        this.pullStrength = reelSpeed / 20.0;
        this.hookLength = hookLength;
    }
}
