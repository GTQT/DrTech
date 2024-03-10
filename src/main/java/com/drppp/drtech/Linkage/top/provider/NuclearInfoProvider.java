package com.drppp.drtech.Linkage.top.provider;

import com.drppp.drtech.MetaTileEntities.muti.ecectric.generator.NuclearReactor;
import com.drppp.drtech.Tags;
import com.drppp.drtech.api.capability.DrtechCapabilities;
import com.drppp.drtech.api.capability.INuclearDataShow;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.util.GTUtility;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
        return GTValues.MODID + ":controllable_provider";
    }

    @Override
    protected void addProbeInfo(INuclearDataShow iNuclearDataShow, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, TileEntity tileEntity, IProbeHitData iProbeHitData) {
    }
}
