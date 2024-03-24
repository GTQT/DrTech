package com.drppp.drtech.api.WirelessNetwork;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import java.math.BigInteger;
import java.util.UUID;

import static com.drppp.drtech.api.WirelessNetwork.GlobalVariableStorage.GlobalEnergy;


public class WirelessNetworkManager {

    private WirelessNetworkManager() {}

    public static void strongCheckOrAddUser(UUID user_uuid) {
        if (!GlobalEnergy.containsKey(user_uuid)) {
            GlobalEnergy.put(user_uuid, BigInteger.ZERO);
        }
    }


    public static boolean addEUToGlobalEnergyMap(UUID user_uuid, BigInteger EU) {
        try {
            GlobalEnergyWorldSavedData.INSTANCE.markDirty();
        } catch (Exception exception) {
            System.out.println("COULD NOT MARK GLOBAL ENERGY AS DIRTY IN ADD EU");
            exception.printStackTrace();
        }

        BigInteger totalEU = GlobalEnergy.getOrDefault(user_uuid, BigInteger.ZERO);
        totalEU = totalEU.add(EU);
        if (totalEU.signum() >= 0) {
            GlobalEnergy.put(user_uuid, totalEU);
            return true;
        }
        return false;
    }

    public static boolean addEUToGlobalEnergyMap(UUID user_uuid, long EU) {
        return addEUToGlobalEnergyMap(user_uuid, BigInteger.valueOf(EU));
    }

    public static BigInteger getUserEU(UUID user_uuid) {
        return GlobalEnergy.getOrDefault(user_uuid, BigInteger.ZERO);
    }

    public static void setUserEU(UUID user_uuid, BigInteger EU) {
        try {
            GlobalEnergyWorldSavedData.INSTANCE.markDirty();
        } catch (Exception exception) {
            System.out.println("COULD NOT MARK GLOBAL ENERGY AS DIRTY IN SET EU");
            exception.printStackTrace();
        }

        GlobalEnergy.put(user_uuid, EU);
    }

    public static void clearGlobalEnergyInformationMaps() {
        GlobalEnergy.clear();
    }

}
