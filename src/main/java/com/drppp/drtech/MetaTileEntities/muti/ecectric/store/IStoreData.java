package com.drppp.drtech.MetaTileEntities.muti.ecectric.store;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public interface IStoreData {
    int getTier();

    BigInteger getCapacity();

    @NotNull String getStoreName();
}
