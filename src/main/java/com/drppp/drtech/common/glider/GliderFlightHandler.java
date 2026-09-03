package com.drppp.drtech.common.glider;

import com.drppp.drtech.Tags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class GliderFlightHandler {
    private GliderFlightHandler() {
    }

    public static ItemStack getGlider(EntityPlayer player) {
        return player.getHeldItemMainhand();
    }

    public static boolean isDeployed(EntityPlayer player) {
        GliderFlightData data = GliderFlightCapability.get(player);
        return data != null && data.isDeployed();
    }

    public static boolean shouldBeGliding(EntityPlayer player) {
        return !player.isDead && !player.onGround && !player.isInWater();
    }

    private static boolean isValid(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemHangGlider
                && !((ItemHangGlider) stack.getItem()).isBroken(stack);
    }

    public static void setDeployed(EntityPlayer player, boolean deployed) {
        GliderFlightData data = GliderFlightCapability.get(player);
        if (data != null) {
            data.setDeployed(deployed);
            if (!player.world.isRemote) {
                GliderNetwork.sync(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayer player = event.player;
        GliderFlightData data = GliderFlightCapability.get(player);
        if (data == null || !data.isDeployed()) {
            return;
        }
        ItemStack stack = getGlider(player);
        if (!isValid(stack)) {
            setDeployed(player, false);
            return;
        }
        if (!shouldBeGliding(player) || player.motionY >= 0.0D) {
            return;
        }

        ItemHangGlider glider = (ItemHangGlider) stack.getItem();
        GliderWind.apply(player, stack);
        player.motionY *= glider.getVerticalSpeed(player.isSneaking());
        double yaw = Math.toRadians(player.rotationYaw + 90.0F);
        double horizontalSpeed = glider.getHorizontalSpeed(player.isSneaking());
        player.motionX += MathHelper.cos((float) yaw) * horizontalSpeed;
        player.motionZ += MathHelper.sin((float) yaw) * horizontalSpeed;
        player.motionX *= glider.getAirResistance();
        player.motionZ *= glider.getAirResistance();
        player.fallDistance = 0.0F;
        if (player.world.isRemote) {
            player.limbSwing = 0.0F;
            player.limbSwingAmount = 0.0F;
        } else if (player.getRNG().nextInt(200) == 0) {
            stack.damageItem(1, player);
            if (glider.isBroken(stack)) {
                setDeployed(player, false);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        GliderNetwork.sync(event.player);
    }

    @SubscribeEvent
    public static void onTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityPlayer && event.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayer target = (EntityPlayer) event.getTarget();
            GliderFlightData data = GliderFlightCapability.get(target);
            if (data != null) {
                GliderNetwork.CHANNEL.sendTo(new GliderNetwork.SyncPacket(target.getEntityId(), data.isDeployed()),
                        (EntityPlayerMP) event.getEntityPlayer());
            }
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote || !(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        GliderFlightData data = GliderFlightCapability.get((EntityPlayer) event.getEntity());
        if (data != null && !isValid(getGlider((EntityPlayer) event.getEntity()))) {
            data.setDeployed(false);
        }
    }
}
