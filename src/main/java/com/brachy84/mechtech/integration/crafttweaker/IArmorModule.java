package com.brachy84.mechtech.integration.crafttweaker;

import com.brachy84.mechtech.api.armor.Modules;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.material.Material;
import stanhebben.zenscript.annotations.*;

@ZenClass("mods.mechtech.MaterialArmorModule")
@ZenRegister
public interface IArmorModule {

    @ZenMethod
    static IArmorModule createBuilder(int id, Material material) {
        return Modules.materialArmorBuilder(id, material);
    }

    @ZenMethod
    IArmorModule armor(double armor, @Optional(valueDouble = -1) double toughness, @Optional(valueLong = -1) int durability);

    @ZenMethod
    IArmorModule dontGenerateRecipe();

    @ZenGetter("armor")
    @ZenMethod
    double getArmor();

    @ZenGetter("toughness")
    @ZenMethod
    double getToughness();

    @ZenGetter("durability")
    @ZenMethod
    int getDurability();

    @ZenSetter("armor")
    @ZenMethod
    void setArmor(double armor);

    @ZenSetter("toughness")
    @ZenMethod
    void setToughness(double toughness);

    @ZenSetter("durability")
    @ZenMethod
    void setDurability(int durability);

    @ZenMethod
    void registerModule();
}
