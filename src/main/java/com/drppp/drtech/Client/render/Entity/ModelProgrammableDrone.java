package com.drppp.drtech.Client.render.Entity;

import com.drppp.drtech.common.drone.entity.EntityProgrammableDrone;
import com.drppp.drtech.common.drone.hardware.DroneUpgradeType;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/** Blocky cuboid drone model derived from the original DrTech concept sheet. */
public final class ModelProgrammableDrone extends ModelBase {

    private final ModelRenderer root;
    private final ModelRenderer[] rotors = new ModelRenderer[4];
    private final ModelRenderer leftLight;
    private final ModelRenderer rightLight;
    private final ModelRenderer toolArm;
    private final ModelRenderer batteryModule;
    private final ModelRenderer propulsionModule;
    private final ModelRenderer efficiencyModule;
    private final ModelRenderer cargoModule;
    private final ModelRenderer wirelessModule;
    private final ModelRenderer fluidCargoModule;
    private final ModelRenderer craftingModule;
    private final ModelRenderer navigationModule;
    private final ModelRenderer euInterfaceModule;
    private final ModelRenderer entityScannerModule;
    private final ModelRenderer combatModule;
    private final ModelRenderer containmentModule;
    private final ModelRenderer waterproofModule;
    private final ModelRenderer selfRepairModule;
    private final ModelRenderer secureAccessModule;
    private final ModelRenderer advancedItemModule;
    private final ModelRenderer fleetCommunicationModule;
    private final ModelRenderer fishingModule;
    private final ModelRenderer alchemyModule;
    private final ModelRenderer cargoStatusLight;

    public ModelProgrammableDrone() {
        textureWidth = 64;
        textureHeight = 64;
        root = new ModelRenderer(this, 0, 0);
        root.addBox(-5.0F, -3.0F, -6.0F, 10, 5, 12);
        root.setRotationPoint(0.0F, 20.0F, 0.0F);

        ModelRenderer topPanel = new ModelRenderer(this, 0, 18);
        topPanel.addBox(-3.0F, -4.0F, -4.0F, 6, 1, 8);
        root.addChild(topPanel);

        ModelRenderer sensor = new ModelRenderer(this, 30, 18);
        sensor.addBox(-2.0F, -1.5F, -7.0F, 4, 3, 1);
        root.addChild(sensor);

        ModelRenderer horizontalArm = new ModelRenderer(this, 0, 28);
        horizontalArm.addBox(-10.0F, -1.0F, -1.0F, 20, 1, 2);
        root.addChild(horizontalArm);
        ModelRenderer verticalArm = new ModelRenderer(this, 0, 28);
        verticalArm.addBox(-1.0F, -1.0F, -10.0F, 2, 1, 20);
        root.addChild(verticalArm);

        addRotor(0, -8.0F, -1.5F, -8.0F);
        addRotor(1, 8.0F, -1.5F, -8.0F);
        addRotor(2, -8.0F, -1.5F, 8.0F);
        addRotor(3, 8.0F, -1.5F, 8.0F);

        leftLight = new ModelRenderer(this, 60, 0);
        leftLight.addBox(-4.4F, -1.6F, -7.15F, 1, 1, 1);
        leftLight.setRotationPoint(0.0F, 20.0F, 0.0F);
        rightLight = new ModelRenderer(this, 60, 0);
        rightLight.addBox(3.4F, -1.6F, -7.15F, 1, 1, 1);
        rightLight.setRotationPoint(0.0F, 20.0F, 0.0F);

        toolArm = new ModelRenderer(this, 48, 12);
        toolArm.addBox(-1.0F, 2.0F, -1.0F, 2, 4, 2);
        ModelRenderer clawLeft = new ModelRenderer(this, 56, 12);
        clawLeft.addBox(-2.0F, 5.5F, -1.0F, 1, 3, 1);
        ModelRenderer clawRight = new ModelRenderer(this, 56, 12);
        clawRight.addBox(1.0F, 5.5F, -1.0F, 1, 3, 1);
        toolArm.addChild(clawLeft);
        toolArm.addChild(clawRight);
        root.addChild(toolArm);

        batteryModule = moduleRoot();
        batteryModule.addBox(-3.5F, 2.1F, -3.0F, 7, 2, 6);
        ModelRenderer batteryCell = new ModelRenderer(this, 48, 20);
        batteryCell.addBox(-2.5F, 4.0F, -2.0F, 5, 1, 4);
        batteryModule.addChild(batteryCell);

        propulsionModule = moduleRoot();
        addThruster(propulsionModule, -4.5F, 0.5F, 4.0F);
        addThruster(propulsionModule, 3.0F, 0.5F, 4.0F);

        efficiencyModule = moduleRoot();
        efficiencyModule.addBox(-2.0F, -5.0F, -2.0F, 4, 1, 4);
        ModelRenderer chipCore = new ModelRenderer(this, 60, 4);
        chipCore.addBox(-0.5F, -5.4F, -0.5F, 1, 1, 1);
        efficiencyModule.addChild(chipCore);

        cargoModule = moduleRoot();
        cargoModule.addBox(-6.8F, -1.0F, -3.0F, 2, 4, 6);
        cargoModule.addBox(4.8F, -1.0F, -3.0F, 2, 4, 6);

        wirelessModule = moduleRoot();
        wirelessModule.addBox(-0.5F, -8.0F, 2.0F, 1, 4, 1);
        ModelRenderer antennaTip = new ModelRenderer(this, 60, 4);
        antennaTip.addBox(-1.0F, -9.0F, 1.5F, 2, 1, 2);
        wirelessModule.addChild(antennaTip);

        fluidCargoModule = moduleRoot();
        fluidCargoModule.addBox(-6.3F, 0.0F, -2.0F, 2, 3, 4);
        fluidCargoModule.addBox(4.3F, 0.0F, -2.0F, 2, 3, 4);

        craftingModule = moduleRoot();
        craftingModule.addBox(-3.0F, 2.0F, -4.5F, 6, 2, 2);
        craftingModule.addBox(-1.0F, 4.0F, -3.8F, 2, 1, 1);

        navigationModule = moduleRoot();
        navigationModule.addBox(-2.0F, -5.4F, -4.0F, 4, 1, 4);
        ModelRenderer navigationDish = new ModelRenderer(this, 60, 4);
        navigationDish.addBox(-1.0F, -6.4F, -3.0F, 2, 1, 2);
        navigationModule.addChild(navigationDish);

        euInterfaceModule = moduleRoot();
        euInterfaceModule.addBox(-5.8F, 1.0F, 2.2F, 3, 3, 3);
        euInterfaceModule.addBox(2.8F, 1.0F, 2.2F, 3, 3, 3);

        entityScannerModule = moduleRoot();
        entityScannerModule.addBox(-2.5F, -5.4F, -6.2F, 5, 2, 2);
        ModelRenderer scannerEye = new ModelRenderer(this, 60, 4);
        scannerEye.addBox(-1.0F, -5.9F, -6.8F, 2, 1, 1);
        entityScannerModule.addChild(scannerEye);

        combatModule = moduleRoot();
        combatModule.addBox(-4.5F, 1.8F, -1.5F, 2, 2, 3);
        combatModule.addBox(2.5F, 1.8F, -1.5F, 2, 2, 3);

        containmentModule = moduleRoot();
        containmentModule.addBox(-3.5F, 2.0F, 1.8F, 7, 4, 3);
        ModelRenderer containmentCore = new ModelRenderer(this, 60, 4);
        containmentCore.addBox(-1.0F, 3.0F, 1.2F, 2, 2, 1);
        containmentModule.addChild(containmentCore);

        waterproofModule = moduleRoot();
        waterproofModule.addBox(-5.4F, -3.4F, -6.4F, 11, 1, 13);

        selfRepairModule = moduleRoot();
        selfRepairModule.addBox(-1.5F, -5.2F, 2.0F, 3, 2, 4);
        ModelRenderer repairArm = new ModelRenderer(this, 56, 12);
        repairArm.addBox(-0.5F, -7.0F, 3.0F, 1, 2, 1);
        selfRepairModule.addChild(repairArm);

        secureAccessModule = moduleRoot();
        secureAccessModule.addBox(-1.8F, -4.8F, -1.8F, 4, 1, 4);
        ModelRenderer securityCore = new ModelRenderer(this, 60, 4);
        securityCore.addBox(-0.5F, -5.5F, -0.5F, 1, 1, 1);
        secureAccessModule.addChild(securityCore);

        advancedItemModule = moduleRoot();
        advancedItemModule.addBox(-6.5F, -1.0F, 1.5F, 2, 4, 4);
        advancedItemModule.addBox(4.5F, -1.0F, 1.5F, 2, 4, 4);

        fleetCommunicationModule = moduleRoot();
        fleetCommunicationModule.addBox(-3.5F, -5.2F, 3.5F, 7, 1, 2);
        ModelRenderer fleetAntenna = new ModelRenderer(this, 60, 4);
        fleetAntenna.addBox(-0.5F, -9.0F, 4.0F, 1, 4, 1);
        fleetCommunicationModule.addChild(fleetAntenna);

        fishingModule = moduleRoot();
        fishingModule.addBox(-2.0F, 2.0F, -4.8F, 4, 2, 2);
        ModelRenderer fishingReel = new ModelRenderer(this, 60, 4);
        fishingReel.addBox(-1.0F, 3.5F, -5.0F, 2, 2, 1);
        fishingModule.addChild(fishingReel);

        alchemyModule = moduleRoot();
        alchemyModule.addBox(-2.5F, 2.0F, 3.8F, 5, 2, 2);
        ModelRenderer alchemyCoil = new ModelRenderer(this, 60, 4);
        alchemyCoil.addBox(-1.0F, 3.5F, 4.0F, 2, 2, 1);
        alchemyModule.addChild(alchemyCoil);

        cargoStatusLight = new ModelRenderer(this, 60, 0);
        cargoStatusLight.addBox(-1.5F, 0.0F, 6.05F, 3, 1, 1);
        cargoStatusLight.setRotationPoint(0.0F, 20.0F, 0.0F);
    }

    private ModelRenderer moduleRoot() {
        ModelRenderer module = new ModelRenderer(this, 40, 20);
        module.setRotationPoint(0.0F, 20.0F, 0.0F);
        return module;
    }

    private void addThruster(ModelRenderer parent, float x, float y, float z) {
        ModelRenderer thruster = new ModelRenderer(this, 48, 28);
        thruster.addBox(x, y, z, 2, 3, 2);
        parent.addChild(thruster);
    }

    private void addRotor(int index, float x, float y, float z) {
        ModelRenderer housing = new ModelRenderer(this, 0, 32);
        housing.setRotationPoint(x, y, z);
        housing.addBox(-4.0F, -1.0F, -4.0F, 8, 2, 8);
        ModelRenderer rotor = new ModelRenderer(this, 32, 32);
        rotor.addBox(-3.5F, -1.2F, -0.5F, 7, 1, 1);
        ModelRenderer blade = new ModelRenderer(this, 32, 35);
        blade.addBox(-0.5F, -1.2F, -3.5F, 1, 1, 7);
        rotor.addChild(blade);
        housing.addChild(rotor);
        root.addChild(housing);
        rotors[index] = rotor;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        root.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        float energyFactor = entity instanceof EntityProgrammableDrone drone && !drone.hasVisualRotorPower()
                ? 0.0F : 1.0F;
        if (entity instanceof EntityProgrammableDrone drone && !drone.areVisualRotorsActive()) energyFactor = 0.0F;
        float propulsionFactor = entity instanceof EntityProgrammableDrone drone
                && drone.hasVisualUpgrade(DroneUpgradeType.PROPULSION) ? 1.35F : 1.0F;
        for (int i = 0; i < rotors.length; i++) {
            rotors[i].rotateAngleY = ageInTicks * (1.4F + i * 0.08F) * energyFactor * propulsionFactor;
        }
        root.rotateAngleZ = limbSwingAmount * 0.08F;
        float attack = entity instanceof EntityProgrammableDrone drone
                ? drone.getAttackAnimationProgress(ageInTicks - entity.ticksExisted) : 0.0F;
        toolArm.showModel = entity instanceof EntityProgrammableDrone drone
                && (drone.hasVisualUpgrade(DroneUpgradeType.TOOL_ARM)
                || drone.hasVisualUpgrade(DroneUpgradeType.COMBAT)
                || drone.hasVisualUpgrade(DroneUpgradeType.FISHING)
                || drone.hasVisualUpgrade(DroneUpgradeType.THAUMCRAFT_ALCHEMY));
        float fishingPull = entity instanceof EntityProgrammableDrone drone
                ? drone.getFishingPullProgress(ageInTicks - entity.ticksExisted) : 0.0F;
        float alchemyMove = entity instanceof EntityProgrammableDrone drone
                ? drone.getAlchemyAnimationProgress(ageInTicks - entity.ticksExisted) : 0.0F;
        toolArm.rotateAngleX = (float) Math.sin(ageInTicks * 0.15F) * 0.04F
                - attack * 1.15F - fishingPull * 0.85F - alchemyMove * 0.45F;
    }

    public void renderStatusLights(float scale) {
        leftLight.render(scale);
        rightLight.render(scale);
    }

    public void renderUpgrade(DroneUpgradeType type, float scale) {
        switch (type) {
            case BATTERY -> batteryModule.render(scale);
            case PROPULSION -> propulsionModule.render(scale);
            case EFFICIENCY -> efficiencyModule.render(scale);
            case CARGO -> cargoModule.render(scale);
            case WIRELESS -> wirelessModule.render(scale);
            case FLUID_CARGO -> fluidCargoModule.render(scale);
            case CRAFTING -> craftingModule.render(scale);
            case ADVANCED_NAVIGATION -> navigationModule.render(scale);
            case EU_INTERFACE -> euInterfaceModule.render(scale);
            case TOOL_ARM -> { }
            case ENTITY_SCANNER -> entityScannerModule.render(scale);
            case COMBAT -> combatModule.render(scale);
            case ENTITY_CONTAINMENT -> containmentModule.render(scale);
            case WATERPROOF -> waterproofModule.render(scale);
            case SELF_REPAIR -> selfRepairModule.render(scale);
            case SECURE_ACCESS -> secureAccessModule.render(scale);
            case ADVANCED_ITEM_HANDLING -> advancedItemModule.render(scale);
            case FLEET_COMMUNICATION -> fleetCommunicationModule.render(scale);
            case FISHING -> fishingModule.render(scale);
            case THAUMCRAFT_ALCHEMY -> alchemyModule.render(scale);
        }
    }

    public void postRenderToolArm(float scale) {
        root.postRender(scale);
        toolArm.postRender(scale);
    }

    public void renderCargoStatusLight(float scale) { cargoStatusLight.render(scale); }
}
