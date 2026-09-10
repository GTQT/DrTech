package com.drppp.drtech.common.tile;

import com.drppp.drtech.common.blocks.BlocksInit;
import com.drppp.drtech.client.Particle.GravitationalAnomalyParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.util.Random;

public class TileEntityGravitationalAnomaly extends TileEntity implements ITickable {

    private static final String NBT_WEIGHT = "weight";
    private static final double PARTICLE_RADIUS = 2.0D;

    public int weight = 0;
    public int max_weight = 2000;
    public int speed = 1;
    public int tick = 0;

    public BlockPos getPosition() {
        return getPos();
    }

    @Override
    public void update() {
        if (world.isRemote) {
            spawnParticles(world);
            return;
        }
        // Clamp accumulated weight, only persist when it actually changed.
        if (weight > max_weight) {
            weight = max_weight;
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.weight = compound.getInteger(NBT_WEIGHT);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger(NBT_WEIGHT, this.weight);
        return super.writeToNBT(compound);
    }

    @SideOnly(Side.CLIENT)
    public void spawnParticles(World world) {
        double centerX = getPos().getX() + 0.5D;
        double centerY = getPos().getY() + 0.5D;
        double centerZ = getPos().getZ() + 0.5D;

        Vec3d radius = new Vec3d(PARTICLE_RADIUS, PARTICLE_RADIUS, PARTICLE_RADIUS);
        Vector3f point = randomSpherePoint(centerX, centerY, centerZ, radius, world.rand);

        GravitationalAnomalyParticle particle = new GravitationalAnomalyParticle(
                world, point.x, point.y, point.z, new Vec3d(centerX, centerY, centerZ));
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    public boolean shouldRender() {
        return world != null && world.getBlockState(getPos()).getBlock() == getBlockType();
    }

    public static Vector3f randomSpherePoint(double x0, double y0, double z0, Vec3d radius, Random rand) {
        double u = rand.nextDouble();
        double v = rand.nextDouble();
        double theta = 6.283185307179586 * u;
        double phi = Math.acos(2.0 * v - 1.0);
        double x = x0 + radius.x * Math.sin(phi) * Math.cos(theta);
        double y = y0 + radius.y * Math.sin(phi) * Math.sin(theta);
        double z = z0 + radius.z * Math.cos(phi);
        return new Vector3f((float) x, (float) y, (float) z);
    }
}