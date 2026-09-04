package com.drppp.drtech.lootgames.api.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.Function;
import java.util.function.Supplier;

public class NBTUtils {

    public static <T extends INBTSerializable<NBTTagCompound>> NBTTagCompound writeTwoDimArrToNBT(T[][] arr) {
        return writeTwoDimArrToNBT(arr, T::serializeNBT);
    }

    public static <T> NBTTagCompound writeTwoDimArrToNBT(T[][] arr, Function<T, NBTTagCompound> serializer) {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList outerList = new NBTTagList();

        for (T[] innerArr : arr) {
            NBTTagList innerList = new NBTTagList();
            for (T elem : innerArr) {
                if (elem != null) {
                    innerList.appendTag(serializer.apply(elem));
                }
            }
            outerList.appendTag(innerList);
        }

        compound.setTag("data", outerList);
        compound.setInteger("width", arr.length);
        compound.setInteger("height", arr[0].length);
        return compound;
    }

    public static <T extends INBTSerializable<NBTTagCompound>> T[][] readTwoDimArrFromNBT(NBTTagCompound compound, Class<T> clazz, Supplier<T> factory) {
        return readTwoDimArrFromNBT(compound, clazz, nbt -> {
            T instance = factory.get();
            instance.deserializeNBT(nbt);
            return instance;
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> T[][] readTwoDimArrFromNBT(NBTTagCompound compound, Class<T> clazz, Function<NBTTagCompound, T> deserializer) {
        NBTTagList outerList = compound.getTagList("data", 10);
        int width = compound.getInteger("width");
        int height = compound.getInteger("height");

        T[][] result = (T[][]) java.lang.reflect.Array.newInstance(clazz, width, height);

        for (int i = 0; i < outerList.tagCount(); i++) {
            NBTTagList innerList = (NBTTagList) outerList.get(i);
            for (int j = 0; j < innerList.tagCount(); j++) {
                result[i][j] = deserializer.apply(innerList.getCompoundTagAt(j));
            }
        }

        return result;
    }
}
