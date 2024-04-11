package com.drppp.drtech.Client.render;


import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityHomoEye;
import com.drppp.drtech.common.Blocks.BlocksInit;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.obj.AdvancedModelLoader;
import thaumcraft.client.lib.obj.IModelCustom;

import java.awt.*;

import static com.drppp.drtech.Client.render.EOH_RenderingUtils.*;

@SideOnly(Side.CLIENT)
public class EOH_TESR extends TileEntitySpecialRenderer<TileEntityHomoEye> {
    public static final ResourceLocation STAR_LAYER_0 = new ResourceLocation(Tags.MODID, "models/starlayer0.png");
    public static final ResourceLocation STAR_LAYER_1 = new ResourceLocation(Tags.MODID, "models/starlayer1.png");
    public static final ResourceLocation STAR_LAYER_2 = new ResourceLocation(Tags.MODID, "models/starlayer2.png");
    public static final IModelCustom starModel= AdvancedModelLoader.loadModel(new ResourceLocation(Tags.MODID, "models/star.obj"));
    public static final IModelCustom spaceModel= AdvancedModelLoader.loadModel(new ResourceLocation(Tags.MODID, "models/space.obj"));
    private static final float STAR_RESCALE = 0.2f;
    @Override
    public void render(TileEntityHomoEye te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // 检查TileEntity是否有效
        if (te == null || te.getWorld() == null) {
            return;
        }
        GL11.glPushMatrix();
        // Required to centre the render to the middle of the block.
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        renderOuterSpaceShell();
        renderOrbitObjects(te);
        renderStar(1);
        GL11.glPopAttrib();

        GL11.glPopMatrix();
    }
    private void renderOrbitObjects(final TileEntityHomoEye EOHRenderTile) {

        if (EOHRenderTile.getOrbitingObjects() != null) {

            if (EOHRenderTile.getOrbitingObjects().size() == 0) {
                EOHRenderTile.generateImportantInfo();
            }
            int num=0;
            for (TileEntityHomoEye.OrbitingObject t : EOHRenderTile.getOrbitingObjects()) {
                renderOrbit(EOHRenderTile, t,num);
                num = (++num)%8;
            }
        }
    }

    void renderOrbit(final TileEntityHomoEye EOHRenderTile, final TileEntityHomoEye.OrbitingObject orbitingObject,int num) {
        // Render orbiting body.
        GL11.glPushMatrix();

        GL11.glRotatef(orbitingObject.zAngle, 0, 0, 1);
        GL11.glRotatef(orbitingObject.xAngle, 1, 0, 0);
        GL11.glRotatef((orbitingObject.rotationSpeed * 0.1f * EOHRenderTile.angle) % 360.0f, 0F, 1F, 0F);
        GL11.glTranslated(-0.2 - orbitingObject.distance - STAR_RESCALE * EOHRenderTile.getSize(), 0, 0);
        GL11.glRotatef((orbitingObject.orbitSpeed * 0.1f * EOHRenderTile.angle) % 360.0f, 0F, 1F, 0F);
        renderBlockInWorld(orbitingObject.block, num, orbitingObject.scale);

        GL11.glPopMatrix();
    }
}
