package com.drppp.drtech.common.drone.program.model;

/** Shared world-overlay style constants for coordinate, area, fleet, and logistics markers. */
public final class DroneWorldMarkerStyle {
    public static final int MARKER_LINE_WIDTH = 2;
    public static final int ROUTE_LINE_WIDTH = 1;
    public static final int MARKER_HEIGHT = 2;
    public static final int MAX_LINKS_PER_VIEW = 128;
    public static final int COLOR_COORDINATE = 0x44CCFF;
    public static final int COLOR_AREA = 0x66DD88;
    public static final int COLOR_ROUTE = 0xFFCC44;
    private DroneWorldMarkerStyle() { }
    public static int clampLinks(int requested) { return Math.max(0, Math.min(MAX_LINKS_PER_VIEW, requested)); }
}
