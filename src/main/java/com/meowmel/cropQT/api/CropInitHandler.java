package com.meowmel.cropQT.api;

import com.drppp.drtech.common.items.ItemsInit;
import com.meowmel.cropQT.api.mutation.MutationPools;
import com.meowmel.cropQT.api.unification.material.CropMaterialScanner;
import com.meowmel.cropQT.api.registries.HydrationRegistry;
import com.meowmel.cropQT.block.BlockCropStick;
import com.meowmel.cropQT.event.CropTickHandler;
import com.meowmel.cropQT.integration.CropScannerLogic;
import com.meowmel.cropQT.api.registries.FertilizerRegistry;
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
        // 土壤组要先登记：CropType.Builder 的默认土壤引用了 SoilTypes.farmland，
        // 而 SoilRegistry 的反查依赖登记顺序
        SoilTypes.ensureRegistered();
        HydrationRegistry.registerDefaults();
        FertilizerRegistry.registerDefaults();
        // 材料驱动的作物先登记（产物物品是 OreDictUnifier 取的，那时已经建好了索引），
        // 再补手写的原版作物
        CropMaterialScanner.registerCrops();
        CropRegistry.ensureRegistered();
        MutationPools.registerDefaults();
        BlockCropStick.initVanillaSeedMap();
        // 让 GT 的扫描仪能分析种子（走 GT 的 registerCustomScannerLogic，不是 mixin）
        gregtech.api.recipes.machines.RecipeMapScanner.registerCustomScannerLogic(new CropScannerLogic());
    }
    public static void clienInit()
    {
        ItemsInit.registerSeedModelsLate();
    }
}
