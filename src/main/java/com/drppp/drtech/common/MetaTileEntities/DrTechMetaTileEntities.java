package com.drppp.drtech.common.MetaTileEntities;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Tags;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.AnnihilationGenerator;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.MetaTileEntityLargeLightningRod;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.*;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityEnergyTransTower;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetatileEntityTwentyFiveFluidTank;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileEntityYotHatch;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityLaserPipeBending;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityUniversalCollector;
import com.drppp.drtech.loaders.recipes.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static gregtech.common.metatileentities.MetaTileEntities.registerSimpleMetaTileEntity;

public class DrTechMetaTileEntities {

    public static AnnihilationGenerator ANNIHILATION_GENERATOR;
    public static MetaTileEntityDronePad DRONE_PAD;
    public static MetaTileEntityEnergyTransTower TRANS_TOWER;
    public static MetaTileEntityInfiniteFluidDrill INFINITE_FLUID_DRILLING_RIG;
    public static MetaTileEntityLargeAlloySmelter LARGE_ALLOY_SMELTER;

    public static MetaTileEntityLogFactory LOG_FACTORY;
    public static MetaTileEntityMatrixSolver MATRIX_SOLVER;
    public static MetaTileEntityMobsKiller MOB_KILLER;
    public static MetaTileEntitySolarTower SOLAR_TOWER;
    public static MetatileEntityTwentyFiveFluidTank TFFT;
    public static MetaTileEntityUniversalCollector[] UNIVERSAL_COLLECTORS = new MetaTileEntityUniversalCollector[10];
    public static MetaTileEntityYotHatch YOT_HARCH;
    public static MetaTileEntityYotTank YOUT_TANK;
    public static MetaTileEntutyLargeBeeHive LARGE_BEE_HIVE;
    public static SimpleMachineMetaTileEntity[] DISASSEMBLY = new SimpleMachineMetaTileEntity[10];
    public static MetaTileentityLargeExtruder LARGE_EXTRUDER;

    public static MetaTileentityConcreteBackfiller CONCRETE_BACK_FILLER1;
    public static MetaTileentityConcreteBackfiller CONCRETE_BACK_FILLER2;
    public static MetaTileEntityLargeLightningRod LARGE_LIGHTING_ROD;
    public static MetaTileEntityCombProcess COMB_PROVESS;
    public static MetaTileEntityIndustrialMixer INDUSTRIAL_MIXER;
    public static MetaTileEntityIndustrialRollerPress INDUSTRIAL_ROLLER_PRESS;
    public static MetaTileEntityIndustrialCablePress INDUSTRIAL_CABLE_PRESS;
    public static MetaTileEntityIndustrialSieve INDUSTRIAL_SIEVE;
    public static MetaTileEntityIndustrialCentrifuge INDUSTRIAL_CENTRIFUGE;

    public static MetaTileentityCropsSimulateMachine CROPS_SIMULATE;
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_256 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_1024 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_4096 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_16384 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_65536 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_262144 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_1048576 = new MetaTileEntityLaserPipeBending[10]; // IV+

    static int startID = 0;

    public static int getID() {
        startID++;
        return startID;
    }

    public static void initialization() {

        //人工分配 ID
        for (int i = 0; i < 10; i++) {
            String tierName = GTValues.VN[i].toLowerCase();
            UNIVERSAL_COLLECTORS[i] = registerMetaTileEntity(100 + i, new MetaTileEntityUniversalCollector(getDrId("universal_collector." + tierName), i + 1, gregtech.client.renderer.texture.Textures.GAS_COLLECTOR_OVERLAY));
        }

        registerSimpleMetaTileEntity(DISASSEMBLY, 110, "disassembly", DrtechReceipes.DISASSEMBLER_RECIPES, Textures.DISASSEMBLY, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);

        //激光折弯
        for (int i = 0; i < 10; i++) {
            LASER_BENDING_256[i] = registerMetaTileEntity(130 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_256." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 256));
            LASER_BENDING_1024[i] = registerMetaTileEntity(140 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_1024." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 1024));
            LASER_BENDING_4096[i] = registerMetaTileEntity(150 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_4096." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 4096));
            LASER_BENDING_16384[i] = registerMetaTileEntity(160 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_16384." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 16384));
            LASER_BENDING_65536[i] = registerMetaTileEntity(170 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_65536." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 65536));
            LASER_BENDING_262144[i] = registerMetaTileEntity(180 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_262144." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 262144));
            LASER_BENDING_1048576[i] = registerMetaTileEntity(190 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_1048576." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 1048576));
        }


        //Common ID
        startID = 500;

        ANNIHILATION_GENERATOR = registerMetaTileEntity(getID(), new AnnihilationGenerator(getDrId("annihilation_generator")));

        DRONE_PAD = registerMetaTileEntity(getID(), new MetaTileEntityDronePad(getDrId("drone_pad")));
        INFINITE_FLUID_DRILLING_RIG = registerMetaTileEntity(getID(), new MetaTileEntityInfiniteFluidDrill(getDrId("fluid_drilling_rig.iv"), 6));
        LARGE_ALLOY_SMELTER = registerMetaTileEntity(getID(), new MetaTileEntityLargeAlloySmelter(getDrId("large_alloy_smelter")));
        LARGE_BEE_HIVE = registerMetaTileEntity(getID(), new MetaTileEntutyLargeBeeHive(getDrId("large_bee_hive")));
        LOG_FACTORY = registerMetaTileEntity(getID(), new MetaTileEntityLogFactory(getDrId("log_factory")));
        MATRIX_SOLVER = registerMetaTileEntity(getID(), new MetaTileEntityMatrixSolver(getDrId("matrix_solver")));
        MOB_KILLER = registerMetaTileEntity(getID(), new MetaTileEntityMobsKiller(getDrId("mob_killer")));

        SOLAR_TOWER = registerMetaTileEntity(getID(), new MetaTileEntitySolarTower(getDrId("solar_tower")));
        TFFT = registerMetaTileEntity(getID(), new MetatileEntityTwentyFiveFluidTank(getDrId("tfft_tank")));
        TRANS_TOWER = registerMetaTileEntity(getID(), new MetaTileEntityEnergyTransTower(getDrId("trans_tower")));
        YOT_HARCH = registerMetaTileEntity(getID(), new MetaTileEntityYotHatch(getDrId("yot_hatch")));
        YOUT_TANK = registerMetaTileEntity(getID(), new MetaTileEntityYotTank(getDrId("yot_tank")));
        CONCRETE_BACK_FILLER1 = registerMetaTileEntity(getID(), new MetaTileentityConcreteBackfiller(getDrId("concrete_backfiller1"), 1));
        CONCRETE_BACK_FILLER2 = registerMetaTileEntity(getID(), new MetaTileentityConcreteBackfiller(getDrId("concrete_backfiller2"), 2));
        startID++;
        LARGE_LIGHTING_ROD = registerMetaTileEntity(getID(), new MetaTileEntityLargeLightningRod(getDrId("large_lighting_rod")));
        COMB_PROVESS = registerMetaTileEntity(getID(), new MetaTileEntityCombProcess(getDrId("comb_process")));

        CROPS_SIMULATE = registerMetaTileEntity(getID(), new MetaTileentityCropsSimulateMachine(getDrId("crops_simulate_machine")));

        if (DrtConfig.MachineSwitch.EnableIndustrialMachines) {
            INDUSTRIAL_MIXER = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialMixer(getDrId("industrial_mixer")));
            INDUSTRIAL_ROLLER_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialRollerPress(getDrId("industrial_roller_press")));
            INDUSTRIAL_CABLE_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCablePress(getDrId("industrial_cable_press")));
            INDUSTRIAL_SIEVE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialSieve(getDrId("industrial_sieve")));
            INDUSTRIAL_CENTRIFUGE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCentrifuge(getDrId("industrial_centrifuge")));
            LARGE_EXTRUDER = registerMetaTileEntity(getID(), new MetaTileentityLargeExtruder(getDrId("large_extruder")));
        }
    }


    public static @NotNull ResourceLocation getDrId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
