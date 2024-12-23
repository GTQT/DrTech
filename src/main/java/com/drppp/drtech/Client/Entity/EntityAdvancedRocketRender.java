package com.drppp.drtech.Client.Entity;

import com.drppp.drtech.Client.lib.obj.AdvancedModelLoader;
import com.drppp.drtech.Client.lib.obj.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EntityAdvancedRocketRender extends RenderLiving {
    private IModelCustom model;
    private static final ResourceLocation MODEL = new ResourceLocation("drtech", "models/shipi.obj");
    private static final ResourceLocation TEXTURE =new ResourceLocation("drtech","models/hongbei.png");
    public EntityAdvancedRocketRender(RenderManager rendermanagerIn) {
        super(rendermanagerIn, null, 0.5f);
        this.model =  AdvancedModelLoader.loadModel(MODEL);
    }

    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        this.bindEntityTexture(entity);
        GlStateManager.translate(x, y, z);
       // GlStateManager.translate(0.5, 0.5, 0.5);
        model.renderAll();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.popAttrib();
        GlStateManager.enableCull();
    }
    protected boolean bindEntityTexture(Entity entity)
    {
        ResourceLocation resourcelocation = this.getEntityTexture(entity);

        if (resourcelocation == null)
        {
            return false;
        }
        else
        {
            this.bindTexture(resourcelocation);
            return true;
        }
    }

    public void bindTexture(ResourceLocation location)
    {
        this.renderManager.renderEngine.bindTexture(location);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }

}
