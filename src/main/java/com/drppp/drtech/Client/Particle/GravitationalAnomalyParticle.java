package com.drppp.drtech.Client.Particle;


import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GravitationalAnomalyParticle extends Particle {
    float smokeParticleScale;
    Vec3d center;

    public GravitationalAnomalyParticle(World world, double x, double y, double z, Vec3d center) {
        this(world, x, y, z, center, 1.0F);
    }

    public GravitationalAnomalyParticle(World world, double x, double y, double z, Vec3d center, float f) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.particleRed = this.particleGreen = this.particleBlue = (float)(Math.random() * 0.30000001192092896);
        this.particleScale *= 0.75F;
        this.particleScale *= f;
        this.smokeParticleScale = this.particleScale;
        this.particleMaxAge = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.particleMaxAge = (int)((float)this.particleMaxAge * f);
        this.center = center;
    }

    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f6 = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
        if (f6 < 0.0F) {
            f6 = 0.0F;
        }

        if (f6 > 1.0F) {
            f6 = 1.0F;
        }

        this.particleScale = this.smokeParticleScale * f6;
        super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX = (this.center.x - this.posX) * 0.1;
        this.motionY = (this.center.y - this.posY) * 0.1;
        this.motionZ = (this.center.z - this.posZ) * 0.1;
    }
}
