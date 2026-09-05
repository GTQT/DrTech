package com.drppp.drtech.common.MetaTileEntities;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Tags;
import com.drppp.drtech.api.Utils.DrtechUtils;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneDock;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneFleetController;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneEndpoint;
import com.drppp.drtech.common.drone.network.DroneEndpoint;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneProgrammer;
import com.drppp.drtech.common.drone.machine.MetaTileEntityDroneRedstoneEmitter;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.AnnihilationGenerator;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.MetaTileEntityFusionReactor;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.MetaTileEntityLargeLightningRod;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityEnergyTransTower;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.store.MetatileEntityTwentyFiveFluidTank;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.*;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileEntityYotHatch;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityIndustrialApiary;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityLaserPipeBending;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityLightsaberAssembler;
import com.drppp.drtech.common.MetaTileEntities.single.MetaTileEntityUniversalCollector;
import com.drppp.drtech.loaders.recipes.DrtechReceipes;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntities;
import static gregtech.common.metatileentities.MetaTileEntities.registerSimpleMetaTileEntity;

public class DrTechMetaTileEntities {

    private static final int DRONE_PROGRAMMER_META_ID = 900;
    private static final int DRONE_DOCK_META_ID = 901;
    private static final int DRONE_REDSTONE_EMITTER_META_ID = 902;
    private static final int DRONE_DOCK_EV_META_ID = 903;
    private static final int DRONE_DOCK_IV_META_ID = 904;
    private static final int DRONE_FLEET_CONTROLLER_META_ID = 905;
    private static final int DRONE_ITEM_ENDPOINT_META_ID = 906;
    private static final int DRONE_FLUID_ENDPOINT_META_ID = 907;
    private static final int DRONE_EU_ENDPOINT_META_ID = 908;

    // 已全部适配 gregtech-gtqt-1.12.2-1.9.0 标准 API 并恢复启用的机器字段
    public static MetaTileEntity ANNIHILATION_GENERATOR;
    public static MetaTileEntityFusionReactor FUSION_REACTOR;
    public static MetaTileEntity DRONE_PAD;
    public static MetaTileEntity TRANS_TOWER;
    public static MetaTileEntity INFINITE_FLUID_DRILLING_RIG;
    public static MetaTileEntity LARGE_ALLOY_SMELTER;

    public static MetaTileEntity MOB_KILLER;
    public static MetaTileEntity SOLAR_TOWER;
    public static MetaTileEntity PLAYER_BEACON;
    public static MetaTileEntity TFFT;
    public static MetaTileEntityUniversalCollector[] UNIVERSAL_COLLECTORS = new MetaTileEntityUniversalCollector[10];
    public static MetaTileEntity YOT_HARCH;
    public static MetaTileEntity YOUT_TANK;
    public static MetaTileEntity LARGE_BEE_HIVE;
    public static SimpleMachineMetaTileEntity[] DISASSEMBLY = new SimpleMachineMetaTileEntity[10];
    public static SimpleMachineMetaTileEntity[] LIGHTSABER_ASSEMBLER = new SimpleMachineMetaTileEntity[10];
    public static MetaTileentityLargeExtruder LARGE_EXTRUDER;

    public static MetaTileEntity CONCRETE_BACK_FILLER1;
    public static MetaTileEntity CONCRETE_BACK_FILLER2;
    public static MetaTileEntity LARGE_LIGHTING_ROD;
    public static MetaTileEntityCombProcess COMB_PROVESS;
    public static MetaTileEntityIndustrialMixer INDUSTRIAL_MIXER;
    public static MetaTileEntityIndustrialRollerPress INDUSTRIAL_ROLLER_PRESS;
    public static MetaTileEntityIndustrialCablePress INDUSTRIAL_CABLE_PRESS;
    public static MetaTileEntityIndustrialSieve INDUSTRIAL_SIEVE;
    public static MetaTileEntityIndustrialCentrifuge INDUSTRIAL_CENTRIFUGE;

    public static MetaTileEntity CROPS_SIMULATE;
    public static MetaTileEntityIndustrialApiary INDUSTRIAL_APIARY;
    public static MetaTileEntityDroneProgrammer DRONE_PROGRAMMER;
    public static MetaTileEntityDroneDock DRONE_DOCK;
    public static MetaTileEntityDroneDock DRONE_DOCK_EV;
    public static MetaTileEntityDroneDock DRONE_DOCK_IV;
    public static MetaTileEntityDroneRedstoneEmitter DRONE_REDSTONE_EMITTER;
    public static MetaTileEntityDroneFleetController DRONE_FLEET_CONTROLLER;
    public static MetaTileEntityDroneEndpoint DRONE_ITEM_ENDPOINT;
    public static MetaTileEntityDroneEndpoint DRONE_FLUID_ENDPOINT;
    public static MetaTileEntityDroneEndpoint DRONE_EU_ENDPOINT;
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
            UNIVERSAL_COLLECTORS[i] = registerMetaTileEntity(100 + i, new MetaTileEntityUniversalCollector(getDrId("universal_collector." + tierName), i + 1));
        }

        registerSimpleMetaTileEntity(DISASSEMBLY, 110, "disassembly", DrtechReceipes.DISASSEMBLER_RECIPES, Textures.DISASSEMBLY, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);
        registerMetaTileEntities(LIGHTSABER_ASSEMBLER, 120, "lightsaber_assembler", (tier, tierName) ->
                new MetaTileEntityLightsaberAssembler(
                        DrtechUtils.getRL(String.format("lightsaber_assembler.%s", tierName)),
                        DrtechReceipes.LIGHTSABER_ASSEMBLER_RECIPES,
                        gregtech.client.renderer.texture.Textures.ASSEMBLER_OVERLAY,
                        tier, true, GTUtility.hvCappedTankSizeFunction));

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

        // Common ID 段：恢复启用的机器注册（详见 PROGRESS.md）
        ANNIHILATION_GENERATOR = registerMetaTileEntity(getID(), new AnnihilationGenerator(getDrId("annihilation_generator")));

        FUSION_REACTOR = registerMetaTileEntity(getID(), new MetaTileEntityFusionReactor(getDrId("fusion_reactor")));

        DRONE_PAD = registerMetaTileEntity(getID(), new MetaTileEntityDronePad(getDrId("drone_pad")));
        INFINITE_FLUID_DRILLING_RIG = registerMetaTileEntity(getID(), new MetaTileEntityInfiniteFluidDrill(getDrId("fluid_drilling_rig.iv"), 6));
        LARGE_ALLOY_SMELTER = registerMetaTileEntity(getID(), new MetaTileEntityLargeAlloySmelter(getDrId("large_alloy_smelter")));
        LARGE_BEE_HIVE = registerMetaTileEntity(getID(), new MetaTileEntutyLargeBeeHive(getDrId("large_bee_hive")));

        MOB_KILLER = registerMetaTileEntity(getID(), new MetaTileEntityExtremeExterminationChamber(getDrId("mob_killer")));
        PLAYER_BEACON = registerMetaTileEntity(getID(), new MetaTileEntityPlayerBeacon(getDrId("player_beacon")));

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
        INDUSTRIAL_APIARY = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialApiary(getDrId("industrial_apiary"), Textures.INDUSTRIAL_APIARY));

        if (DrtConfig.MachineSwitch.EnableIndustrialMachines) {
            INDUSTRIAL_MIXER = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialMixer(getDrId("industrial_mixer")));
            INDUSTRIAL_ROLLER_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialRollerPress(getDrId("industrial_roller_press")));
            INDUSTRIAL_CABLE_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCablePress(getDrId("industrial_cable_press")));
            INDUSTRIAL_SIEVE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialSieve(getDrId("industrial_sieve")));
            INDUSTRIAL_CENTRIFUGE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCentrifuge(getDrId("industrial_centrifuge")));
            LARGE_EXTRUDER = registerMetaTileEntity(getID(), new MetaTileentityLargeExtruder(getDrId("large_extruder")));
        }

        DRONE_PROGRAMMER = registerMetaTileEntity(DRONE_PROGRAMMER_META_ID,
                new MetaTileEntityDroneProgrammer(getDrId("drone_programmer")));
        DRONE_DOCK = registerMetaTileEntity(DRONE_DOCK_META_ID,
                new MetaTileEntityDroneDock(getDrId("drone_dock"), GTValues.HV));
        DRONE_REDSTONE_EMITTER = registerMetaTileEntity(DRONE_REDSTONE_EMITTER_META_ID,
                new MetaTileEntityDroneRedstoneEmitter(getDrId("drone_redstone_emitter")));
        DRONE_DOCK_EV = registerMetaTileEntity(DRONE_DOCK_EV_META_ID,
                new MetaTileEntityDroneDock(getDrId("drone_dock.ev"), GTValues.EV));
        DRONE_DOCK_IV = registerMetaTileEntity(DRONE_DOCK_IV_META_ID,
                new MetaTileEntityDroneDock(getDrId("drone_dock.iv"), GTValues.IV));
        DRONE_FLEET_CONTROLLER = registerMetaTileEntity(DRONE_FLEET_CONTROLLER_META_ID,
                new MetaTileEntityDroneFleetController(getDrId("drone_fleet_controller")));
        DRONE_ITEM_ENDPOINT = registerMetaTileEntity(DRONE_ITEM_ENDPOINT_META_ID,
                new MetaTileEntityDroneEndpoint(getDrId("drone_item_endpoint"), DroneEndpoint.Kind.ITEM));
        DRONE_FLUID_ENDPOINT = registerMetaTileEntity(DRONE_FLUID_ENDPOINT_META_ID,
                new MetaTileEntityDroneEndpoint(getDrId("drone_fluid_endpoint"), DroneEndpoint.Kind.FLUID));
        DRONE_EU_ENDPOINT = registerMetaTileEntity(DRONE_EU_ENDPOINT_META_ID,
                new MetaTileEntityDroneEndpoint(getDrId("drone_eu_endpoint"), DroneEndpoint.Kind.EU));
    }


    public static @NotNull ResourceLocation getDrId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
