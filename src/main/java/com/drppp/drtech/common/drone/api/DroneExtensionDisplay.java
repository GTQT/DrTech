package com.drppp.drtech.common.drone.api;

import net.minecraft.util.ResourceLocation;

import java.util.Objects;

/** Server-safe client presentation metadata; it contains keys and ids, never client classes. */
public final class DroneExtensionDisplay {
    private final ResourceLocation extensionId;
    private final String titleKey;
    private final String descriptionKey;
    private final ResourceLocation icon;

    public DroneExtensionDisplay(ResourceLocation extensionId, String titleKey, String descriptionKey,
            ResourceLocation icon) {
        this.extensionId = Objects.requireNonNull(extensionId, "Extension id is required");
        if (extensionId.toString().length() > 128) throw new IllegalArgumentException("Extension id is too long");
        this.titleKey = checkedKey(titleKey, "Title key");
        this.descriptionKey = checkedKey(descriptionKey, "Description key");
        this.icon = Objects.requireNonNull(icon, "Icon id is required");
        if (icon.toString().length() > 128) throw new IllegalArgumentException("Icon id is too long");
    }

    public ResourceLocation getExtensionId() { return extensionId; }
    public String getTitleKey() { return titleKey; }
    public String getDescriptionKey() { return descriptionKey; }
    public ResourceLocation getIcon() { return icon; }

    private static String checkedKey(String value, String label) {
        String checked = value == null ? "" : value.trim();
        if (checked.isEmpty() || checked.length() > 128) throw new IllegalArgumentException(label + " is invalid");
        return checked;
    }
}
