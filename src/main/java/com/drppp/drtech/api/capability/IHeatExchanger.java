package com.drppp.drtech.api.capability;

public interface IHeatExchanger {
    //自身存储热量极限 0代表不存储热量
    int getMaxHeat();

    //获取当前已存储热量
    int getHeat();

    //设置热量
    void setHeat(int amount);

    //是否和反应堆交互
    boolean reactorInteraction();

    //是否与元件交互
    boolean elementInteraction();
    //反应堆传递速率
    int getReactorExchangeHeatRate();
    //元件传递速率
    int getElementExchangeHeatRate();
}
