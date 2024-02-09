package com.drppp.drtech.Utils;

import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.BlockYotTankPart;
import gregtech.api.metatileentity.multiblock.IBatteryData;
import gregtech.common.blocks.BlockBatteryPart;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;


public class Datas {
    public static final Object2ObjectMap<IBlockState, IBatteryData> YOT_CASINGS = new Object2ObjectOpenHashMap<>();
    public static void init()
    {
        for (BlockYotTankPart.BlockYotTankPartType type : BlockYotTankPart.BlockYotTankPartType.values()) {
            YOT_CASINGS.put(BlocksInit.YOT_TANK.getState(type), type);
        }
    }
}
