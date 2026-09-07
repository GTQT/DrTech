package com.drppp.drtech.common.Items.lightsaber;

public enum LightsaberHilt {
    GRAFLEX("graflex", "Graflex", LightsaberColor.DEEP_BLUE, 16.0F, 8.8F, 16.0F, 1.0F),
    REDEEMER("redeemer", "Redeemer", LightsaberColor.DEEP_BLUE, 30.0F, 8.0F, 12.3F, 1.0F),
    MAULER("mauler", "Mauler", LightsaberColor.RED, 18.0F, 12.0F, 21.6F, 0.25F),
    PRODIGAL_SON("prodigal_son", "Prodigal Son", LightsaberColor.GREEN, 31.0F, 8.4F, 13.3F, 2.0F),
    KNIGHTED("knighted", "Knighted", LightsaberColor.RED, 12.6F, 8.4F, 20.0F, 13.3F),
    VAID_ANCIENT("vaid_ancient", "Vaid (Ancient)", LightsaberColor.PURPLE, 18.5F, 9.2F, 22.37F, 6.6F),
    VAID_MODERN("vaid_modern", "Vaid (Modern)", LightsaberColor.PURPLE, 18.5F, 9.2F, 22.37F, 6.6F),
    DROIDEKA("droideka", "Droideka", LightsaberColor.AMBER, 5.0F, 9.6F, 29.4F, 6.5F),
    FULCRUM("fulcrum", "Fulcrum", LightsaberColor.WHITE, 19.0F, 10.0F, 30.0F, 7.0F),
    JUGGERNAUT("juggernaut", "Juggernaut", LightsaberColor.RED, 14.7F, 12.4F, 16.0F, 7.0F),
    MECHANICAL("mechanical", "Mechanical", LightsaberColor.RED, 16.0F, 8.8F, 16.0F, 2.5F),
    MANDALORIAN("mandalorian", "Mandalorian", LightsaberColor.WHITE, 12.55F, 2.87F, 26.0F, 7.45F),
    FURY("fury", "Fury", LightsaberColor.PURPLE, 19.0F, 5.6F, 16.0F, 8.3F),
    REBEL("rebel", "Rebel", LightsaberColor.MEDIUM_BLUE, 12.9F, 7.0F, 20.0F, 6.0F),
    IMPERIAL("imperial", "Imperial", LightsaberColor.RED, 7.6F, 3.7F, 15.8F, 6.5F),
    REBORN("reborn", "Reborn", LightsaberColor.PURPLE, 14.86F, 10.0F, 19.0F, 6.0F);

    private static final LightsaberHilt[] VALUES = values();
    private final String serializedName;
    private final String displayName;
    private final LightsaberColor defaultColor;
    private final float[] heights;

    LightsaberHilt(String serializedName, String displayName, LightsaberColor defaultColor, float emitter,
                   float switchSection, float body, float pommel) {
        this.serializedName = serializedName;
        this.displayName = displayName;
        this.defaultColor = defaultColor;
        this.heights = new float[] { emitter, switchSection, body, pommel };
    }

    public int getMetadata() {
        return ordinal();
    }

    public String getSerializedName() {
        return serializedName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LightsaberColor getDefaultColor() {
        return defaultColor;
    }

    public float getHeight(LightsaberPartType type) {
        return heights[type.ordinal()];
    }

    public String getModelSuffix() {
        return this == VAID_ANCIENT || this == VAID_MODERN ? "Vaid" : toModelSuffix(serializedName);
    }

    public String getTextureName(LightsaberPartType type) {
        return this == REBORN ? "reborn" : type.getTextureName() + "_" + serializedName;
    }

    public float[] getBodyInstructions() {
        return this == FULCRUM ? new float[] { 11.7F, 2.0F, 9.8F, 3.0F, 9.0F } : null;
    }

    public float[] getCrossguard() {
        return this == KNIGHTED ? new float[] { 0.0F, 0.083F, 0.23F } : null;
    }

    public static LightsaberHilt byMetadata(int metadata) {
        return metadata >= 0 && metadata < VALUES.length ? VALUES[metadata] : GRAFLEX;
    }

    private static String toModelSuffix(String value) {
        StringBuilder builder = new StringBuilder();
        boolean upper = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '_') {
                upper = true;
            } else {
                builder.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return builder.toString();
    }
}
