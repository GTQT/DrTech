package com.drppp.drtech.Mixin.gregtech;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.client.particle.IMachineParticleEffect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(SimpleMachineMetaTileEntity.class)
public class SimpleMachineMetaTileEntityMixin {

    @Shadow
    private EnumFacing outputFacingItems;
    @Shadow
    private EnumFacing outputFacingFluids;
    @Shadow
    private boolean autoOutputItems;
    @Shadow
    private boolean autoOutputFluids;
    @Shadow
    private boolean allowInputFromOutputSideItems;
    @Shadow
    private boolean allowInputFromOutputSideFluids;
    @Shadow
    protected IItemHandler outputItemInventory;
    @Shadow
    protected IFluidHandler outputFluidInventory;
    @Shadow
    private IItemHandlerModifiable actualImportItems;

    @Inject(method = "createGuiTemplate", at = @At("RETURN"), cancellable = true)
    public void onCreateGuiTemplate(EntityPlayer player, CallbackInfoReturnable<ModularUI.Builder> info) {

    }
}
