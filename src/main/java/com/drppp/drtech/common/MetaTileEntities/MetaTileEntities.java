package com.drppp.drtech.common.MetaTileEntities;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.generator.MeTaTileEntityWindDrivenGenerator;
import com.drppp.drtech.common.MetaTileEntities.muti.electric.standard.*;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileEntityInputAssembly;
import com.drppp.drtech.common.MetaTileEntities.muti.mutipart.MetaTileEntityWirelessEnergyHatch;
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

import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityLaserHatch;
import keqing.gtqtcore.api.GTQTValue;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static gregtech.common.metatileentities.MetaTileEntities.registerSimpleMetaTileEntity;

public class MetaTileEntities {
    public static MetaTileEntityDronePad DRONE_PAD;
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
    public static MetaTileEntityEnergyTransTower TRANS_TOWER;
    public static MetaTileEntitySolarTower SOLAR_TOWER;
    public static NuclearReactor NUCLEAR_GENERATOR;
    public static MetaTileEntutyLargeBeeHive LARGE_BEE_HIVE;
    public static MeTaTileEntityWindDrivenGenerator WIND_DRIVEN_GENERATOR_HV;
    public static MetaTileEntityWirelessEnergyHatch[] WIRELESS_EMERGY_HATCH_RECEIVER = new MetaTileEntityWirelessEnergyHatch[10];
    public static MetaTileEntityWirelessEnergyHatch[] WIRELESS_EMERGY_HATCH_TRANSMITTER= new MetaTileEntityWirelessEnergyHatch[10];
    public static MetaTileEntityPlayerBeacon PLAYER_BEACON;
    public static final MetaTileEntityInputAssembly[] ITEM_IMPORT_BUS = new MetaTileEntityInputAssembly[GTValues.UHV + 1];
    public static final MetaTileEntityUniversalCollector[] UNIVERSAL_COLLECTORS = new MetaTileEntityUniversalCollector[10];
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
        TRANS_TOWER = registerMetaTileEntity(17066,new MetaTileEntityEnergyTransTower(getmyId("trans_tower")));
        SOLAR_TOWER = registerMetaTileEntity(17067,new MetaTileEntitySolarTower(getmyId("solar_tower")));
        NUCLEAR_GENERATOR = registerMetaTileEntity(17068,new NuclearReactor(getmyId("nuclear_generator")));
        LARGE_BEE_HIVE = registerMetaTileEntity(17069,new MetaTileEntutyLargeBeeHive(getmyId("large_bee_hive")));
        DRONE_PAD = registerMetaTileEntity(17070, new MetaTileEntityDronePad(getmyId("drone_pad")));
        WIND_DRIVEN_GENERATOR_HV = registerMetaTileEntity(17071, new MeTaTileEntityWindDrivenGenerator(getmyId("wind_driven_generator")));
        String tierName;
        for(int i = 1; i <= 10; ++i) {
            tierName = GTValues.VN[i].toLowerCase();
            WIRELESS_EMERGY_HATCH_RECEIVER[i-1] = registerMetaTileEntity(17072 + i - 1, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.receiver." + tierName), i, false));
            WIRELESS_EMERGY_HATCH_TRANSMITTER[i-1] = registerMetaTileEntity(17082 + i - 1, new MetaTileEntityWirelessEnergyHatch(getmyId("wireless_energy_hatch.transmitter." + tierName), i, true));
        }
        PLAYER_BEACON = registerMetaTileEntity(17093,new MetaTileEntityPlayerBeacon(getmyId("player_beacon")));
        for (int i = 0; i < 10; i++) {
            tierName = GTValues.VN[i].toLowerCase();
            UNIVERSAL_COLLECTORS[i] = registerMetaTileEntity(17094+i,new MetaTileEntityUniversalCollector(getmyId("universal_collector."+tierName),i+1, gregtech.client.renderer.texture.Textures.GAS_COLLECTOR_OVERLAY));
        }
    }


    public static @NotNull ResourceLocation getmyId(@NotNull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
