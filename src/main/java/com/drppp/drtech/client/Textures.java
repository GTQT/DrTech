package com.drppp.drtech.client;

import com.drppp.drtech.Tags;
import com.drppp.drtech.client.render.CentrifugeRender;
import gregtech.api.GTValues;
import gregtech.api.gui.resources.AdoptableTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class Textures {
    public static final TextureArea BEE_DRONE_LOGO = TextureArea.fullImage("textures/gui/bee_drone.png");
    public static final TextureArea BEE_QUEEN_LOGO = TextureArea.fullImage("textures/gui/bee_queen.png");
    public static final TextureArea CROSS = TextureArea.fullImage("textures/gui/cross.png");
    public static final TextureArea CHECK_MARK = TextureArea.fullImage("textures/gui/checkmark.png");
    public static final CentrifugeRender CENTRIFUGE_RENDER = new CentrifugeRender();

    public static OrientedOverlayRenderer INDUSTRIAL_MACHINE;

    public static SimpleOverlayRenderer ELEMENT_CONSTRAINS_MACHINE_CASING;
    public static SimpleOverlayRenderer YOT_TANK_CASING;
    public static SimpleOverlayRenderer TFFT_TANK_CASING;
    public static SimpleOverlayRenderer SALT_INHIBITION_CASING;
    public static SimpleOverlayRenderer JIAO_BAN_CASING;
    public static SimpleOverlayRenderer SIEVE_CASING;
    public static SimpleOverlayRenderer CENTRIFUGE_CASING;
    public static OrientedOverlayRenderer CENTRIFUGE;

    public static OrientedOverlayRenderer TFFT_OVERLAY;
    public static OrientedOverlayRenderer LIGHTING_ROD_OVERLAY;
    public static TextureArea BACKGROUND;

    public static SimpleOverlayRenderer DRONE_DOCK_OVERLAY;
    public static SimpleOverlayRenderer DRONE_PROGRAMMER_OVERLAY;
    public static SimpleOverlayRenderer DRONE_CONTROLLER_OVERLAY;
    public static SimpleOverlayRenderer DRONE_ENDPOINT_ITEM_OVERLAY;
    public static SimpleOverlayRenderer DRONE_ENDPOINT_FLUID_OVERLAY;
    public static SimpleOverlayRenderer DRONE_ENDPOINT_EU_OVERLAY;
    public static SimpleSidedCubeRenderer DRONE_DOCK_CASING;
    public static SimpleSidedCubeRenderer DRONE_PROGRAMMER_CASING;
    public static SimpleSidedCubeRenderer DRONE_CONTROLLER_CASING;
    public static SimpleSidedCubeRenderer DRONE_ENDPOINT_ITEM_CASING;
    public static SimpleSidedCubeRenderer DRONE_ENDPOINT_FLUID_CASING;
    public static SimpleSidedCubeRenderer DRONE_ENDPOINT_EU_CASING;
    public static SimpleSidedCubeRenderer FUSION_REACTOR_CASING;
    public static OrientedOverlayRenderer DISASSEMBLY;
    public static OrientedOverlayRenderer INDUSTRIAL_APIARY;
    //crop_breeder
    public static OrientedOverlayRenderer CROP_BREEDER_OVERLAY;
    //crop_synthesizer
    public static OrientedOverlayRenderer CROP_SYNTHESIZER_OVERLAY;
    //gene_extractor
    public static OrientedOverlayRenderer GENE_EXTRACTOR_OVERLAY;
    //seed_generator
    public static OrientedOverlayRenderer SEED_GENERATOR_OVERLAY;
    //工业农场的砖砌农业外壳
    public static SimpleOverlayRenderer BRICKED_AGRICULTURAL_CASING;

    public static void init() {
        CENTRIFUGE = new OrientedOverlayRenderer("overlay/centrifuge");
        DISASSEMBLY = new OrientedOverlayRenderer("machines/disassembly");
        INDUSTRIAL_APIARY = new OrientedOverlayRenderer("machines/industrial_apiary");
        CROP_BREEDER_OVERLAY = new OrientedOverlayRenderer("machines/crop_breeder");
        CROP_SYNTHESIZER_OVERLAY = new OrientedOverlayRenderer("machines/crop_synthesizer");
        GENE_EXTRACTOR_OVERLAY = new OrientedOverlayRenderer("machines/gene_extractor");
        SEED_GENERATOR_OVERLAY = new OrientedOverlayRenderer("machines/seed_generator");
        BRICKED_AGRICULTURAL_CASING = new SimpleOverlayRenderer("bricked_agricultural_casing");
        ELEMENT_CONSTRAINS_MACHINE_CASING = new SimpleOverlayRenderer("casings/element_constrains_machine_casing");
        YOT_TANK_CASING = new SimpleOverlayRenderer("casings/yot_tank_casing");
        TFFT_TANK_CASING = new SimpleOverlayRenderer("casings/tfft_casing");
        INDUSTRIAL_MACHINE = new OrientedOverlayRenderer("multiblock/industrial_machine");
        TFFT_OVERLAY = new OrientedOverlayRenderer("multiblock/tfft_tank");
        LIGHTING_ROD_OVERLAY = new OrientedOverlayRenderer("multiblock/lighting_rod");
        SALT_INHIBITION_CASING = new SimpleOverlayRenderer("casings/salt_inhibition_casing");
        JIAO_BAN_CASING = new SimpleOverlayRenderer("casings/jiao_ban_casing");
        SIEVE_CASING = new SimpleOverlayRenderer("casings/sieve_casing");
        CENTRIFUGE_CASING = new SimpleOverlayRenderer("casings/centrifuge_casing");
        BACKGROUND = AdoptableTextureArea.fullImage("textures/overgui/background.png", 176, 256, 3, 3);
        DRONE_DOCK_OVERLAY = new SimpleOverlayRenderer("drtech:overlay/drone/dock");
        DRONE_PROGRAMMER_OVERLAY = new SimpleOverlayRenderer("drtech:overlay/drone/programmer");
        DRONE_CONTROLLER_OVERLAY = new SimpleOverlayRenderer("drtech:overlay/drone/controller");
        DRONE_ENDPOINT_ITEM_OVERLAY = new SimpleOverlayRenderer("drtech:overlay/drone/endpoint_item");
        DRONE_ENDPOINT_FLUID_OVERLAY = new SimpleOverlayRenderer("drtech:overlay/drone/endpoint_fluid");
        DRONE_ENDPOINT_EU_OVERLAY = new SimpleOverlayRenderer("drtech:overlay/drone/endpoint_eu");
        DRONE_DOCK_CASING = new SimpleSidedCubeRenderer("drtech:casings/drone/dock");
        DRONE_PROGRAMMER_CASING = new SimpleSidedCubeRenderer("drtech:casings/drone/programmer");
        DRONE_CONTROLLER_CASING = new SimpleSidedCubeRenderer("drtech:casings/drone/controller");
        DRONE_ENDPOINT_ITEM_CASING = new SimpleSidedCubeRenderer("drtech:casings/drone/endpoint_item");
        DRONE_ENDPOINT_FLUID_CASING = new SimpleSidedCubeRenderer("drtech:casings/drone/endpoint_fluid");
        DRONE_ENDPOINT_EU_CASING = new SimpleSidedCubeRenderer("drtech:casings/drone/endpoint_eu");
        FUSION_REACTOR_CASING = new SimpleSidedCubeRenderer("drtech:blocks/fusion/plasma_containment_casing");
    }

    public static void register(TextureMap textureMap) {

    }

}
