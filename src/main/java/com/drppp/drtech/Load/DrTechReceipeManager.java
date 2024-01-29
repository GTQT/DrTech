package com.drppp.drtech.Load;

public class DrTechReceipeManager {
    public static void init()
    {
        MachineReceipe.load();
        CraftingReceipe.load();
    }
}
