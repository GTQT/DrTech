package com.drppp.drtech.World.SaveDatas;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerSpawnData extends WorldSavedData {
    private static final String DATA_NAME = "Drtech_PlayerSpawnData";
    private final Set<UUID> hasSpawned = new HashSet<>();

    public PlayerSpawnData() {
        super(DATA_NAME);
    }

    public PlayerSpawnData(String name) {
        super(name);
    }

    public boolean hasPlayerSpawned(UUID playerId) {
        return hasSpawned.contains(playerId);
    }

    public void markPlayerSpawned(UUID playerId) {
        hasSpawned.add(playerId);
        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("hasSpawned", 10);
        for (NBTBase base : list) {
            NBTTagCompound tag = (NBTTagCompound) base;
            UUID playerId = UUID.fromString(tag.getString("uuid"));
            hasSpawned.add(playerId);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (UUID playerId : hasSpawned) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("uuid", playerId.toString());
            list.appendTag(tag);
        }
        nbt.setTag("hasSpawned", list);
        return nbt;
    }

    public static PlayerSpawnData get(World world) {
        MapStorage storage = world.getMapStorage();
        PlayerSpawnData instance = (PlayerSpawnData) storage.getOrLoadData(PlayerSpawnData.class, DATA_NAME);

        if (instance == null) {
            instance = new PlayerSpawnData();
            storage.setData(DATA_NAME, instance);
        }

        return instance;
    }
}