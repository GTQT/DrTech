package com.brachy84.mechtech.api.armor;

import com.brachy84.mechtech.api.armor.modules.MaterialArmorModule;
import com.brachy84.mechtech.integration.crafttweaker.IArmorModule;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenMethod;

public class MaterialArmorModuleBuilder implements IArmorModule {

    public final int id;
    public final Material material;
    public double armor, toughness;
    public int durability = 0;
    public ISpecialArmorModule specialArmorModule;
    private MaterialArmorModule module;
    public boolean doGenerateRecipe = true;

    public MaterialArmorModuleBuilder(int id, Material material) {
        this.id = id;
        this.material = material;
        this.armor = 3.75;
        this.toughness = 0;
    }

    public ItemStack getItemStack() {
        if (module == null)
            return ItemStack.EMPTY;
        MetaItem<?>.MetaValueItem metaValueItem = module.getMetaValueItem();
        if (metaValueItem == null)
            return ItemStack.EMPTY;
        return metaValueItem.getStackForm();
    }

    public MaterialArmorModuleBuilder armor(double armor) {
        this.armor = armor;
        return this;
    }

    public MaterialArmorModuleBuilder toughness(double toughness) {
        this.toughness = toughness;
        return this;
    }

    public MaterialArmorModuleBuilder armor(double armor, double toughness) {
        this.armor = armor;
        this.toughness = toughness;
        return this;
    }

    public MaterialArmorModuleBuilder durability(int durability) {
        this.durability = durability;
        return this;
    }

    @Override
    public MaterialArmorModuleBuilder armor(double armor, @Optional double toughness, @Optional int durability) {
        if (armor >= 0) {
            this.armor = armor;
        }
        if (toughness >= 0) {
            this.toughness = toughness;
        }
        if (durability >= 0 || this.durability <= 0) {
            this.durability = durability;
        }
        return this;
    }

    public MaterialArmorModuleBuilder specialArmor(ISpecialArmorModule specialArmorModule) {
        this.specialArmorModule = specialArmorModule;
        return this;
    }

    public MaterialArmorModuleBuilder specialArmor(DamageSource source, double absorbtion, int maxAbsorbtion) {
        this.specialArmorModule = ((entity, modularArmorPiece, moduleData, source1, damage, slot) -> {
            if (source == source1)
                return new AbsorbResult(absorbtion, maxAbsorbtion);
            return AbsorbResult.ZERO;
        });
        return this;
    }

    @Override
    public MaterialArmorModuleBuilder dontGenerateRecipe() {
        this.doGenerateRecipe = false;
        return this;
    }

    @Override
    public double getArmor() {
        return armor;
    }

    @Override
    public double getToughness() {
        return toughness;
    }

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public void setArmor(double armor) {
        this.armor = armor;
    }

    @Override
    public void setToughness(double toughness) {
        this.toughness = toughness;
    }

    @Override
    public void setDurability(int durability) {
        this.durability = durability;
    }

    @ZenMethod
    public void registerModule() {
        armor = Math.max(armor, 0);
        toughness = Math.max(toughness, 0);
        if (durability <= 0) {
            if (material.hasProperty(PropertyKey.TOOL)) {
                durability = material.getProperty(PropertyKey.TOOL).getToolDurability();
            } else {
                durability = 128;
            }
        }
        this.module = new MaterialArmorModule(material, armor, toughness, durability, specialArmorModule);
        Modules.registerModule(id, module);
    }

    public MaterialArmorModule getModule() {
        return module;
    }

    public boolean isRegistered() {
        return module != null && Modules.getModule(id) == module;
    }
}
