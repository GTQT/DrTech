package com.drppp.drtech.common.drone.energy;

import gregtech.api.GTValues;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DroneEuTransferTest {

    @Test
    void importsOneSafeVoltagePacketWithoutCreatingEu() {
        DroneEnergyStorage drone = new DroneEnergyStorage(10_000L, GTValues.HV, 0L);
        Endpoint source = new Endpoint(5_000L, 10_000L, 0L, GTValues.V[GTValues.MV], false, true);

        DroneEuTransfer.Result result = DroneEuTransfer.importToDrone(drone, source, 9_999L);

        assertEquals(DroneEuTransfer.Status.SUCCESS, result.getStatus());
        assertEquals(GTValues.V[GTValues.MV], result.getAmount());
        assertEquals(5_000L - result.getAmount(), source.stored);
        assertEquals(result.getAmount(), drone.getStored());
    }

    @Test
    void rejectsInputAboveDroneVoltageBeforeMutatingSource() {
        DroneEnergyStorage drone = new DroneEnergyStorage(10_000L, GTValues.HV, 0L);
        Endpoint source = new Endpoint(5_000L, 10_000L, 0L, GTValues.V[GTValues.EV], false, true);

        assertEquals(DroneEuTransfer.Status.OVERVOLTAGE,
                DroneEuTransfer.importToDrone(drone, source, 512L).getStatus());
        assertEquals(5_000L, source.stored);
        assertEquals(0L, drone.getStored());
    }

    @Test
    void partialTargetAcceptanceReturnsUnacceptedEuToDrone() {
        DroneEnergyStorage drone = new DroneEnergyStorage(10_000L, GTValues.HV, 512L);
        Endpoint target = new Endpoint(0L, 10_000L, GTValues.V[GTValues.HV], 0L, true, false);
        target.acceptLimit = 100L;

        DroneEuTransfer.Result result = DroneEuTransfer.exportFromDrone(drone, target, 512L);

        assertEquals(100L, result.getAmount());
        assertEquals(100L, target.stored);
        assertEquals(412L, drone.getStored());
    }

    @Test
    void doesNotSendDroneVoltageIntoLowerInputTier() {
        DroneEnergyStorage drone = new DroneEnergyStorage(10_000L, GTValues.HV, 512L);
        Endpoint target = new Endpoint(0L, 10_000L, GTValues.V[GTValues.MV], 0L, true, false);

        assertEquals(DroneEuTransfer.Status.OVERVOLTAGE,
                DroneEuTransfer.exportFromDrone(drone, target, 512L).getStatus());
        assertEquals(512L, drone.getStored());
    }

    private static final class Endpoint implements DroneEuEndpoint {
        private long stored;
        private final long capacity;
        private final long inputVoltage;
        private final long outputVoltage;
        private final boolean input;
        private final boolean output;
        private long acceptLimit = Long.MAX_VALUE;

        private Endpoint(long stored, long capacity, long inputVoltage, long outputVoltage,
                boolean input, boolean output) {
            this.stored = stored;
            this.capacity = capacity;
            this.inputVoltage = inputVoltage;
            this.outputVoltage = outputVoltage;
            this.input = input;
            this.output = output;
        }

        @Override public long getStored() { return stored; }
        @Override public long getCapacity() { return capacity; }
        @Override public long getInputVoltage() { return inputVoltage; }
        @Override public long getOutputVoltage() { return outputVoltage; }
        @Override public long getInputAmperage() { return 1L; }
        @Override public long getOutputAmperage() { return 1L; }
        @Override public boolean inputsEnergy() { return input; }
        @Override public boolean outputsEnergy() { return output; }
        @Override public long changeEnergy(long delta) {
            if (delta < 0L) {
                long removed = Math.min(stored, -delta);
                stored -= removed;
                return -removed;
            }
            long inserted = Math.min(Math.min(delta, capacity - stored), acceptLimit);
            stored += inserted;
            return inserted;
        }
    }
}
