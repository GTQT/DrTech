package com.drppp.drtech.personalspace.blocks.tile;

import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TilePersonalPortalSpecialRender extends TileEntitySpecialRenderer<TilePersonalPortal> {

    private static final ResourceLocation TEXTURE_BOOK = new ResourceLocation(
            "textures/entity/enchanting_table_book.png");
    private final ModelBook book = new ModelBook();

    @Override
    public void render(TilePersonalPortal te, double x, double y, double z, float partialTicks, int destroyStage,
                       float alpha) {
        if (te.getDisplayName() != null) {
            this.setLightmapDisabled(true);
            this.drawNameplate(te, te.getDisplayName().getFormattedText(), x, y + 0.5D, z, 24);
            this.setLightmapDisabled(false);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5f, (float) y + 1.125f, (float) z + 0.5f);

        float time = MathHelper.sin(te.getWorld().getWorldInfo().getWorldTotalTime() + partialTicks);
        float levValue = MathHelper.sin(time * 0.01f);
        GlStateManager.translate(0.0f, 0.1F + levValue, 0.0f);

        float rot = te.getBookRotPrev() + (te.getBookRot() - te.getBookRotPrev()) * partialTicks;

        GlStateManager.rotate(rot * 180.0f / (float) Math.PI, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-270.0f / (float) Math.PI, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(80.0F, 0.0F, 0.0F, 1.0F);

        this.bindTexture(TEXTURE_BOOK);
        float pageRightAngle = 0.3F;
        float pageLeftAngle = 0.9F;
        pageRightAngle = (pageRightAngle - (float) MathHelper.fastFloor((double) pageRightAngle)) * 1.6F - 0.3F;
        pageLeftAngle = (pageLeftAngle - (float) MathHelper.fastFloor((double) pageLeftAngle)) * 1.6F - 0.3F;

        pageRightAngle = MathHelper.clamp(pageRightAngle, 0.0f, 1.0f);
        pageLeftAngle = MathHelper.clamp(pageLeftAngle, 0.0f, 1.0f);
        float pageSpread;
        if (te.isActive()) {
            pageSpread = Math.abs(pageRightAngle - pageLeftAngle);
        } else {
            pageSpread = 0.0f;
        }

        // float age = 0.75f + 0.1f * (float)Math.sin(time);
        GlStateManager.enableCull();
        this.book.render(null, time, pageRightAngle, pageLeftAngle, pageSpread, 0.0f, 0.0625f);
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawNameplate(TilePersonalPortal te, String str, double x, double y, double z, int maxDistance) {
        super.drawNameplate(te, str, x, y, z, maxDistance);
    }
}
