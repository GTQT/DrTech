package com.brachy84.mechtech.client.gui;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.List;

public class ErrorTextWidget extends Widget {

    private static final int TEXT_TIME = 100;
    private int countdown = 0;
    private String text = "";
    protected int color;
    protected boolean isCentered;
    protected boolean isShadow;
    protected float scale;
    protected int width;


    public ErrorTextWidget(int xPosition, int yPosition) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.isCentered = true;
        this.scale = 1.0F;
        this.color = 0x8A1F11;
    }

    public ErrorTextWidget setCentered(boolean centered) {
        isCentered = centered;
        return this;
    }

    public ErrorTextWidget setTextColor(int color) {
        this.color = color;
        return this;
    }

    public ErrorTextWidget setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public ErrorTextWidget setWidth(int width) {
        this.width = width;
        return this;
    }

    public void updateText(String text, Object... data) {
        if (gui != null && gui.entityPlayer.getEntityWorld().isRemote) {
            this.text = I18n.format(text, data);
            countdown = TEXT_TIME;
            this.updateSize();
        }
    }

    public void updateTextUnlocalized(String text) {
        if (gui != null && gui.entityPlayer.getEntityWorld().isRemote) {
            this.text = text;
            countdown = TEXT_TIME;
            this.updateSize();
        }
    }

    private void updateSize() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int stringWidth = fontRenderer.getStringWidth(text);
        this.setSize(new Size(stringWidth, fontRenderer.FONT_HEIGHT));
        if (this.uiAccess != null) {
            this.uiAccess.notifySizeChange();
        }

    }

    @Override
    public void updateScreen() {
        if (countdown > 0)
            --countdown;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (countdown > 0) {
            String text = this.text;
            List<String> texts;
            if (this.width > 0) {
                int var10002 = (int) ((float) this.width * (1.0F / this.scale));
                texts = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(text, var10002);
            } else {
                texts = Collections.singletonList(text);
            }

            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            Position pos = this.getPosition();
            float height = (float) fontRenderer.FONT_HEIGHT * this.scale * (float) texts.size();

            for (int i = 0; i < texts.size(); ++i) {
                String resultText = texts.get(i);
                float width = (float) fontRenderer.getStringWidth(resultText) * this.scale;
                float x = (float) pos.x - (this.isCentered ? width / 2.0F : 0.0F);
                float y = (float) pos.y + (float) (i * fontRenderer.FONT_HEIGHT);
                drawText(resultText, x, y, this.scale, this.color, this.isShadow);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
