package com.drppp.drtech.Client;

import com.drppp.drtech.Client.Custome.CentrifugeRender;
import com.drppp.drtech.Tags;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.AdoptableTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import keqing.gtqtcore.client.textures.custom.IsaMillRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class Textures {
    public static TextureAtlasSprite LASER_PIPE_IN;
    public static TextureAtlasSprite LASER_PIPE_SIDE;
    public static TextureAtlasSprite LASER_PIPE_OVERLAY;
    public static TextureAtlasSprite LASER_PIPE_OVERLAY_EMISSIVE;
    public static  OrientedOverlayRenderer UUPRODUCTER_OVERLAY;
    public static  OrientedOverlayRenderer INDUSTRIAL_MACHINE;
    public static  OrientedOverlayRenderer DUPLICATOR;
    public static  SimpleOverlayRenderer NEUTRON_MACHINE_CASING;
    public static SimpleOverlayRenderer MASS_GENERATION_CASING;
    public static  SimpleOverlayRenderer ASEPTIC_MACHINE_CASING;
    public static  SimpleOverlayRenderer ELEMENT_CONSTRAINS_MACHINE_CASING;
    public static  SimpleOverlayRenderer YOT_TANK_CASING;
    public static  SimpleOverlayRenderer TFFT_TANK_CASING;
    public static  SimpleOverlayRenderer NUCLEAR_PART_CASING;
    public static  SimpleOverlayRenderer SALT_INHIBITION_CASING;
    public static  SimpleOverlayRenderer JIAO_BAN_CASING;
    public static  SimpleOverlayRenderer SIEVE_CASING;
    public static  SimpleOverlayRenderer CENTRIFUGE_CASING;
    public static  SimpleOverlayRenderer MATRIX_CASING;
    public static  OrientedOverlayRenderer CENTRIFUGE;
    public static  OrientedOverlayRenderer LARGE_UU_PRODUCTER;
    public static  OrientedOverlayRenderer ELECTRIC_IMPLOSION_OVERLAY;
    public static  OrientedOverlayRenderer TFFT_OVERLAY;
    public static OrientedOverlayRenderer LIGHTING_ROD_OVERLAY;
    public static  TextureArea BACKGROUND;
    public static  TextureArea PAIL_BACKGROUND;
    public static SimpleOverlayRenderer WIRELESS_HATCH_HATCH;
    public static OrientedOverlayRenderer DISASSEMBLY;
    public static OrientedOverlayRenderer INDUSTRIAL_APIARY;
    public static OrientedOverlayRenderer RU_SHREDDER;
    public static OrientedOverlayRenderer RU_MIXER;
    public static OrientedOverlayRenderer RU_SIFTER;
    public static OrientedOverlayRenderer RU_COMPRESSOR;
    public static OrientedOverlayRenderer RU_EXTRUDER;
    public static OrientedOverlayRenderer RU_HAMMER;
    public static OrientedOverlayRenderer RU_WIREMILL;
    public static OrientedOverlayRenderer RU_BENDER;
    public static OrientedOverlayRenderer RU_CENTRIFUGE;
    public static OrientedOverlayRenderer RU_SPLITTER;
    public static OrientedOverlayRenderer HU_BASE_BURRING_BOX;
    public static OrientedOverlayRenderer HU_BASE_BURRING_BOX_LIQUID;
    public static  SimpleOverlayRenderer HU_BURRING_BOX_SIDE_OVERLAY;
    public static  SimpleOverlayRenderer HU_BURRING_BOX_SIDE_FULL_OVERLAY;
    public static OrientedOverlayRenderer FILTER_OVERLAY;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_4x;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_16x;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_64x;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_256x;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_1024x;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_4096x;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_16384x;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_65536x;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_262144x;
    public static SimpleOverlayRenderer MULTIPART_WIRELESS_ENERGY_1048576x;
    public static final TextureArea BEE_DRONE_LOGO = TextureArea.fullImage("textures/gui/bee_drone.png");
    public static final TextureArea BEE_QUEEN_LOGO = TextureArea.fullImage("textures/gui/bee_queen.png");
    public static final TextureArea CROSS = TextureArea.fullImage("textures/gui/cross.png");
    public static final TextureArea CHECK_MARK = TextureArea.fullImage("textures/gui/checkmark.png");
    public static final CentrifugeRender CENTRIFUGE_RENDER = new CentrifugeRender();
    public static final SimpleSidedCubeRenderer[] MACHINE_CASINGS = new SimpleSidedCubeRenderer[4];
    public static final SimpleSidedCubeRenderer BASE_BURRING_BOX_TEXTURE = new SimpleSidedCubeRenderer("casings/drtmachine/base");
    public static void init()
    {
        MULTIPART_WIRELESS_ENERGY = new SimpleOverlayRenderer("wireless_hatch/overlay_front");
        MULTIPART_WIRELESS_ENERGY_4x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.4x");
        MULTIPART_WIRELESS_ENERGY_16x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.16x");
        MULTIPART_WIRELESS_ENERGY_64x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.64x");
        MULTIPART_WIRELESS_ENERGY_256x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.256x");
        MULTIPART_WIRELESS_ENERGY_1024x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.1024x");
        MULTIPART_WIRELESS_ENERGY_4096x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.4096x");
        MULTIPART_WIRELESS_ENERGY_16384x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.16384x");
        MULTIPART_WIRELESS_ENERGY_65536x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.65536x");
        MULTIPART_WIRELESS_ENERGY_262144x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.262144x");
        MULTIPART_WIRELESS_ENERGY_1048576x = new SimpleOverlayRenderer("wireless_hatch/overlay_front.1048576x");

        NEUTRON_MACHINE_CASING = new SimpleOverlayRenderer("casings/neutron_mechanical_casing");
        UUPRODUCTER_OVERLAY= new OrientedOverlayRenderer("machines/uu_producter");
        CENTRIFUGE= new OrientedOverlayRenderer("overlay/centrifuge");
        DUPLICATOR = new OrientedOverlayRenderer("machines/duplicator");
        DISASSEMBLY = new OrientedOverlayRenderer("machines/disassembly");
        INDUSTRIAL_APIARY = new OrientedOverlayRenderer("machines/industrial_apiary");
        RU_SHREDDER = new OrientedOverlayRenderer("machines/ru_shredder");
        RU_MIXER = new OrientedOverlayRenderer("machines/ru_mixer");
        RU_SIFTER = new OrientedOverlayRenderer("machines/ru_sifter");
        RU_COMPRESSOR = new OrientedOverlayRenderer("machines/ru_compressor");
        RU_EXTRUDER = new OrientedOverlayRenderer("machines/ru_extruder");
        RU_HAMMER = new OrientedOverlayRenderer("machines/ru_hammer");
        RU_WIREMILL = new OrientedOverlayRenderer("machines/ru_wiremill");
        RU_BENDER= new OrientedOverlayRenderer("machines/ru_bender");
        RU_CENTRIFUGE= new OrientedOverlayRenderer("machines/ru_centrifuge");
        RU_SPLITTER= new OrientedOverlayRenderer("machines/ru_splitter");
        HU_BASE_BURRING_BOX= new OrientedOverlayRenderer("machines/hu_base_burring_box");
        HU_BASE_BURRING_BOX_LIQUID= new OrientedOverlayRenderer("machines/hu_base_burring_box_liquid");
        HU_BURRING_BOX_SIDE_OVERLAY = new SimpleOverlayRenderer("casings/hu_burring_box_side_overlay");
        HU_BURRING_BOX_SIDE_FULL_OVERLAY = new SimpleOverlayRenderer("casings/hu_burring_box_side_full_overlay");
        FILTER_OVERLAY = new OrientedOverlayRenderer("machines/type_filter");
        MASS_GENERATION_CASING = new SimpleOverlayRenderer("casings/mass_generation_casing");
        ASEPTIC_MACHINE_CASING = new SimpleOverlayRenderer("casings/aseptic_machine_casing");
        ELEMENT_CONSTRAINS_MACHINE_CASING = new SimpleOverlayRenderer("casings/element_constrains_machine_casing");
        YOT_TANK_CASING = new SimpleOverlayRenderer("casings/yot_tank_casing");
        TFFT_TANK_CASING = new SimpleOverlayRenderer("casings/tfft_casing");
        MATRIX_CASING = new SimpleOverlayRenderer("casings/matrix_casing");
        LARGE_UU_PRODUCTER = new OrientedOverlayRenderer("multiblock/large_uu_producter");
        INDUSTRIAL_MACHINE = new OrientedOverlayRenderer("multiblock/industrial_machine");
        ELECTRIC_IMPLOSION_OVERLAY = new OrientedOverlayRenderer("multiblock/electric_implosion");
        TFFT_OVERLAY = new OrientedOverlayRenderer("multiblock/tfft_tank");
        LIGHTING_ROD_OVERLAY = new OrientedOverlayRenderer("multiblock/lighting_rod");
        SALT_INHIBITION_CASING = new SimpleOverlayRenderer("casings/salt_inhibition_casing");
        JIAO_BAN_CASING = new SimpleOverlayRenderer("casings/jiao_ban_casing");
        SIEVE_CASING = new SimpleOverlayRenderer("casings/sieve_casing");
        CENTRIFUGE_CASING = new SimpleOverlayRenderer("casings/centrifuge_casing");
        NUCLEAR_PART_CASING = new SimpleOverlayRenderer("casings/nuclear_part_casing");
        BACKGROUND = AdoptableTextureArea.fullImage("textures/overgui/background.png", 176, 256, 3, 3);
        PAIL_BACKGROUND = AdoptableTextureArea.fullImage("textures/overgui/pail_background.png", 500, 500, 3, 3);
        WIRELESS_HATCH_HATCH = new SimpleOverlayRenderer("overlay/wireless_hatch/overlay_front");
        for(int i = 0; i < MACHINE_CASINGS.length; ++i) {
            String voltageName = GTValues.VN[i].toLowerCase();
            MACHINE_CASINGS[i] = new SimpleSidedCubeRenderer("casings/drtmachine/" + voltageName);
        }
    }
    public static void register(TextureMap textureMap) {

        LASER_PIPE_SIDE = textureMap.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pipe/pipe_laser_side"));
        LASER_PIPE_IN = textureMap.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pipe/pipe_laser_in"));
        LASER_PIPE_OVERLAY = textureMap.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pipe/pipe_laser_side_overlay"));
        LASER_PIPE_OVERLAY_EMISSIVE = textureMap.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pipe/pipe_laser_side_overlay_emissive"));

    }

}
