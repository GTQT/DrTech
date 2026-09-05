package com.drppp.drtech.common.drone.action;

import com.drppp.drtech.common.drone.filter.DroneBlockFilterSpec;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public final class DroneWorldQuery {
    private final BlockPos target;
    private final DroneArea area;
    private final DroneBlockFilterSpec blockFilter;
    private final int resultLimit;

    public DroneWorldQuery(@Nullable BlockPos target, @Nullable DroneArea area,
            @Nullable DroneBlockFilterSpec blockFilter, int resultLimit) {
        if ((target == null) == (area == null)) throw new IllegalArgumentException("Exactly one query target is required");
        if (resultLimit <= 0) throw new IllegalArgumentException("Result limit must be positive");
        this.target = target == null ? null : target.toImmutable();
        this.area = area;
        this.blockFilter = blockFilter;
        this.resultLimit = resultLimit;
    }

    @Nullable public BlockPos getTarget() { return target; }
    @Nullable public DroneArea getArea() { return area; }
    @Nullable public DroneBlockFilterSpec getBlockFilter() { return blockFilter; }
    public int getResultLimit() { return resultLimit; }
}
