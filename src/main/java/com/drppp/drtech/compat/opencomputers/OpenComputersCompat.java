package com.drppp.drtech.compat.opencomputers;

/** Optional OpenComputers bridge. The core mod never links OC classes directly. */
public final class OpenComputersCompat {
    private static final String API_CLASS = "li.cil.oc.api.Network";
    private OpenComputersCompat() { }

    public static boolean isAvailable() {
        try { Class.forName(API_CLASS, false, OpenComputersCompat.class.getClassLoader()); return true; }
        catch (ClassNotFoundException ignored) { return false; }
    }

    public static String statusKey() {
        return isAvailable() ? "drtech.compat.opencomputers.available" : "drtech.compat.opencomputers.unavailable";
    }
}
