package com.drppp.drtech.drone.program.codec;

import net.minecraft.nbt.NBTTagCompound;

/** Ordered, defensive migration entry point for every persisted visual program. */
public final class DroneProgramMigrator {

    public static final int EARLIEST_SUPPORTED_SCHEMA = 1;
    public static final int CURRENT_SCHEMA = 2;

    private DroneProgramMigrator() {}

    public static NBTTagCompound migrate(NBTTagCompound source) throws DroneProgramFormatException {
        if (source == null) {
            throw new DroneProgramFormatException("Program tag is missing");
        }
        NBTTagCompound migrated = source.copy();
        int schema = migrated.getInteger("Schema");
        if (schema < EARLIEST_SUPPORTED_SCHEMA || schema > CURRENT_SCHEMA) {
            throw new DroneProgramFormatException("Unsupported drone program schema " + schema);
        }
        while (schema < CURRENT_SCHEMA) {
            switch (schema) {
                case 1:
                    migrateOneToTwo(migrated);
                    schema = 2;
                    break;
                default:
                    throw new DroneProgramFormatException("No migration path from drone program schema " + schema);
            }
        }
        migrated.setInteger("Schema", CURRENT_SCHEMA);
        return migrated;
    }

    /** Schema 2 establishes an explicit migration boundary; the schema 1 graph representation remains valid. */
    private static void migrateOneToTwo(NBTTagCompound program) {
        program.setInteger("Schema", 2);
    }
}
