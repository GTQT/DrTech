package com.drppp.drtech.Client.render.Items;

import com.drppp.drtech.Client.render.Items.lightsaber.model.ModelBodyGraflex;
import com.drppp.drtech.Client.render.Items.lightsaber.model.ModelEmitterGraflex;
import com.drppp.drtech.Client.render.Items.lightsaber.model.ModelPommelGraflex;
import com.drppp.drtech.Client.render.Items.lightsaber.model.ModelSwitchSectionGraflex;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class LightsaberRenderHelper {
    private static final float EMITTER_HEIGHT = 16.0F;
    private static final float SWITCH_HEIGHT = 8.8F;
    private static final float BODY_HEIGHT = 16.0F;
    private static final float POMMEL_HEIGHT = 1.0F;
    private static final float TOTAL_HEIGHT = EMITTER_HEIGHT + SWITCH_HEIGHT + BODY_HEIGHT + POMMEL_HEIGHT;
    private static final int LIGHTING_LUMINOUS = 0xF0F0;
    private static final float RENDER_GLOBAL_MULTIPLIER = 1.0F;
    private static final float RENDER_WIDTH_MULTIPLIER = 1.0F;
    private static final float RENDER_SMOOTHING_MULTIPLIER = 1.0F;
    private static final float RENDER_OPACITY_MULTIPLIER = 1.0F;
    private static final float RENDER_LIGHTING_MULTIPLIER = 1.0F;

    private static final ResourceLocation EMITTER_TEX = texture("emitter_graflex");
    private static final ResourceLocation SWITCH_TEX = texture("switch_section_graflex");
    private static final ResourceLocation BODY_TEX = texture("body_graflex");
    private static final ResourceLocation POMMEL_TEX = texture("pommel_graflex");

    private static final ModelBase EMITTER = new ModelEmitterGraflex();
    private static final ModelBase SWITCH = new ModelSwitchSectionGraflex();
    private static final ModelBase BODY = new ModelBodyGraflex();
    private static final ModelBase POMMEL = new ModelPommelGraflex();
    private static final ModelLightsaberBlade LIGHTSABER_BLADE = new ModelLightsaberBlade(38);

    private LightsaberRenderHelper() {
    }

    public static void renderLightsaber(ItemStack stack, boolean inWorld, boolean forceBlade) {
        renderLightsaberHilt();
        GL11.glScalef(3.0F, 3.0F, 3.0F);
        GL11.glTranslatef(0.0F, -(SWITCH_HEIGHT + EMITTER_HEIGHT) * 0.0234375F, 0.0F);
        renderLightsaberBlade(stack, inWorld, forceBlade);
    }

    public static void renderLightsaberHilt() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslatef(0.0F, -(BODY_HEIGHT + POMMEL_HEIGHT - TOTAL_HEIGHT / 2.0F) / 16.0F, 0.0F);

        bind(EMITTER_TEX);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -SWITCH_HEIGHT / 16.0F, 0.0F);
        EMITTER.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();

        bind(SWITCH_TEX);
        SWITCH.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

        bind(BODY_TEX);
        BODY.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

        bind(POMMEL_TEX);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, BODY_HEIGHT / 16.0F, 0.0F);
        POMMEL.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        GL11.glPopMatrix();
    }

    public static float getHiltHeight() {
        return TOTAL_HEIGHT;
    }

    public static void renderLightsaberBlade(ItemStack stack, boolean inWorld, boolean forceBlade) {
        if (!forceBlade && !ItemLightsaber.isActive(stack)) {
            return;
        }

        boolean prevCull = GL11.glGetBoolean(GL11.GL_CULL_FACE);
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        int lighting = (int) Math.ceil(LIGHTING_LUMINOUS * RENDER_LIGHTING_MULTIPLIER);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (lighting % 65536) / 255.0F, (lighting / 65536) / 255.0F);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
        GL11.glTranslatef(0.0F, 0.095F, 0.0F);

        GL11.glAlphaFunc(GL11.GL_GREATER, 0.99F);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        LIGHTSABER_BLADE.renderOuter(stack, ItemLightsaber.getColor(stack).getRgb(),
                ItemLightsaber.getColor(stack).getGlowIntensity(), inWorld);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

        GL11.glPushMatrix();
        LIGHTSABER_BLADE.renderInner();
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (prevCull) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        GL11.glPopMatrix();
    }

    public static void drawTip(float size, float tip) {
        float f = 0.0625F;
        float f1 = f / 2.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        vertex(buffer, size, size, 0.0F);
        vertex(buffer, -size, size, 0.0F);
        vertex(buffer, -size + f1, -size - tip, -f1);
        vertex(buffer, size - f1, -size - tip, -f1);
        vertex(buffer, size, size, -f);
        vertex(buffer, -size, size, -f);
        vertex(buffer, -size + f1, -size - tip, -f + f1);
        vertex(buffer, size - f1, -size - tip, -f + f1);
        vertex(buffer, -f1, size, size - f1);
        vertex(buffer, -f1, size, -size - f1);
        vertex(buffer, 0.0F, -size - tip, -size);
        vertex(buffer, 0.0F, -size - tip, size - f);
        vertex(buffer, f1, size, size - f1);
        vertex(buffer, f1, size, -size - f1);
        vertex(buffer, 0.0F, -size - tip, -size);
        vertex(buffer, 0.0F, -size - tip, size - f);
        tessellator.draw();
    }

    private static void bind(ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(Tags.MODID, "textures/models/lightsaber/" + name + ".png");
    }

    private static void vertex(BufferBuilder buffer, float x, float y, float z) {
        buffer.pos(x, y, z).endVertex();
    }

    private static final class ModelLightsaberBlade extends ModelBase {
        private final ModelRenderer blade;
        private final int bladeLength;

        private ModelLightsaberBlade(int length) {
            textureWidth = 64;
            textureHeight = 32;
            blade = new ModelRenderer(this, 0, 0);
            blade.addBox(-0.5F, -length, -0.5F, 1, length, 1);
            bladeLength = length;
        }

        private void renderInner() {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            blade.render(0.0625F);
            GL11.glTranslatef(0.0F, -0.0625F * (0.5F + bladeLength), 0.0625F / 2.0F);
            LightsaberRenderHelper.drawTip(0.03125F, 0.125F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        private void renderOuter(ItemStack itemStack, float[] rgb, float colorIntensity, boolean inWorld) {
            int smooth = 10;
            float width = 0.6F;
            float f = 1.0F;
            float f1 = 1.0F;
            float f2 = 1.0F;
            float f3 = 0.1F;

            if (inWorld) {
                width *= RENDER_GLOBAL_MULTIPLIER * RENDER_WIDTH_MULTIPLIER;
                smooth *= RENDER_GLOBAL_MULTIPLIER * RENDER_SMOOTHING_MULTIPLIER;
            }

            if (itemStack.getDisplayName().equals("jeb_")) {
                smooth *= 0.25F;
            }

            int layerCount = 5 * smooth;
            float opacityMultiplier = inWorld ? RENDER_GLOBAL_MULTIPLIER * RENDER_OPACITY_MULTIPLIER : 1.0F;

            for (int i = 0; i < layerCount; ++i) {
                GL11.glColor4f(rgb[0], rgb[1], rgb[2],
                        f3 / smooth * opacityMultiplier * colorIntensity);
                float scale = 1.0F + i * (width / smooth);
                float f4 = (float) i / layerCount * 50.0F;

                GL11.glPushMatrix();
                GL11.glScaled(scale * f, (1.0F - f4 * 0.005F + 0.2F) * f1, scale * f2);
                GL11.glTranslatef(0.0F, -f4 / 400.0F + 0.06F, 0.0F);
                blade.render(0.0625F);
                GL11.glPopMatrix();
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
