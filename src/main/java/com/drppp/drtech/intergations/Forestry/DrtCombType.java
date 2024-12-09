package com.drppp.drtech.intergations.Forestry;


public enum DrtCombType {

    // 内容颜色 边框颜色
    BORAX("borax", 0x178c29, 0xbab8fe),
    ETHER("ether", 0x117c29, 0xba18f8),
    BRIGHT("bright", 0x7A007A, 0xFFFFFF),
    WITHER("wither", 0x040102, 0x144F5B),
    MUTAGENIC_AGENT("mutagenic_agent", 0xa39f00, 0x1fff5b),
    PRIMITIVE_STRAIN_A("primitive_strain_a", 0xB106B1, 0xffffff),
    PRIMITIVE_STRAIN_B("primitive_strain_b", 0x9B2B2B, 0xffffff),
    PRIMITIVE_STRAIN_C("primitive_strain_c", 0x85294C, 0xffffff),
    PRIMITIVE_STRAIN_D("primitive_strain_d", 0x89B289, 0xffffff),
    PRIMITIVE_STRAIN_E("primitive_strain_e", 0x8F8A54, 0xffffff),
    COMMON_MINE_STRAIN("common_mine_strain", 0x04B5BD, 0xffffff),
    DIRECTED_PLATINUM_STRAIN("directed_platinum_strain", 0x43BE7A, 0xffffff),
    UNIVERSAL_DEMON_STRAIN("universal_demon_strain", 0x546D20, 0xffffff),
    UNIVERSAL_BYPRODUCT_STRAINS("universal_byproduct_strains", 0x5AB007, 0xffffff),
    INDUSTRIAL_SYNTHETIC_STRAINS("industrial_synthetic_strains", 0x7A4127, 0xffffff),
    INDUSTRIAL_REDUCTION_CULTURE("industrial_reduction_culture", 0x742A9A, 0xffffff),
    INDUSTRIAL_OXIDIZING_BACTERIA("industrial_oxidizing_bacteria", 0x6E8E6E, 0xffffff),
    INDUSTRIAL_CATALYTIC_STRAINS("industrial_catalytic_strains", 0x6B6060, 0xffffff),
    DIRECTED_LANTHANIDE_STRAINS("directed_lanthanide_strains", 0x41BA77, 0xffffff),
    FUEL("fuel", 0xDBA800, 0x9C6F40),
    HIGH_CETANE_DIESEL("high_cetane_diesel", 0xB5C806, 0x9C6F40),
    GASOLINE("gasoline", 0xBE4E07,0xBD7F06) ,
    ETHYLENE("ethylene", 0x9AA4A5, 0x9AA4A5),
    TETRAFLUOROETHYLENE("tetrafluoroethylene", 0x585858, 0x585858),
    CRYOLITE("cryolite", 0x6ac6d4, 0xaedfe8);

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
