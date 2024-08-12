package com.drppp.drtech.intergations.Forestry;


public enum DrtCombType {

    // 内容颜色 边框颜色
    BORAX("borax", 0x178c29, 0xbab8fe),
    ETHER("ether", 0x117c29, 0xba18f8),
    BRIGHT("bright", 0x7A007A, 0xFFFFFF),
    WITHER("wither", 0x040102, 0x144F5B),
    MUTAGENIC_AGENT("mutagenic_agent", 0xa39f00, 0x1fff5b);

    public static final DrtCombType[] VALUES = values();

    public final boolean showInList;
    public final String name;
    public final int[] color;

    DrtCombType(String name, int primary, int secondary) {
        this(name, primary, secondary, true);
    }

    DrtCombType(String name, int primary, int secondary, boolean show) {
        this.name = name;
        this.color = new int[] { primary, secondary };
        this.showInList = show;
    }

    public static DrtCombType getComb(int meta) {
        return meta < 0 || meta > VALUES.length ? VALUES[0] : VALUES[meta];
    }
}
