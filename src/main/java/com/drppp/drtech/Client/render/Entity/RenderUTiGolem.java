package com.drppp.drtech.Client.render.Entity;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Entity.moster.EntityUTiGolem;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class RenderUTiGolem extends RenderLiving<EntityUTiGolem>
{
    public static final ResourceLocation TUI_GOLEM_TEXTURES = new ResourceLocation(Tags.MODID,"textures/entity/uti_golem.png");

    public RenderUTiGolem(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelUTIGolem(), 0.5F);
        this.addLayer(new LayerUTIGolemFlower(this));
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityUTiGolem entity)
    {
        return TUI_GOLEM_TEXTURES;
    }

    protected void applyRotations(EntityUTiGolem entityLiving, float ageInTicks, float rotationYaw, float partialTicks)
    {
        super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);

        if ((double)entityLiving.limbSwingAmount >= 0.01D)
        {
            float f = 13.0F;
            float f1 = entityLiving.limbSwing - entityLiving.limbSwingAmount * (1.0F - partialTicks) + 6.0F;
            float f2 = (Math.abs(f1 % 13.0F - 6.5F) - 3.25F) / 3.25F;
            GlStateManager.rotate(6.5F * f2, 0.0F, 0.0F, 1.0F);
        }
    }


    @Override
    protected void preRenderCallback(EntityUTiGolem entitylivingbaseIn, float partialTickTime) {
        super.preRenderCallback(entitylivingbaseIn, partialTickTime);
        GlStateManager.scale(1.5F, 1.5F, 1.5F);
    }


}