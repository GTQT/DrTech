package com.drppp.drtech.Client.render.Items;

import com.drppp.drtech.Client.lib.obj.AdvancedModelLoader;
import com.drppp.drtech.Client.lib.obj.IModelCustom;
import com.drppp.drtech.Tags;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderItemStoneAxle extends TileEntityItemStackRenderer {
    public static final IModelCustom waterMill = AdvancedModelLoader.loadModel(new ResourceLocation(Tags.MODID, "models/obj/wood_axle.obj"));
    ResourceLocation texture = new ResourceLocation(Tags.MODID, "models/obj/water_wheel.png");
    private float rotationAngle = 0.0F;
    private int time=0;
    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90,0,1,0);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
        if (++time >= 5) {
            rotationAngle = (rotationAngle + 1.0F) % 360.0F;
            time = 0;
        }
        GlStateManager.rotate(rotationAngle, 0.0F, 0.0F, 1.0F);
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
        waterMill.renderAll();
        GlStateManager.popMatrix();
    }
}