package com.meowmel.cropQT.api.capability;

public interface IFarmPart {
    // 升级的类型
    FarmType getFarmType();

    // 升级模块的等级
    int getPartTier();
}
