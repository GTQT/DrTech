package com.drppp.drtech.Mixin.gregtech;

import com.drppp.drtech.common.MetaTileEntities.single.RuMachine.MetaTileEntityRuMachine;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.integration.theoneprobe.provider.ElectricContainerInfoProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.NumberFormat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElectricContainerInfoProvider.class)
public abstract class ElectricContainerInfoProviderMixin {
    @Inject(method = "addProbeInfo*", at = @At("HEAD"), cancellable = true, remap = false)
    protected void addProbeInfo(@NotNull IEnergyContainer capability, @NotNull IProbeInfo probeInfo, EntityPlayer player, @NotNull TileEntity tileEntity, @NotNull IProbeHitData data, CallbackInfo ci) {
        if(GTUtility.getMetaTileEntity(player.getEntityWorld(),data.getPos()) instanceof MetaTileEntityRuMachine)
        {
                probeInfo.progress(capability.getEnergyStored(), capability.getEnergyStored(),
                        probeInfo.defaultProgressStyle()
                                .suffix(" / " + TextFormattingUtil.formatNumbers(capability.getEnergyStored()) + " RU")
                                .filledColor(-298000).alternateFilledColor(-298000).borderColor(-0x2BCB35)
                                .numberFormat(NumberFormat.COMMAS));
                ci.cancel();
        }

    }
}
