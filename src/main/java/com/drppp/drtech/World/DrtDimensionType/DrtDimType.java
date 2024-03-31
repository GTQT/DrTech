package com.drppp.drtech.World.DrtDimensionType;

import com.drppp.drtech.World.WorldPollution;
import com.drppp.drtech.World.WorldRoss128;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;


public class DrtDimType
{
    public static DimensionType POLLUTION;
    public static DimensionType ROSS128B;
    public static void init()
    {
        POLLUTION = DimensionType.register("the_pollution", "_pollution", 300, WorldPollution.class, false);
        ROSS128B = DimensionType.register("the_ross128b", "_ross128b", 301, WorldRoss128.class, false);
    }
}