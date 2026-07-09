package com.drppp.drtech.hooked;

public final class HookConfig {
    public static final int SEARCH_BAUBLES = 1;
    public static final int SEARCH_HANDS = 2;
    public static final int SEARCH_HOTBAR = 4;
    public static final int SEARCH_INVENTORY = 8;

    public static int searchLocations = SEARCH_BAUBLES;

    private HookConfig() {
    }
}
