package com.drppp.drtech.World;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class WorldRegisterHandler {

    public static void init()
    {
        DimensionManager.registerDimension(300, DimensionType.register("drtech", "_pollution_dimension", 300, WorldPollution.class, false));
    }
}
