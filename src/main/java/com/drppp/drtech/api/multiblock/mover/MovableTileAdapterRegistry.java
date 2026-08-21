package com.drppp.drtech.api.multiblock.mover;

import com.drppp.drtech.Tags;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.RelativeDirection;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MovableTileAdapterRegistry {
    public static final ResourceLocation GT_META_TILE_ENTITY =
            new ResourceLocation(Tags.MODID, "gt_meta_tile_entity");

    private static final Map<ResourceLocation, IMovableTileAdapter> ADAPTERS = new LinkedHashMap<>();
    private static boolean builtinsRegistered;

    private MovableTileAdapterRegistry() {
    }

    public static synchronized void register(ResourceLocation id, IMovableTileAdapter adapter) {
        if (id == null || adapter == null) throw new IllegalArgumentException("Adapter id and instance are required");
        ensureBuiltins();
        if (ADAPTERS.containsKey(id)) throw new IllegalArgumentException("Duplicate movable tile adapter: " + id);
        ADAPTERS.put(id, adapter);
    }

    @Nullable
    public static synchronized AdapterEntry find(TileEntity tileEntity) {
        ensureBuiltins();
        for (Map.Entry<ResourceLocation, IMovableTileAdapter> entry : ADAPTERS.entrySet()) {
            if (entry.getValue().supports(tileEntity)) {
                return new AdapterEntry(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }

    @Nullable
    public static synchronized IMovableTileAdapter get(ResourceLocation id) {
        ensureBuiltins();
        return ADAPTERS.get(id);
    }

    public static synchronized Map<ResourceLocation, IMovableTileAdapter> view() {
        ensureBuiltins();
        return Collections.unmodifiableMap(new LinkedHashMap<>(ADAPTERS));
    }

    private static void ensureBuiltins() {
        if (builtinsRegistered) return;
        builtinsRegistered = true;
        ADAPTERS.put(GT_META_TILE_ENTITY, new IRotatableTileAdapter() {
            @Override
            public boolean supports(TileEntity tileEntity) {
                return tileEntity instanceof MetaTileEntityHolder;
            }

            @Override
            public boolean canMove(EntityPlayerMP player, TileEntity tileEntity) {
                MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
                return metaTileEntity != null && metaTileEntity.canBeModifiedBy(player);
            }

            @Override
            public NBTTagCompound capture(TileEntity tileEntity) {
                return tileEntity.writeToNBT(new NBTTagCompound());
            }

            @Override
            public boolean canRotate(EntityPlayerMP player, TileEntity tileEntity, Rotation rotation) {
                // Capture already called canMove(), including the ownership check.
                // Re-running canBeModifiedBy() here produces false negatives for
                // formed multiblock parts whose controller linkage is projected.
                MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
                return metaTileEntity != null;
            }

            @Override
            public IBlockState rotateState(IBlockState state, Rotation rotation) {
                return state.getBlock().withRotation(state, rotation);
            }

            @Override
            public void relocateNbt(NBTTagCompound tag, net.minecraft.util.math.BlockPos source,
                                    net.minecraft.util.math.BlockPos destination) {
                tag.setInteger("x", destination.getX());
                tag.setInteger("y", destination.getY());
                tag.setInteger("z", destination.getZ());
                translateFormedPieceCenters(tag.getCompoundTag("MetaTileEntity"),
                        destination.subtract(source));
            }

            @Override
            public void rotateNbt(NBTTagCompound tag, Rotation rotation) {
                NBTTagCompound meta = tag.getCompoundTag("MetaTileEntity");
                EnumFacing oldFront = null;
                EnumFacing newFront = null;
                if (meta.hasKey("FrontFacing", 3)) {
                    oldFront = EnumFacing.byIndex(meta.getInteger("FrontFacing"));
                    newFront = rotation.rotate(oldFront);
                    meta.setInteger("FrontFacing", newFront.getIndex());
                }
                if (oldFront != null && meta.hasKey("UpwardsFacing", 99)) {
                    EnumFacing oldUpwards = EnumFacing.byIndex(meta.getInteger("UpwardsFacing"));
                    EnumFacing newUpwards = oldFront.getAxis() == EnumFacing.Axis.Y
                            ? rotation.rotate(oldUpwards)
                            : RelativeDirection.simulateAxisRotation(
                                    newFront, oldFront, oldUpwards);
                    meta.setByte("UpwardsFacing", (byte) newUpwards.getIndex());
                }
                rotateFormedPieceCenters(tag, meta, rotation);
                NBTTagList covers = meta.getTagList("Covers", 10);
                for (int i = 0; i < covers.tagCount(); i++) {
                    NBTTagCompound cover = covers.getCompoundTagAt(i);
                    if (!cover.hasKey("Side", 1)) continue;
                    EnumFacing side = EnumFacing.byIndex(cover.getByte("Side"));
                    cover.setByte("Side", (byte) rotation.rotate(side).getIndex());
                }
            }
        });
    }

    private static void rotateFormedPieceCenters(NBTTagCompound holderTag,
                                                 NBTTagCompound meta, Rotation rotation) {
        if (!holderTag.hasKey("x", 99) || !holderTag.hasKey("y", 99)
                || !holderTag.hasKey("z", 99) || !meta.hasKey("FormedMetadata", 10)) return;
        net.minecraft.util.math.BlockPos origin = new net.minecraft.util.math.BlockPos(
                holderTag.getInteger("x"), holderTag.getInteger("y"), holderTag.getInteger("z"));
        NBTTagCompound centers = meta.getCompoundTag("FormedMetadata")
                .getCompoundTag("PieceCenters");
        for (String key : centers.getKeySet()) {
            net.minecraft.util.math.BlockPos center = net.minecraft.util.math.BlockPos.fromLong(
                    centers.getLong(key));
            net.minecraft.util.math.BlockPos relative = center.subtract(origin);
            net.minecraft.util.math.BlockPos rotated = rotatePosition(relative, rotation).add(origin);
            centers.setLong(key, rotated.toLong());
        }
    }

    private static void translateFormedPieceCenters(NBTTagCompound meta,
                                                    net.minecraft.util.math.BlockPos offset) {
        if (!meta.hasKey("FormedMetadata", 10)) return;
        NBTTagCompound centers = meta.getCompoundTag("FormedMetadata")
                .getCompoundTag("PieceCenters");
        for (String key : centers.getKeySet()) {
            net.minecraft.util.math.BlockPos center = net.minecraft.util.math.BlockPos.fromLong(
                    centers.getLong(key));
            centers.setLong(key, center.add(offset).toLong());
        }
    }

    private static net.minecraft.util.math.BlockPos rotatePosition(
            net.minecraft.util.math.BlockPos pos, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_90:
                return new net.minecraft.util.math.BlockPos(-pos.getZ(), pos.getY(), pos.getX());
            case CLOCKWISE_180:
                return new net.minecraft.util.math.BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case COUNTERCLOCKWISE_90:
                return new net.minecraft.util.math.BlockPos(pos.getZ(), pos.getY(), -pos.getX());
            default:
                return pos.toImmutable();
        }
    }

    public static final class AdapterEntry {
        private final ResourceLocation id;
        private final IMovableTileAdapter adapter;

        private AdapterEntry(ResourceLocation id, IMovableTileAdapter adapter) {
            this.id = id;
            this.adapter = adapter;
        }

        public ResourceLocation getId() {
            return id;
        }

        public IMovableTileAdapter getAdapter() {
            return adapter;
        }
    }
}
