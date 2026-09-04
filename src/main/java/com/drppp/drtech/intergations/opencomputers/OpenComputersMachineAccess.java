package com.drppp.drtech.intergations.opencomputers;

import com.drppp.drtech.drone.machine.MetaTileEntityDroneDock;
import com.drppp.drtech.drone.machine.MetaTileEntityDroneFleetController;
import com.drppp.drtech.drone.machine.MetaTileEntityDroneProgrammer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/** OC-free machine lookup shared by the pairing command and the optional driver. */
public final class OpenComputersMachineAccess {
    private OpenComputersMachineAccess() { }

    @Nullable
    public static MetaTileEntity getMachine(World world, BlockPos position) {
        if (world == null || position == null || !world.isBlockLoaded(position)) return null;
        TileEntity tile = world.getTileEntity(position);
        return tile instanceof IGregTechTileEntity ? ((IGregTechTileEntity) tile).getMetaTileEntity() : null;
    }

    @Nullable
    public static String componentFor(MetaTileEntity machine) {
        if (machine instanceof MetaTileEntityDroneDock) return OpenComputersComponentIds.DRONE_DOCK;
        if (machine instanceof MetaTileEntityDroneProgrammer) return OpenComputersComponentIds.DRONE_PROGRAMMER;
        if (machine instanceof MetaTileEntityDroneFleetController) return OpenComputersComponentIds.DRONE_FLEET;
        return null;
    }
}
