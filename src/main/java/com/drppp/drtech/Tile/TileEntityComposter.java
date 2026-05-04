package com.drppp.drtech.Tile;

import com.drppp.drtech.common.Blocks.BlockComposter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class TileEntityComposter extends TileEntity {
    private final IItemHandler topHandler = new TopHandler();
    private final IItemHandler bottomHandler = new BottomHandler();

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return facing == EnumFacing.UP || facing == EnumFacing.DOWN;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == EnumFacing.UP) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(topHandler);
            }
            if (facing == EnumFacing.DOWN) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(bottomHandler);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    private BlockComposter getBlock() {
        if (world == null) {
            return null;
        }
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BlockComposter ? (BlockComposter) state.getBlock() : null;
    }

    private class TopHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            BlockComposter block = getBlock();
            if (block == null || world == null || stack.isEmpty()) {
                return stack;
            }
            return block.insertForAutomation(world, pos, world.getBlockState(pos), stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }

    private class BottomHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            BlockComposter block = getBlock();
            if (block == null || world == null) {
                return ItemStack.EMPTY;
            }
            return block.extractProduce(world, pos, world.getBlockState(pos), true);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }
            BlockComposter block = getBlock();
            if (block == null || world == null) {
                return ItemStack.EMPTY;
            }
            return block.extractProduce(world, pos, world.getBlockState(pos), simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    }
}
