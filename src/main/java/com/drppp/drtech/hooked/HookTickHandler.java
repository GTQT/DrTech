package com.drppp.drtech.hooked;

import com.drppp.drtech.DrTechMain;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public final class HookTickHandler {
    private HookTickHandler() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new HookTickHandler());
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (!event.getEntityPlayer().world.isRemote && event.getTarget() instanceof EntityPlayer) {
            HookNetwork.syncHooks(event.getTarget());
        }
    }

    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP && !event.getEntity().world.isRemote) {
            HookNetwork.syncHooks(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getEntityPlayer().onGround) {
            HooksCap cap = HookCapability.get(event.getEntityPlayer());
            if (cap != null) {
                for (HookInfo hook : cap.hooks) {
                    if (hook.status == EnumHookStatus.PLANTED) {
                        event.setNewSpeed(event.getNewSpeed() * 5);
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        DrTechMain.proxy.setAutoJump(player, true);
        HooksCap cap = HookCapability.get(player);
        if (cap == null) {
            return;
        }

        ItemStackHolder holder = getHookContext(player, cap);
        if (holder.type == null) {
            return;
        }
        HookType type = holder.type;
        int count = holder.hookItem != null && ItemHook.isInhibited(holder.hookItem) ? 1 : type.count;
        if (type != HookType.RED || cap.hooks.size() != 1) {
            cap.verticalHangDistance = 0.0;
        }

        Vec3d waist = getWaistPos(player);
        boolean update = false;
        boolean updatePos = false;

        for (HookInfo hook : cap.hooks) {
            if (hook.status == EnumHookStatus.RETRACTING) {
                hook.status = EnumHookStatus.DEAD;
                update = true;
            }
            if (hook.status == EnumHookStatus.EXTENDING) {
                double distanceLeft = type.range - HookMath.subtract(hook.pos, waist).length();
                double speedOrRemaining = Math.min(type.speed, distanceLeft) + type.hookLength;
                RayTraceResult trace = player.world.rayTraceBlocks(hook.pos, HookMath.add(hook.pos, HookMath.scale(hook.direction, speedOrRemaining)), false, true, false);
                if (trace == null || trace.typeOfHit == RayTraceResult.Type.MISS) {
                    hook.pos = HookMath.add(hook.pos, HookMath.scale(hook.direction, speedOrRemaining));
                } else {
                    hook.pos = HookMath.subtract(trace.hitVec, HookMath.scale(hook.direction, type.hookLength));
                    hook.status = EnumHookStatus.PLANTED;
                    hook.block = trace.getBlockPos();
                    hook.side = trace.sideHit;
                    update = true;
                    updatePos = true;
                }
            }

            if (player.world.isRemote && type == HookType.ENDER) {
                renderEnderParticles(player, waist, hook);
            }

            if (hook.pos.squareDistanceTo(waist) > type.rangeSq) {
                hook.status = EnumHookStatus.TO_RETRACT;
            }
            if (hook.block != null && hook.status == EnumHookStatus.PLANTED && player.world.isAirBlock(hook.block)) {
                hook.status = EnumHookStatus.TO_RETRACT;
            }
            if (hook.status == EnumHookStatus.TO_RETRACT) {
                hook.block = new BlockPos(hook.pos);
                hook.pos = waist;
                hook.status = EnumHookStatus.RETRACTING;
                update = true;
            }
        }

        for (Iterator<HookInfo> iterator = cap.hooks.iterator(); iterator.hasNext(); ) {
            if (iterator.next().status == EnumHookStatus.DEAD) {
                iterator.remove();
                update = true;
                updatePos = true;
            }
        }
        while (countPlanted(cap) > count) {
            HookInfo planted = firstPlanted(cap);
            if (planted == null) {
                break;
            }
            planted.status = EnumHookStatus.TO_RETRACT;
            update = true;
            updatePos = true;
        }

        if (updatePos) {
            cap.updatePos();
        }
        if (update) {
            cap.update(player);
        }
        if (cap.getCenterPos() == null) {
            cap.updatePos();
            cap.update(player);
        }
        if (cap.hookType == HookType.RED && countPlanted(cap) > 0 && player.world.isRemote) {
            cap.updateRedMovement(player);
        }
        applyPull(player, cap, type, waist);
        Vec3d center = cap.getCenterPos();
        if (center != null && !player.world.isRemote) {
            player.world.spawnParticle(EnumParticleTypes.FLAME, center.x, center.y, center.z, 0.0, 0.0, 0.0, 0);
        }
    }

    public static Vec3d getWaistPos(Entity entity) {
        return HookMath.add(entity.getPositionVector(), HookMath.vec(0, entity.getEyeHeight() / 2.0, 0));
    }

    private ItemStackHolder getHookContext(EntityPlayer player, HooksCap cap) {
        net.minecraft.item.ItemStack hookItem = ItemHook.getItem(player);
        HookType itemType = ItemHook.getType(hookItem);
        if (itemType != cap.hookType) {
            cap.hookType = itemType;
            cap.hooks.clear();
            cap.verticalHangDistance = 0.0;
            cap.setCenterPos(null);
            cap.update(player);
        }
        return new ItemStackHolder(hookItem, cap.hookType);
    }

    private void renderEnderParticles(EntityPlayer player, Vec3d waist, HookInfo hook) {
        double len = HookMath.subtract(hook.pos, waist).length();
        if (len <= 0.0) {
            return;
        }
        Vec3d normal = HookMath.scale(HookMath.subtract(hook.pos, waist), 1.0 / len);
        Vec3d negNormal = HookMath.scale(normal, -1.0);
        double spacing = 0.1;
        double v = 1.0;
        while (v < len && len > 2) {
            if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                Vec3d pos = HookMath.add(waist, HookMath.scale(normal, v));
                Vec3d vel = ThreadLocalRandom.current().nextBoolean() ? negNormal : normal;
                player.world.spawnParticle(
                    EnumParticleTypes.PORTAL,
                    true,
                    pos.x + randomOffset(),
                    pos.y + randomOffset() + 0.1,
                    pos.z + randomOffset(),
                    vel.x + randomOffset(),
                    vel.y + randomOffset() - 0.65,
                    vel.z + randomOffset()
                );
            }
            v += spacing;
        }
    }

    private void applyPull(EntityPlayer player, HooksCap cap, HookType type, Vec3d waist) {
        Vec3d center = cap.getCenterPos();
        if (center == null) {
            return;
        }
        boolean shouldSet = false;
        Vec3d pull = center.subtract(waist);
        double len = pull.length();
        Vec3d adjusted;
        if (len > type.pullStrength) {
            adjusted = pull.scale(type.pullStrength / len);
        } else {
            adjusted = pull;
            shouldSet = true;
        }

        double forceCoeff = 0.5;
        if (shouldSet) {
            player.motionX = adjusted.x;
            player.motionY = adjusted.y;
            player.motionZ = adjusted.z;
        } else {
            for (HookInfo hook : cap.hooks) {
                if (hook.status != EnumHookStatus.PLANTED) {
                    continue;
                }
                Vec3d pullVec = hook.pos.subtract(waist);
                double projection = HookMath.dot(new Vec3d(player.motionX, player.motionY, player.motionZ), pullVec) / pullVec.length();
                if (projection < 0) {
                    Vec3d add = pullVec.scale(projection / pullVec.length());
                    player.motionX -= add.x;
                    player.motionY -= add.y;
                    player.motionZ -= add.z;
                }
            }
            if (Math.abs(player.motionX) < Math.abs(adjusted.x)) {
                double newX = player.motionX + adjusted.x * forceCoeff;
                player.motionX = Math.abs(newX) > Math.abs(adjusted.x) ? adjusted.x : newX;
            }
            if (Math.abs(player.motionY) < Math.abs(adjusted.y)) {
                double newY = player.motionY + adjusted.y * forceCoeff;
                player.motionY = Math.abs(newY) > Math.abs(adjusted.y) ? adjusted.y : newY;
            }
            if (Math.abs(player.motionZ) < Math.abs(adjusted.z)) {
                double newZ = player.motionZ + adjusted.z * forceCoeff;
                player.motionZ = Math.abs(newZ) > Math.abs(adjusted.z) ? adjusted.z : newZ;
            }
        }
        player.fallDistance = 0f;
        setJumpTicks(player, 10);
        DrTechMain.proxy.setAutoJump(player, false);
    }

    private int countPlanted(HooksCap cap) {
        int count = 0;
        for (HookInfo hook : cap.hooks) {
            if (hook.status == EnumHookStatus.PLANTED) {
                count++;
            }
        }
        return count;
    }

    private HookInfo firstPlanted(HooksCap cap) {
        for (HookInfo hook : cap.hooks) {
            if (hook.status == EnumHookStatus.PLANTED) {
                return hook;
            }
        }
        return null;
    }

    private double randomOffset() {
        return ThreadLocalRandom.current().nextDouble(-0.05, 0.05);
    }

    private void setJumpTicks(EntityLivingBase entity, int value) {
        ObfuscationReflectionHelper.setPrivateValue(EntityLivingBase.class, entity, value, "field_70773_bE", "jumpTicks");
    }

    private static final class ItemStackHolder {
        private final net.minecraft.item.ItemStack hookItem;
        private final HookType type;

        private ItemStackHolder(net.minecraft.item.ItemStack hookItem, HookType type) {
            this.hookItem = hookItem;
            this.type = type;
        }
    }
}
