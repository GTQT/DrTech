package com.drppp.drtech.common.MetaTileEntities;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Tags;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.api.capability.RuMachineAcceptFacing;
import com.drppp.drtech.common.MetaTileEntities.muti.MetaTileEntityIndustrialCokeOven;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.AnnihilationGenerator;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.MetaTileEntityLargeLightningRod;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.NuclearReactor;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.*;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityEnergyTransTower;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetatileEntityTwentyFiveFluidTank;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileEntityBatteryEnergyHatch;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileEntityYotHatch;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileeneityPassthroughHatchComputationHatch;
import com.drppp.drtech.common.MetaTileEntities.single.*;
import com.drppp.drtech.common.MetaTileEntities.single.RuMachine.MetaTileEntityRuMachine;
import com.drppp.drtech.common.MetaTileEntities.single.RuMachine.MetaTileEntityRuSplitter;
import com.drppp.drtech.common.MetaTileEntities.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.drtech.common.MetaTileEntities.single.hu.MetaTileEntityCombustionchamberLiquid;
import com.drppp.drtech.loaders.recipes.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.GTValues.VN;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static gregtech.common.metatileentities.MetaTileEntities.registerSimpleMetaTileEntity;

public class DrTechMetaTileEntities {
    public static final MetaTileEntityBatteryEnergyHatch[] BATTERY_INPUT_ENERGY_HATCH = new MetaTileEntityBatteryEnergyHatch[15];
    public static final MetaTileEntityBatteryEnergyHatch[] BATTERY_INPUT_ENERGY_HATCH_4A = new MetaTileEntityBatteryEnergyHatch[15];
    public static final MetaTileEntityBatteryEnergyHatch[] BATTERY_INPUT_ENERGY_HATCH_16A = new MetaTileEntityBatteryEnergyHatch[15];
    public static final MetaTileEntityBatteryEnergyHatch[] BATTERY_INPUT_ENERGY_HATCH_64A = new MetaTileEntityBatteryEnergyHatch[15];

    public static AnnihilationGenerator ANNIHILATION_GENERATOR;
    public static MetaTileEntityDeepGroundPump DEEP_GROUND_PUMP;
    public static MetaTileEntityDronePad DRONE_PAD;
    public static MetaTileEntityEnergyTransTower TRANS_TOWER;
    public static MetaTileEntityInfiniteFluidDrill INFINITE_FLUID_DRILLING_RIG;
    public static MetaTileEntityLargeAlloySmelter LARGE_ALLOY_SMELTER;
    public static MetaTileEntityLargeMolecularRecombination LARGE_MOLECULAR_RECOMBINATION;

    public static MetaTileEntityLogFactory LOG_FACTORY;
    public static MetaTileEntityMatrixSolver MATRIX_SOLVER;
    public static MetaTileEntityMobsKiller MOB_KILLER;
    public static MetaTileEntitySolarTower SOLAR_TOWER;
    public static MetatileEntityTwentyFiveFluidTank TFFT;
    public static MetaTileEntityUniversalCollector[] UNIVERSAL_COLLECTORS = new MetaTileEntityUniversalCollector[10];
    public static MetaTileEntityYotHatch YOT_HARCH;
    public static MetaTileEntityYotTank YOUT_TANK;
    public static MetaTileEntutyLargeBeeHive LARGE_BEE_HIVE;
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
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_256 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_1024 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_4096 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_16384 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_65536 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_262144 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_1048576 = new MetaTileEntityLaserPipeBending[10]; // IV+

    public static MetaTileEntityRuGenerator RU_GENERATOR;
    public static MetaTileEntityRuMachine RU_MACERATOR;
    public static MetaTileEntityRuMachine RU_MIXER;
    public static MetaTileEntityRuMachine RU_SIFTER;
    public static MetaTileEntityRuMachine RU_COMPRESSOR;
    public static MetaTileEntityRuMachine RU_EXTRUDER;
    public static MetaTileEntityRuMachine RU_WIREMILL;
    public static MetaTileEntityRuMachine RU_HAMMER;
    public static MetaTileEntityRuMachine RU_BENDER;
    public static MetaTileEntityRuMachine RU_CENTRIFUGE;
    public static MetaTileEntityRuSplitter RU_SPLITTER;
    public static MetaTileEntityCombustionchamber[] HU_BURRING_BOXS = new MetaTileEntityCombustionchamber[8];
    public static MetaTileEntityCombustionchamber[] HU_DENSE_BURRING_BOXS = new MetaTileEntityCombustionchamber[8];
    public static MetaTileEntityCombustionchamberLiquid[] HU_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[8];
    public static MetaTileEntityCombustionchamberLiquid[] HU_DENSE_BURRING_BOXS_LIQUID = new MetaTileEntityCombustionchamberLiquid[8];

    static int startID = 0;

    public static int getID() {
        startID++;
        return startID;
    }

    public static void initialization() {
        //Common ID
        ANNIHILATION_GENERATOR = registerMetaTileEntity(getID(), new AnnihilationGenerator(getDrId("annihilation_generator")));
        DEEP_GROUND_PUMP = registerMetaTileEntity(getID(), new MetaTileEntityDeepGroundPump(getDrId("deep_ground_pump")));
        DRONE_PAD = registerMetaTileEntity(getID(), new MetaTileEntityDronePad(getDrId("drone_pad")));
        INFINITE_FLUID_DRILLING_RIG = registerMetaTileEntity(getID(), new MetaTileEntityInfiniteFluidDrill(getDrId("fluid_drilling_rig.iv"), 6));
        LARGE_ALLOY_SMELTER = registerMetaTileEntity(getID(), new MetaTileEntityLargeAlloySmelter(getDrId("large_alloy_smelter")));
        LARGE_BEE_HIVE = registerMetaTileEntity(getID(), new MetaTileEntutyLargeBeeHive(getDrId("large_bee_hive")));

        LARGE_MOLECULAR_RECOMBINATION = registerMetaTileEntity(getID(), new MetaTileEntityLargeMolecularRecombination(getDrId("molecular_recombination")));
        getID();
        LOG_FACTORY = registerMetaTileEntity(getID(), new MetaTileEntityLogFactory(getDrId("log_factory")));
        MATRIX_SOLVER = registerMetaTileEntity(getID(), new MetaTileEntityMatrixSolver(getDrId("matrix_solver")));
        MOB_KILLER = registerMetaTileEntity(getID(), new MetaTileEntityMobsKiller(getDrId("mob_killer")));
        NUCLEAR_GENERATOR = registerMetaTileEntity(getID(), new NuclearReactor(getDrId("nuclear_generator")));
        SOLAR_TOWER = registerMetaTileEntity(getID(), new MetaTileEntitySolarTower(getDrId("solar_tower")));
        TFFT = registerMetaTileEntity(getID(), new MetatileEntityTwentyFiveFluidTank(getDrId("tfft_tank")));
        TRANS_TOWER = registerMetaTileEntity(getID(), new MetaTileEntityEnergyTransTower(getDrId("trans_tower")));
        getID();
        YOT_HARCH = registerMetaTileEntity(getID(), new MetaTileEntityYotHatch(getDrId("yot_hatch")));
        YOUT_TANK = registerMetaTileEntity(getID(), new MetaTileEntityYotTank(getDrId("yot_tank")));
        if (DrtConfig.MachineSwitch.EnableIndustrialMachines) {
            LARGE_EXTRUDER = registerMetaTileEntity(getID(), new MetaTileentityLargeExtruder(getDrId("large_extruder")));
        } else {
            getID();
        }
        PASSTHROUGH_COMPUTER = registerMetaTileEntity(getID(), new MetaTileeneityPassthroughHatchComputationHatch(getDrId("passthrough_computationhatch")));
        INDUSTRIAL_APIARY = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialApiary(getDrId("industrial_apiary"), Textures.INDUSTRIAL_APIARY));
        CONCRETE_BACK_FILLER1 = registerMetaTileEntity(getID(), new MetaTileentityConcreteBackfiller(getDrId("concrete_backfiller1"), 1));
        CONCRETE_BACK_FILLER2 = registerMetaTileEntity(getID(), new MetaTileentityConcreteBackfiller(getDrId("concrete_backfiller2"), 2));
        TYPE_FILTER = registerMetaTileEntity(getID(), new MetaTileEntityTypeFilter(getDrId("type_filter")));
        INDUSTRIAL_COKE_OVEN = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCokeOven(getDrId("industrial_coke_oven")));
        LARGE_LIGHTING_ROD = registerMetaTileEntity(getID(), new MetaTileEntityLargeLightningRod(getDrId("large_lighting_rod")));
        COMB_PROVESS = registerMetaTileEntity(getID(), new MetaTileEntityCombProcess(getDrId("comb_process")));
        if (DrtConfig.MachineSwitch.EnableIndustrialMachines) {
            INDUSTRIAL_MIXER = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialMixer(getDrId("industrial_mixer")));
            INDUSTRIAL_ROLLER_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialRollerPress(getDrId("industrial_roller_press")));
            INDUSTRIAL_CABLE_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCablePress(getDrId("industrial_cable_press")));
            INDUSTRIAL_SIEVE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialSieve(getDrId("industrial_sieve")));
            INDUSTRIAL_CENTRIFUGE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCentrifuge(getDrId("industrial_centrifuge")));
        } else {
            getID();
            getID();
            getID();
            getID();
            getID();
        }
        getID();
        RU_GENERATOR = registerMetaTileEntity(getID(), new MetaTileEntityRuGenerator(getDrId("ru_generator")));

        RU_MACERATOR = registerMetaTileEntity(getID(), new MetaTileEntityRuMachine(getDrId("ru_macerator"), RecipeMaps.MACERATOR_RECIPES, Textures.RU_SHREDDER, 8, true, new RuMachineAcceptFacing[]{RuMachineAcceptFacing.LEFT, RuMachineAcceptFacing.RIGHT}));
        RU_MIXER = registerMetaTileEntity(getID(), new MetaTileEntityRuMachine(getDrId("ru_mixer"), RecipeMaps.MIXER_RECIPES, Textures.RU_MIXER, 8, true, new RuMachineAcceptFacing[]{RuMachineAcceptFacing.DOWN}));
        RU_SIFTER = registerMetaTileEntity(getID(), new MetaTileEntityRuMachine(getDrId("ru_sifter"), RecipeMaps.SIFTER_RECIPES, Textures.RU_SIFTER, 8, true, new RuMachineAcceptFacing[]{RuMachineAcceptFacing.LEFT, RuMachineAcceptFacing.RIGHT}));
        RU_COMPRESSOR = registerMetaTileEntity(getID(), new MetaTileEntityRuMachine(getDrId("ru_compressor"), RecipeMaps.COMPRESSOR_RECIPES, Textures.RU_COMPRESSOR, 8, true, new RuMachineAcceptFacing[]{RuMachineAcceptFacing.BACK}));
        RU_EXTRUDER = registerMetaTileEntity(getID(), new MetaTileEntityRuMachine(getDrId("ru_extruder"), RecipeMaps.EXTRACTOR_RECIPES, Textures.RU_EXTRUDER, 8, true, new RuMachineAcceptFacing[]{RuMachineAcceptFacing.LEFT, RuMachineAcceptFacing.RIGHT}));
        RU_HAMMER = registerMetaTileEntity(getID(), new MetaTileEntityRuMachine(getDrId("ru_hammer"), RecipeMaps.FORGE_HAMMER_RECIPES, Textures.RU_HAMMER, 8, true, new RuMachineAcceptFacing[]{RuMachineAcceptFacing.BACK}));
        RU_WIREMILL = registerMetaTileEntity(getID(), new MetaTileEntityRuMachine(getDrId("ru_wiremill"), RecipeMaps.WIREMILL_RECIPES, Textures.RU_WIREMILL, 8, true, new RuMachineAcceptFacing[]{RuMachineAcceptFacing.LEFT, RuMachineAcceptFacing.RIGHT}));
        RU_BENDER = registerMetaTileEntity(getID(), new MetaTileEntityRuMachine(getDrId("ru_bender"), RecipeMaps.BENDER_RECIPES, Textures.RU_BENDER, 8, true, new RuMachineAcceptFacing[]{RuMachineAcceptFacing.LEFT, RuMachineAcceptFacing.RIGHT}));
        RU_CENTRIFUGE = registerMetaTileEntity(getID(), new MetaTileEntityRuMachine(getDrId("ru_centrifuge"), RecipeMaps.CENTRIFUGE_RECIPES, Textures.RU_CENTRIFUGE, 8, true, new RuMachineAcceptFacing[]{RuMachineAcceptFacing.DOWN}));
        RU_SPLITTER = registerMetaTileEntity(getID(), new MetaTileEntityRuSplitter(getDrId("ru_splitter")));
        for (int i = 0; i < HU_BURRING_BOXS.length; i++) {
            String[] names = {"qian", "qingtong", "gang", "yingang", "ge", "tai", "wu", "wugang"};
            int[] color = {0x251945, 0x815024, 0x4F4F4E, 0x87875C, 0xA39393, 0x896495, 0x1D1D1D, 0x3C3C61};
            double[] efficiency = {0.5, 0.75, 0.7, 1, 0.85, 0.85, 1, 0.9};
            int[] output = {16, 24, 32, 16, 112, 96, 128, 128};
            HU_BURRING_BOXS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamber(getDrId(names[i] + "_burring_box"), color[i], efficiency[i], output[i], false));
            HU_DENSE_BURRING_BOXS[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamber(getDrId("dense_" + names[i] + "_burring_box"), color[i], efficiency[i], output[i] * 4, true));
            HU_BURRING_BOXS_LIQUID[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamberLiquid(getDrId(names[i] + "_burring_box_liquid"), color[i], efficiency[i], (int) (output[i] * 1.5), false));
            HU_DENSE_BURRING_BOXS_LIQUID[i] = registerMetaTileEntity(getID(), new MetaTileEntityCombustionchamberLiquid(getDrId("dense_" + names[i] + "_burring_box_liquid"), color[i], efficiency[i], (int) (output[i] * 4 * 1.5), true));
        }

        //人工分配 ID
        for (int i = 0; i < 10; i++) {
            String tierName = GTValues.VN[i].toLowerCase();
            UNIVERSAL_COLLECTORS[i] = registerMetaTileEntity(500 + i, new MetaTileEntityUniversalCollector(getDrId("universal_collector." + tierName), i + 1, gregtech.client.renderer.texture.Textures.GAS_COLLECTOR_OVERLAY));
        }

        registerSimpleMetaTileEntity(DISASSEMBLY, 510, "disassembly", DrtechReceipes.DISASSEMBLER_RECIPES, Textures.DISASSEMBLY, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);

        //17630开始 激光折弯
        for (int i = 0; i < 10; i++) {
            LASER_BENDING_256[i] = registerMetaTileEntity(530 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_256." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 256));
            LASER_BENDING_1024[i] = registerMetaTileEntity(540 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_1024." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 1024));
            LASER_BENDING_4096[i] = registerMetaTileEntity(550 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_4096." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 4096));
            LASER_BENDING_16384[i] = registerMetaTileEntity(560 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_16384." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 16384));
            LASER_BENDING_65536[i] = registerMetaTileEntity(570 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_65536." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 65536));
            LASER_BENDING_262144[i] = registerMetaTileEntity(580 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_262144." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 262144));
            LASER_BENDING_1048576[i] = registerMetaTileEntity(590 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_1048576." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 1048576));



        }

        for (int i = 0; i < 15; i++) {
            String tier = VN[i].toLowerCase();
            BATTERY_INPUT_ENERGY_HATCH[i] = registerMetaTileEntity(600 + i, new MetaTileEntityBatteryEnergyHatch(getDrId("battery_energy_hatch.input." + tier), i, 2, false));
            BATTERY_INPUT_ENERGY_HATCH_4A[i]= registerMetaTileEntity(615 + i, new MetaTileEntityBatteryEnergyHatch(getDrId("battery_energy_hatch.input_4a." + tier), i, 4, false));
            BATTERY_INPUT_ENERGY_HATCH_16A[i]= registerMetaTileEntity(630 + i, new MetaTileEntityBatteryEnergyHatch(getDrId("battery_energy_hatch.input_16a." + tier), i, 16, false));
            BATTERY_INPUT_ENERGY_HATCH_64A[i]= registerMetaTileEntity(645 + i, new MetaTileEntityBatteryEnergyHatch(getDrId("battery_energy_hatch.input_64a." + tier), i, 64, false));
        }

    }


    public static @NotNull ResourceLocation getDrId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
