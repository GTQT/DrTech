package com.drppp.drtech.lootgames.api.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public interface ITask extends INBTSerializable<NBTTagCompound> {
    void run(World world);
}
