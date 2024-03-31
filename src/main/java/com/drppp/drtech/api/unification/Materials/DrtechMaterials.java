package com.drppp.drtech.api.unification.Materials;

import gregtech.api.unification.material.Material;

import static gregtech.api.util.GTUtility.gregtechId;

public class DrtechMaterials {
public static Material ColdSunSalt;
public static Material HotSunSalt;
public static Material CoolantLiquid;
public static Material HotCoolantLiquid;
public static Material Chorus;
    public static void init()
    {
        ColdSunSalt = new Material.Builder(30000,gregtechId("cold_sun_salt")).fluid().color(0x685cff).build();
        HotSunSalt = new Material.Builder(30001,gregtechId("hot_sun_salt")).fluid().color(0xed8bb1).build();
        CoolantLiquid = new Material.Builder(30002,gregtechId("coolant_liquid")).fluid().color(0x145565).build();
        HotCoolantLiquid = new Material.Builder(30003,gregtechId("hot_coolant_liquid")).fluid().color(0xEA3609).build();
        Chorus = new Material.Builder(30004,gregtechId("chorus")).dust().fluid().color(0xC66DC5).build();
    }
}
