package com.drppp.drtech.common.Items.lightsaber;

public enum LightsaberPartType {
    EMITTER("emitter"),
    SWITCH_SECTION("switch_section"),
    BODY("body"),
    POMMEL("pommel");

    private final String textureName;

    LightsaberPartType(String textureName) {
        this.textureName = textureName;
    }

    public String getTextureName() {
        return textureName;
    }
}
