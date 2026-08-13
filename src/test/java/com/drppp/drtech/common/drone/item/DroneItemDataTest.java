package com.drppp.drtech.common.drone.item;

import com.drppp.drtech.common.drone.hardware.DroneChassisTier;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DroneItemDataTest {

    @Test
    void inventoryPayloadIsCopiedAndNormalizedToHardwareMaximum() {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("Size", 27);
        payload.setString("Marker", "saved");

        NBTTagCompound loaded = DroneItemData.copyInventoryPayload(payload);
        payload.setString("Marker", "mutated");

        assertEquals(18, loaded.getInteger("Size"));
        assertEquals("saved", loaded.getString("Marker"));
        loaded.setString("Marker", "client mutation");
        assertFalse("client mutation".equals(DroneItemData.copyInventoryPayload(payload).getString("Marker")));
    }

    @Test
    void missingInventoryProducesAnEmptyMaximumCargoPayload() {
        assertEquals(18, DroneItemData.copyInventoryPayload(null).getInteger("Size"));
    }

    @Test
    void upgradePayloadIsCopiedAndMigratedToStableVersion() {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("Size", 99);
        payload.setString("Marker", "module");

        NBTTagCompound loaded = DroneItemData.copyUpgradesPayload(payload);
        payload.setString("Marker", "mutated");

        assertEquals(2, loaded.getInteger("Version"));
        assertEquals(0, loaded.getTagList("Entries", 10).tagCount());
    }

    @Test
    void runtimeSnapshotIsDefensivelyCopied() {
        NBTTagCompound runtime = new NBTTagCompound();
        runtime.setString("Status", "PAUSED");

        NBTTagCompound loaded = DroneItemData.copyRuntimePayload(runtime);
        loaded.setString("Status", "mutated");
        assertEquals("PAUSED", DroneItemData.copyRuntimePayload(runtime).getString("Status"));
    }

    @Test
    void safetyFirmwarePayloadIsDefensivelyCopied() {
        NBTTagCompound firmware = new NBTTagCompound();
        firmware.setInteger("ReturnAtPercent", 25);

        NBTTagCompound loaded = DroneItemData.copySafetyFirmwarePayload(firmware);
        loaded.setInteger("ReturnAtPercent", 80);

        assertEquals(25, DroneItemData.copySafetyFirmwarePayload(firmware).getInteger("ReturnAtPercent"));
    }

    @Test
    void fluidPayloadIsDefensivelyCopied() {
        NBTTagCompound fluid = new NBTTagCompound();
        fluid.setString("FluidName", "water");
        fluid.setInteger("Amount", 4_000);

        NBTTagCompound loaded = DroneItemData.copyFluidPayload(fluid);
        loaded.setInteger("Amount", 1);

        assertEquals(4_000, DroneItemData.copyFluidPayload(fluid).getInteger("Amount"));
        assertEquals(0, DroneItemData.copyFluidPayload(null).getInteger("Amount"));
    }

    @Test
    void migrationAssignsStableIdentityOnceAndPreservesTheOriginalOwner() {
        NBTTagCompound payload = new NBTTagCompound();
        UUID firstOwner = UUID.randomUUID();
        UUID secondOwner = UUID.randomUUID();

        DroneItemData.migratePayload(payload, DroneChassisTier.HV.getMetadata(), firstOwner);
        UUID droneId = UUID.fromString(payload.getString(DroneItemData.DRONE_ID_TAG));
        DroneItemData.migratePayload(payload, DroneChassisTier.IV.getMetadata(), secondOwner);

        assertNotNull(droneId);
        assertEquals(droneId, UUID.fromString(payload.getString(DroneItemData.DRONE_ID_TAG)));
        assertEquals(firstOwner, UUID.fromString(payload.getString(DroneItemData.OWNER_TAG)));
        assertEquals(DroneChassisTier.HV.getId().toString(), payload.getString(DroneItemData.CHASSIS_TAG));
        assertEquals(DroneItemData.CURRENT_DATA_VERSION,
                payload.getInteger(DroneItemData.DATA_VERSION_TAG));
    }
}
