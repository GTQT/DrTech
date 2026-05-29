package com.drppp.drtech.common.Blocks;


import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.*;
import com.drppp.drtech.api.Utils.Datas;
import com.drppp.drtech.common.Blocks.Crops.BlockCropStick;
import com.drppp.drtech.common.Blocks.MetaBlocks.*;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.lwjgl.util.vector.Vector3f;

import java.util.Random;

public class BlocksInit {
    public static final BlockGravitationalAnomaly BLOCK_GRAVITATIONAL_ANOMALY = new BlockGravitationalAnomaly();
    public static final BlockConnector BLOCK_CONNECTOR1 = new BlockConnector(1);
    public static final BlockConnector BLOCK_CONNECTOR2 = new BlockConnector(2);
    public static final BlockConnector BLOCK_CONNECTOR3 = new BlockConnector(3);
    public static final BlockGoldenSea BLOCK_GOLDEN_SEA = new BlockGoldenSea();
    public static final BlockBubbleColumn BLOCK_BUBBLE_COLUMN = new BlockBubbleColumn();
    public static final BlockDriedGhast BLOCK_DRIED_GHAST = new BlockDriedGhast();
    public static final BlockSimpleDrTech BLOCK_SMOOTH_BASALT = new BlockSimpleDrTech("smooth_basalt", net.minecraft.block.material.Material.ROCK, net.minecraft.block.SoundType.STONE, 1.25F, 4.2F);
    public static final BlockSimpleDrTech BLOCK_CALCITE = new BlockSimpleDrTech("calcite", net.minecraft.block.material.Material.ROCK, net.minecraft.block.SoundType.STONE, 0.75F, 0.75F);
    public static final BlockSimpleDrTech BLOCK_AMETHYST_BLOCK = new BlockSimpleDrTech("amethyst_block", net.minecraft.block.material.Material.ROCK, net.minecraft.block.SoundType.GLASS, 1.5F, 1.5F);
    public static final BlockBuddingAmethyst BLOCK_BUDDING_AMETHYST = new BlockBuddingAmethyst();
    public static final BlockAmethystCluster BLOCK_SMALL_AMETHYST_BUD = new BlockAmethystCluster("small_amethyst_bud", 6, 3, 1, false);
    public static final BlockAmethystCluster BLOCK_MEDIUM_AMETHYST_BUD = new BlockAmethystCluster("medium_amethyst_bud", 5, 4, 2, false);
    public static final BlockAmethystCluster BLOCK_LARGE_AMETHYST_BUD = new BlockAmethystCluster("large_amethyst_bud", 4, 5, 4, false);
    public static final BlockAmethystCluster BLOCK_AMETHYST_CLUSTER = new BlockAmethystCluster("amethyst_cluster", 3, 7, 5, true);
    public static final BlockLantern BLOCK_LANTERN = new BlockLantern("lantern", 15);
    public static final BlockLantern BLOCK_SOUL_LANTERN = new BlockLantern("soul_lantern", 10);
    public static final BlockChain BLOCK_CHAIN = new BlockChain();
    public static final BlockComposter BLOCK_COMPOSTER = new BlockComposter();
    public static final BlockPeacefulTable BLOCK_PEACEFUL_TABLE = new BlockPeacefulTable();
    public static final BlockWasteDirt BLOCK_WASTE_DIRT = new BlockWasteDirt();
    public static final BlockSapBag BLOCK_SAP_BAG = new BlockSapBag();
    public static final BlockStoragePail BLOCK_STORAGE_PAIL = new BlockStoragePail("compress", 1);
    public static final MetaGlasses1 TRANSPARENT_CASING1 = new MetaGlasses1("glasses_casing1");
    public static final MetaCasing COMMON_CASING = new MetaCasing();
    public static final MetaCasing1 COMMON_CASING1 = new MetaCasing1();
    public static final BlockFusionReactorCasing FUSION_REACTOR_CASING = new BlockFusionReactorCasing();
    public static final BlockYotTankPart YOT_TANK = new BlockYotTankPart();
    public static final BlockFTTFPart TFFT_TANK = new BlockFTTFPart();
    public static final BlockAdvancedCauldron BLOCK_ADVANCED_CAULDRON = new BlockAdvancedCauldron();
    public static final BlockTimeTable BLOCK_TIME_TABLE = new BlockTimeTable();
    public static BlockCropStick CROP_STICK = new BlockCropStick();

    public static void init(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(BLOCK_GRAVITATIONAL_ANOMALY);
        GameRegistry.registerTileEntity(TileEntityGravitationalAnomaly.class, new ResourceLocation(Tags.MODID, "gravitational_anomaly"));
        event.getRegistry().register(BLOCK_CONNECTOR1);
        event.getRegistry().register(BLOCK_CONNECTOR2);
        event.getRegistry().register(BLOCK_CONNECTOR3);
        GameRegistry.registerTileEntity(TileEntityConnector.class, new ResourceLocation(Tags.MODID, "connetor"));
        event.getRegistry().register(BLOCK_GOLDEN_SEA);
        event.getRegistry().register(BLOCK_BUBBLE_COLUMN);
        event.getRegistry().register(BLOCK_DRIED_GHAST);
        event.getRegistry().register(BLOCK_SMOOTH_BASALT);
        event.getRegistry().register(BLOCK_CALCITE);
        event.getRegistry().register(BLOCK_AMETHYST_BLOCK);
        event.getRegistry().register(BLOCK_BUDDING_AMETHYST);
        event.getRegistry().register(BLOCK_SMALL_AMETHYST_BUD);
        event.getRegistry().register(BLOCK_MEDIUM_AMETHYST_BUD);
        event.getRegistry().register(BLOCK_LARGE_AMETHYST_BUD);
        event.getRegistry().register(BLOCK_AMETHYST_CLUSTER);
        event.getRegistry().register(BLOCK_LANTERN);
        event.getRegistry().register(BLOCK_SOUL_LANTERN);
        event.getRegistry().register(BLOCK_CHAIN);
        event.getRegistry().register(BLOCK_COMPOSTER);
        event.getRegistry().register(BLOCK_PEACEFUL_TABLE);
        event.getRegistry().register(BLOCK_STORAGE_PAIL);
        event.getRegistry().register(BLOCK_WASTE_DIRT);
        event.getRegistry().register(BLOCK_SAP_BAG);
        GameRegistry.registerTileEntity(TileEntityGoldenSea.class, new ResourceLocation(Tags.MODID, "gold_coin"));
        GameRegistry.registerTileEntity(TileEntityDriedGhast.class, new ResourceLocation(Tags.MODID, "dried_ghast"));
        GameRegistry.registerTileEntity(TileEntityPeacefulTable.class, new ResourceLocation(Tags.MODID, "peaceful_table"));
        GameRegistry.registerTileEntity(TileEntityStoragePail.class, new ResourceLocation(Tags.MODID, "storage_pail"));
        GameRegistry.registerTileEntity(TileEntitySapBag.class, new ResourceLocation(Tags.MODID, "sap_bag"));
        GameRegistry.registerTileEntity(TileEntityComposter.class, new ResourceLocation(Tags.MODID, "composter"));
        event.getRegistry().register(TRANSPARENT_CASING1);
        event.getRegistry().register(COMMON_CASING);
        event.getRegistry().register(COMMON_CASING1);
        event.getRegistry().register(FUSION_REACTOR_CASING);
        event.getRegistry().register(YOT_TANK);
        event.getRegistry().register(TFFT_TANK);
        event.getRegistry().register(BLOCK_ADVANCED_CAULDRON);
        GameRegistry.registerTileEntity(TileEntityAdvancedCauldron.class, new ResourceLocation(Tags.MODID, "advanced_cauldron"));
        event.getRegistry().register(BLOCK_TIME_TABLE);
        GameRegistry.registerTileEntity(TileEntityTimeTable.class, new ResourceLocation(Tags.MODID, "time_table"));
        event.getRegistry().register(CROP_STICK);
        GameRegistry.registerTileEntity(TileCropStick.class, Tags.MODID + ":crop_stick");
        Datas.init();
    }

    public static Vector3f randomSpherePoint(double x0, double y0, double z0, Vec3d radius, Random rand) {
        double u = rand.nextDouble();
        double v = rand.nextDouble();
        double theta = 6.283185307179586 * u;
        double phi = Math.acos(2.0 * v - 1.0);
        double x = x0 + radius.x * Math.sin(phi) * Math.cos(theta);
        double y = y0 + radius.y * Math.sin(phi) * Math.sin(theta);
        double z = z0 + radius.z * Math.cos(phi);
        return new Vector3f((float) x, (float) y, (float) z);
    }
}