package com.brachy84.mechtech.integration.crafttweaker;

import com.brachy84.mechtech.api.ToroidBlock;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlock;
import crafttweaker.api.block.IBlockState;
import gregtech.api.unification.material.Material;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenSetter;

@ZenClass("mods.mechtech.ToroidBlock")
@ZenRegister
public interface IToroidBlock {

    @ZenMethod
    static IToroidBlock get(String name) {
        return ToroidBlock.get(name);
    }

    @ZenMethod
    static void remove(String name) {
        ToroidBlock.remove(name);
    }

    @ZenMethod
    static IToroidBlock create(String name, IBlockState state) {
        return ToroidBlock.create(name, state);
    }

    @ZenMethod
    static IToroidBlock create(String name, IBlock block) {
        return ToroidBlock.create(name, block);
    }

    @ZenMethod
    static IToroidBlock create(Material material) {
        return ToroidBlock.create(material);
    }

    @ZenMethod
    static IToroidBlock create(String name, Material material) {
        return ToroidBlock.create(name, material);
    }

    @ZenMethod
    default IToroidBlock setDmgModifier(float dmgModifier) {
        setDmgModifierCT(dmgModifier);
        return this;
    }

    @ZenSetter("dmgModifier")
    void setDmgModifierCT(float dmgModifier);

    @ZenMethod
    default IToroidBlock setRangeModifier(float rangeModifier) {
        setRangeModifierCT(rangeModifier);
        return this;
    }

    @ZenSetter("rangeModifier")
    void setRangeModifierCT(float rangeModifier);

    @ZenMethod
    default IToroidBlock setAmpsPerBlock(float ampsPerBlock) {
        setAmpsPerBlockCT(ampsPerBlock);
        return this;
    }

    @ZenSetter("ampsPerBlock")
    void setAmpsPerBlockCT(float ampsPerBlock);

    @ZenMethod
    IToroidBlock register();

    @ZenGetter("dmgModifier")
    @ZenMethod
    float getDmgModifier();

    @ZenGetter("rangeModifier")
    @ZenMethod
    float getRangeModifier();

    @ZenGetter("ampsPerBlock")
    @ZenMethod
    float getAmpsPerBlock();

    @ZenGetter("name")
    @ZenMethod
    String getName();
}
