package com.drppp.drtech.Client.render;


import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public interface ITextureRegistrar {
    @SideOnly(Side.CLIENT)
    List<ResourceLocation> getTextureLocations();
}