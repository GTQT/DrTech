package com.drppp.drtech.api.Utils;

import com.drppp.drtech.Network.SyncInit;
import com.drppp.drtech.Network.UpdateTileEntityPacket;
import com.drppp.drtech.Tags;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.drppp.drtech.loaders.recipes.DrtechReceipes.LOG_CREATE;
import static gregtech.api.GregTechAPI.materialManager;

public class DrtechUtils {
    public static  List<Material> listMater = new ArrayList<>();
    public static  Integer baseTime = 700;
    @Nonnull
    public static ResourceLocation getRL(@Nonnull String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

    public static void initList()
    {
        var ms = materialManager.getRegisteredMaterials();
        for (var s:ms)
        {
            if(s != Materials.UUMatter)
                listMater.add(s);
        }
    }
    public static String getName(MetaItem.MetaValueItem is) {
        return is.getStackForm().getDisplayName();
    }
    public static String getName(Material mater)
    {
        return mater.getLocalizedName();
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
    public static int getSolarTire(int[][] block)
    {
        int tire=0;
        String five="0,9),0,10),0,11),0,12),0,13),0,14),0,15),0,16),0,17),0,18),0,19),0,20),0,21),1,8),1,22),2,7),2,23),3,6),3,24),4,5),4,25),5,4),5,26),6,3),6,27),7,2),7,28),8,1),8,29),9,0),9,30),10,0),10,30),11,0),11,30),12,0),12,30),13,0),13,30),14,0),14,30),15,0),15,30),16,0),16,30),17,0),17,30),18,0),18,30)," +
                "19,0),19,30),20,0),20,30),21,0),21,30),22,1),22,29),23,2),23,28),24,3),24,27),25,4),25,26),26,5),26,25),27,6),27,24),28,7),28,23),29,8),29,22),30,9),30,10),30,11),30,12),30,13),30,14),30,15),30,16),30,17),30,18),30,19),30,20),30,21";
        String four="2,10),2,11),2,12),2,13),2,14),2,15),2,16),2,17),2,18),2,19),2,20),3,9),3,21),4,8),4,22),5,7),5,23),6,6),6,24),7,5),7,25)," +
                "8,4),8,26),9,3),9,27),10,2),10,28),11,2),11,28),12,2),12,28),13,2),13,28),14,2),14,28),15,2),15,28),16,2),16,28),17,2),17,28)," +
                "18,2),18,28),19,2),19,28),20,2),20,28),21,3),21,27),22,4),22,26),23,5),23,25),24,6),24,24),25,7),25,23)," +
                "26,8),26,22),27,9),27,21),28,10),28,11),28,12),28,13),28,14),28,15),28,16),28,17),28,18),28,19),28,20";
        String three="4,11),4,12),4,13),4,14),4,15),4,16),4,17),4,18),4,19),5,10),5,20),6,9),6,21),7,8),7,22),8,7),8,23),9,6),9,24),10,5),10,25),11,4),11,26),12,4),12,26)," +
                "13,4),13,26),14,4),14,26),15,4),15,26),16,4),16,26),17,4),17,26),18,4),18,26),19,4),19,26),20,5),20,25),21,6),21,24)," +
                "22,7),22,23),23,8),23,22),24,9),24,21),25,10),25,20),26,11),26,12),26,13),26,14),26,15),26,16),26,17),26,18),26,19";
        String two="6,12),6,13),6,14),6,15),6,16),6,17),6,18),7,11),7,19),8,10),8,20)," +
                "9,9),9,21),10,8),10,22),11,7),11,23),12,6),12,24),13,6),13,24),14,6),14,24)," +
                "15,6),15,24),16,6),16,24),17,6),17,24),18,6),18,24),19,7),19,23),20,8),20,22)," +
                "21,9),21,21),22,10),22,20),23,11),23,19),24,12),24,13),24,14),24,15),24,16),24,17),24,18";
        String one="8,13),8,14),8,15),8,16),8,17),9,12),9,18),10,11),10,19),11,10),11,20)," +
                "12,9),12,21),13,8),13,22),14,8),14,22),15,8),15,22),16,8),16,22),17,8),17,22)," +
                "18,9),18,21),19,10),19,20),20,11),20,19),21,12),21,18),22,13),22,14),22,15),22,16),22,17";
        String[] list = one.split("\\),");
        int flag=1;
        for (String s:list
             ) {
            String[] xz = s.split(",");
            if(block[Integer.parseInt(xz[0])][Integer.parseInt(xz[1])] == 0)
            {
                flag=0;
                break;
            }
        }
        if(flag==1)
            tire=1;
        else
            return tire;

        list = two.split("\\),");
        flag=1;
        for (String s:list
        ) {
            String[] xz = s.split(",");
            if(block[Integer.parseInt(xz[0])][Integer.parseInt(xz[1])] == 0)
            {
                flag=0;
                break;
            }
        }
        if(flag==1)
            tire=2;
        else
            return tire;

        list = three.split("\\),");
        flag=1;
        for (String s:list
        ) {
            String[] xz = s.split(",");
            if(block[Integer.parseInt(xz[0])][Integer.parseInt(xz[1])] == 0)
            {
                flag=0;
                break;
            }
        }
        if(flag==1)
            tire=3;
        else
            return tire;

        list = four.split("\\),");
        flag=1;
        for (String s:list
        ) {
            String[] xz = s.split(",");
            if(block[Integer.parseInt(xz[0])][Integer.parseInt(xz[1])] == 0)
            {
                flag=0;
                break;
            }
        }
        if(flag==1)
            tire=4;
        else
            return tire;

        list = five.split("\\),");
        flag=1;
        for (String s:list
        ) {
            String[] xz = s.split(",");
            if(block[Integer.parseInt(xz[0])][Integer.parseInt(xz[1])] == 0)
            {
                flag=0;
                break;
            }
        }
        if(flag==1)
            return 5;
        else
            return tire;
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
