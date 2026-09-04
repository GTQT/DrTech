package com.drppp.drtech.drone.energy;

/** Small capability-neutral view of an external GT EU buffer. All mutation returns the actual signed delta. */
public interface DroneEuEndpoint {
    long getStored();
    long getCapacity();
    long getInputVoltage();
    long getOutputVoltage();
    long getInputAmperage();
    long getOutputAmperage();
    boolean inputsEnergy();
    boolean outputsEnergy();

    /** Positive inserts into this endpoint; negative removes. Return the actual signed EU change. */
    long changeEnergy(long delta);
}
