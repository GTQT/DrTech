package com.drppp.drtech.Tile;

import com.drppp.drtech.Blocks.BlocksInit;
import com.drppp.drtech.Client.Particle.GravitationalAnomalyParticle;
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

public class TileEntityGravitationalAnomaly extends TileEntity implements ITickable {
    public int weight=0;
    public int max_weight=2000;
    public int speed = 1;
    public int tick = 0;
    public TileEntityGravitationalAnomaly()
    {

    }
    public BlockPos getPosition() {
        return this.getPos();
    }

    public void update()
    {
        if (this.world.isRemote)
        {
            this.spawnParticles(this.world);
        }
        weight = Math.min(weight,max_weight);
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.weight = compound.getInteger("weight");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("weight",this.weight);
        return super.writeToNBT(compound);
    }

    @SideOnly(Side.CLIENT)
    public void spawnParticles(World world) {
        double radius = 2d;
        Vector3f point = BlocksInit.randomSpherePoint((double)this.getPos().getX() + 0.5, (double)this.getPos().getY() + 0.5, (double)this.getPos().getZ() + 0.5, new Vec3d(radius, radius, radius), world.rand);
        GravitationalAnomalyParticle particle = new GravitationalAnomalyParticle(world, (double)point.x, (double)point.y, (double)point.z, new Vec3d((double)((float)this.getPos().getX() + 0.5F), (double)((float)this.getPos().getY() + 0.5F), (double)((float)this.getPos().getZ() + 0.5F)));
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }
    public boolean shouldRender() {
        return this.world.getBlockState(this.getPos()).getBlock() == this.getBlockType();
    }
}
