package com.drppp.drtech.common.drone.entity;

import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.common.drone.inventory.DroneItemFilter;
import com.drppp.drtech.common.drone.action.DroneInteractionRequest;
import com.drppp.drtech.common.Items.lightsaber.ItemLightsaber;
import com.drppp.drtech.common.Items.lightsaber.ItemDoubleLightsaber;
import com.meowmel.cropQT.block.BlockCropStick;
import com.meowmel.cropQT.tile.TileCropStick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemStackHandler;


/** Event-compatible server world actions executed through a dedicated Forge fake player. */
final class DroneWorldActions {

    enum InteractionOutcome { SUCCESS, NO_ITEM, DENIED }
    enum PlantingOutcome { PLANTED, SKIPPED, NO_ITEM, DENIED }

    static final class FishingRetraction {
        final ItemStack rod;
        final java.util.List<EntityItem> loot;

        FishingRetraction(ItemStack rod, java.util.List<EntityItem> loot) {
            this.rod = rod;
            this.loot = loot;
        }
    }

    private DroneWorldActions() {}

    static boolean breakBlock(EntityProgrammableDrone drone, ItemStackHandler inventory, BlockPos target) {
        WorldServer world = (WorldServer) drone.world;
        if (!world.isBlockLoaded(target) || world.isAirBlock(target)) return true;
        IBlockState state = world.getBlockState(target);
        if (state.getBlockHardness(world, target) < 0.0F) return false;

        int toolSlot = selectBestTool(inventory, state);
        FakePlayer player = preparePlayer(drone);
        ItemStack held = toolSlot < 0 ? ItemStack.EMPTY
                : inventory.extractItem(toolSlot, inventory.getSlotLimit(toolSlot), false);
        player.inventory.setInventorySlotContents(0, held);
        boolean harvested;
        try {
            harvested = player.interactionManager.tryHarvestBlock(target);
        } finally {
            if (toolSlot >= 0) inventory.setStackInSlot(toolSlot, player.getHeldItemMainhand());
            clearPlayer(player);
        }
        return harvested || world.isAirBlock(target);
    }

    static boolean placeBlock(EntityProgrammableDrone drone, ItemStackHandler inventory, BlockPos target,
            DroneItemFilter filter) {
        WorldServer world = (WorldServer) drone.world;
        if (!world.isBlockLoaded(target) || !world.getBlockState(target).getBlock().isReplaceable(world, target)) {
            return false;
        }
        int itemSlot = findPlaceableItem(inventory, filter);
        if (itemSlot < 0) return false;
        EnumFacing supportDirection = findSupport(world, target);
        if (supportDirection == null) return false;

        BlockPos support = target.offset(supportDirection);
        EnumFacing clickedFace = supportDirection.getOpposite();
        FakePlayer player = preparePlayer(drone);
        ItemStack held = inventory.extractItem(itemSlot, inventory.getSlotLimit(itemSlot), false);
        player.inventory.setInventorySlotContents(0, held);
        player.setSneaking(true);
        EnumActionResult result;
        try {
            result = player.interactionManager.processRightClickBlock(player, world, player.getHeldItemMainhand(),
                    EnumHand.MAIN_HAND, support, clickedFace, 0.5F, 0.5F, 0.5F);
        } finally {
            inventory.setStackInSlot(itemSlot, player.getHeldItemMainhand());
            clearPlayer(player);
        }
        return result == EnumActionResult.SUCCESS
                && !world.getBlockState(target).getBlock().isReplaceable(world, target);
    }

    static InteractionOutcome interactBlock(EntityProgrammableDrone drone, ItemStackHandler inventory,
            DroneInteractionRequest request) {
        WorldServer world = (WorldServer) drone.world;
        BlockPos target = request.getTarget();
        if (!world.isBlockLoaded(target)) return InteractionOutcome.DENIED;
        int itemSlot = request.isUseHeldItem()
                ? findMatchingItem(inventory, DroneItemFilter.fromSpec(request.getHeldItemFilter())) : -1;
        if (request.isUseHeldItem() && itemSlot < 0) return InteractionOutcome.NO_ITEM;
        EnumFacing clickedFace = request.getSide();
        if (clickedFace == null) {
            clickedFace = EnumFacing.getFacingFromVector((float) (drone.posX - target.getX() - 0.5D),
                    (float) (drone.posY - target.getY() - 0.5D),
                    (float) (drone.posZ - target.getZ() - 0.5D));
        }
        FakePlayer player = preparePlayer(drone);
        ItemStack held = itemSlot < 0 ? ItemStack.EMPTY
                : inventory.extractItem(itemSlot, inventory.getSlotLimit(itemSlot), false);
        player.inventory.setInventorySlotContents(0, held);
        player.setSneaking(request.isSneaking());
        EnumActionResult result;
        try {
            result = player.interactionManager.processRightClickBlock(player, world, player.getHeldItemMainhand(),
                    EnumHand.MAIN_HAND, target, clickedFace, 0.5F, 0.5F, 0.5F);
        } finally {
            if (itemSlot >= 0) inventory.setStackInSlot(itemSlot, player.getHeldItemMainhand());
            clearPlayer(player);
        }
        return result == EnumActionResult.SUCCESS ? InteractionOutcome.SUCCESS : InteractionOutcome.DENIED;
    }

    static InteractionOutcome useItem(EntityProgrammableDrone drone, ItemStackHandler inventory,
            DroneItemFilter filter, boolean sneaking) {
        int itemSlot = findMatchingItem(inventory, filter);
        if (itemSlot < 0) return InteractionOutcome.NO_ITEM;
        WorldServer world = (WorldServer) drone.world;
        FakePlayer player = preparePlayer(drone);
        ItemStack held = inventory.extractItem(itemSlot, inventory.getSlotLimit(itemSlot), false);
        player.inventory.setInventorySlotContents(0, held);
        player.setSneaking(sneaking);
        EnumActionResult result;
        try {
            result = player.interactionManager.processRightClick(player, world,
                    player.getHeldItemMainhand(), EnumHand.MAIN_HAND);
        } finally {
            inventory.setStackInSlot(itemSlot, player.getHeldItemMainhand());
            clearPlayer(player);
        }
        return result == EnumActionResult.SUCCESS ? InteractionOutcome.SUCCESS : InteractionOutcome.DENIED;
    }

    static boolean authorizeSignEdit(EntityProgrammableDrone drone, BlockPos target) {
        if (!(drone.world instanceof WorldServer) || target == null || !drone.world.isBlockLoaded(target)) {
            return false;
        }
        FakePlayer player = preparePlayer(drone);
        try {
            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, EnumHand.MAIN_HAND,
                    target, EnumFacing.UP, new Vec3d(0.5D, 0.5D, 0.5D));
            return !event.isCanceled() && event.getUseBlock() != Event.Result.DENY;
        } finally {
            clearPlayer(player);
        }
    }

    static boolean isMatureCrop(WorldServer world, BlockPos target) {
        if (!world.isBlockLoaded(target)) return false;
        TileEntity tile = world.getTileEntity(target);
        if (tile instanceof TileCropStick cropStick) return cropStick.hasCrop() && cropStick.isMature();
        IBlockState state = world.getBlockState(target);
        if (state.getBlock() instanceof BlockCrops crops) return crops.isMaxAge(state);
        if (state.getBlock() == Blocks.NETHER_WART) {
            return state.getValue(BlockNetherWart.AGE) >= 3;
        }
        if (state.getBlock() instanceof BlockCocoa) return state.getValue(BlockCocoa.AGE) >= 2;
        return false;
    }

    static boolean harvestCrop(EntityProgrammableDrone drone, ItemStackHandler inventory, BlockPos target) {
        WorldServer world = (WorldServer) drone.world;
        if (!isMatureCrop(world, target)) return false;
        if (world.getBlockState(target).getBlock() instanceof BlockCropStick) {
            return harvestCropStick(drone, target);
        }
        return breakBlock(drone, inventory, target);
    }

    static boolean isTreeLog(WorldServer world, BlockPos target) {
        return world.isBlockLoaded(target) && !world.isAirBlock(target)
                && world.getBlockState(target).getBlock().isWood(world, target);
    }

    static PlantingOutcome replant(EntityProgrammableDrone drone, ItemStackHandler inventory, BlockPos target,
            DroneItemFilter filter) {
        WorldServer world = (WorldServer) drone.world;
        if (!world.isBlockLoaded(target) || !world.isBlockLoaded(target.down())) return PlantingOutcome.DENIED;
        if (!isPotentialPlantingTarget(world, target)) return PlantingOutcome.SKIPPED;
        BlockPos soilPosition = target.down();
        IBlockState soil = world.getBlockState(soilPosition);

        DroneItemFilter effectiveFilter = filter == null ? DroneItemFilter.ANY : filter;
        boolean hasFilteredPlant = false;
        int selectedSlot = -1;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty() || !effectiveFilter.matches(stack)) continue;
            IPlantable plant = asPlantable(stack);
            if (plant == null) continue;
            hasFilteredPlant = true;
            if (soil.getBlock().canSustainPlant(soil, world, soilPosition, EnumFacing.UP, plant)) {
                selectedSlot = slot;
                break;
            }
        }
        if (selectedSlot < 0) return hasFilteredPlant ? PlantingOutcome.SKIPPED : PlantingOutcome.NO_ITEM;

        FakePlayer player = preparePlayer(drone);
        ItemStack held = inventory.extractItem(selectedSlot, inventory.getSlotLimit(selectedSlot), false);
        player.inventory.setInventorySlotContents(0, held);
        EnumActionResult result;
        try {
            result = player.interactionManager.processRightClickBlock(player, world, player.getHeldItemMainhand(),
                    EnumHand.MAIN_HAND, soilPosition, EnumFacing.UP, 0.5F, 1.0F, 0.5F);
        } finally {
            inventory.setStackInSlot(selectedSlot, player.getHeldItemMainhand());
            clearPlayer(player);
        }
        return result == EnumActionResult.SUCCESS
                && !world.getBlockState(target).getBlock().isReplaceable(world, target)
                ? PlantingOutcome.PLANTED : PlantingOutcome.DENIED;
    }

    static boolean isPotentialPlantingTarget(WorldServer world, BlockPos target) {
        if (!world.isBlockLoaded(target) || !world.isBlockLoaded(target.down())
                || !world.getBlockState(target).getBlock().isReplaceable(world, target)) return false;
        net.minecraft.block.Block soil = world.getBlockState(target.down()).getBlock();
        return soil == Blocks.FARMLAND || soil == Blocks.DIRT || soil == Blocks.GRASS
                || soil == Blocks.SOUL_SAND;
    }

    private static IPlantable asPlantable(ItemStack stack) {
        if (stack.getItem() instanceof IPlantable plant) return plant;
        if (stack.getItem() instanceof ItemBlock itemBlock && itemBlock.getBlock() instanceof IPlantable plant) {
            return plant;
        }
        return null;
    }

    static InteractionOutcome interactEntity(EntityProgrammableDrone drone, ItemStackHandler inventory,
            Entity target, boolean useItem, DroneItemFilter filter) {
        if (!(drone.world instanceof WorldServer) || target == null || !target.isEntityAlive()) {
            return InteractionOutcome.DENIED;
        }
        FakePlayer player = preparePlayer(drone);
        int itemSlot = useItem ? findMatchingItem(inventory, filter) : -1;
        if (useItem && itemSlot < 0) return InteractionOutcome.NO_ITEM;
        ItemStack held = itemSlot < 0 ? ItemStack.EMPTY
                : inventory.extractItem(itemSlot, inventory.getSlotLimit(itemSlot), false);
        player.inventory.setInventorySlotContents(0, held);
        EnumActionResult accepted;
        try {
            accepted = player.interactOn(target, EnumHand.MAIN_HAND);
        } finally {
            if (itemSlot >= 0) inventory.setStackInSlot(itemSlot, player.getHeldItemMainhand());
            clearPlayer(player);
        }
        return accepted == EnumActionResult.SUCCESS ? InteractionOutcome.SUCCESS : InteractionOutcome.DENIED;
    }

    static boolean attackEntity(EntityProgrammableDrone drone, ItemStackHandler inventory, EntityLivingBase target) {
        if (!DrtConfig.EnableDroneCombat || !(drone.world instanceof WorldServer) || target == null
                || !target.isEntityAlive() || !target.isNonBoss()
                || target instanceof EntityPlayer && !DrtConfig.EnableDronePlayerAttack) return false;
        FakePlayer player = preparePlayer(drone);
        int weaponSlot = selectBestWeapon(inventory, inventory.getSlots());
        ItemStack held = weaponSlot < 0 ? ItemStack.EMPTY
                : inventory.extractItem(weaponSlot, inventory.getSlotLimit(weaponSlot), false);
        activateLightsaber(held);
        player.inventory.setInventorySlotContents(0, held);
        float healthBefore = target.getHealth();
        try {
            player.attackTargetEntityWithCurrentItem(target);
        } finally {
            if (weaponSlot >= 0) inventory.setStackInSlot(weaponSlot, player.getHeldItemMainhand());
            clearPlayer(player);
        }
        return !target.isEntityAlive() || target.getHealth() < healthBefore;
    }

    static boolean hasUsableWeapon(ItemStackHandler inventory) {
        return inventory != null && selectBestWeapon(inventory, inventory.getSlots()) >= 0;
    }

    static boolean isWeapon(ItemStack stack) {
        return weaponDamage(stack) > 0.0D;
    }

    private static int selectBestWeapon(ItemStackHandler inventory, int activeSlots) {
        int selected = -1;
        double bestDamage = 0.0D;
        int limit = Math.min(inventory.getSlots(), Math.max(0, activeSlots));
        for (int slot = 0; slot < limit; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            double damage = weaponDamage(stack);
            if (damage > bestDamage) {
                selected = slot;
                bestDamage = damage;
            }
        }
        return selected;
    }

    private static double weaponDamage(ItemStack stack) {
        if (stack.isEmpty()) return 0.0D;
        if (stack.getItem() instanceof ItemDoubleLightsaber) return ItemDoubleLightsaber.ATTACK_DAMAGE;
        if (stack.getItem() instanceof ItemLightsaber) return ItemLightsaber.ATTACK_DAMAGE;
        double damage = 0.0D;
        for (AttributeModifier modifier : stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
                .get(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) damage += modifier.getAmount();
        return damage;
    }

    static void activateLightsaber(ItemStack stack) {
        if (stack.getItem() instanceof ItemDoubleLightsaber) ItemDoubleLightsaber.setActive(stack, true);
        else if (stack.getItem() instanceof ItemLightsaber) ItemLightsaber.setActive(stack, true);
    }

    static EntityFishHook castFishingHook(EntityProgrammableDrone drone, ItemStack rod, BlockPos target) {
        if (!(drone.world instanceof WorldServer) || rod.isEmpty()
                || !(rod.getItem() instanceof ItemFishingRod) || target == null) return null;
        WorldServer world = (WorldServer) drone.world;
        FakePlayer player = prepareFishingPlayer(drone, rod, null);
        EntityFishHook hook = new EntityFishHook(world, player,
                target.getX() + 0.5D, target.getY() + 0.35D, target.getZ() + 0.5D);
        hook.getEntityData().setString("DrTechDroneFishingOwner", drone.getDroneId().toString());
        hook.setLuck(EnchantmentHelper.getEnchantmentLevel(Enchantments.LUCK_OF_THE_SEA, rod));
        hook.setLureSpeed(EnchantmentHelper.getEnchantmentLevel(Enchantments.LURE, rod));
        player.fishEntity = hook;
        if (!world.spawnEntity(hook)) {
            player.fishEntity = null;
            clearPlayer(player);
            return null;
        }
        world.playSound(null, drone.posX, drone.posY, drone.posZ,
                SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F,
                0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
        return hook;
    }

    static boolean maintainFishingHook(EntityProgrammableDrone drone, ItemStack rod, EntityFishHook hook) {
        if (hook == null || hook.isDead || rod.isEmpty() || !(rod.getItem() instanceof ItemFishingRod)) return false;
        prepareFishingPlayer(drone, rod, hook);
        return true;
    }

    static void cleanupFishingHooks(EntityProgrammableDrone drone) {
        if (!(drone.world instanceof WorldServer)) return;
        String owner = drone.getDroneId().toString();
        for (EntityFishHook hook : drone.world.getEntities(EntityFishHook.class,
                candidate -> candidate != null && owner.equals(
                        candidate.getEntityData().getString("DrTechDroneFishingOwner")))) {
            hook.setDead();
        }
    }

    static boolean isFishingCatchReady(EntityFishHook hook) {
        if (hook == null || hook.isDead) return false;
        if (hook.caughtEntity != null) return true;
        Integer catchable = ObfuscationReflectionHelper.getPrivateValue(EntityFishHook.class, hook,
                "field_146045_ax", "ticksCatchable");
        return catchable != null && catchable > 0;
    }

    static FishingRetraction retractFishingHook(EntityProgrammableDrone drone, ItemStack rod, EntityFishHook hook) {
        if (hook == null || hook.isDead || rod.isEmpty()) {
            return new FishingRetraction(rod, java.util.Collections.emptyList());
        }
        AxisAlignedBB scanBounds = new AxisAlignedBB(
                Math.min(drone.posX, hook.posX) - 2.0D,
                Math.min(drone.posY, hook.posY) - 2.0D,
                Math.min(drone.posZ, hook.posZ) - 2.0D,
                Math.max(drone.posX, hook.posX) + 2.0D,
                Math.max(drone.posY, hook.posY) + 2.0D,
                Math.max(drone.posZ, hook.posZ) + 2.0D);
        java.util.Set<Integer> existingItems = new java.util.HashSet<>();
        for (EntityItem item : drone.world.getEntitiesWithinAABB(EntityItem.class, scanBounds)) {
            existingItems.add(item.getEntityId());
        }
        FakePlayer player = prepareFishingPlayer(drone, rod, hook);
        int damage = hook.handleHookRetraction();
        ItemStack updated = player.getHeldItemMainhand();
        if (damage > 0 && !updated.isEmpty()) updated.damageItem(damage, player);
        java.util.List<EntityItem> loot = new java.util.ArrayList<>();
        for (EntityItem item : drone.world.getEntitiesWithinAABB(EntityItem.class, scanBounds)) {
            if (!item.isDead && !existingItems.contains(item.getEntityId())) loot.add(item);
        }
        drone.world.playSound(null, drone.posX, drone.posY, drone.posZ,
                SoundEvents.ENTITY_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F,
                0.4F / (drone.world.rand.nextFloat() * 0.4F + 0.8F));
        player.fishEntity = null;
        clearPlayer(player);
        return new FishingRetraction(updated, loot);
    }

    private static FakePlayer prepareFishingPlayer(EntityProgrammableDrone drone, ItemStack rod,
            EntityFishHook hook) {
        WorldServer world = (WorldServer) drone.world;
        java.util.UUID fishingId = java.util.UUID.nameUUIDFromBytes(
                ("drtech:fishing:" + drone.getDroneId()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        FakePlayer player = FakePlayerFactory.get(world,
                new com.mojang.authlib.GameProfile(fishingId, "[DrTechFishing]"));
        clearPlayer(player);
        player.setPositionAndRotation(drone.posX, drone.posY, drone.posZ,
                drone.rotationYaw, drone.rotationPitch);
        player.interactionManager.setGameType(GameType.SURVIVAL);
        player.interactionManager.setBlockReachDistance(5.0D);
        player.inventory.setInventorySlotContents(0, rod.copy());
        player.fishEntity = hook;
        return player;
    }

    /** Uses CropQT's own empty-hand harvesting contract so the rack, crop identity and inherited stats survive. */
    private static boolean harvestCropStick(EntityProgrammableDrone drone, BlockPos target) {
        WorldServer world = (WorldServer) drone.world;
        TileEntity tile = world.getTileEntity(target);
        if (!(tile instanceof TileCropStick cropStick) || !cropStick.hasCrop() || !cropStick.isMature()) return false;
        String cropId = cropStick.getCropId();
        FakePlayer player = preparePlayer(drone);
        EnumActionResult result;
        try {
            result = player.interactionManager.processRightClickBlock(player, world, ItemStack.EMPTY,
                    EnumHand.MAIN_HAND, target, EnumFacing.UP, 0.5F, 0.5F, 0.5F);
        } finally {
            clearPlayer(player);
        }
        TileEntity updated = world.getTileEntity(target);
        return result == EnumActionResult.SUCCESS && updated instanceof TileCropStick harvested
                && cropId.equals(harvested.getCropId()) && harvested.getGrowthStage() == 0;
    }

    private static FakePlayer preparePlayer(EntityProgrammableDrone drone) {
        WorldServer world = (WorldServer) drone.world;
        java.util.UUID ownerId = drone.getOwnerId();
        net.minecraft.entity.player.EntityPlayer owner = ownerId == null ? null : world.getPlayerEntityByUUID(ownerId);
        DroneFakePlayerIdentity strategy = DrtConfig.DroneFakePlayerIdentityStrategy == null
                ? DroneFakePlayerIdentity.PER_DRONE : DrtConfig.DroneFakePlayerIdentityStrategy;
        FakePlayer player = FakePlayerFactory.get(world, strategy.profile(drone.getDroneId(), ownerId,
                owner == null ? null : owner.getGameProfile().getName()));
        clearPlayer(player);
        player.setPositionAndRotation(drone.posX, drone.posY, drone.posZ, drone.rotationYaw, drone.rotationPitch);
        player.interactionManager.setGameType(GameType.SURVIVAL);
        player.interactionManager.setBlockReachDistance(5.0D);
        player.inventory.currentItem = 0;
        return player;
    }

    private static void clearPlayer(EntityPlayerMP player) {
        player.setSneaking(false);
        player.inventory.clear();
        player.inventory.currentItem = 0;
    }

    private static int selectBestTool(ItemStackHandler inventory, IBlockState state) {
        int selected = -1;
        float bestSpeed = 1.0F;
        boolean bestCanHarvest = false;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            boolean canHarvest = stack.canHarvestBlock(state);
            float speed = stack.getDestroySpeed(state);
            if (selected < 0 || canHarvest && !bestCanHarvest || canHarvest == bestCanHarvest && speed > bestSpeed) {
                selected = slot;
                bestSpeed = speed;
                bestCanHarvest = canHarvest;
            }
        }
        return selected;
    }

    private static int findPlaceableItem(ItemStackHandler inventory, DroneItemFilter filter) {
        DroneItemFilter effectiveFilter = filter == null ? DroneItemFilter.ANY : filter;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock && effectiveFilter.matches(stack)) return slot;
        }
        return -1;
    }

    private static int findMatchingItem(ItemStackHandler inventory, DroneItemFilter filter) {
        DroneItemFilter effectiveFilter = filter == null ? DroneItemFilter.ANY : filter;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && effectiveFilter.matches(stack)) return slot;
        }
        return -1;
    }

    private static EnumFacing findSupport(WorldServer world, BlockPos target) {
        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos support = target.offset(direction);
            IBlockState state = world.getBlockState(support);
            if (!state.getBlock().isReplaceable(world, support)
                    && state.isSideSolid(world, support, direction.getOpposite())) {
                return direction;
            }
        }
        return null;
    }
}
