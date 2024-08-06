package com.drppp.drtech.api.modularui;

import com.cleanroommc.modularui.factory.GuiData;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DrtPosGuiData extends GuiData {
    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private final int x;
    private final int y;
    private final int z;

    public DrtPosGuiData(EntityPlayer player, int x, int y, int z) {
        super(player);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() {
        return this.getPlayer().world;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public MetaTileEntity getTileEntity() {
        pos.setPos(this.x, this.y, this.z);
        return GTUtility.getMetaTileEntity(getWorld(),getBlockPos());
    }
}

