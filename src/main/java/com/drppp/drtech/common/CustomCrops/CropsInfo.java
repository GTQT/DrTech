package com.drppp.drtech.common.CustomCrops;

public enum CropsInfo {
    GAN_ZHE("GAN_ZHE",1,1),
    TU_DOU("TU_DOU",1,2),
    HU_LUO_BO("HU_LUO_BO",1,3),
    XIAO_MAI("XIAO_MAI",1,4);
    private String name;
    private int tire;
    private int index;
    private int[] parent = new int[2];
    private CropsInfo(String name,int tire,int index){
        this.tire = tire;
        this.name = name;
        this.index = index;
        parent[0] = 1;
        parent[1] = 2;
    }
}
