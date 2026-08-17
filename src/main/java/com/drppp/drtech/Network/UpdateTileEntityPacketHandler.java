package com.drppp.drtech.Network;

import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityIndustrialApiary;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneProgrammer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraft.util.math.BlockPos;
import java.util.UUID;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateTileEntityPacketHandler implements IMessageHandler<UpdateTileEntityPacket, IMessage> {

    @Override
    public IMessage onMessage(UpdateTileEntityPacket message, MessageContext ctx) {
        IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
        mainThread.addScheduledTask(() -> {
            WorldServer serverWorld = ctx.getServerHandler().player.getServerWorld();
            TileEntity tileEntity = serverWorld.getTileEntity(message.getPos());
            NBTTagCompound nbt = message.getNbt();
            if (tileEntity instanceof IGregTechTileEntity && nbt.hasKey("industrialApiaryAction")) {
                MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                if (metaTileEntity instanceof MetaTileEntityIndustrialApiary) {
                    ((MetaTileEntityIndustrialApiary) metaTileEntity).handleApiaryClientAction(nbt.getString("industrialApiaryAction"));
                    tileEntity.markDirty();
                }
            }
            if (tileEntity instanceof IGregTechTileEntity && nbt.getBoolean("DroneWorldSelection")) {
                MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                if (metaTileEntity instanceof MetaTileEntityDroneProgrammer && nbt.hasKey("First", 4)) {
                    try {
                        UUID nodeId = UUID.fromString(nbt.getString("NodeId"));
                        BlockPos first = BlockPos.fromLong(nbt.getLong("First"));
                        BlockPos second = nbt.hasKey("Second", 4) ? BlockPos.fromLong(nbt.getLong("Second")) : null;
                        ((MetaTileEntityDroneProgrammer) metaTileEntity).applyWorldSelection(
                                ctx.getServerHandler().player, nodeId, first, second);
                    } catch (IllegalArgumentException ignored) {
                        // Invalid client UUIDs are rejected without mutating the program card.
                    }
                }
            }
        });
        return null;
    }
}
