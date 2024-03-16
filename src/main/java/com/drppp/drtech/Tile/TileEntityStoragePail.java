package com.drppp.drtech.Tile;

import com.drppp.drtech.Client.Textures;
import com.drppp.drtech.api.ItemHandler.PailItemStackHandler;
import com.drppp.drtech.api.TileEntity.TileEntityWithUI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class TileEntityStoragePail extends TileEntityWithUI {

    public PailItemStackHandler inventory = new PailItemStackHandler(243);
    public TileEntityStoragePail(){

    }
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("StoragePail"))
            inventory.deserializeNBT(compound.getCompoundTag("StoragePail"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("StoragePail",inventory.serializeNBT());
        return compound;
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        builder = ModularUI.builder(Textures.PAIL_BACKGROUND, 500, 500);
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 27; x++)
                builder.slot(inventory, y * 27 + x, 7 + (18 * x), 130+18 + (18 * y));
        builder.bindPlayerInventory(entityPlayer.inventory,GuiTextures.SLOT,169, 312);
        return builder.build(this, entityPlayer);
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

    @Override
    public void update() {

    }

}
