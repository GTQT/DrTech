package com.drppp.drtech.api.Muti;

import com.drppp.drtech.api.capability.IAssembly;
import gregtech.api.capability.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IPassthroughHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrtMultiblockAbility {
    public static MultiblockAbility<IAssembly> EXPORT_ASSEMBLY; //= new MultiblockAbility<>("export_assembly");
    public static  MultiblockAbility<IAssembly> IMPORT_ASSEMBLY; //= new MultiblockAbility<>("import_assembly");

}
