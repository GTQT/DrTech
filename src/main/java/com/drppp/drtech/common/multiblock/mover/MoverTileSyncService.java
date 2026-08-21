package com.drppp.drtech.common.multiblock.mover;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

/** Ensures newly reconstructed tile entities have a valid client baseline before deltas arrive. */
public final class MoverTileSyncService {
    private MoverTileSyncService() {
    }

    public static void queueInitialData(TileEntity tileEntity) {
        if (!(tileEntity instanceof MetaTileEntityHolder)) return;
        MetaTileEntityHolder holder = (MetaTileEntityHolder) tileEntity;
        MetaTileEntity metaTileEntity = holder.getMetaTileEntity();
        if (metaTileEntity == null) {
            throw new IllegalStateException("Restored GT holder has no MetaTileEntity at "
                    + holder.getPos());
        }
        holder.writeCustomData(GregtechDataCodes.INITIALIZE_MTE, buffer -> {
            buffer.writeVarInt(metaTileEntity.getRegistry().getNetworkId());
            buffer.writeVarInt(metaTileEntity.getRegistry()
                    .getIdByObjectName(metaTileEntity.metaTileEntityId));
            metaTileEntity.writeInitialSyncData(buffer);
        });
    }

    public static void notifyPosition(WorldServer world, BlockPos pos) {
        syncPosition(world, pos);
        notifyNeighbors(world, pos);
    }

    /** Sends the final block state to clients without running block physics. */
    public static void syncPosition(WorldServer world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    /** Runs the server-side neighbor callbacks for an already synchronized position. */
    public static void notifyNeighbors(WorldServer world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
    }
}
