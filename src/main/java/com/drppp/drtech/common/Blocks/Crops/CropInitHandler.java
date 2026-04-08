package com.drppp.drtech.common.Blocks.Crops;

import com.drppp.drtech.common.event.CropTickHandler;
import net.minecraftforge.common.MinecraftForge;

/**
 * 作物系统初始化入口
 *
 * 使用方法:
 *
 *   // preInit阶段 (双端)
 *   CropInitHandler.preInit(config);
 *
 *   // init阶段 (双端)
 *   CropInitHandler.init();
 *
 *   // init阶段 (仅客户端, 在ClientProxy中调用)
 *   CropClientInit.init();
 */
public class CropInitHandler {

    public static void preInit()
    {
        CropRegistry.registerDefaults();
        CrossBreedingRegistry.registerDefaults();
        BlockCropStick.initVanillaSeedMap();
        MinecraftForge.EVENT_BUS.register(new CropTickHandler());
    }
    public static void init()
    {
        CropRegistry.registerMoreInfo();
    }

}
