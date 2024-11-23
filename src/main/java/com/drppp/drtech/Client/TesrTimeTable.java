package com.drppp.drtech.Client;

import com.drppp.drtech.Tile.TileEntityTimeTable;
import keqing.gtqtcore.common.items.GTQTMetaItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TesrTimeTable extends TileEntitySpecialRenderer<TileEntityTimeTable> {
    @Override
    public void render(TileEntityTimeTable te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translate(x, y, z);

        // 使用一个更小的浮动动画幅度
        double animationOffset = Math.sin((System.currentTimeMillis() / 500.0) + te.getPos().getX() + te.getPos().getY() + te.getPos().getZ()) * 0.05;

        // 计算物品位置
        GlStateManager.translate(0.5, 1.2 + animationOffset, 0.5);
        GlStateManager.scale(1.6, 1.6, 1.6);

        // 添加旋转效果，旋转速度可以根据需要调整
        float rotationAngle = (System.currentTimeMillis() % 3600L) / 10.0f; // 每100毫秒旋转10度
        GlStateManager.rotate(rotationAngle, 0.0f, 1.0f, 0.0f); // 绕Y轴旋转

        // 创建EntityItem以渲染
        EntityItem items = new EntityItem(te.getWorld(), te.getPos().getX() + 0.5, te.getPos().getY() + 1.2 + animationOffset, te.getPos().getZ() + 0.5, new ItemStack(GTQTMetaItems.TIME_BOTTLE.getMetaItem(), 1, GTQTMetaItems.TIME_BOTTLE.metaValue));
        items.hoverStart = 0;

        // 渲染物品
        Minecraft.getMinecraft().getRenderManager().renderEntity(items, 0, 0, 0, 0, 0, false);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }



}
