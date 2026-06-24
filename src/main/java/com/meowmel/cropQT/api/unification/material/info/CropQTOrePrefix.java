package com.meowmel.cropQT.api.unification.material.info;

import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;

import static com.meowmel.cropQT.api.unification.ore.CropQTFlags.*;
import static gregtech.api.GTValues.M;
import static gregtech.api.unification.ore.OrePrefix.Flags.ENABLE_UNIFICATION;

public class CropQTOrePrefix {

    public static final OrePrefix bonsai = new OrePrefix("bonsai", M, null, CropQTMaterialIconType.bonsai,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_BONSAI));

    public static final OrePrefix botania = new OrePrefix("botania", M, null, CropQTMaterialIconType.botania,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_BOTANIA));

    public static final OrePrefix flower = new OrePrefix("flower", M, null, CropQTMaterialIconType.flower,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_FLOWER));

    public static final OrePrefix grain = new OrePrefix("grain", M, null, CropQTMaterialIconType.grain,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_GRAIN));

    public static final OrePrefix magic = new OrePrefix("magic", M, null, CropQTMaterialIconType.magic,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_MAGIC));

    public static final OrePrefix oreberry = new OrePrefix("oreberry", M, null, CropQTMaterialIconType.oreberry,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_OREBERRY));

    public static final OrePrefix spore = new OrePrefix("spore", M, null, CropQTMaterialIconType.spore,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SPORE));

    public static final OrePrefix vanilla = new OrePrefix("vanilla", M, null, CropQTMaterialIconType.vanilla,
            ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_VANILLA));

    public static void init() {
        MetaItems.addOrePrefix(CropQTOrePrefix.bonsai);
        MetaItems.addOrePrefix(CropQTOrePrefix.botania);
        MetaItems.addOrePrefix(CropQTOrePrefix.flower);
        MetaItems.addOrePrefix(CropQTOrePrefix.grain);
        MetaItems.addOrePrefix(CropQTOrePrefix.magic);
        MetaItems.addOrePrefix(CropQTOrePrefix.oreberry);
        MetaItems.addOrePrefix(CropQTOrePrefix.spore);
        MetaItems.addOrePrefix(CropQTOrePrefix.vanilla);
    }
}
