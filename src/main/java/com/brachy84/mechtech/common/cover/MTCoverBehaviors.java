package com.brachy84.mechtech.common.cover;

import com.brachy84.mechtech.MechTech;
import com.brachy84.mechtech.common.items.MTMetaItems;
import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.items.behavior.CoverItemBehavior;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class MTCoverBehaviors {

    public static void init() {
        registerBehavior(200, new ResourceLocation(MechTech.MODID, "wireless_receiver"), MTMetaItems.WIRELESS_RECEIVER, CoverWirelessReceiver::new);
    }

    public static void registerBehavior(int coverNetworkId, @NotNull ResourceLocation coverId, @NotNull MetaItem<?>.MetaValueItem placerItem, @NotNull CoverDefinition.CoverCreator behaviorCreator) {
        CoverDefinition coverDefinition = new CoverDefinition(coverId, behaviorCreator, placerItem.getStackForm());
        GregTechAPI.COVER_REGISTRY.register(coverNetworkId, coverId, coverDefinition);
        placerItem.addComponents(new CoverItemBehavior(coverDefinition));
    }
}
