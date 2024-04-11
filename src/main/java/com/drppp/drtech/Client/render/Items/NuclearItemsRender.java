package com.drppp.drtech.Client.render.Items;

import com.drppp.drtech.api.capability.DrtechCapabilities;
import com.drppp.drtech.api.capability.DrtechCommonCapabilities;
import gregtech.api.unification.material.Materials;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NuclearItemsRender {
    // 订阅事件的方法需要在客户端运行
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderItemInFrame(RenderItemInFrameEvent event) {
        ItemStack stack = event.getItem();
        var render = event.getRenderer();
        // 检查物品是否有耐久度（即是否能损坏）
        if (stack.hasCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD,null)) {
            var ca = stack.getCapability(DrtechCommonCapabilities.CAPABILITY_FUEL_ROAD,null);
            // 计算耐久度的百分比
            float durabilityRatio = (float) ((float)ca.getMaxDurability() - (float)ca.getDurability()) / (float)ca.getMaxDurability();
            // 执行绘制耐久度条的代码
            // 你需要在这里添加具体实现绘制的逻辑
            String string = durabilityRatio+"%";
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            int strLenHalved = fontRenderer.getStringWidth(string) / 2;
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); //将画笔置为黑色, 便于进行绘画(这里没有进行绘画)

            GlStateManager.disableLighting();
            GlStateManager.enablePolygonOffset();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend(); //开启混合器(使GL支持Alpha透明通道)
            GlStateManager.doPolygonOffset(-1, -20);

            fontRenderer.drawString(string, -strLenHalved, 0, 0xFFFFFFFF);

            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.disablePolygonOffset();
            GlStateManager.enableLighting();

            GlStateManager.popMatrix();
        }
    }
}
