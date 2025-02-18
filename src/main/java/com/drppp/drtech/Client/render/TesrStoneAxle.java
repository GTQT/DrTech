package com.drppp.drtech.Client.render;


import com.drppp.drtech.Client.lib.obj.AdvancedModelLoader;
import com.drppp.drtech.Client.lib.obj.IModelCustom;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityStoneAxle;
import com.drppp.drtech.Tile.TileEntityWaterMill;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class TesrStoneAxle extends TileEntitySpecialRenderer<TileEntityStoneAxle> {
    public static final IModelCustom waterMill = AdvancedModelLoader.loadModel(new ResourceLocation(Tags.MODID, "models/obj/stone_axle.obj"));
    ResourceLocation texture = new ResourceLocation(Tags.MODID, "models/obj/water_wheel.png");
    private float rotationAngle = 0.0F;
    private int time=0;
    @Override
    public void render(TileEntityStoneAxle te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        GlStateManager.pushMatrix();
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        // 根据方向旋转模型
        EnumFacing facing = te.getFacing();
        if (facing == EnumFacing.NORTH) {
            GlStateManager.rotate(0, 0, 1, 0);
        } else if (facing == EnumFacing.EAST) {
            GlStateManager.rotate(270, 0, 1, 0);
        } else if (facing == EnumFacing.SOUTH) {
            GlStateManager.rotate(180, 0, 1, 0);
        } else if (facing == EnumFacing.WEST) {
            GlStateManager.rotate(90, 0, 1, 0);
        }
        GlStateManager.scale(1.8F, 1.8F, 1.8F);
        float rotationSpeed = te.getRotationSpeed();
        float rotationAngle = (te.getRotationTicks() + partialTicks) * rotationSpeed;
        GlStateManager.rotate(rotationAngle % 360.0F, 0, 0, 1);
        waterMill.renderAll();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityStoneAxle te) {
        return true;
    }

}
