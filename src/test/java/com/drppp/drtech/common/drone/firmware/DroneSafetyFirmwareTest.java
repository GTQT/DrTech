package com.drppp.drtech.common.drone.firmware;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneSafetyFirmwareTest {

    @Test
    void lowEnergyReturnChargingAndRecoveryPreemptTheProgram() {
        DroneSafetyFirmware firmware = new DroneSafetyFirmware();

        assertEquals(DroneSafetyState.RETURNING, firmware.evaluate(20, true, true, false));
        assertEquals(DroneSafetyState.CHARGING, firmware.evaluate(18, true, true, true));
        assertEquals(DroneSafetyState.RECOVERING, firmware.evaluate(90, true, true, true));
        firmware.finishRecovery();
        assertEquals(DroneSafetyState.PROGRAM, firmware.getState());
    }

    @Test
    void missingDockFallsBackWithoutDiscardingResumeIntent() {
        DroneSafetyFirmware firmware = new DroneSafetyFirmware();
        firmware.beginReturn(true);

        assertEquals(DroneSafetyState.SAFE_IDLE, firmware.evaluate(10, true, false, false));
        assertTrue(firmware.shouldResumeProgram());
        firmware.retryBoundDock();
        assertEquals(DroneSafetyState.RETURNING, firmware.getState());
    }

    @Test
    void thresholdsAndStateSurviveNbtWithSafeBounds() {
        DroneSafetyFirmware source = new DroneSafetyFirmware();
        source.setThresholds(30, 80);
        source.beginReturn(true);

        NBTTagCompound saved = source.writeToNbt();
        DroneSafetyFirmware restored = new DroneSafetyFirmware();
        restored.readFromNbt(saved);

        assertEquals(30, restored.getReturnAtPercent());
        assertEquals(80, restored.getResumeAtPercent());
        assertEquals(DroneSafetyState.RETURNING, restored.getState());
        assertTrue(restored.shouldResumeProgram());
    }

    @Test
    void exhaustedDroneCannotRemainInADeadReturningLoop() {
        DroneSafetyFirmware firmware = new DroneSafetyFirmware();
        firmware.beginReturn(true);

        assertEquals(DroneSafetyState.EMERGENCY_LAND,
                firmware.evaluate(0, true, true, false));
    }

    @Test
    void lowEnergyWithoutAUsableDockDoesNotPreemptAProgramThatMayBindOne() {
        DroneSafetyFirmware firmware = new DroneSafetyFirmware();

        assertEquals(false, firmware.shouldPreemptProgram(13, false));
        assertEquals(true, firmware.shouldPreemptProgram(13, true));
        assertEquals(true, firmware.shouldPreemptProgram(1, false));
    }

    @Test
    void safeIdleWithoutAServiceableDockRecoversInsteadOfBrickingTheProgram() {
        DroneSafetyFirmware firmware = new DroneSafetyFirmware();
        firmware.beginReturn(true);
        assertEquals(DroneSafetyState.SAFE_IDLE, firmware.evaluate(13, false, false, false));

        assertEquals(DroneSafetyState.RECOVERING, firmware.evaluate(13, false, false, false));
        assertTrue(firmware.shouldResumeProgram());
    }

    @Test
    void dockLaunchClearsPersistedReturnOrChargingState() {
        DroneSafetyFirmware firmware = new DroneSafetyFirmware();
        firmware.beginReturn(true);
        assertEquals(DroneSafetyState.CHARGING, firmware.evaluate(15, true, true, true));

        firmware.prepareForLaunch();

        assertEquals(DroneSafetyState.PROGRAM, firmware.getState());
        assertEquals(false, firmware.shouldResumeProgram());
    }
}
