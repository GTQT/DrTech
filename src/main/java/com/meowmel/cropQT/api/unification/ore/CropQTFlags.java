package com.meowmel.cropQT.api.unification.ore;

import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.properties.PropertyKey;

public class CropQTFlags {
    public static final MaterialFlag GENERATE_BONSAI = new MaterialFlag.Builder("generate_bonsai")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_BOTANIA = new MaterialFlag.Builder("generate_botania")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_FLOWER = new MaterialFlag.Builder("generate_flower")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_GRAIN = new MaterialFlag.Builder("generate_grain")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_MAGIC = new MaterialFlag.Builder("generate_magic")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_OREBERRY = new MaterialFlag.Builder("generate_oreberry")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_SPORE = new MaterialFlag.Builder("generate_spore")
            .requireProps(PropertyKey.DUST)
            .build();

    public static final MaterialFlag GENERATE_VANILLA = new MaterialFlag.Builder("generate_vanilla")
            .requireProps(PropertyKey.DUST)
            .build();
}
