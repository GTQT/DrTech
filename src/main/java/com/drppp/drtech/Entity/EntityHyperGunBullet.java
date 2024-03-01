package com.drppp.drtech.Entity;

import com.drppp.drtech.Client.Particle.EntityParticleSpray;
import com.drppp.drtech.Utils.DrtechMathUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class EntityHyperGunBullet extends EntityGunBullet {
    public EntityHyperGunBullet(World world)
    {
        super(world);
        this.power = 20.0f;
    }

    public EntityHyperGunBullet(World world, EntityPlayer owner, float power, int maxTick) {
        super(world,owner, power, maxTick);

    }

    @Override
    protected Entity findEntityOnPath(Vec3d start, Vec3d end)
    {
        Entity entity = null;
        List<Entity> list = getEntityWorld().getEntitiesInAABBexcluding(this,
                this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D),
                EntityGunBullet.GUN_TARGETS);
        double d0 = 0.0D;

        for (int i = 0; i < list.size(); ++i) {
            Entity entity1 = list.get(i);

            if (shooter != null && shooter == entity1) {
                continue;
            }

            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.3D);
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(start, end);

            if (raytraceresult != null) {
                double d1 = start.squareDistanceTo(raytraceresult.hitVec);

                if (d1 < d0 || d0 == 0.0D) {
                    entity = entity1;
                    d0 = d1;
                }
            }
        }

        return entity;
    }

    @Override
    protected void sprayEffect() {
        float initYaw =  this.rotationYaw;
        float initPitch = this.rotationPitch;
        for(int i=0; i < 3; i++)
        {
            initYaw += DrtechMathUtils.getRandomFromRange(360, 0);
            initPitch += DrtechMathUtils.getRandomFromRange(360, 0);
            EntityParticleSpray particleSpray = new EntityParticleSpray(world, this, new Vec3d(1.0f, 0.6f, 0.6f), 800, 6, true);
            particleSpray.shoot(initYaw, initPitch, 0.8f);
            particleSpray.setScaleSize(new Vec3d(0.08d, 0.08d, 0.08d));
            world.spawnEntity(particleSpray);
        }
    }

    @Override
    protected void shoot(double x, double y, double z, float velocity)
    {
        float f = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / (double)f;
        y = y / (double)f;
        z = z / (double)f;
        x = x * (double)velocity;
        y = y * (double)velocity;
        z = z * (double)velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        float f1 = MathHelper.sqrt(x * x + z * z);
        this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }
}