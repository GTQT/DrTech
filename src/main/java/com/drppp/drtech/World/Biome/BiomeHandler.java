package com.drppp.drtech.World.Biome;

import com.drppp.drtech.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class BiomeHandler {

    // 创建一个静态实例
    public static final Biome POLLUTION_BIOME = new BiomePollution(); // 替换为你的自定义生物群系类

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        IForgeRegistry<Biome> registry = event.getRegistry();

        // 设置注册名
        POLLUTION_BIOME.setRegistryName(new ResourceLocation(Tags.MODID, "pollution_biome"));

        // 注册生物群系
        registry.register(POLLUTION_BIOME);
        //玩家可以在这个生物群系出生
        BiomeManager.addSpawnBiome(POLLUTION_BIOME);
    }
}
