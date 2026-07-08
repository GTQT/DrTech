package com.drppp.drtech.hooked;

import com.drppp.drtech.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class HookClientHooks {
    public static final KeyBinding keyFire = new KeyBinding("key.drtech.fire_hook", KeyConflictContext.IN_GAME, Keyboard.KEY_C, "key.categories.drtech");
    private static final double MIN_COS = Math.cos(Math.toRadians(10.0));
    private static final Map<HookType, HookRenderer> RENDERERS = new EnumMap<>(HookType.class);
    private static int jumpTimer;
    private static boolean jumpDown;

    static {
        for (HookType type : HookType.values()) {
            RENDERERS.put(type, new HookRenderer(type));
        }
    }

    private HookClientHooks() {
    }

    public static void init() {
        ClientRegistry.registerKeyBinding(keyFire);
        MinecraftForge.EVENT_BUS.register(new HookClientHooks());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (jumpTimer > 0 && event.phase == TickEvent.Phase.END) {
            jumpTimer--;
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        HooksCap cap = HookCapability.get(player);
        if (keyFire.isPressed()) {
            if (player.isSneaking()) {
                if (cap == null) {
                    return;
                }
                retractLookedHook(player, cap);
            } else {
                HookNetwork.PacketFireHook packet = new HookNetwork.PacketFireHook(player.getPositionEyes(1f), player.getLookVec());
                packet.apply(player);
                HookNetwork.CHANNEL.sendToServer(packet);
            }
        }

        boolean wasDown = jumpDown;
        jumpDown = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
        if (!wasDown && jumpDown && cap != null) {
            if (cap.hookType != HookType.RED) {
                HookNetwork.PacketRetractHooks packet = new HookNetwork.PacketRetractHooks(true);
                packet.apply(player);
                HookNetwork.CHANNEL.sendToServer(packet);
            }
            if (jumpTimer == 0) {
                jumpTimer = 7;
            } else if (cap.hookType == HookType.RED) {
                HookNetwork.PacketRetractHooks packet = new HookNetwork.PacketRetractHooks(true);
                packet.apply(player);
                HookNetwork.CHANNEL.sendToServer(packet);
            }
        }
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        for (HookType type : HookType.values()) {
            String path = type.name().toLowerCase(Locale.ROOT);
            event.getMap().registerSprite(new ResourceLocation(Tags.MODID, "hooks/" + path + "/chain1"));
            event.getMap().registerSprite(new ResourceLocation(Tags.MODID, "hooks/" + path + "/chain2"));
            event.getMap().registerSprite(new ResourceLocation(Tags.MODID, "hooks/" + path + "/hook"));
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        if (Minecraft.getMinecraft().player != event.getEntityPlayer()) {
            render(event.getEntityPlayer(), Minecraft.getMinecraft().getRenderPartialTicks());
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) {
            return;
        }
        GlStateManager.pushMatrix();
        Vec3d lastPos = new Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
        Vec3d partialOffset = player.getPositionVector().subtract(lastPos).scale(event.getPartialTicks());
        Vec3d globalize = lastPos.add(partialOffset).scale(-1.0);
        GlStateManager.translate(globalize.x, globalize.y, globalize.z);
        if (mc.world != null) {
            for (EntityPlayer worldPlayer : mc.world.playerEntities) {
                render(worldPlayer, event.getPartialTicks());
            }
        }
        GlStateManager.popMatrix();
    }

    private void retractLookedHook(EntityPlayer player, HooksCap cap) {
        Vec3d look = player.getLookVec();
        Vec3d eye = player.getPositionEyes(1f);
        HookInfo found = null;
        double best = -1.0;
        for (HookInfo hook : cap.hooks) {
            double dot = Math.max(
                safeDot(hook.pos.subtract(eye).normalize(), look),
                safeDot(hook.pos.add(hook.direction.scale(cap.hookType != null ? cap.hookType.hookLength : 0.5)).subtract(eye).normalize(), look)
            );
            if (dot > best) {
                best = dot;
                found = hook;
            }
        }
        if (found != null && best > MIN_COS) {
            HookNetwork.PacketRetractHook packet = new HookNetwork.PacketRetractHook(found.uuid);
            packet.apply(player);
            HookNetwork.CHANNEL.sendToServer(packet);
        }
    }

    private void render(Entity entity, float partialTicks) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        HooksCap cap = HookCapability.get((EntityPlayer) entity);
        if (cap == null || cap.hookType == null) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Vec3d lastPos = new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
        Vec3d partialOffset = entity.getPositionVector().subtract(lastPos).scale(partialTicks);
        Vec3d waist = HookTickHandler.getWaistPos(entity).subtract(partialOffset);
        HookRenderer renderer = RENDERERS.get(cap.hookType);
        for (HookInfo hook : cap.hooks) {
            renderer.renderHook(waist, hook, entity.world);
        }
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private double safeDot(Vec3d a, Vec3d b) {
        if (!Double.isFinite(a.x) || !Double.isFinite(a.y) || !Double.isFinite(a.z)) {
            return -1.0;
        }
        return HookMath.dot(a, b);
    }

    static final class HookRenderer {
        private final HookType type;
        private final ResourceLocation modelLocation;
        private final ResourceLocation ropeTextureVertical;
        private final ResourceLocation ropeTextureHorizontal;

        private HookRenderer(HookType type) {
            this.type = type;
            String name = type.name().toLowerCase(Locale.ROOT);
            this.modelLocation = new ResourceLocation(Tags.MODID, "hook/" + name);
            this.ropeTextureVertical = new ResourceLocation(Tags.MODID, "textures/hooks/" + name + "/chain1.png");
            this.ropeTextureHorizontal = new ResourceLocation(Tags.MODID, "textures/hooks/" + name + "/chain2.png");
        }

        private IBakedModel getModel() {
            net.minecraftforge.client.model.IModel model = ModelLoaderRegistry.getModelOrMissing(modelLocation);
            return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        }

        private float signAngle(Vec3d a, Vec3d b, Vec3d n) {
            Vec3d cross = HookMath.cross(a, b);
            double s = cross.length();
            double c = HookMath.dot(a, b);
            float angle = (float) Math.toDegrees(MathHelper.atan2(s, c));
            if (n != null && HookMath.dot(n, cross) < 0) {
                angle = -angle;
            }
            return angle;
        }

        private void renderHook(Vec3d waist, HookInfo hook, World world) {
            Minecraft.getMinecraft().entityRenderer.enableLightmap();
            GlStateManager.pushMatrix();
            float rY = signAngle(new Vec3d(0, 0, 1), new Vec3d(hook.direction.x, 0, hook.direction.z).normalize(), new Vec3d(0, 1, 0));
            float rX = signAngle(new Vec3d(0, 1, 0), hook.direction, null);
            GlStateManager.translate(hook.pos.x, hook.pos.y, hook.pos.z);
            GlStateManager.rotate(rY, 0f, 1f, 0f);
            GlStateManager.rotate(rX, 1f, 0f, 0f);
            GlStateManager.translate(-0.5, 0.0, -0.5);
            BlockPos hookLightPos = new BlockPos(hook.pos.add(hook.direction.scale(type.hookLength / 2)));
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            int lightmap = world.getBlockState(hookLightPos).getPackedLightmapCoords(world, hookLightPos);
            BufferBuilder vb = Tessellator.getInstance().getBuffer();
            vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            for (net.minecraft.client.renderer.block.model.BakedQuad quad : getModel().getQuads(null, null, 0)) {
                vb.addVertexData(quad.getVertexData());
                vb.putBrightness4(lightmap, lightmap, lightmap, lightmap);
            }
            Tessellator.getInstance().draw();
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            Vec3d delta = hook.pos.subtract(waist);
            double distance = delta.length();
            if (distance > 0.0) {
                Vec3d normal = delta.scale(1.0 / distance);
                rY = signAngle(new Vec3d(0, 0, 1), new Vec3d(normal.x, 0, normal.z).normalize(), new Vec3d(0, 1, 0));
                rX = signAngle(new Vec3d(0, 1, 0), normal, null);
                GlStateManager.translate(waist.x, waist.y, waist.z);
                GlStateManager.rotate(rY, 0f, 1f, 0f);
                GlStateManager.rotate(rX, 1f, 0f, 0f);
                GlStateManager.rotate(45f, 0f, 1f, 0f);
                Minecraft.getMinecraft().renderEngine.bindTexture(ropeTextureVertical);
                chain(distance, waist, normal, new Vec3d(0.5, 0.0, 0.0), world);
                Minecraft.getMinecraft().renderEngine.bindTexture(ropeTextureHorizontal);
                chain(distance, waist, normal, new Vec3d(0.0, 0.0, 0.5), world);
            }
            GlStateManager.popMatrix();
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
        }

        private void chain(double distance, Vec3d waist, Vec3d normal, Vec3d offset, World world) {
            double buffer = type == HookType.RED ? 1.5 : 0.0;
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder vb = tess.getBuffer();
            vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
            double len = distance;
            while (len > buffer) {
                if (len > buffer + 1) {
                    len -= 1.0;
                    BlockPos blockPos = new BlockPos(waist.add(normal.scale(len + 0.5)));
                    chainQuad(blockPos, len, offset, 1.0, world, vb);
                } else {
                    BlockPos blockPos = new BlockPos(waist.add(normal.scale(len / 2.0)));
                    chainQuad(blockPos, buffer, offset, len - buffer, world, vb);
                    len = 0.0;
                }
            }
            tess.draw();
        }

        private void chainQuad(BlockPos blockPos, double distance, Vec3d offset, double length, World world, BufferBuilder vb) {
            int lightmap = world.getBlockState(blockPos).getPackedLightmapCoords(world, blockPos);
            int skyLight = lightmap >> 16 & 0xFFFF;
            int blockLight = lightmap & 0xFFFF;
            float b = 1f;
            double begin = distance;
            double end = distance + length;
            vb.pos(offset.x, begin, offset.z).tex(0.0, length).lightmap(skyLight, blockLight).color(b, b, b, 1f).endVertex();
            vb.pos(-offset.x, begin, -offset.z).tex(1.0, length).lightmap(skyLight, blockLight).color(b, b, b, 1f).endVertex();
            vb.pos(-offset.x, end, -offset.z).tex(1.0, 0.0).lightmap(skyLight, blockLight).color(b, b, b, 1f).endVertex();
            vb.pos(offset.x, end, offset.z).tex(0.0, 0.0).lightmap(skyLight, blockLight).color(b, b, b, 1f).endVertex();
            vb.pos(offset.x, end, offset.z).tex(0.0, 0.0).lightmap(skyLight, blockLight).color(b, b, b, 1f).endVertex();
            vb.pos(-offset.x, end, -offset.z).tex(1.0, 0.0).lightmap(skyLight, blockLight).color(b, b, b, 1f).endVertex();
            vb.pos(-offset.x, begin, -offset.z).tex(1.0, length).lightmap(skyLight, blockLight).color(b, b, b, 1f).endVertex();
            vb.pos(offset.x, begin, offset.z).tex(0.0, length).lightmap(skyLight, blockLight).color(b, b, b, 1f).endVertex();
        }
    }
}
