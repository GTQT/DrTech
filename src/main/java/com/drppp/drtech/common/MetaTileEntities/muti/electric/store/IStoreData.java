package com.drppp.drtech.common.MetaTileEntities.muti.electric.store;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public interface IStoreData {
    int getTier();

    BigInteger getCapacity();

    @NotNull String getStoreName();
}
