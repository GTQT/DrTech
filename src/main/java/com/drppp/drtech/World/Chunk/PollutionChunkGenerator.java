package com.drppp.drtech.World.Chunk;

import com.drppp.drtech.World.Biome.BiomeHandler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkGeneratorSettings;

import java.util.Random;

public class PollutionChunkGenerator extends ChunkGeneratorOverworld {
    private final Random rand;
    private Biome[] biomesForGeneration;
    private ChunkGeneratorSettings settings;
    private final World world;
    public PollutionChunkGenerator(World world, long seed, boolean mapFeaturesEnabled, String generatorOptions) {
        super(world, seed, mapFeaturesEnabled, generatorOptions);
        this.rand = new Random(seed);
        this.world = world;
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        // 在这里，你可以完全自定义区块的生成，包括使用你的自定义群系
        // 为了简化，你可以复制默认的generateChunk逻辑，但总是返回你的群系
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        this.setBlocksInChunk(x, z, chunkprimer);
        this.biomesForGeneration = new Biome[1];//this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.biomesForGeneration[0]= BiomeHandler.POLLUTION_BIOME;
        //this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration);

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte) Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }
}