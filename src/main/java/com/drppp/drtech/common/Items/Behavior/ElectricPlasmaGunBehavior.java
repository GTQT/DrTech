package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.Client.Sound.SoundManager;
import com.drppp.drtech.common.Entity.EntityPlasmaBullet;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ElectricPlasmaGunBehavior implements IItemBehaviour {
    private static final long ENERGY_COST = 5000L;
    private static final long COOLDOWN_MS = 500L;

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        long now = System.currentTimeMillis();
        if (now - WeaponBehaviorUtil.getLastRightClick(stack) < COOLDOWN_MS || !WeaponBehaviorUtil.canUse(stack, ENERGY_COST)) {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }
        if (!world.isRemote && WeaponBehaviorUtil.drainEnergy(stack, ENERGY_COST)) {
            EntityPlasmaBullet entity = new EntityPlasmaBullet(world, player, 20f);
            entity.shoot(player.rotationYaw, player.rotationPitch, 1.5f);
            WeaponBehaviorUtil.moveProjectileToMuzzle(entity, player, 0.75D);
            world.spawnEntity(entity);
            world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundManager.plasma_launch, player.getSoundCategory(), 0.5f, 0.8F);
            WeaponBehaviorUtil.setLastRightClick(stack, now);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }
}
