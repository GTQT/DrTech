package com.drppp.drtech.Client.render.Items;

import com.drppp.drtech.Client.render.Items.lightsaber.LightsaberModelRegistry;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.Items.lightsaber.FocusingCrystal;
import com.drppp.drtech.common.Items.lightsaber.LightsaberHilt;
import com.drppp.drtech.common.Items.lightsaber.LightsaberPartType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public final class LightsaberRenderHelper {
    private static final int LIGHTING_LUMINOUS = 0xF0F0;
    private static final float RENDER_GLOBAL_MULTIPLIER = 1.0F;
    private static final float RENDER_WIDTH_MULTIPLIER = 1.0F;
    private static final float RENDER_SMOOTHING_MULTIPLIER = 1.0F;
    private static final float RENDER_OPACITY_MULTIPLIER = 1.0F;
    private static final float RENDER_LIGHTING_MULTIPLIER = 1.0F;

    private static final ModelLightsaberBlade LIGHTSABER_BLADE = new ModelLightsaberBlade(38);
    private static final ModelLightsaberBlade CROSSGUARD_BLADE = new ModelLightsaberBlade(4);

    private LightsaberRenderHelper() {
    }

    public static void renderLightsaber(ItemStack stack, boolean inWorld, boolean forceBlade) {
        renderLightsaberHilt(stack);
        GL11.glScalef(3.0F, 3.0F, 3.0F);
        float switchHeight = getPartHeight(stack, LightsaberPartType.SWITCH_SECTION);
        float emitterHeight = getPartHeight(stack, LightsaberPartType.EMITTER);
        GL11.glTranslatef(0.0F, -(switchHeight + emitterHeight) * 0.0234375F, 0.0F);
        renderLightsaberBlade(stack, inWorld, forceBlade);
    }

    public static void renderLightsaberHilt(ItemStack stack) {
        float bodyHeight = getPartHeight(stack, LightsaberPartType.BODY);
        float pommelHeight = getPartHeight(stack, LightsaberPartType.POMMEL);
        float totalHeight = getHiltHeight(stack);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslatef(0.0F, -(bodyHeight + pommelHeight - totalHeight / 2.0F) / 16.0F, 0.0F);

        for (LightsaberPartType type : LightsaberPartType.values()) {
            LightsaberHilt hilt = ItemLightsaber.getPart(stack, type);
            ModelBase model = LightsaberModelRegistry.getModel(hilt, type);
            bind(LightsaberModelRegistry.getTexture(hilt, type));

            if (type == LightsaberPartType.EMITTER) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, -getPartHeight(stack, LightsaberPartType.SWITCH_SECTION) / 16.0F, 0.0F);
                model.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                GL11.glPopMatrix();
            } else if (type == LightsaberPartType.POMMEL) {
                GL11.glPushMatrix();
                applyBodyInstructions(ItemLightsaber.getPart(stack, LightsaberPartType.BODY), bodyHeight);
                model.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                GL11.glPopMatrix();
            } else {
                model.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            }
        }
    }

    public static float getHiltHeight(ItemStack stack) {
        float height = 0.0F;
        for (LightsaberPartType type : LightsaberPartType.values()) {
            height += getPartHeight(stack, type);
        }
        return height;
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

        float[] crossguard = ItemLightsaber.getPart(stack, LightsaberPartType.EMITTER).getCrossguard();
        float[] rgb = ItemLightsaber.getColor(stack).getRgb();
        if (crossguard != null) {
            for (int side = -1; side <= 1; side += 2) {
                GL11.glPushMatrix();
                GL11.glTranslatef(crossguard[0], crossguard[1], crossguard[2] * -side);
                GL11.glRotatef(side * 90.0F, 1.0F, 0.0F, 0.0F);
                if (side == 1) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }
                CROSSGUARD_BLADE.renderOuter(stack, rgb,
                        ItemLightsaber.getColor(stack).getGlowIntensity(), inWorld, true);
                GL11.glPopMatrix();
            }
        }
        LIGHTSABER_BLADE.renderOuter(stack, rgb,
                ItemLightsaber.getColor(stack).getGlowIntensity(), inWorld);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

        GL11.glPushMatrix();
        if (crossguard != null) {
            for (int side = -1; side <= 1; side += 2) {
                GL11.glPushMatrix();
                GL11.glTranslatef(crossguard[0], crossguard[1], crossguard[2] * -side);
                GL11.glRotatef(side * 90.0F, 1.0F, 0.0F, 0.0F);
                if (side == 1) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }
                CROSSGUARD_BLADE.renderInner(stack, rgb, true);
                GL11.glPopMatrix();
            }
        }
        LIGHTSABER_BLADE.renderInner(stack, rgb, false);
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

    private static float getPartHeight(ItemStack stack, LightsaberPartType type) {
        return ItemLightsaber.getPart(stack, type).getHeight(type);
    }

    private static void applyBodyInstructions(LightsaberHilt body, float bodyHeight) {
        float[] instructions = body.getBodyInstructions();
        if (instructions == null || instructions.length == 0) {
            GL11.glTranslatef(0.0F, bodyHeight / 16.0F, 0.0F);
            return;
        }
        for (int i = 0; i < instructions.length; i++) {
            if ((i & 1) == 0) {
                GL11.glTranslatef(0.0F, instructions[i] / 16.0F, 0.0F);
            } else {
                GL11.glRotatef(instructions[i], 1.0F, 0.0F, 0.0F);
            }
        }
    }

    private static void bind(net.minecraft.util.ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    }

    private static void vertex(BufferBuilder buffer, float x, float y, float z) {
        buffer.pos(x, y, z).endVertex();
    }

    private static boolean hasFocusingCrystal(ItemStack stack, FocusingCrystal crystal) {
        return (ItemLightsaber.getFocusingCrystalMask(stack) & crystal.getMask()) != 0;
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

        private void renderInner(ItemStack stack, float[] rgb, boolean crossguard) {
            boolean fineCut = hasFocusingCrystal(stack, FocusingCrystal.FINE_CUT);
            if (crossguard && fineCut) {
                GL11.glScalef(1.0F, 1.2F, 1.0F);
            }
            if (hasFocusingCrystal(stack, FocusingCrystal.INVERTING)) {
                GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
            } else if (hasFocusingCrystal(stack, FocusingCrystal.PRISMATIC)) {
                GL11.glColor4f(rgb[0], rgb[1], rgb[2], 1.0F);
            } else {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
            if (hasFocusingCrystal(stack, FocusingCrystal.COMPRESSED)) {
                GL11.glScalef(0.6F, 1.0F, 0.6F);
            }
            if (hasFocusingCrystal(stack, FocusingCrystal.CRACKED)) {
                renderCrackedCore();
            }
            if (fineCut) {
                renderFineCutCore();
            } else {
                blade.render(0.0625F);
                GL11.glTranslatef(0.0F, -0.0625F * (0.5F + bladeLength), 0.0625F / 2.0F);
                LightsaberRenderHelper.drawTip(0.03125F, 0.125F);
            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        private void renderOuter(ItemStack itemStack, float[] rgb, float colorIntensity, boolean inWorld) {
            renderOuter(itemStack, rgb, colorIntensity, inWorld, false);
        }

        private void renderOuter(ItemStack itemStack, float[] rgb, float colorIntensity, boolean inWorld,
                                 boolean crossguard) {
            int smooth = 10;
            float width = crossguard ? 0.4F : 0.6F;
            float f = 1.0F;
            float f1 = 1.0F;
            float f2 = 1.0F;
            float f3 = 0.1F;
            boolean fineCut = hasFocusingCrystal(itemStack, FocusingCrystal.FINE_CUT);

            if (hasFocusingCrystal(itemStack, FocusingCrystal.COMPRESSED)) {
                width = crossguard ? 0.2F : 0.4F;
                smooth = 7;
                f1 = crossguard ? 0.9F : 1.0F;
                f3 = 0.07F;
            }
            if (hasFocusingCrystal(itemStack, FocusingCrystal.INVERTING)
                    && hasFocusingCrystal(itemStack, FocusingCrystal.PRISMATIC)) {
                rgb = new float[3];
                f3 *= crossguard ? 1.0F : 1.5F;
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
            if (fineCut) {
                f *= 0.55F;
                f1 *= 0.925F;
                f2 *= crossguard ? 1.3F : 1.1F;
            }

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
                float taper = crossguard ? 0.05F : (fineCut ? 0.003F : 0.005F);
                float base = crossguard ? 2.0F : 0.2F;
                GL11.glScaled(scale * f, (1.0F - f4 * taper + base) * f1, scale * f2);
                GL11.glTranslatef(0.0F, -f4 / 400.0F + 0.06F, 0.0F);
                if (fineCut) {
                    GL11.glTranslatef(0.0F, 0.0F, 0.005F + f4 * 0.00001F);
                }
                blade.render(0.0625F);
                GL11.glPopMatrix();
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        private void renderCrackedCore() {
            int ticks = Minecraft.getMinecraft().player == null ? 0 : Minecraft.getMinecraft().player.ticksExisted;
            Random random = new Random(ticks % 100 * 1000L);
            for (int i = 0; i < 3; i++) {
                GL11.glPushMatrix();
                GL11.glTranslatef((random.nextFloat() - 0.5F) / 60.0F, 0.0F,
                        (random.nextFloat() - 0.5F) / 60.0F);
                blade.render(0.0625F);
                GL11.glPopMatrix();
            }
        }

        private void renderFineCutCore() {
            float unit = 0.0625F;
            float longSection = unit * bladeLength * 0.7F;
            float shortSection = unit * bladeLength * 0.3F;
            float edge = unit * 1.5F;
            float end = longSection + shortSection;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            quad(buffer, -unit / 2, -unit, unit / 2, unit / 2, -unit, unit / 2,
                    unit / 2, -longSection, unit / 2, -unit / 2, -longSection, unit / 2);
            quad(buffer, -unit / 2, -unit, unit / 2, -unit / 2, -longSection, unit / 2,
                    0.0F, -longSection, edge, 0.0F, -unit * 1.5F, edge);
            quad(buffer, unit / 2, -unit, unit / 2, 0.0F, -unit * 1.5F, edge,
                    0.0F, -longSection, edge, unit / 2, -longSection, unit / 2);
            quad(buffer, -unit / 2, -longSection, unit / 2, unit / 2, -longSection, unit / 2,
                    unit / 2, -end, -unit / 2, -unit / 2, -end, -unit / 2);
            quad(buffer, -unit / 2, -longSection, -unit / 2, -unit / 2, -end, -unit / 2,
                    unit / 2, -end, -unit / 2, unit / 2, -longSection, -unit / 2);
            tessellator.draw();
            blade.render(unit);
        }

        private void quad(BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2,
                          float x3, float y3, float z3, float x4, float y4, float z4) {
            vertex(buffer, x1, y1, z1);
            vertex(buffer, x2, y2, z2);
            vertex(buffer, x3, y3, z3);
            vertex(buffer, x4, y4, z4);
        }
    }
}
