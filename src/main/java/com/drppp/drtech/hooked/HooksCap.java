package com.drppp.drtech.hooked;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class HooksCap {
    public static final double VERTICAL_SPEED = 0.25;

    public final List<HookInfo> hooks = new ArrayList<>();
    public HookType hookType;
    private Vec3d centerPos;
    public double verticalHangDistance;

    public void update(Entity player) {
        if (!player.world.isRemote) {
            HookNetwork.syncHooks(player);
        }
    }

    @Nullable
    public Vec3d getCenterPos() {
        return centerPos;
    }

    public void setCenterPos(@Nullable Vec3d centerPos) {
        this.centerPos = HookMath.finite(centerPos) ? centerPos : null;
    }

    public void updatePos() {
        List<HookInfo> planted = new ArrayList<>();
        for (HookInfo hook : hooks) {
            if (hook.status == EnumHookStatus.PLANTED) {
                planted.add(hook);
            }
        }
        if (planted.isEmpty()) {
            centerPos = null;
        } else if (planted.size() == 1) {
            centerPos = HookMath.subtract(planted.get(0).pos, HookMath.vec(0, verticalHangDistance, 0));
        } else {
            Vec3d sum = Vec3d.ZERO;
            double weightSum = 0.0;
            for (HookInfo hook : planted) {
                sum = HookMath.add(sum, HookMath.scale(hook.pos, hook.getWeight()));
                weightSum += hook.getWeight();
            }
            centerPos = HookMath.scale(sum, 1.0 / weightSum);
        }
    }

    public void updateRedMovement(EntityPlayer player) {
        if (countPlanted() != 1) {
            verticalHangDistance = 0.0;
        }
        if (!player.world.isRemote) {
            return;
        }
        if (player != Minecraft.getMinecraft().player) {
            return;
        }
        double movementY = 0.0;
        if (player.isSneaking()) {
            movementY -= VERTICAL_SPEED;
        }
        if (Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown()) {
            movementY += VERTICAL_SPEED;
        }
        float strafe = countPlanted() == 1 ? 0f : player.moveStrafing;
        float forward = countPlanted() == 1 ? 0f : player.moveForward;
        if (player.isSneaking()) {
            strafe /= 0.3f;
            forward /= 0.3f;
        }
        Vec2f horizontal = moveRelative(player, strafe, forward, 0.25f);
        Vec3d offset = new Vec3d(horizontal.x, movementY, horizontal.y);
        offset = collideOffset(player, offset);
        if (offset.lengthSquared() > 0.001) {
            setCenterPos(HookMath.add(HookTickHandler.getWaistPos(player), offset));
            correctPos();
        }
    }

    private Vec3d collideOffset(EntityPlayer player, Vec3d offset) {
        AxisAlignedBB box = player.getEntityBoundingBox();
        List<AxisAlignedBB> collisions = player.world.getCollisionBoxes(player, box.expand(offset.x, offset.y, offset.z));
        double x = offset.x;
        double y = offset.y;
        double z = offset.z;

        if (y != 0.0D) {
            for (AxisAlignedBB collision : collisions) {
                y = collision.calculateYOffset(box, y);
            }
            box = box.offset(0.0D, y, 0.0D);
        }
        if (x != 0.0D) {
            for (AxisAlignedBB collision : collisions) {
                x = collision.calculateXOffset(box, x);
            }
            if (x != 0.0D) {
                box = box.offset(x, 0.0D, 0.0D);
            }
        }
        if (z != 0.0D) {
            for (AxisAlignedBB collision : collisions) {
                z = collision.calculateZOffset(box, z);
            }
        }
        return new Vec3d(x, y, z);
    }

    public Vec3d limitToHookLength(Vec3d pos, List<HookInfo> planted) {
        Vec3d newPos = pos;
        double clampRange = HookType.RED.range - 1.0 / 16.0;
        for (HookInfo hook : planted) {
            Vec3d relativePos = HookMath.subtract(pos, hook.pos);
            double length = relativePos.length();
            if (length > clampRange) {
                newPos = HookMath.add(hook.pos, HookMath.scale(relativePos, clampRange / length));
            }
        }
        return newPos;
    }

    public void correctPos() {
        if (centerPos == null) {
            return;
        }
        List<HookInfo> planted = getPlantedHooks();
        switch (planted.size()) {
            case 1: {
                HookInfo hook0 = planted.get(0);
                Vec3d clamped = limitToHookLength(centerPos, planted);
                verticalHangDistance = hook0.pos.y - Math.min(hook0.pos.y, clamped.y);
                break;
            }
            case 2: {
                HookInfo hook0 = planted.get(0);
                HookInfo hook1 = planted.get(1);
                Vec3d closest = limitToHookLength(closestPointOnLine(centerPos, hook0.pos, hook1.pos).point, planted);
                hook1.setWeight(HookMath.subtract(closest, hook0.pos).length() / HookMath.subtract(hook1.pos, hook0.pos).length());
                hook0.setWeight(1 - hook1.getWeight());
                hook0.setWeight(hook0.getWeight() * 2);
                hook1.setWeight(hook1.getWeight() * 2);
                break;
            }
            case 3: {
                HookInfo hook0 = planted.get(0);
                HookInfo hook1 = planted.get(1);
                HookInfo hook2 = planted.get(2);
                Vec3d projected = projectToTri(centerPos, hook0.pos, hook1.pos, hook2.pos);
                Vec3d closest = limitToHookLength(closestPointOnTriangle(projected, hook0.pos, hook1.pos, hook2.pos).point, planted);
                Vec3d bary = HookMath.barycentric(closest, hook0.pos, hook1.pos, hook2.pos);
                hook0.setWeight(bary.x * 3);
                hook1.setWeight(bary.y * 3);
                hook2.setWeight(bary.z * 3);
                break;
            }
            case 4: {
                HookInfo hook0 = planted.get(0);
                HookInfo hook1 = planted.get(1);
                HookInfo hook2 = planted.get(2);
                HookInfo hook3 = planted.get(3);
                Vec3d closest = limitToHookLength(
                    closestPointInTetrahedron(centerPos, hook0.pos, hook1.pos, hook2.pos, hook3.pos).point,
                    planted
                );
                HookMath.Vec4d bary = HookMath.barycentric(closest, hook0.pos, hook1.pos, hook2.pos, hook3.pos);
                hook0.setWeight(bary.x * 4);
                hook1.setWeight(bary.y * 4);
                hook2.setWeight(bary.z * 4);
                hook3.setWeight(bary.w * 4);
                break;
            }
            default:
                break;
        }
        updatePos();
        HookNetwork.sendWeights(this);
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList hooksTag = new NBTTagList();
        for (HookInfo hook : hooks) {
            hooksTag.appendTag(hook.serialize());
        }
        tag.setTag("hooks", hooksTag);
        if (hookType != null) {
            tag.setInteger("hookType", hookType.ordinal());
        }
        if (centerPos != null) {
            NBTTagCompound centerTag = new NBTTagCompound();
            centerTag.setDouble("x", centerPos.x);
            centerTag.setDouble("y", centerPos.y);
            centerTag.setDouble("z", centerPos.z);
            tag.setTag("center", centerTag);
        }
        tag.setDouble("verticalHangDistance", verticalHangDistance);
        return tag;
    }

    public void deserializeNBT(NBTTagCompound tag) {
        hooks.clear();
        NBTTagList list = tag.getTagList("hooks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            hooks.add(HookInfo.deserialize(list.getCompoundTagAt(i)));
        }
        if (tag.hasKey("hookType", Constants.NBT.TAG_INT)) {
            int hookTypeIndex = Math.max(0, Math.min(tag.getInteger("hookType"), HookType.values().length - 1));
            hookType = HookType.values()[hookTypeIndex];
        } else {
            hookType = null;
        }
        if (tag.hasKey("center", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound center = tag.getCompoundTag("center");
            setCenterPos(new Vec3d(center.getDouble("x"), center.getDouble("y"), center.getDouble("z")));
        } else {
            setCenterPos(null);
        }
        verticalHangDistance = tag.getDouble("verticalHangDistance");
    }

    public static Vec2f moveRelative(Entity entity, float strafe, float forward, float friction) {
        float f = strafe * strafe + forward * forward;
        if (f >= 1.0E-4f) {
            f = MathHelper.sqrt(f);
            if (f < 1.0f) {
                f = 1.0f;
            }
            f = friction / f;
            float yawSin = MathHelper.sin(entity.rotationYaw * 0.017453292f) * f;
            float yawCos = MathHelper.cos(entity.rotationYaw * 0.017453292f) * f;
            return new Vec2f(strafe * yawCos - forward * yawSin, forward * yawCos + strafe * yawSin);
        }
        return new Vec2f(0f, 0f);
    }

    private int countPlanted() {
        int count = 0;
        for (HookInfo hook : hooks) {
            if (hook.status == EnumHookStatus.PLANTED) {
                count++;
            }
        }
        return count;
    }

    private List<HookInfo> getPlantedHooks() {
        List<HookInfo> planted = new ArrayList<>();
        for (HookInfo hook : hooks) {
            if (hook.status == EnumHookStatus.PLANTED) {
                planted.add(hook);
            }
        }
        return planted;
    }

    private DistancePoint closestPointInTetrahedron(Vec3d p, Vec3d a, Vec3d b, Vec3d c, Vec3d d) {
        if (insideTetrahedron(p, a, b, c, d)) {
            return new DistancePoint(0.0, p);
        }
        DistancePoint best = new DistancePoint(Double.POSITIVE_INFINITY, p);
        best = min(best, new DistancePoint(HookMath.subtract(a, p).lengthSquared(), a));
        best = min(best, new DistancePoint(HookMath.subtract(b, p).lengthSquared(), b));
        best = min(best, new DistancePoint(HookMath.subtract(c, p).lengthSquared(), c));
        best = min(best, new DistancePoint(HookMath.subtract(d, p).lengthSquared(), d));
        best = min(best, closestPointOnLineRay(p, a, b));
        best = min(best, closestPointOnLineRay(p, a, c));
        best = min(best, closestPointOnLineRay(p, a, d));
        best = min(best, closestPointOnLineRay(p, b, c));
        best = min(best, closestPointOnLineRay(p, b, d));
        best = min(best, closestPointOnLineRay(p, c, d));
        best = min(best, closestPointOnTrianglePlane(p, a, b, c));
        best = min(best, closestPointOnTrianglePlane(p, a, b, d));
        best = min(best, closestPointOnTrianglePlane(p, a, c, d));
        best = min(best, closestPointOnTrianglePlane(p, b, c, d));
        return best;
    }

    private DistancePoint closestPointOnTriangle(Vec3d p, Vec3d a, Vec3d b, Vec3d c) {
        if (insideTri(p, a, b, c)) {
            return new DistancePoint(0.0, p);
        }
        DistancePoint best = new DistancePoint(Double.POSITIVE_INFINITY, p);
        best = min(best, new DistancePoint(HookMath.subtract(a, p).lengthSquared(), a));
        best = min(best, new DistancePoint(HookMath.subtract(b, p).lengthSquared(), b));
        best = min(best, new DistancePoint(HookMath.subtract(c, p).lengthSquared(), c));
        best = min(best, closestPointOnLineRay(p, a, b));
        best = min(best, closestPointOnLineRay(p, b, c));
        best = min(best, closestPointOnLineRay(p, c, a));
        return best;
    }

    private DistancePoint closestPointOnLine(Vec3d p, Vec3d a, Vec3d b) {
        DistancePoint best = new DistancePoint(Double.POSITIVE_INFINITY, p);
        best = min(best, new DistancePoint(HookMath.subtract(a, p).lengthSquared(), a));
        best = min(best, new DistancePoint(HookMath.subtract(b, p).lengthSquared(), b));
        best = min(best, closestPointOnLineRay(p, a, b));
        return best;
    }

    private DistancePoint closestPointOnLineRay(Vec3d p, Vec3d a, Vec3d b) {
        Vec3d line = HookMath.subtract(b, a);
        double len = line.length();
        line = HookMath.scale(line, 1.0 / len);
        Vec3d v = HookMath.subtract(p, a);
        double d = HookMath.dot(v, line);
        Vec3d point = HookMath.add(a, HookMath.scale(line, d));
        if (d < 0.0 || d > len) {
            return new DistancePoint(Double.POSITIVE_INFINITY, point);
        }
        return new DistancePoint(HookMath.subtract(p, point).lengthSquared(), point);
    }

    private DistancePoint closestPointOnTrianglePlane(Vec3d p, Vec3d a, Vec3d b, Vec3d c) {
        Vec3d projected = projectToTri(p, a, b, c);
        if (insideTri(projected, a, b, c)) {
            return new DistancePoint(HookMath.subtract(p, projected).lengthSquared(), projected);
        }
        return new DistancePoint(Double.POSITIVE_INFINITY, projected);
    }

    private Vec3d projectToTri(Vec3d p, Vec3d a, Vec3d b, Vec3d c) {
        Vec3d normal = triangleNormal(a, b, c);
        double d = HookMath.dot(HookMath.subtract(p, a), normal);
        return HookMath.subtract(p, HookMath.scale(normal, d));
    }

    private boolean insideTri(Vec3d p, Vec3d a, Vec3d b, Vec3d c) {
        return sameSide(p, a, b, c) && sameSide(p, b, a, c) && sameSide(p, c, a, b);
    }

    private boolean sameSide(Vec3d p1, Vec3d p2, Vec3d a, Vec3d b) {
        Vec3d cp1 = HookMath.cross(HookMath.subtract(b, a), HookMath.subtract(p1, a));
        Vec3d cp2 = HookMath.cross(HookMath.subtract(b, a), HookMath.subtract(p2, a));
        return HookMath.dot(cp1, cp2) >= 0;
    }

    private boolean insideTetrahedron(Vec3d p, Vec3d a, Vec3d b, Vec3d c, Vec3d d) {
        return sameSide(a, b, c, d, p)
            && sameSide(b, c, d, a, p)
            && sameSide(c, d, a, b, p)
            && sameSide(d, a, b, c, p);
    }

    private boolean sameSide(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, Vec3d p) {
        Vec3d normal = HookMath.cross(HookMath.subtract(v2, v1), HookMath.subtract(v3, v1));
        double dotV4 = HookMath.dot(normal, HookMath.subtract(v4, v1));
        double dotP = HookMath.dot(normal, HookMath.subtract(p, v1));
        return (dotV4 < 0 && dotP < 0) || (dotV4 > 0 && dotP > 0);
    }

    private Vec3d triangleNormal(Vec3d a, Vec3d b, Vec3d c) {
        return HookMath.cross(HookMath.subtract(a, b), HookMath.subtract(b, c)).normalize();
    }

    private DistancePoint min(DistancePoint left, DistancePoint right) {
        return left.distance <= right.distance ? left : right;
    }

    private static final class DistancePoint {
        private final double distance;
        private final Vec3d point;

        private DistancePoint(double distance, Vec3d point) {
            this.distance = distance;
            this.point = point;
        }
    }
}
