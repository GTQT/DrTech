package com.drppp.drtech.common.MetaTileEntities;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Tags;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.common.MetaTileEntities.muti.MetaTileEntityIndustrialCokeOven;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.AnnihilationGenerator;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.MeTaTileEntityWindDrivenGenerator;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.MetaTileEntityLargeLightningRod;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.NuclearReactor;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.*;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityEnergyTransTower;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetatileEntityTwentyFiveFluidTank;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.*;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityIndustrialApiary;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityLaserPipeBending;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityTypeFilter;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityUniversalCollector;
import com.drppp.drtech.loaders.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityLaserHatch;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.GTValues.VN;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static gregtech.common.metatileentities.MetaTileEntities.registerSimpleMetaTileEntity;

public class MetaTileEntities {
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
    public static final MetaTileEntityBatteryEnergyHatch[] BATTERY_INPUT_ENERGY_HATCH = new MetaTileEntityBatteryEnergyHatch[15];
    public static final MetaTileentityItemAndFluidHatch[] ITEM_FLUID_IMPORT_HATCH = new MetaTileentityItemAndFluidHatch[15];
    public static final MetaTileentityItemAndFluidHatch[] ITEM_FLUID_EXPORT_HATCH = new MetaTileentityItemAndFluidHatch[15];
    public static AnnihilationGenerator ANNIHILATION_GENERATOR;
    public static MetaTileEntityAdvancedProsscessArray ADVANCED_PROCESS_ARRAY;
    public static MetaTileEntityDeepGroundPump DEEP_GROUND_PUMP;
    public static MetaTileEntityDronePad DRONE_PAD;
    public static MetaTileEntityElectricImplosionCompressor LARGE_LARGE;
    public static MetaTileEntityEnergyTransTower TRANS_TOWER;
    public static MetaTileEntityInfiniteFluidDrill INFINITE_FLUID_DRILLING_RIG;
    public static MetaTileEntityLargeAlloySmelter LARGE_ALLOY_SMELTER;
    public static MetaTileEntityLargeMolecularRecombination LARGE_MOLECULAR_RECOMBINATION;
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
    public static MetaTileEntityBeneathTrans BENEATH_TRANS;
    public static NuclearReactor NUCLEAR_GENERATOR;
    public static SimpleMachineMetaTileEntity[] DISASSEMBLY = new SimpleMachineMetaTileEntity[10];
    public static MetaTileentityLargeExtruder LARGE_EXTRUDER;
    public static MetaTileeneityPassthroughHatchComputationHatch PASSTHROUGH_COMPUTER;
    public static MetaTileEntityIndustrialApiary INDUSTRIAL_APIARY;
    public static MetaTileentityConcreteBackfiller CONCRETE_BACK_FILLER1;
    public static MetaTileentityConcreteBackfiller CONCRETE_BACK_FILLER2;
    public static MetaTileEntityTypeFilter TYPE_FILTER;
    public static MetaTileEntityIndustrialCokeOven INDUSTRIAL_COKE_OVEN;
    public static MetaTileEntityLargeLightningRod LARGE_LIGHTING_ROD;
    public static MetaTileEntityCombProcess COMB_PROVESS;
    public static MetaTileEntityIndustrialMixer INDUSTRIAL_MIXER;
    public static MetaTileEntityIndustrialRollerPress INDUSTRIAL_ROLLER_PRESS;
    public static MetaTileEntityIndustrialCablePress INDUSTRIAL_CABLE_PRESS;
    public static MetaTileEntityIndustrialSieve INDUSTRIAL_SIEVE;
    public static MetaTileEntityIndustrialCentrifuge INDUSTRIAL_CENTRIFUGE;
    public static MetaTileEntityRocketLaunchPad ROCKET_LAUNCH_PAD;
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_256 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_1024 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_4096 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_16384 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_65536 = new MetaTileEntityLaserPipeBending[10]; // IV+
    static int startID = 16999;

    private static <F extends MetaTileEntity> F registerPartMetaTileEntity(int id, F mte) {
        if (id > 1000)
            return null;
        return registerMetaTileEntity(id + 17300, mte);
    }

    public static int getID() {
        startID++;
        return startID;
    }

    public static void Init() {
        String tierName;
        int endPos = GregTechAPI.isHighTier() ? LASER_OUTPUT_HATCH_16384.length - 1 :
                Math.min(LASER_OUTPUT_HATCH_16384.length - 1, GTValues.UHV - GTValues.IV);

        //Common ID 17000
        ADVANCED_PROCESS_ARRAY = registerMetaTileEntity(getID(), new MetaTileEntityAdvancedProsscessArray(getmyId("advanced_process_array"), 1));
        ANNIHILATION_GENERATOR = registerMetaTileEntity(getID(), new AnnihilationGenerator(getmyId("annihilation_generator")));
        DEEP_GROUND_PUMP = registerMetaTileEntity(getID(), new MetaTileEntityDeepGroundPump(getmyId("deep_ground_pump")));
        DRONE_PAD = registerMetaTileEntity(getID(), new MetaTileEntityDronePad(getmyId("drone_pad")));
        INFINITE_FLUID_DRILLING_RIG = registerMetaTileEntity(getID(), new MetaTileEntityInfiniteFluidDrill(getmyId("fluid_drilling_rig.iv"), 6));
        LARGE_ALLOY_SMELTER = registerMetaTileEntity(getID(), new MetaTileEntityLargeAlloySmelter(getmyId("large_alloy_smelter")));
        LARGE_BEE_HIVE = registerMetaTileEntity(getID(), new MetaTileEntutyLargeBeeHive(getmyId("large_bee_hive")));
        BENEATH_TRANS = registerMetaTileEntity(getID(), new MetaTileEntityBeneathTrans(getmyId("beneath_trans")));
        LARGE_LARGE = registerMetaTileEntity(getID(), new MetaTileEntityElectricImplosionCompressor(getmyId("electric_implosion_compressor")));
        LARGE_MOLECULAR_RECOMBINATION = registerMetaTileEntity(getID(), new MetaTileEntityLargeMolecularRecombination(getmyId("molecular_recombination")));
        getID();
        LOG_FACTORY = registerMetaTileEntity(getID(), new MetaTileEntityLogFactory(getmyId("log_factory")));
        MATRIX_SOLVER = registerMetaTileEntity(getID(), new MetaTileEntityMatrixSolver(getmyId("matrix_solver")));
        MOB_KILLER = registerMetaTileEntity(getID(), new MetaTileEntityMobsKiller(getmyId("mob_killer")));
        NUCLEAR_GENERATOR = registerMetaTileEntity(getID(), new NuclearReactor(getmyId("nuclear_generator")));
        PLAYER_BEACON = registerMetaTileEntity(getID(), new MetaTileEntityPlayerBeacon(getmyId("player_beacon")));
        SOLAR_TOWER = registerMetaTileEntity(getID(), new MetaTileEntitySolarTower(getmyId("solar_tower")));
        TFFT = registerMetaTileEntity(getID(), new MetatileEntityTwentyFiveFluidTank(getmyId("tfft_tank")));
        TRANS_TOWER = registerMetaTileEntity(getID(), new MetaTileEntityEnergyTransTower(getmyId("trans_tower")));
        WIND_DRIVEN_GENERATOR_HV = registerMetaTileEntity(getID(), new MeTaTileEntityWindDrivenGenerator(getmyId("wind_driven_generator")));
        YOT_HARCH = registerMetaTileEntity(getID(), new MetaTileEntityYotHatch(getmyId("yot_hatch")));
        YOUT_TANK = registerMetaTileEntity(getID(), new MetaTileEntityYotTank(getmyId("yot_tank")));
        if (DrtConfig.MachineSwitch.EnableIndustrialMachines) {
            LARGE_EXTRUDER = registerMetaTileEntity(getID(), new MetaTileentityLargeExtruder(getmyId("large_extruder")));
        } else {
            getID();
        }
        PASSTHROUGH_COMPUTER = registerMetaTileEntity(getID(), new MetaTileeneityPassthroughHatchComputationHatch(getmyId("passthrough_computationhatch")));
        INDUSTRIAL_APIARY = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialApiary(getmyId("industrial_apiary"), Textures.INDUSTRIAL_APIARY));
        CONCRETE_BACK_FILLER1 = registerMetaTileEntity(getID(), new MetaTileentityConcreteBackfiller(getmyId("concrete_backfiller1"), 1));
        CONCRETE_BACK_FILLER2 = registerMetaTileEntity(getID(), new MetaTileentityConcreteBackfiller(getmyId("concrete_backfiller2"), 2));
        TYPE_FILTER = registerMetaTileEntity(getID(), new MetaTileEntityTypeFilter(getmyId("type_filter")));
        INDUSTRIAL_COKE_OVEN = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCokeOven(getmyId("industrial_coke_oven")));
        LARGE_LIGHTING_ROD = registerMetaTileEntity(getID(), new MetaTileEntityLargeLightningRod(getmyId("large_lighting_rod")));
        COMB_PROVESS = registerMetaTileEntity(getID(), new MetaTileEntityCombProcess(getmyId("comb_process")));
        if (DrtConfig.MachineSwitch.EnableIndustrialMachines) {
            INDUSTRIAL_MIXER = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialMixer(getmyId("industrial_mixer")));
            INDUSTRIAL_ROLLER_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialRollerPress(getmyId("industrial_roller_press")));
            INDUSTRIAL_CABLE_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCablePress(getmyId("industrial_cable_press")));
            INDUSTRIAL_SIEVE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialSieve(getmyId("industrial_sieve")));
            INDUSTRIAL_CENTRIFUGE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCentrifuge(getmyId("industrial_centrifuge")));
        } else {
            getID();
            getID();
            getID();
            getID();
            getID();
        }
        ROCKET_LAUNCH_PAD = registerMetaTileEntity(getID(), new MetaTileEntityRocketLaunchPad(getmyId("rocket_launch_pad")));
        //人工分配 ID 17100
        for (int i = 0; i < 10; i++) {
            tierName = GTValues.VN[i].toLowerCase();
            UNIVERSAL_COLLECTORS[i] = registerMetaTileEntity(17100 + i, new MetaTileEntityUniversalCollector(getmyId("universal_collector." + tierName), i + 1, gregtech.client.renderer.texture.Textures.GAS_COLLECTOR_OVERLAY));
        }

        for (int i = 0; i < endPos; i++) {
            int v = i + GTValues.IV;
            String voltageName = GTValues.VN[v].toLowerCase();
            LASER_OUTPUT_HATCH_16384[i] = registerMetaTileEntity(17115 + i, new MetaTileEntityLaserHatch(
                    getmyId("laser_hatch.target_16384a." + voltageName), false, v, 16384));
            LASER_OUTPUT_HATCH_16384[i] = registerMetaTileEntity(17130 + i,
                    new MetaTileEntityLaserHatch(getmyId("laser_hatch.source_16384a." + voltageName), true, v, 16384));
        }
        registerSimpleMetaTileEntity(DISASSEMBLY, 17230, "disassembly", DrtechReceipes.DISASSEMBLER_RECIPES, Textures.DISASSEMBLY, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);


        //  ULV-MAX Wireless Energy/Dynamo Hatch (consist of high-amp version) ID 17300
        for (int i = 0; i < 15; i++) {
            String tier = VN[i].toLowerCase();
            WIRELESS_INPUT_ENERGY_HATCH[i] = registerPartMetaTileEntity(i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input." + tier), i, 2, false));
            WIRELESS_INPUT_ENERGY_HATCH_4A[i] = registerPartMetaTileEntity(15 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_4a." + tier), i, 4, false));
            WIRELESS_INPUT_ENERGY_HATCH_16A[i] = registerPartMetaTileEntity(30 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_16a." + tier), i, 16, false));
            WIRELESS_INPUT_ENERGY_HATCH_64A[i] = registerPartMetaTileEntity(45 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_64a." + tier), i, 64, false));
            WIRELESS_INPUT_ENERGY_HATCH_256A[i] = registerPartMetaTileEntity(60 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_256a." + tier), i, 256, false));
            WIRELESS_INPUT_ENERGY_HATCH_1024A[i] = registerPartMetaTileEntity(75 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_1024a." + tier), i, 1024, false));
            WIRELESS_INPUT_ENERGY_HATCH_4096A[i] = registerPartMetaTileEntity(90 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_4096a." + tier), i, 4096, false));
            WIRELESS_INPUT_ENERGY_HATCH_16384A[i] = registerPartMetaTileEntity(105 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_16384a." + tier), i, 16384, false));
            WIRELESS_INPUT_ENERGY_HATCH_65536A[i] = registerPartMetaTileEntity(120 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_65536a." + tier), i, 65536, false));
            WIRELESS_INPUT_ENERGY_HATCH_262144A[i] = registerPartMetaTileEntity(135 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_262144a." + tier), i, 262144, false));
            WIRELESS_INPUT_ENERGY_HATCH_1048576A[i] = registerPartMetaTileEntity(150 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.input_1048576a." + tier), i, 1048576, false));

            WIRELESS_OUTPUT_ENERGY_HATCH[i] = registerPartMetaTileEntity(165 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output." + tier), i, 2, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_4A[i] = registerPartMetaTileEntity(180 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_4a." + tier), i, 4, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_16A[i] = registerPartMetaTileEntity(195 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_16a." + tier), i, 16, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_64A[i] = registerPartMetaTileEntity(210 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_64a." + tier), i, 64, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_256A[i] = registerPartMetaTileEntity(225 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_256a." + tier), i, 256, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_1024A[i] = registerPartMetaTileEntity(240 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_1024a." + tier), i, 1024, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_4096A[i] = registerPartMetaTileEntity(255 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_4096a." + tier), i, 4096, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_16384A[i] = registerPartMetaTileEntity(270 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_16384a." + tier), i, 16384, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_65536A[i] = registerPartMetaTileEntity(285 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_65536a." + tier), i, 65536, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_262144A[i] = registerPartMetaTileEntity(300 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_262144a." + tier), i, 262144, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_1048576A[i] = registerPartMetaTileEntity(315 + i, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.output_1048576a." + tier), i, 1048576, true));
        }
        //17630开始 激光折弯
        for (int i = 0; i < 10; i++) {
            LASER_BENDING_256[i] = registerMetaTileEntity(17630 + i, new MetaTileEntityLaserPipeBending(getmyId("laser_bending_256." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 256));
            LASER_BENDING_1024[i] = registerMetaTileEntity(17640 + i, new MetaTileEntityLaserPipeBending(getmyId("laser_bending_1024." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 1024));
            LASER_BENDING_4096[i] = registerMetaTileEntity(17650 + i, new MetaTileEntityLaserPipeBending(getmyId("laser_bending_4096." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 4096));
            LASER_BENDING_16384[i] = registerMetaTileEntity(17660 + i, new MetaTileEntityLaserPipeBending(getmyId("laser_bending_16384." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 16384));
            LASER_BENDING_65536[i] = registerMetaTileEntity(17670 + i, new MetaTileEntityLaserPipeBending(getmyId("laser_bending_65536." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 65536));
        }
        for (int i = 0; i < 15; i++) {
            String tier = VN[i].toLowerCase();
            BATTERY_INPUT_ENERGY_HATCH[i] = registerMetaTileEntity(17680 + i, new MetaTileEntityBatteryEnergyHatch(getmyId("battery_energy_hatch.input." + tier), i, 2, false));
        }
        for (int i = 0; i < 15; i++) {
            String tier = VN[i].toLowerCase();
            ITEM_FLUID_IMPORT_HATCH[i] = registerMetaTileEntity(17695 + i, new MetaTileentityItemAndFluidHatch(getmyId("item_and_fluid_import_hatch_" + tier), i, false));
            MultiblockAbility.REGISTRY.get(MultiblockAbility.IMPORT_ITEMS).add(ITEM_FLUID_IMPORT_HATCH[i]);
            MultiblockAbility.REGISTRY.get(MultiblockAbility.IMPORT_FLUIDS).add(ITEM_FLUID_IMPORT_HATCH[i]);
            ITEM_FLUID_EXPORT_HATCH[i] = registerMetaTileEntity(17710 + i, new MetaTileentityItemAndFluidHatch(getmyId("item_and_fluid_export_hatch_" + tier), i, true));
            MultiblockAbility.REGISTRY.get(MultiblockAbility.EXPORT_ITEMS).add(ITEM_FLUID_EXPORT_HATCH[i]);
            MultiblockAbility.REGISTRY.get(MultiblockAbility.EXPORT_FLUIDS).add(ITEM_FLUID_EXPORT_HATCH[i]);
        }
    }


    public static @NotNull ResourceLocation getmyId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
