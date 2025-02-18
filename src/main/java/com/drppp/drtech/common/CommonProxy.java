package com.drppp.drtech.common;

import com.drppp.drtech.Tags;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class CommonProxy {
    public void preLoad(){

    }
    public void registerItemRenderer(Item item, int meta, String id) {

    }
}