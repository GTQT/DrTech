package com.drppp.drtech.common.command;

import com.drppp.drtech.common.enent.MobHordePlayerData;
import com.drppp.drtech.common.enent.MobHordeWorldData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

public class CommandHordeStop extends CommandBase {

    @NotNull
    @Override
    public String getName() {
        return "stop";
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "drtech.command.horde.stop.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;

            MobHordePlayerData playerData = MobHordeWorldData.get(player.world)
                    .getPlayerData(player.getPersistentID());

            if (!playerData.hasActiveInvasion) {
                ITextComponent textComponent = new TextComponentTranslation("drtech.command.horde.stop.has_active_no_invasion");
                sender.sendMessage(textComponent);
                return;
            }

            String invasion = playerData.currentInvasion;

            playerData.stopInvasion(player);

            ITextComponent textComponent = new TextComponentTranslation("drtech.command.horde.stop.stopped", invasion);
            sender.sendMessage(textComponent);
        }
    }

}