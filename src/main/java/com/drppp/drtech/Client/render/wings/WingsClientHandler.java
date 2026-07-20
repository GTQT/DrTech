package com.drppp.drtech.Client.render.wings;

import com.drppp.drtech.Client.audio.WingsSound;
import com.drppp.drtech.Tags;
import com.drppp.drtech.wings.ItemWings;
import com.drppp.drtech.wings.WingType;
import com.drppp.drtech.wings.WingsFlightCapability;
import com.drppp.drtech.wings.WingsFlightData;
import com.drppp.drtech.wings.WingsFlightHandler;
import com.drppp.drtech.wings.WingsNetwork;
import me.paulf.wings.client.flight.Animator;
import me.paulf.wings.client.flight.AnimatorAvian;
import me.paulf.wings.client.flight.AnimatorInsectoid;
import me.paulf.wings.client.model.ModelWingsAvian;
import me.paulf.wings.client.model.ModelWingsInsectoid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.IdentityHashMap;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public final class WingsClientHandler {
    private static final ModelWingsAvian AVIAN_MODEL = new ModelWingsAvian();
    private static final ModelWingsInsectoid INSECTOID_MODEL = new ModelWingsInsectoid();
    private static final Map<Integer, AnimatorState> ANIMATORS = new HashMap<>();
    private static final Set<RenderPlayer> LAYERED_RENDERERS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final KeyBinding TOGGLE_FLIGHT = new KeyBinding("key.drtech.wings.fly", Keyboard.KEY_R,
            "key.categories.drtech.wings");
    private static EntityPlayerSoundState soundState;

    private WingsClientHandler() {
    }

    public static void init() {
        ClientRegistry.registerKeyBinding(TOGGLE_FLIGHT);
    }

    public static void initRenderLayers() {
        for (RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            if (LAYERED_RENDERERS.add(renderer)) {
                renderer.addLayer(new LayerWings(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.world == null) {
            soundState = null;
            return;
        }
        if (soundState == null || soundState.player != minecraft.player) {
            soundState = new EntityPlayerSoundState(minecraft.player);
            minecraft.getSoundHandler().playSound(soundState.sound);
        }
        while (TOGGLE_FLIGHT.isPressed()) {
            WingsFlightData data = WingsFlightCapability.get(minecraft.player);
            if (data == null || WingsFlightHandler.getWings(minecraft.player).isEmpty()) {
                continue;
            }
            boolean flying = !data.isFlying();
            if (flying && !WingsFlightHandler.canFly(minecraft.player)) {
                minecraft.player.sendStatusMessage(new TextComponentTranslation("message.drtech.wings.flight.unavailable"), true);
                continue;
            }
            // The original mod updates the local capability before asking the server to mirror it.
            data.setFlying(flying);
            minecraft.player.sendStatusMessage(new TextComponentTranslation(
                    flying ? "message.drtech.wings.flight.enabled" : "message.drtech.wings.flight.disabled"), true);
            WingsNetwork.CHANNEL.sendToServer(new WingsNetwork.TogglePacket(flying));
        }
        for (net.minecraft.entity.player.EntityPlayer player : minecraft.world.playerEntities) {
            updateAnimator(player);
        }
    }

    static void renderWings(AbstractClientPlayer player, RenderPlayer renderer, float partialTicks) {
        if (player.isInvisible()) {
            return;
        }
        ItemStack stack = WingsFlightHandler.getWings(player);
        if (!(stack.getItem() instanceof ItemWings)) {
            return;
        }
        AnimatorState state = ANIMATORS.get(player.getEntityId());
        if (state == null) {
            return;
        }
        ItemWings wings = (ItemWings) stack.getItem();
        renderer.bindTexture(new ResourceLocation("wings", "textures/entity/wings/"
                + wings.getWingType().getSerializedName() + ".png"));
        GlStateManager.pushMatrix();
        ModelBiped body = renderer.getMainModel();
        if (player.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }
        body.bipedBody.postRender(0.0625F);
        GlStateManager.enableCull();
        if (wings.getWingType().getShape() == WingType.Shape.AVIAN) {
            AVIAN_MODEL.render((AnimatorAvian) state.animator, partialTicks, 0.0625F);
        } else {
            INSECTOID_MODEL.render((AnimatorInsectoid) state.animator, partialTicks, 0.0625F);
        }
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }

    private static void updateAnimator(net.minecraft.entity.player.EntityPlayer player) {
        ItemStack stack = WingsFlightHandler.getWings(player);
        if (!(stack.getItem() instanceof ItemWings)) {
            ANIMATORS.remove(player.getEntityId());
            return;
        }
        WingType type = ((ItemWings) stack.getItem()).getWingType();
        AnimatorState state = ANIMATORS.get(player.getEntityId());
        if (state == null || state.shape != type.getShape()) {
            state = new AnimatorState(type.getShape());
            ANIMATORS.put(player.getEntityId(), state);
        }
        WingsFlightData flight = WingsFlightCapability.get(player);
        AnimationMode mode = getAnimationMode(player, flight);
        if (mode != state.mode) {
            state.setMode(mode);
        }
        state.animator.update();
    }

    private static AnimationMode getAnimationMode(net.minecraft.entity.player.EntityPlayer player, WingsFlightData flight) {
        if (flight != null && flight.isFlying()) {
            if (player.motionY > 0.08D) {
                return AnimationMode.LIFT;
            }
            return AnimationMode.GLIDE;
        }
        if (!player.onGround && player.motionY < -0.08D) {
            return AnimationMode.FALL;
        }
        return player.onGround ? AnimationMode.IDLE : AnimationMode.LAND;
    }

    private enum AnimationMode {
        IDLE,
        LIFT,
        GLIDE,
        FALL,
        LAND
    }

    private static final class AnimatorState {
        private final WingType.Shape shape;
        private final Animator animator;
        private AnimationMode mode;

        private AnimatorState(WingType.Shape shape) {
            this.shape = shape;
            animator = shape == WingType.Shape.AVIAN ? new AnimatorAvian() : new AnimatorInsectoid();
        }

        private void setMode(AnimationMode mode) {
            this.mode = mode;
            switch (mode) {
                case LIFT:
                    animator.beginLift();
                    break;
                case GLIDE:
                    animator.beginGlide();
                    break;
                case FALL:
                    animator.beginFall();
                    break;
                case LAND:
                    animator.beginLand();
                    break;
                case IDLE:
                default:
                    animator.beginIdle();
                    break;
            }
        }
    }

    private static final class EntityPlayerSoundState {
        private final net.minecraft.entity.player.EntityPlayer player;
        private final WingsSound sound;

        private EntityPlayerSoundState(net.minecraft.entity.player.EntityPlayer player) {
            this.player = player;
            sound = new WingsSound(player);
        }
    }
}
