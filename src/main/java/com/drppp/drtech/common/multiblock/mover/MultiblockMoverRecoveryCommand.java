package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.api.multiblock.mover.MovableTileAdapterRegistry;
import com.drppp.drtech.api.multiblock.mover.IRotatableTileAdapter;
import com.drppp.drtech.api.multiblock.mover.MultiblockMovePermissionRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class MultiblockMoverRecoveryCommand extends CommandBase {
    @Override
    public String getName() {
        return "drtechmover";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.drtechmover.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length == 1 && "list".equalsIgnoreCase(args[0])) {
            list(server, sender);
            return;
        }
        if (args.length == 1 && "adapters".equalsIgnoreCase(args[0])) {
            sender.sendMessage(new TextComponentTranslation("commands.drtechmover.adapters",
                    MovableTileAdapterRegistry.view().size()));
            for (net.minecraft.util.ResourceLocation id : MovableTileAdapterRegistry.view().keySet()) {
                sender.sendMessage(new TextComponentTranslation("commands.drtechmover.adapter_entry",
                        id, MovableTileAdapterRegistry.view().get(id) instanceof IRotatableTileAdapter));
            }
            return;
        }
        if (args.length == 1 && "permissions".equalsIgnoreCase(args[0])) {
            sender.sendMessage(new TextComponentTranslation("commands.drtechmover.permissions",
                    MultiblockMovePermissionRegistry.view().size()));
            for (net.minecraft.util.ResourceLocation id : MultiblockMovePermissionRegistry.view().keySet()) {
                sender.sendMessage(new TextComponentTranslation("commands.drtechmover.registry_entry", id));
            }
            return;
        }
        if (args.length == 1 && "performance".equalsIgnoreCase(args[0])) {
            performance(sender);
            return;
        }
        if (args.length == 2 && "performance".equalsIgnoreCase(args[0])
                && "clear".equalsIgnoreCase(args[1])) {
            int cleared = MoverPerformanceMetrics.clear();
            DrTechMain.LOGGER.info("{} cleared {} multiblock mover performance samples",
                    sender.getName(), cleared);
            sender.sendMessage(new TextComponentTranslation(
                    "commands.drtechmover.performance_cleared", cleared));
            return;
        }
        if (args.length != 2) throw new CommandException(getUsage(sender));
        UUID transactionId;
        try {
            transactionId = UUID.fromString(args[1]);
        } catch (IllegalArgumentException error) {
            throw new CommandException("commands.drtechmover.invalid_id");
        }
        if ("info".equalsIgnoreCase(args[0])) {
            info(server, sender, transactionId);
        } else if ("restore".equalsIgnoreCase(args[0])) {
            restore(server, sender, transactionId);
        } else if ("discard".equalsIgnoreCase(args[0])) {
            if (!MultiblockRecoveryJournal.discard(server.getWorld(0), transactionId)) {
                throw new CommandException("commands.drtechmover.not_found");
            }
            DrTechMain.LOGGER.warn("{} discarded multiblock mover recovery journal {}",
                    sender.getName(), transactionId);
            sender.sendMessage(new TextComponentTranslation(
                    "commands.drtechmover.discarded", transactionId));
        } else {
            throw new CommandException(getUsage(sender));
        }
    }

    private static void list(MinecraftServer server, ICommandSender sender) throws CommandException {
        try {
            List<MultiblockRecoveryJournal.RecoveryEntry> entries =
                    MultiblockRecoveryJournal.list(server.getWorld(0));
            sender.sendMessage(new TextComponentTranslation(
                    "commands.drtechmover.list", entries.size()));
            for (MultiblockRecoveryJournal.RecoveryEntry entry : entries) {
                sender.sendMessage(new TextComponentTranslation("commands.drtechmover.entry",
                        entry.transactionId, entry.dimension, entry.sourceController,
                        entry.targetController, entry.positionCount));
            }
        } catch (IOException error) {
            throw new CommandException("commands.drtechmover.read_failed");
        }
    }

    private static void performance(ICommandSender sender) {
        MoverPerformanceMetrics.Summary summary = MoverPerformanceMetrics.summarize();
        sender.sendMessage(new TextComponentTranslation(
                "commands.drtechmover.performance_summary",
                summary.count, summary.successes, summary.failures, summary.rollbacks,
                summary.averageTotalMillis, summary.p95TotalMillis, summary.maxTotalMillis));
        sender.sendMessage(new TextComponentTranslation(
                "commands.drtechmover.performance_stages",
                summary.averageSelectionMillis, summary.averageCaptureMillis,
                summary.averageValidationMillis, summary.averageSnapshotMillis,
                summary.averageJournalMillis, summary.averageCommitMillis,
                summary.averageRollbackMillis));
    }

    private static void info(MinecraftServer server, ICommandSender sender, UUID transactionId)
            throws CommandException {
        try {
            MultiblockRecoveryJournal.RecoveryEntry entry =
                    MultiblockRecoveryJournal.inspect(server.getWorld(0), transactionId);
            if (entry == null) throw new CommandException("commands.drtechmover.not_found");
            sender.sendMessage(new TextComponentTranslation("commands.drtechmover.info",
                    entry.transactionId, entry.playerId, entry.dimension,
                    entry.sourceController, entry.targetController, entry.positionCount,
                    entry.rotation.getDegrees(),
                    DateFormat.getDateTimeInstance().format(new Date(entry.createdAt))));
        } catch (IOException error) {
            throw new CommandException("commands.drtechmover.read_failed");
        }
    }

    private static void restore(MinecraftServer server, ICommandSender sender, UUID transactionId)
            throws CommandException {
        MultiblockRecoveryJournal.RecoveryEntry entry;
        try {
            entry = MultiblockRecoveryJournal.inspect(server.getWorld(0), transactionId);
        } catch (IOException error) {
            throw new CommandException("commands.drtechmover.read_failed");
        }
        if (entry == null) throw new CommandException("commands.drtechmover.not_found");
        WorldServer world = server.getWorld(entry.dimension);
        if (world == null) throw new CommandException("commands.drtechmover.wrong_dimension");
        try {
            int restored = MultiblockRecoveryJournal.restore(world, transactionId);
            DrTechMain.LOGGER.warn("{} invoked disk recovery for multiblock mover transaction {}",
                    sender.getName(), transactionId);
            sender.sendMessage(new TextComponentTranslation(
                    "commands.drtechmover.restored", transactionId, restored));
        } catch (IOException error) {
            throw new CommandException("commands.drtechmover.restore_failed");
        } catch (MoverException error) {
            throw new CommandException(error.getTranslationKey());
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
                                           String[] args, BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "list", "info", "restore", "discard",
                    "adapters", "permissions", "performance");
        }
        if (args.length == 2) {
            if ("performance".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args, "clear");
            }
            try {
                List<MultiblockRecoveryJournal.RecoveryEntry> entries =
                        MultiblockRecoveryJournal.list(server.getWorld(0));
                String[] ids = new String[entries.size()];
                for (int i = 0; i < entries.size(); i++) ids[i] = entries.get(i).transactionId.toString();
                return getListOfStringsMatchingLastWord(args, ids);
            } catch (IOException ignored) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}
