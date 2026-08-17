package com.drppp.drtech.Client.drone;

import com.cleanroommc.modularui.drawable.UITexture;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Stable per-node editor texture lookup shared by the library and graph canvas. */
public final class DroneNodeIconTextures {
    private static final Map<ResourceLocation, UITexture> CACHE = new ConcurrentHashMap<>();

    private DroneNodeIconTextures() { }

    public static UITexture get(ResourceLocation nodeType) {
        if (nodeType == null) return UITexture.fullImage("drtech", "gui/drone/nodes/missing");
        return CACHE.computeIfAbsent(nodeType, type -> UITexture.fullImage("drtech",
                "gui/drone/nodes/" + safePath(type.getPath())));
    }

    public static void draw(ResourceLocation nodeType, int x, int y, int size) {
        get(nodeType).draw(x, y, size, size);
    }

    private static String safePath(String path) {
        return path == null || !path.matches("[a-z0-9_./-]+") ? "missing" : path;
    }
}
