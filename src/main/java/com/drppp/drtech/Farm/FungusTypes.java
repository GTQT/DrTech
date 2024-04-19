package com.drppp.drtech.Farm;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FungusTypes {
    public static  List<FungusType> allType ;
    public static  FungusType COMMON;
//    public static final FungusType DIRT = new FungusType();
//    public static final FungusType SPORE = new FungusType();
//    public static final FungusType CLAY = new FungusType();
//    public static final FungusType SWEET = new FungusType();
//    public static final FungusType NETHER = new FungusType();
//    public static final FungusType ENDER = new FungusType();
//    public static final FungusType FLICKER = new FungusType();
    public static void init()
    {
        allType = new ArrayList<>();
        COMMON = new FungusType(
                "Common",1,1,1,3,15,null,new ArrayList<FungusDrop>(){{
            add(new FungusDrop(new ItemStack(Blocks.DIRT),100));
        }},new ArrayList<FungusDrop>(){{
            add(new FungusDrop(new ItemStack(Items.DIAMOND,2),50));
        }}
        );
    }
}
