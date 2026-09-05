package com.drppp.drtech.common.Entity.moster;

import com.drppp.drtech.common.Items.ItemsInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class EntityHappyGhast extends EntityGhast {
    private static final DataParameter<Boolean> HARNESSED = EntityDataManager.createKey(EntityHappyGhast.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BABY = EntityDataManager.createKey(EntityHappyGhast.class, DataSerializers.BOOLEAN);
    public static final int DEFAULT_GROWTH_TICKS = 24000;
    private static final int SNOWBALL_GROWTH_BOOST = DEFAULT_GROWTH_TICKS / 10;
    private static final double DISMOUNT_RADIUS = 2.85D;
    private BlockPos homePos;
    private int remainingGrowthTicks;

    public EntityHappyGhast(World worldIn) {
        super(worldIn);
        this.isImmuneToFire = false;
        this.experienceValue = 3;
        this.moveHelper = new HappyGhastMoveHelper(this);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AIFollowTemptingPlayer(this));
        this.tasks.addTask(2, new AIFollowFriendlyMob(this));
        this.tasks.addTask(5, new AIRandomFly(this));
        this.tasks.addTask(7, new AILookAround(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HARNESSED, Boolean.FALSE);
        this.dataManager.register(BABY, Boolean.FALSE);
        updateGhastSize();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.05D);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.homePos == null) {
            this.homePos = new BlockPos(this);
        }
        if (!this.world.isRemote && !this.isChildForm() && this.isOccupiedAsPlatform()) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            if (this.moveHelper instanceof HappyGhastMoveHelper) {
                ((HappyGhastMoveHelper) this.moveHelper).stop();
            }
        }
        if (!this.world.isRemote && this.isChildForm() && this.remainingGrowthTicks > 0) {
            this.remainingGrowthTicks--;
            if (this.remainingGrowthTicks <= 0) {
                growUp();
            }
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (this.isChildForm() && stack.getItem() == Items.SNOWBALL) {
            if (!this.world.isRemote) {
                feedSnowball(player, stack);
            }
            return true;
        }

        if (this.isChildForm()) {
            return false;
        }

        if (!this.getHarnessed() && stack.getItem() == ItemsInit.HAPPY_GHAST_HARNESS) {
            if (!this.world.isRemote) {
                this.setHarnessed(true);
                this.updateHome();
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return true;
        }

        if (this.getHarnessed() && stack.getItem() == Items.SHEARS) {
            if (!this.world.isRemote) {
                this.setHarnessed(false);
                this.updateHome();
                this.entityDropItem(new ItemStack(ItemsInit.HAPPY_GHAST_HARNESS), 0.0F);
                stack.damageItem(1, player);
            }
            return true;
        }

        if (super.processInteract(player, hand)) {
            return true;
        }

        if (this.getHarnessed()) {
            if (!this.world.isRemote) {
                player.startRiding(this);
            }
            return true;
        }

        return false;
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return this.getPassengers().size() < 4;
    }

    @Override
    public boolean canBeSteered() {
        return this.getHarnessed() && !this.isChildForm();
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        List<Entity> passengers = this.getPassengers();
        return passengers.isEmpty() ? null : passengers.get(0);
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (!this.isPassenger(passenger)) {
            return;
        }
        int index = this.getPassengers().indexOf(passenger);
        Vec3d offset = getSeatOffset(index).rotateYaw(-this.rotationYaw * 0.017453292F);
        double y = this.posY + this.getMountedYOffset() + passenger.getYOffset();
        passenger.setPosition(this.posX + offset.x, y, this.posZ + offset.z);
    }

    private Vec3d getSeatOffset(int index) {
        switch (index) {
            case 0:
                return new Vec3d(0.0D, 0.0D, -0.45D);
            case 1:
                return new Vec3d(1.5D, 0.0D, -0.2D);
            case 2:
                return new Vec3d(0.0D, 0.0D, 1.1D);
            case 3:
                return new Vec3d(-1.5D, 0.0D, -0.2D);
            default:
                return Vec3d.ZERO;
        }
    }

    @Override
    public double getMountedYOffset() {
        return 3.75D;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        Entity controller = this.getControllingPassenger();
        if (!this.isChildForm() && !this.isBeingRidden() && this.isOccupiedAsPlatform()) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            return;
        }
        if (this.getHarnessed() && controller instanceof EntityLivingBase && this.isBeingRidden()) {
            EntityLivingBase rider = (EntityLivingBase) controller;
            this.rotationYaw = rider.rotationYaw;
            this.prevRotationYaw = this.rotationYaw;
            this.rotationPitch = rider.rotationPitch * 0.5F;
            this.setRotation(this.rotationYaw, this.rotationPitch);
            this.renderYawOffset = this.rotationYaw;
            this.rotationYawHead = this.rotationYaw;

            if (this.canPassengerSteer()) {
                double speed = 0.28D;
                double forwardInput = rider.moveForward;
                double strafeInput = rider.moveStrafing * 0.5D;
                Vec3d look = rider.getLookVec();
                Vec3d side = new Vec3d(-Math.sin(Math.toRadians(this.rotationYaw)), 0.0D, Math.cos(Math.toRadians(this.rotationYaw)));

                double motionX = look.x * forwardInput * speed + side.x * strafeInput * speed;
                double motionY = look.y * forwardInput * speed;
                double motionZ = look.z * forwardInput * speed + side.z * strafeInput * speed;

                if (rider.isSneaking()) {
                    motionY -= 0.18D;
                }
                if (forwardInput < 0.0F) {
                    motionY *= 0.5D;
                }

                this.motionX = motionX;
                this.motionY = motionY;
                this.motionZ = motionZ;
                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                this.motionX *= 0.91D;
                this.motionY *= 0.91D;
                this.motionZ *= 0.91D;
            } else {
                this.motionX *= 0.8D;
                this.motionY *= 0.8D;
                this.motionZ *= 0.8D;
            }
            this.updateArmSwingProgress();
            return;
        }

        super.travel(strafe, vertical, forward);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.isChildForm() ? null : this.getEntityBoundingBox();
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return this.isChildForm() ? null : this.getEntityBoundingBox();
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.world.isRemote) {
            placePassengerAfterDismount(passenger);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!this.world.isRemote && this.getHarnessed()) {
            this.dropItem(ItemsInit.HAPPY_GHAST_HARNESS, 1);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return false;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return super.getHurtSound(damageSourceIn);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return super.getDeathSound();
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable() {
        return super.getLootTable();
    }

    @Override
    public String getName() {
        if (this.hasCustomName()) {
            return this.getCustomNameTag();
        }
        return I18n.translateToLocal(this.isChildForm() ? "entity.drtech.ghastling.name" : "entity.happy_ghast.name");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Harnessed", this.getHarnessed());
        compound.setBoolean("Baby", this.isChildForm());
        compound.setInteger("RemainingGrowthTicks", this.remainingGrowthTicks);
        if (this.homePos != null) {
            compound.setInteger("HomeX", this.homePos.getX());
            compound.setInteger("HomeY", this.homePos.getY());
            compound.setInteger("HomeZ", this.homePos.getZ());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setHarnessed(compound.getBoolean("Harnessed"));
        this.setChildForm(compound.getBoolean("Baby"));
        this.remainingGrowthTicks = compound.getInteger("RemainingGrowthTicks");
        if (compound.hasKey("HomeX", 99)) {
            this.homePos = new BlockPos(compound.getInteger("HomeX"), compound.getInteger("HomeY"), compound.getInteger("HomeZ"));
        }
    }

    public boolean getHarnessed() {
        return this.dataManager.get(HARNESSED);
    }

    public void setHarnessed(boolean harnessed) {
        this.dataManager.set(HARNESSED, harnessed);
    }

    public boolean isChildForm() {
        return this.dataManager.get(BABY);
    }

    public void setChildForm(boolean child) {
        this.dataManager.set(BABY, child);
        if (child && this.remainingGrowthTicks <= 0) {
            this.remainingGrowthTicks = DEFAULT_GROWTH_TICKS;
        }
        if (!child) {
            this.remainingGrowthTicks = 0;
        }
        updateGhastSize();
    }

    public void setRemainingGrowthTicks(int ticks) {
        this.remainingGrowthTicks = Math.max(0, ticks);
        if (this.remainingGrowthTicks > 0 && !this.isChildForm()) {
            this.setChildForm(true);
        }
    }

    public int getRemainingGrowthTicks() {
        return this.remainingGrowthTicks;
    }

    public void setHome(BlockPos pos) {
        this.homePos = pos;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (BABY.equals(key)) {
            updateGhastSize();
        }
    }

    private void updateHome() {
        this.homePos = new BlockPos(this);
    }

    private double getHomeRadius() {
        return this.getHarnessed() || this.isChildForm() ? 32.0D : 64.0D;
    }

    private void updateGhastSize() {
        boolean baby = false;
        if (this.dataManager != null) {
            try {
                baby = this.dataManager.get(BABY);
            } catch (IllegalArgumentException ignored) {
                baby = false;
            }
        }
        float size = baby ? 1.05F : 4.5F;
        this.setSize(size, size);
        this.experienceValue = baby ? 0 : 3;
    }

    private void feedSnowball(EntityPlayer player, ItemStack stack) {
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        this.remainingGrowthTicks = Math.max(0, this.remainingGrowthTicks - SNOWBALL_GROWTH_BOOST);
        if (this.remainingGrowthTicks <= 0) {
            growUp();
        }
    }

    private void growUp() {
        this.remainingGrowthTicks = 0;
        this.setChildForm(false);
        this.updateHome();
    }

    private boolean isOccupiedAsPlatform() {
        AxisAlignedBB bodyBox = this.getEntityBoundingBox();
        if (this.isChildForm() || bodyBox == null) {
            return false;
        }
        AxisAlignedBB topSurface = new AxisAlignedBB(
                bodyBox.minX - 0.15D,
                bodyBox.maxY - 0.2D,
                bodyBox.minZ - 0.15D,
                bodyBox.maxX + 0.15D,
                bodyBox.maxY + 1.1D,
                bodyBox.maxZ + 0.15D
        );
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, topSurface);
        for (Entity entity : entities) {
            if (entity.isDead || this.isPassenger(entity)) {
                continue;
            }
            if (entity.getEntityBoundingBox().minY <= bodyBox.maxY + 0.2D
                    && entity.getEntityBoundingBox().maxY >= bodyBox.maxY - 0.2D) {
                return true;
            }
        }
        return false;
    }

    private void placePassengerAfterDismount(Entity passenger) {
        AxisAlignedBB collisionBox = this.getCollisionBoundingBox();
        double targetY = (collisionBox == null ? this.posY + this.height : collisionBox.maxY) + 0.05D;
        Vec3d[] offsets = new Vec3d[] {
                new Vec3d(0.0D, 0.0D, -DISMOUNT_RADIUS).rotateYaw(-this.rotationYaw * 0.017453292F),
                new Vec3d(DISMOUNT_RADIUS, 0.0D, 0.0D).rotateYaw(-this.rotationYaw * 0.017453292F),
                new Vec3d(-DISMOUNT_RADIUS, 0.0D, 0.0D).rotateYaw(-this.rotationYaw * 0.017453292F),
                new Vec3d(0.0D, 0.0D, DISMOUNT_RADIUS).rotateYaw(-this.rotationYaw * 0.017453292F),
                new Vec3d(0.0D, 0.75D, 0.0D)
        };

        for (Vec3d offset : offsets) {
            double x = this.posX + offset.x;
            double y = targetY + offset.y;
            double z = this.posZ + offset.z;
            AxisAlignedBB movedBox = passenger.getEntityBoundingBox().offset(x - passenger.posX, y - passenger.posY, z - passenger.posZ);
            if (this.world.getCollisionBoxes(passenger, movedBox).isEmpty()) {
                passenger.setPosition(x, y, z);
                passenger.motionX = 0.0D;
                passenger.motionY = 0.0D;
                passenger.motionZ = 0.0D;
                return;
            }
        }

        passenger.setPosition(this.posX, targetY + 0.75D, this.posZ);
        passenger.motionX = 0.0D;
        passenger.motionY = 0.0D;
        passenger.motionZ = 0.0D;
    }

    static class AIRandomFly extends EntityAIBase {
        private final EntityHappyGhast parentEntity;

        AIRandomFly(EntityHappyGhast ghast) {
            this.parentEntity = ghast;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (this.parentEntity.isBeingRidden()) {
                return false;
            }
            if (this.parentEntity.isOccupiedAsPlatform()) {
                return false;
            }
            EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();
            if (!entitymovehelper.isUpdating()) {
                return true;
            }
            double d0 = entitymovehelper.getX() - this.parentEntity.posX;
            double d1 = entitymovehelper.getY() - this.parentEntity.posY;
            double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            return d3 < 1.0D || d3 > 3600.0D;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            Random random = this.parentEntity.getRNG();
            BlockPos home = this.parentEntity.homePos == null ? new BlockPos(this.parentEntity) : this.parentEntity.homePos;
            double radius = this.parentEntity.getHomeRadius();
            double d0 = home.getX() + 0.5D + (random.nextDouble() * 2.0D - 1.0D) * radius;
            double d1 = Math.max(8.0D, home.getY() + 8.0D + (random.nextDouble() * 2.0D - 1.0D) * 16.0D);
            double d2 = home.getZ() + 0.5D + (random.nextDouble() * 2.0D - 1.0D) * radius;
            this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1.0D);
        }
    }

    static class AILookAround extends EntityAIBase {
        private final EntityHappyGhast parentEntity;

        AILookAround(EntityHappyGhast ghast) {
            this.parentEntity = ghast;
            this.setMutexBits(2);
        }

        @Override
        public boolean shouldExecute() {
            return !this.parentEntity.isBeingRidden() && !this.parentEntity.isOccupiedAsPlatform();
        }

        @Override
        public void updateTask() {
            this.parentEntity.rotationYaw = -((float) MathHelper.atan2(this.parentEntity.motionX, this.parentEntity.motionZ)) * (180F / (float) Math.PI);
            this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;
        }
    }

    static class AIFollowTemptingPlayer extends EntityAIBase {
        private final EntityHappyGhast parentEntity;
        private EntityPlayer targetPlayer;

        AIFollowTemptingPlayer(EntityHappyGhast ghast) {
            this.parentEntity = ghast;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (this.parentEntity.isBeingRidden() || this.parentEntity.isOccupiedAsPlatform()) {
                return false;
            }
            List<EntityPlayer> players = this.parentEntity.world.getEntitiesWithinAABB(EntityPlayer.class,
                    this.parentEntity.getEntityBoundingBox().grow(24.0D, 16.0D, 24.0D));
            for (EntityPlayer player : players) {
                if (isHoldingTemptItem(player)) {
                    this.targetPlayer = player;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.targetPlayer != null && this.targetPlayer.isEntityAlive()
                    && this.parentEntity.getDistance(this.targetPlayer) < 28.0F
                    && isHoldingTemptItem(this.targetPlayer)
                    && !this.parentEntity.isBeingRidden()
                    && !this.parentEntity.isOccupiedAsPlatform();
        }

        @Override
        public void resetTask() {
            this.parentEntity.updateHome();
            this.targetPlayer = null;
        }

        @Override
        public void updateTask() {
            if (this.targetPlayer == null) {
                return;
            }
            Vec3d look = this.targetPlayer.getLookVec();
            double x = this.targetPlayer.posX + look.x * 2.0D;
            double y = this.targetPlayer.posY + 2.0D + look.y;
            double z = this.targetPlayer.posZ + look.z * 2.0D;
            this.parentEntity.getMoveHelper().setMoveTo(x, y, z, 1.1D);
        }

        private boolean isHoldingTemptItem(EntityPlayer player) {
            return isHoldingSnowball(player) || isHoldingHarness(player);
        }

        private boolean isHoldingSnowball(EntityPlayer player) {
            return player.getHeldItemMainhand().getItem() == Items.SNOWBALL
                    || player.getHeldItemOffhand().getItem() == Items.SNOWBALL;
        }

        private boolean isHoldingHarness(EntityPlayer player) {
            if (this.parentEntity.isChildForm() || this.parentEntity.getHarnessed()) {
                return false;
            }
            return player.getHeldItemMainhand().getItem() == ItemsInit.HAPPY_GHAST_HARNESS
                    || player.getHeldItemOffhand().getItem() == ItemsInit.HAPPY_GHAST_HARNESS;
        }
    }

    static class AIFollowFriendlyMob extends EntityAIBase {
        private final EntityHappyGhast parentEntity;
        private Entity targetEntity;

        AIFollowFriendlyMob(EntityHappyGhast ghast) {
            this.parentEntity = ghast;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!this.parentEntity.isChildForm() || this.parentEntity.isBeingRidden() || this.parentEntity.isOccupiedAsPlatform()) {
                return false;
            }
            List<Entity> entities = this.parentEntity.world.getEntitiesWithinAABB(Entity.class,
                    this.parentEntity.getEntityBoundingBox().grow(16.0D, 8.0D, 16.0D));
            Entity nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (Entity entity : entities) {
                if (!isValidTarget(entity)) {
                    continue;
                }
                double distance = this.parentEntity.getDistanceSq(entity);
                if (distance < nearestDistance) {
                    nearest = entity;
                    nearestDistance = distance;
                }
            }
            this.targetEntity = nearest;
            return this.targetEntity != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.targetEntity != null
                    && this.parentEntity.isChildForm()
                    && this.targetEntity.isEntityAlive()
                    && this.parentEntity.getDistanceSq(this.targetEntity) < 20.0D * 20.0D
                    && !this.parentEntity.isBeingRidden()
                    && !this.parentEntity.isOccupiedAsPlatform();
        }

        @Override
        public void resetTask() {
            this.parentEntity.updateHome();
            this.targetEntity = null;
        }

        @Override
        public void updateTask() {
            if (this.targetEntity == null) {
                return;
            }
            double x = this.targetEntity.posX;
            double y = this.targetEntity.posY + this.targetEntity.height + 1.0D;
            double z = this.targetEntity.posZ;
            this.parentEntity.getMoveHelper().setMoveTo(x, y, z, 1.0D);
        }

        private boolean isValidTarget(Entity entity) {
            if (entity == this.parentEntity || !entity.isEntityAlive()) {
                return false;
            }
            if (entity instanceof EntityAnimal) {
                return true;
            }
            return entity instanceof EntityHappyGhast && !((EntityHappyGhast) entity).isChildForm();
        }
    }

    static class HappyGhastMoveHelper extends EntityMoveHelper {
        private final EntityFlying parentEntity;
        private int courseChangeCooldown;

        HappyGhastMoveHelper(EntityFlying ghast) {
            super(ghast);
            this.parentEntity = ghast;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action == Action.MOVE_TO) {
                double d0 = this.posX - this.parentEntity.posX;
                double d1 = this.posY - this.parentEntity.posY;
                double d2 = this.posZ - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
                    d3 = (double) MathHelper.sqrt(d3);

                    if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
                        this.parentEntity.motionX += d0 / d3 * 0.1D;
                        this.parentEntity.motionY += d1 / d3 * 0.1D;
                        this.parentEntity.motionZ += d2 / d3 * 0.1D;
                    } else {
                        this.action = Action.WAIT;
                    }
                }
            }
        }

        public void stop() {
            this.action = Action.WAIT;
        }

        private boolean isNotColliding(double x, double y, double z, double distance) {
            double d0 = (x - this.parentEntity.posX) / distance;
            double d1 = (y - this.parentEntity.posY) / distance;
            double d2 = (z - this.parentEntity.posZ) / distance;
            AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();

            for (int i = 1; (double) i < distance; ++i) {
                axisalignedbb = axisalignedbb.offset(d0, d1, d2);
                if (!this.parentEntity.world.getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }
}
