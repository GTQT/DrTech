package com.drppp.drtech.lootgames.api.minigame;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import com.drppp.drtech.lootgames.api.packet.SMessageGameUpdate;
import com.drppp.drtech.lootgames.api.task.TEPostponeTaskScheduler;
import com.drppp.drtech.lootgames.api.tileentity.TileEntityGameMaster;
import com.drppp.drtech.lootgames.packets.NetworkHandler;

import java.util.List;

public abstract class LootGame {
    protected TileEntityGameMaster<?> masterTileEntity;
    protected TEPostponeTaskScheduler serverTaskPostponer;

    private Runnable winCallback;
    private Runnable loseCallback;
    private Runnable endGameCallback;

    public void setMasterTileEntity(TileEntityGameMaster<?> masterTileEntity) {
        this.masterTileEntity = masterTileEntity;
    }

    public void init() {
        this.serverTaskPostponer = new TEPostponeTaskScheduler(masterTileEntity);
    }

    public void onTick() {
        if (isServerSide()) {
            serverTaskPostponer.onUpdate();
        }
    }

    public boolean isServerSide() {
        return !masterTileEntity.getWorld().isRemote;
    }

    public World getWorld() {
        return masterTileEntity.getWorld();
    }

    public BlockPos getMasterPos() {
        return masterTileEntity.getPos();
    }

    protected final void winGame() {
        onGameEnded();
        if (winCallback != null) winCallback.run();
    }

    protected abstract BlockPos getCentralRoomPos();

    protected final void loseGame() {
        onGameEnded();
        if (loseCallback != null) loseCallback.run();
    }

    private void onGameEnded() {
        if (endGameCallback != null) endGameCallback.run();
    }

    public int getDefaultBroadcastDistance() {
        return 17;
    }

    protected abstract BlockPos getRoomFloorPos();

    public void setOnWin(Runnable onWin) {
        this.winCallback = onWin;
    }

    public void setOnLose(Runnable onLose) {
        this.loseCallback = onLose;
    }

    public void setOnGameEnded(Runnable onEnd) {
        this.endGameCallback = onEnd;
    }

    public void saveDataAndSendToClient() {
        masterTileEntity.setBlockToUpdateAndSave();
    }

    public void saveData() {
        masterTileEntity.markDirty();
    }

    public NBTTagCompound writeNBTForSaving() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeCommonNBT(nbt);
        nbt.setTag("task_scheduler", serverTaskPostponer.serializeNBT());
        return nbt;
    }

    public void readNBTFromSave(NBTTagCompound compound) {
        readCommonNBT(compound);
        serverTaskPostponer.deserializeNBT((NBTTagList) compound.getTag("task_scheduler"));
    }

    public NBTTagCompound writeNBTForClient() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeCommonNBT(nbt);
        return nbt;
    }

    public void sendUpdatePacket(String key, NBTTagCompound compoundToSend) {
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(
                getWorld().provider.getDimension(),
                masterTileEntity.getPos().getX(),
                masterTileEntity.getPos().getY(),
                masterTileEntity.getPos().getZ(), -1);
        NetworkHandler.INSTANCE.sendToAllTracking(new SMessageGameUpdate(masterTileEntity.getPos(), key, compoundToSend), point);
    }

    @SideOnly(Side.CLIENT)
    public void onUpdatePacket(String key, @Nullable NBTTagCompound compoundIn) {
    }

    @SideOnly(Side.CLIENT)
    public void readNBTFromClient(NBTTagCompound compound) {
        readCommonNBT(compound);
    }

    public void writeCommonNBT(NBTTagCompound compound) {
    }

    public void readCommonNBT(NBTTagCompound compound) {
    }
}
