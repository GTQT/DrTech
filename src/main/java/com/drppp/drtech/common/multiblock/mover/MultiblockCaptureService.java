package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.api.multiblock.mover.MovableTileAdapterRegistry;
import com.drppp.drtech.api.multiblock.mover.MultiblockMovePermissionRegistry;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.CommittedStructureGraph;
import gregtech.api.pattern.StructureIterateResult;
import gregtech.api.pattern.StructureOperationRequest;
import gregtech.api.pattern.StructureOrientation;
import gregtech.api.pattern.StructureRuntime;
import gregtech.api.util.BlockInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MultiblockCaptureService {

    private MultiblockCaptureService() {
    }

    public static MultiblockSnapshot capture(EntityPlayerMP player, BlockPos controllerPos,
                                             boolean requireInactive) throws MoverException {
        WorldServer world = player.getServerWorld();
        TileEntity tile = world.getTileEntity(controllerPos);
        if (!(tile instanceof IGregTechTileEntity)) {
            throw new MoverException("drtech.multiblock_mover.error.not_controller");
        }

        MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tile).getMetaTileEntity();
        if (!(metaTileEntity instanceof MultiblockControllerBase)) {
            throw new MoverException("drtech.multiblock_mover.error.not_controller");
        }

        MultiblockControllerBase controller = (MultiblockControllerBase) metaTileEntity;
        if (!controller.canBeModifiedBy(player)) {
            throw new MoverException("drtech.multiblock_mover.error.no_permission");
        }
        ensureFormed(controller);
        if (requireInactive && (controller.isActive()
                || controller.getRecipeLogic() != null && controller.getRecipeLogic().isWorking())) {
            throw new MoverException("drtech.multiblock_mover.error.active");
        }

        Set<BlockPos> positions = collectPatternPositions(world, controller);
        positions.add(controllerPos.toImmutable());
        validateSpan(positions);
        List<MovingBlockSnapshot> blocks = new ArrayList<>(positions.size());
        for (BlockPos worldPos : positions) {
            if (!world.isBlockLoaded(worldPos)) {
                throw new MoverException("drtech.multiblock_mover.error.chunk_unloaded");
            }
            IBlockState state = world.getBlockState(worldPos);
            if (state.getBlock() == Blocks.AIR || state.getBlock().isAir(state, world, worldPos)) {
                continue;
            }
            if (!MultiblockMovePermissionRegistry.canRemove(player, world, worldPos)) {
                throw new MoverException("drtech.multiblock_mover.error.no_permission");
            }

            TileEntity blockTile = world.getTileEntity(worldPos);
            NBTTagCompound tileNbt = null;
            ResourceLocation tileAdapterId = null;
            if (blockTile != null) {
                try {
                    MovableTileAdapterRegistry.AdapterEntry adapter =
                            MovableTileAdapterRegistry.find(blockTile);
                    if (adapter == null) {
                        throw new MoverException("drtech.multiblock_mover.error.unsupported_tile");
                    }
                    if (!adapter.getAdapter().canMove(player, blockTile)) {
                        throw new MoverException("drtech.multiblock_mover.error.no_permission");
                    }
                    tileNbt = adapter.getAdapter().capture(blockTile);
                    if (tileNbt == null) {
                        throw new MoverException("drtech.multiblock_mover.error.unsupported_tile");
                    }
                    tileAdapterId = adapter.getId();
                } catch (MoverException error) {
                    throw error;
                } catch (Throwable adapterFailure) {
                    DrTechMain.LOGGER.error("Multiblock mover tile adapter failed at {} for {}",
                            worldPos, blockTile.getClass().getName(), adapterFailure);
                    throw new MoverException("drtech.multiblock_mover.error.adapter_failed",
                            adapterFailure);
                }
            }

            blocks.add(new MovingBlockSnapshot(
                    worldPos.subtract(controllerPos), state, tileNbt, tileAdapterId,
                    worldPos.equals(controllerPos)));
        }

        if (blocks.isEmpty()) {
            throw new MoverException("drtech.multiblock_mover.error.empty_pattern");
        }
        if (blocks.size() > DrtConfig.MultiblockMover.maxBlocks) {
            throw new MoverException("drtech.multiblock_mover.error.too_large");
        }
        return new MultiblockSnapshot(
                world.provider.getDimension(), controllerPos,
                controller.getFrontFacingForStructure(), controller.getUpwardsFacing(),
                controller.isFlipped(), blocks);
    }

    private static Set<BlockPos> collectPatternPositions(WorldServer world,
                                                          MultiblockControllerBase controller)
            throws MoverException {
        Set<BlockPos> positions = new HashSet<>();
        collectCommittedPositions(controller, positions);
        collectRuntimePositions(world, controller, positions);
        collectAttachedParts(controller, positions);

        // Compatibility fallback for legacy patterns which do not publish the
        // exact formed-position graph used by repeatable and multi-piece patterns.
        if (controller.structurePattern != null && controller.structurePattern.getCache() != null) {
            for (Long packed : controller.structurePattern.getCache().keySet()) {
                positions.add(BlockPos.fromLong(packed));
            }
        }

        // Legacy repeatable patterns keep the matched repetition counts even when
        // their position cache/committed graph is not published. Use those counts
        // before a new check, since rechecking is what produced the false
        // "not formed" result on otherwise active variable-length controllers.
        if (positions.isEmpty() && controller.structurePattern != null) {
            Map<BlockPos, BlockInfo> formedBlocks = controller.structurePattern.getAllStructureBlocks(
                    world, controller.getPos(), controller.getFrontFacingForStructure(),
                    controller.getUpwardsFacing(), controller.isFlipped());
            positions.addAll(formedBlocks.keySet());
        }

        if (positions.isEmpty()) {
            controller.checkStructurePattern();
            if (!isFormed(controller)) {
                throw new MoverException("drtech.multiblock_mover.error.not_formed");
            }
            collectCommittedPositions(controller, positions);
            collectRuntimePositions(world, controller, positions);
            collectAttachedParts(controller, positions);
            if (controller.structurePattern != null && controller.structurePattern.getCache() != null) {
                for (Long packed : controller.structurePattern.getCache().keySet()) {
                    positions.add(BlockPos.fromLong(packed));
                }
            }
        }

        if (positions.isEmpty() && controller.structurePattern != null) {
            Map<BlockPos, BlockInfo> allBlocks = controller.structurePattern.getAllStructureBlocks(
                    world, controller.getPos(), controller.getFrontFacingForStructure(),
                    controller.getUpwardsFacing(), controller.isFlipped());
            positions.addAll(allBlocks.keySet());
        }
        return positions;
    }

    private static void collectCommittedPositions(MultiblockControllerBase controller,
                                                  Set<BlockPos> positions) {
        StructureRuntime runtime = controller.getStructureRuntime();
        if (runtime == null) return;
        CommittedStructureGraph graph = runtime.getCommittedGraph();
        if (graph == null || graph.getPositionIndex() == null) return;
        for (Long packed : graph.getPositionIndex().getAllFormedPositions()) {
            positions.add(BlockPos.fromLong(packed));
        }
    }

    /**
     * GTCEu's committed position graph intentionally omits some opaque ability
     * cells. The formed controller linkage remains authoritative for hatches,
     * buses and other multiblock parts, so always union those positions into the
     * movement snapshot.
     */
    private static void collectAttachedParts(MultiblockControllerBase controller,
                                             Set<BlockPos> positions) {
        for (IMultiblockPart part : controller.getMultiblockParts()) {
            if (part instanceof MetaTileEntity) {
                positions.add(((MetaTileEntity) part).getPos().toImmutable());
            }
        }
        StructureRuntime runtime = controller.getStructureRuntime();
        if (runtime == null || runtime.getLifecycleState() == null) return;
        for (IMultiblockPart part : runtime.getLifecycleState().getParts()) {
            if (part instanceof MetaTileEntity) {
                positions.add(((MetaTileEntity) part).getPos().toImmutable());
            }
        }
    }

    /**
     * Enumerates the active declarative structure. This is the authoritative
     * fallback for GTCEu multi-piece definitions (including repeatable pieces),
     * which do not necessarily expose the deprecated structurePattern cache.
     */
    private static void collectRuntimePositions(WorldServer world,
                                                MultiblockControllerBase controller,
                                                Set<BlockPos> positions) {
        StructureRuntime runtime = controller.getStructureRuntime();
        if (runtime == null) return;
        try {
            StructureOperationRequest request = StructureOperationRequest.iterate(
                    world, controller.getPos(), StructureOrientation.fromController(controller), controller);
            StructureIterateResult result = runtime.getMultiPiecePattern() == null
                    ? runtime.iterateSingleResult(request)
                    : runtime.iterateMultiPiece(request);
            if (!result.visited()) return;
            if (result.getPositions() != null) {
                for (Long packed : result.getPositions()) {
                    positions.add(BlockPos.fromLong(packed));
                }
            }
            if (result.getBlocks() != null) {
                positions.addAll(result.getBlocks().keySet());
            }
        } catch (RuntimeException iterationFailure) {
            DrTechMain.LOGGER.warn("Could not enumerate GTCEu structure runtime at {}; using compatibility fallbacks",
                    controller.getPos(), iterationFailure);
        }
    }

    private static void ensureFormed(MultiblockControllerBase controller) throws MoverException {
        if (isFormed(controller)) return;
        // Some declarative/repeatable controllers have a stale lifecycle state
        // until their scheduled server check runs. A synchronous check is safe
        // here because capture is read-only and requires a formed structure.
        controller.checkStructurePattern();
        if (!isFormed(controller)) {
            StructureRuntime runtime = controller.getStructureRuntime();
            DrTechMain.LOGGER.warn("Multiblock mover found unformed controller {} at {}; runtime={}, failure={}",
                    controller.getClass().getName(), controller.getPos(),
                    runtime == null ? "missing" : runtime.describeShape(),
                    runtime == null ? "unavailable" : runtime.getLastFailure());
            throw new MoverException("drtech.multiblock_mover.error.not_formed");
        }
    }

    private static boolean isFormed(MultiblockControllerBase controller) {
        if (controller.isStructureFormed()) return true;
        StructureRuntime runtime = controller.getStructureRuntime();
        boolean runtimeFormed = runtime != null
                && runtime.getLifecycleState() != null
                && runtime.getLifecycleState().isFormed();
        if (runtimeFormed) {
            DrTechMain.LOGGER.debug("Using GTCEu lifecycle state for formed controller at {}",
                    controller.getPos());
        }
        return runtimeFormed;
    }

    private static void validateSpan(Set<BlockPos> positions) throws MoverException {
        if (positions.isEmpty()) {
            throw new MoverException("drtech.multiblock_mover.error.empty_pattern");
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        int limit = DrtConfig.MultiblockMover.maxAxisLength;
        if (maxX - minX + 1 > limit || maxY - minY + 1 > limit || maxZ - minZ + 1 > limit) {
            throw new MoverException("drtech.multiblock_mover.error.too_large");
        }
    }
}
