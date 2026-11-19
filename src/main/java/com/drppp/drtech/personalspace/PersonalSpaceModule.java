package com.drppp.drtech.personalspace;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.drppp.drtech.personalspace.compat.TheOneProbeCompat;
import com.drppp.drtech.personalspace.proxy.CommonProxy;
import com.drppp.drtech.personalspace.world.PWWorldProvider;

/**
 * DrTech 的私人维度模块入口。功能实现整体照抄 PersonalWorlds-rewrite，
 * 这里只把它原来的 @Mod 入口改成普通静态入口，由 DrTechMain 调用。
 */
public final class PersonalSpaceModule {

    public static final Logger log = LogManager.getLogger("drtech_personalspace");
    public static DimensionType dimType;
    public static CommonProxy proxy;

    private PersonalSpaceModule() {}

    public static void preInit(FMLPreInitializationEvent event) {
        PWConfig.load(event.getModConfigurationDirectory());
        MinecraftForge.ORE_GEN_BUS.register(new CommonProxy.oreGenBusListener());
        MinecraftForge.TERRAIN_GEN_BUS.register(new CommonProxy.BiomeBusListener());
        MinecraftForge.EVENT_BUS.register(new CommonProxy());
        if (FMLCommonHandler.instance().getSide().isClient()) {
            try {
                proxy = (CommonProxy) Class.forName("com.drppp.drtech.personalspace.proxy.ClientProxy")
                        .getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to create personal space client proxy", ex);
            }
        } else {
            proxy = new CommonProxy();
        }
        proxy.onPreInit(event);
        dimType = DimensionType.register("personal_world", "pw", DimensionType.values().length,
                PWWorldProvider.class, false);
    }

    public static void init(FMLInitializationEvent event) {
        proxy.onInit(event);
        if (Loader.isModLoaded("theoneprobe")) {
            new TheOneProbeCompat().init();
        }
    }

    public static void onServerStarting(FMLServerStartingEvent event) {
        proxy.onServerStarting(event);
    }

    public static void onServerStopping(FMLServerStoppingEvent event) {
        proxy.onServerStopping(event);
    }

    public static void onServerStopped(FMLServerStoppedEvent event) {
        proxy.serverStopped(event);
    }
}
