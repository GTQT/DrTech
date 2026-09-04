package com.drppp.drtech.lootgames.api.minigame;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameManager {
    private static final Map<Class<? extends LootGame>, ILootGameFactory> GAME_MAP = new HashMap<>();
    private static final List<ILootGameFactory> GAME_GEN_LIST = new ArrayList<>();
    private final Random rand = new Random();

    public <T extends LootGame> void registerGame(Class<T> clazz, ILootGameFactory generator) {
        GAME_MAP.put(clazz, generator);
        GAME_GEN_LIST.add(generator);
    }

    public void generateRandomGame(World world, BlockPos puzzleMasterPos, BlockPos bottomPos, BlockPos topPos) {
        if (!GAME_GEN_LIST.isEmpty()) {
            GAME_GEN_LIST.get(rand.nextInt(GAME_GEN_LIST.size())).genOnPuzzleMasterClick(world, puzzleMasterPos, bottomPos, topPos);
        }
    }
}
