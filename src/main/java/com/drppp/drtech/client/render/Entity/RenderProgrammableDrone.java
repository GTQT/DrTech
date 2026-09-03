package com.drppp.drtech.client.render.Entity;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.common.drone.firmware.DroneSafetyState;
import com.drppp.drtech.common.drone.program.runtime.DroneRuntimeStatus;
import com.drppp.drtech.common.drone.hardware.DroneChassisTier;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeType;
import com.drppp.drtech.common.drone.visual.DroneVisualState;
import com.drppp.drtech.client.render.Items.LightsaberRenderHelper;
import com.drppp.drtech.client.render.Items.RenderItemDoubleLightsaber;
import com.drppp.drtech.common.items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.items.lightsaber.ItemDoubleLightsaber;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public final class RenderProgrammableDrone extends RenderLiving<EntityProgrammableDrone> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID,
            "textures/entity/drone/programmable_drone.png");
    private final ModelProgrammableDrone droneModel;

    public RenderProgrammableDrone(RenderManager manager) {
        this(manager, new ModelProgrammableDrone());
    }

    private RenderProgrammableDrone(RenderManager manager, ModelProgrammableDrone model) {
        super(manager, model, 0.35F);
        this.droneModel = model;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityProgrammableDrone entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(EntityProgrammableDrone entity, double x, double y, double z,
            float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        renderFishingLine(entity, x, y, z, partialTicks);
        renderEntityTargetLine(entity, x, y, z, partialTicks);
        renderFlightMarker(entity, x, y, z);
        String label = entity.getVisualStatusLabel();
        if (!label.isEmpty() && renderManager.renderViewEntity != null
                && entity.getDistanceSq(renderManager.renderViewEntity) <= 4096.0D) {
            renderLivingLabel(entity, label, x, y - 0.25D, z, 64);
        }
    }

    @Override
    protected void renderModel(EntityProgrammableDrone entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        setChassisColor(entity.getVisualChassis());
        super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture(TEXTURE);
        renderInstalledUpgrades(entity, scaleFactor);
        renderHeldWeapons(entity, scaleFactor);
        renderFishingRod(entity, scaleFactor);
        renderAlchemyJar(entity, scaleFactor);
        GlStateManager.disableLighting();
        float previousX = OpenGlHelper.lastBrightnessX;
        float previousY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        setStatusColor(entity);
        droneModel.renderStatusLights(scaleFactor);
        float cargo = entity.getVisualCargoFill();
        GlStateManager.color(Math.min(1.0F, cargo * 1.7F), Math.min(1.0F, (1.0F - cargo) * 1.7F), 0.08F, 1.0F);
        droneModel.renderCargoStatusLight(scaleFactor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, previousX, previousY);
        GlStateManager.enableLighting();
    }

    private void renderFlightMarker(EntityProgrammableDrone entity, double x, double y, double z) {
        DroneVisualState.FlightMarker marker = entity.getVisualFlightMarker();
        if (marker == DroneVisualState.FlightMarker.NONE) return;
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.glLineWidth(2.0F);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        float red = marker == DroneVisualState.FlightMarker.TAKEOFF ? 0.15F : 1.0F;
        float green = marker == DroneVisualState.FlightMarker.TAKEOFF ? 0.85F : 0.65F;
        for (int point = 0; point < 32; point++) {
            double angle = point * Math.PI * 2.0D / 32.0D;
            double radius = 0.46D + Math.sin(entity.ticksExisted * 0.2D) * 0.05D;
            buffer.pos(x + Math.cos(angle) * radius, y - 0.28D, z + Math.sin(angle) * radius)
                    .color(red, green, 1.0F, 0.85F).endVertex();
        }
        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    private void renderInstalledUpgrades(EntityProgrammableDrone entity, float scaleFactor) {
        for (DroneUpgradeType type : DroneUpgradeType.values()) {
            if (!entity.hasVisualUpgrade(type)) continue;
            setUpgradeColor(type);
            droneModel.renderUpgrade(type, scaleFactor);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderHeldWeapons(EntityProgrammableDrone entity, float scaleFactor) {
        if (!entity.hasVisualUpgrade(DroneUpgradeType.COMBAT)) return;
        renderHeldWeapon(entity, entity.getVisualHeldWeapon(0), -0.13F, scaleFactor);
        renderHeldWeapon(entity, entity.getVisualHeldWeapon(1), 0.13F, scaleFactor);
    }

    private void renderHeldWeapon(EntityProgrammableDrone entity, ItemStack weapon, float x, float scaleFactor) {
        if (weapon.isEmpty()) return;
        GlStateManager.pushMatrix();
        droneModel.postRenderToolArm(scaleFactor);
        GlStateManager.translate(x, 0.46F, -0.03F);
        if (weapon.getItem() instanceof ItemDoubleLightsaber) {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(0.20F, 0.20F, 0.20F);
            RenderItemDoubleLightsaber.renderLightsaber(weapon, true, false);
        } else if (weapon.getItem() instanceof ItemLightsaber) {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(0.22F, 0.22F, 0.22F);
            LightsaberRenderHelper.renderLightsaber(weapon, true, false);
        } else {
            GlStateManager.scale(0.72F, 0.72F, 0.72F);
            Minecraft.getMinecraft().getRenderItem().renderItem(
                    weapon, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
        }
        GlStateManager.popMatrix();
        bindTexture(TEXTURE);
    }

    private void renderFishingRod(EntityProgrammableDrone entity, float scaleFactor) {
        if (!entity.hasVisualUpgrade(DroneUpgradeType.FISHING)) return;
        ItemStack rod = entity.getVisualFishingRod();
        if (rod.isEmpty()) return;
        GlStateManager.pushMatrix();
        droneModel.postRenderToolArm(scaleFactor);
        GlStateManager.translate(0.0F, 0.46F, -0.03F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-38.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(-0.045F, 0.015F, 0.0F);
        GlStateManager.scale(0.78F, 0.78F, 0.78F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Minecraft.getMinecraft().getRenderItem().renderItem(
                rod, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        bindTexture(TEXTURE);
    }

    private void renderAlchemyJar(EntityProgrammableDrone entity, float scaleFactor) {
        if (!entity.hasVisualUpgrade(DroneUpgradeType.THAUMCRAFT_ALCHEMY)) return;
        ItemStack jar = entity.getVisualAlchemyJar();
        if (jar.isEmpty()) return;
        float animation = entity.getAlchemyAnimationProgress(0.0F);
        GlStateManager.pushMatrix();
        droneModel.postRenderToolArm(scaleFactor);
        GlStateManager.translate(0.0F, 0.54F - animation * 0.08F, 0.0F);
        GlStateManager.rotate((float) Math.sin(entity.ticksExisted * 0.35F) * animation * 9.0F,
                0.0F, 0.0F, 1.0F);
        GlStateManager.pushMatrix();
        // FIXED block-item models arrive upside down under the claw: flip vertically first,
        // then turn the jar face toward the drone's forward direction.
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.62F, 0.62F, 0.62F);
        Minecraft.getMinecraft().getRenderItem().renderItem(
                jar, ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.popMatrix();
        int color = entity.getVisualAlchemyAspectColor();
        if (color >= 0) {
            float fill = entity.getAlchemyFillProgress(0.0F);
            float fillHeight = 0.018F + 0.142F * fill;
            float red = ((color >> 16) & 0xFF) / 255.0F;
            float green = ((color >> 8) & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.color(red, green, blue, 0.72F);
            // Render in the claw's base space, not in the jar item's matrix. Anchor the lower
            // face at the jar floor and raise only the top face as transfer progresses.
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.170F - fillHeight * 0.5F, 0.0F);
            GlStateManager.scale(0.075F, fillHeight * 0.5F, 0.075F);
            GL11.glBegin(GL11.GL_QUADS);
            cubeVertex(-1,-1, 1); cubeVertex( 1,-1, 1); cubeVertex( 1, 1, 1); cubeVertex(-1, 1, 1);
            cubeVertex( 1,-1,-1); cubeVertex(-1,-1,-1); cubeVertex(-1, 1,-1); cubeVertex( 1, 1,-1);
            cubeVertex(-1,-1,-1); cubeVertex(-1,-1, 1); cubeVertex(-1, 1, 1); cubeVertex(-1, 1,-1);
            cubeVertex( 1,-1, 1); cubeVertex( 1,-1,-1); cubeVertex( 1, 1,-1); cubeVertex( 1, 1, 1);
            cubeVertex(-1, 1, 1); cubeVertex( 1, 1, 1); cubeVertex( 1, 1,-1); cubeVertex(-1, 1,-1);
            cubeVertex(-1,-1,-1); cubeVertex( 1,-1,-1); cubeVertex( 1,-1, 1); cubeVertex(-1,-1, 1);
            GL11.glEnd();
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }
        GlStateManager.popMatrix();
        bindTexture(TEXTURE);
    }

    private static void cubeVertex(double x, double y, double z) { GL11.glVertex3d(x, y, z); }

    private void renderFishingLine(EntityProgrammableDrone entity, double x, double y, double z,
            float partialTicks) {
        if (!entity.hasVisualUpgrade(DroneUpgradeType.FISHING)
                || entity.getVisualFishingRod().isEmpty()) return;
        EntityFishHook hook = entity.getVisualFishingHook();
        BlockPos visualTarget = entity.getVisualFishingTarget();
        if ((hook == null || hook.isDead) && visualTarget == null) return;
        double droneX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double droneY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double droneZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        boolean fallbackBobber = hook == null || hook.isDead;
        double hookX;
        double hookY;
        double hookZ;
        if (!fallbackBobber) {
            hookX = hook.lastTickPosX + (hook.posX - hook.lastTickPosX) * partialTicks;
            hookY = hook.lastTickPosY + (hook.posY - hook.lastTickPosY) * partialTicks;
            hookZ = hook.lastTickPosZ + (hook.posZ - hook.lastTickPosZ) * partialTicks;
        } else {
            float flight = entity.getFishingCastFlightProgress(partialTicks);
            double targetX = visualTarget.getX() + 0.5D;
            double targetY = visualTarget.getY() + 0.38D
                    + (flight >= 1.0F ? Math.sin((entity.ticksExisted + partialTicks) * 0.22D) * 0.025D : 0.0D);
            double targetZ = visualTarget.getZ() + 0.5D;
            hookX = droneX + (targetX - droneX) * flight;
            hookY = droneY + 0.05D + (targetY - droneY - 0.05D) * flight
                    + Math.sin(flight * Math.PI) * 0.75D;
            hookZ = droneZ + (targetZ - droneZ) * flight;
        }
        double endX = x + hookX - droneX;
        double endY = y + hookY - droneY;
        double endZ = z + hookZ - droneZ;
        double startY = y + 0.08D;
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.glLineWidth(1.5F);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int segment = 0; segment <= 24; segment++) {
            double progress = segment / 24.0D;
            double sag = Math.sin(progress * Math.PI) * 0.22D;
            buffer.pos(x + (endX - x) * progress,
                            startY + (endY - startY) * progress - sag,
                            z + (endZ - z) * progress)
                    .color(28, 28, 24, 255).endVertex();
        }
        Tessellator.getInstance().draw();
        if (fallbackBobber) renderFallbackFishingBobber(endX, endY, endZ);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    /** Client fallback for vanilla hooks owned by an untracked FakePlayer. */
    private void renderFallbackFishingBobber(double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        drawBobberBox(-0.065D, 0.0D, -0.065D, 0.065D, 0.12D, 0.065D, 0.82F, 0.12F, 0.10F);
        drawBobberBox(-0.055D, -0.10D, -0.055D, 0.055D, 0.0D, 0.055D, 0.94F, 0.94F, 0.88F);
        GlStateManager.popMatrix();
    }

    private static void drawBobberBox(double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ, float red, float green, float blue) {
        GlStateManager.color(red, green, blue, 1.0F);
        GL11.glBegin(GL11.GL_QUADS);
        cubeVertex(minX, minY, minZ); cubeVertex(maxX, minY, minZ); cubeVertex(maxX, maxY, minZ); cubeVertex(minX, maxY, minZ);
        cubeVertex(maxX, minY, maxZ); cubeVertex(minX, minY, maxZ); cubeVertex(minX, maxY, maxZ); cubeVertex(maxX, maxY, maxZ);
        cubeVertex(minX, minY, maxZ); cubeVertex(minX, minY, minZ); cubeVertex(minX, maxY, minZ); cubeVertex(minX, maxY, maxZ);
        cubeVertex(maxX, minY, minZ); cubeVertex(maxX, minY, maxZ); cubeVertex(maxX, maxY, maxZ); cubeVertex(maxX, maxY, minZ);
        cubeVertex(minX, maxY, minZ); cubeVertex(maxX, maxY, minZ); cubeVertex(maxX, maxY, maxZ); cubeVertex(minX, maxY, maxZ);
        cubeVertex(minX, minY, maxZ); cubeVertex(maxX, minY, maxZ); cubeVertex(maxX, minY, minZ); cubeVertex(minX, minY, minZ);
        GL11.glEnd();
    }

    private void renderEntityTargetLine(EntityProgrammableDrone drone, double x, double y, double z,
            float partialTicks) {
        Entity target = drone.getVisualEntityTarget();
        if (target == null || target.isDead) return;
        double droneX = drone.lastTickPosX + (drone.posX - drone.lastTickPosX) * partialTicks;
        double droneY = drone.lastTickPosY + (drone.posY - drone.lastTickPosY) * partialTicks;
        double droneZ = drone.lastTickPosZ + (drone.posZ - drone.lastTickPosZ) * partialTicks;
        double targetX = target.lastTickPosX + (target.posX - target.lastTickPosX) * partialTicks;
        double targetY = target.lastTickPosY + (target.posY - target.lastTickPosY) * partialTicks
                + target.height * 0.5D;
        double targetZ = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partialTicks;
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.glLineWidth(1.25F);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y + 0.12D, z).color(64, 220, 255, 180).endVertex();
        buffer.pos(x + targetX - droneX, y + targetY - droneY, z + targetZ - droneZ)
                .color(255, 92, 64, 220).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    private static void setChassisColor(DroneChassisTier chassis) {
        switch (chassis) {
            case HV -> GlStateManager.color(0.92F, 0.96F, 1.0F, 1.0F);
            case EV -> GlStateManager.color(0.68F, 0.95F, 1.0F, 1.0F);
            case IV -> GlStateManager.color(0.82F, 0.68F, 1.0F, 1.0F);
        }
    }

    private static void setUpgradeColor(DroneUpgradeType type) {
        switch (type) {
            case BATTERY -> GlStateManager.color(1.0F, 0.58F, 0.08F, 1.0F);
            case PROPULSION -> GlStateManager.color(0.05F, 0.9F, 1.0F, 1.0F);
            case EFFICIENCY -> GlStateManager.color(0.18F, 1.0F, 0.28F, 1.0F);
            case CARGO -> GlStateManager.color(0.95F, 0.42F, 0.08F, 1.0F);
            case WIRELESS -> GlStateManager.color(0.65F, 0.28F, 1.0F, 1.0F);
            case FLUID_CARGO -> GlStateManager.color(0.12F, 0.55F, 1.0F, 1.0F);
            case CRAFTING -> GlStateManager.color(0.95F, 0.72F, 0.18F, 1.0F);
            case ADVANCED_NAVIGATION -> GlStateManager.color(0.18F, 0.95F, 0.88F, 1.0F);
            case EU_INTERFACE -> GlStateManager.color(1.0F, 0.86F, 0.12F, 1.0F);
            case TOOL_ARM -> GlStateManager.color(0.72F, 0.76F, 0.82F, 1.0F);
            case ENTITY_SCANNER -> GlStateManager.color(0.15F, 0.95F, 0.72F, 1.0F);
            case COMBAT -> GlStateManager.color(0.9F, 0.12F, 0.08F, 1.0F);
            case ENTITY_CONTAINMENT -> GlStateManager.color(0.18F, 0.62F, 1.0F, 1.0F);
            case WATERPROOF -> GlStateManager.color(0.1F, 0.55F, 0.95F, 1.0F);
            case SELF_REPAIR -> GlStateManager.color(0.25F, 1.0F, 0.45F, 1.0F);
            case SECURE_ACCESS -> GlStateManager.color(1.0F, 0.78F, 0.12F, 1.0F);
            case ADVANCED_ITEM_HANDLING -> GlStateManager.color(0.95F, 0.48F, 0.12F, 1.0F);
            case FLEET_COMMUNICATION -> GlStateManager.color(0.35F, 0.82F, 1.0F, 1.0F);
            case FISHING -> GlStateManager.color(0.18F, 0.72F, 0.95F, 1.0F);
            case THAUMCRAFT_ALCHEMY -> GlStateManager.color(0.62F, 0.24F, 0.92F, 1.0F);
        }
    }

    private static void setStatusColor(EntityProgrammableDrone entity) {
        int override = entity.getVisualStatusLightMode();
        if (override == 4) {
            GlStateManager.color(0.05F, 0.05F, 0.05F, 1.0F);
            return;
        }
        if (override == 1) {
            GlStateManager.color(0.1F, 1.0F, 0.2F, 1.0F);
            return;
        }
        if (override == 2) {
            GlStateManager.color(1.0F, 0.78F, 0.05F, 1.0F);
            return;
        }
        if (override == 3) {
            GlStateManager.color(1.0F, 0.08F, 0.05F, 1.0F);
            return;
        }
        DroneSafetyState safety = entity.getVisualSafetyState();
        if (safety == DroneSafetyState.RETURNING) {
            float pulse = entity.ticksExisted % 12 < 6 ? 1.0F : 0.35F;
            GlStateManager.color(1.0F, 0.72F * pulse, 0.05F, 1.0F);
            return;
        }
        if (safety == DroneSafetyState.CHARGING || safety == DroneSafetyState.RECOVERING) {
            float pulse = entity.ticksExisted % 16 < 8 ? 1.0F : 0.45F;
            GlStateManager.color(0.08F, 0.75F * pulse, 1.0F, 1.0F);
            return;
        }
        if (safety == DroneSafetyState.SAFE_IDLE) {
            GlStateManager.color(1.0F, 0.38F, 0.05F, 1.0F);
            return;
        }
        if (safety == DroneSafetyState.EMERGENCY_LAND || entity.getEnergyPercent() <= 10) {
            float pulse = entity.ticksExisted % 10 < 5 ? 1.0F : 0.25F;
            GlStateManager.color(1.0F, 0.25F * pulse, 0.05F, 1.0F);
            return;
        }
        DroneRuntimeStatus status = entity.getVisualRuntimeStatus();
        switch (status) {
            case RUNNING -> GlStateManager.color(0.1F, 0.9F, 1.0F, 1.0F);
            case PAUSED -> GlStateManager.color(0.75F, 0.25F, 1.0F, 1.0F);
            case COMPLETED -> GlStateManager.color(0.2F, 1.0F, 0.35F, 1.0F);
            case ERROR -> GlStateManager.color(1.0F, 0.08F, 0.05F, 1.0F);
            default -> GlStateManager.color(0.45F, 0.5F, 0.55F, 1.0F);
        }
    }
}
