package com.drppp.drtech.api.capability;

public interface ICoolantCell {
    int getDurability();
    int getMaxDurability();
    //设置当前耐久
    void setDurability(int now);
}
