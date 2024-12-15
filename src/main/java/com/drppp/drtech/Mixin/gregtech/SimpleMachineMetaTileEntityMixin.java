package com.drppp.drtech.Mixin.gregtech;

import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IGhostSlotConfigurable;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(SimpleMachineMetaTileEntity.class)
public abstract class SimpleMachineMetaTileEntityMixin extends WorkableTieredMetaTileEntity implements IActiveOutputSide, IGhostSlotConfigurable {

    public SimpleMachineMetaTileEntityMixin(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
    }

    @Inject(method = "createGuiTemplate", at = @At("RETURN"), cancellable = true)
    public void onCreateGuiTemplate(EntityPlayer player, CallbackInfoReturnable<ModularUI.Builder> info) {

    }

    private void onButtonClick(Widget.ClickData clickData) {

    }


}
