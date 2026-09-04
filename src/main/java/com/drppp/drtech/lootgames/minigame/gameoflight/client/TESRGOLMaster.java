package com.drppp.drtech.lootgames.minigame.gameoflight.client;

import com.drppp.drtech.Tags;
import com.drppp.drtech.lootgames.api.util.DirectionOctagonal;
import com.drppp.drtech.lootgames.minigame.gameoflight.TileEntityGOLMaster;
import com.drppp.drtech.lootgames.packets.CMessageGOLFeedback;
import com.drppp.drtech.lootgames.packets.NetworkHandler;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import static com.drppp.drtech.lootgames.minigame.gameoflight.TileEntityGOLMaster.*;

@SideOnly(Side.CLIENT)
public class TESRGOLMaster extends TileEntitySpecialRenderer<TileEntityGOLMaster> {
    private static final ResourceLocation GAME_FIELD = new ResourceLocation(Tags.MODID, "textures/blocks/lootgames/gameoflight/game_field.png");
    private static final ResourceLocation GAME_FIELD_ACTIVATED = new ResourceLocation(Tags.MODID, "textures/blocks/lootgames/gameoflight/game_field_active.png");
    private static final ResourceLocation SPECIAL_STUFF = new ResourceLocation(Tags.MODID, "textures/blocks/lootgames/gameoflight/special_stuff.png");

    private static final float INV_48 = 1F / 48F;

    @Override
    public void render(TileEntityGOLMaster te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getGameStage() == GameStage.NOT_CONSTRUCTED) {
            return;
        }

        drawField(te, x, y, z, te.getTicks(), partialTicks);

        if (te.getGameStage() == GameStage.SHOWING_SEQUENCE) {
            drawStuff(te, EnumDrawStuff.SHOWING_SEQUENCE, x, y, z, te.getTicks(), partialTicks);

            if (!te.isOnPause()) {
                if (te.isShowingSymbols()) {
                    drawSymbol(te, te.getCurrentSymbolPosOffset(), x, y, z, te.getTicks(), partialTicks);
                } else if (!te.isFeedbackPacketReceived()) {
                    NetworkHandler.INSTANCE.sendToServer(new CMessageGOLFeedback(te.getPos()));
                    te.onClientThingsDone();
                }
            }
        }

        if (te.getSymbolsEnteredByPlayer() != null) {
            te.getSymbolsEnteredByPlayer().forEach(clickInfo -> drawSymbol(te, clickInfo.getOffset(), x, y, z, te.getTicks(), partialTicks));
        }

        if (te.getStuffToDraw() != null) {
            te.getStuffToDraw().forEach(stuff -> drawStuff(te, stuff.getStuff(), x, y, z, te.getTicks(), partialTicks));
        }
    }

    private void drawField(TileEntityGOLMaster te, double x, double y, double z, int ticks, float partialTicks) {
        bindTexture(GAME_FIELD);

        boolean isExpanding = te.getGameStage() == GameStage.UNDER_EXPANDING;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F, y + 1F, z + 0.5F);
        GlStateManager.disableLighting();
        GlStateManager.rotate(90, 1, 0, 0);

        float progress = isExpanding ? (ticks + partialTicks) / MAX_TICKS_EXPANDING : 1F;
        float length = isExpanding ? 48F + 96F * Math.min(progress, 1F) : 144F;

        GlStateManager.scale(INV_48, INV_48, INV_48);
        GlStateManager.translate(-length / 2F, -length / 2F, 0);

        float textureStart, textureLength;
        if (isExpanding && ticks < MAX_TICKS_EXPANDING) {
            textureStart = 16F - 16F * progress;
            textureLength = 32F + 16F * progress - textureStart;
        } else {
            textureStart = 0F;
            textureLength = 48F;
        }

        drawTexturedRect(0, 0, length, length, -0.005F,
                textureStart * INV_48, textureStart * INV_48,
                textureLength * INV_48, textureLength * INV_48);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawSymbol(TileEntityGOLMaster te, DirectionOctagonal offset, double masterX, double masterY, double masterZ, int ticks, float partialTicks) {
        if (ticks > ticksPerShowSymbols) {
            return;
        }

        bindTexture(GAME_FIELD_ACTIVATED);

        GlStateManager.pushMatrix();
        GlStateManager.translate(masterX + offset.getOffsetX(), masterY + 1F, masterZ + offset.getOffsetZ());
        GlStateManager.disableLighting();
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.scale(INV_48, INV_48, INV_48);

        float u = (16F + 16F * offset.getOffsetX()) * INV_48;
        float v = (16F + 16F * offset.getOffsetZ()) * INV_48;

        drawTexturedRect(0, 0, 48, 48, -0.005F, u, v, 16F * INV_48, 16F * INV_48);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawStuff(TileEntityGOLMaster te, EnumDrawStuff stuff, double masterX, double masterY, double masterZ, int ticks, float partialTicks) {
        bindTexture(SPECIAL_STUFF);

        GlStateManager.pushMatrix();
        GlStateManager.translate(masterX, masterY + 1F, masterZ);
        GlStateManager.disableLighting();
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.scale(INV_48, INV_48, INV_48);

        float u = 16F * (stuff == EnumDrawStuff.SEQUENCE_ACCEPTED ? 0 : stuff == EnumDrawStuff.SHOWING_SEQUENCE ? 1 : 2) * INV_48;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();

        drawTexturedRect(0, 0, 48, 48, -0.005F, u, 0, 16F * INV_48, 16F * INV_48);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
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
    public boolean isGlobalRenderer(TileEntityGOLMaster te) {
        return true;
    }
}
