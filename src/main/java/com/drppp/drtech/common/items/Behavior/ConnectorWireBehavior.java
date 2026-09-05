package com.drppp.drtech.common.Items.Behavior;

import com.drppp.drtech.Tile.TileEntityConnector;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ConnectorWireBehavior implements IItemBehaviour {
    private static final String LINK_TAG = "DrtechConnectorLink";

    private final int wireTier;

    public ConnectorWireBehavior(int wireTier) {
        this.wireTier = wireTier;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        TileEntity tileEntity = world.getTileEntity(pos);
        ItemStack stack = player.getHeldItem(hand);
        StoredLink storedLink = readStoredLink(stack);
        if (!(tileEntity instanceof TileEntityConnector) && storedLink == null) {
            return EnumActionResult.PASS;
        }

        if (player.isSneaking() && hasStoredLink(stack)) {
            if (!world.isRemote) {
                clearStoredLink(stack);
                sendMessage(player, "message.drtech.connector_wire.cleared");
            }
            return EnumActionResult.SUCCESS;
        }

        if (tileEntity instanceof TileEntityConnector && !((TileEntityConnector) tileEntity).canConnectWire(wireTier)) {
            if (!world.isRemote) {
                sendMessage(player, "message.drtech.connector_wire.wrong_tier");
            }
            return EnumActionResult.FAIL;
        }
        if (!(tileEntity instanceof TileEntityConnector) && getEnergyConnectableSide(tileEntity, side) == null) {
            return storedLink == null ? EnumActionResult.PASS : EnumActionResult.FAIL;
        }

        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (storedLink == null) {
            storeLink(stack, world, pos);
            sendMessage(player, "message.drtech.connector_wire.saved");
            return EnumActionResult.SUCCESS;
        }

        EnumActionResult result = tryConnect(player, world, stack, storedLink, pos, side);
        if (result != EnumActionResult.SUCCESS) {
            clearStoredLink(stack);
        }
        return result;
    }

    private EnumActionResult tryConnect(EntityPlayer player, World world, ItemStack stack, StoredLink storedLink, BlockPos targetPos, EnumFacing targetSide) {
        if (storedLink.dimension != world.provider.getDimension()) {
            sendMessage(player, "message.drtech.connector_wire.wrong_dimension");
            return EnumActionResult.FAIL;
        }
        if (storedLink.pos.equals(targetPos)) {
            sendMessage(player, "message.drtech.connector_wire.same_connector");
            return EnumActionResult.FAIL;
        }
        if (storedLink.wireTier != wireTier) {
            sendMessage(player, "message.drtech.connector_wire.invalid");
            return EnumActionResult.FAIL;
        }
        if (!world.isBlockLoaded(storedLink.pos)) {
            sendMessage(player, "message.drtech.connector_wire.invalid");
            return EnumActionResult.FAIL;
        }

        TileEntity firstTile = world.getTileEntity(storedLink.pos);
        TileEntity secondTile = world.getTileEntity(targetPos);
        if (!(firstTile instanceof TileEntityConnector) || secondTile == null) {
            sendMessage(player, "message.drtech.connector_wire.invalid");
            return EnumActionResult.FAIL;
        }

        TileEntityConnector first = (TileEntityConnector) firstTile;
        boolean targetIsConnector = secondTile instanceof TileEntityConnector;
        if (!first.canConnectWire(wireTier) || (targetIsConnector && !((TileEntityConnector) secondTile).canConnectWire(wireTier))) {
            sendMessage(player, "message.drtech.connector_wire.wrong_tier");
            return EnumActionResult.FAIL;
        }
        EnumFacing normalizedTargetSide = targetIsConnector ? targetSide : getEnergyConnectableSide(secondTile, targetSide);
        if (!targetIsConnector && normalizedTargetSide == null) {
            sendMessage(player, "message.drtech.connector_wire.invalid");
            return EnumActionResult.FAIL;
        }
        if (first.hasConnection(targetPos) || (targetIsConnector && ((TileEntityConnector) secondTile).hasConnection(storedLink.pos))) {
            sendMessage(player, "message.drtech.connector_wire.exists");
            return EnumActionResult.FAIL;
        }

        int length = MathHelper.ceil(Math.sqrt(storedLink.pos.distanceSq(targetPos)));
        int maxLength = TileEntityConnector.getMaxWireLength(wireTier);
        if (length > maxLength) {
            sendMessage(player, "message.drtech.connector_wire.too_far", maxLength);
            return EnumActionResult.FAIL;
        }
        if (isObstructed(world, storedLink.pos, targetPos)) {
            sendMessage(player, "message.drtech.connector_wire.blocked");
            return EnumActionResult.FAIL;
        }

        boolean connected;
        if (targetIsConnector) {
            connected = TileEntityConnector.connect(world, storedLink.pos, targetPos, wireTier);
        } else {
            connected = first.addMachineConnection(targetPos, wireTier, length, normalizedTargetSide);
        }
        if (!connected) {
                sendMessage(player, "message.drtech.connector_wire.invalid");
                return EnumActionResult.FAIL;
        }

        clearStoredLink(stack);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        sendMessage(player, "message.drtech.connector_wire.connected", length);
        return EnumActionResult.SUCCESS;
    }

    private static EnumFacing getEnergyConnectableSide(TileEntity tileEntity, EnumFacing preferredSide) {
        if (isEnergyConnectable(tileEntity, preferredSide)) {
            return preferredSide;
        }
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side != preferredSide && isEnergyConnectable(tileEntity, side)) {
                return side;
            }
        }
        return null;
    }

    private static boolean isEnergyConnectable(TileEntity tileEntity, EnumFacing side) {
        if (tileEntity == null) {
            return false;
        }
        IEnergyContainer energyContainer = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
        return energyContainer != null && (energyContainer.inputsEnergy(side) || energyContainer.outputsEnergy(side));
    }

    private static boolean isObstructed(World world, BlockPos start, BlockPos end) {
        double distance = Math.sqrt(start.distanceSq(end));
        int steps = Math.max(16, MathHelper.ceil(distance * 8.0D));
        for (int i = 1; i < steps; i++) {
            double progress = i / (double) steps;
            double x = start.getX() + 0.5D + (end.getX() - start.getX()) * progress;
            double y = start.getY() + 0.5D + (end.getY() - start.getY()) * progress;
            double z = start.getZ() + 0.5D + (end.getZ() - start.getZ()) * progress;
            BlockPos checkPos = new BlockPos(x, y, z);
            if (checkPos.equals(start) || checkPos.equals(end)) {
                continue;
            }

            IBlockState state = world.getBlockState(checkPos);
            if (state.getBlock().isAir(state, world, checkPos)) {
                continue;
            }

            AxisAlignedBB collisionBox = state.getCollisionBoundingBox(world, checkPos);
            if (collisionBox != null && collisionBox != Block.NULL_AABB) {
                return true;
            }
        }
        return false;
    }

    private void storeLink(ItemStack stack, World world, BlockPos pos) {
        NBTTagCompound stackTag = stack.getTagCompound();
        if (stackTag == null) {
            stackTag = new NBTTagCompound();
            stack.setTagCompound(stackTag);
        }

        NBTTagCompound linkTag = new NBTTagCompound();
        linkTag.setInteger("Dimension", world.provider.getDimension());
        linkTag.setInteger("X", pos.getX());
        linkTag.setInteger("Y", pos.getY());
        linkTag.setInteger("Z", pos.getZ());
        linkTag.setInteger("WireTier", wireTier);
        stackTag.setTag(LINK_TAG, linkTag);
    }

    private StoredLink readStoredLink(ItemStack stack) {
        if (!hasStoredLink(stack)) {
            return null;
        }

        NBTTagCompound linkTag = stack.getTagCompound().getCompoundTag(LINK_TAG);
        return new StoredLink(
                linkTag.getInteger("Dimension"),
                new BlockPos(linkTag.getInteger("X"), linkTag.getInteger("Y"), linkTag.getInteger("Z")),
                linkTag.getInteger("WireTier"));
    }

    private static boolean hasStoredLink(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(LINK_TAG);
    }

    private static void clearStoredLink(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return;
        }

        NBTTagCompound stackTag = stack.getTagCompound();
        stackTag.removeTag(LINK_TAG);
        if (stackTag.getKeySet().isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    private static void sendMessage(EntityPlayer player, String key, Object... args) {
        player.sendStatusMessage(new TextComponentTranslation(key, args), true);
    }

    private static class StoredLink {
        private final int dimension;
        private final BlockPos pos;
        private final int wireTier;

        private StoredLink(int dimension, BlockPos pos, int wireTier) {
            this.dimension = dimension;
            this.pos = pos;
            this.wireTier = wireTier;
        }
    }
}
