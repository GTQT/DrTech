package com.drppp.drtech.Client.render;
import com.drppp.drtech.Tile.TileEntityConnector;
import com.drppp.drtech.Tile.TileEntityGravitationalAnomaly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
public class ConnectorTesr extends TileEntitySpecialRenderer<TileEntityConnector> {
    public ConnectorTesr(){

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(TileEntityConnector te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        if(te.shouldRender())
        {
            renderLine(te.beforePos,te.getPos(),partialTicks);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void renderLine(BlockPos startPos, BlockPos endPos,float ticks)
    {
        BlockPos pos1 = startPos; // 第一个方块的位置
        BlockPos pos2 = endPos; // 第二个方块的位置

        // 获取当前玩家的偏移量，以保证渲染的位置正确
        EntityPlayer player = Minecraft.getMinecraft().player;
        double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * ticks;
        double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * ticks;
        double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * ticks;

        // 开始渲染
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(2.0F); // 设置线宽

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // 设置线的颜色（RGBA）
        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        float alpha = 1.0F;

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(pos1.getX() - doubleX, pos1.getY() - doubleY, pos1.getZ() - doubleZ).color(red, green, blue, alpha).endVertex();
        buffer.pos(pos2.getX() - doubleX, pos2.getY() - doubleY, pos2.getZ() - doubleZ).color(red, green, blue, alpha).endVertex();

        tessellator.draw();

        // 恢复之前的设置
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}
