package com.drppp.drtech.loaders.chain;

import gregtech.api.items.metaitem.MetaItem;

import static com.drppp.drtech.common.MetaTileEntities.MetaTileEntities.*;
import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.items.MetaItems.ELECTRIC_PUMP_UV;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public class LaserBending {
    public static void init() {
        MetaItem.MetaValueItem[] emitters = {EMITTER_IV,EMITTER_LuV,EMITTER_ZPM,EMITTER_UV,EMITTER_UHV,EMITTER_UEV,EMITTER_UIV,EMITTER_UXV,EMITTER_OpV};
        MetaItem.MetaValueItem[] sendors = {SENSOR_IV,SENSOR_LuV,SENSOR_ZPM,SENSOR_UV,SENSOR_UHV,SENSOR_UEV,SENSOR_UIV,SENSOR_UXV,SENSOR_OpV};
        MetaItem.MetaValueItem[] pumps = {ELECTRIC_PUMP_IV,ELECTRIC_PUMP_LuV,ELECTRIC_PUMP_ZPM,ELECTRIC_PUMP_UV,ELECTRIC_PUMP_UHV,ELECTRIC_PUMP_UEV,ELECTRIC_PUMP_UIV,ELECTRIC_PUMP_UXV,ELECTRIC_PUMP_OpV};

        for (int i = 0; i < 9; i++)
        {
            if(HULL[IV+i]==null)return;
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[IV+i])
                    .input(lens, Diamond)
                    .input(emitters[i])
                    .input(sendors[i])
                    .input(pumps[i])
                    .input(cableGtSingle, Platinum, 4)
                    .circuitMeta(1)
                    .output(LASER_BENDING_256[i])
                    .duration(300).EUt(VA[IV]).buildAndRegister();
        }
        for (int i = 0; i < 9; i++)
        {
            if(HULL[IV+i]==null)return;
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[IV+i])
                    .input(lens, Diamond)
                    .input(emitters[i],2)
                    .input(sendors[i],2)
                    .input(pumps[i],2)
                    .input(cableGtDouble, Platinum, 4)
                    .circuitMeta(1)
                    .output(LASER_BENDING_1024[i])
                    .duration(300).EUt(VA[LuV]).buildAndRegister();
        }
        for (int i = 0; i < 9; i++)
        {
            if(HULL[IV+i]==null)return;
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[IV+i])
                    .input(lens, Diamond)
                    .input(emitters[i],4)
                    .input(sendors[i],4)
                    .input(pumps[i],4)
                    .input(cableGtQuadruple, Platinum, 4)
                    .circuitMeta(1)
                    .output(LASER_BENDING_4096[i])
                    .duration(300).EUt(VA[ZPM]).buildAndRegister();
        }
        for (int i = 0; i < 9; i++)
        {
            if(HULL[IV+i]==null)return;
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[IV+i])
                    .input(lens, Diamond)
                    .input(emitters[i],8)
                    .input(sendors[i],8)
                    .input(pumps[i],8)
                    .input(cableGtOctal, Platinum, 4)
                    .circuitMeta(1)
                    .output(LASER_BENDING_16384[i])
                    .duration(300).EUt(VA[UV]).buildAndRegister();
        }
        for (int i = 0; i < 9; i++)
        {
            if(HULL[IV+i]==null)return;
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[IV+i])
                    .input(lens, Diamond)
                    .input(emitters[i],16)
                    .input(sendors[i],16)
                    .input(pumps[i],16)
                    .input(cableGtHex, Platinum, 4)
                    .circuitMeta(1)
                    .output(LASER_BENDING_65536[i])
                    .duration(300).EUt(VA[UHV]).buildAndRegister();
        }
    }
}
