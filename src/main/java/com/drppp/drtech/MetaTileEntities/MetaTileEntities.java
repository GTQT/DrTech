package com.drppp.drtech.MetaTileEntities;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.Load.DrtechReceipes;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.generator.AnnihilationGenerator;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.standard.*;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.MetaTileEntityYotTank;
import com.drppp.drtech.MetaTileEntities.muti.ecectric.store.MetatileEntityTwentyFiveFluidTank;
import com.drppp.drtech.MetaTileEntities.muti.mutipart.MetaTileEntityYotHatch;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Utils.DrtechUtils;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;

import gregtech.common.metatileentities.multi.electric.MetaTileEntityFluidDrill;
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
    public static MetaTileEntityInfiniteFluidDrill INFINITE_FLUID_DRILLING_RIG;
    public static MetaTileEntityLargeUUProducter LARGE_UU_PRODUCTER;
    public static MetaTileEntityLargeElementDuplicator LARGE_ELEMENT_DUPLICATOR;
    public static MetaTileEntityLogFactory LOG_FACTORY;
    public static MetaTileEntityLargeMolecularRecombination LARGE_MOLECULAR_RECOMBINATION;
    public static MetaTileEntityYotTank YOUT_TANK;
    public static MetaTileEntityMobsKiller MOB_KILLER;
    public static MetaTileEntityDeepGroundPump DEEP_GROUND_PUMP;
    public static MetatileEntityTwentyFiveFluidTank TFFT;
    public static MetaTileEntityYotHatch YOT_HARCH;
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
        registerSimpleMetaTileEntity(DUPLICATOR, 17035, "duplicator", DrtechReceipes.COPY_RECIPES, Textures.DUPLICATOR, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);
        //17032
        INFINITE_FLUID_DRILLING_RIG =registerMetaTileEntity(17056, new MetaTileEntityInfiniteFluidDrill(getmyId("fluid_drilling_rig.iv"), 6));
        LARGE_UU_PRODUCTER = registerMetaTileEntity(17057,new MetaTileEntityLargeUUProducter(getmyId("large_uu_producter")));
        LARGE_ELEMENT_DUPLICATOR = registerMetaTileEntity(17058,new MetaTileEntityLargeElementDuplicator(getmyId("large_element_duplicator")));
        LOG_FACTORY = registerMetaTileEntity(17059,new MetaTileEntityLogFactory(getmyId("log_factory")));
        LARGE_MOLECULAR_RECOMBINATION = registerMetaTileEntity(17060,new MetaTileEntityLargeMolecularRecombination(getmyId("molecular_recombination")));
        YOUT_TANK = registerMetaTileEntity(17061,new MetaTileEntityYotTank(getmyId("yot_tank")));
        MOB_KILLER = registerMetaTileEntity(17062,new MetaTileEntityMobsKiller(getmyId("mob_killer")));
        DEEP_GROUND_PUMP = registerMetaTileEntity(17063,new MetaTileEntityDeepGroundPump(getmyId("deep_ground_pump")));
        TFFT = registerMetaTileEntity(17064,new MetatileEntityTwentyFiveFluidTank(getmyId("tfft_tank")));
        YOT_HARCH = registerMetaTileEntity(17065,new MetaTileEntityYotHatch(getmyId("yot_hatch")));
    }


    public static @NotNull ResourceLocation getmyId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
