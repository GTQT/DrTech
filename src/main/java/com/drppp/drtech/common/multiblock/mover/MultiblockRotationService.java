package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.api.multiblock.mover.IMovableTileAdapter;
import com.drppp.drtech.api.multiblock.mover.IRotatableTileAdapter;
import com.drppp.drtech.api.multiblock.mover.MovableTileAdapterRegistry;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MultiblockRotationService {
    private MultiblockRotationService() {
    }

    public static List<MovingBlockSnapshot> rotate(EntityPlayerMP player,
                                                    MultiblockSnapshot snapshot,
                                                    MoverRotation rotation)
            throws MoverException {
        if (rotation == MoverRotation.NONE) return snapshot.getBlocks();
        List<MovingBlockSnapshot> rotated = new ArrayList<>(snapshot.getBlockCount());
        Set<BlockPos> occupied = new HashSet<>();
        for (MovingBlockSnapshot block : snapshot.getBlocks()) {
            BlockPos relative = rotation.rotate(block.getRelativePos());
            if (!occupied.add(relative)) {
                throw new MoverException("drtech.multiblock_mover.error.rotation_collision");
            }
            IBlockState state;
            NBTTagCompound tileNbt = block.getTileNbt();
            try {
                if (tileNbt == null) {
                    state = block.getState().getBlock().withRotation(
                            block.getState(), rotation.getMinecraftRotation());
                    validateStateRoundTrip(block.getState(), state, rotation, null);
                } else {
                    IMovableTileAdapter movementAdapter = block.getTileAdapterId() == null
                            ? null : MovableTileAdapterRegistry.get(block.getTileAdapterId());
                    IRotatableTileAdapter adapter = movementAdapter instanceof IRotatableTileAdapter
                            ? (IRotatableTileAdapter) movementAdapter : null;
                    TileEntity sourceTile = player.getServerWorld().getTileEntity(
                            snapshot.getControllerPos().add(block.getRelativePos()));
                    if (adapter == null) {
                        logRejectedBlock(snapshot, block, "no rotatable adapter");
                        throw new MoverException("drtech.multiblock_mover.error.rotation_unsupported");
                    }
                    if (sourceTile == null) {
                        logRejectedBlock(snapshot, block, "source block entity is missing");
                        throw new MoverException("drtech.multiblock_mover.error.rotation_unsupported");
                    }
                    if (!adapter.canRotate(player, sourceTile, rotation.getMinecraftRotation())) {
                        logRejectedBlock(snapshot, block, "adapter rejected rotation");
                        throw new MoverException("drtech.multiblock_mover.error.rotation_unsupported");
                    }
                    state = adapter.rotateState(block.getState(), rotation.getMinecraftRotation());
                    validateStateRoundTrip(block.getState(), state, rotation, adapter);
                    NBTTagCompound originalNbt = tileNbt.copy();
                    adapter.rotateNbt(tileNbt, rotation.getMinecraftRotation(), sourceTile);
                    NBTTagCompound restoredNbt = tileNbt.copy();
                    adapter.rotateNbt(restoredNbt,
                            rotation.inverseRotation().getMinecraftRotation(), sourceTile);
                    if (!originalNbt.equals(restoredNbt)) {
                        logRejectedBlock(snapshot, block, "block entity NBT failed round-trip validation");
                        throw new MoverException(
                                "drtech.multiblock_mover.error.rotation_unsafe_state");
                    }
                }
            } catch (MoverException error) {
                throw error;
            } catch (Throwable error) {
                DrTechMain.LOGGER.error("Multiblock rotation adapter failed at {} for {}",
                        snapshot.getControllerPos().add(block.getRelativePos()),
                        block.getTileAdapterId(), error);
                throw new MoverException("drtech.multiblock_mover.error.rotation_unsupported", error);
            }
            rotated.add(new MovingBlockSnapshot(relative, block.getSourceRelativePos(), state, tileNbt,
                    block.getTileAdapterId(), block.isController()));
        }
        return rotated;
    }

    private static void logRejectedBlock(MultiblockSnapshot snapshot,
                                         MovingBlockSnapshot block, String reason) {
        DrTechMain.LOGGER.warn("Multiblock mover rotation rejected block at {}: {}; adapter={}",
                snapshot.getControllerPos().add(block.getRelativePos()), reason,
                block.getTileAdapterId());
    }

    private static void validateStateRoundTrip(IBlockState original, IBlockState rotated,
                                               MoverRotation rotation,
                                               IRotatableTileAdapter adapter)
            throws MoverException {
        if (rotated == null || rotated.getBlock() != original.getBlock()) {
            throw new MoverException("drtech.multiblock_mover.error.rotation_unsafe_state");
        }
        validateDirectionalProperties(original, rotated, rotation.getMinecraftRotation());
        Rotation inverse = rotation.inverseRotation().getMinecraftRotation();
        IBlockState restored = adapter == null
                ? rotated.getBlock().withRotation(rotated, inverse)
                : adapter.rotateState(rotated, inverse);
        if (!original.equals(restored)) {
            throw new MoverException("drtech.multiblock_mover.error.rotation_unsafe_state");
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void validateDirectionalProperties(IBlockState original, IBlockState rotated,
                                                      Rotation rotation)
            throws MoverException {
        for (IProperty property : original.getPropertyKeys()) {
            if (!(property instanceof PropertyDirection)) continue;
            EnumFacing before = (EnumFacing) original.getValue(property);
            EnumFacing after = (EnumFacing) rotated.getValue(property);
            if (after != rotation.rotate(before)) {
                throw new MoverException(
                        "drtech.multiblock_mover.error.rotation_unsafe_state");
            }
        }
    }
}
