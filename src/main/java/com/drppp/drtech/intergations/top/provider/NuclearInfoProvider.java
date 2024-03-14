package com.drppp.drtech.intergations.top.provider;

import com.drppp.drtech.api.capability.DrtechCapabilities;
import com.drppp.drtech.api.capability.INuclearDataShow;
import gregtech.api.GTValues;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

public class NuclearInfoProvider extends CapabilityInfoProvider<INuclearDataShow> {

    @NotNull
    @Override
    protected Capability<INuclearDataShow> getCapability() {
        return DrtechCapabilities.CAPABILITY_NUCLEAR_DATA;
    }

    @Override
    public String getID() {
        return GTValues.MODID + ":nuclear_data_show_provider";
    }

    @Override
    protected void addProbeInfo(INuclearDataShow iNuclearDataShow, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, TileEntity tileEntity, IProbeHitData iProbeHitData) {
        iProbeInfo.text(TextStyleClass.WARNING + "热量:"+iNuclearDataShow.getHeat()+"/"+iNuclearDataShow.getMaxHeat());
        iProbeInfo.text(TextStyleClass.INFO + "发电功率:"+iNuclearDataShow.getEnergyOut()+"Eu/t");
    }
}
