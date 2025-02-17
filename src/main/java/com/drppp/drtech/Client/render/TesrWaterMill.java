package com.drppp.drtech.Client.render;


import com.drppp.drtech.Client.lib.obj.AdvancedModelLoader;
import com.drppp.drtech.Client.lib.obj.IModelCustom;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityWaterMill;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
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
        GlStateManager.scale(1.5F, 1.5F, 1.5F);
        if (++time >= 5) {
            rotationAngle = (rotationAngle + 1.0F) % 360.0F;
            time = 0;
        }
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        waterMill.renderAll();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityWaterMill te) {
        return true;
    }

}
