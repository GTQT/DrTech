package com.drppp.drtech.Client.lib.obj;

import net.minecraft.util.ResourceLocation;
public class ObjModelLoader implements IModelCustomLoader {
    private static final String[] types = new String[]{"obj"};

    public ObjModelLoader() {
    }

    public String getType() {
        return "OBJ model";
    }

    public String[] getSuffixes() {
        return types;
    }

    public IModelCustom loadInstance(ResourceLocation resource) throws thaumcraft.client.lib.obj.WavefrontObject.ModelFormatException {
        return new WavefrontObject(resource);
    }
}
