package com.drppp.drtech.api.unification.Materials;

import gregtech.api.unification.material.Material;

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
