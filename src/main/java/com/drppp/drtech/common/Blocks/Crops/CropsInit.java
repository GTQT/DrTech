package com.drppp.drtech.common.Blocks.Crops;

import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraftforge.event.RegistryEvent;

public class CropsInit {
    public static final DtrechCommonCrop REDSTONE_CROP = new DtrechCommonCrop("red_stone", ItemsInit.ITEM_RED_STONE_SEED, Items.REDSTONE);
    public static final DtrechCommonCrop LAPIS_CROP = new DtrechCommonCrop("lapis", ItemsInit.ITEM_LAPIS_SEED, Items.DYE);
    public static final DtrechCommonCrop FLU_CROP = new DtrechCommonCrop("flu", ItemsInit.ITEM_FLU_SEED, Items.GLOWSTONE_DUST);

    public static void init(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(REDSTONE_CROP);
        event.getRegistry().register(LAPIS_CROP);
        event.getRegistry().register(FLU_CROP);
    }
}
