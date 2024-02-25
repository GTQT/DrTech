package com.drppp.drtech.Client;

import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class Textures {
    public static TextureAtlasSprite LASER_PIPE_IN;
    public static TextureAtlasSprite LASER_PIPE_SIDE;
    public static TextureAtlasSprite LASER_PIPE_OVERLAY;
    public static TextureAtlasSprite LASER_PIPE_OVERLAY_EMISSIVE;
    public static  OrientedOverlayRenderer UUPRODUCTER_OVERLAY;

    public static  OrientedOverlayRenderer DUPLICATOR;
    public static  SimpleOverlayRenderer NEUTRON_MACHINE_CASING;
    public static SimpleOverlayRenderer MASS_GENERATION_CASING;
    public static  SimpleOverlayRenderer ASEPTIC_MACHINE_CASING;
    public static  SimpleOverlayRenderer ELEMENT_CONSTRAINS_MACHINE_CASING;
    public static  SimpleOverlayRenderer YOT_TANK_CASING;
    public static  SimpleOverlayRenderer TFFT_TANK_CASING;
    public static  SimpleOverlayRenderer SALT_INHIBITION_CASING;
    public static  OrientedOverlayRenderer LARGE_UU_PRODUCTER;
    public static  OrientedOverlayRenderer ELECTRIC_IMPLOSION_OVERLAY;
    public static  OrientedOverlayRenderer TFFT_OVERLAY;
    public static void init()
    {
        NEUTRON_MACHINE_CASING = new SimpleOverlayRenderer("casings/neutron_mechanical_casing");
        UUPRODUCTER_OVERLAY= new OrientedOverlayRenderer("machines/uu_producter");
        DUPLICATOR = new OrientedOverlayRenderer("machines/duplicator");
        MASS_GENERATION_CASING = new SimpleOverlayRenderer("casings/mass_generation_casing");
        ASEPTIC_MACHINE_CASING = new SimpleOverlayRenderer("casings/aseptic_machine_casing");
        ELEMENT_CONSTRAINS_MACHINE_CASING = new SimpleOverlayRenderer("casings/element_constrains_machine_casing");
        YOT_TANK_CASING = new SimpleOverlayRenderer("casings/yot_tank_casing");
        TFFT_TANK_CASING = new SimpleOverlayRenderer("casings/tfft_casing");
        LARGE_UU_PRODUCTER = new OrientedOverlayRenderer("multiblock/large_uu_producter");
        ELECTRIC_IMPLOSION_OVERLAY = new OrientedOverlayRenderer("multiblock/electric_implosion");
        TFFT_OVERLAY = new OrientedOverlayRenderer("multiblock/tfft_tank");
        SALT_INHIBITION_CASING = new SimpleOverlayRenderer("casings/salt_inhibition_casing");
    }
    public static void register(TextureMap textureMap) {

        LASER_PIPE_SIDE = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_side"));
        LASER_PIPE_IN = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_in"));
        LASER_PIPE_OVERLAY = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_side_overlay"));
        LASER_PIPE_OVERLAY_EMISSIVE = textureMap.registerSprite(new ResourceLocation("drtech", "blocks/pipe/pipe_laser_side_overlay_emissive"));

    }

}
