package com.drppp.drtech.lootgames.api.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import com.drppp.drtech.lootgames.api.minigame.LootGame;

import javax.annotation.Nonnull;

public abstract class TileEntityGameMaster<T extends LootGame> extends TileEntity implements ITickable {
    protected T game;

    public TileEntityGameMaster(T game) {
        this.game = game;
        game.setMasterTileEntity(this);
        game.init();
    }

    @Override
    public void update() {
        game.onTick();
    }

    public abstract void destroyGameBlocks();

    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound compound) {
        writeNBTForSaving(compound);
        compound.setTag("game", game.writeNBTForSaving());
        writeCommonNBT(compound);
        return compound;
    }

    @Override
    public final void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("client_flag")) {
            readNBTFromClient(compound);
            game.readNBTFromClient(compound.getCompoundTag("game_synced"));
        } else {
            readNBTFromSave(compound);
            game.readNBTFromSave(compound.getCompoundTag("game"));
        }
        readCommonNBT(compound);
    }

    protected NBTTagCompound writeCommonNBT(NBTTagCompound compound) {
        return compound;
    }

    protected void readCommonNBT(NBTTagCompound compound) {
    }

    protected NBTTagCompound writeNBTForSaving(NBTTagCompound compound) {
        writeCommonNBT(compound);
        return super.writeToNBT(compound);
    }

    protected void readNBTFromSave(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    protected void readNBTFromClient(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    protected NBTTagCompound writeNBTForClient(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }

    @Nonnull
    @Override
    public final NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        writeNBTForClient(compound);
        writeCommonNBT(compound);
        compound.setTag("game_synced", game.writeNBTForClient());
        compound.setByte("client_flag", (byte) 0);
        return compound;
    }

    @Override
    public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound compound = pkt.getNbtCompound();
        readNBTFromClient(compound);
        readCommonNBT(compound);
        game.readNBTFromClient(compound.getCompoundTag("game_synced"));
    }

    @Override
    public final SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
    }

    public void setBlockToUpdateAndSave() {
        world.notifyBlockUpdate(pos, getState(), getState(), 3);
        markDirty();
    }

    public void onSubordinateBlockClicked(BlockPos subordinatePos, EntityPlayer player) {
    }

    public IBlockState getState() {
        return world.getBlockState(pos);
    }

    public T getGame() {
        return game;
    }
}
