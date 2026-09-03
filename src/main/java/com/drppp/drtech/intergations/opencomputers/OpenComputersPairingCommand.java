package com.drppp.drtech.intergations.opencomputers;

import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneDock;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Player-presence proof for creating and revoking OC credentials. */
public final class OpenComputersPairingCommand extends CommandBase {
    @Override public String getName() { return "drtechoc"; }
    @Override public String getUsage(ICommandSender sender) { return "commands.drtechoc.usage"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) throw new CommandException("commands.drtechoc.player_only");
        EntityPlayerMP player = (EntityPlayerMP) sender;
        if (args.length != 1 || !("pair".equalsIgnoreCase(args[0]) || "revoke".equalsIgnoreCase(args[0])
                || "status".equalsIgnoreCase(args[0]))) throw new CommandException(getUsage(sender));
        RayTraceResult hit = player.rayTrace(8.0D, 1.0F);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            throw new CommandException("commands.drtechoc.no_machine");
        }
        BlockPos position = hit.getBlockPos();
        MetaTileEntity machine = OpenComputersMachineAccess.getMachine(player.world, position);
        String component = OpenComputersMachineAccess.componentFor(machine);
        if (component == null) throw new CommandException("commands.drtechoc.no_machine");
        UUID owner = player.getUniqueID();
        if (machine instanceof MetaTileEntityDroneDock
                && !((MetaTileEntityDroneDock) machine).claimForOwner(owner)) {
            throw new CommandException("commands.drtechoc.denied");
        }
        OpenComputersPairingState state = OpenComputersPairingState.get(player.world);
        int dimension = player.world.provider.getDimension();
        if ("status".equalsIgnoreCase(args[0])) {
            player.sendMessage(new TextComponentTranslation(state.isPaired(dimension, position, component)
                    ? "commands.drtechoc.status.paired" : "commands.drtechoc.status.unpaired", component));
            return;
        }
        if ("revoke".equalsIgnoreCase(args[0])) {
            if (!state.revoke(dimension, position, component, owner)) {
                throw new CommandException("commands.drtechoc.denied");
            }
            player.sendMessage(new TextComponentTranslation("commands.drtechoc.revoked", component));
            return;
        }
        String token = state.rotate(dimension, position, component, owner, player.world.getTotalWorldTime());
        if (token.isEmpty()) throw new CommandException("commands.drtechoc.denied");
        player.sendMessage(new TextComponentTranslation("commands.drtechoc.paired", component));
        player.sendMessage(new TextComponentTranslation("commands.drtechoc.token", token));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "pair", "revoke", "status")
                : Collections.emptyList();
    }
}
