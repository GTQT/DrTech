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
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderItemWaterMill extends TileEntityItemStackRenderer {
    public static final IModelCustom waterMill = AdvancedModelLoader.loadModel(new ResourceLocation(Tags.MODID, "models/obj/water_wheel.obj"));
    ResourceLocation texture = new ResourceLocation(Tags.MODID, "models/obj/water_wheel.png");
    private float rotationAngle = 0.0F;
    private int time=0;
    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GlStateManager.pushMatrix();
        // 将模型的原点移动到渲染中心
        GlStateManager.translate(0.5F, 0.5F, 0.5F); // 假设模型的原点在几何中心
        // 围绕 X 轴旋转 90 度（如果需要）
        GlStateManager.rotate(90, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        // 更新旋转角度
        if (++time >= 5) {
            rotationAngle = (rotationAngle + 1.0F) % 360.0F; // 每帧增加 1 度
            time = 0;
        }
        // 围绕 Y 轴旋转
        GlStateManager.rotate(rotationAngle, 0.0F, 1.0F, 0.0F);
        // 将模型的原点移回初始位置
        GlStateManager.translate(-0.5F, -0.5F, -0.5F); // 恢复模型的原点
        // 绑定纹理并渲染模型
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
        waterMill.renderAll();
        GlStateManager.popMatrix();
    }
}