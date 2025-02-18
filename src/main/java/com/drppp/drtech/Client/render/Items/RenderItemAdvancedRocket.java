package com.drppp.drtech.Client.render.Items;

import com.drppp.drtech.Client.lib.obj.AdvancedModelLoader;
import com.drppp.drtech.Client.lib.obj.IModelCustom;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderItemAdvancedRocket extends TileEntityItemStackRenderer {
    public static final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(Tags.MODID, "models/shipi.obj"));
    private final   ResourceLocation texture = new ResourceLocation(Tags.MODID, "models/hongbei.png");
    private float rotationAngle = 0.0F;
    private int time=0;
    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        if(stack.getItem()== ItemsInit.ITEM_ROCKET)
        {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.1F, 0.1F, 0.1F); // 调整模型大小
            GlStateManager.translate(5.5F, 1.5F, 0.5F); // 调整模型位置
            if(++time>=5)
            {
                rotationAngle = (rotationAngle + 1.0F) % 360.0F; // 每帧增加 1 度
                time=0;
            }
            GlStateManager.rotate(rotationAngle, 0.0F, 1.0F, 0.0F); // 围绕 Y 轴旋转
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            model.renderAll();

            GlStateManager.popMatrix();
        }
        else
            super.renderByItem( stack,  partialTicks);
    }
}