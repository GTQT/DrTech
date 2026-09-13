package com.drppp.drtech.common.blocks;


import com.drppp.drtech.Tags;
import com.drppp.drtech.api.utils.Datas;
import com.drppp.drtech.common.blocks.metaBlocks.*;
import com.drppp.drtech.common.tile.*;
import com.meowmel.cropQT.block.BlockCropStick;
import com.meowmel.cropQT.block.BlockSeedBed;
import com.meowmel.cropQT.tile.TileCropStick;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlocksInit {
    public static final BlockGravitationalAnomaly BLOCK_GRAVITATIONAL_ANOMALY = new BlockGravitationalAnomaly();
    public static final BlockConnector BLOCK_CONNECTOR1 = new BlockConnector(1);
    public static final BlockConnector BLOCK_CONNECTOR2 = new BlockConnector(2);
    public static final BlockConnector BLOCK_CONNECTOR3 = new BlockConnector(3);
    public static final BlockGoldenSea BLOCK_GOLDEN_SEA = new BlockGoldenSea();
    public static final BlockPeacefulTable BLOCK_PEACEFUL_TABLE = new BlockPeacefulTable();
    public static final BlockWasteDirt BLOCK_WASTE_DIRT = new BlockWasteDirt();
    public static final BlockStoragePail BLOCK_STORAGE_PAIL = new BlockStoragePail("compress", 1);
    public static final MetaGlasses TRANSPARENT_CASING = new MetaGlasses("glasses_casing");
    public static final MetaCasing COMMON_CASING = new MetaCasing();
    public static final MetaCasing1 COMMON_CASING1 = new MetaCasing1();
    public static final BlockFusionReactorCasing FUSION_REACTOR_CASING = new BlockFusionReactorCasing();
    public static final BlockFusionReactorCasing2 FUSION_REACTOR_CASING2 = new BlockFusionReactorCasing2();
    public static final BlockFusionReactorCasing3 FUSION_REACTOR_CASING3 = new BlockFusionReactorCasing3();
    public static final BlockYotTankPart YOT_TANK = new BlockYotTankPart();
    public static final BlockFTTFPart TFFT_TANK = new BlockFTTFPart();
    public static final BlockAdvancedCauldron BLOCK_ADVANCED_CAULDRON = new BlockAdvancedCauldron();
    public static final BlockTimeTable BLOCK_TIME_TABLE = new BlockTimeTable();

    // 工业农场的两个组件方块（作物系统）
    public static final BlockSeedBed SEED_BED = new BlockSeedBed();
    public static BlockCropStick CROP_STICK = new BlockCropStick();

    public static void init(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(BLOCK_GRAVITATIONAL_ANOMALY);
        GameRegistry.registerTileEntity(TileEntityGravitationalAnomaly.class, new ResourceLocation(Tags.MODID, "gravitational_anomaly"));
        event.getRegistry().register(BLOCK_CONNECTOR1);
        event.getRegistry().register(BLOCK_CONNECTOR2);
        event.getRegistry().register(BLOCK_CONNECTOR3);
        GameRegistry.registerTileEntity(TileEntityConnector.class, new ResourceLocation(Tags.MODID, "connetor"));
        event.getRegistry().register(BLOCK_GOLDEN_SEA);
        event.getRegistry().register(BLOCK_PEACEFUL_TABLE);
        event.getRegistry().register(BLOCK_STORAGE_PAIL);
        event.getRegistry().register(BLOCK_WASTE_DIRT);
        GameRegistry.registerTileEntity(TileEntityGoldenSea.class, new ResourceLocation(Tags.MODID, "gold_coin"));
        GameRegistry.registerTileEntity(TileEntityPeacefulTable.class, new ResourceLocation(Tags.MODID, "peaceful_table"));
        GameRegistry.registerTileEntity(TileEntityStoragePail.class, new ResourceLocation(Tags.MODID, "storage_pail"));
        event.getRegistry().register(TRANSPARENT_CASING);
        event.getRegistry().register(COMMON_CASING);
        event.getRegistry().register(COMMON_CASING1);
        event.getRegistry().register(FUSION_REACTOR_CASING);
        event.getRegistry().register(FUSION_REACTOR_CASING2);
        event.getRegistry().register(FUSION_REACTOR_CASING3);
        event.getRegistry().register(YOT_TANK);
        event.getRegistry().register(TFFT_TANK);
        event.getRegistry().register(BLOCK_ADVANCED_CAULDRON);
        GameRegistry.registerTileEntity(TileEntityAdvancedCauldron.class, new ResourceLocation(Tags.MODID, "advanced_cauldron"));
        event.getRegistry().register(BLOCK_TIME_TABLE);
        GameRegistry.registerTileEntity(TileEntityTimeTable.class, new ResourceLocation(Tags.MODID, "time_table"));
        event.getRegistry().register(CROP_STICK);
        event.getRegistry().register(SEED_BED);
        GameRegistry.registerTileEntity(TileCropStick.class, Tags.MODID + ":crop_stick");
        Datas.init();
    }
}
