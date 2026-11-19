package com.drppp.drtech.personalspace.world;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.ForgeEventFactory;

public class PWChunkGenerator implements IChunkGenerator {

    private final World world;
    private final Random random;
    private final DimensionConfig dimensionConfig;

    public PWChunkGenerator(World world) {
        this.world = world;
        this.random = world.rand;
        this.dimensionConfig = ((PWWorldProvider) world.provider).getConfig();
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer chunkPrimer = new ChunkPrimer();
        int y = 0;
        if (!dimensionConfig.getLayers().isEmpty()) {
            for (FlatLayerInfo fli : dimensionConfig.getLayers()) {
                Block block = fli.getLayerMaterial().getBlock();
                if (block == null || block == Blocks.AIR) {
                    y += fli.getLayerCount();
                    continue;
                }
                for (; y < fli.getMinY() + fli.getLayerCount() && y < world.getHeight(); ++y) {
                    for (int localX = 0; localX < 16; localX++) {
                        for (int localZ = 0; localZ < 16; localZ++) {
                            chunkPrimer.setBlockState(localX, y, localZ, fli.getLayerMaterial());
                        }
                    }
                }
            }
        }

        int groundLevel = dimensionConfig.getGroundLevel() - 1;
        if (groundLevel >= 0 && groundLevel < world.getHeight()) {
            applyGridSettings(chunkPrimer, x, z, groundLevel);
        }

        Chunk chunk = new Chunk(world, chunkPrimer, x, z);
        byte biomeId = (byte) Biome.getIdForBiome(dimensionConfig.getBiome());
        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; i++) {
            biomeArray[i] = biomeId;
        }
        chunk.generateSkylightMap();
        return chunk;
    }

    private void applyGridSettings(ChunkPrimer chunkPrimer, int chunkX, int chunkZ, int groundLevel) {
        RoadGrid grid = RoadGrid.of(this.dimensionConfig);
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                IBlockState state = grid.stateAt(baseX + localX, baseZ + localZ);
                if (state != null) {
                    chunkPrimer.setBlockState(localX, groundLevel, localZ, state);
                }
            }
        }
    }

    @Override
    public void populate(int x, int z) {
        net.minecraft.block.BlockFalling.fallInstantly = true;
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        this.random.setSeed(this.world.getSeed());
        long k = this.random.nextLong() / 2L * 2L + 1L;
        long l = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long) x * k + (long) z * l ^ this.world.getSeed());
        Biome biome = world.provider.getBiomeProvider().getBiome(new BlockPos(i + 16, 0, j + 16));

        ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, x, z, false);
        // Only run decoration when something can actually be placed; the biome
        // listener already denies every decoration type in an empty dimension,
        // so calling into it would just waste the per-chunk loops.
        if (dimensionConfig.vegetationEnabled() || dimensionConfig.generateTrees()) {
            biome.decorate(world, random, blockpos);
        }
        ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, x, z, false);
        net.minecraft.block.BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position,
                                           boolean findUnexplored) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {}

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }
}
