package com.drppp.drtech.Mixin.gtfo;

import com.drppp.drtech.Tile.TileCropStick;
import gregtechfoodoption.machines.farmer.MetaTileEntityFarmer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MetaTileEntityFarmer.class)
public abstract  class MixinMetaTileEntityFarmer {
    @Shadow
    private BlockPos.MutableBlockPos operationPosition;
    @Inject(
            method = "isCropSpaceEmpty",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onIsCropSpaceEmpty(CallbackInfoReturnable<Boolean> cir)
    {
        MetaTileEntityFarmer farmer = (MetaTileEntityFarmer)(Object)this;
        var tile = farmer.getWorld().getTileEntity(operationPosition);
        if(tile!=null && tile instanceof TileCropStick)
        {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
