package com.drppp.drtech.lootgames.api.util;

public class Pos2i {
    private int x;
    private int y;

    public Pos2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Pos2i add(int x, int y) {
        return new Pos2i(this.x + x, this.y + y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos2i pos2i = (Pos2i) o;
        return x == pos2i.x && y == pos2i.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "Pos2i{" + "x=" + x + ", y=" + y + '}';
    }
}
