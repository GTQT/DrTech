package com.drppp.drtech.World;


import com.drppp.drtech.World.Biome.BiomeHandler;
import com.drppp.drtech.World.Chunk.PollutionChunkGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldPollution extends WorldProvider {
    private int skylightSubtracted=10;
    @Override
    public void init() {
        this.hasSkyLight = true;
        this.biomeProvider = new BiomeProviderSingle(BiomeHandler.POLLUTION_BIOME);
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionType.OVERWORLD;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        // 返回一个表示夜晚某个时间点的值，比如 0.75 代表深夜
        return 0.7512f;
    }
    @Override
    public boolean isDaytime() {
        // 永远返回 false，模拟永远是夜晚
        return false;
    }

    @Override
    public boolean isSurfaceWorld() {
        return false; // 使得天空变暗
    }

    // 重写返回天空颜色的方法，可以进一步暗化天空
    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return new float[]{0.486f, 0.988f, 0.0f, 0.5f}; // 红，绿，蓝，Alpha（不透明）
    }

    @Override
    public Vec3d getCloudColor(float partialTicks) {
        return new Vec3d(1, 0.2, 0.1);
    }

    @Override
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        return new Vec3d(0, 0.9, 0);
    }

    // 重写以确定玩家是否能够睡觉
    @Override
    public boolean canRespawnHere() {
        return false;
    }
    @Override
    public void updateWeather() {
        // 忽略原来的天气更新逻辑，强制设置为下雨状态
        if(!this.world.getWorldInfo().isRaining())
         this.world.getWorldInfo().setRaining(true); // 强制设置为下雨状态
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new PollutionChunkGenerator(this.world,this.getSeed(),true,"{\"useLavaOceans\": true, \"seaLevel\": 63}");
    }
}