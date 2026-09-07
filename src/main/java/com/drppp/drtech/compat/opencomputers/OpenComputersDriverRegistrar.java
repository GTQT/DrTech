package com.drppp.drtech.compat.opencomputers;

import li.cil.oc.api.Driver;

/** This class is reflectively loaded only after the OC API presence check succeeds. */
public final class OpenComputersDriverRegistrar {
    private static boolean registered;
    private OpenComputersDriverRegistrar() { }

    public static synchronized void register() {
        if (registered) return;
        Driver.add(new OpenComputersDroneDriver());
        registered = true;
    }
}
