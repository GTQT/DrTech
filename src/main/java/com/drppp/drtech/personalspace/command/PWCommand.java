package com.drppp.drtech.personalspace.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import org.jetbrains.annotations.Nullable;

import com.drppp.drtech.personalspace.proxy.CommonProxy;
import com.drppp.drtech.personalspace.world.PWTeleporter;

public class PWCommand extends CommandBase {

    @Override
    public String getName() {
        return "pworlds";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.pworlds.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || sender == null)
            throw new WrongUsageException("commands.pworlds.usage");

        switch (args[0].toLowerCase()) {
            case "ls": {
                CommonProxy.getDimensionConfigs(false).forEachEntry((dimID, dimCFG) -> {
                    if (dimCFG == null)
                        return true;

                    sender.sendMessage(
                            new TextComponentTranslation(String.format("%d: %s", dimID, dimCFG.cloudsEnabled())));
                    return true;
                });
                return;
            }
            case "where": {
                if (args.length < 2) throw new WrongUsageException("commands.pworlds.usage");
                EntityPlayerMP player = getPlayer(server, sender, args[1]);
                sender.sendMessage(new TextComponentTranslation("commands.pworlds.where", player.getName(),
                        player.getEntityWorld().provider.getDimension()));
                return;
            }
            case "tpx": {
                if (args.length < 3) throw new WrongUsageException("commands.pworlds.usage");
                EntityPlayerMP player = getPlayer(server, sender, args[1]);
                int dim = parseInt(args[2]);
                if (!DimensionManager.isDimensionRegistered(dim))
                    throw new CommandException("commands.pworlds.badDimension");
                WorldServer dimWorld = DimensionManager.getWorld(dim);
                if (dimWorld == null) {
                    DimensionManager.initDimension(dim);
                    dimWorld = DimensionManager.getWorld(dim);
                    if (dimWorld == null) throw new CommandException("commands.pworlds.badDimension");
                }
                BlockPos target = dimWorld.getSpawnCoordinate();
                target = new BlockPos(target.getX(), dimWorld.getTopSolidOrLiquidBlock(target).getY() + 1,
                        target.getZ());
                if (args.length >= 6) {
                    target = sender.getPosition();
                }
                PWTeleporter tp = new PWTeleporter(dimWorld, target);
                player.changeDimension(dim, tp);
                sender.sendMessage(new TextComponentTranslation("commands.pworlds.tpx", player.getName(), dim,
                        target.getX(), target.getY(), target.getZ()));
                return;
            }
            case "give-portal": {
                if (args.length > 3) throw new WrongUsageException("commands.pworlds.usage");
                EntityPlayerMP player = getPlayer(server, sender, args[1]);
                int dim = parseInt(args[2]);
                if (!DimensionManager.isDimensionRegistered(dim))
                    throw new CommandException("commands.pworlds.badDimension");
                WorldServer dimWorld = DimensionManager.getWorld(dim);
                if (dimWorld == null) {
                    DimensionManager.initDimension(dim);
                    dimWorld = DimensionManager.getWorld(dim);
                    if (dimWorld == null) throw new CommandException("commands.pworlds.badDimension");
                }
                BlockPos target = dimWorld.getSpawnCoordinate();
                target = new BlockPos(target.getX(), dimWorld.getTopSolidOrLiquidBlock(target).getY() + 1,
                        target.getZ());
                if (args.length >= 6) {
                    target = sender.getPosition();
                }
                ItemStack item = new ItemStack(CommonProxy.itemBlockPersonalPortal, 1, 0);
                NBTTagCompound compound = new NBTTagCompound();
                compound.setBoolean("active", true);
                compound.setIntArray("target",
                        new int[] { dim, target.getX(), target.getY(), target.getZ() });
                item.setTagCompound(compound);
                EntityItem eItem = player.dropItem(item, false);
                eItem.setPickupDelay(0);
                return;
            }
        }

        throw new WrongUsageException("commands.pworlds.usage");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        return switch (args.length) {
            case 0, 1 -> getListOfStringsMatchingLastWord(
                    args,
                    "ls",
                    "where",
                    "tpx",
                    "give-portal");
            case 2 -> getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            default -> null;
        };
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 1;
    }
}
