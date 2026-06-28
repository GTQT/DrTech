package com.drppp.drtech.api.unification.Materials;

import gregtech.api.unification.material.Material;

import static gregtech.api.util.GTUtility.gregtechId;

public class DrtechMaterials {
    public static Material ColdSunSalt;
    public static Material HotSunSalt;
    public static Material CoolantLiquid;
    public static Material HotCoolantLiquid;
    public static Material Chorus;
    public static Material Demulsifier;
    public static Material HighEnergyDemulsifier;
    public static Material HighEnergyChorusSolution;
    public static Material EnderPrecipitate;
    public static Material EnderExtractant;
    public static Material ExtractedChorusSolvent;
    public static Material ChorusWaste;
    public static Material EnderEnergyFactor;
    public static Material HighEnergyDiesel;
    public static Material HighEnergyGasoline;

    public static void init()
    {
        ColdSunSalt = new Material.Builder(30000,gregtechId("cold_sun_salt")).fluid().color(0x685cff).build();
        HotSunSalt = new Material.Builder(30001,gregtechId("hot_sun_salt")).fluid().color(0xed8bb1).build();
        CoolantLiquid = new Material.Builder(30002,gregtechId("coolant_liquid")).fluid().color(0x145565).build();
        HotCoolantLiquid = new Material.Builder(30003,gregtechId("hot_coolant_liquid")).fluid().color(0xEA3609).build();
        Chorus = new Material.Builder(30004,gregtechId("chorus")).dust().fluid().color(0xC66DC5).build();
        Demulsifier = new Material.Builder(30005, gregtechId("demulsifier")).fluid().color(0x8FA6AE).build();
        HighEnergyDemulsifier = new Material.Builder(30006, gregtechId("high_energy_demulsifier")).fluid().color(0xB75AF4).build();
        HighEnergyChorusSolution = new Material.Builder(30007, gregtechId("high_energy_chorus_solution")).fluid().color(0xB546C9).build();
        EnderPrecipitate = new Material.Builder(30008, gregtechId("ender_precipitate")).dust().color(0x2A6E65).build();
        EnderExtractant = new Material.Builder(30009, gregtechId("ender_extractant")).fluid().color(0x40BFA6).build();
        ExtractedChorusSolvent = new Material.Builder(30010, gregtechId("extracted_chorus_solvent")).fluid().color(0x7A45C7).build();
        ChorusWaste = new Material.Builder(30011, gregtechId("chorus_waste")).fluid().color(0x4E3559).build();
        EnderEnergyFactor = new Material.Builder(30012, gregtechId("ender_energy_factor")).fluid().color(0x26F0B3).build();
        HighEnergyDiesel = new Material.Builder(30013, gregtechId("high_energy_diesel")).fluid().color(0xD7A32B).build();
        HighEnergyGasoline = new Material.Builder(30014, gregtechId("high_energy_gasoline")).fluid().color(0xD98942).build();
    }
}
