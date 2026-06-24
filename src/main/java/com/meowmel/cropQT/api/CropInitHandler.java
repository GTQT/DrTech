package com.meowmel.cropQT.api;

import com.meowmel.cropQT.block.BlockCropStick;
import com.meowmel.cropQT.event.CropTickHandler;
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
        MinecraftForge.EVENT_BUS.register(new CropTickHandler());
    }
    public static void init()
    {
        CropRegistry.registerAll();
        CrossBreedingRegistry.registerDefaults();
        BlockCropStick.initVanillaSeedMap();
    }

}
