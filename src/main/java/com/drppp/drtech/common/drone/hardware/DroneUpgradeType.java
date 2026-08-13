package com.drppp.drtech.common.drone.hardware;

import com.drppp.drtech.Tags;
import net.minecraft.util.ResourceLocation;

/** One dedicated slot exists for every upgrade type. Explicit metadata and ids are stable persistence keys. */
public enum DroneUpgradeType {
    BATTERY(0, "battery"),
    PROPULSION(1, "propulsion"),
    EFFICIENCY(2, "efficiency"),
    CARGO(3, "cargo"),
    WIRELESS(4, "wireless"),
    FLUID_CARGO(5, "fluid_cargo");

    private final int metadata;
    private final String serializedName;
    private final ResourceLocation id;

    DroneUpgradeType(int metadata, String serializedName) {
        this.metadata = metadata;
        this.serializedName = serializedName;
        this.id = new ResourceLocation(Tags.MODID, serializedName);
    }

    public int getMetadata() { return metadata; }
    public String getSerializedName() { return serializedName; }
    public ResourceLocation getId() { return id; }

    public static DroneUpgradeType fromMetadata(int metadata) {
        DroneUpgradeType[] values = values();
        for (DroneUpgradeType type : values) {
            if (type.metadata == metadata) return type;
        }
        return BATTERY;
    }

    public static DroneUpgradeType fromId(ResourceLocation id) {
        if (id == null) return null;
        for (DroneUpgradeType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return null;
    }
}
