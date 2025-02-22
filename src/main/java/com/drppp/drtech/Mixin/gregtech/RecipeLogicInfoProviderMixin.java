package com.drppp.drtech.Mixin.gregtech;

import com.drppp.drtech.api.capability.impl.RecipeLogicRU;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.PrimitiveRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import gregtech.integration.theoneprobe.provider.RecipeLogicInfoProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeLogicInfoProvider.class)
public abstract class RecipeLogicInfoProviderMixin {
    @Inject(method = "addProbeInfo*", at = @At("HEAD"), cancellable = true, remap = false
    )
    private void onAddProbeInfo(@NotNull AbstractRecipeLogic capability, @NotNull IProbeInfo probeInfo, @NotNull EntityPlayer player, @NotNull TileEntity tileEntity, @NotNull IProbeHitData data, CallbackInfo ci)
    {
        if (capability.isWorking() && capability instanceof RecipeLogicRU) {

            int EUt = capability.getInfoProviderEUt();
            int absEUt = Math.abs(EUt);
            String text = null;
            if (text == null) {
                text = TextFormatting.RED + TextFormattingUtil.formatNumbers(absEUt) + TextStyleClass.INFO + " RU" + TextFormatting.GREEN + " (" + GTValues.VNF[GTUtility.getTierByVoltage(absEUt)] + TextFormatting.GREEN + ")";
            }

            if (EUt == 0) {
                return;
            }

            if (capability.consumesEnergy()) {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_consumption*} " + text);
            } else {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.energy_production*} " + text);
            }
            ci.cancel();
        }
    }
}