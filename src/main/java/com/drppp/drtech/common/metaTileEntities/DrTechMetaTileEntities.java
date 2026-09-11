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
import com.meowmel.cropQT.machine.MetaTileEntityCropMachine;
import com.meowmel.cropQT.machine.MetaTileEntityCropManager;
import com.meowmel.cropQT.machine.MetaTileEntityCropSynthesizer;
import com.meowmel.cropQT.machine.MetaTileEntityIndustrialFarm;
import com.meowmel.cropQT.machine.MetaTileEntitySeedGenerator;
import com.drppp.drtech.common.metaTileEntities.single.MetaTileEntityLaserPipeBending;
import com.drppp.drtech.common.metaTileEntities.single.MetaTileEntityLightsaberAssembler;
import com.drppp.drtech.common.metaTileEntities.single.MetaTileEntityUniversalCollector;
import com.drppp.drtech.loaders.recipes.DrtechRecipes;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

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

    public static AnnihilationGenerator ANNIHILATION_GENERATOR;
    public static MetaTileEntityAdvancedFusionReactor ADVANCED_FUSION_REACTOR;

    public static MetaTileEntityDronePad DRONE_PAD;
    public static MetaTileEntityEnergyTransTower TRANS_TOWER;
    public static MetaTileEntityLargeAlloySmelter LARGE_ALLOY_SMELTER;

    public static MetaTileEntityExtremeExterminationChamber MOB_KILLER;
    public static MetaTileEntitySolarTower SOLAR_TOWER;
    public static MetatileEntityTwentyFiveFluidTank TFFT;
    public static MetaTileEntityUniversalCollector[] UNIVERSAL_COLLECTORS = new MetaTileEntityUniversalCollector[10];
    public static MetaTileEntityYotHatch YOT_HARCH;
    public static MetaTileEntityYotTank YOUT_TANK;
    public static MetaTileEntutyLargeBeeHive LARGE_BEE_HIVE;
    public static SimpleMachineMetaTileEntity[] DISASSEMBLY = new SimpleMachineMetaTileEntity[10];
    public static SimpleMachineMetaTileEntity[] LIGHTSABER_ASSEMBLER = new SimpleMachineMetaTileEntity[10];
    public static MetaTileEntityLargeExtruder LARGE_EXTRUDER;

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
    public static MetaTileEntityArmorWorkbench ARMOR_WORKBENCH;
    public static EnergySink ENERGY_SINK;
    public static MetaTileEntityIndustrialApiary INDUSTRIAL_APIARY;

    /** 五台作物机器，按声明的档位排列。 */
    public static MetaTileEntitySeedGenerator[] CROP_SEED_GENERATOR;
    public static MetaTileEntityCropManager[] CROP_MANAGER;
    public static MetaTileEntityCropBreeder[] CROP_BREEDER;
    public static MetaTileEntityCropGeneExtractor[] CROP_GENE_EXTRACTOR;
    public static MetaTileEntityCropSynthesizer[] CROP_SYNTHESIZER;
    /** 工业农场：单控制器，等级由输入电压决定。 */
    public static MetaTileEntityIndustrialFarm INDUSTRIAL_FARM;
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

        registerSimpleMetaTileEntity(DISASSEMBLY, 110, "disassembly", DrtechRecipes.DISASSEMBLER_RECIPES, Textures.DISASSEMBLY, true, DrtechUtils::getRL, GTUtility.hvCappedTankSizeFunction);
        registerMetaTileEntities(LIGHTSABER_ASSEMBLER, 120, "lightsaber_assembler", (tier, tierName) ->
                new MetaTileEntityLightsaberAssembler(
                        DrtechUtils.getRL(String.format("lightsaber_assembler.%s", tierName)),
                        DrtechRecipes.LIGHTSABER_ASSEMBLER_RECIPES,
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

        ANNIHILATION_GENERATOR = registerMetaTileEntity(getID(), new AnnihilationGenerator(getDrId("annihilation_generator")));
        ADVANCED_FUSION_REACTOR = registerMetaTileEntity(getID(), new MetaTileEntityAdvancedFusionReactor(getDrId("advanced_fusion_reactor")));

        DRONE_PAD = registerMetaTileEntity(getID(), new MetaTileEntityDronePad(getDrId("drone_pad")));
        LARGE_ALLOY_SMELTER = registerMetaTileEntity(getID(), new MetaTileEntityLargeAlloySmelter(getDrId("large_alloy_smelter")));
        LARGE_BEE_HIVE = registerMetaTileEntity(getID(), new MetaTileEntutyLargeBeeHive(getDrId("large_bee_hive")));

        MOB_KILLER = registerMetaTileEntity(getID(), new MetaTileEntityExtremeExterminationChamber(getDrId("mob_killer")));

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

        /*
        if (DrtConfig.machine.EnableIndustrialMachines) {
            INDUSTRIAL_MIXER = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialMixer(getDrId("industrial_mixer")));
            INDUSTRIAL_ROLLER_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialRollerPress(getDrId("industrial_roller_press")));
            INDUSTRIAL_CABLE_PRESS = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCablePress(getDrId("industrial_cable_press")));
            INDUSTRIAL_SIEVE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialSieve(getDrId("industrial_sieve")));
            INDUSTRIAL_CENTRIFUGE = registerMetaTileEntity(getID(), new MetaTileEntityIndustrialCentrifuge(getDrId("industrial_centrifuge")));
            LARGE_EXTRUDER = registerMetaTileEntity(getID(), new MetaTileEntityLargeExtruder(getDrId("large_extruder")));
        }

         */

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

        // 模块化装甲（原 mechtech）
        ARMOR_WORKBENCH = registerMetaTileEntity(getID(), new MetaTileEntityArmorWorkbench(getDrId("armor_workbench")));
        ENERGY_SINK = registerMetaTileEntity(getID(), new EnergySink(getDrId("energy_sink")));

        registerCropMachines();
    }

    /**
     * 作物系统的单方块机器。
     *
     * <p>ID 段：200 起，每台占 10 个号（只用到前几个电压档）。
     * 名称走 {@code drtech.machine.<path>.name}，路径形如 {@code seed_generator.lv}。
     */
    private static void registerCropMachines() {
        CROP_SEED_GENERATOR = registerTieredCropMachine(200, "seed_generator",
                MetaTileEntitySeedGenerator::new, CROP_TIERS_LV_TO_IV, MetaTileEntitySeedGenerator[]::new);
        CROP_MANAGER = registerTieredCropMachine(210, "crop_manager",
                MetaTileEntityCropManager::new, CROP_TIERS_LV_TO_IV, MetaTileEntityCropManager[]::new);
        CROP_BREEDER = registerTieredCropMachine(220, "crop_breeder",
                MetaTileEntityCropBreeder::new, CROP_TIERS_LV_TO_IV, MetaTileEntityCropBreeder[]::new);
        CROP_GENE_EXTRACTOR = registerTieredCropMachine(230, "gene_extractor",
                MetaTileEntityCropGeneExtractor::new, CROP_TIERS_EV_TO_IV, MetaTileEntityCropGeneExtractor[]::new);
        CROP_SYNTHESIZER = registerTieredCropMachine(240, "crop_synthesizer",
                MetaTileEntityCropSynthesizer::new, CROP_TIERS_EV_TO_IV, MetaTileEntityCropSynthesizer[]::new);
        INDUSTRIAL_FARM = registerMetaTileEntity(250,
                new MetaTileEntityIndustrialFarm(getDrId("industrial_farm")));
    }

    /** 常用机器主用的电压档：LV / MV / HV / EV / IV。 */
    private static final int[] CROP_TIERS_LV_TO_IV = { GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV };

    /** 提取器 / 合成器只做 EV 与 IV 两档——它们是后期机器。 */
    private static final int[] CROP_TIERS_EV_TO_IV = { GTValues.EV, GTValues.IV };

    /**
     * 按电压档批量注册一台作物机器。
     *
     * <p>数组由调用方通过 {@code arrayFactory} 提供——泛型擦除下没法在方法里
     * {@code new T[n]}，硬转会在运行时抛 ClassCastException。
     *
     * @param startId      首个 MTE id，之后的档位依次 +1
     * @param name         机器名（会成为 {@code <name>.<电压名>} 的路径前缀）
     * @param arrayFactory 通常直接传 {@code MetaTileEntityXxx[]::new}
     */
    private static <T extends MetaTileEntityCropMachine> T[] registerTieredCropMachine(
            int startId, String name, CropMachineFactory<T> factory, int[] tiers,
            IntFunction<T[]> arrayFactory) {
        T[] machines = arrayFactory.apply(tiers.length);
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            machines[i] = registerMetaTileEntity(startId + i,
                    factory.create(getDrId(name + "." + GTValues.VN[tier].toLowerCase()), tier));
        }
        return machines;
    }

    /** 机器工厂：{@code (ResourceLocation, tier) -> MetaTileEntity}。 */
    @FunctionalInterface
    private interface CropMachineFactory<T extends MetaTileEntityCropMachine> {
        T create(ResourceLocation id, int tier);
    }


    public static @NotNull ResourceLocation getDrId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
