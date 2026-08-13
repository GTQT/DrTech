package com.drppp.drtech.common.drone.hardware;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DroneUpgradeDataCodecTest {

    @Test
    void migratesLegacyItemHandlerSlotsToStableIds() {
        NBTTagCompound legacy = new NBTTagCompound();
        legacy.setInteger("Size", 5);
        NBTTagList items = new NBTTagList();
        NBTTagCompound battery = new NBTTagCompound();
        battery.setByte("Slot", (byte) DroneUpgradeType.BATTERY.getMetadata());
        battery.setShort("Damage", (short) DroneUpgradeType.BATTERY.getMetadata());
        items.appendTag(battery);
        NBTTagCompound wireless = new NBTTagCompound();
        wireless.setByte("Slot", (byte) DroneUpgradeType.WIRELESS.getMetadata());
        wireless.setShort("Damage", (short) DroneUpgradeType.WIRELESS.getMetadata());
        items.appendTag(wireless);
        legacy.setTag("Items", items);

        NBTTagCompound migrated = DroneUpgradeDataCodec.migrate(legacy);

        assertEquals(DroneUpgradeDataCodec.CURRENT_VERSION, migrated.getInteger("Version"));
        assertEquals(1, DroneUpgradeDataCodec.getLevel(migrated, DroneUpgradeType.BATTERY));
        assertEquals(1, DroneUpgradeDataCodec.getLevel(migrated, DroneUpgradeType.WIRELESS));
        assertEquals(0, DroneUpgradeDataCodec.getLevel(migrated, DroneUpgradeType.CARGO));
    }

    @Test
    void ignoresUnknownStableUpgradeIdsAndClampsLevels() {
        NBTTagCompound stable = new NBTTagCompound();
        stable.setInteger("Version", DroneUpgradeDataCodec.CURRENT_VERSION);
        NBTTagList entries = new NBTTagList();
        NBTTagCompound known = new NBTTagCompound();
        known.setString("Id", DroneUpgradeType.CARGO.getId().toString());
        known.setInteger("Level", 99);
        entries.appendTag(known);
        NBTTagCompound unknown = new NBTTagCompound();
        unknown.setString("Id", "missing_mod:oversized_hold");
        unknown.setInteger("Level", 1);
        entries.appendTag(unknown);
        stable.setTag("Entries", entries);

        NBTTagCompound sanitized = DroneUpgradeDataCodec.migrate(stable);

        assertEquals(1, sanitized.getTagList("Entries", 10).tagCount());
        assertEquals(1, DroneUpgradeDataCodec.getLevel(sanitized, DroneUpgradeType.CARGO));
    }
}
