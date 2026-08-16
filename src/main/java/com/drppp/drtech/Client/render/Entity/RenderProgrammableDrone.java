package com.drppp.drtech.Client.render.Entity;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.common.drone.firmware.DroneSafetyState;
import com.drppp.drtech.common.drone.program.runtime.DroneRuntimeStatus;
import com.drppp.drtech.common.drone.hardware.DroneChassisTier;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeType;
import com.drppp.drtech.Client.render.Items.LightsaberRenderHelper;
import com.drppp.drtech.Client.render.Items.RenderItemDoubleLightsaber;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.Items.lightsaber.ItemDoubleLightsaber;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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
        GlStateManager.disableLighting();
        float previousX = OpenGlHelper.lastBrightnessX;
        float previousY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        setStatusColor(entity);
        droneModel.renderStatusLights(scaleFactor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, previousX, previousY);
        GlStateManager.enableLighting();
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
            case ENTITY_SCANNER -> GlStateManager.color(0.15F, 0.95F, 0.72F, 1.0F);
            case COMBAT -> GlStateManager.color(0.9F, 0.12F, 0.08F, 1.0F);
            case ENTITY_CONTAINMENT -> GlStateManager.color(0.18F, 0.62F, 1.0F, 1.0F);
            case WATERPROOF -> GlStateManager.color(0.1F, 0.55F, 0.95F, 1.0F);
            case SELF_REPAIR -> GlStateManager.color(0.25F, 1.0F, 0.45F, 1.0F);
            case SECURE_ACCESS -> GlStateManager.color(1.0F, 0.78F, 0.12F, 1.0F);
            case ADVANCED_ITEM_HANDLING -> GlStateManager.color(0.95F, 0.48F, 0.12F, 1.0F);
            case FLEET_COMMUNICATION -> GlStateManager.color(0.35F, 0.82F, 1.0F, 1.0F);
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
