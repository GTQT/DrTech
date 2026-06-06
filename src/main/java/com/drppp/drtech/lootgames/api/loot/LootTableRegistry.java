package com.drppp.drtech.lootgames.api.loot;

import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for game loot tables.
 * Loot tables should be registered during mod initialization.
 */
public class LootTableRegistry {
    private static final Map<ResourceLocation, GameLootTable> TABLES = new HashMap<>();

    /**
     * Registers a game loot table.
     *
     * @param table the loot table to register
     * @throws IllegalArgumentException if a table with the same name is already registered
     */
    public static void register(GameLootTable table) {
        ResourceLocation name = table.getName();
        if (TABLES.containsKey(name)) {
            throw new IllegalArgumentException("Loot table " + name + " is already registered!");
        }
        TABLES.put(name, table);
    }

    /**
     * Retrieves a registered loot table by name.
     *
     * @param name the resource location of the loot table
     * @return the loot table, or null if not found
     */
    @Nullable
    public static GameLootTable get(ResourceLocation name) {
        return TABLES.get(name);
    }

    /**
     * Checks if a loot table with the given name is registered.
     */
    public static boolean has(ResourceLocation name) {
        return TABLES.containsKey(name);
    }

    /**
     * Removes all registered loot tables. Used for reloading.
     */
    public static void clear() {
        TABLES.clear();
    }
}
