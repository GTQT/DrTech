package com.drppp.drtech.MetaTileEntities;

import com.drppp.drtech.MetaTileEntities.muti.ecectric.generator.AnnihilationGenerator;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.standard.MetaTileEntityAdvancedProsscessArray;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.standard.MetaTileEntityElectricImplosionCompressor;
import com.drppp.drtech.Tags;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityLaserHatch;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class MetaTileEntities {

    public static  AnnihilationGenerator ANNIHILATION_GENERATOR;
    public static MetaTileEntityAdvancedProsscessArray ADVANCED_PROCESS_ARRAY;
    public static MetaTileEntityElectricImplosionCompressor LARGE_LARGE;
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_16384 = new MetaTileEntityLaserHatch[10];

    public static void Init() {
        ANNIHILATION_GENERATOR = registerMetaTileEntity(17000, new AnnihilationGenerator(getmyId("annihilation_generator")));
        ADVANCED_PROCESS_ARRAY = registerMetaTileEntity(17001, new MetaTileEntityAdvancedProsscessArray(getmyId("advanced_process_array"),1));
        LARGE_LARGE = registerMetaTileEntity(17002, new MetaTileEntityElectricImplosionCompressor(getmyId("electric_implosion_compressor")));
        int endPos = GregTechAPI.isHighTier() ? LASER_OUTPUT_HATCH_16384.length - 1 :
                Math.min(LASER_OUTPUT_HATCH_16384.length - 1, GTValues.UHV - GTValues.IV);
        for (int i = 0; i < endPos; i++) {
            int v = i + GTValues.IV;
            String voltageName = GTValues.VN[v].toLowerCase();
            LASER_OUTPUT_HATCH_16384[i] = registerMetaTileEntity(17003 + i, new MetaTileEntityLaserHatch(
                    getmyId("laser_hatch.target_16384a." + voltageName), false, v, 16384));
            LASER_OUTPUT_HATCH_16384[i] = registerMetaTileEntity(17012 + i,
                    new MetaTileEntityLaserHatch(getmyId("laser_hatch.source_16384a." + voltageName), true, v, 16384));
        }
    }


    public static @NotNull ResourceLocation getmyId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
