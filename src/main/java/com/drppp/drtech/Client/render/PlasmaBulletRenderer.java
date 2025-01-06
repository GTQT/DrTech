package com.drppp.drtech.Client.render;


import com.drppp.drtech.Client.Particle.XCustomizedParticle;
import com.drppp.drtech.api.Utils.DrtechMathUtils;
import com.drppp.drtech.common.Entity.EntityPlasmaBullet;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.Vector;

public class PlasmaBulletRenderer extends Render<EntityPlasmaBullet> {
    private final Vector<XCustomizedParticle> particleVector = new Vector();

    public PlasmaBulletRenderer(RenderManager manager) {
        super(manager);
    }

    public void doRender(EntityPlasmaBullet entity, double x, double y, double z, float entityYaw, float partialTicks) {
        for (int i = 0; i < particleVector.size(); i++) {
            XCustomizedParticle particle = particleVector.get(i);
            particle.onRender(partialTicks);
            if (particle.getIsFinish()) {
                particleVector.remove(i);
            }
        }


        for (int count = 0; count < 24; count++) {
            XCustomizedParticle particle = new XCustomizedParticle(new Vec3d(0.8f, 1.0f, 1.0f), new Vec3d(x, y, z),
                    new Vec3d(getNewMotion(entity.motionX),
                            getNewMotion(entity.motionY),
                            getNewMotion(entity.motionZ)),
                    new Vec3d(0.1f, 0.1f, 0.1f), new Vec3d(0.0f, 0.0f, 1.0f), 0.0f,
                    DrtechMathUtils.getRandomFromRange(20, 5));
            particleVector.add(particle);
        }
    }

    private double getNewMotion(double motion) {
        double value = motion;
        if (new Random().nextInt() % 2 == 0) {
            value *= DrtechMathUtils.getRandomFromRange(0.1, 0.01);
            value /= 12.0f;
        } else {
            value *= DrtechMathUtils.getRandomFromRange(-0.01, -0.1);
            value /= 12.0f;
        }
        return value;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPlasmaBullet entity) {
        return null;
    }

}
