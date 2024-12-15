package com.drppp.drtech.api.Muti;

import com.drppp.drtech.api.capability.IItemAndFluidHandler;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;

public class DrtMultiblockAbility {
    public static final MultiblockAbility<IItemAndFluidHandler> EXPORT_ITEM_FLUID = new MultiblockAbility("export_item_fluid");
    public static final MultiblockAbility<IItemAndFluidHandler> IMPORT_ITEM_FLUID = new MultiblockAbility("import_item_fluid");


}
