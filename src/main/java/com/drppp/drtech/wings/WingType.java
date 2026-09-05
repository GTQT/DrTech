package com.drppp.drtech.wings;

public enum WingType {
    ANGEL("angel", Shape.AVIAN, 960),
    SLIME("slime", Shape.INSECTOID, 960),
    BLUE_BUTTERFLY("blue_butterfly", Shape.INSECTOID, 960),
    MONARCH_BUTTERFLY("monarch_butterfly", Shape.INSECTOID, 960),
    FIRE("fire", Shape.AVIAN, 1920),
    BAT("bat", Shape.AVIAN, 1920),
    FAIRY("fairy", Shape.INSECTOID, 960),
    EVIL("evil", Shape.AVIAN, 1920),
    DRAGON("dragon", Shape.AVIAN, 2880);

    private final String serializedName;
    private final Shape shape;
    private final int durability;

    WingType(String serializedName, Shape shape, int durability) {
        this.serializedName = serializedName;
        this.shape = shape;
        this.durability = durability;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public Shape getShape() {
        return shape;
    }

    public int getDurability() {
        return durability;
    }

    public enum Shape {
        AVIAN,
        INSECTOID
    }
}
