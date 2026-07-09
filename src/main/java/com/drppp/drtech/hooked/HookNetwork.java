package com.drppp.drtech.hooked;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.Tags;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class HookNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID + "_hooked");

    private HookNetwork() {
    }

    public static void init() {
        CHANNEL.registerMessage(FireHookHandler.class, PacketFireHook.class, 0, Side.SERVER);
        CHANNEL.registerMessage(RetractHookHandler.class, PacketRetractHook.class, 1, Side.SERVER);
        CHANNEL.registerMessage(RetractHooksHandler.class, PacketRetractHooks.class, 2, Side.SERVER);
        CHANNEL.registerMessage(UpdateWeightsHandler.class, PacketUpdateWeights.class, 3, Side.SERVER);
        if (FMLCommonHandler.instance().getSide().isClient()) {
            CHANNEL.registerMessage(SyncHooksHandler.class, PacketHookCapSync.class, 4, Side.CLIENT);
        }
    }

    public static void syncHooks(Entity entity) {
        NBTTagCompound tag = null;
        if (entity instanceof EntityPlayer) {
            HooksCap cap = HookCapability.get((EntityPlayer) entity);
            if (cap != null) {
                tag = cap.serializeNBT();
            }
        }
        PacketHookCapSync packet = new PacketHookCapSync(entity.getEntityId(), tag);
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            CHANNEL.sendTo(packet, player);
            CHANNEL.sendToAllTracking(packet, player);
        }
    }

    public static void sendWeights(HooksCap cap) {
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return;
        }
        Map<UUID, Double> weights = new LinkedHashMap<>();
        for (HookInfo hook : cap.hooks) {
            if (hook.status == EnumHookStatus.PLANTED) {
                weights.put(hook.uuid, hook.getWeight());
            }
        }
        CHANNEL.sendToServer(new PacketUpdateWeights(cap.verticalHangDistance, weights));
    }

    public static class PacketFireHook implements IMessage {
        Vec3d pos = Vec3d.ZERO;
        Vec3d normal = Vec3d.ZERO;

        public PacketFireHook() {
        }

        public PacketFireHook(Vec3d pos, Vec3d normal) {
            this.pos = pos;
            this.normal = normal;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            normal = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeDouble(pos.x);
            buf.writeDouble(pos.y);
            buf.writeDouble(pos.z);
            buf.writeDouble(normal.x);
            buf.writeDouble(normal.y);
            buf.writeDouble(normal.z);
        }

        void apply(EntityPlayer player) {
            HooksCap cap = HookCapability.get(player);
            if (cap == null) {
                return;
            }
            double spawnDistance = player.getPositionVector().distanceTo(pos);
            if (spawnDistance > 10) {
                DrTechMain.LOGGER.warn("Player {} spawned a hook too far from their body: {}", player.getName(), spawnDistance);
                cap.update(player);
            }
            HookType type = cap.hookType;
            if (type == null) {
                return;
            }
            int activeCount = 0;
            int plantedCount = 0;
            HookInfo planted = null;
            for (HookInfo hook : cap.hooks) {
                if (hook.status.active) {
                    activeCount++;
                }
                if (hook.status == EnumHookStatus.PLANTED) {
                    plantedCount++;
                    planted = hook;
                }
            }
            if (activeCount <= type.count + 1) {
                if (plantedCount == 1 && planted != null) {
                    planted.setWeight(1.0);
                }
                cap.hooks.add(new HookInfo(pos, normal.normalize(), EnumHookStatus.EXTENDING, null, null));
            }
        }
    }

    public static class FireHookHandler implements IMessageHandler<PacketFireHook, IMessage> {
        @Override
        public IMessage onMessage(PacketFireHook message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                message.apply(player);
                HooksCap cap = HookCapability.get(player);
                if (cap != null) {
                    cap.update(player);
                }
            });
            return null;
        }
    }

    public static class PacketRetractHook implements IMessage {
        UUID uuid = UUID.randomUUID();

        public PacketRetractHook() {
        }

        public PacketRetractHook(UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            uuid = new UUID(buf.readLong(), buf.readLong());
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());
        }

        void apply(EntityPlayer player) {
            HooksCap cap = HookCapability.get(player);
            if (cap == null) {
                return;
            }
            for (HookInfo hook : cap.hooks) {
                if (hook.uuid.equals(uuid)) {
                    hook.status = EnumHookStatus.TO_RETRACT;
                }
            }
            cap.updatePos();
        }
    }

    public static class RetractHookHandler implements IMessageHandler<PacketRetractHook, IMessage> {
        @Override
        public IMessage onMessage(PacketRetractHook message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                message.apply(player);
                HooksCap cap = HookCapability.get(player);
                if (cap != null) {
                    cap.update(player);
                }
            });
            return null;
        }
    }

    public static class PacketRetractHooks implements IMessage {
        boolean jumping;

        public PacketRetractHooks() {
        }

        public PacketRetractHooks(boolean jumping) {
            this.jumping = jumping;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            jumping = buf.readBoolean();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeBoolean(jumping);
        }

        void apply(EntityPlayer player) {
            HooksCap cap = HookCapability.get(player);
            if (cap == null) {
                return;
            }
            boolean planted = false;
            for (HookInfo hook : cap.hooks) {
                if (hook.status == EnumHookStatus.PLANTED) {
                    planted = true;
                    break;
                }
            }
            if (jumping && planted) {
                player.motionX *= 1.25;
                player.motionY *= 1.25;
                player.motionZ *= 1.25;
                player.jump();
            }
            for (HookInfo hook : cap.hooks) {
                hook.status = EnumHookStatus.TO_RETRACT;
            }
            cap.updatePos();
        }
    }

    public static class RetractHooksHandler implements IMessageHandler<PacketRetractHooks, IMessage> {
        @Override
        public IMessage onMessage(PacketRetractHooks message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                message.apply(player);
                HooksCap cap = HookCapability.get(player);
                if (cap != null) {
                    cap.update(player);
                }
            });
            return null;
        }
    }

    public static class PacketUpdateWeights implements IMessage {
        double vertical;
        final Map<UUID, Double> weights = new LinkedHashMap<>();

        public PacketUpdateWeights() {
        }

        public PacketUpdateWeights(double vertical, Map<UUID, Double> weights) {
            this.vertical = vertical;
            this.weights.putAll(weights);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            vertical = buf.readDouble();
            int size = buf.readInt();
            weights.clear();
            for (int i = 0; i < size; i++) {
                UUID uuid = new UUID(buf.readLong(), buf.readLong());
                weights.put(uuid, buf.readDouble());
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeDouble(vertical);
            buf.writeInt(weights.size());
            for (Map.Entry<UUID, Double> entry : weights.entrySet()) {
                buf.writeLong(entry.getKey().getMostSignificantBits());
                buf.writeLong(entry.getKey().getLeastSignificantBits());
                buf.writeDouble(entry.getValue());
            }
        }

        void apply(EntityPlayer player) {
            HooksCap cap = HookCapability.get(player);
            if (cap == null) {
                return;
            }
            cap.verticalHangDistance = vertical;
            for (HookInfo hook : cap.hooks) {
                Double weight = weights.get(hook.uuid);
                if (weight != null) {
                    hook.setWeight(weight);
                }
            }
            cap.updatePos();
        }
    }

    public static class UpdateWeightsHandler implements IMessageHandler<PacketUpdateWeights, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateWeights message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                message.apply(player);
                HooksCap cap = HookCapability.get(player);
                if (cap != null) {
                    cap.update(player);
                }
            });
            return null;
        }
    }

    public static class PacketHookCapSync implements IMessage {
        int entityId;
        NBTTagCompound tag;

        public PacketHookCapSync() {
        }

        public PacketHookCapSync(int entityId, NBTTagCompound tag) {
            this.entityId = entityId;
            this.tag = tag;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            entityId = buf.readInt();
            tag = ByteBufUtils.readTag(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(entityId);
            ByteBufUtils.writeTag(buf, tag);
        }
    }

    public static class SyncHooksHandler implements IMessageHandler<PacketHookCapSync, IMessage> {
        @Override
        public IMessage onMessage(PacketHookCapSync message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (Minecraft.getMinecraft().world == null) {
                    return;
                }
                if (!(Minecraft.getMinecraft().world.getEntityByID(message.entityId) instanceof EntityPlayer)) {
                    return;
                }
                EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().world.getEntityByID(message.entityId);
                HooksCap cap = HookCapability.get(player);
                if (cap != null && message.tag != null) {
                    cap.deserializeNBT(message.tag);
                }
            });
            return null;
        }
    }
}
