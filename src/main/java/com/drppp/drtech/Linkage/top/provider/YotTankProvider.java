package com.drppp.drtech.Linkage.top.provider;

import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.MetaTileEntityYotTank;
import com.drppp.drtech.Tags;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockLamp;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class YotTankProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Tags.MODID+":yot_tank_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData) {
        if (GTUtility.getMetaTileEntity(world,iProbeHitData.getPos()) instanceof MetaTileEntityYotTank) {
            var s = (MetaTileEntityYotTank)GTUtility.getMetaTileEntity(world,iProbeHitData.getPos());
            if(s.isActive() && s.isWorkingEnabled())
            {
                iProbeInfo.text("流体:"+s.getFluid().getLocalizedName());
                iProbeInfo.text("容量"+s.getFluidBank().getStored()+"/"+s.getFluidBank().getCapacity());
            }
        }
    }
}
