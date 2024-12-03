package com.drppp.drtech.intergations.Forestry;

public enum DrtDropType {

    ETHYLENE("ethylene", 0x9AA4A5, 0x9AA4A5),
    TETRAFLUOROETHYLENE("tetrafluoroethylene", 0x585858, 0x585858);

    public static final DrtDropType[] VALUES = values();

    public final String name;
    public final int[] color;

    DrtDropType(String name, int primary, int secondary) {
        this.name = name;
        this.color = new int[] { primary, secondary };
    }

    public static DrtDropType getDrop(int meta) {
        return meta < 0 || meta >= VALUES.length ? VALUES[0] : VALUES[meta];
    }
}
