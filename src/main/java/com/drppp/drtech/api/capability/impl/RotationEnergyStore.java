package com.drppp.drtech.api.capability.impl;

import com.drppp.drtech.api.capability.IRotationEnergy;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

public class RotationEnergyStore implements Capability.IStorage<IRotationEnergy>{

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IRotationEnergy> capability, IRotationEnergy instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Ru",instance.getEnergyOutput());
        tag.setBoolean("IsOut",instance.isOutPut());
        return tag ;
    }
    @Override
    public void readNBT(Capability<IRotationEnergy> capability, IRotationEnergy instance, EnumFacing side, NBTBase nbt) {
        NBTTagCompound tag =  (NBTTagCompound)nbt;
        instance.setRuEnergy(tag.getInteger("Ru"));
        instance.setOutPut(tag.getBoolean("IsOut"));
    }
}
