package com.drppp.drtech.Client.render.Entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class ModelHappyGhast extends ModelBase {
    private final ModelRenderer body;
    private final ModelRenderer[] tentacles = new ModelRenderer[9];

    public ModelHappyGhast() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
        this.body.rotationPointY = 8.0F;

        Random random = new Random(1660L);

        for (int i = 0; i < this.tentacles.length; ++i) {
            this.tentacles[i] = new ModelRenderer(this, 0, 0);
            float x = (((float) (i % 3) - (float) (i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
            float z = ((float) (i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
            int length = random.nextInt(7) + 8;
            this.tentacles[i].addBox(-1.0F, 0.0F, -1.0F, 2, length, 2);
            this.tentacles[i].rotationPointX = x;
            this.tentacles[i].rotationPointZ = z;
            this.tentacles[i].rotationPointY = 15.0F;
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.6F, 0.0F);
        this.body.render(scale);

        for (ModelRenderer tentacle : this.tentacles) {
            tentacle.render(scale);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        for (int i = 0; i < this.tentacles.length; ++i) {
            this.tentacles[i].rotateAngleX = 0.2F * MathHelper.sin(ageInTicks * 0.3F + i) + 0.4F;
        }
    }
}
