package com.drppp.drtech.World.Chunk;

import com.drppp.drtech.World.WordStruct.StructRuins;
import com.drppp.drtech.common.Blocks.BlocksInit;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Random;

public class Ross128ChunkGenerator extends ChunkGeneratorOverworld {
    private Random rand = new Random();
    private final World world;
    private final StructRuins.RuinsBase ruinsBase = new StructRuins.RuinsBase();
    public Ross128ChunkGenerator(World worldIn, long seed, boolean mapFeaturesEnabledIn, String generatorOptions) {
        super(worldIn, seed, mapFeaturesEnabledIn, generatorOptions);
        this.world = worldIn;
    }


    /**
     * Generate initial structures in this chunk, e.g. mineshafts, temples, lakes, and dungeons
     *
     * @param x Chunk x coordinate
     * @param z Chunk z coordinate
     */
    @Override
    public void populate(int x, int z) {
        int k = x * 16;
        int l = z * 16;
        int x1;
        int y1;
        int z1;
        super.populate(x, z);
        Biome biomegenbase = this.world.getBiomeForCoordsBody(new BlockPos(k + 16, 80,l + 16));
        if ( !BiomeDictionary.hasType(biomegenbase, BiomeDictionary.Type.OCEAN) && BiomeDictionary.hasType(biomegenbase, BiomeDictionary.Type.RIVER)
                && this.rand.nextInt(10) == 0) {
            x1 = k + this.rand.nextInt(16) + 3;
            y1 = this.rand.nextInt(256);
            z1 = l + this.rand.nextInt(16) + 3;
            this.ruinsBase.generate(this.world, this.rand, new BlockPos(x1, y1, z1));
        }

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
                    IBlockState podzolState = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
                    primer.setBlockState(i, y, j, podzolState);
                }
            }
        }
    }
}
