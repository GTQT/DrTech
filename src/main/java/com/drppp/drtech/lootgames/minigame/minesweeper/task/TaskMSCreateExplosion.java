package com.drppp.drtech.lootgames.minigame.minesweeper.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.drppp.drtech.lootgames.api.task.ITask;

public class TaskMSCreateExplosion implements ITask {
    private double x, y, z;
    private float strength;
    private boolean damagesTerrain;
    private int masterX, masterZ;

    public TaskMSCreateExplosion() {}

    public TaskMSCreateExplosion(BlockPos masterPos, BlockPos bombPos, float strength, boolean damagesTerrain) {
        this.x = bombPos.getX();
        this.y = bombPos.getY();
        this.z = bombPos.getZ();
        this.strength = strength;
        this.damagesTerrain = damagesTerrain;
        this.masterX = masterPos.getX();
        this.masterZ = masterPos.getZ();
    }

    @Override
    public void run(World world) {
        world.createExplosion(null, x, y + 0.5, z, strength, damagesTerrain);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound c = new NBTTagCompound();
        c.setDouble("x", x);
        c.setDouble("y", y);
        c.setDouble("z", z);
        c.setFloat("strength", strength);
        c.setBoolean("damagesTerrain", damagesTerrain);
        c.setInteger("masterX", masterX);
        c.setInteger("masterZ", masterZ);
        return c;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        x = nbt.getDouble("x");
        y = nbt.getDouble("y");
        z = nbt.getDouble("z");
        strength = nbt.getFloat("strength");
        damagesTerrain = nbt.getBoolean("damagesTerrain");
        masterX = nbt.getInteger("masterX");
        masterZ = nbt.getInteger("masterZ");
    }
}
