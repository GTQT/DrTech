package com.drppp.drtech.World;

import com.drppp.drtech.World.DrtDimensionType.DrtDimType;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class WorldRegisterHandler {

    public static void init()
    {
        DimensionManager.registerDimension(300, DrtDimType.POLLUTION);
        DimensionManager.registerDimension(301, DrtDimType.ROSS128B);
    }
}
