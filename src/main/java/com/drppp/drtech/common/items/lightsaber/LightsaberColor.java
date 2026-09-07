package com.drppp.drtech.common.Items.lightsaber;

public enum LightsaberColor {
    DEEP_BLUE(0, "deep_blue", 0x0000FF),
    MEDIUM_BLUE(1, "medium_blue", 0x006BFF),
    LIGHT_BLUE(2, "light_blue", 0x59B9FF),
    ARCTIC_BLUE(3, "arctic_blue", 0xDDF6FF),
    WHITE(4, "white", 0xFFFFFF),
    INDIGO(5, "indigo", 0x5D00FF),
    PURPLE(6, "purple", 0xAD00AD),
    MAGENTA(7, "magenta", 0xFF00FF),
    PINK(8, "pink", 0xFF8FBA),
    RED(9, "red", 0xFF0000),
    BLOOD_ORANGE(10, "blood_orange", 0xFF8000),
    AMBER(11, "amber", 0xFFB600),
    YELLOW(12, "yellow", 0xFFFF00),
    GOLD(13, "gold", 0xFFFF3A),
    LIME_GREEN(14, "lime_green", 0xBFFF00),
    GREEN(15, "green", 0x00FF00),
    MINT_GREEN(16, "mint_green", 0x00FF9B),
    CYAN(17, "cyan", 0x00FFFF);

    private static final LightsaberColor[] VALUES = values();

    private final int metadata;
    private final String serializedName;
    private final int packedRgb;
    private final float[] rgb;
    private final float glowIntensity;

    LightsaberColor(int metadata, String serializedName, int color) {
        this.metadata = metadata;
        this.serializedName = serializedName;
        this.packedRgb = color;
        this.rgb = new float[] {
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        };
        this.glowIntensity = (float) (1.0D / Math.sqrt(
                rgb[0] * rgb[0] + rgb[1] * rgb[1] + rgb[2] * rgb[2]));
    }

    public int getMetadata() {
        return metadata;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public float[] getRgb() {
        return rgb;
    }

    public int getPackedRgb() {
        return packedRgb;
    }

    public float getGlowIntensity() {
        return glowIntensity;
    }

    public static LightsaberColor byMetadata(int metadata) {
        return VALUES[Math.floorMod(metadata, VALUES.length)];
    }
}
