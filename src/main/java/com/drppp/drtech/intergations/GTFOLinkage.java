package com.drppp.drtech.intergations;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static com.drppp.drtech.loaders.DrtechReceipes.LOG_CREATE;

public class GTFOLinkage {

    public static final String GTFO_ID = "gregtechfoodoption";
    public static void MachineRecipeInit()
    {
        String[] trees = new String[]
                {       "gregtechfoodoption:gtfo_sapling_0",
                        "gregtechfoodoption:gtfo_sapling_0:2",
                        "gregtechfoodoption:gtfo_sapling_0:4"
                        ,"gregtechfoodoption:gtfo_sapling_0:6"
                        ,"gregtechfoodoption:gtfo_sapling_0:8"
                        ,"gregtechfoodoption:gtfo_sapling_0:10"
                        ,"gregtechfoodoption:gtfo_sapling_0:12"
                        ,"gregtechfoodoption:gtfo_sapling_1:2"
                        ,"gregtechfoodoption:gtfo_sapling_1"};
        String[] results = new String[]
                {"gregtechfoodoption:gtfo_log_0","gregtechfoodoption:gtfo_meta_item:122",
                "gregtechfoodoption:gtfo_log_4","gregtechfoodoption:gtfo_meta_item:123",
                "gregtechfoodoption:gtfo_log_8","gregtechfoodoption:gtfo_meta_item:125",
                "gregtechfoodoption:gtfo_log_12","gregtechfoodoption:gtfo_meta_item:126",
                "gregtechfoodoption:gtfo_log_1","gregtechfoodoption:gtfo_meta_item:17",
                "gregtechfoodoption:gtfo_log_1:4","gregtechfoodoption:gtfo_meta_item:18",
                "gregtechfoodoption:gtfo_log_1:8","gregtechfoodoption:gtfo_meta_item:73",
                "gregtechfoodoption:gtfo_log_2","gregtechfoodoption:gtfo_meta_item:271",
                "gregtechfoodoption:gtfo_log_2:4","gregtechfoodoption:gtfo_meta_item:342"
        };
        int k=0;
        for (int i = 0; i < trees.length; i++) {
            int inmeta=0,outmeta=0,outmeta1=0;
            String[] inids = trees[i].split(":");
            String[] outids = results[k].split(":");
            String[] outids1 = results[k+1].split(":");
            if(inids.length>2)
                inmeta = (int)Integer.parseInt(inids[2]);
            if(outids.length>2)
                outmeta = (int)Integer.parseInt(outids[2]);
            if(outids1.length>2)
                outmeta1 = (int)Integer.parseInt(outids1[2]);
            LOG_CREATE.recipeBuilder()
                    .notConsumable(new ItemStack(Block.getBlockFromName(inids[0]+":"+inids[1]),1,inmeta))
                    .outputs(new ItemStack(Block.getBlockFromName(outids[0]+":"+outids[1]),1,outmeta))
                    .outputs(new ItemStack(Item.getByNameOrId(outids1[0]+":"+outids1[1]),1,outmeta1))
                    .EUt(30)
                    .duration(100)
                    .buildAndRegister();
            k+=2;
        }

    }

}
