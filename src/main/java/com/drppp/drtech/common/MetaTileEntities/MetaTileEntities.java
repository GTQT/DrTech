package com.drppp.drtech.common.MetaTileEntities;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.MeTaTileEntityWindDrivenGenerator;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.*;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileEntityWirelessEnergyHatch;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileeneityPassthroughHatchComputationHatch;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityUniversalCollector;
import com.drppp.drtech.loaders.DrtechReceipes;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.AnnihilationGenerator;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.NuclearReactor;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityEnergyTransTower;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetatileEntityTwentyFiveFluidTank;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileEntityYotHatch;
import com.drppp.drtech.Tags;
import com.drppp.drtech.api.Utils.DrtechUtils;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityLaserHatch;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.GTValues.VN;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static gregtech.common.metatileentities.MetaTileEntities.registerSimpleMetaTileEntity;

public class MetaTileEntities {
    public static AnnihilationGenerator ANNIHILATION_GENERATOR;
    public static MetaTileEntityAdvancedProsscessArray ADVANCED_PROCESS_ARRAY;
    public static MetaTileEntityDeepGroundPump DEEP_GROUND_PUMP;
    public static MetaTileEntityDronePad DRONE_PAD;
    public static MetaTileEntityElectricImplosionCompressor LARGE_LARGE;
    public static MetaTileEntityEnergyTransTower TRANS_TOWER;
    public static MetaTileEntityInfiniteFluidDrill INFINITE_FLUID_DRILLING_RIG;
    public static MetaTileEntityLargeAlloySmelter LARGE_ALLOY_SMELTER;
    public static MetaTileEntityLargeElementDuplicator LARGE_ELEMENT_DUPLICATOR;
    public static MetaTileEntityLargeMolecularRecombination LARGE_MOLECULAR_RECOMBINATION;
    public static MetaTileEntityLargeUUProducter LARGE_UU_PRODUCTER;
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_16384 = new MetaTileEntityLaserHatch[10];
    public static MetaTileEntityLogFactory LOG_FACTORY;
    public static MetaTileEntityMatrixSolver MATRIX_SOLVER;
    public static MetaTileEntityMobsKiller MOB_KILLER;
    public static MetaTileEntityPlayerBeacon PLAYER_BEACON;
    public static MetaTileEntitySolarTower SOLAR_TOWER;
    public static MetatileEntityTwentyFiveFluidTank TFFT;
    public static MetaTileEntityUniversalCollector[] UNIVERSAL_COLLECTORS = new MetaTileEntityUniversalCollector[10];
    public static MeTaTileEntityWindDrivenGenerator WIND_DRIVEN_GENERATOR_HV;
    public static MetaTileEntityYotHatch YOT_HARCH;
    public static MetaTileEntityYotTank YOUT_TANK;
    public static MetaTileEntutyLargeBeeHive LARGE_BEE_HIVE;
    public static NuclearReactor NUCLEAR_GENERATOR;
    public static SimpleMachineMetaTileEntity[] DISASSEMBLY = new SimpleMachineMetaTileEntity[10];
    public static SimpleMachineMetaTileEntity[] DUPLICATOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] UU_PRODUCTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static MetaTileentityLargeExtruder LARGE_EXTRUDER;
    public static MetaTileeneityPassthroughHatchComputationHatch PASSTHROUGH_COMPUTER;
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_4A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_4A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_16A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_16A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_64A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_64A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_256A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_256A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_1024A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_1024A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_4096A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_4096A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_16384A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_16384A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_65536A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_65536A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_262144A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_262144A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_1048576A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_1048576A = new MetaTileEntityWirelessEnergyHatch[15];

    private static <F extends MetaTileEntity> F registerPartMetaTileEntity(int id, F mte) {
        if (id > 1000)
            return null;
        return registerMetaTileEntity(id + 17300, mte);
    }
    static int startID=16999;
    public static int getID()
    {
        startID++;
        return startID;
    }
    public static void Init() {
        String tierName;
        int endPos = GregTechAPI.isHighTier() ? LASER_OUTPUT_HATCH_16384.length - 1 :
                Math.min(LASER_OUTPUT_HATCH_16384.length - 1, GTValues.UHV - GTValues.IV);

        //Common ID 17000
        ADVANCED_PROCESS_ARRAY = registerMetaTileEntity(getID(), new MetaTileEntityAdvancedProsscessArray(getmyId("advanced_process_array"),1));
        ANNIHILATION_GENERATOR = registerMetaTileEntity(getID(), new AnnihilationGenerator(getmyId("annihilation_generator")));
        DEEP_GROUND_PUMP = registerMetaTileEntity(getID(),new MetaTileEntityDeepGroundPump(getmyId("deep_ground_pump")));
        DRONE_PAD = registerMetaTileEntity(getID(), new MetaTileEntityDronePad(getmyId("drone_pad")));
        INFINITE_FLUID_DRILLING_RIG =registerMetaTileEntity(getID(), new MetaTileEntityInfiniteFluidDrill(getmyId("fluid_drilling_rig.iv"), 6));
        LARGE_ALLOY_SMELTER = registerMetaTileEntity(getID(), new MetaTileEntityLargeAlloySmelter(getmyId("large_alloy_smelter")));
        LARGE_BEE_HIVE = registerMetaTileEntity(getID(),new MetaTileEntutyLargeBeeHive(getmyId("large_bee_hive")));
        LARGE_ELEMENT_DUPLICATOR = registerMetaTileEntity(getID(),new MetaTileEntityLargeElementDuplicator(getmyId("large_element_duplicator")));
        LARGE_LARGE = registerMetaTileEntity(getID(), new MetaTileEntityElectricImplosionCompressor(getmyId("electric_implosion_compressor")));
        LARGE_MOLECULAR_RECOMBINATION = registerMetaTileEntity(getID(),new MetaTileEntityLargeMolecularRecombination(getmyId("molecular_recombination")));
        LARGE_UU_PRODUCTER = registerMetaTileEntity(getID(),new MetaTileEntityLargeUUProducter(getmyId("large_uu_producter")));
        LOG_FACTORY = registerMetaTileEntity(getID(),new MetaTileEntityLogFactory(getmyId("log_factory")));
        MATRIX_SOLVER = registerMetaTileEntity(getID(), new MetaTileEntityMatrixSolver(getmyId("matrix_solver")));
        MOB_KILLER = registerMetaTileEntity(getID(),new MetaTileEntityMobsKiller(getmyId("mob_killer")));
        NUCLEAR_GENERATOR = registerMetaTileEntity(getID(),new NuclearReactor(getmyId("nuclear_generator")));
        PLAYER_BEACON = registerMetaTileEntity(getID(),new MetaTileEntityPlayerBeacon(getmyId("player_beacon")));
        SOLAR_TOWER = registerMetaTileEntity(getID(),new MetaTileEntitySolarTower(getmyId("solar_tower")));
        TFFT = registerMetaTileEntity(getID(),new MetatileEntityTwentyFiveFluidTank(getmyId("tfft_tank")));
        TRANS_TOWER = registerMetaTileEntity(getID(),new MetaTileEntityEnergyTransTower(getmyId("trans_tower")));
        WIND_DRIVEN_GENERATOR_HV = registerMetaTileEntity(getID(), new MeTaTileEntityWindDrivenGenerator(getmyId("wind_driven_generator")));
        YOT_HARCH = registerMetaTileEntity(getID(),new MetaTileEntityYotHatch(getmyId("yot_hatch")));
        YOUT_TANK = registerMetaTileEntity(getID(),new MetaTileEntityYotTank(getmyId("yot_tank")));
        LARGE_EXTRUDER = registerMetaTileEntity(getID(),new MetaTileentityLargeExtruder(getmyId("large_extruder")));
        PASSTHROUGH_COMPUTER = registerMetaTileEntity(getID(),new MetaTileeneityPassthroughHatchComputationHatch(getmyId("passthrough_computationhatch")));
        //人工分配 ID 17100
        for (int i = 0; i < 10; i++) {
            tierName = GTValues.VN[i].toLowerCase();
            UNIVERSAL_COLLECTORS[i] = registerMetaTileEntity(17100+i,new MetaTileEntityUniversalCollector(getmyId("universal_collector."+tierName),i+1, gregtech.client.renderer.texture.Textures.GAS_COLLECTOR_OVERLAY));
        }

        for (int i = 0; i < endPos; i++) {
            int v = i + GTValues.IV;
            String voltageName = GTValues.VN[v].toLowerCase();
            LASER_OUTPUT_HATCH_16384[i] = registerMetaTileEntity(17115 + i, new MetaTileEntityLaserHatch(
                    getmyId("laser_hatch.target_16384a." + voltageName), false, v, 16384));
            LASER_OUTPUT_HATCH_16384[i] = registerMetaTileEntity(17130 + i,
                    new MetaTileEntityLaserHatch(getmyId("laser_hatch.source_16384a." + voltageName), true, v, 16384));
        }
        //单方块机器 ID 17200
        registerSimpleMetaTileEntity(UU_PRODUCTER, 17200, "uu_producter", DrtechReceipes.UU_RECIPES, Textures.UUPRODUCTER_OVERLAY, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);
        registerSimpleMetaTileEntity(DUPLICATOR, 17215, "duplicator", DrtechReceipes.COPY_RECIPES, Textures.DUPLICATOR, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);
        registerSimpleMetaTileEntity(DISASSEMBLY, 17230 , "disassembly", DrtechReceipes.DISASSEMBLER_RECIPES, Textures.DISASSEMBLY, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);


        //  ULV-MAX Wireless Energy/Dynamo Hatch (consist of high-amp version) ID 17300
        for (int i = 0; i < 15; i++) {
            String tier = VN[i].toLowerCase();
            WIRELESS_INPUT_ENERGY_HATCH[i]          = registerPartMetaTileEntity( i,       new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input."          + tier), i, 2,       false));
            WIRELESS_INPUT_ENERGY_HATCH_4A[i]       = registerPartMetaTileEntity( 15 + i,  new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_4a."       + tier), i, 4,       false));
            WIRELESS_INPUT_ENERGY_HATCH_16A[i]      = registerPartMetaTileEntity( 30 + i,  new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_16a."      + tier), i, 16,      false));
            WIRELESS_INPUT_ENERGY_HATCH_64A[i]      = registerPartMetaTileEntity( 45 + i,  new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_64a."      + tier), i, 64,      false));
            WIRELESS_INPUT_ENERGY_HATCH_256A[i]     = registerPartMetaTileEntity( 60 + i,  new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_256a."     + tier), i, 256,     false));
            WIRELESS_INPUT_ENERGY_HATCH_1024A[i]    = registerPartMetaTileEntity( 75 + i,  new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_1024a."    + tier), i, 1024,    false));
            WIRELESS_INPUT_ENERGY_HATCH_4096A[i]    = registerPartMetaTileEntity( 90 + i,  new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_4096a."    + tier), i, 4096,    false));
            WIRELESS_INPUT_ENERGY_HATCH_16384A[i]   = registerPartMetaTileEntity( 105 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_16384a."   + tier), i, 16384,   false));
            WIRELESS_INPUT_ENERGY_HATCH_65536A[i]   = registerPartMetaTileEntity( 120 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_65536a."   + tier), i, 65536,   false));
            WIRELESS_INPUT_ENERGY_HATCH_262144A[i]  = registerPartMetaTileEntity( 135 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_262144a."  + tier), i, 262144,  false));
            WIRELESS_INPUT_ENERGY_HATCH_1048576A[i] = registerPartMetaTileEntity( 150 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_1048576a." + tier), i, 1048576, false));

            WIRELESS_OUTPUT_ENERGY_HATCH[i]          = registerPartMetaTileEntity( 165 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output."          + tier), i, 2,       true));
            WIRELESS_OUTPUT_ENERGY_HATCH_4A[i]       = registerPartMetaTileEntity( 180 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_4a."       + tier), i, 4,       true));
            WIRELESS_OUTPUT_ENERGY_HATCH_16A[i]      = registerPartMetaTileEntity( 195 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_16a."      + tier), i, 16,      true));
            WIRELESS_OUTPUT_ENERGY_HATCH_64A[i]      = registerPartMetaTileEntity( 210 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_64a."      + tier), i, 64,      true));
            WIRELESS_OUTPUT_ENERGY_HATCH_256A[i]     = registerPartMetaTileEntity( 225 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_256a."     + tier), i, 256,     true));
            WIRELESS_OUTPUT_ENERGY_HATCH_1024A[i]    = registerPartMetaTileEntity( 240 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_1024a."    + tier), i, 1024,    true));
            WIRELESS_OUTPUT_ENERGY_HATCH_4096A[i]    = registerPartMetaTileEntity( 255 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_4096a."    + tier), i, 4096,    true));
            WIRELESS_OUTPUT_ENERGY_HATCH_16384A[i]   = registerPartMetaTileEntity( 270 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_16384a."   + tier), i, 16384,   true));
            WIRELESS_OUTPUT_ENERGY_HATCH_65536A[i]   = registerPartMetaTileEntity( 285 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_65536a."   + tier), i, 65536,   true));
            WIRELESS_OUTPUT_ENERGY_HATCH_262144A[i]  = registerPartMetaTileEntity( 300 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_262144a."  + tier), i, 262144,  true));
            WIRELESS_OUTPUT_ENERGY_HATCH_1048576A[i] = registerPartMetaTileEntity( 315 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_1048576a." + tier), i, 1048576, true));
        }
    }


    public static @NotNull ResourceLocation getmyId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
