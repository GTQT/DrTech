package com.drppp.drtech.Client.lib.obj;

import net.minecraft.util.ResourceLocation;
public interface IModelCustomLoader {
    String getType();

    String[] getSuffixes();

    IModelCustom loadInstance(ResourceLocation var1) throws WavefrontObject.ModelFormatException;
    IModelCustom loadInstance(ResourceLocation var1,boolean var2) throws WavefrontObject.ModelFormatException;
}
