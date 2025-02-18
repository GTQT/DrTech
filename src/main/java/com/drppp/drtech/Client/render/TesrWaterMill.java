package com.drppp.drtech.Client.render;


import com.drppp.drtech.Client.lib.obj.AdvancedModelLoader;
import com.drppp.drtech.Client.lib.obj.IModelCustom;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityWaterMill;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class TesrWaterMill extends TileEntitySpecialRenderer<TileEntityWaterMill> {
    public static final IModelCustom waterMill = AdvancedModelLoader.loadModel(new ResourceLocation(Tags.MODID, "models/obj/water_wheel.obj"));
    ResourceLocation texture = new ResourceLocation(Tags.MODID, "models/obj/water_wheel.png");
    private float rotationAngle = 0.0F;
    private int time=0;
    @Override
    public void render(TileEntityWaterMill te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(90, 1.0F, 0.0F, 0.0F);
        // 根据方向旋转模型
        EnumFacing facing = te.getFacing();
        if (facing == EnumFacing.NORTH) {
            GlStateManager.rotate(0, 0, 0, 1);
        } else if (facing == EnumFacing.EAST) {
            GlStateManager.rotate(90, 0, 0, 1);
        } else if (facing == EnumFacing.SOUTH) {
            GlStateManager.rotate(180, 0, 0, 1);
        } else if (facing == EnumFacing.WEST) {
            GlStateManager.rotate(270, 0, 0, 1);
        }
        GlStateManager.scale(1.5F, 1.5F, 1.5F);

        float rotationSpeed = te.getRotationSpeed();
        float rotationAngle = (te.getRotationTicks() + partialTicks) * rotationSpeed;
        GlStateManager.rotate(rotationAngle % 360.0F, 0, 1, 0);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        waterMill.renderAll();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityWaterMill te) {
        return true;
    }

}
