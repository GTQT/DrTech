package com.drppp.drtech.Materials;

import com.drppp.drtech.Utils.DrtechUtils;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.ToolProperty;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.EXT2_METAL;
import static gregtech.api.unification.material.Materials.EXT_METAL;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FRAME;
import static gregtech.api.unification.material.info.MaterialIconSet.METALLIC;
import static gregtech.api.util.GTUtility.gregtechId;

public class DrtechMaterials {
public static Material BedRock;
    public static void init()
    {
        BedRock = new Material.Builder(30000, gregtechId("bedrock"))
                .ingot(6)
                .color(0x020806)
                .iconSet(METALLIC)
                .element(DrtechElements.Brk)
                .liquid(new FluidBuilder().temperature(110_000))
                .flags(EXT_METAL, GENERATE_BOLT_SCREW, GENERATE_FRAME, GENERATE_GEAR, GENERATE_LONG_ROD,
                        GENERATE_DOUBLE_PLATE)
                .toolStats(ToolProperty.Builder.of(260.0F, 200.0F, 65535, 6)
                        .attackSpeed(0.8F).enchantability(33).magnetic().unbreakable().build())
                .rotorStats(28.0f, 14.0f, 6553600)
                .cableProperties(V[MAX], 64, 0)
                .fluidPipeProperties(110_000, 8000, true, true, true, true)
                .blastTemp(12800, BlastProperty.GasTier.HIGHEST, VA[UEV])

                .build();
    }
}
