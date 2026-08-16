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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.items.ItemStackHandler;


/** Event-compatible server world actions executed through a dedicated Forge fake player. */
final class DroneWorldActions {

    enum InteractionOutcome { SUCCESS, NO_ITEM, DENIED }

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
