package com.drppp.drtech.Blocks.Crops;

import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Items;
import net.minecraftforge.event.RegistryEvent;

import java.util.ArrayList;
import java.util.List;

public class CropsInit {
    public static final DtrechCommonCrop REDSTONE_CROP = new DtrechCommonCrop("red_stone", MyMetaItems.REDSTONE_SEED.getMetaItem(), Items.REDSTONE);
    public static void init(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(REDSTONE_CROP);
    }
}
