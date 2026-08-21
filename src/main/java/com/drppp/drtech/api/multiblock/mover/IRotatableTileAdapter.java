package com.drppp.drtech.api.multiblock.mover;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;

/** Explicit opt-in contract for horizontally rotating a movable block entity. */
public interface IRotatableTileAdapter extends IMovableTileAdapter {
    boolean canRotate(EntityPlayerMP player, TileEntity tileEntity, Rotation rotation);

    default IBlockState rotateState(IBlockState state, Rotation rotation) {
        return state.getBlock().withRotation(state, rotation);
    }

    /** Rotate only adapter-owned directional data. Coordinates are relocated separately. */
    void rotateNbt(NBTTagCompound tag, Rotation rotation);

    /**
     * Context-aware rotation hook. Implementations may use the live source tile
     * to distinguish persisted sentinel directions from real world directions.
     */
    default void rotateNbt(NBTTagCompound tag, Rotation rotation, TileEntity sourceTile) {
        rotateNbt(tag, rotation);
    }
}
