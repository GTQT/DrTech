package com.drppp.drtech.hooked;

import net.minecraft.util.math.Vec3d;

public final class HookMath {
    private HookMath() {
    }

    public static Vec3d add(Vec3d a, Vec3d b) {
        return a.add(b);
    }

    public static Vec3d subtract(Vec3d a, Vec3d b) {
        return a.subtract(b);
    }

    public static Vec3d scale(Vec3d vec, double scale) {
        return vec.scale(scale);
    }

    public static double dot(Vec3d a, Vec3d b) {
        return a.dotProduct(b);
    }

    public static Vec3d cross(Vec3d a, Vec3d b) {
        return a.crossProduct(b);
    }

    public static Vec3d vec(Number x, Number y, Number z) {
        return new Vec3d(x.doubleValue(), y.doubleValue(), z.doubleValue());
    }

    public static boolean finite(Vec3d vec) {
        return vec != null && Double.isFinite(vec.x) && Double.isFinite(vec.y) && Double.isFinite(vec.z);
    }

    public static Vec4d barycentric(Vec3d p, Vec3d a, Vec3d b, Vec3d c, Vec3d d) {
        Vec3d vap = subtract(p, a);
        Vec3d vbp = subtract(p, b);
        Vec3d vab = subtract(b, a);
        Vec3d vac = subtract(c, a);
        Vec3d vad = subtract(d, a);
        Vec3d vbc = subtract(c, b);
        Vec3d vbd = subtract(d, b);

        double va6 = scalarTripleProduct(vbp, vbd, vbc);
        double vb6 = scalarTripleProduct(vap, vac, vad);
        double vc6 = scalarTripleProduct(vap, vad, vab);
        double vd6 = scalarTripleProduct(vap, vab, vac);
        double v6 = 1.0 / scalarTripleProduct(vab, vac, vad);
        return new Vec4d(va6 * v6, vb6 * v6, vc6 * v6, vd6 * v6);
    }

    public static Vec3d barycentric(Vec3d p, Vec3d a, Vec3d b, Vec3d c) {
        Vec3d v0 = subtract(b, a);
        Vec3d v1 = subtract(c, a);
        Vec3d v2 = subtract(p, a);

        double d00 = dot(v0, v0);
        double d01 = dot(v0, v1);
        double d11 = dot(v1, v1);
        double d20 = dot(v2, v0);
        double d21 = dot(v2, v1);
        double denom = d00 * d11 - d01 * d01;
        double v = (d11 * d20 - d01 * d21) / denom;
        double w = (d00 * d21 - d01 * d20) / denom;
        double u = 1.0 - v - w;
        return new Vec3d(u, v, w);
    }

    private static double scalarTripleProduct(Vec3d a, Vec3d b, Vec3d c) {
        return dot(a, cross(b, c));
    }

    public static final class Vec4d {
        public final double x;
        public final double y;
        public final double z;
        public final double w;

        public Vec4d(double x, double y, double z, double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }
}
