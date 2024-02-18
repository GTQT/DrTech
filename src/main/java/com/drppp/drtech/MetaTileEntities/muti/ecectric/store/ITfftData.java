package com.drppp.drtech.MetaTileEntities.muti.ecectric.store;

import org.jetbrains.annotations.NotNull;

public interface ITfftData {
    int getTier();
    int getEut();

    long getCapacity();

    @NotNull String getBatteryName();
}
