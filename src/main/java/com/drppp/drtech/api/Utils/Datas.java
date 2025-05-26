package com.drppp.drtech.api.Utils;

import com.drppp.drtech.common.Blocks.BlocksInit;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockFTTFPart;
import com.drppp.drtech.common.Blocks.MetaBlocks.BlockYotTankPart;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.IStoreData;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.ITfftData;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;

import java.util.HashMap;
import java.util.Map;


public class Datas {
    public static final Object2ObjectMap<IBlockState, IStoreData> YOT_CASINGS = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<IBlockState, ITfftData> TFFT_CASINGS = new Object2ObjectOpenHashMap<>();
    public static void init()
    {
        for (BlockYotTankPart.BlockYotTankPartType type : BlockYotTankPart.BlockYotTankPartType.values()) {
            YOT_CASINGS.put(BlocksInit.YOT_TANK.getState(type), type);
        }
        for (BlockFTTFPart.BlockYotTankPartType type : BlockFTTFPart.BlockYotTankPartType.values()) {
            TFFT_CASINGS.put(BlocksInit.TFFT_TANK.getState(type), type);
        }

    }
}
