package com.drppp.drtech.api.utils;

import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.common.blocks.metaBlocks.BlockFTTFPart;
import com.drppp.drtech.common.metaTileEntities.muti.electric.store.ITFFTData;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;


public class Datas {

    public static final Object2ObjectMap<IBlockState, ITFFTData> TFFT_CASINGS = new Object2ObjectOpenHashMap<>();
    public static void init()
    {
        for (BlockFTTFPart.TFFTPartType type : BlockFTTFPart.TFFTPartType.values()) {
            TFFT_CASINGS.put(BlocksInit.TFFT_TANK.getState(type), type);
        }
    }
}
