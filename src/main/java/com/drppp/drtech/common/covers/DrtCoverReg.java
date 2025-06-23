package com.drppp.drtech.common.covers;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.items.behavior.CoverItemBehavior;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.util.ResourceLocation;

import static gregtech.api.GTValues.OpV;

public class DrtCoverReg {

    public static void init()
    {


    }

    @SuppressWarnings("rawtypes")
    public static void registerBehavior(ResourceLocation coverId,
                                        MetaItem.MetaValueItem placerItem,
                                        CoverDefinition.CoverCreator behaviorCreator) {
        CoverDefinition coverDefinition = gregtech.common.covers.CoverBehaviors.registerCover(coverId, placerItem.getStackForm(), behaviorCreator);
        placerItem.addComponents(new CoverItemBehavior(coverDefinition));
    }
}

