package com.drppp.drtech.common.Entity;

import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.Items.lightsaber.ItemDoubleLightsaber;
import com.drppp.drtech.common.Sound.DrTechSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class EntityThrownLightsaber extends EntityThrowable implements IEntityAdditionalSpawnData {
    private static final DataParameter<Boolean> RETURNING = EntityDataManager.createKey(EntityThrownLightsaber.class, DataSerializers.BOOLEAN);
    private static final String ITEM_TAG = "LightsaberItem";
    private static final String HAND_TAG = "ReturnHand";

    private ItemStack lightsaber = ItemStack.EMPTY;
    private EnumHand returnHand = EnumHand.MAIN_HAND;

    public EntityThrownLightsaber(World world) {
        super(world);
    }

    public EntityThrownLightsaber(World world, EntityLivingBase thrower, ItemStack stack, EnumHand hand) {
        super(world, thrower);
        returnHand = hand;
        setItem(stack);
        shoot(thrower, thrower.rotationPitch, thrower.rotationYaw, 0.0F, 2.0F, 0.0F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(RETURNING, false);
    }

    @Override
    public void onUpdate() {
        if (lightsaber.isEmpty()) {
            setDead();
            return;
        }

        EntityLivingBase thrower = getThrower();

        if (!world.isRemote) {
            if (thrower == null || !thrower.isEntityAlive()) {
                dropItem(this);
                setDead();
                return;
            }

            if (ticksExisted > 20) {
                setReturning(true);
            }

            if (isReturning()) {
                if (getDistance(thrower) <= 2.0D) {
                    giveBack(thrower);
                    setDead();
                    return;
                }

                Vec3d target = new Vec3d(thrower.posX, thrower.posY + thrower.height * 0.6D, thrower.posZ);
                Vec3d motion = target.subtract(getPositionVector()).normalize().scale(1.8D);
                motionX = motion.x;
                motionY = motion.y;
                motionZ = motion.z;
                velocityChanged = true;
            }

            if (ticksExisted % 3 == 0) {
                world.playSound(null, posX, posY, posZ, DrTechSounds.LIGHTSABER_SWING,
                        SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }

        super.onUpdate();
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (world.isRemote) {
            return;
        }

        EntityLivingBase thrower = getThrower();

        if (result.entityHit != null && result.entityHit != thrower) {
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), (float) ItemLightsaber.ATTACK_DAMAGE);
            world.playSound(null, posX, posY, posZ, DrTechSounds.LIGHTSABER_HIT,
                    SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        setReturning(true);
        motionY += 0.15D;
    }

    @Override
    protected float getGravityVelocity() {
        return isReturning() ? 0.0F : 0.01F;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        if (!lightsaber.isEmpty()) {
            compound.setTag(ITEM_TAG, lightsaber.writeToNBT(new NBTTagCompound()));
        }

        compound.setString(HAND_TAG, returnHand.name());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        if (compound.hasKey(ITEM_TAG)) {
            setItem(new ItemStack(compound.getCompoundTag(ITEM_TAG)));
        }

        try {
            returnHand = EnumHand.valueOf(compound.getString(HAND_TAG));
        } catch (IllegalArgumentException ignored) {
            returnHand = EnumHand.MAIN_HAND;
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeItemStack(buffer, lightsaber);
        buffer.writeByte(returnHand.ordinal());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        setItem(ByteBufUtils.readItemStack(additionalData));
        returnHand = additionalData.readByte() == EnumHand.OFF_HAND.ordinal() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
    }

    public ItemStack getItem() {
        return lightsaber;
    }

    private void setItem(ItemStack stack) {
        lightsaber = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (!lightsaber.isEmpty()) {
            lightsaber.setCount(1);
            setLightsaberActive(lightsaber, true);
            setSize(lightsaber.getItem() instanceof ItemDoubleLightsaber ? 1.42F : 0.9F, 0.2F);
        }
    }

    private boolean isReturning() {
        return dataManager.get(RETURNING);
    }

    private void setReturning(boolean returning) {
        dataManager.set(RETURNING, returning);
    }

    private void giveBack(EntityLivingBase thrower) {
        ItemStack stack = lightsaber.copy();
        setLightsaberActive(stack, true);

        if (thrower instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) thrower;
            if (player.getHeldItem(returnHand).isEmpty()) {
                player.setHeldItem(returnHand, stack);
            } else if (!player.inventory.addItemStackToInventory(stack)) {
                dropItem(player);
            }
            player.inventoryContainer.detectAndSendChanges();
        } else {
            thrower.setHeldItem(returnHand, stack);
        }

        lightsaber = ItemStack.EMPTY;
    }

    private static void setLightsaberActive(ItemStack stack, boolean active) {
        if (stack.getItem() instanceof ItemDoubleLightsaber) {
            ItemDoubleLightsaber.setActive(stack, active);
        } else {
            ItemLightsaber.setActive(stack, active);
        }
    }

    private void dropItem(Entity location) {
        if (lightsaber.isEmpty()) {
            return;
        }

        EntityItem item = new EntityItem(world, location.posX, location.posY, location.posZ, lightsaber.copy());
        world.spawnEntity(item);
        lightsaber = ItemStack.EMPTY;
    }
}
