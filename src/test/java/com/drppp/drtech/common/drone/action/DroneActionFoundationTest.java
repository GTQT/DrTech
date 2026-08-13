package com.drppp.drtech.common.drone.action;

import com.drppp.drtech.common.drone.filter.DroneItemFilterSpec;
import com.drppp.drtech.common.drone.program.model.DroneArea;
import com.drppp.drtech.common.drone.program.runtime.DroneActionState;
import com.drppp.drtech.common.drone.program.runtime.DroneActionStatus;
import com.drppp.drtech.common.drone.program.runtime.DroneExecutionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DroneActionFoundationTest {

    @Test
    void worldItemRequestBoundsRadiusAmountAndFilter() {
        DroneItemWorldRequest request = new DroneItemWorldRequest(4.5D, 64, null, DroneFailurePolicy.ERROR);
        assertEquals(4.5D, request.getRadius());
        assertEquals(64, request.getMaximumAmount());
        assertEquals(0, request.getFilter().getRules().size());
        assertThrows(IllegalArgumentException.class,
                () -> new DroneItemWorldRequest(0.25D, 1, null, DroneFailurePolicy.ERROR));
        assertThrows(IllegalArgumentException.class,
                () -> new DroneItemWorldRequest(1.0D, 0, null, DroneFailurePolicy.ERROR));
    }

    @Test
    void transferRequestBoundsBatchAndRequiresExactlyOneTarget() {
        DroneTransferRequest request = DroneTransferRequest.at(new BlockPos(1, 2, 3), EnumFacing.DOWN,
                64, 128, DroneFailurePolicy.FAILURE_PORT, DroneItemFilterSpec.ANY);

        assertEquals(64, request.getMaximumAmount());
        assertEquals(64, request.getBatchSize());
        assertEquals(DroneSearchMode.EXACT, request.getSearchMode());
        assertThrows(IllegalArgumentException.class, () -> DroneTransferRequest.within(
                DroneArea.between(BlockPos.ORIGIN, BlockPos.ORIGIN), null, 1, 1, DroneSearchMode.EXACT,
                DroneFailurePolicy.ERROR, DroneItemFilterSpec.ANY));

        DroneTransferRequest areaRequest = DroneTransferRequest.within(
                DroneArea.between(BlockPos.ORIGIN, new BlockPos(2, 0, 2)), EnumFacing.UP, 32, 8,
                DroneSearchMode.NEAREST, false, DroneFailurePolicy.FAILURE_PORT, DroneItemFilterSpec.ANY);
        assertEquals(DroneSearchMode.NEAREST, areaRequest.getSearchMode());
        assertEquals(false, areaRequest.isSkipUnavailable());
        assertEquals(9L, areaRequest.getArea().getVolume());
    }

    @Test
    void semanticFailureStatusRemainsCompatibleWithRuntimeState() {
        DroneExecutionResult result = DroneExecutionResult.failure(
                DroneActionStatus.DENIED, "failed", "Claim denied the action");

        assertEquals(DroneActionState.FAILURE, result.getState());
        assertEquals(DroneActionStatus.DENIED, result.getStatus());
        assertEquals("failed", result.getOutputPort());
        assertEquals("Claim denied the action", result.getError());
    }
}
