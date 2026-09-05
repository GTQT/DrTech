package com.drppp.drtech.compat.opencomputers;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Prevents a replacement machine at the same coordinates from inheriting a stale credential. */
public final class OpenComputersPairingCleanupHandler {
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isRemote) return;
        MetaTileEntity machine = OpenComputersMachineAccess.getMachine(event.getWorld(), event.getPos());
        String component = OpenComputersMachineAccess.componentFor(machine);
        if (component != null) OpenComputersPairingState.get((net.minecraft.world.World) event.getWorld())
                .removeDevice(event.getWorld().provider.getDimension(), event.getPos(), component);
    }
}
