package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.Client.Sound.SoundManager;
import com.drppp.drtech.common.Entity.EntityTachyonBullet;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AdvancedTachinoDisruptorBehavior implements IItemBehaviour {
    private static final long RANGED_COST = 50000L;
    private static final long MELEE_COST = 300L;
    private static final long COOLDOWN_MS = 250L;

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        long now = System.currentTimeMillis();
        if (now - WeaponBehaviorUtil.getLastRightClick(stack) < COOLDOWN_MS || !WeaponBehaviorUtil.canUse(stack, RANGED_COST)) {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }
        if (!world.isRemote && WeaponBehaviorUtil.drainEnergy(stack, RANGED_COST)) {
            EntityTachyonBullet entity = new EntityTachyonBullet(world, player, 120f, 600);
            entity.shoot(player.rotationYaw, player.rotationPitch, 4.0f);
            WeaponBehaviorUtil.moveProjectileToMuzzle(entity, player, 0.75D);
            world.spawnEntity(entity);
            world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundManager.laser_bullet_shoot, player.getSoundCategory(), 0.2f, 0.4F);
            WeaponBehaviorUtil.setLastRightClick(stack, now);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (!(entity instanceof EntityLivingBase target) || !WeaponBehaviorUtil.canUse(stack, MELEE_COST)) {
            return false;
        }
        if (!player.world.isRemote && WeaponBehaviorUtil.drainEnergy(stack, MELEE_COST)) {
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 30, 3));
            target.knockBack(player, 1.0f, MathHelper.sin(player.rotationYaw * 0.017453292F),
                    -MathHelper.cos(player.rotationYaw * 0.017453292F));
            target.attackEntityFrom(DamageSource.causePlayerDamage(player), 35.0F);
        }
        return true;
    }
}
