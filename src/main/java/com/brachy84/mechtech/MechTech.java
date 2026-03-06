package com.brachy84.mechtech;

import com.brachy84.mechtech.api.armor.Modules;
import com.brachy84.mechtech.client.Keys;
import com.brachy84.mechtech.client.Sounds;
import com.brachy84.mechtech.common.CommonProxy;
import com.brachy84.mechtech.common.cover.MTCoverBehaviors;
import com.brachy84.mechtech.common.machines.MTTileEntities;
import com.brachy84.mechtech.common.machines.multis.MetaTileEntityTeslaTower;
import com.brachy84.mechtech.network.NetworkHandler;
import gregtech.GTInternalTags;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = MechTech.MODID,
        name = MechTech.NAME,
        version = MechTech.VERSION,
        dependencies = "required:forge@[14.23.5.2847,);" + GTInternalTags.DEP_VERSION_STRING)
public class MechTech {

    public static final String MODID = "mechtech";
    public static final String NAME = "MechTech";
    public static final String VERSION = "1.9.0";

    public static final Keys keys = new Keys();

    public static final Logger logger = LogManager.getLogger("MechTech");

    @SidedProxy(modId = MODID, clientSide = "com.brachy84.mechtech.common.ClientProxy",
            serverSide = "com.brachy84.mechtech.common.CommonProxy")
    public static CommonProxy proxy;

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        NetworkHandler.init();
        proxy.preLoad();
        Sounds.registerSounds();
        MTTileEntities.init();
        MetaTileEntityTeslaTower.initTorusBlocks();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        MTCoverBehaviors.init();
        Modules.init();
    }

    public static String blockPosToString(BlockPos pos) {
        return "X: " + pos.getX() + ", Y: " + pos.getY() + ", Z: " + pos.getZ();
    }

    public static Vec3d getMiddleOf(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static Vec3d getMiddleOf(Entity entity) {
        return new Vec3d(entity.posX, entity.posY + entity.height / 2, entity.posZ);
    }
}
