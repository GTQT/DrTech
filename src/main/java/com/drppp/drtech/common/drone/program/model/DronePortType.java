package com.drppp.drtech.common.drone.program.model;

/**
 * Types shared by the visual editor and the server-side compiler.
 *
 * <p>FLOW is deliberately not compatible with ANY_DATA. This prevents a malformed visual connection from becoming
 * an execution edge after deserialization.</p>
 */
public enum DronePortType {
    FLOW,
    BOOLEAN,
    NUMBER,
    STRING,
    COORDINATE,
    AREA,
    ITEM_FILTER,
    BLOCK_FILTER,
    FLUID_FILTER,
    ENTITY_FILTER,
    DIRECTION,
    DOCK_REFERENCE,
    PROGRAM_REFERENCE,
    ACTION_STATUS,
    ANY_DATA;

    public boolean accepts(DronePortType sourceType) {
        if (this == sourceType) {
            return true;
        }
        return this == ANY_DATA && sourceType != FLOW;
    }
}
