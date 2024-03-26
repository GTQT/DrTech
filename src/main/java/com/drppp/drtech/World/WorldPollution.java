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

    @Override
    public void init() {
        this.hasSkyLight = true;
        this.biomeProvider = new BiomeProviderSingle(BiomeHandler.POLLUTION_BIOME);
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionType.OVERWORLD;
    }

//    @Override
//    public float calculateCelestialAngle(long worldTime, float partialTicks) {
//        return 18000;
//    }

    @Override
    public boolean isSurfaceWorld() {
        return true; // 使得天空变暗
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

    // 重写getSunBrightnessFactor和getSunBrightnessBody来控制白天亮度
//    @Override
//    public float getSunBrightnessFactor(float par1) {
//        return 0.9F; // 根据需要调整值
//    }

//    @Override
//    public float getSunBrightness(float par1) {
//        return 2.0F; // 白天最亮
//    }

    // 重写以取消维度中的下雨效果
    @Override
    public boolean canDoRainSnowIce(Chunk chunk) {
        return true;
    }

    // 重写以确定玩家是否能够睡觉
    @Override
    public boolean canRespawnHere() {
        return false;
    }

    // 如果你想要永远白天且不会变成夜晚，你可以重写这个方法
    @Override
    public boolean isDaytime() {
        return false;
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new PollutionChunkGenerator(this.world,this.getSeed(),true,"{\"useLavaOceans\": true, \"seaLevel\": 63}");
    }
}