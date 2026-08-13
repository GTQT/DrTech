package com.drppp.drtech.common.drone.energy;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneEnergyStorageTest {

    @Test
    void rejectsOverVoltageAndClampsAcceptedEu() {
        DroneEnergyStorage storage = new DroneEnergyStorage(1_000L, 3);

        assertEquals(0L, storage.insert(100L, 4, false));
        assertEquals(1_000L, storage.insert(2_000L, 3, false));
        assertEquals(1_000L, storage.getStored());
    }

    @Test
    void simulationDoesNotMutateAndConsumeIsAtomic() {
        DroneEnergyStorage storage = new DroneEnergyStorage(1_000L, 3, 300L);

        assertEquals(200L, storage.extract(200L, true));
        assertEquals(300L, storage.getStored());
        assertFalse(storage.consume(301L));
        assertTrue(storage.consume(300L));
        assertEquals(0L, storage.getStored());
    }

    @Test
    void nbtReadClampsCorruptStoredValue() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("Capacity", 500L);
        tag.setLong("StoredEU", 9_000L);
        tag.setInteger("Tier", 2);

        DroneEnergyStorage decoded = DroneEnergyStorage.readFromNbt(tag, 100L, 1);

        assertEquals(500L, decoded.getCapacity());
        assertEquals(500L, decoded.getStored());
        assertEquals(2, decoded.getTier());
    }
}
