package com.drppp.drtech.common.metaTileEntities;

import com.drppp.drtech.client.Textures;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Tags;
import com.drppp.drtech.api.utils.DrtechUtils;
import com.drppp.drtech.common.metaTileEntities.muti.electric.generator.MetaTileEntityAdvancedFusionReactor;
import com.drppp.drtech.drone.machine.MetaTileEntityDroneDock;
import com.drppp.drtech.drone.machine.MetaTileEntityDroneFleetController;
import com.drppp.drtech.drone.machine.MetaTileEntityDroneEndpoint;
import com.drppp.drtech.drone.network.DroneEndpoint;
import com.drppp.drtech.drone.machine.MetaTileEntityDroneProgrammer;
import com.drppp.drtech.drone.machine.MetaTileEntityDroneRedstoneEmitter;
import com.drppp.drtech.common.metaTileEntities.muti.electric.generator.AnnihilationGenerator;
import com.drppp.drtech.common.metaTileEntities.muti.electric.generator.MetaTileEntityLargeLightningRod;
import com.drppp.drtech.common.metaTileEntities.muti.electric.standard.*;
import com.drppp.drtech.common.metaTileEntities.muti.electric.store.MetaTileEntityEnergyTransTower;
import com.drppp.drtech.common.metaTileEntities.muti.electric.store.MetaTileEntityYotTank;
import com.drppp.drtech.common.metaTileEntities.muti.electric.store.MetatileEntityTwentyFiveFluidTank;
import com.drppp.drtech.common.metaTileEntities.muti.mutipart.MetaTileEntityYotHatch;
import com.drppp.drtech.common.metaTileEntities.single.EnergySink;
import com.drppp.drtech.common.metaTileEntities.single.MetaTileEntityArmorWorkbench;
import com.drppp.drtech.common.metaTileEntities.single.MetaTileEntityIndustrialApiary;
import com.meowmel.cropQT.machine.MetaTileEntityCropBreeder;
import com.meowmel.cropQT.machine.MetaTileEntityCropGeneExtractor;
import com.meowmel.cropQT.api.capability.FarmType;
import com.meowmel.cropQT.machine.MetaTileEntityCropHarvester;
import com.meowmel.cropQT.machine.MetaTileEntityCropSupervisor;
import com.meowmel.cropQT.machine.MetaTileEntityCropSynthesizer;
import com.meowmel.cropQT.machine.MetaTileEntityIndustrialFarm;
import com.meowmel.cropQT.machine.part.MetaTileEntityFarmPart;
import com.meowmel.cropQT.machine.MetaTileEntitySeedGenerator;
import com.drppp.drtech.common.metaTileEntities.single.MetaTileEntityLaserPipeBending;
import com.drppp.drtech.common.metaTileEntities.single.MetaTileEntityLightsaberAssembler;
import com.drppp.drtech.common.metaTileEntities.single.MetaTileEntityUniversalCollector;
import com.drppp.drtech.loaders.recipes.DrtechRecipes;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntFunction;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntities;
import static gregtech.common.metatileentities.MetaTileEntities.registerSimpleMetaTileEntity;

public class DrTechMetaTileEntities {
    // 发电机
    public static AnnihilationGenerator ANNIHILATION_GENERATOR;
    public static MetaTileEntityAdvancedFusionReactor ADVANCED_FUSION_REACTOR;

    // 多方块设备
    public static MetaTileEntityDronePad DRONE_PAD;
    public static MetaTileEntutyLargeBeeHive LARGE_BEE_HIVE;
    public static MetaTileEntityExtremeExterminationChamber MOB_KILLER;
    public static MetaTileEntitySolarTower SOLAR_TOWER;
    public static MetatileEntityTwentyFiveFluidTank TFFT;
    public static MetaTileEntityEnergyTransTower TRANS_TOWER;
    public static MetaTileEntityYotHatch YOT_HARCH;
    public static MetaTileEntityYotTank YOUT_TANK;
    public static MetaTileentityConcreteBackfiller CONCRETE_BACK_FILLER1;
    public static MetaTileentityConcreteBackfiller CONCRETE_BACK_FILLER2;
    public static MetaTileEntityLargeLightningRod LARGE_LIGHTING_ROD;
    public static MetaTileEntityCombProcess COMB_PROVESS;
    public static MetaTileentityCropsSimulateMachine CROPS_SIMULATE;
    public static MetaTileEntityIndustrialApiary INDUSTRIAL_APIARY;

    // 单方块机器
    public static MetaTileEntityUniversalCollector[] UNIVERSAL_COLLECTORS = new MetaTileEntityUniversalCollector[10];
    public static SimpleMachineMetaTileEntity[] DISASSEMBLY = new SimpleMachineMetaTileEntity[10];
    public static SimpleMachineMetaTileEntity[] LIGHTSABER_ASSEMBLER = new SimpleMachineMetaTileEntity[10];

    // 激光折弯
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_256 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_1024 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_4096 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_16384 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_65536 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_262144 = new MetaTileEntityLaserPipeBending[10]; // IV+
    public static MetaTileEntityLaserPipeBending[] LASER_BENDING_1048576 = new MetaTileEntityLaserPipeBending[10]; // IV+

    // 无人机模块
    public static MetaTileEntityDroneProgrammer DRONE_PROGRAMMER;
    public static MetaTileEntityDroneDock DRONE_DOCK;
    public static MetaTileEntityDroneRedstoneEmitter DRONE_REDSTONE_EMITTER;
    public static MetaTileEntityDroneDock DRONE_DOCK_EV;
    public static MetaTileEntityDroneDock DRONE_DOCK_IV;
    public static MetaTileEntityDroneFleetController DRONE_FLEET_CONTROLLER;
    public static MetaTileEntityDroneEndpoint DRONE_ITEM_ENDPOINT;
    public static MetaTileEntityDroneEndpoint DRONE_FLUID_ENDPOINT;
    public static MetaTileEntityDroneEndpoint DRONE_EU_ENDPOINT;

    // 模块化装甲（原 mechtech）
    public static MetaTileEntityArmorWorkbench ARMOR_WORKBENCH;
    public static EnergySink ENERGY_SINK;

    // 作物系统
    public static MetaTileEntitySeedGenerator[] CROP_SEED_GENERATOR = new MetaTileEntitySeedGenerator[5];
    public static MetaTileEntityCropSupervisor[] CROP_SUPERVISOR = new MetaTileEntityCropSupervisor[5];
    public static MetaTileEntityCropHarvester[] CROP_HARVESTER = new MetaTileEntityCropHarvester[5];
    public static MetaTileEntityCropBreeder[] CROP_BREEDER = new MetaTileEntityCropBreeder[5];
    public static MetaTileEntityCropGeneExtractor[] CROP_GENE_EXTRACTOR = new MetaTileEntityCropGeneExtractor[5];
    public static MetaTileEntityCropSynthesizer[] CROP_SYNTHESIZER = new MetaTileEntityCropSynthesizer[5];

    /** 工业农场：单控制器，等级由升级仓与苗床的档次决定。 */
    public static MetaTileEntityIndustrialFarm INDUSTRIAL_FARM;

    /** 工业农场的升级仓，按类型分组、每组按档位排列。 */
    public static MetaTileEntityFarmPart[] ENVIRONMENTAL_ENHANCEMENT_FARM_PARTS = new MetaTileEntityFarmPart[10];
    public static MetaTileEntityFarmPart[] GROWTH_ACCELERATION_FARM_PARTS = new MetaTileEntityFarmPart[10];
    public static MetaTileEntityFarmPart[] FERTILIZER_FARM_PARTS = new MetaTileEntityFarmPart[10];
    public static MetaTileEntityFarmPart[] ADVANCED_HARVESTING_FARM_PARTS = new MetaTileEntityFarmPart[10];
    public static MetaTileEntityFarmPart[] OVERCLOCKED_GROWTH_ACCELERATION_FARM_PARTS = new MetaTileEntityFarmPart[10];

    public static void initialization() {
        // 发电机
        ANNIHILATION_GENERATOR = registerMetaTileEntity(1, new AnnihilationGenerator(getDrId("annihilation_generator")));
        ADVANCED_FUSION_REACTOR = registerMetaTileEntity(2, new MetaTileEntityAdvancedFusionReactor(getDrId("advanced_fusion_reactor")));

        // 多方块设备
        DRONE_PAD = registerMetaTileEntity(10, new MetaTileEntityDronePad(getDrId("drone_pad")));
        LARGE_BEE_HIVE = registerMetaTileEntity(12, new MetaTileEntutyLargeBeeHive(getDrId("large_bee_hive")));
        MOB_KILLER = registerMetaTileEntity(13, new MetaTileEntityExtremeExterminationChamber(getDrId("mob_killer")));
        SOLAR_TOWER = registerMetaTileEntity(14, new MetaTileEntitySolarTower(getDrId("solar_tower")));
        TFFT = registerMetaTileEntity(15, new MetatileEntityTwentyFiveFluidTank(getDrId("tfft_tank")));
        TRANS_TOWER = registerMetaTileEntity(16, new MetaTileEntityEnergyTransTower(getDrId("trans_tower")));
        YOT_HARCH = registerMetaTileEntity(17, new MetaTileEntityYotHatch(getDrId("yot_hatch")));
        YOUT_TANK = registerMetaTileEntity(18, new MetaTileEntityYotTank(getDrId("yot_tank")));
        CONCRETE_BACK_FILLER1 = registerMetaTileEntity(19, new MetaTileentityConcreteBackfiller(getDrId("concrete_backfiller1"), 1));
        CONCRETE_BACK_FILLER2 = registerMetaTileEntity(20, new MetaTileentityConcreteBackfiller(getDrId("concrete_backfiller2"), 2));
        LARGE_LIGHTING_ROD = registerMetaTileEntity(21, new MetaTileEntityLargeLightningRod(getDrId("large_lighting_rod")));
        COMB_PROVESS = registerMetaTileEntity(22, new MetaTileEntityCombProcess(getDrId("comb_process")));
        CROPS_SIMULATE = registerMetaTileEntity(23, new MetaTileentityCropsSimulateMachine(getDrId("crops_simulate_machine")));
        INDUSTRIAL_APIARY = registerMetaTileEntity(24, new MetaTileEntityIndustrialApiary(getDrId("industrial_apiary"), Textures.INDUSTRIAL_APIARY));


        // 单方块机器
        for (int i = 0; i < UNIVERSAL_COLLECTORS.length; i++) {
            int tier = i + 1;
            String tierName = GTValues.VN[tier].toLowerCase();
            UNIVERSAL_COLLECTORS[i] = registerMetaTileEntity(100 + i, new MetaTileEntityUniversalCollector(getDrId("universal_collector." + tierName), tier));
        }

        registerSimpleMetaTileEntity(DISASSEMBLY, 110, "disassembly", DrtechRecipes.DISASSEMBLER_RECIPES, Textures.DISASSEMBLY, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);

        registerMetaTileEntities(LIGHTSABER_ASSEMBLER, 120, "lightsaber_assembler", (tier, tierName) ->
                new MetaTileEntityLightsaberAssembler(
                        DrtechUtils.getRL(String.format("lightsaber_assembler.%s", tierName)),
                        DrtechRecipes.LIGHTSABER_ASSEMBLER_RECIPES,
                        gregtech.client.renderer.texture.Textures.ASSEMBLER_OVERLAY,
                        tier, true, GTUtility.hvCappedTankSizeFunction));

        // 激光折弯
        for (int i = 0; i < LASER_BENDING_256.length; i++) {
            LASER_BENDING_256[i] = registerMetaTileEntity(130 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_256." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 256));
            LASER_BENDING_1024[i] = registerMetaTileEntity(140 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_1024." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 1024));
            LASER_BENDING_4096[i] = registerMetaTileEntity(150 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_4096." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 4096));
            LASER_BENDING_16384[i] = registerMetaTileEntity(160 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_16384." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 16384));
            LASER_BENDING_65536[i] = registerMetaTileEntity(170 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_65536." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 65536));
            LASER_BENDING_262144[i] = registerMetaTileEntity(180 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_262144." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 262144));
            LASER_BENDING_1048576[i] = registerMetaTileEntity(190 + i, new MetaTileEntityLaserPipeBending(getDrId("laser_bending_1048576." + GTValues.VN[GTValues.IV + i]), GTValues.IV + i, 1048576));
        }

        // 无人机模块
        DRONE_PROGRAMMER = registerMetaTileEntity(200,
                new MetaTileEntityDroneProgrammer(getDrId("drone_programmer")));
        DRONE_DOCK = registerMetaTileEntity(201,
                new MetaTileEntityDroneDock(getDrId("drone_dock"), GTValues.HV));
        DRONE_REDSTONE_EMITTER = registerMetaTileEntity(202,
                new MetaTileEntityDroneRedstoneEmitter(getDrId("drone_redstone_emitter")));
        DRONE_DOCK_EV = registerMetaTileEntity(203,
                new MetaTileEntityDroneDock(getDrId("drone_dock.ev"), GTValues.EV));
        DRONE_DOCK_IV = registerMetaTileEntity(204,
                new MetaTileEntityDroneDock(getDrId("drone_dock.iv"), GTValues.IV));
        DRONE_FLEET_CONTROLLER = registerMetaTileEntity(205,
                new MetaTileEntityDroneFleetController(getDrId("drone_fleet_controller")));
        DRONE_ITEM_ENDPOINT = registerMetaTileEntity(206,
                new MetaTileEntityDroneEndpoint(getDrId("drone_item_endpoint"), DroneEndpoint.Kind.ITEM));
        DRONE_FLUID_ENDPOINT = registerMetaTileEntity(207,
                new MetaTileEntityDroneEndpoint(getDrId("drone_fluid_endpoint"), DroneEndpoint.Kind.FLUID));
        DRONE_EU_ENDPOINT = registerMetaTileEntity(208,
                new MetaTileEntityDroneEndpoint(getDrId("drone_eu_endpoint"), DroneEndpoint.Kind.EU));

        // 模块化装甲（原 mechtech）
        ARMOR_WORKBENCH = registerMetaTileEntity(250, new MetaTileEntityArmorWorkbench(getDrId("armor_workbench")));
        ENERGY_SINK = registerMetaTileEntity(251, new EnergySink(getDrId("energy_sink")));

        // 作物系统
        for (int i = 0; i < CROP_SEED_GENERATOR.length; i++) {
            int tier = i + 1;
            String tierName = GTValues.VN[tier].toLowerCase();
            CROP_SEED_GENERATOR[i] = registerMetaTileEntity(300 + i, new MetaTileEntitySeedGenerator(getDrId("seed_generator." + tierName), tier));
        }
        for (int i = 0; i < CROP_SUPERVISOR.length; i++) {
            int tier = i + 1;
            String tierName = GTValues.VN[tier].toLowerCase();
            CROP_SUPERVISOR[i] = registerMetaTileEntity(310 + i, new MetaTileEntityCropSupervisor(getDrId("crop_supervisor." + tierName), tier));
        }
        for (int i = 0; i < CROP_HARVESTER.length; i++) {
            int tier = i + 1;
            String tierName = GTValues.VN[tier].toLowerCase();
            CROP_HARVESTER[i] = registerMetaTileEntity(320 + i, new MetaTileEntityCropHarvester(getDrId("crop_harvester." + tierName), tier));
        }
        for (int i = 0; i < CROP_BREEDER.length; i++) {
            int tier = i + 1;
            String tierName = GTValues.VN[tier].toLowerCase();
            CROP_BREEDER[i] = registerMetaTileEntity(330 + i, new MetaTileEntityCropBreeder(getDrId("crop_breeder." + tierName), tier));
        }
        for (int i = 0; i < CROP_GENE_EXTRACTOR.length; i++) {
            int tier = i + 1;
            String tierName = GTValues.VN[tier].toLowerCase();
            CROP_GENE_EXTRACTOR[i] = registerMetaTileEntity(340 + i, new MetaTileEntityCropGeneExtractor(getDrId("gene_extractor." + tierName), tier));
        }
        for (int i = 0; i < CROP_SYNTHESIZER.length; i++) {
            int tier = i + 1;
            String tierName = GTValues.VN[tier].toLowerCase();
            CROP_SYNTHESIZER[i] = registerMetaTileEntity(350 + i, new MetaTileEntityCropSynthesizer(getDrId("crop_synthesizer." + tierName), tier));
        }


        INDUSTRIAL_FARM = registerMetaTileEntity(360,
                new MetaTileEntityIndustrialFarm(getDrId("industrial_farm")));


        for (int i = 0; i < ENVIRONMENTAL_ENHANCEMENT_FARM_PARTS.length; i++) {
            int tier = i + 2;
            String tierName = GTValues.VN[tier].toLowerCase();
            ENVIRONMENTAL_ENHANCEMENT_FARM_PARTS[i] = registerMetaTileEntity(370 + i, new MetaTileEntityFarmPart(getDrId("farm_part.environmental_enhancement."+ tierName), tier, FarmType.ENVIRONMENTAL_ENHANCEMENT));
        }
        for (int i = 0; i < GROWTH_ACCELERATION_FARM_PARTS.length; i++) {
            int tier = i + 2;
            String tierName = GTValues.VN[tier].toLowerCase();
            GROWTH_ACCELERATION_FARM_PARTS[i] = registerMetaTileEntity(380 + i, new MetaTileEntityFarmPart(getDrId("farm_part.growth_acceleration."+ tierName), tier, FarmType.GROWTH_ACCELERATION));
        }
        for (int i = 0; i < FERTILIZER_FARM_PARTS.length; i++) {
            int tier = i + 2;
            String tierName = GTValues.VN[tier].toLowerCase();
            FERTILIZER_FARM_PARTS[i] = registerMetaTileEntity(390 + i, new MetaTileEntityFarmPart(getDrId("farm_part.fertilizer."+ tierName), tier, FarmType.FERTILIZER));
        }
        for (int i = 0; i < ADVANCED_HARVESTING_FARM_PARTS.length; i++) {
            int tier = i + 2;
            String tierName = GTValues.VN[tier].toLowerCase();
            ADVANCED_HARVESTING_FARM_PARTS[i] = registerMetaTileEntity(400 + i, new MetaTileEntityFarmPart(getDrId("farm_part.advanced_harvesting."+ tierName), tier, FarmType.ADVANCED_HARVESTING));
        }
        for (int i = 0; i < OVERCLOCKED_GROWTH_ACCELERATION_FARM_PARTS.length; i++) {
            int tier = i + 2;
            String tierName = GTValues.VN[tier].toLowerCase();
            OVERCLOCKED_GROWTH_ACCELERATION_FARM_PARTS[i] = registerMetaTileEntity(410 + i, new MetaTileEntityFarmPart(getDrId("farm_part.overclocked_growth_acceleration."+ tierName), tier, FarmType.OVERCLOCKED_GROWTH_ACCELERATION));
        }
    }

    public static @NotNull ResourceLocation getDrId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
