package com.drppp.drtech.api.modularui;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DrtMetaTileEntityGuiFactory  extends AbstractUIFactory<DrtPosGuiData> {
    public static final DrtMetaTileEntityGuiFactory INSTANCE = new DrtMetaTileEntityGuiFactory();

    private DrtMetaTileEntityGuiFactory() {
        super("drt:metatile");
        GuiManager.registerFactory(this);
    }

    public static <T extends MetaTileEntity & IGuiHolder<DrtPosGuiData>> void open(EntityPlayer player, T tile) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(tile);
        if (!tile.isValid()) {
            throw new IllegalArgumentException("Can't open invalid MetaTileEntity GUI!");
        } else if (player.world != tile.getWorld()) {
            throw new IllegalArgumentException("MetaTileEntity must be in same dimension as the player!");
        } else {
            BlockPos pos = tile.getPos();
            DrtPosGuiData data = new DrtPosGuiData(player, pos.getX(), pos.getY(), pos.getZ());
            GuiManager.open(INSTANCE, data, (EntityPlayerMP)player);
        }
    }

    public static void open(EntityPlayer player, BlockPos pos) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(pos);
        DrtPosGuiData data = new DrtPosGuiData(player, pos.getX(), pos.getY(), pos.getZ());
        GuiManager.open(INSTANCE, data, (EntityPlayerMP)player);
    }

    public @NotNull IGuiHolder<DrtPosGuiData> getGuiHolder(DrtPosGuiData data) {
        var s = this.castGuiHolder(data.getTileEntity());
        return (IGuiHolder)Objects.requireNonNull(s, "Found TileEntity is not a gui holder!");
    }

    public void writeGuiData(DrtPosGuiData guiData, PacketBuffer buffer) {
        buffer.writeVarInt(guiData.getX());
        buffer.writeVarInt(guiData.getY());
        buffer.writeVarInt(guiData.getZ());
    }

    public @NotNull DrtPosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new DrtPosGuiData(player, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }
}
