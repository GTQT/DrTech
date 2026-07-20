package com.drppp.drtech.wings;

import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class WingsFlightHandler {
    private static final float MIN_SPEED = 0.03F;
    private static final float MAX_SPEED = 0.0715F;
    private static final float Y_BOOST = 0.05F;
    private static final float FALL_REDUCTION = 0.9F;
    private static final float PITCH_OFFSET = 30.0F;

    private WingsFlightHandler() {
    }

    public static ItemStack getWings(EntityPlayer player) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return chest.getItem() instanceof ItemWings ? chest : WingsBaublesCompat.findWings(player);
    }

    public static boolean canFly(EntityPlayer player) {
        ItemStack wings = getWings(player);
        return !wings.isEmpty()
                && (!wings.isItemStackDamageable() || wings.getItemDamage() < wings.getMaxDamage() - 1)
                && player.getFoodStats().getFoodLevel() >= 7;
    }

    private static boolean canLand(EntityPlayer player) {
        return !getWings(player).isEmpty() && player.getFoodStats().getFoodLevel() >= 2;
    }

    public static void setFlying(EntityPlayer player, boolean flying) {
        WingsFlightData data = WingsFlightCapability.get(player);
        if (data == null) {
            return;
        }
        boolean allowed = flying && canFly(player);
        if (data.isFlying() == allowed) {
            return;
        }
        data.setFlying(allowed);
        if (!player.capabilities.isCreativeMode) {
            player.capabilities.allowFlying = allowed;
            player.capabilities.isFlying = allowed;
            if (allowed && player.onGround) {
                // Wings originally delegates the no-gravity state to its core patch.
                // Use the vanilla flight state here and lift off once so R works from level ground.
                player.motionY = Math.max(player.motionY, 0.15D);
                player.onGround = false;
            }
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
            }
        }
        WingsNetwork.sync(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayer player = event.player;
        WingsFlightData data = WingsFlightCapability.get(player);
        if (data == null) {
            return;
        }

        data.setPreviousTimeFlying(data.getTimeFlying());
        ItemStack wings = getWings(player);
        if (wings.isEmpty()) {
            if (!player.world.isRemote && data.isFlying()) {
                setFlying(player, false);
            }
            updateAnimation(data);
            return;
        }

        if (!player.world.isRemote) {
            if (data.isFlying() && !canFly(player)) {
                setFlying(player, false);
            }
            if (data.isFlying()) {
                applyFlightMotion(player);
                if (data.incrementDurabilityTimer() >= 20) {
                    data.resetDurabilityTimer();
                    wings.damageItem(1, player);
                }
            } else {
                data.resetDurabilityTimer();
            }
            if (canLand(player)) {
                if (player.motionY < 0.0D) {
                    player.motionY *= FALL_REDUCTION;
                }
                player.fallDistance = 0.0F;
            }
            // Match Wings: leave enough transition time to launch before a grounded player can land.
            if (data.isFlying() && data.getTimeFlying() >= 20 && player.onGround) {
                setFlying(player, false);
            }
        }
        updateAnimation(data);
    }

    private static void updateAnimation(WingsFlightData data) {
        if (data.isFlying()) {
            data.setTimeFlying(Math.min(20, data.getTimeFlying() + 1));
        } else {
            data.setTimeFlying(Math.max(0, data.getTimeFlying() - 1));
        }
    }

    private static void applyFlightMotion(EntityPlayer player) {
        float speed = (float) MathHelper.clampedLerp(MIN_SPEED, MAX_SPEED, player.moveForward);
        float elevationBoost = transform(Math.abs(player.rotationPitch), 45.0F, 90.0F, 1.0F, 0.0F);
        float pitch = (float) Math.toRadians(-(player.rotationPitch - PITCH_OFFSET * elevationBoost));
        float yaw = (float) Math.toRadians(-player.rotationYaw) - (float) Math.PI;
        float horizontal = -MathHelper.cos(pitch);
        float vertical = MathHelper.sin(pitch);
        float motionZ = MathHelper.cos(yaw);
        float motionX = MathHelper.sin(yaw);
        player.motionX += motionX * horizontal * speed;
        player.motionY += vertical * speed + Y_BOOST * (player.rotationPitch > 0.0F ? elevationBoost : 1.0F);
        player.motionZ += motionZ * horizontal * speed;
        int distance = Math.round((float) Math.sqrt(player.motionX * player.motionX + player.motionY * player.motionY
                + player.motionZ * player.motionZ) * 100.0F);
        if (distance > 0) {
            player.addExhaustion(distance * 0.001F);
        }
    }

    private static float transform(float value, float fromMin, float fromMax, float toMin, float toMax) {
        float progress = MathHelper.clamp((value - fromMin) / (fromMax - fromMin), 0.0F, 1.0F);
        return toMin + (toMax - toMin) * progress;
    }

    @SubscribeEvent
    public static void onBottleBatBlood(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());
        if (!(event.getTarget() instanceof EntityBat) || stack.getItem() != Items.GLASS_BOTTLE) {
            return;
        }
        player.world.playSound(player, player.posX, player.posY, player.posZ,
                net.minecraft.init.SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        ItemStack destroyed = stack.copy();
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        StatBase stat = StatList.getObjectUseStats(Items.GLASS_BOTTLE);
        if (stat != null) {
            player.addStat(stat);
        }
        ItemStack batBlood = new ItemStack(ItemsInit.BAT_BLOOD);
        if (stack.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem(player, destroyed, event.getHand());
            player.setHeldItem(event.getHand(), batBlood);
        } else if (!player.inventory.addItemStackToInventory(batBlood)) {
            player.dropItem(batBlood, false);
        }
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        WingsNetwork.sync(event.player);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityPlayer && event.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayer target = (EntityPlayer) event.getTarget();
            WingsFlightData data = WingsFlightCapability.get(target);
            if (data != null) {
                WingsNetwork.CHANNEL.sendTo(new WingsNetwork.SyncPacket(target.getEntityId(), data.isFlying(), data.getTimeFlying()),
                        (EntityPlayerMP) event.getEntityPlayer());
            }
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote || !(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        WingsFlightData data = WingsFlightCapability.get((EntityPlayer) event.getEntity());
        if (data != null) {
            data.setPreviousTimeFlying(data.getTimeFlying());
        }
    }
}
