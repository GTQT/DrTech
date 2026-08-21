package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.api.multiblock.mover.IRotatableTileAdapter;
import com.drppp.drtech.api.multiblock.mover.MovableTileAdapterRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import gregtech.api.util.RelativeDirection;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoverRotationTest {
    private static final BlockPos SAMPLE = new BlockPos(2, 5, -3);

    @Test
    void rotatesAroundControllerYAxis() {
        assertEquals(new BlockPos(2, 5, -3), MoverRotation.NONE.rotate(SAMPLE));
        assertEquals(new BlockPos(3, 5, 2), MoverRotation.CLOCKWISE_90.rotate(SAMPLE));
        assertEquals(new BlockPos(-2, 5, 3), MoverRotation.CLOCKWISE_180.rotate(SAMPLE));
        assertEquals(new BlockPos(-3, 5, -2), MoverRotation.COUNTERCLOCKWISE_90.rotate(SAMPLE));
    }

    @Test
    void inverseRestoresSourceCoordinate() {
        for (MoverRotation rotation : MoverRotation.values()) {
            assertEquals(SAMPLE, rotation.inverse(rotation.rotate(SAMPLE)));
        }
    }

    @Test
    void stepWrapsInBothDirections() {
        assertEquals(MoverRotation.CLOCKWISE_90, MoverRotation.NONE.step(1));
        assertEquals(MoverRotation.COUNTERCLOCKWISE_90, MoverRotation.NONE.step(-1));
        assertEquals(MoverRotation.NONE, MoverRotation.COUNTERCLOCKWISE_90.step(1));
        assertEquals(MoverRotation.NONE, MoverRotation.CLOCKWISE_90.step(-1));
    }

    @Test
    void chargesShortestRotationPath() {
        assertEquals(0, MoverRotation.NONE.getChargedQuarterTurns());
        assertEquals(1, MoverRotation.CLOCKWISE_90.getChargedQuarterTurns());
        assertEquals(2, MoverRotation.CLOCKWISE_180.getChargedQuarterTurns());
        assertEquals(1, MoverRotation.COUNTERCLOCKWISE_90.getChargedQuarterTurns());
    }

    @Test
    void protectsExactFluidAndEveryDirectNeighbourFromPhysicsNotifications() {
        BlockPos fluid = new BlockPos(10, 20, 30);
        Set<BlockPos> fluids = Collections.singleton(fluid);

        assertTrue(MultiblockMoveTransaction.isFluidSensitivePosition(fluid, fluids));
        for (EnumFacing side : EnumFacing.VALUES) {
            assertTrue(MultiblockMoveTransaction.isFluidSensitivePosition(
                    fluid.offset(side), fluids));
        }
        assertFalse(MultiblockMoveTransaction.isFluidSensitivePosition(
                fluid.add(2, 0, 0), fluids));
    }

    @Test
    void checksAllProtectedFluidCells() {
        Set<BlockPos> fluids = new HashSet<>();
        fluids.add(BlockPos.ORIGIN);
        fluids.add(new BlockPos(8, 4, -2));

        assertTrue(MultiblockMoveTransaction.isFluidSensitivePosition(
                new BlockPos(8, 5, -2), fluids));
        assertFalse(MultiblockMoveTransaction.isFluidSensitivePosition(
                new BlockPos(8, 6, -2), fluids));
    }

    @Test
    void gtAdapterRotatesFrontAndCoverAttachmentSides() {
        IRotatableTileAdapter adapter = (IRotatableTileAdapter) MovableTileAdapterRegistry.get(
                MovableTileAdapterRegistry.GT_META_TILE_ENTITY);
        NBTTagCompound holder = new NBTTagCompound();
        NBTTagCompound meta = new NBTTagCompound();
        meta.setInteger("FrontFacing", EnumFacing.NORTH.getIndex());
        meta.setByte("UpwardsFacing", (byte) EnumFacing.WEST.getIndex());
        NBTTagList covers = new NBTTagList();
        covers.appendTag(cover(EnumFacing.WEST));
        covers.appendTag(cover(EnumFacing.UP));
        meta.setTag("Covers", covers);
        holder.setTag("MetaTileEntity", meta);
        NBTTagCompound original = holder.copy();

        adapter.rotateNbt(holder, Rotation.CLOCKWISE_90);

        assertEquals(EnumFacing.EAST.getIndex(), meta.getInteger("FrontFacing"));
        // GTCEu stores this as a local-axis roll reference. For a horizontal
        // front rotation its own simulateAxisRotation() keeps WEST unchanged.
        assertEquals(EnumFacing.WEST.getIndex(), meta.getByte("UpwardsFacing"));
        assertEquals(EnumFacing.NORTH.getIndex(), covers.getCompoundTagAt(0).getByte("Side"));
        assertEquals(EnumFacing.UP.getIndex(), covers.getCompoundTagAt(1).getByte("Side"));

        adapter.rotateNbt(holder, Rotation.COUNTERCLOCKWISE_90);
        assertEquals(original, holder);
    }

    @Test
    void gtAdapterRotatesAndRelocatesFormedPieceCenters() {
        IRotatableTileAdapter adapter = (IRotatableTileAdapter) MovableTileAdapterRegistry.get(
                MovableTileAdapterRegistry.GT_META_TILE_ENTITY);
        BlockPos source = new BlockPos(100, 20, -40);
        BlockPos destination = new BlockPos(-8, 35, 70);
        NBTTagCompound holder = new NBTTagCompound();
        holder.setInteger("x", source.getX());
        holder.setInteger("y", source.getY());
        holder.setInteger("z", source.getZ());
        NBTTagCompound meta = new NBTTagCompound();
        NBTTagCompound formedMetadata = new NBTTagCompound();
        NBTTagCompound repeats = new NBTTagCompound();
        repeats.setIntArray("body", new int[] {7});
        formedMetadata.setTag("PieceRepeats", repeats);
        NBTTagCompound centers = new NBTTagCompound();
        centers.setLong("body", source.add(2, 4, -3).toLong());
        formedMetadata.setTag("PieceCenters", centers);
        meta.setTag("FormedMetadata", formedMetadata);
        holder.setTag("MetaTileEntity", meta);

        adapter.rotateNbt(holder, Rotation.CLOCKWISE_90);
        adapter.relocateNbt(holder, source, destination);

        assertEquals(destination.add(3, 4, 2),
                BlockPos.fromLong(centers.getLong("body")));
        assertEquals(7, repeats.getIntArray("body")[0]);
        assertEquals(destination.getX(), holder.getInteger("x"));
        assertEquals(destination.getY(), holder.getInteger("y"));
        assertEquals(destination.getZ(), holder.getInteger("z"));
    }

    @Test
    void gtAdapterPreservesLocalUpAxisForHorizontalFrontRotation() {
        IRotatableTileAdapter adapter = (IRotatableTileAdapter) MovableTileAdapterRegistry.get(
                MovableTileAdapterRegistry.GT_META_TILE_ENTITY);
        NBTTagCompound holder = new NBTTagCompound();
        NBTTagCompound meta = new NBTTagCompound();
        meta.setInteger("FrontFacing", EnumFacing.WEST.getIndex());
        meta.setByte("UpwardsFacing", (byte) EnumFacing.NORTH.getIndex());
        holder.setTag("MetaTileEntity", meta);

        adapter.rotateNbt(holder, Rotation.CLOCKWISE_90);

        assertEquals(EnumFacing.NORTH.getIndex(), meta.getInteger("FrontFacing"));
        assertEquals(EnumFacing.NORTH.getIndex(), meta.getByte("UpwardsFacing"));
        adapter.rotateNbt(holder, Rotation.COUNTERCLOCKWISE_90);
        assertEquals(EnumFacing.WEST.getIndex(), meta.getInteger("FrontFacing"));
        assertEquals(EnumFacing.NORTH.getIndex(), meta.getByte("UpwardsFacing"));
    }

    @Test
    void everyGtBasisRotatesAsOneRigidStructure() {
        BlockPos origin = new BlockPos(17, 41, -9);
        BlockPos[] localSamples = {
                new BlockPos(1, 0, 0), new BlockPos(0, 1, 0),
                new BlockPos(0, 0, 1), new BlockPos(3, -2, 5)
        };
        for (MoverRotation rotation : MoverRotation.values()) {
            for (EnumFacing oldFront : EnumFacing.values()) {
                for (EnumFacing oldUp : EnumFacing.Plane.HORIZONTAL) {
                    EnumFacing newFront = rotation.getMinecraftRotation().rotate(oldFront);
                    EnumFacing newUp = oldFront.getAxis() == EnumFacing.Axis.Y
                            ? rotation.getMinecraftRotation().rotate(oldUp)
                            : RelativeDirection.simulateAxisRotation(newFront, oldFront, oldUp);
                    for (boolean flipped : new boolean[] {false, true}) {
                        for (BlockPos local : localSamples) {
                            BlockPos oldWorldOffset = RelativeDirection.offsetPos(
                                    origin, oldFront, oldUp, flipped,
                                    local.getX(), local.getY(), local.getZ()).subtract(origin);
                            BlockPos newWorldOffset = RelativeDirection.offsetPos(
                                    origin, newFront, newUp, flipped,
                                    local.getX(), local.getY(), local.getZ()).subtract(origin);
                            assertEquals(rotation.rotate(oldWorldOffset), newWorldOffset,
                                    "rotation=" + rotation + ", front=" + oldFront
                                            + ", up=" + oldUp + ", flipped=" + flipped
                                            + ", local=" + local);
                        }
                    }
                }
            }
        }
    }

    private static NBTTagCompound cover(EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Side", (byte) side.getIndex());
        return tag;
    }
}
