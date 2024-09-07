package com.drppp.drtech.api.capability;

import appeng.api.storage.data.IAEFluidStack;
import gregtech.api.capability.IObjectHolder;
import gregtech.api.capability.IWorkable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.List;

public class DrtechCapabilities {
    public static final MultiblockAbility<IFluidTank> YOT_HATCH = new MultiblockAbility("yot_hatch");

    @CapabilityInject(INuclearDataShow.class)
    public static Capability<INuclearDataShow> CAPABILITY_NUCLEAR_DATA = null;

}
