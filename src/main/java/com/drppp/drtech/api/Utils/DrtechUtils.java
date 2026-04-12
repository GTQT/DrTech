package com.drppp.drtech.api.Utils;

import com.drppp.drtech.Network.SyncInit;
import com.drppp.drtech.Network.UpdateTileEntityPacket;
import com.drppp.drtech.Tags;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.drppp.drtech.loaders.recipes.DrtechReceipes.LOG_CREATE;
import static gregtech.api.GregTechAPI.materialManager;

public class DrtechUtils {
    public static Map<Item, IBlockState> ItemCrops = new HashMap();
    @Nonnull
    public static ResourceLocation getRL(@Nonnull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

    public static void initCropsList()
    {
        for (Block block : GameRegistry.findRegistry(Block.class).getValues()) {
            if (block instanceof BlockCrops) {
                BlockCrops crop = (BlockCrops)block;
                var seed = getSeedFromCropReflection(crop);
                if(seed != null);
                {
                    ItemCrops.put(seed,crop.withAge(crop.getMaxAge()));
                }
            }
        }
    }
    public static Item getSeedFromCropReflection(BlockCrops crop) {
        try {
            // 1. 获取 BlockCrops 的 Class 对象
            Class<?> cropClass = BlockCrops.class;

            // 2. 获取 getSeed() 方法（protected 方法，需要用 getDeclaredMethod）
            Method getSeedMethod = cropClass.getDeclaredMethod("getSeed");

            // 3. 设置可访问（绕过 protected 限制）
            getSeedMethod.setAccessible(true);

            // 4. 调用方法并返回种子
            return (Item) getSeedMethod.invoke(crop);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 反射失败
        }
    }
    public static void addAspectsToItemStack(ItemStack stack, String aspectKey, int amount) {
        if (stack.isEmpty()) return;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }

        // 获取或创建 Aspects 列表
        NBTTagList aspectsList;
        if (tag.hasKey("Aspects", 9)) { // 9 = NBTTagList 的 ID
            aspectsList = tag.getTagList("Aspects", 10); // 10 = NBTTagCompound 的 ID
        } else {
            aspectsList = new NBTTagList();
        }

        // 创建单个 Aspect 的 Compound
        NBTTagCompound aspectCompound = new NBTTagCompound();
        aspectCompound.setString("key", aspectKey);
        aspectCompound.setInteger("amount", amount);

        // 添加到列表
        aspectsList.appendTag(aspectCompound);

        // 写回根标签
        tag.setTag("Aspects", aspectsList);
        stack.setTagCompound(tag);
    }
    public static void addLogCreate(int EUt, int tick, int outNum, int meta)
    {
        LOG_CREATE.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.SAPLING,1,meta))
                .outputs(new ItemStack(Blocks.LOG,outNum,meta))
                .EUt(EUt)
                .duration(tick)
                .buildAndRegister();
    }
    public static void addLog2Create(int EUt, int tick, int outNum, int meta)
    {
        LOG_CREATE.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.SAPLING,1,meta+4))
                .outputs(new ItemStack(Blocks.LOG2,outNum,meta))
                .EUt(EUt)
                .duration(tick)
                .buildAndRegister();
    }
    public static BigInteger getBigIntegerMin(BigInteger a,BigInteger b)
    {
        int comparisonResult = a.compareTo(b);

        BigInteger minValue;
        if (comparisonResult < 0) {
            minValue = a;
        } else {
            minValue = b;
        }
        return  minValue;
    }

    public static void sendTileEntityUpdate(TileEntity tileEntity,NBTTagCompound nbt) {
        tileEntity.writeToNBT(nbt);
        UpdateTileEntityPacket packet = new UpdateTileEntityPacket(tileEntity.getPos(), nbt);
        SyncInit.NETWORK.sendToServer(packet);
    }
    public static void sendTileEntityClientUpdate(TileEntity tileEntity,NBTTagCompound nbt) {
        tileEntity.writeToNBT(nbt);
        UpdateTileEntityPacket packet = new UpdateTileEntityPacket(tileEntity.getPos(), nbt);
        SyncInit.NETWORK_CLIENT.sendToAll(packet);
    }
    public static int getPosDist(BlockPos a,BlockPos b)
    {
        int x = (int)Math.pow(a.getX()-b.getX(),2);
        int y = (int)Math.pow(a.getY()-b.getY(),2);
        int z = (int)Math.pow(a.getZ()-b.getZ(),2);

        return  (int)Math.sqrt(x+y+z);
    }

    public static EnumFacing getDirectionFromB1ToB2(BlockPos b1, BlockPos b2) {
        // 检查b1和b2是否为相同的位置
        if (b1.equals(b2)) {
            return EnumFacing.UP;
        }

        int diffX = b2.getX() - b1.getX();
        int diffY = b2.getY() - b1.getY();
        int diffZ = b2.getZ() - b1.getZ();

        // Minecraft中的方向是基于玩家视角的，南北东西对应负正的Z和X轴
        if (diffX > 0) {
            return EnumFacing.EAST;
        } else if (diffX < 0) {
            return EnumFacing.WEST;
        } else if (diffZ > 0) {
            return EnumFacing.SOUTH;
        } else if (diffZ < 0) {
            return EnumFacing.NORTH;
        } else if (diffY > 0) {
            return EnumFacing.UP;
        } else if (diffY < 0) {
            return EnumFacing.DOWN;
        } else {
            return EnumFacing.UP;
        }
    }

    public abstract class ItemIdManager {
        public  static ItemId create(NBTTagCompound tag) {
            return new ItemId(
                    Item.getItemById(tag.getShort("item")),
                    tag.getShort("meta"),
                    tag.hasKey("tag", Constants.NBT.TAG_COMPOUND) ? tag.getCompoundTag("tag") : null);
        }

        /**
         * This method copies NBT, as it is mutable.
         */
        public static ItemId create(ItemStack itemStack) {
            NBTTagCompound nbt = itemStack.getTagCompound();
            if (nbt != null) {
                nbt = (NBTTagCompound) nbt.copy();
            }

            return new ItemId(itemStack.getItem(), itemStack.getMetadata(), nbt);
        }

        /**
         * This method copies NBT, as it is mutable.
         */
        public static ItemId create(Item item, int metaData, @Nullable NBTTagCompound nbt) {
            if (nbt != null) {
                nbt = (NBTTagCompound) nbt.copy();
            }
            return new ItemId(item, metaData, nbt);
        }

        /**
         * This method stores NBT as null.
         */
        public static ItemId createWithoutNBT(ItemStack itemStack) {
            return new ItemId(itemStack.getItem(), itemStack.getMetadata(), null);
        }

        /**
         * This method does not copy NBT in order to save time. Make sure not to mutate it!
         */
        public static ItemId createNoCopy(ItemStack itemStack) {
            return new ItemId(
                    itemStack.getItem(),
                    itemStack.getMetadata(),
                    itemStack.getTagCompound());
        }

        /**
         * This method does not copy NBT in order to save time. Make sure not to mutate it!
         */
        public ItemId createNoCopy(Item item, int metaData, @Nullable NBTTagCompound nbt) {
            return new ItemId(item, metaData, nbt);
        }

        protected abstract Item item();

        protected abstract int metaData();

        @Nullable
        protected abstract NBTTagCompound nbt();

        public NBTTagCompound writeToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setShort("item", (short) Item.getIdFromItem(item()));
            tag.setShort("meta", (short) metaData());
            if (nbt() != null) tag.setTag("tag", nbt());
            return tag;
        }

        @Nonnull
        public ItemStack getItemStack() {
            ItemStack itemStack = new ItemStack(item(), 1, metaData());
            itemStack.setTagCompound(nbt());
            return itemStack;
        }
    }
}
