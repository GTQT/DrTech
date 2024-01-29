package com.drppp.drtech.MetaTileEntities;

import com.drppp.drtech.MetaTileEntities.muti.ecectric.generator.AnnihilationGenerator;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.standard.MetaTileEntityAdvancedProsscessArray;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.standard.MetaTileEntityElectricImplosionCompressor;
import com.drppp.drtech.Tags;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class MetaTileEntities {

    public static  AnnihilationGenerator ANNIHILATION_GENERATOR;
    public static MetaTileEntityAdvancedProsscessArray ADVANCED_PROCESS_ARRAY;
    public static MetaTileEntityElectricImplosionCompressor LARGE_LARGE;

    public static void Init() {
        ANNIHILATION_GENERATOR = registerMetaTileEntity(17000, new AnnihilationGenerator(getmyId("annihilation_generator")));
        ADVANCED_PROCESS_ARRAY = registerMetaTileEntity(17001, new MetaTileEntityAdvancedProsscessArray(getmyId("advanced_process_array"),1));
        LARGE_LARGE = registerMetaTileEntity(17002, new MetaTileEntityElectricImplosionCompressor(getmyId("electric_implosion_compressor")));
    }


    public static @NotNull ResourceLocation getmyId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
