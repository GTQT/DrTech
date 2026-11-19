package com.drppp.drtech.personalspace.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

import com.drppp.drtech.personalspace.blocks.tile.TilePersonalPortal;

public class PWTeleporter extends Teleporter {

    private final BlockPos pos;
    TilePersonalPortal tpp;

    public PWTeleporter(WorldServer worldIn, BlockPos pos) {
        super(worldIn);
        this.pos = pos;
    }

    public PWTeleporter(WorldServer worldIn, TilePersonalPortal tpp) {
        super(worldIn);
        this.tpp = tpp;
        this.pos = tpp.getPos();
    }

    @Override
    public boolean isVanilla() {
        return false;
    }

    @Override
    public boolean makePortal(Entity entityIn) {
        return true;
    }

    @Override
    public void placeInPortal(Entity entityIn, float rotationYaw) {
        placeInExistingPortal(entityIn, rotationYaw);
    }

    @Override
    public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
        entityIn.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, rotationYaw, 0.0F);
        entityIn.motionX = 0.0F;
        entityIn.motionY = 0.0F;
        entityIn.motionZ = 0.0F;
        return true;
    }

    @Override
    public void removeStalePortalLocations(long worldTime) {}
}
