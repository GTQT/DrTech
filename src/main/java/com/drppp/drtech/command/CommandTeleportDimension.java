package com.drppp.drtech.command;

import com.drppp.drtech.Network.DimensionTeleporter;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandTeleportDimension extends CommandBase {
    @Override
    public String getName() {
        return "tpdim";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/tpdim <维度ID> [x] [y] [z]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            // 解析参数
            int dimension = parseInt(args[0]);
            BlockPos pos = args.length >= 3
                    ? new BlockPos(parseInt(args[1]), parseInt(args[2]), parseInt(args[3]))
                    : null;

            // 执行传送
            DimensionTeleporter.teleportToDimension(player, dimension, pos);
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayerMP
                && sender.canUseCommand(4, this.getName());
    }
}