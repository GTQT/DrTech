package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SaveHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SyncFailedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MultiblockRecoveryJournal {
    private static final int FORMAT_VERSION = 1;
    private static final String DIRECTORY = "drtech/multiblock_mover_recovery";
    private static final AtomicBoolean SYNC_FALLBACK_WARNING_LOGGED = new AtomicBoolean();

    private MultiblockRecoveryJournal() {
    }

    public static void prepare(WorldServer world, MoverSession session, BlockPos targetController,
                               Set<BlockPos> affected) throws IOException {
        NBTTagCompound root = new NBTTagCompound();
        root.setInteger("Version", FORMAT_VERSION);
        root.setUniqueId("Transaction", session.getId());
        root.setUniqueId("Player", session.getPlayerId());
        root.setLong("CreatedAt", System.currentTimeMillis());
        root.setInteger("Dimension", world.provider.getDimension());
        root.setLong("SourceController", session.getSnapshot().getControllerPos().toLong());
        root.setLong("TargetController", targetController.toLong());
        root.setInteger("Rotation", session.getRotation().getQuarterTurns());

        NBTTagList blocks = new NBTTagList();
        List<BlockPos> ordered = new ArrayList<>(affected);
        ordered.sort((left, right) -> {
            int y = Integer.compare(left.getY(), right.getY());
            if (y != 0) return y;
            int x = Integer.compare(left.getX(), right.getX());
            return x != 0 ? x : Integer.compare(left.getZ(), right.getZ());
        });
        for (BlockPos pos : ordered) {
            NBTTagCompound blockTag = new NBTTagCompound();
            blockTag.setLong("Pos", pos.toLong());
            blockTag.setTag("State", NBTUtil.writeBlockState(
                    new NBTTagCompound(), world.getBlockState(pos)));
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) blockTag.setTag("Tile", tile.writeToNBT(new NBTTagCompound()));
            blocks.appendTag(blockTag);
        }
        root.setTag("Blocks", blocks);
        writeAtomic(file(world, session.getId()), root);
        DrTechMain.LOGGER.info("Multiblock mover recovery journal {} prepared with {} positions at {}",
                session.getId(), affected.size(), file(world, session.getId()));
    }

    public static void complete(WorldServer world, UUID transactionId) throws IOException {
        File journal = file(world, transactionId);
        if (!journal.isFile()) return;
        File resolved = resolvedFile(world, transactionId);
        moveAtomic(journal, resolved);
        if (!resolved.delete()) {
            DrTechMain.LOGGER.warn("Completed multiblock mover journal is safely resolved but could not be deleted: {}",
                    resolved);
        }
    }

    public static List<RecoveryEntry> list(WorldServer overworld) throws IOException {
        File directory = directory(overworld);
        cleanupResolved(directory);
        File[] files = directory.listFiles((ignored, name) -> name.endsWith(".dat"));
        if (files == null || files.length == 0) return Collections.emptyList();
        List<RecoveryEntry> result = new ArrayList<>();
        for (File file : files) {
            try {
                result.add(readEntry(file));
            } catch (Exception error) {
                DrTechMain.LOGGER.error("Could not read multiblock mover recovery journal {}", file, error);
            }
        }
        result.sort((left, right) -> Long.compare(right.createdAt, left.createdAt));
        return result;
    }

    public static RecoveryEntry inspect(WorldServer overworld, UUID transactionId) throws IOException {
        File journal = file(overworld, transactionId);
        if (!journal.isFile()) return null;
        return readEntry(journal);
    }

    public static int restore(WorldServer world, UUID transactionId) throws IOException, MoverException {
        File journal = file(world, transactionId);
        if (!journal.isFile()) throw new MoverException("commands.drtechmover.not_found");
        NBTTagCompound root = read(journal);
        if (root.getInteger("Version") != FORMAT_VERSION) {
            throw new MoverException("commands.drtechmover.unsupported_version");
        }
        if (root.getInteger("Dimension") != world.provider.getDimension()) {
            throw new MoverException("commands.drtechmover.wrong_dimension");
        }

        NBTTagList blocks = root.getTagList("Blocks", 10);
        List<NBTTagCompound> records = new ArrayList<>(blocks.tagCount());
        for (int i = 0; i < blocks.tagCount(); i++) {
            NBTTagCompound record = blocks.getCompoundTagAt(i);
            BlockPos pos = BlockPos.fromLong(record.getLong("Pos"));
            if (!world.isBlockLoaded(pos)) {
                throw new MoverException("commands.drtechmover.chunk_unloaded");
            }
            records.add(record);
        }

        for (NBTTagCompound record : records) {
            BlockPos pos = BlockPos.fromLong(record.getLong("Pos"));
            if (world.getTileEntity(pos) != null) world.removeTileEntity(pos);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        }
        for (NBTTagCompound record : records) {
            BlockPos pos = BlockPos.fromLong(record.getLong("Pos"));
            IBlockState state = NBTUtil.readBlockState(record.getCompoundTag("State"));
            if (state.getBlock() != Blocks.AIR) world.setBlockState(pos, state, 2);
        }
        for (NBTTagCompound record : records) {
            if (!record.hasKey("Tile", 10)) continue;
            BlockPos pos = BlockPos.fromLong(record.getLong("Pos"));
            NBTTagCompound tileTag = record.getCompoundTag("Tile").copy();
            tileTag.setInteger("x", pos.getX());
            tileTag.setInteger("y", pos.getY());
            tileTag.setInteger("z", pos.getZ());
            TileEntity tile = TileEntity.create(world, tileTag);
            if (tile == null) throw new IOException("Could not recreate tile entity at " + pos);
            world.setTileEntity(pos, tile);
            tile.validate();
            tile.markDirty();
            MoverTileSyncService.queueInitialData(tile);
        }
        for (NBTTagCompound record : records) {
            BlockPos pos = BlockPos.fromLong(record.getLong("Pos"));
            MoverTileSyncService.notifyPosition(world, pos);
        }
        BlockPos sourceController = BlockPos.fromLong(root.getLong("SourceController"));
        TileEntity controllerTile = world.getTileEntity(sourceController);
        if (controllerTile instanceof IGregTechTileEntity) {
            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) controllerTile).getMetaTileEntity();
            if (metaTileEntity instanceof MultiblockControllerBase) {
                MultiblockControllerBase controller = (MultiblockControllerBase) metaTileEntity;
                controller.checkStructurePattern();
                if (!controller.isStructureFormed()) {
                    throw new IOException("Recovered GT controller did not form at " + sourceController);
                }
            }
        }
        complete(world, transactionId);
        DrTechMain.LOGGER.warn("Administrator restored multiblock mover transaction {} from disk; positions={}",
                transactionId, records.size());
        return records.size();
    }

    public static boolean discard(WorldServer overworld, UUID transactionId) {
        File journal = file(overworld, transactionId);
        return journal.isFile() && journal.delete();
    }

    private static RecoveryEntry readEntry(File file) throws IOException {
        NBTTagCompound root = read(file);
        return new RecoveryEntry(root.getUniqueId("Transaction"), root.getUniqueId("Player"),
                root.getLong("CreatedAt"), root.getInteger("Dimension"),
                BlockPos.fromLong(root.getLong("SourceController")),
                BlockPos.fromLong(root.getLong("TargetController")),
                MoverRotation.byQuarterTurns(root.getInteger("Rotation")),
                root.getTagList("Blocks", 10).tagCount());
    }

    private static NBTTagCompound read(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            return CompressedStreamTools.readCompressed(input);
        }
    }

    static void writeAtomic(File target, NBTTagCompound root) throws IOException {
        File directory = target.getParentFile();
        if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory()) {
            throw new IOException("Could not create recovery directory " + directory);
        }
        File temporary = new File(directory, target.getName() + ".tmp");
        IOException syncFailure = null;
        try (FileOutputStream output = new FileOutputStream(temporary)) {
            CompressedStreamTools.writeCompressed(root, output);
            output.flush();
            try {
                output.getChannel().force(true);
            } catch (IOException channelFailure) {
                try {
                    output.getFD().sync();
                } catch (SyncFailedException descriptorFailure) {
                    descriptorFailure.addSuppressed(channelFailure);
                    syncFailure = descriptorFailure;
                }
            }
        } catch (IOException writeFailure) {
            deleteTemporaryAfterFailure(temporary);
            throw writeFailure;
        }

        try {
            verifyJournal(temporary, root);
        } catch (IOException verificationFailure) {
            deleteTemporaryAfterFailure(temporary);
            throw verificationFailure;
        }
        if (syncFailure != null) {
            if (SYNC_FALLBACK_WARNING_LOGGED.compareAndSet(false, true)) {
                DrTechMain.LOGGER.warn("Recovery journal durability sync is unsupported on this "
                                + "filesystem/JVM; closed files will be fully verified before commit ({})",
                        syncFailure.toString());
            }
            DrTechMain.LOGGER.debug("Recovery journal sync fallback details for {}",
                    temporary, syncFailure);
        }
        moveAtomic(temporary, target);
        verifyJournal(target, root);
    }

    private static void verifyJournal(File file, NBTTagCompound expected) throws IOException {
        if (!file.isFile() || file.length() <= 0) {
            throw new IOException("Recovery journal was not written: " + file);
        }
        NBTTagCompound actual = read(file);
        if (!expected.equals(actual)) {
            throw new IOException("Recovery journal verification failed: " + file);
        }
    }

    private static void deleteTemporaryAfterFailure(File temporary) {
        if (temporary.isFile() && !temporary.delete()) {
            DrTechMain.LOGGER.warn("Could not clean failed recovery journal temporary file {}",
                    temporary);
        }
    }

    private static void moveAtomic(File source, File target) throws IOException {
        try {
            Files.move(source.toPath(), target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicFailure) {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void cleanupResolved(File directory) {
        File[] resolvedFiles = directory.listFiles((ignored, name) ->
                name.endsWith(".resolved") || name.endsWith(".dat.tmp"));
        if (resolvedFiles == null) return;
        for (File resolved : resolvedFiles) {
            if (!resolved.delete()) {
                DrTechMain.LOGGER.warn("Could not clean non-recoverable mover journal artifact {}",
                        resolved);
            }
        }
    }

    private static File file(WorldServer world, UUID transactionId) {
        return new File(directory(world), transactionId.toString() + ".dat");
    }

    private static File resolvedFile(WorldServer world, UUID transactionId) {
        return new File(directory(world), transactionId.toString() + ".resolved");
    }

    private static File directory(WorldServer world) {
        if (!(world.getSaveHandler() instanceof SaveHandler)) {
            throw new IllegalStateException("Unsupported world save handler: " + world.getSaveHandler());
        }
        return new File(((SaveHandler) world.getSaveHandler()).getWorldDirectory(), DIRECTORY);
    }

    public static final class RecoveryEntry {
        public final UUID transactionId;
        public final UUID playerId;
        public final long createdAt;
        public final int dimension;
        public final BlockPos sourceController;
        public final BlockPos targetController;
        public final MoverRotation rotation;
        public final int positionCount;

        private RecoveryEntry(UUID transactionId, UUID playerId, long createdAt, int dimension,
                              BlockPos sourceController, BlockPos targetController,
                              MoverRotation rotation, int positionCount) {
            this.transactionId = transactionId;
            this.playerId = playerId;
            this.createdAt = createdAt;
            this.dimension = dimension;
            this.sourceController = sourceController;
            this.targetController = targetController;
            this.rotation = rotation;
            this.positionCount = positionCount;
        }
    }
}
