package com.drppp.drtech.common.drone.program.model;

/** Stable minimap frame and viewport metrics shared by editor renderers. */
public final class DroneMinimapStyle {
    public static final int BORDER_WIDTH = 1;
    public static final int PADDING = 3;
    public static final int MIN_WIDTH = 96;
    public static final int MIN_HEIGHT = 64;
    private DroneMinimapStyle() { }

    public static Viewport viewport(int x, int y, int width, int height) {
        return new Viewport(x, y, Math.max(MIN_WIDTH, width), Math.max(MIN_HEIGHT, height));
    }

    public static final class Viewport {
        public final int x, y, width, height;
        private Viewport(int x, int y, int width, int height) { this.x = x; this.y = y; this.width = width; this.height = height; }
        public int contentX() { return x + BORDER_WIDTH + PADDING; }
        public int contentY() { return y + BORDER_WIDTH + PADDING; }
        public int contentWidth() { return Math.max(1, width - 2 * (BORDER_WIDTH + PADDING)); }
        public int contentHeight() { return Math.max(1, height - 2 * (BORDER_WIDTH + PADDING)); }
    }
}
