package com.drppp.drtech.Client.render;



import com.drppp.drtech.Client.lib.obj.AdvancedModelLoader;
import com.drppp.drtech.Client.lib.obj.IModelCustom;
import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityHomoEye;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class EOH_TESR extends TileEntitySpecialRenderer<TileEntityHomoEye> {
    public static final IModelCustom testModel= AdvancedModelLoader.loadModel(new ResourceLocation(Tags.MODID, "models/ship.obj"));
    ResourceLocation ss =new ResourceLocation("drtech","models/hongbei.png");
    @Override
    public void render(TileEntityHomoEye te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
       FMLClientHandler.instance().getClient().getTextureManager().bindTexture(ss);
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5, 0.5, 0.5);
        float angle = (System.currentTimeMillis() % 3600) / 10.0f;
      //  GlStateManager.rotate(angle, 0F, 0F, 1F);
       // GlStateManager.scale(0.1,0.1,0.1);
        testModel.renderAll();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.popAttrib();
        GlStateManager.enableCull();
    }
    @Override
    public boolean isGlobalRenderer(TileEntityHomoEye te) {
        return super.isGlobalRenderer(te);
    }

}
