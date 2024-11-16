package com.drppp.drtech.common.CustomCrops;

import com.drppp.drtech.Tags;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CustomeCropsInit {
    public static final CropGanZhe CROP_GAN_ZHE = new CropGanZhe();
    public static void init(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(CROP_GAN_ZHE);
        GameRegistry.registerTileEntity(DrtCropTileBase.class, new ResourceLocation(Tags.MODID, "custom_crop_ganzhe"));
    }
}
