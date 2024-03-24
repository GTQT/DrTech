package com.drppp.drtech.api.WirelessNetwork;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.drppp.drtech.api.WirelessNetwork.GlobalVariableStorage.GlobalEnergy;


public class GlobalEnergyWorldSavedData extends WorldSavedData {

    public static GlobalEnergyWorldSavedData INSTANCE = new GlobalEnergyWorldSavedData();

    private static final String DATA_NAME = "Drtech_WirelessEUWorldSavedData";

    private static final String GlobalEnergyNBTTag = "Drtech_GlobalEnergy_MapNBTTag";


    private static void loadInstance(World world) {

        GlobalEnergy.clear();

        MapStorage storage = world.getMapStorage();
        INSTANCE = (GlobalEnergyWorldSavedData) storage.getOrLoadData(GlobalEnergyWorldSavedData.class, DATA_NAME);
        if (INSTANCE == null) {
            INSTANCE = new GlobalEnergyWorldSavedData();
            storage.setData(DATA_NAME, INSTANCE);
        }
        INSTANCE.markDirty();
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            loadInstance(event.getWorld());
        }
    }

    public GlobalEnergyWorldSavedData() {
        super(DATA_NAME);
    }

    @SuppressWarnings("unused")
    public GlobalEnergyWorldSavedData(String name) {
        super(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readFromNBT(NBTTagCompound nbtTagCompound) {

        try {
            byte[] ba = nbtTagCompound.getByteArray(GlobalEnergyNBTTag);
            InputStream byteArrayInputStream = new ByteArrayInputStream(ba);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object data = objectInputStream.readObject();
            HashMap<Object, BigInteger> hashData = (HashMap<Object, BigInteger>) data;
            for (Map.Entry<Object, BigInteger> entry : hashData.entrySet()) {
                GlobalEnergy.put(
                    UUID.fromString(
                        entry.getKey()
                            .toString()),
                    entry.getValue());
            }
        } catch (IOException | ClassNotFoundException exception) {
            System.out.println(GlobalEnergyNBTTag + " FAILED");
            exception.printStackTrace();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(GlobalEnergy);
            objectOutputStream.flush();
            byte[] data = byteArrayOutputStream.toByteArray();
            nbtTagCompound.setByteArray(GlobalEnergyNBTTag, data);
        } catch (IOException exception) {
            System.out.println(GlobalEnergyNBTTag + " SAVE FAILED");
            exception.printStackTrace();
        }
        return nbtTagCompound;
    }
}
