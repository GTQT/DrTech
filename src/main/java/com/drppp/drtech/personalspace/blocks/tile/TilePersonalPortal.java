package com.drppp.drtech.personalspace.blocks.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import org.jetbrains.annotations.Nullable;

import com.drppp.drtech.personalspace.PersonalSpaceModule;
import com.drppp.drtech.personalspace.blocks.BlockPersonalPortal;
import com.drppp.drtech.personalspace.packet.Packets;
import com.drppp.drtech.personalspace.proxy.CommonProxy;
import com.drppp.drtech.personalspace.world.DimensionConfig;
import com.drppp.drtech.personalspace.world.PWTeleporter;
import com.drppp.drtech.personalspace.world.PWWorldProvider;

public class TilePersonalPortal extends TileEntity implements IWorldNameable, ITickable {

    private boolean isActive = false;
    private int targetID = 0;

    private BlockPos targetPos = new BlockPos(8, 8, 8);

    private String customName = "";

    private float bookRot = 0.0f;
    private float bookRotPrev = 0.0f;

    @Override
    public void update() {
        this.bookRotPrev = this.bookRot;
        EntityPlayer player = this.world.getClosestPlayer((this.pos.getX() + 0.5), (this.pos.getY() + 0.5),
                (this.pos.getZ() + 0.5), 3.0D, false);
        if (player != null) {
            double d0 = player.posX - (this.pos.getX() + 0.5F);
            double d1 = player.posZ - (this.pos.getZ() + 0.5F);
            this.bookRot = (float) MathHelper.atan2(d0, d1);

        }
        // if (bookRot > PI / 2) {
        // bookRot = (2.5f * (float)PI) - bookRot;
        // } else {
        // bookRot = (float)PI / 2.0f - bookRot;
        // }

        while (bookRot > Math.PI) {
            bookRot -= 2.0 * Math.PI;
        }
        while (bookRot < -Math.PI) {
            bookRot += 2.0 * Math.PI;
        }
    }

    public void transport(EntityPlayerMP player, BlockPos pos) {
        if (world.isRemote || !this.isActive || player == null) {
            return;
        }
        int x = player.getPosition().getX() - pos.getX();
        int z = player.getPosition().getZ() - pos.getZ();

        PWTeleporter tp = new PWTeleporter((WorldServer) world,
                new BlockPos(targetPos.getX() + x, targetPos.getY(), targetPos.getZ() + z));
        player.changeDimension(this.targetID, tp);
    }

    public void linkOtherPortal(boolean spawnPortal, EntityPlayerMP player) {
        if (!this.isActive)
            return;
        if (world.isRemote)
            return;

        WorldServer otherWorld = DimensionManager.getWorld(this.targetID);
        if (otherWorld == null) {
            DimensionManager.initDimension(this.targetID);
            otherWorld = DimensionManager.getWorld(this.targetID);
        }
        if (otherWorld == null) {
            PersonalSpaceModule.log.fatal("Couldn't initialize world {}", this.targetID);
            return;
        }

        int otherX = pos.getX(), otherY = pos.getY(), otherZ = pos.getZ();
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(otherX, otherY, otherZ);
        searchloop:
        for (otherX = pos.getX() - 1; otherX < pos.getX() + 1; otherX++) {
            for (otherY = pos.getY() - 1; otherY < pos.getY() + 1; otherY++) {
                if (otherY < 0 || otherY > otherWorld.getHeight()) continue;

                for (otherZ = pos.getZ() - 1; otherZ < pos.getZ() + 1; otherZ++) {
                    if (!otherWorld.getChunkProvider().chunkExists(otherX, otherZ)) {
                        otherWorld.getChunkProvider().loadChunk(otherX >> 4, otherZ >> 4);
                    }
                    blockPos.setPos(otherX, otherY, otherZ);
                    if (otherWorld.getBlockState(blockPos).getBlock() instanceof BlockPersonalPortal) {
                        break searchloop;
                    }
                }
            }
        }
        DimensionConfig cfg = DimensionConfig.getConfig(this.targetID, false);
        TilePersonalPortal otherPortal = null;
        if (otherWorld.getBlockState(blockPos).getBlock() instanceof BlockPersonalPortal) {
            TileEntity te = otherWorld.getTileEntity(blockPos.toImmutable());
            if (te instanceof TilePersonalPortal)
                otherPortal = (TilePersonalPortal) te;
        } else if (spawnPortal) {
            otherX = targetPos.getX();
            otherY = targetPos.getY();
            otherZ = targetPos.getZ();
            BlockPos newPos = new BlockPos(otherX, otherY, otherZ);
            otherWorld.setBlockState(newPos, CommonProxy.blockPersonalPortal.getDefaultState(), 3);
            otherPortal = (TilePersonalPortal) otherWorld.getTileEntity(newPos);
            if (cfg.getLayers().isEmpty() || cfg.getLayersAsString().equals("minecraft:air")) {
                otherPortal.setPos(new BlockPos(targetPos.getX(), 128, targetPos.getZ()));
                for (int x = 5; x < 12; x++) {
                    for (int z = 5; z < 12; z++) {
                        otherWorld.setBlockState(new BlockPos(x, newPos.getY() - 1, z), Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
        if (otherPortal != null) {
            otherPortal.isActive = true;
            DimensionConfig otherPortalCfg = DimensionConfig.getConfig(otherPortal.targetID, false);
            if (otherPortal.targetID != world.provider.getDimension() && otherPortalCfg != null) {
                if (player != null) {
                    player.sendMessage(new TextComponentTranslation("chat.personalWorld.relinked.error"));
                }
                return;
            }
            otherPortal.targetID = world.provider.getDimension();
            otherPortal.targetPos = pos;
            otherPortal.markDirty();
            if (player != null) {
                player.sendMessage(new TextComponentTranslation("chat.personalWorld.relinked", targetID));
            }
            PersonalSpaceModule.log.info(
                    "Linked portal at {}:{},{},{} to {}:{},{},{}",
                    targetID,
                    otherX,
                    otherY,
                    otherZ,
                    world.provider.getDimension(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ());
        }
    }

    public void updateSettings(EntityPlayerMP player, DimensionConfig conf, String name) {
        if (world.isRemote || player == null) {
            return;
        }

        if (!world.canMineBlockBody(player, pos)) {
            player.sendMessage(new TextComponentTranslation("chat.personalWorld.denied"));
            return;
        }

        boolean createNewDim = false;
        int targetID = 0;
        if (this.world.provider instanceof PWWorldProvider) {
            targetID = this.world.provider.getDimension();
        } else if (this.isActive) {
            targetID = this.targetID;
        }
        if (targetID > 0) {
            DimensionConfig existingConfig = DimensionConfig.getConfig(targetID, false);
            if (existingConfig == null) {
                return;
            }
            conf.setDimID(targetID);
            existingConfig.copyFrom(conf);
            existingConfig.update();
            WorldServer targetWorld = DimensionManager.getWorld(targetID);
            if (targetWorld != null && targetWorld.provider instanceof PWWorldProvider provider) {
                provider.refreshConfigCache();
            }
        } else {
            if (this.world.provider.getDimension() != 0) {
                return;
            }
            targetID = DimensionManager.getNextFreeDimId();
            conf.setDimID(targetID);
            if (!conf.registerWithDimManager(false, true)) {
                player.sendMessage(new TextComponentTranslation("chat.personalWorld.failed"));
                return;
            }
            this.isActive = true;
            this.targetID = targetID;
            this.targetPos = new BlockPos(this.targetPos.getX(), conf.getGroundLevel(), this.targetPos.getZ());
            markDirty();
            createNewDim = true;

            linkOtherPortal(true, player);
        }
        this.setCustomName(name);
        Packets.INSTANCE.sendWorldList().sendToClients();
        if (createNewDim) {
            player.sendMessage(new TextComponentTranslation("chat.personalWorld.created"));
        } else {
            player.sendMessage(new TextComponentTranslation("chat.personalWorld.updated"));
        }
    }

    public void sendToClient() {
        if (world != null) {
            world.markBlockRangeForRenderUpdate(pos, pos);
            world.notifyBlockUpdate(pos, this.getBlockType().getStateFromMeta(this.getBlockMetadata()),
                    this.getBlockType().getStateFromMeta(this.getBlockMetadata()), 3);
            world.scheduleBlockUpdate(pos, this.getBlockType(), 0, 0);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("active", this.isActive);
        compound.setIntArray("target",
                new int[] { this.targetID, this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ() });
        compound.setString("name", this.customName);
        super.writeToNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("active")) {
            this.isActive = compound.getBoolean("active");
        }
        if (compound.hasKey("target")) {
            int[] array = compound.getIntArray("target");
            this.targetID = array[0];
            this.targetPos = new BlockPos(array[1], array[2], array[3]);
        }
        if (compound.hasKey("name")) {
            this.customName = compound.getString("name");
        }
        this.markDirty();
        this.sendToClient();
    }

    @Override
    public String getName() {
        return customName.isEmpty() ? null : customName;
    }

    @Nullable
    @Override
    public ITextComponent getDisplayName() {
        return customName.isEmpty() ? null : new TextComponentString(customName);
    }

    @Override
    public boolean hasCustomName() {
        return !customName.isEmpty();
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public boolean isActive() {
        return isActive;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public int getTargetID() {
        return targetID;
    }

    public String getCustomName() {
        return customName;
    }

    public float getBookRot() {
        return bookRot;
    }

    public float getBookRotPrev() {
        return bookRotPrev;
    }
}
