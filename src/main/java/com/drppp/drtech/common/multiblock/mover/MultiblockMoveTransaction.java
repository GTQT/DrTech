package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.api.multiblock.mover.IMovableTileAdapter;
import com.drppp.drtech.api.multiblock.mover.MovableTileAdapterRegistry;
import com.drppp.drtech.api.multiblock.mover.MultiblockMovePermissionRegistry;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.StructureFailureTrace;
import gregtech.api.pattern.StructureRuntime;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MultiblockMoveTransaction {

    private MultiblockMoveTransaction() {
    }

    public static int move(EntityPlayerMP player, MoverSession session,
                           BlockPos targetControllerPos, ItemStack mover,
                           long energyCost) throws MoverException {
        MoverPerformanceMetrics.Builder metrics = MoverPerformanceMetrics.begin(session);
        boolean success = false;
        try {
            int moved = moveMeasured(player, session, targetControllerPos, mover, energyCost, metrics);
            success = true;
            return moved;
        } finally {
            MoverPerformanceMetrics.record(metrics.finish(success));
        }
    }

    private static int moveMeasured(EntityPlayerMP player, MoverSession session,
                                    BlockPos targetControllerPos, ItemStack mover,
                                    long energyCost, MoverPerformanceMetrics.Builder metrics)
            throws MoverException {
        WorldServer world = player.getServerWorld();
        MultiblockSnapshot selected = session.getSnapshot();
        if (selected.getDimension() != world.provider.getDimension()) {
            throw new MoverException("drtech.multiblock_mover.error.wrong_dimension");
        }
        if (selected.getControllerPos().equals(targetControllerPos)) {
            throw new MoverException("drtech.multiblock_mover.error.same_position");
        }
        if (session.getRotation() != MoverRotation.NONE
                && !DrtConfig.MultiblockMover.enableRotation) {
            throw new MoverException("drtech.multiblock_mover.error.rotation_disabled");
        }
        double maxDistance = DrtConfig.MultiblockMover.maxDistance;
        if (player.getDistanceSqToCenter(targetControllerPos) > maxDistance * maxDistance) {
            throw new MoverException("drtech.multiblock_mover.error.too_far");
        }

        long stageStarted = System.nanoTime();
        MultiblockSnapshot current;
        try {
            current = MultiblockCaptureService.capture(
                    player, selected.getControllerPos(), true);
        } finally {
            metrics.sourceCapture(System.nanoTime() - stageStarted);
        }
        if (!selected.contentEquals(current)) {
            throw new MoverException("drtech.multiblock_mover.error.source_changed");
        }

        stageStarted = System.nanoTime();
        List<MovingBlockSnapshot> transformed = MultiblockRotationService.rotate(
                player, current, session.getRotation());
        Map<BlockPos, MovingBlockSnapshot> destinations;
        try {
            destinations = buildDestinations(transformed, targetControllerPos);
            validateDestinations(player, world, current.getSourcePositions(), destinations);
        } finally {
            metrics.validation(System.nanoTime() - stageStarted);
        }

        stageStarted = System.nanoTime();
        Set<BlockPos> affected = new HashSet<>(current.getSourcePositions());
        affected.addAll(destinations.keySet());
        Map<BlockPos, WorldBlockRecord> originals;
        try {
            originals = captureWorldRegion(world, affected);
        } finally {
            metrics.worldSnapshot(System.nanoTime() - stageStarted);
        }

        MultiblockControllerBase oldController = getController(world, current.getControllerPos());
        boolean energyConsumed = false;
        stageStarted = System.nanoTime();
        try {
            MultiblockRecoveryJournal.prepare(world, session, targetControllerPos, affected);
            metrics.journal(System.nanoTime() - stageStarted);
        } catch (Exception journalFailure) {
            metrics.journal(System.nanoTime() - stageStarted);
            DrTechMain.LOGGER.error("Could not prepare multiblock mover recovery journal {}",
                    session.getId(), journalFailure);
            throw new MoverException("drtech.multiblock_mover.error.journal_failed", journalFailure);
        }
        long commitStarted = System.nanoTime();
        try {
            oldController.invalidateStructure();
            clearPositions(world, current.getSourcePositions());
            placeStates(world, destinations);
            placeTiles(world, destinations, current.getControllerPos());
            notifyPositions(world, affected, collectFluidPositions(destinations));

            MultiblockControllerBase restoredController = getController(world, targetControllerPos);
            restoredController.checkStructurePattern();
            if (!restoredController.isStructureFormed()) {
                StructureRuntime runtime = restoredController.getStructureRuntime();
                StructureFailureTrace trace = runtime == null ? null : runtime.getLastFailure();
                throw new IllegalStateException("Restored GTCEu controller did not form at "
                        + targetControllerPos + "; front=" + restoredController.getFrontFacing()
                        + ", structureFront=" + restoredController.getFrontFacingForStructure()
                        + ", up=" + restoredController.getUpwardsFacing()
                        + ", failure=" + (trace == null ? "unavailable" : trace));
            }
            if (!MoverEnergyService.consume(player, mover, energyCost)) {
                throw new IllegalStateException("Mover energy changed during transaction");
            }
            energyConsumed = true;
            DrTechMain.LOGGER.info("Multiblock mover transaction {} verified controller formation at {}",
                    session.getId(), targetControllerPos);
            MultiblockRecoveryJournal.complete(world, session.getId());
            metrics.commit(System.nanoTime() - commitStarted);
            return current.getBlockCount();
        } catch (Throwable failure) {
            metrics.commit(System.nanoTime() - commitStarted);
            if (energyConsumed && !MoverEnergyService.refund(player, mover, energyCost)) {
                DrTechMain.LOGGER.error("Multiblock mover transaction {} could not refund {} EU",
                        session.getId(), energyCost);
            }
            DrTechMain.LOGGER.error("Multiblock mover transaction {} failed; rolling back",
                    session.getId(), failure);
            long rollbackStarted = System.nanoTime();
            try {
                restoreOriginals(world, affected, originals);
                MultiblockControllerBase rolledBack = getController(world, current.getControllerPos());
                rolledBack.checkStructurePattern();
                DrTechMain.LOGGER.warn("Multiblock mover transaction {} rolled back successfully at {}",
                        session.getId(), current.getControllerPos());
                MultiblockRecoveryJournal.complete(world, session.getId());
            } catch (Throwable rollbackFailure) {
                DrTechMain.LOGGER.error("Multiblock mover rollback {} failed", session.getId(), rollbackFailure);
                failure.addSuppressed(rollbackFailure);
                throw new MoverException("drtech.multiblock_mover.error.rollback_failed", failure);
            } finally {
                metrics.rollback(System.nanoTime() - rollbackStarted);
            }
            throw new MoverException("drtech.multiblock_mover.error.move_failed", failure);
        }
    }

    private static Map<BlockPos, MovingBlockSnapshot> buildDestinations(
            List<MovingBlockSnapshot> blocks, BlockPos targetControllerPos) {
        Map<BlockPos, MovingBlockSnapshot> result = new LinkedHashMap<>();
        for (MovingBlockSnapshot block : blocks) {
            result.put(targetControllerPos.add(block.getRelativePos()), block);
        }
        return result;
    }

    private static void validateDestinations(EntityPlayerMP player, WorldServer world,
                                             Set<BlockPos> sourcePositions,
                                             Map<BlockPos, MovingBlockSnapshot> destinations)
            throws MoverException {
        for (BlockPos pos : destinations.keySet()) {
            if (pos.getY() < 0 || pos.getY() >= world.getActualHeight()) {
                throw new MoverException("drtech.multiblock_mover.error.out_of_world");
            }
            if (!world.isBlockLoaded(pos)) {
                throw new MoverException("drtech.multiblock_mover.error.chunk_unloaded");
            }
            if (!MultiblockMovePermissionRegistry.canPlace(player, world, pos)) {
                throw new MoverException("drtech.multiblock_mover.error.no_permission");
            }
            if (sourcePositions.contains(pos)) continue;

            IBlockState existing = world.getBlockState(pos);
            if (!world.isAirBlock(pos) && !existing.getBlock().isReplaceable(world, pos)) {
                throw new MoverException("drtech.multiblock_mover.error.blocked");
            }
            if (world.getTileEntity(pos) != null) {
                throw new MoverException("drtech.multiblock_mover.error.blocked");
            }
        }
    }

    private static Map<BlockPos, WorldBlockRecord> captureWorldRegion(WorldServer world,
                                                                       Set<BlockPos> positions) {
        Map<BlockPos, WorldBlockRecord> records = new HashMap<>();
        for (BlockPos pos : positions) {
            TileEntity tile = world.getTileEntity(pos);
            records.put(pos.toImmutable(), new WorldBlockRecord(
                    world.getBlockState(pos),
                    tile == null ? null : tile.writeToNBT(new NBTTagCompound())));
        }
        return records;
    }

    private static void clearPositions(WorldServer world, Set<BlockPos> positions) {
        List<BlockPos> ordered = new ArrayList<>(positions);
        ordered.sort((left, right) -> Integer.compare(right.getY(), left.getY()));
        for (BlockPos pos : ordered) {
            if (world.getTileEntity(pos) != null) world.removeTileEntity(pos);
            if (!world.isAirBlock(pos)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            }
        }
    }

    private static void placeStates(WorldServer world,
                                    Map<BlockPos, MovingBlockSnapshot> destinations) {
        // Solid containment must exist before an exact fluid pattern cell is restored.
        // Keeping fluids last also prevents modded blocks from observing an incomplete tank.
        for (Map.Entry<BlockPos, MovingBlockSnapshot> entry : destinations.entrySet()) {
            if (entry.getValue().isController() || isFluid(entry.getValue().getState())) continue;
            world.setBlockState(entry.getKey(), entry.getValue().getState(), 2);
        }
        for (Map.Entry<BlockPos, MovingBlockSnapshot> entry : destinations.entrySet()) {
            if (entry.getValue().isController() || !isFluid(entry.getValue().getState())) continue;
            world.setBlockState(entry.getKey(), entry.getValue().getState(), 2);
        }
        for (Map.Entry<BlockPos, MovingBlockSnapshot> entry : destinations.entrySet()) {
            if (entry.getValue().isController()) {
                world.setBlockState(entry.getKey(), entry.getValue().getState(), 2);
            }
        }
    }

    private static void placeTiles(WorldServer world,
                                   Map<BlockPos, MovingBlockSnapshot> destinations,
                                   BlockPos sourceController) {
        for (Map.Entry<BlockPos, MovingBlockSnapshot> entry : destinations.entrySet()) {
            MovingBlockSnapshot snapshot = entry.getValue();
            NBTTagCompound tileNbt = snapshot.getTileNbt();
            if (tileNbt == null) continue;
            if (snapshot.getTileAdapterId() == null) {
                throw new IllegalStateException("Tile snapshot has no movement adapter at " + entry.getKey());
            }
            IMovableTileAdapter adapter = MovableTileAdapterRegistry.get(snapshot.getTileAdapterId());
            if (adapter == null) {
                throw new IllegalStateException("Missing movement adapter " + snapshot.getTileAdapterId());
            }
            restoreTile(world, sourceController.add(snapshot.getSourceRelativePos()),
                    entry.getKey(), tileNbt, adapter);
        }
    }

    private static void restoreOriginals(WorldServer world, Set<BlockPos> affected,
                                         Map<BlockPos, WorldBlockRecord> originals) {
        clearPositions(world, affected);
        for (Map.Entry<BlockPos, WorldBlockRecord> entry : originals.entrySet()) {
            if (entry.getValue().state.getBlock() != Blocks.AIR) {
                world.setBlockState(entry.getKey(), entry.getValue().state, 2);
            }
        }
        for (Map.Entry<BlockPos, WorldBlockRecord> entry : originals.entrySet()) {
            if (entry.getValue().tileNbt != null) {
                restoreTile(world, entry.getKey(), entry.getKey(), entry.getValue().tileNbt, null);
            }
        }
        notifyPositions(world, affected, collectOriginalFluidPositions(originals));
    }

    private static void restoreTile(WorldServer world, BlockPos sourcePos, BlockPos pos,
                                    NBTTagCompound originalNbt,
                                    @Nullable IMovableTileAdapter adapter) {
        NBTTagCompound relocated = originalNbt.copy();
        if (adapter != null) {
            adapter.relocateNbt(relocated, sourcePos, pos);
        } else {
            relocated.setInteger("x", pos.getX());
            relocated.setInteger("y", pos.getY());
            relocated.setInteger("z", pos.getZ());
        }
        TileEntity restored = TileEntity.create(world, relocated);
        if (restored == null) {
            throw new IllegalStateException("Could not create tile entity at " + pos);
        }
        if (world.getTileEntity(pos) != null) world.removeTileEntity(pos);
        world.setTileEntity(pos, restored);
        restored.validate();
        if (adapter != null) adapter.afterRestore(restored);
        restored.markDirty();
        MoverTileSyncService.queueInitialData(restored);
    }

    private static Set<BlockPos> collectFluidPositions(
            Map<BlockPos, MovingBlockSnapshot> destinations) {
        Set<BlockPos> fluids = new HashSet<>();
        for (Map.Entry<BlockPos, MovingBlockSnapshot> entry : destinations.entrySet()) {
            if (isFluid(entry.getValue().getState())) fluids.add(entry.getKey());
        }
        return fluids;
    }

    private static Set<BlockPos> collectOriginalFluidPositions(
            Map<BlockPos, WorldBlockRecord> originals) {
        Set<BlockPos> fluids = new HashSet<>();
        for (Map.Entry<BlockPos, WorldBlockRecord> entry : originals.entrySet()) {
            if (isFluid(entry.getValue().state)) fluids.add(entry.getKey());
        }
        return fluids;
    }

    private static boolean isFluid(IBlockState state) {
        return state.getMaterial().isLiquid();
    }

    private static void notifyPositions(WorldServer world, Set<BlockPos> positions,
                                        Set<BlockPos> protectedFluids) {
        // Client synchronization does not need neighbor callbacks. Do it for every position,
        // including protected fluids, so moved blocks never remain visually stale.
        for (BlockPos pos : positions) {
            MoverTileSyncService.syncPosition(world, pos);
        }
        // An exact WATER pattern cell can turn into FLOWING_WATER when an adjacent casing sends
        // neighborChanged. Avoid physics callbacks from the fluid itself and its six neighbours;
        // all other moved blocks still receive the normal post-transaction notification.
        for (BlockPos pos : positions) {
            if (!isFluidSensitivePosition(pos, protectedFluids)) {
                MoverTileSyncService.notifyNeighbors(world, pos);
            }
        }
    }

    static boolean isFluidSensitivePosition(BlockPos pos, Set<BlockPos> protectedFluids) {
        if (protectedFluids.contains(pos)) return true;
        for (net.minecraft.util.EnumFacing side : net.minecraft.util.EnumFacing.VALUES) {
            if (protectedFluids.contains(pos.offset(side))) return true;
        }
        return false;
    }

    private static MultiblockControllerBase getController(WorldServer world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof IGregTechTileEntity)) {
            throw new IllegalStateException("GT controller holder missing at " + pos);
        }
        MetaTileEntity meta = ((IGregTechTileEntity) tile).getMetaTileEntity();
        if (!(meta instanceof MultiblockControllerBase)) {
            throw new IllegalStateException("GT multiblock controller missing at " + pos);
        }
        return (MultiblockControllerBase) meta;
    }

    private static final class WorldBlockRecord {
        private final IBlockState state;
        @Nullable
        private final NBTTagCompound tileNbt;

        private WorldBlockRecord(IBlockState state, @Nullable NBTTagCompound tileNbt) {
            this.state = state;
            this.tileNbt = tileNbt == null ? null : tileNbt.copy();
        }
    }
}
