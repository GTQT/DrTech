package com.drppp.drtech.intergations.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChanceTextRenderer extends ItemStackRenderer {

    private final int chanceValue;

    public ChanceTextRenderer(int chanceValue) {
        this.chanceValue = chanceValue;
    }

    @Override
    public void render(@NotNull Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
        super.render(minecraft, xPosition, yPosition, ingredient);

        if (this.chanceValue >= 0) {
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            GlStateManager.translate(0, 0, 160);

            String s = this.chanceValue + "%";
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawStringWithShadow(s, (xPosition + 6) * 2 - fontRenderer.getStringWidth(s) + 19,
                    (yPosition + 1) * 2, 0xFFFF00);

            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
        }
    }
}
