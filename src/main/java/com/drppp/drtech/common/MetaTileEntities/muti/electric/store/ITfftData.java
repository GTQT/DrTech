package com.drppp.drtech.common.MetaTileEntities.muti.electric.store;

import org.jetbrains.annotations.NotNull;

public interface ITfftData {
    int getTier();
    int getEut();

    long getCapacity();

    @NotNull String getBatteryName();
}
