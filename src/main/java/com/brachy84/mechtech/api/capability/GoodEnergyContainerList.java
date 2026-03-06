package com.brachy84.mechtech.api.capability;

import gregtech.api.capability.IEnergyContainer;
import net.minecraft.util.EnumFacing;

import java.util.List;
import java.util.OptionalLong;

public class GoodEnergyContainerList implements IEnergyContainer {

    private final List<IEnergyContainer> energyContainers;

    public GoodEnergyContainerList(List<IEnergyContainer> energyContainers) {
        this.energyContainers = energyContainers;
    }

    public boolean inputsEnergy(EnumFacing enumFacing) {
        for(IEnergyContainer energyContainer : energyContainers)
            if(energyContainer.inputsEnergy(enumFacing))
                return true;
        return false;
    }

    @Override
    public long getInputPerSec() {
        long sum = 0;
        for (IEnergyContainer energyContainer : energyContainers) {
            sum += energyContainer.getInputPerSec();
        }
        return sum;
    }

    @Override
    public long getOutputPerSec() {
        long sum = 0;
        for (IEnergyContainer energyContainer : energyContainers) {
            sum += energyContainer.getOutputPerSec();
        }
        return sum;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        long oAmps = amperage;
        for (IEnergyContainer energyContainer : energyContainers) {
            amperage -= energyContainer.acceptEnergyFromNetwork(null, voltage, amperage);
            if (amperage == 0) break;
        }
        return oAmps - amperage;
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        long energyAdded = 0L;
        for (IEnergyContainer energyContainer : energyContainers) {
            energyAdded += energyContainer.changeEnergy(energyToAdd - energyAdded);
            if (energyAdded == energyToAdd) break;
        }
        return energyAdded;
    }

    @Override
    public long getEnergyStored() {
        return energyContainers.stream()
                .mapToLong(IEnergyContainer::getEnergyStored)
                .sum();
    }

    @Override
    public long getEnergyCapacity() {
        return energyContainers.stream()
                .mapToLong(IEnergyContainer::getEnergyCapacity)
                .sum();
    }

    @Override
    public long getInputAmperage() {
        OptionalLong amps = energyContainers.stream()
                .mapToLong(IEnergyContainer::getInputAmperage)
                .min();
        if(amps.isPresent())
            return amps.getAsLong();
        return 0;
    }

    @Override
    public long getInputVoltage() {
        OptionalLong amps = energyContainers.stream()
                .mapToLong(IEnergyContainer::getInputVoltage)
                .min();
        if(amps.isPresent())
            return amps.getAsLong();
        return 0;
    }
}
