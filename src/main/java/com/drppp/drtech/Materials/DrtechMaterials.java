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
public static Material ColdSunSalt;
public static Material HotSunSalt;
    public static void init()
    {
        ColdSunSalt = new Material.Builder(30000,gregtechId("cold_sun_salt")).fluid().color(0x685cff).build();
        HotSunSalt = new Material.Builder(30001,gregtechId("hot_sun_salt")).fluid().color(0xed8bb1).build();
    }
}
