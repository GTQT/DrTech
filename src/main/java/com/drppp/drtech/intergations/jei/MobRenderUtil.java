package com.drppp.drtech.intergations.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

/**
 * JEI 页面内的实体渲染与裁剪工具。
 * 逻辑照 JER 的 RenderHelper（scissor + RenderManager.renderEntity），去掉渲染钩子扩展点；
 * 字体采用 Unicode 版 FontRenderer（中文可正常显示，观感对齐 JustEnoughDrops 的文本行）。
 * JER 为 "Don't Be a Jerk" 非商业许可，可参考。
 */
public class MobRenderUtil {

    private static FontRenderer unicodeFont;
    private static boolean fontInitFailed;

    private MobRenderUtil() {
    }

    // ---------------- 字体 ----------------

    public static FontRenderer font() {
        FontRenderer f = unicodeFont;
        if (f == null && !fontInitFailed) {
            Minecraft mc = Minecraft.getMinecraft();
            try {
                f = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"),
                        mc.getTextureManager(), true);
                if (mc.getResourceManager() instanceof IReloadableResourceManager) {
                    ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(f);
                }
                unicodeFont = f;
            } catch (Exception e) {
                fontInitFailed = true;
                return mc.fontRenderer;
            }
        }
        return f != null ? f : Minecraft.getMinecraft().fontRenderer;
    }

    /** 与 JED 相同的小字/正文绘制（颜色 8、无阴影） */
    public static void drawText(String text, int x, int y) {
        font().drawString(text, x, y, 8, false);
    }

    public static int getTextWidth(String text) {
        return font().getStringWidth(text);
    }

    // ---------------- 实体渲染 ----------------

    /** 以 (x, y) 为中心渲染实体（照 JER renderEntity；yaw/pitch 由鼠标位置换算） */
    public static void renderEntity(int x, int y, float scale, float yaw, float pitch, EntityLivingBase entity) {
        if (entity == null) return;
        if (entity.world == null) entity.world = Minecraft.getMinecraft().world;
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

        float renderYawOffset = entity.renderYawOffset;
        float rotationYaw = entity.rotationYaw;
        float rotationPitch = entity.rotationPitch;
        float prevRotationYawHead = entity.prevRotationYawHead;
        float rotationYawHead = entity.rotationYawHead;

        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-((float) Math.atan(pitch / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float) Math.atan(yaw / 40.0F) * 20.0F;
        entity.rotationYaw = (float) Math.atan(yaw / 40.0F) * 40.0F;
        entity.rotationPitch = -((float) Math.atan(pitch / 40.0F)) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;

        GlStateManager.translate(0.0F, entity.getYOffset(), 0.0F);
        RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0F);
        renderManager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);

        entity.renderYawOffset = renderYawOffset;
        entity.rotationYaw = rotationYaw;
        entity.rotationPitch = rotationPitch;
        entity.prevRotationYawHead = prevRotationYawHead;
        entity.rotationYawHead = rotationYawHead;

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    // ---------------- 裁剪 ----------------

    /** 在 (x, y, w, h) 区域内开启 scissor（GUI 缩放坐标），用于把实体裁进面板区 */
    public static void scissor(int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getMinecraft();
        int scale = new ScaledResolution(mc).getScaleFactor();
        float[] xyzTranslation = getGLTranslation(scale);
        x *= scale;
        y *= scale;
        w *= scale;
        h *= scale;
        int scissorX = Math.round(xyzTranslation[0] + x);
        int scissorY = Math.round(mc.displayHeight - y - h - xyzTranslation[1]);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, Math.round(w), Math.round(h));
    }

    public static void stopScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private static float[] getGLTranslation(int scale) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf);
        buf.rewind();
        Matrix4f mat = new Matrix4f();
        mat.load(buf);
        return new float[]{mat.m30 * scale, mat.m31 * scale, mat.m32 * scale};
    }
}
