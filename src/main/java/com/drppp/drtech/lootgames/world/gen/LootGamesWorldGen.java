package com.drppp.drtech.lootgames.world.gen;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import com.drppp.drtech.DrTechMain;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * World generation for LootGames dungeons.
 * Uses a rhombus-based distribution to space dungeons evenly across the world.
 */
public class LootGamesWorldGen implements IWorldGenerator {

    /** Master switch - set to false to disable all dungeon world gen */
    private static final boolean DUNGEON_WORLD_GEN_ENABLED = true;

    /** Which dimensions dungeons can spawn in */
    private static final List<Integer> ENABLED_DIMENSIONS = Arrays.asList(0);

    /** Rhomb size per dimension: controls dungeon spacing. Larger = fewer dungeons. */
    private static final int DEFAULT_RHOMB_SIZE = 12;

    private final Random rand = new Random();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!checkSpawnConditions(chunkX, chunkZ, world)) return;

        DungeonGenerator dungeonGenerator = new DungeonGenerator();
        dungeonGenerator.generateDungeon(world, (chunkX << 4) + 8, (chunkZ << 4) + 8);
    }

    private boolean checkSpawnConditions(int chunkX, int chunkZ, World world) {
        if (!DUNGEON_WORLD_GEN_ENABLED) {
            DrTechMain.LOGGER.trace("LootGames world gen is DISABLED");
            return false;
        }

        int dim = world.provider.getDimension();
        if (!ENABLED_DIMENSIONS.contains(dim)) {
            DrTechMain.LOGGER.trace("Dim {} not whitelisted for LootGames world gen", dim);
            return false;
        }

        if (!canSpawnInChunk(chunkX, chunkZ, world)) {
            DrTechMain.LOGGER.trace("Chunk ({}, {}) not suitable for LootGames dungeon", chunkX, chunkZ);
            return false;
        }

        DrTechMain.LOGGER.trace("LootGames dungeon will be placed at chunk ({}, {})", chunkX, chunkZ);
        return true;
    }

    /**
     * Rhombus-based chunk selection algorithm.
     * Creates a deterministic but pseudo-random distribution of dungeon positions.
     */
    private boolean canSpawnInChunk(int chunkX, int chunkZ, World world) {
        int rhombSize = getRhombSize(world);

        int xc = (chunkX * 2) + chunkZ;
        int zc = (chunkZ * 2) + chunkX;

        rand.setSeed(world.getSeed()
                + (xc / (rhombSize * 2))
                + ((zc / (rhombSize * 2)) << 14));

        int pos1 = 3 + rand.nextInt(rhombSize * 2 - 3);
        int pos2 = 3 + rand.nextInt(rhombSize * 2 - 3);

        int modXC = mod(xc, rhombSize * 2);
        int modZC = mod(zc, rhombSize * 2);

        if (modXC >= 3 && modZC >= 3) {
            return (modXC == pos1 && modZC == pos2)
                    || (modXC == pos1 + 1 && (modZC == pos2 || modZC == pos2 + 1));
        }

        return false;
    }

    private int getRhombSize(World world) {
        int dim = world.provider.getDimension();
        // Currently all enabled dimensions use the same rhomb size.
        // Extend with a dimension→size map if needed.
        return DEFAULT_RHOMB_SIZE;
    }

    private int mod(int x, int div) {
        int r = x % div;
        return r < 0 ? r + div : r;
    }
}
