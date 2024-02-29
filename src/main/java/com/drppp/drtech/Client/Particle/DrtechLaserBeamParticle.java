package com.drppp.drtech.Client.Particle;

import codechicken.lib.vec.Vector3;
import com.drppp.drtech.Tags;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.particle.GTLaserBeamParticle;
import gregtech.client.utils.EffectRenderContext;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrtechLaserBeamParticle extends GTLaserBeamParticle {
    private int particleAge = 0;
    private final int particleMaxAge;
    public static final ResourceLocation body = new ResourceLocation(Tags.MODID,"textures/laser/laser.png");
    public static final ResourceLocation head = new ResourceLocation(Tags.MODID,"textures/laser/laser_start.png");

    public DrtechLaserBeamParticle(MetaTileEntity mte, Vector3 startPos, Vector3 endPos, int maxAge) {
        super(mte, startPos, endPos);
        this.particleMaxAge = maxAge;
        this.setBody(body);
        this.setHead(head);
        this.setEmit(1.0F);
    }
    public DrtechLaserBeamParticle(MetaTileEntity mte, BlockPos startPos, BlockPos endPos, int maxAge) {

        super(mte, new Vector3(startPos.getX(),startPos.getY(),startPos.getZ()),new Vector3(endPos.getX(),endPos.getY(),endPos.getZ()));
        this.particleMaxAge = maxAge;
        this.setBody(body);
        this.setHead(head);
        this.setEmit(1.0F);
    }
    public float getAlpha() {
        return (float)this.particleAge / (float)this.particleMaxAge;
    }

    public void onUpdate() {
        super.onUpdate();
        if (this.particleAge != this.particleMaxAge) {
            this.setAlpha(1.0F - (float)this.particleAge / (float)this.particleMaxAge);
            ++this.particleAge;
        } else {
            this.setExpired();
        }

    }

    public void renderParticle(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {
        this.setAlpha(1.0F - (float)this.particleAge / (float)this.particleMaxAge);
        super.renderParticle(buffer, context);
    }
}
