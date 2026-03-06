package com.brachy84.mechtech.api.armor.modules;

import com.brachy84.mechtech.api.armor.IModule;
import com.brachy84.mechtech.api.armor.Modules;
import com.google.common.collect.Lists;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.input.KeyBind;
import gregtech.common.items.MetaItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Collection;

public class AdvancedJetpack extends JetpackModule {

    @Override
    public int getEnergyPerUse() {
        return 512;
    }

    @Override
    public Collection<IModule> getIncompatibleModules() {
        return Lists.newArrayList(Modules.JETPACK);
    }

    @Override
    public double getSprintEnergyModifier() {
        return 4.0;
    }

    @Override
    public double getSprintSpeedModifier() {
        return 1.8;
    }

    @Override
    public double getVerticalHoverSpeed() {
        return 0.4;
    }

    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.005;
    }

    @Override
    public double getVerticalAcceleration() {
        return 0.14;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.8;
    }

    @Override
    public double getSidewaysSpeed() {
        return 0.19;
    }

    @Override
    public EnumParticleTypes getParticle() {
        return EnumParticleTypes.CLOUD;
    }

    @Override
    public float getFallDamageReduction() {
        return 3.5f;
    }

    @Override
    public String getModuleId() {
        return "advanced_jetpack";
    }

    @Override
    public MetaItem<?>.MetaValueItem getMetaValueItem() {
        return MetaItems.ELECTRIC_JETPACK_ADVANCED;
    }
}
