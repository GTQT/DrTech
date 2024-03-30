package com.drppp.drtech.World;

import codechicken.lib.vec.Vector3;
import com.drppp.drtech.World.Chunk.PollutionChunkGenerator;
import com.drppp.drtech.World.Chunk.Ross128ChunkGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldRoss128  extends WorldProvider {
    public WorldRoss128() {
        this.hasSkyLight = true;

    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionType.OVERWORLD;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0.0F;
    }
    @Override
    public boolean isDaytime() {
        return true;
    }

    @Override
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        float red = 200 / 255f;
        float green = 120 / 255f;
        float blue = 0.0F;
        return new Vec3d(red, green, blue);
    }
    @Override
    public float getSunBrightness(float par1) {
        return super.getSunBrightness(par1) * 0.875f;
    }

    @Override
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
        float angle = MathHelper.cos(p_76562_1_ * (float) Math.PI * 2.0F) * 2.0F + 0.5F;

        if (angle < 0.0F) {
            angle = 0.0F;
        }

        if (angle > 1.0F) {
            angle = 1.0F;
        }

        float red = 200 / 255f;
        float green = 80 / 255f;
        float blue = 0.0F;
        red *= angle * 0.94F + 0.06F;
        green *= angle * 0.94F + 0.06F;
        return new Vec3d(red, green, blue);
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new Ross128ChunkGenerator(this.world,this.getSeed(),true,"{ \"useLavaOceans\": false,\"seaLevel\": 63,\"useVillages\": false}");
    }
}
