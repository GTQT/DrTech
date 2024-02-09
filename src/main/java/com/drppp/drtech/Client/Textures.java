package com.drppp.drtech.Client;

import codechicken.lib.texture.TextureUtils;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;

import static gregtech.client.renderer.texture.Textures.iconRegisters;

public class Textures {
    public static TextureAtlasSprite LASER_PIPE_IN;
    public static TextureAtlasSprite LASER_PIPE_SIDE;
    public static TextureAtlasSprite LASER_PIPE_OVERLAY;
    public static TextureAtlasSprite LASER_PIPE_OVERLAY_EMISSIVE;
    public static OrientedOverlayRenderer UUPRODUCTER_OVERLAY;

    public static OrientedOverlayRenderer DUPLICATOR;
    public static final SimpleOverlayRenderer NEUTRON_MACHINE_CASING = new SimpleOverlayRenderer("casings/neutron_mechanical_casing");
    public static final SimpleOverlayRenderer MASS_GENERATION_CASING = new SimpleOverlayRenderer("casings/mass_generation_casing");
    public static final SimpleOverlayRenderer ASEPTIC_MACHINE_CASING = new SimpleOverlayRenderer("casings/aseptic_machine_casing");
    public static final SimpleOverlayRenderer ELEMENT_CONSTRAINS_MACHINE_CASING = new SimpleOverlayRenderer("casings/element_constrains_machine_casing");
    public static final SimpleOverlayRenderer YOT_TANK_CASING = new SimpleOverlayRenderer("casings/yot_tank_casing");
    public static final OrientedOverlayRenderer LARGE_UU_PRODUCTER = new OrientedOverlayRenderer("multiblock/large_uu_producter");
    public static OrientedOverlayRenderer ELECTRIC_IMPLOSION_OVERLAY = new OrientedOverlayRenderer("multiblock/electric_implosion");
    @SideOnly(Side.CLIENT)
    public static void register(TextureMap textureMap) {

        LASER_PIPE_SIDE = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_side"));
        LASER_PIPE_IN = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_in"));
        LASER_PIPE_OVERLAY = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_side_overlay"));
        LASER_PIPE_OVERLAY_EMISSIVE = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_side_overlay_emissive"));
        UUPRODUCTER_OVERLAY  = new OrientedOverlayRenderer("machines/uu_producter");
        DUPLICATOR  = new OrientedOverlayRenderer("machines/duplicator");

    }
}
