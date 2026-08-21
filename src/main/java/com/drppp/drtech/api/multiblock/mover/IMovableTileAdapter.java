package com.drppp.drtech.api.multiblock.mover;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public interface IMovableTileAdapter {
    boolean supports(TileEntity tileEntity);

    default boolean canMove(EntityPlayerMP player, TileEntity tileEntity) {
        return false;
    }

    NBTTagCompound capture(TileEntity tileEntity);

    default void relocateNbt(NBTTagCompound tag, BlockPos source, BlockPos destination) {
        tag.setInteger("x", destination.getX());
        tag.setInteger("y", destination.getY());
        tag.setInteger("z", destination.getZ());
    }

    default void afterRestore(TileEntity restored) {
        restored.markDirty();
    }

}
