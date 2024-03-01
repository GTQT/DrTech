package com.drppp.drtech.Client.Particle;

import com.drppp.drtech.Utils.DrtechMathUtils;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.util.Vector;

public class ParticleRenderer extends Render<EntityParticleGroup>{
	
	public ParticleRenderer(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityParticleGroup entity, double x, double y, double z, float entityYaw,
			float partialTicks) {
		for(int i=0; i < particleVector.size(); i++)
		{
			XCustomizedParticle particle = particleVector.get(i);
			particle.onRender(partialTicks);
			if(particle.getIsFinish())
			{
				particleVector.remove(i);
			}
		}

		if(entity.ticksExisted % entity.getParticleSpawnPerTick() == 0)
			for(int count = 0; count < entity.getParticleSpawnCount(); count ++)
			{
				
				XCustomizedParticle particle = new XCustomizedParticle(new Vec3d(0.8f, 1.0f, 1.0f),
						new Vec3d(x, y, z), 
						new Vec3d((entity.motionX * DrtechMathUtils.getRandomFromRange(1, -1) + DrtechMathUtils.getRandomFromRange(1, -1)) * Math.random() / 15.0f,
								  (entity.motionY * DrtechMathUtils.getRandomFromRange(1, -1) + DrtechMathUtils.getRandomFromRange(1, -1)) * Math.random()/ 15.0f,
								  (entity.motionZ * DrtechMathUtils.getRandomFromRange(1, -1) + DrtechMathUtils.getRandomFromRange(1, -1)) * Math.random()/ 15.0f),
						new Vec3d(0.1f, 0.1f, 0.1f), new Vec3d(0.0f, 0.0f, 1.0f), 0.0f, DrtechMathUtils.getRandomFromRange(20, 5));
				particleVector.add(particle);
			}

	}

	@Override
	protected ResourceLocation getEntityTexture(EntityParticleGroup entity) {
		return null;
	}
	
	private Vector<XCustomizedParticle> particleVector = new Vector();
}
