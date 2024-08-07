package com.drppp.drtech.Client;

import com.cleanroommc.modularui.drawable.UITexture;
import com.drppp.drtech.Tags;

public interface ModularUiTextures {
    UITexture BEE_DRONE_ICON = UITexture.builder().location(Tags.MODID, "gui/bee_drone").imageSize(18, 18).build();
    UITexture BEE_QUEEN_ICON = UITexture.builder().location(Tags.MODID, "gui/bee_queen").imageSize(18, 18).build();
    UITexture CHECK_MARK = UITexture.builder().location(Tags.MODID, "gui/checkmark").imageSize(16, 16).build();
    UITexture CROSS = UITexture.builder().location(Tags.MODID, "gui/cross").imageSize(16, 16).build();
    UITexture INFORMATION = UITexture.builder().location("modularui", "gui/widgets/information").imageSize(16, 16).build();
}
