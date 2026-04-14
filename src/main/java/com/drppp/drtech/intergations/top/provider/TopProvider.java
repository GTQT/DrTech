package com.drppp.drtech.intergations.top.provider;

import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileCropStick;
import com.drppp.drtech.api.crop.CropType;
import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import gregtech.api.util.GTUtility;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class TopProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Tags.MODID+":top_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData)
    {
        if (GTUtility.getMetaTileEntity(world,iProbeHitData.getPos()) instanceof MetaTileEntityYotTank) {
            var s = (MetaTileEntityYotTank)GTUtility.getMetaTileEntity(world,iProbeHitData.getPos());
            if(s.isActive() && s.isWorkingEnabled())
            {
                iProbeInfo.text("流体:"+s.getFluid().getLocalizedName());
                iProbeInfo.text("容量"+s.getFluidBank().getStored()+"/"+s.getFluidBank().getCapacity());
            }
        }
        if(world.getTileEntity(iProbeHitData.getPos()) instanceof TileCropStick)
        {
            TileCropStick tile = (TileCropStick)world.getTileEntity(iProbeHitData.getPos());
            CropType type = tile.getCropType();
            String name = type != null ? type.getDisplayName() : tile.getCropId();
            iProbeInfo.text(new TextComponentString(
                    TextFormatting.GREEN + "作物: " + TextFormatting.WHITE + name).getFormattedText());
            if(entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getItem()== ItemsInit.CROP_ANALYZER)
            {
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.GREEN + "Tier: " + TextFormatting.WHITE +
                                (type != null ? type.getTier() : "?")).getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.GREEN + "阶段: " + TextFormatting.WHITE +
                                tile.getGrowthStage() + "/" +
                                (type != null ? type.getMaxGrowthStage() : "?")).getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.GOLD + "--- 属性 ---").getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.RED + "Growth:     " +
                                TextFormatting.WHITE + " " + tile.getStats().getGrowth()).getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.YELLOW + "Gain:       " +
                                TextFormatting.WHITE + " " + tile.getStats().getGain()).getFormattedText());
                iProbeInfo.text(new TextComponentString(
                        TextFormatting.BLUE + "Resistance: " +
                                TextFormatting.WHITE + " " + tile.getStats().getResistance()).getFormattedText());
            }
        }
    }
}
