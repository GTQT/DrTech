package com.drppp.drtech.intergations.opencomputers;

import gregtech.api.metatileentity.MetaTileEntity;
import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/** Adjacent block driver for the dock, programmer and fleet controller. */
public final class OpenComputersDroneDriver implements DriverBlock, NamedBlock {
    @Override
    public boolean worksWith(World world, BlockPos position, EnumFacing side) {
        return OpenComputersMachineAccess.componentFor(
                OpenComputersMachineAccess.getMachine(world, position)) != null;
    }

    @Nullable
    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos position, EnumFacing side) {
        MetaTileEntity machine = OpenComputersMachineAccess.getMachine(world, position);
        String component = OpenComputersMachineAccess.componentFor(machine);
        return component == null ? null : new OpenComputersMachineEnvironment(world, position, component);
    }

    @Override public String preferredName() { return "drtech_drone"; }
    @Override public int priority() { return 0; }
}
