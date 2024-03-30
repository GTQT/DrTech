package com.drppp.drtech.World.WordStruct;

import java.util.ArrayList;
import java.util.List;

public class StructUtil {
    public static List<Integer> cableid;
    public static List<Integer> machineLv;
    public static List<Integer> machineMv;
    public static List<Integer> machineHv;
    public static List<Integer> machineEv;
    public static List<List<Integer>> machines;

    public static void init()
    {
        cableid = new ArrayList<>();
        machineLv = new ArrayList<>();
        machineMv = new ArrayList<>();
        machineHv = new ArrayList<>();
        machineEv = new ArrayList<>();
        machines=new ArrayList<>();
        cableid.add(112);
        cableid.add(252);
        cableid.add(41);
        cableid.add(2);
        for (int i = 50; i <=965 ; i+=15) {
            machineLv.add(i);
            machineMv.add(i+1);
            machineHv.add(i+2);
            machineEv.add(i+3);
        }
        machines.add(machineLv);
        machines.add(machineMv);
        machines.add(machineHv);
        machines.add(machineEv);
    }
}
