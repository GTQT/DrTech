package com.drppp.drtech.common.drone.action;

import com.drppp.drtech.common.drone.filter.DroneItemFilterSpec;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Objects;

public final class DroneInteractionRequest {
    private final BlockPos target;
    private final EnumFacing side;
    private final DroneItemFilterSpec heldItemFilter;
    private final DroneFailurePolicy failurePolicy;
    private final boolean useHeldItem;
    private final boolean sneaking;

    public DroneInteractionRequest(BlockPos target, @Nullable EnumFacing side,
            @Nullable DroneItemFilterSpec heldItemFilter, DroneFailurePolicy failurePolicy) {
        this(target, side, heldItemFilter, failurePolicy, true, false);
    }

    public DroneInteractionRequest(BlockPos target, @Nullable EnumFacing side,
            @Nullable DroneItemFilterSpec heldItemFilter, DroneFailurePolicy failurePolicy,
            boolean useHeldItem, boolean sneaking) {
        this.target = Objects.requireNonNull(target, "target").toImmutable();
        this.side = side;
        this.heldItemFilter = heldItemFilter == null ? DroneItemFilterSpec.ANY : heldItemFilter;
        this.failurePolicy = Objects.requireNonNull(failurePolicy, "failurePolicy");
        this.useHeldItem = useHeldItem;
        this.sneaking = sneaking;
    }

    public BlockPos getTarget() { return target; }
    @Nullable public EnumFacing getSide() { return side; }
    public DroneItemFilterSpec getHeldItemFilter() { return heldItemFilter; }
    public DroneFailurePolicy getFailurePolicy() { return failurePolicy; }
    public boolean isUseHeldItem() { return useHeldItem; }
    public boolean isSneaking() { return sneaking; }
}
