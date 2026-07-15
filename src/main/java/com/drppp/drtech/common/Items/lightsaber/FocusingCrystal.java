package com.drppp.drtech.common.Items.lightsaber;

public enum FocusingCrystal {
    COMPRESSED("compressed"),
    CRACKED("cracked"),
    INVERTING("inverting"),
    FINE_CUT("fine_cut"),
    PRISMATIC("prismatic");

    private static final FocusingCrystal[] VALUES = values();
    private final String serializedName;

    FocusingCrystal(String serializedName) {
        this.serializedName = serializedName;
    }

    public int getMetadata() {
        return ordinal();
    }

    public int getMask() {
        return 1 << ordinal();
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static FocusingCrystal byMetadata(int metadata) {
        return metadata >= 0 && metadata < VALUES.length ? VALUES[metadata] : COMPRESSED;
    }
}
