package com.drppp.drtech.drone.program.model;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

public final class DroneProgramNode {

    private final UUID id;
    private final ResourceLocation type;
    private final int x;
    private final int y;
    private final NBTTagCompound configuration;

    public DroneProgramNode(UUID id, ResourceLocation type, int x, int y, NBTTagCompound configuration) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.x = x;
        this.y = y;
        this.configuration = configuration == null ? new NBTTagCompound() : configuration.copy();
    }

    public static DroneProgramNode create(ResourceLocation type, int x, int y) {
        return new DroneProgramNode(UUID.randomUUID(), type, x, y, new NBTTagCompound());
    }

    public UUID getId() {
        return id;
    }

    public ResourceLocation getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public NBTTagCompound getConfiguration() {
        return configuration.copy();
    }

    public DroneProgramNode movedTo(int x, int y) {
        return new DroneProgramNode(id, type, x, y, configuration);
    }

    public DroneProgramNode withConfiguration(NBTTagCompound configuration) {
        return new DroneProgramNode(id, type, x, y, configuration);
    }
}
