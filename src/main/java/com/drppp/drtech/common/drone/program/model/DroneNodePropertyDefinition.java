package com.drppp.drtech.common.drone.program.model;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Declarative, bounded node configuration field. No client widget classes are referenced here. */
public final class DroneNodePropertyDefinition {

    private final String id;
    private final DroneNodePropertyType type;
    private final boolean required;
    private final double minimum;
    private final double maximum;
    private final int maxLength;
    private final Set<String> allowedValues;

    private DroneNodePropertyDefinition(String id, DroneNodePropertyType type, boolean required,
            double minimum, double maximum, int maxLength, Set<String> allowedValues) {
        if (id == null || id.isEmpty() || id.length() > 64) throw new IllegalArgumentException("Invalid property id");
        this.id = id;
        this.type = Objects.requireNonNull(type, "type");
        this.required = required;
        this.minimum = minimum;
        this.maximum = maximum;
        this.maxLength = maxLength;
        this.allowedValues = Collections.unmodifiableSet(new LinkedHashSet<>(allowedValues));
    }

    public static DroneNodePropertyDefinition integer(String id, int minimum, int maximum) {
        return new DroneNodePropertyDefinition(id, DroneNodePropertyType.INTEGER, false,
                minimum, maximum, 0, Collections.emptySet());
    }

    public static DroneNodePropertyDefinition number(String id, double minimum, double maximum) {
        return new DroneNodePropertyDefinition(id, DroneNodePropertyType.NUMBER, false,
                minimum, maximum, 0, Collections.emptySet());
    }

    public static DroneNodePropertyDefinition bool(String id) {
        return new DroneNodePropertyDefinition(id, DroneNodePropertyType.BOOLEAN, false,
                0, 1, 0, Collections.emptySet());
    }

    public static DroneNodePropertyDefinition string(String id, int maxLength) {
        return new DroneNodePropertyDefinition(id, DroneNodePropertyType.STRING, false,
                0, 0, maxLength, Collections.emptySet());
    }

    public static DroneNodePropertyDefinition enumeration(String id, String... values) {
        return new DroneNodePropertyDefinition(id, DroneNodePropertyType.ENUM, false,
                0, 0, 64, new LinkedHashSet<>(Arrays.asList(values)));
    }

    public static DroneNodePropertyDefinition selector(String id, DroneNodePropertyType type) {
        if (type != DroneNodePropertyType.ITEM_SELECTOR && type != DroneNodePropertyType.FLUID_SELECTOR
                && type != DroneNodePropertyType.BLOCK_SELECTOR && type != DroneNodePropertyType.ENTITY_SELECTOR
                && type != DroneNodePropertyType.COORDINATE
                && type != DroneNodePropertyType.AREA && type != DroneNodePropertyType.PROGRAM_REFERENCE
                && type != DroneNodePropertyType.DOCK_REFERENCE) {
            throw new IllegalArgumentException("Property type is not a selector");
        }
        return new DroneNodePropertyDefinition(id, type, false, 0, 0,
                type == DroneNodePropertyType.FLUID_SELECTOR ? 128 : 0, Collections.emptySet());
    }

    public DroneNodePropertyDefinition required() {
        return new DroneNodePropertyDefinition(id, type, true, minimum, maximum, maxLength, allowedValues);
    }

    public String getId() { return id; }
    public DroneNodePropertyType getType() { return type; }
    public boolean isRequired() { return required; }
    public double getMinimum() { return minimum; }
    public double getMaximum() { return maximum; }
    public int getMaxLength() { return maxLength; }
    public Set<String> getAllowedValues() { return allowedValues; }

    /** Returns a stable diagnostic reason, or null when the configuration is valid. */
    @Nullable
    public String validate(NBTTagCompound configuration) {
        if (!configuration.hasKey(id)) return required ? "missing_property:" + id : null;
        switch (type) {
            case INTEGER:
            case NUMBER:
                if (!configuration.hasKey(id, 99)) return "invalid_property_type:" + id;
                if (type == DroneNodePropertyType.INTEGER && configuration.getTagId(id) > 4) {
                    return "invalid_property_type:" + id;
                }
                double value = configuration.getDouble(id);
                if (!Double.isFinite(value) || value < minimum || value > maximum) {
                    return "property_out_of_range:" + id;
                }
                return null;
            case BOOLEAN:
                return configuration.hasKey(id, 1) ? null : "invalid_property_type:" + id;
            case STRING:
                if (!configuration.hasKey(id, 8)) return "invalid_property_type:" + id;
                return configuration.getString(id).length() <= maxLength ? null : "property_too_long:" + id;
            case ENUM:
            case DIRECTION:
                if (!configuration.hasKey(id, 8)) return "invalid_property_type:" + id;
                return allowedValues.contains(configuration.getString(id)) ? null : "invalid_property_value:" + id;
            case FLUID_SELECTOR:
                if (!configuration.hasKey(id, 8)) return "invalid_property_type:" + id;
                return configuration.getString(id).length() <= maxLength ? null : "property_too_long:" + id;
            case PROGRAM_REFERENCE: {
                if (!configuration.hasKey(id, 10)) return "invalid_property_type:" + id;
                NBTTagCompound program = configuration.getCompoundTag(id);
                if (!validUuid(program, "ProgramId") || !program.hasKey("Revision", 99)
                        || program.getLong("Revision") < 0L) return "invalid_program_reference:" + id;
                return program.getString("Name").length() <= 64 ? null : "property_too_long:" + id;
            }
            case DOCK_REFERENCE: {
                if (!configuration.hasKey(id, 10)) return "invalid_property_type:" + id;
                NBTTagCompound dock = configuration.getCompoundTag(id);
                return validUuid(dock, "DockId") && dock.hasKey("Position", 4)
                        && dock.hasKey("Dimension", 99) ? null : "invalid_dock_reference:" + id;
            }
            default:
                return configuration.hasKey(id, 10) ? null : "invalid_property_type:" + id;
        }
    }

    private static boolean validUuid(NBTTagCompound compound, String key) {
        if (!compound.hasKey(key, 8)) return false;
        try {
            java.util.UUID.fromString(compound.getString(key));
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
