package com.drppp.drtech.hooked;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class HooksCapProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
    final HooksCap cap = new HooksCap();

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == HookCapability.CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == HookCapability.CAPABILITY) {
            return HookCapability.CAPABILITY.cast(cap);
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return cap.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        cap.deserializeNBT(nbt);
    }
}
