package com.drppp.drtech.common.event;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.lightsaber.ItemDoubleLightsaber;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.Sound.DrTechSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class LightsaberCombatHandler {
    private LightsaberCombatHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) {
            return;
        }

        ItemStack stack = player.getHeldItemMainhand();
        boolean single = stack.getItem() instanceof ItemLightsaber && ItemLightsaber.isActive(stack);
        boolean dual = stack.getItem() instanceof ItemDoubleLightsaber && ItemDoubleLightsaber.isActive(stack);
        if (!single && !dual) {
            return;
        }

        event.setCanceled(true);
        float damage = dual
                ? ItemDoubleLightsaber.rollAttackDamage(player.getRNG())
                : ItemLightsaber.rollAttackDamage(player.getRNG());
        DamageSource source = DamageSource.causePlayerDamage(player);
        boolean hit = dual
                ? attackDoubleLightsaberTargets(player, source, damage)
                : attackTarget(event.getTarget(), player, source, damage);
        player.resetCooldown();

        if (hit) {
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    DrTechSounds.LIGHTSABER_HIT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static boolean attackDoubleLightsaberTargets(EntityPlayer player, DamageSource source, float damage) {
        List<EntityLivingBase> targets = player.world.getEntitiesWithinAABB(EntityLivingBase.class,
                player.getEntityBoundingBox().grow(ItemDoubleLightsaber.ATTACK_RANGE), entity -> entity != player);
        boolean hit = false;
        for (EntityLivingBase target : targets) {
            hit |= attackTarget(target, player, source, damage);
        }
        return hit;
    }

    private static boolean attackTarget(Entity target, EntityPlayer player, DamageSource source, float damage) {
        return target != player && target.canBeAttackedWithItem() && !target.hitByEntity(player)
                && target.attackEntityFrom(source, damage);
    }
}
