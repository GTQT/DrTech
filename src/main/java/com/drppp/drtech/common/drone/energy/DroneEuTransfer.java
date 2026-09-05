package com.drppp.drtech.common.drone.energy;

import gregtech.api.GTValues;

/** Voltage-safe, conservation-preserving EU transfer between a deployed drone and one endpoint. */
public final class DroneEuTransfer {

    public enum Status {
        SUCCESS,
        INVALID_ENDPOINT,
        OVERVOLTAGE,
        NO_RESOURCE,
        NO_SPACE
    }

    public static final class Result {
        private final Status status;
        private final long amount;

        private Result(Status status, long amount) {
            this.status = status;
            this.amount = amount;
        }

        public Status getStatus() { return status; }
        public long getAmount() { return amount; }
        public boolean isSuccess() { return status == Status.SUCCESS; }
    }

    private DroneEuTransfer() {}

    public static Result importToDrone(DroneEnergyStorage drone, DroneEuEndpoint source, long requested) {
        return importToDrone(drone, source, requested, 1);
    }

    public static Result importToDrone(DroneEnergyStorage drone, DroneEuEndpoint source, long requested,
            int maximumAmperage) {
        if (drone == null || source == null || !source.outputsEnergy()) return result(Status.INVALID_ENDPOINT);
        long packet = packetLimit(drone, requested, maximumAmperage);
        if (source.getOutputVoltage() <= 0L || source.getOutputVoltage() > GTValues.V[drone.getTier()]) {
            return result(Status.OVERVOLTAGE);
        }
        long sourcePacket = safeMultiply(source.getOutputVoltage(), source.getOutputAmperage());
        long allowed = Math.min(packet, Math.min(sourcePacket,
                Math.min(source.getStored(), drone.getCapacity() - drone.getStored())));
        if (allowed <= 0L) return result(source.getStored() <= 0L ? Status.NO_RESOURCE : Status.NO_SPACE);

        long removed = Math.max(0L, -source.changeEnergy(-allowed));
        if (removed <= 0L) return result(Status.NO_RESOURCE);
        long accepted = drone.insert(removed, drone.getTier(), false);
        if (accepted < removed) source.changeEnergy(removed - accepted);
        return accepted > 0L ? new Result(Status.SUCCESS, accepted) : result(Status.NO_SPACE);
    }

    public static Result exportFromDrone(DroneEnergyStorage drone, DroneEuEndpoint target, long requested) {
        return exportFromDrone(drone, target, requested, 1);
    }

    public static Result exportFromDrone(DroneEnergyStorage drone, DroneEuEndpoint target, long requested,
            int maximumAmperage) {
        if (drone == null || target == null || !target.inputsEnergy()) return result(Status.INVALID_ENDPOINT);
        long packet = packetLimit(drone, requested, maximumAmperage);
        if (target.getInputVoltage() <= 0L || target.getInputVoltage() < GTValues.V[drone.getTier()]) {
            return result(Status.OVERVOLTAGE);
        }
        long free = Math.max(0L, target.getCapacity() - target.getStored());
        long targetPacket = safeMultiply(target.getInputVoltage(), target.getInputAmperage());
        long allowed = Math.min(packet, Math.min(targetPacket, Math.min(drone.getStored(), free)));
        if (allowed <= 0L) return result(drone.getStored() <= 0L ? Status.NO_RESOURCE : Status.NO_SPACE);

        long extracted = drone.extract(allowed, false);
        long inserted = Math.max(0L, target.changeEnergy(extracted));
        if (inserted > extracted) inserted = extracted;
        if (inserted < extracted) drone.insert(extracted - inserted, drone.getTier(), false);
        return inserted > 0L ? new Result(Status.SUCCESS, inserted) : result(Status.NO_SPACE);
    }

    public static int percent(DroneEuEndpoint endpoint) {
        if (endpoint == null || endpoint.getCapacity() <= 0L) return 0;
        return (int) Math.min(100L, endpoint.getStored() * 100L / endpoint.getCapacity());
    }

    private static long packetLimit(DroneEnergyStorage drone, long requested, int maximumAmperage) {
        long requestedBounded = Math.max(1L, requested);
        long voltage = GTValues.V[Math.max(0, Math.min(GTValues.V.length - 1, drone.getTier()))];
        return Math.min(requestedBounded, safeMultiply(voltage, Math.max(1, maximumAmperage)));
    }

    private static Result result(Status status) {
        return new Result(status, 0L);
    }

    private static long safeMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) return 0L;
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }
}
