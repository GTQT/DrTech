package com.drppp.drtech.personalspace.world;

import java.util.Arrays;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.drppp.drtech.personalspace.PersonalSpaceModule;
import com.drppp.drtech.personalspace.proxy.CommonProxy;

public class PWWorldProvider extends WorldProvider {

    private DimensionConfig dimensionConfig;
    private BiomeProviderSingle biomeProviderSingle;
    private PWChunkGenerator pwChunkGenerator;

    public PWWorldProvider() {}

    public DimensionConfig getConfig() {
        if (this.dimensionConfig == null) {
            boolean isClient = (this.world != null) ? this.world.isRemote :
                    FMLCommonHandler.instance().getEffectiveSide().isClient();
            this.dimensionConfig = DimensionConfig.getConfig(this.getDimension(), isClient);
            if (this.dimensionConfig == null) {
                PersonalSpaceModule.log.fatal(
                        "PersonalSpace couldn't find dimension config for dimension {}, detected side: {}\nknown client dimension IDs: {}\nknown server dimension IDs: {}\n",
                        this.getDimension(),
                        isClient ? "CLIENT" : "SERVER",
                        Arrays.toString(CommonProxy.getDimensionConfigs(true).keys()),
                        Arrays.toString(CommonProxy.getDimensionConfigs(false).keys()));
            }
        }
        return this.dimensionConfig;
    }

    public void refreshConfigCache() {
        this.biomeProviderSingle = null;
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionType.OVERWORLD;
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        if (pwChunkGenerator == null) {
            pwChunkGenerator = new PWChunkGenerator(this.world);
        }
        return pwChunkGenerator;
    }

    @Override
    public String getSaveFolder() {
        return "personal_world_" + this.getDimension();
    }

    @Override
    public BlockPos getSpawnCoordinate() {
        return new BlockPos(8, dimensionConfig.getGroundLevel(), 8);
    }

    @Override
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
        int color = getConfig().getSkyColor();

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        return new Vec3d(red, green, blue);
    }

    @Override
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        return getFogColor(0.0F, partialTicks);
    }

    @Override
    public float getCloudHeight() {
        return getConfig().cloudsEnabled() ? 256.0F : Float.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isSurfaceWorld() {
        return true;
    }

    @Override
    public boolean canRespawnHere() {
        return true;
    }

    @Override
    public boolean isDaytime() {
        if (getConfig().getDaylightCycle() == DimensionConfig.DaylightCycle.CYCLE) return super.isDaytime();

        return !(this.getConfig().getDaylightCycle() == DimensionConfig.DaylightCycle.MOON);
    }

    @Override
    public float getSunBrightnessFactor(float par1) {
        if (getConfig().getDaylightCycle() == DimensionConfig.DaylightCycle.CYCLE)
            return super.getSunBrightnessFactor(par1);

        return this.getConfig().getDaylightCycle() == DimensionConfig.DaylightCycle.MOON ? 0.0f : 1.0f;
    }

    @Override
    public float getSunBrightness(float par1) {
        if (getConfig().getDaylightCycle() == DimensionConfig.DaylightCycle.CYCLE) return super.getSunBrightness(par1);

        return this.getConfig().getDaylightCycle() == DimensionConfig.DaylightCycle.MOON ? 0.2f : 1.0f;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getStarBrightness(float brightness) {
        if (getConfig().getDaylightCycle() == DimensionConfig.DaylightCycle.CYCLE)
            return super.getStarBrightness(brightness);
        return getConfig().getStarsVisibility();
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        if (getConfig().getDaylightCycle() == DimensionConfig.DaylightCycle.CYCLE)
            return super.calculateCelestialAngle(worldTime, partialTicks);

        return this.getConfig().getDaylightCycle() == DimensionConfig.DaylightCycle.MOON ? 0.5f : 0.0f;
    }

    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        BlockPos blockPos = this.world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        return this.world.getBlockState(blockPos).getMaterial().blocksMovement();
    }

    @Override
    public void updateWeather() {
        if (!this.world.isRemote && getConfig().weatherEnabled()) {
            super.updateWeather();
        } else {
            this.world.rainingStrength = 0.0f;
            this.world.thunderingStrength = 0.0f;
            this.world.prevRainingStrength = 0.0f;
            this.world.prevThunderingStrength = 0.0f;
        }
    }

    @Override
    public void calculateInitialWeather() {
        if (!this.world.isRemote && getConfig().weatherEnabled()) {
            super.calculateInitialWeather();
        } else {
            this.world.rainingStrength = 0.0f;
            this.world.thunderingStrength = 0.0f;
            this.world.prevRainingStrength = 0.0f;
            this.world.prevThunderingStrength = 0.0f;
            this.world.getWorldInfo().setRaining(false);
            this.world.getWorldInfo().setThundering(false);
        }
    }

    @Override
    public boolean canDoLightning(Chunk chunk) {
        return this.getConfig().weatherEnabled();
    }

    @Override
    public boolean canDoRainSnowIce(Chunk chunk) {
        return this.getConfig().weatherEnabled();
    }

    @Override
    public boolean canBlockFreeze(BlockPos pos, boolean byWater) {
        return this.getConfig().weatherEnabled() && super.canBlockFreeze(pos, byWater);
    }

    @Override
    public boolean canSnowAt(BlockPos pos, boolean checkLight) {
        return this.getConfig().weatherEnabled() && super.canSnowAt(pos, checkLight);
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        if (biomeProviderSingle == null) {
            biomeProviderSingle = new BiomeProviderSingle(this.getConfig().getBiome());
        }
        return biomeProviderSingle;
    }

    @Override
    public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {
        super.setAllowedSpawnTypes(getConfig().spawnMonsters(), getConfig().spawnPassiveMobs());
    }
}
