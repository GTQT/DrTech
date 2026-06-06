package com.drppp.drtech.lootgames.minigame.minesweeper.client;

import com.drppp.drtech.Tags;
import com.drppp.drtech.lootgames.minigame.minesweeper.GameMineSweeper;
import com.drppp.drtech.lootgames.minigame.minesweeper.tileentity.TileEntityMSMaster;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import static com.drppp.drtech.lootgames.minigame.minesweeper.MSBoard.MSField.*;

@SideOnly(Side.CLIENT)
public class TESRMSMaster extends TileEntitySpecialRenderer<TileEntityMSMaster> {
    private static final ResourceLocation MS_BOARD = new ResourceLocation(Tags.MODID, "textures/blocks/lootgames/minesweeper/ms_board.png");

    @Override
    public void render(TileEntityMSMaster te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        bindTexture(MS_BOARD);
        GameMineSweeper game = te.getGame();
        int boardSize = game.getBoardSize();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 1F, z);
        GlStateManager.disableLighting();
        GlStateManager.rotate(90, 1, 0, 0);

        float cellUV = 0.25F; // 1/4 of the atlas (4x4 grid)

        if (!game.cIsGenerated || !game.getBoard().isGenerated()) {
            for (int xL = 0; xL < boardSize; xL++) {
                for (int zL = 0; zL < boardSize; zL++) {
                    drawTexturedRect(xL, zL, 1, 1, -0.005F, 0, 0, cellUV, cellUV);
                }
            }
        } else {
            for (int xL = 0; xL < boardSize; xL++) {
                for (int zL = 0; zL < boardSize; zL++) {
                    boolean isHidden = game.getBoard().isHidden(xL, zL);

                    int type = game.getBoard().getType(xL, zL);

                    if (!isHidden && type == BOMB) {
                        int max = game.detonationTimeInTicks;
                        int ticks = game.getTicks();

                        int times = 9;
                        float period = (float) max / times;

                        float extendedPeriod = period * (times + 1) / times;
                        double alphaFactor = game.getStage() == GameMineSweeper.Stage.EXPLODING ? 1 : Math.abs(Math.sin(Math.toRadians(ticks / extendedPeriod * 180F)));

                        drawTexturedRect(xL, zL, 1, 1, -0.005F, cellUV, 0, cellUV, cellUV);
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                        GlStateManager.enableAlpha();
                        GL11.glColor4d(1, 1, 1, alphaFactor);
                        drawTexturedRect(xL, zL, 1, 1, -0.005F, cellUV, 3 * cellUV, cellUV, cellUV);
                        GlStateManager.disableAlpha();
                        GlStateManager.disableBlend();
                    } else {
                        int mark = game.getBoard().getMark(xL, zL);

                        float textureU;
                        float textureV;

                        if (isHidden) {
                            if (mark == NO_MARK) {
                                textureU = 0;
                                textureV = 0;
                            } else if (mark == FLAG) {
                                textureU = 3 * cellUV;
                                textureV = 0;
                            } else {
                                textureU = 0;
                                textureV = 3 * cellUV;
                            }
                        } else {
                            if (type > 0) {
                                textureU = (type % 4 == 0 ? 3 : (type % 4 - 1)) * cellUV;
                                textureV = (type <= 4 ? 1 : 2) * cellUV;
                            } else {
                                textureU = 2 * cellUV;
                                textureV = 0;
                            }
                        }

                        drawTexturedRect(xL, zL, 1, 1, -0.005F, textureU, textureV, cellUV, cellUV);
                    }
                }
            }
        }
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawTexturedRect(double x, double y, double width, double height, double zLevel,
                                  float u, float v, float uWidth, float vHeight) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, zLevel).tex(u, v + vHeight).endVertex();
        buffer.pos(x + width, y + height, zLevel).tex(u + uWidth, v + vHeight).endVertex();
        buffer.pos(x + width, y, zLevel).tex(u + uWidth, v).endVertex();
        buffer.pos(x, y, zLevel).tex(u, v).endVertex();
        tessellator.draw();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityMSMaster te) {
        return true;
    }
}
