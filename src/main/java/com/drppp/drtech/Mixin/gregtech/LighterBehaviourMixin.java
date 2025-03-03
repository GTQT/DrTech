package com.drppp.drtech.Mixin.gregtech;

import com.drppp.drtech.common.MetaTileEntities.single.hu.MetaTileEntityCombustionchamber;
import com.drppp.drtech.common.MetaTileEntities.single.hu.MetaTileEntityCombustionchamberLiquid;
import gregtech.api.util.GTUtility;
import gregtech.common.items.behaviors.LighterBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LighterBehaviour.class)
public abstract class LighterBehaviourMixin {
    @Shadow
    private  boolean canOpen;
    @Shadow
    public abstract boolean consumeFuel(EntityPlayer entity, ItemStack stack);
    @Inject(method = "onItemUseFirst*", at = @At("HEAD"), cancellable = true, remap = false)
    public void onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir)
    {
        ItemStack item = player.getHeldItem(hand);
        NBTTagCompound commmm = GTUtility.getOrCreateNbtCompound(item);
        if (!player.canPlayerEdit(pos, side, player.getHeldItem(hand))) {
            cir.cancel();
        } else if ((!this.canOpen || commmm.getBoolean("lighterOpen") && !player.isSneaking()) && this.consumeFuel(player, item))
        {
            var mte =GTUtility.getMetaTileEntity(world,pos);
            if(mte!=null && mte instanceof MetaTileEntityCombustionchamber)
            {
                MetaTileEntityCombustionchamber mteg = (MetaTileEntityCombustionchamber) mte;
                mteg.setActive(true);
                cir.cancel();
            }else if(mte!=null && mte instanceof MetaTileEntityCombustionchamberLiquid)
            {
                MetaTileEntityCombustionchamberLiquid mteg = (MetaTileEntityCombustionchamberLiquid) mte;
                mteg.setActive(true);
                cir.cancel();
            }
        }
    }
}
