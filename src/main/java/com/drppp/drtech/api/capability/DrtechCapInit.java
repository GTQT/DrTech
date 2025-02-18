package com.drppp.drtech.api.capability;

import com.drppp.drtech.api.capability.impl.RotationEnergyHandler;
import com.drppp.drtech.api.capability.impl.RotationEnergyStore;
import gregtech.api.capability.SimpleCapabilityManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.Callable;

public class DrtechCapInit {
    public static <T> void registerCapabilityWithNoDefault(Class<T> capabilityClass) {
        CapabilityManager.INSTANCE.register(capabilityClass, new Capability.IStorage<T>() {
            public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
                throw new UnsupportedOperationException("Not supported");
            }

            public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {

                throw new UnsupportedOperationException("Not supported");
            }
        }, () -> {
            throw new UnsupportedOperationException("This capability has no default implementation");
        });
    }
    public static void init()
    {
        registerCapabilityWithNoDefault(IFuelRodData.class);
        registerCapabilityWithNoDefault(IHeatVent.class);
        registerCapabilityWithNoDefault(INeutronReflector.class);
        registerCapabilityWithNoDefault(IHeatExchanger.class);
        registerCapabilityWithNoDefault(ICoolantCell.class);
        registerCapabilityWithNoDefault(INuclearDataShow.class);
        CapabilityManager.INSTANCE.register(IRotationEnergy.class,new RotationEnergyStore(), RotationEnergyHandler::new);
    }
}
