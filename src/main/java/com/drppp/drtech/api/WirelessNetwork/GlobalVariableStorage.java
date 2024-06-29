package com.drppp.drtech.api.WirelessNetwork;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

public abstract class GlobalVariableStorage {
    public static HashMap<UUID, BigInteger> GlobalEnergy = new HashMap<>(100, 0.9f);
    public static HashMap<UUID, UUID> GlobalMay = new HashMap<>(100, 0.9f);
}
