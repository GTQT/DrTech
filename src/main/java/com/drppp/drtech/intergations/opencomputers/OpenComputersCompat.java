package com.drppp.drtech.intergations.opencomputers;

/** Optional OpenComputers bridge. The core mod never links OC classes directly. */
public final class OpenComputersCompat {
    private static final String API_CLASS = "li.cil.oc.api.Network";
    private static final String REGISTRAR_CLASS =
            "com.drppp.drtech.compat.opencomputers.OpenComputersDriverRegistrar";
    private static boolean initialized;
    private OpenComputersCompat() { }

    public static boolean isAvailable() {
        try { Class.forName(API_CLASS, false, OpenComputersCompat.class.getClassLoader()); return true; }
        catch (ClassNotFoundException ignored) { return false; }
    }

    /** Loads the OC-linked registrar only when the API is actually present. */
    public static synchronized void initialize() {
        if (initialized || !isAvailable()) return;
        try {
            Class<?> registrar = Class.forName(REGISTRAR_CLASS, true, OpenComputersCompat.class.getClassLoader());
            registrar.getMethod("register").invoke(null);
            initialized = true;
        } catch (ReflectiveOperationException | LinkageError error) {
            com.drppp.drtech.DrTechMain.LOGGER.error("Unable to initialize optional OpenComputers integration", error);
        }
    }

    public static String statusKey() {
        return isAvailable() ? "drtech.compat.opencomputers.available" : "drtech.compat.opencomputers.unavailable";
    }
}
