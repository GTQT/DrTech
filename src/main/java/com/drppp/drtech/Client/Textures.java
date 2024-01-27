package com.drppp.drtech.Client;

import codechicken.lib.texture.TextureUtils;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;

import static gregtech.client.renderer.texture.Textures.iconRegisters;

public class Textures {
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite LASER_PIPE_IN;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite LASER_PIPE_SIDE;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite LASER_PIPE_OVERLAY;
    @SideOnly(Side.CLIENT)
    public static TextureAtlasSprite LASER_PIPE_OVERLAY_EMISSIVE;
    @SideOnly(Side.CLIENT)
    public static void register(TextureMap textureMap) {

        LASER_PIPE_SIDE = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_side"));
        LASER_PIPE_IN = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_in"));
        LASER_PIPE_OVERLAY = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_side_overlay"));
        LASER_PIPE_OVERLAY_EMISSIVE = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_side_overlay_emissive"));

    }
}
