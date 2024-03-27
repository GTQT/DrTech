package com.drppp.drtech.Tile;

import com.drppp.drtech.api.ItemHandler.PailItemStackHandler;
import gregtech.api.util.GTTransferUtils;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntitySapBag  extends TileEntity implements ITickable{

    public ItemStackHandler inventory = new ItemStackHandler(4);
    private int ticks=0;
    @Override
    public void update() {
        if (!world.isRemote && ++ticks > 300) {
            ticks = 0;
            // 定义所有六个方向
            EnumFacing[] directions = EnumFacing.values();

            for (EnumFacing direction : directions) {
                BlockPos checkPos = pos.offset(direction);

                Block block = world.getBlockState(checkPos).getBlock();
                if (block == MetaBlocks.RUBBER_LOG) {
                    List<ItemStack> list = new ArrayList<>();
                    list.add(new ItemStack(MetaItems.STICKY_RESIN.getMetaItem(), 1, 438));

                    if (GTTransferUtils.addItemsToItemHandler(inventory, true, list)) {
                        GTTransferUtils.addItemsToItemHandler(inventory, false, list);
                    }
                }
            }
        }
    }
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("Storage"))
            inventory.deserializeNBT(compound.getCompoundTag("Storage"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Storage",inventory.serializeNBT());
        return compound;
    }
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability== CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventory);
        }
        return super.getCapability(capability, facing);
    }
}
