package com.drppp.drtech.common.drone.action;

import com.drppp.drtech.common.drone.filter.DroneItemFilterSpec;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Objects;

/** Immutable transfer intent shared by item actions and the future server-side inventory search service. */
public final class DroneTransferRequest {
    private final BlockPos target;
    private final DroneArea area;
    private final EnumFacing side;
    private final int maximumAmount;
    private final int batchSize;
    private final DroneSearchMode searchMode;
    private final DroneFailurePolicy failurePolicy;
    private final DroneItemFilterSpec filter;
    private final boolean skipUnavailable;

    private DroneTransferRequest(@Nullable BlockPos target, @Nullable DroneArea area, @Nullable EnumFacing side,
            int maximumAmount, int batchSize, DroneSearchMode searchMode, DroneFailurePolicy failurePolicy,
            DroneItemFilterSpec filter, boolean skipUnavailable) {
        if ((target == null) == (area == null)) throw new IllegalArgumentException("Exactly one transfer target is required");
        if (maximumAmount <= 0 || batchSize <= 0 || maximumAmount > 1_000_000 || batchSize > 1_000_000) {
            throw new IllegalArgumentException("Transfer amounts must be between 1 and 1000000");
        }
        this.target = target == null ? null : target.toImmutable();
        this.area = area;
        this.side = side;
        this.maximumAmount = maximumAmount;
        this.batchSize = Math.min(batchSize, maximumAmount);
        this.searchMode = Objects.requireNonNull(searchMode, "searchMode");
        this.failurePolicy = Objects.requireNonNull(failurePolicy, "failurePolicy");
        this.filter = filter == null ? DroneItemFilterSpec.ANY : filter;
        this.skipUnavailable = skipUnavailable;
    }

    public static DroneTransferRequest at(BlockPos target, @Nullable EnumFacing side, int maximumAmount,
            int batchSize, DroneFailurePolicy failurePolicy, DroneItemFilterSpec filter) {
        return new DroneTransferRequest(Objects.requireNonNull(target, "target"), null, side, maximumAmount,
                batchSize, DroneSearchMode.EXACT, failurePolicy, filter, false);
    }

    public static DroneTransferRequest within(DroneArea area, @Nullable EnumFacing side, int maximumAmount,
            int batchSize, DroneSearchMode searchMode, DroneFailurePolicy failurePolicy, DroneItemFilterSpec filter) {
        if (searchMode == DroneSearchMode.EXACT) throw new IllegalArgumentException("Area transfer needs a search mode");
        return new DroneTransferRequest(null, Objects.requireNonNull(area, "area"), side, maximumAmount,
                batchSize, searchMode, failurePolicy, filter, true);
    }

    public static DroneTransferRequest within(DroneArea area, @Nullable EnumFacing side, int maximumAmount,
            int batchSize, DroneSearchMode searchMode, boolean skipUnavailable,
            DroneFailurePolicy failurePolicy, DroneItemFilterSpec filter) {
        if (searchMode == DroneSearchMode.EXACT) throw new IllegalArgumentException("Area transfer needs a search mode");
        return new DroneTransferRequest(null, Objects.requireNonNull(area, "area"), side, maximumAmount,
                batchSize, searchMode, failurePolicy, filter, skipUnavailable);
    }

    @Nullable public BlockPos getTarget() { return target; }
    @Nullable public DroneArea getArea() { return area; }
    @Nullable public EnumFacing getSide() { return side; }
    public int getMaximumAmount() { return maximumAmount; }
    public int getBatchSize() { return batchSize; }
    public DroneSearchMode getSearchMode() { return searchMode; }
    public DroneFailurePolicy getFailurePolicy() { return failurePolicy; }
    public DroneItemFilterSpec getFilter() { return filter; }
    public boolean isSkipUnavailable() { return skipUnavailable; }
}
