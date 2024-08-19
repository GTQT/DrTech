package com.drppp.drtech.Client.lib.obj;


import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AdvancedModelLoader {
    private static Map<String, IModelCustomLoader> instances = Maps.newHashMap();

    public AdvancedModelLoader() {
    }

    public static void registerModelHandler(IModelCustomLoader modelHandler) {
        String[] var1 = modelHandler.getSuffixes();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String suffix = var1[var3];
            instances.put(suffix, modelHandler);
        }

    }

    public static IModelCustom loadModel(ResourceLocation resource) throws IllegalArgumentException, WavefrontObject.ModelFormatException {
        String name = resource.getPath();
        int i = name.lastIndexOf(46);
        if (i == -1) {
            FMLLog.severe("The resource name %s is not valid", new Object[]{resource});
            throw new IllegalArgumentException("The resource name is not valid");
        } else {
            String suffix = name.substring(i + 1);
            IModelCustomLoader loader = (IModelCustomLoader)instances.get(suffix);
            if (loader == null) {
                FMLLog.severe("The resource name %s is not supported", new Object[]{resource});
                throw new IllegalArgumentException("The resource name is not supported");
            } else {
                return loader.loadInstance(resource);
            }
        }
    }

    public static Collection<String> getSupportedSuffixes() {
        return instances.keySet();
    }

    static {
        registerModelHandler(new ObjModelLoader());
    }
}
