package com.drppp.drtech.common.event;

import com.drppp.drtech.api.Utils.Datas;
import com.drppp.drtech.common.Entity.EntityAdvancedRocket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RocketFuelHUD {
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return; // 只在经验条渲染时执行
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        // 检查玩家是否骑乘 EntityAdvancedRocket
        if (player.getRidingEntity() instanceof EntityAdvancedRocket) {
            EntityAdvancedRocket rocket = (EntityAdvancedRocket) player.getRidingEntity();
            int fuelAmount = rocket.getFuelAmount();
            int maxFuel = 1000; // 假设最大燃料量为 100

            // 获取屏幕分辨率
            ScaledResolution resolution = new ScaledResolution(mc);
            int width = resolution.getScaledWidth();
            int height = resolution.getScaledHeight();

            // 绘制背景条
            int barWidth = 100; // 进度条宽度
            int barHeight = 10; // 进度条高度
            int x = (width - barWidth) / 2; // 水平居中
            int y = height - 60; // 距离屏幕底部 30 像素

            Gui.drawRect(x, y, x + barWidth, y + barHeight, 0xFF000000); // 黑色背景

            // 绘制燃料条
            int fuelWidth = (int) ((float) fuelAmount / maxFuel * barWidth);
            Gui.drawRect(x, y, x + fuelWidth, y + barHeight, 0xFF00FF00); // 绿色燃料条

            // 绘制文字
            String text = "燃料: " + fuelAmount + "/" + maxFuel;
            mc.fontRenderer.drawStringWithShadow(text, x, y - 12, 0xFFFFFF); // 白色文字

            // 绘制目标维度文本
            String dimText = "目标维度: " + Datas.DIMENSION_NAMES.getOrDefault(rocket.getDimId(),"未知维度");
            int dimTextY = y - 24; // 在燃料文本上方
            mc.fontRenderer.drawStringWithShadow(dimText, x, dimTextY, 0xFFFFFF);
        }
    }

}