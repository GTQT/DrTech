package com.drppp.drtech.MetaTileEntities;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Load.DrtechReceipes;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.generator.AnnihilationGenerator;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.standard.MetaTileEntityAdvancedProsscessArray;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.standard.MetaTileEntityElectricImplosionCompressor;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Utils.DrtechUtils;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;

import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityLaserHatch;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static gregtech.common.metatileentities.MetaTileEntities.registerSimpleMetaTileEntity;

public class MetaTileEntities {

    public static  AnnihilationGenerator ANNIHILATION_GENERATOR;
    public static MetaTileEntityAdvancedProsscessArray ADVANCED_PROCESS_ARRAY;
    public static MetaTileEntityElectricImplosionCompressor LARGE_LARGE;
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_16384 = new MetaTileEntityLaserHatch[10];
    public static final SimpleMachineMetaTileEntity[] UU_PRODUCTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] DUPLICATOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
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
        registerSimpleMetaTileEntity(UU_PRODUCTER, 17012 +endPos, "uu_producter", DrtechReceipes.UU_RECIPES, Textures.UUPRODUCTER_OVERLAY, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);
        //17024
        registerSimpleMetaTileEntity(DUPLICATOR, 17024, "duplicator", DrtechReceipes.COPY_RECIPES, Textures.DUPLICATOR, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);

    }


    public static @NotNull ResourceLocation getmyId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
