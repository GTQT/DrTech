package com.drppp.drtech.World.Chunk;

import com.drppp.drtech.World.Biome.BiomeHandler;
import com.drppp.drtech.common.Blocks.BlocksInit;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
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
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration);
        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i)
        {
            abyte[i] = (byte)Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }
    @Override
    public void replaceBiomeBlocks(int x, int z, ChunkPrimer primer, Biome[] biomesIn)
    {
        super.replaceBiomeBlocks(x,z,primer,biomesIn);
        if (!net.minecraftforge.event.ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, this.world)) return;
        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                // 这里我们从最大高度（通常是世界的海平面高度）开始向下检查，找到第一个非空气方块
                int y = 150;
                boolean replace = false;

                // 循环向下直到找到非空气方块
                while (y > 0) {
                    IBlockState state = primer.getBlockState(i, y, j);
                    if (state.getMaterial() != Material.AIR) {
                        // 如果是草方块或者泥土，则将replace设置为true
                        if (state.getBlock() == Blocks.GRASS || state.getBlock() == Blocks.DIRT) {
                            replace = true;
                        }
                        break;
                    }
                    y--;
                }

                // 如果找到了要替换的方块，则进行替换
                if (replace) {
                    primer.setBlockState(i, y, j, BlocksInit.BLOCK_WASTE_DIRT.getDefaultState());
                }
            }
        }
    }
}