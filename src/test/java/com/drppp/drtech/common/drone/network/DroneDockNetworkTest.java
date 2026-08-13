package com.drppp.drtech.common.drone.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneDockNetworkTest {

    @Test
    void selectsOwnerCompatibleOnlineDockByPriorityThenDistance() {
        UUID owner = UUID.randomUUID();
        DroneDockNetwork network = new DroneDockNetwork();
        network.heartbeat(record(owner, new BlockPos(2, 64, 0), 3, 0, 1_000L, true));
        DroneDockRecord preferred = record(owner, new BlockPos(20, 64, 0), 3, 5, 1_000L, true);
        network.heartbeat(preferred);
        network.heartbeat(record(UUID.randomUUID(), new BlockPos(1, 64, 0), 3, 100, 1_000L, true));

        assertEquals(preferred.getDockId(), network.findNearest(0, BlockPos.ORIGIN, owner, 3,
                1_020L, true, null).orElseThrow(AssertionError::new).getDockId());
    }

    @Test
    void rejectsOfflineOvervoltageAndOccupiedFallbacks() {
        UUID owner = UUID.randomUUID();
        DroneDockNetwork network = new DroneDockNetwork();
        network.heartbeat(record(owner, new BlockPos(1, 0, 0), 4, 0, 1_000L, true));
        network.heartbeat(record(owner, new BlockPos(2, 0, 0), 3, 0, 800L, true));
        network.heartbeat(record(owner, new BlockPos(3, 0, 0), 3, 0, 1_000L, false));

        assertFalse(network.findNearest(0, BlockPos.ORIGIN, owner, 3, 1_020L, true, null).isPresent());
    }

    @Test
    void recordsRoundTripAndAncientEntriesArePruned() {
        UUID owner = UUID.randomUUID();
        DroneDockNetwork source = new DroneDockNetwork();
        DroneDockRecord current = record(owner, new BlockPos(8, 70, -4), 3, 2, 30_000L, true);
        source.heartbeat(current);
        source.heartbeat(record(owner, BlockPos.ORIGIN, 3, 0, 1L, true));

        NBTTagCompound saved = source.writeToNBT(new NBTTagCompound());
        DroneDockNetwork restored = new DroneDockNetwork();
        restored.readFromNBT(saved);
        restored.prune(30_000L);

        assertEquals(1, restored.size());
        assertTrue(restored.getRecord(current.getDockId()).isPresent());
    }

    private static DroneDockRecord record(UUID owner, BlockPos position, int tier, int priority,
            long heartbeat, boolean accepting) {
        return new DroneDockRecord(UUID.randomUUID(), 0, position, owner, "Test Dock", tier, priority,
                heartbeat, true, accepting ? 0 : 1, 10_000L, true, accepting);
    }
}
