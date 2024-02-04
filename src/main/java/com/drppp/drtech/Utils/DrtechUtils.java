package com.drppp.drtech.Utils;

import com.drppp.drtech.Tags;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class DrtechUtils {
    @Nonnull
    public static ResourceLocation getRL(@Nonnull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }
}
