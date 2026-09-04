package com.drppp.drtech.lootgames.api.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TaskCreateExplosion implements ITask {
    protected double x;
    private double y;
    private double z;
    private float strength;
    private boolean damagesTerrain;

    public TaskCreateExplosion() {}

    public TaskCreateExplosion(BlockPos pos, float strength, boolean damagesTerrain) {
        this(pos.getX(), pos.getY(), pos.getZ(), strength, damagesTerrain);
    }

    public TaskCreateExplosion(double x, double y, double z, float strength, boolean damagesTerrain) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.strength = strength;
        this.damagesTerrain = damagesTerrain;
    }

    @Override
    public void run(World world) {
        world.createExplosion(null, x, y, z, strength, damagesTerrain);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound c = new NBTTagCompound();
        c.setDouble("x", x);
        c.setDouble("y", y);
        c.setDouble("z", z);
        c.setFloat("strength", strength);
        c.setBoolean("damagesTerrain", damagesTerrain);
        return c;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        x = nbt.getDouble("x");
        y = nbt.getDouble("y");
        z = nbt.getDouble("z");
        strength = nbt.getFloat("strength");
        damagesTerrain = nbt.getBoolean("damagesTerrain");
    }
}
