package com.drppp.drtech.Linkage.top.provider;

import com.drppp.drtech.Tags;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.blocks.BlockLamp;
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
        if (iBlockState instanceof MetaTileEntity) {
            BlockLamp lamp = (BlockLamp) iBlockState.getBlock();
            boolean inverted = lamp.isInverted(iBlockState);
            boolean bloomEnabled = lamp.isBloomEnabled(iBlockState);
            boolean lightEnabled = lamp.isLightEnabled(iBlockState);

            if (inverted) iProbeInfo.text("{*tile.gregtech_lamp.tooltip.inverted*}");
            if (!bloomEnabled) iProbeInfo.text("{*tile.gregtech_lamp.tooltip.no_bloom*}");
            if (!lightEnabled) iProbeInfo.text("{*tile.gregtech_lamp.tooltip.no_light*}");
        }
    }
}
