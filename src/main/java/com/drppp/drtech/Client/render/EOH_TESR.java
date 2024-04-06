package com.drppp.drtech.Client.render;


import com.drppp.drtech.Tags;
import com.drppp.drtech.Tile.TileEntityHomoEye;
import com.drppp.drtech.common.Blocks.BlockHomoEye;
import com.drppp.drtech.common.Entity.moster.EntityUTiGolem;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static com.drppp.drtech.Tags.MODID;

@SideOnly(Side.CLIENT)
public class EOH_TESR extends TileEntitySpecialRenderer<TileEntityHomoEye> {
    @Override
    public void render(TileEntityHomoEye te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        // 检查TileEntity是否有效
        if (te == null || te.getWorld() == null) {
            return;
        }

        // 获取方块的朝向属性
        IBlockState state = te.getWorld().getBlockState(te.getPos());
        EnumFacing facing = state.getValue(BlockHomoEye.FACING);

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();


        // 根据方块朝向调整渲染方向
        float angle = 0.0F;
        switch (facing) {
            case NORTH:
                angle = 0.0F;
                break;
            case SOUTH:
                angle = 180.0F;
                break;
            case WEST:
                angle = -90.0F;
                break;
            case EAST:
                angle = 90.0F;
                break;
            case UP:
            case DOWN:
                // 可以添加对垂直方向的处理
                break;
        }
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5, 0, 0.5);
        GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.2, 0.2, 0.2);

        // 调整完渲染方向后，开始渲染实体
        Minecraft.getMinecraft().getRenderManager().renderEntity(new EntityUTiGolem(te.getWorld()), 0, 0, 0, 0, 0, false);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
