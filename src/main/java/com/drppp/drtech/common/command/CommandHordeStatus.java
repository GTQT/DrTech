package com.drppp.drtech.common.command;

import com.drppp.drtech.common.enent.MobHordePlayerData;
import com.drppp.drtech.common.enent.MobHordeWorldData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.List;

public class CommandHordeStatus extends CommandBase {

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "drtech.command.horde.status.usage";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            if (args.length > 0) {
                String name = args[0];
                player = this.getPlayer(server, player, name);
            }

            MobHordePlayerData playerData = MobHordeWorldData.get(player.world)
                    .getPlayerData(player.getPersistentID());

            if (playerData.hasActiveInvasion) {
                ITextComponent textComponent = new TextComponentTranslation("drtech.command.horde.status.has_active_invasion", player.getDisplayName(), playerData.currentInvasion, playerData.ticksActive, playerData.timeoutPeriod - playerData.ticksActive);
                sender.sendMessage(textComponent);
            } else {
                ITextComponent textComponent = new TextComponentTranslation("drtech.command.horde.status.no_active_invasion", player.getDisplayName());
                sender.sendMessage(textComponent);
            }
        }
    }
}