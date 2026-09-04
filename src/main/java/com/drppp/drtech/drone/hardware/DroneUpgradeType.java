package com.drppp.drtech.drone.hardware;

import com.drppp.drtech.Tags;
import net.minecraft.util.ResourceLocation;

/** One dedicated slot exists for every upgrade type. Explicit metadata and ids are stable persistence keys. */
public enum DroneUpgradeType {
    BATTERY(0, "battery"),
    PROPULSION(1, "propulsion"),
    EFFICIENCY(2, "efficiency"),
    CARGO(3, "cargo"),
    WIRELESS(4, "wireless"),
    FLUID_CARGO(5, "fluid_cargo"),
    CRAFTING(6, "crafting"),
    ADVANCED_NAVIGATION(7, "advanced_navigation"),
    EU_INTERFACE(8, "eu_interface"),
    TOOL_ARM(9, "tool_arm"),
    ENTITY_SCANNER(10, "entity_scanner"),
    COMBAT(11, "combat"),
    ENTITY_CONTAINMENT(12, "entity_containment"),
    WATERPROOF(13, "waterproof"),
    SELF_REPAIR(14, "self_repair"),
    SECURE_ACCESS(15, "secure_access"),
    ADVANCED_ITEM_HANDLING(16, "advanced_item_handling"),
    FLEET_COMMUNICATION(17, "fleet_communication"),
    FISHING(18, "fishing"),
    THAUMCRAFT_ALCHEMY(19, "thaumcraft_alchemy");

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
