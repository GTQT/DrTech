package com.drppp.drtech.personalspace.compat;

import com.drppp.drtech.Tags;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.*;
import com.drppp.drtech.personalspace.blocks.tile.TilePersonalPortal;

public class TheOneProbeCompat implements IProbeInfoProvider {

    public void init() {
        ITheOneProbe theOneProbe = TheOneProbe.theOneProbeImp;
        theOneProbe.registerProvider(this);
    }

    @Override
    public String getID() {
        return Tags.MODID;
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world,
                             IBlockState iBlockState, IProbeHitData iProbeHitData) {
        BlockPos pos = iProbeHitData.getPos();
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TilePersonalPortal tpp) {
            if (tpp.isActive()) {
                iProbeInfo.text("Portal is active");
                iProbeInfo.text(String.format("Dimension: %s", tpp.getTargetID()));
                iProbeInfo.text(String.format("Position: %s", tpp.getTargetPos()));
            } else {
                iProbeInfo.text("Portal is not active");
            }
        }
    }
}
