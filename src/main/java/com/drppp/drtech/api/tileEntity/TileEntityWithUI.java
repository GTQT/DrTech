package com.drppp.drtech.api.TileEntity;

import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEntityWithUI extends TileEntity implements ITickable, IUIHolder {
    @Override
    public boolean isValid() {
        return !super.isInvalid();
    }

    @Override
    public boolean isRemote() {
        return this.world.isRemote;
    }

    @Override
    public int getUIColorOverride() {
        return IUIHolder.super.getUIColorOverride();
    }

    @Override
    public void markAsDirty() {
        this.markDirty();
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    @Override
    public void update() {

    }

    public ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }
}
