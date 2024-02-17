package com.drppp.drtech.Utils;

import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Blocks.MetaBlocks.BlockFTTFPart;
import com.drppp.drtech.Blocks.MetaBlocks.BlockYotTankPart;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.IStoreData;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.ITfftData;
import gregtech.api.metatileentity.multiblock.IBatteryData;
import gregtech.common.blocks.BlockBatteryPart;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;


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
