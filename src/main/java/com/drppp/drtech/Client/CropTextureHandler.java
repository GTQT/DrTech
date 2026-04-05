package com.drppp.drtech.Client;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Blocks.Crops.CropRegistry;
import com.drppp.drtech.common.Blocks.Crops.CropType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 贴图注册处理器
 *
 * 将每种作物的每个生长阶段贴图注册到纹理图集(TextureMap)
 * 贴图路径规则: assets/drtech/textures/blocks/crop/作物名/stage_N.png
 *
 * 例如:
 *   wheat的7个阶段: wheat/stage_0.png ~ wheat/stage_7.png
 *   ferru的5个阶段: ferru/stage_0.png ~ ferru/stage_5.png
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public class CropTextureHandler {

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (CropType type : CropRegistry.getAll().values()) {
            String cropId = type.getId();
            int maxStage = type.getMaxGrowthStage();

            for (int stage = 0; stage <= maxStage; stage++) {
                String path = Tags.MODID + ":blocks/crop/" + cropId + "/stage_" + stage;
                event.getMap().registerSprite(new ResourceLocation(path));
            }
        }
    }
}
