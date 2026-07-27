package com.drppp.drtech.Client.render.glider;

import com.drppp.drtech.Tags;
import com.drppp.drtech.glider.GliderFlightCapability;
import com.drppp.drtech.glider.GliderFlightData;
import com.drppp.drtech.glider.GliderFlightHandler;
import com.drppp.drtech.glider.GliderNetwork;
import com.drppp.drtech.glider.ItemHangGlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class GliderClientHandler {
    private static final ModelGlider MODEL = new ModelGlider();
    private static final Set<RenderPlayer> LAYERED = Collections.newSetFromMap(new IdentityHashMap<RenderPlayer, Boolean>());
    private static final Set<Integer> HORIZONTAL_PLAYERS = new java.util.HashSet<>();

    private GliderClientHandler() {
    }

    public static void initRenderLayers() {
        for (RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            if (LAYERED.add(renderer)) {
                renderer.addLayer(new LayerGlider(renderer));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!GliderFlightHandler.isDeployed(player) || !GliderFlightHandler.shouldBeGliding(player)
                || Minecraft.getMinecraft().currentScreen instanceof GuiInventory) {
            return;
        }
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
        GlStateManager.pushMatrix();
        GlStateManager.translate(event.getX(), event.getY(), event.getZ());
        GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, player.height / 2.0F, 0.0F);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, -player.height / 2.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());
        HORIZONTAL_PLAYERS.add(player.getEntityId());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (HORIZONTAL_PLAYERS.remove(event.getEntityPlayer().getEntityId())) {
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public static void onWorldRender(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (minecraft.gameSettings.thirdPersonView != 0 || player == null || !GliderFlightHandler.isDeployed(player)
                || !GliderFlightHandler.shouldBeGliding(player)) {
            return;
        }
        ItemStack stack = GliderFlightHandler.getGlider(player);
        if (!(stack.getItem() instanceof ItemHangGlider)) {
            return;
        }
        minecraft.getTextureManager().bindTexture(LayerGlider.texture(stack));
        GlStateManager.pushMatrix();
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getPartialTicks();
        GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, 1.9F, player.isSneaking() ? -0.05F : 0.0F);
        int brightness = player.getBrightnessForRender();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightness % 65536, brightness / 65536);
        minecraft.entityRenderer.enableLightmap();
        MODEL.render(player, player.limbSwing, player.limbSwingAmount, player.ticksExisted,
                player.rotationYawHead, player.rotationPitch, 1.0F);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void onHandRender(RenderSpecificHandEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && GliderFlightHandler.isDeployed(player)) {
            ItemStack held = player.getHeldItemMainhand();
            if (held.getItem() instanceof ItemHangGlider && event.getHand() == EnumHand.OFF_HAND) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouse(MouseEvent event) {
        if (event.getDwheel() == 0) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        GliderFlightData data = player == null ? null : GliderFlightCapability.get(player);
        if (data != null && data.isDeployed()) {
            data.setDeployed(false);
            GliderNetwork.CHANNEL.sendToServer(new GliderNetwork.TogglePacket(false));
        }
    }
}
