package com.drppp.drtech.api.metaTileEntity;

import com.meowmel.cropQT.api.capability.IFarmPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;

public class DrtechMultiblockAbility {

    public static final MultiblockAbility<IFarmPart> FARM_PART = new MultiblockAbility<>("farm_part", IFarmPart.class);
}
