package com.drppp.drtech.Farm;

import com.drppp.drtech.common.Blocks.Crops.CropsInit;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileFungus extends TileEntity implements ITickable {
    public FungusType type = FungusTypes.COMMON;
    public int growSpeed;
    public int tire;
    public int yield;
    public static final  Random  random= new Random();
    private int tick=0;
    @Override
    public void update() {
        if(!this.world.isRemote)
        {
            //自我蔓延
            if(++tick >= 80+20*this.type.Level)
            {
                tick=0;
                tire=type.Tire;
                isSurroundedByMycelium(this.world,this.pos);
            }
        }
    }
    //随机刻更新
    public void updateTick()
    {
        FungusGrow();
    }
    //检测菌丝
    public void isSurroundedByMycelium(World world, BlockPos pos) {
        // 检查北、南、东、西四个方向
        BlockPos[] directions = new BlockPos[]{
                pos.north(),
                pos.south(),
                pos.east(),
                pos.west()
        };

        for (BlockPos checkPos : directions) {
            IBlockState state = world.getBlockState(checkPos.offset(EnumFacing.DOWN));
            if (state.getBlock() == Blocks.MYCELIUM && random.nextInt(100)==1 && world.getBlockState(checkPos).getBlock()==Blocks.AIR) {
                world.setBlockState(checkPos, world.getBlockState(this.pos));
            }
        }
    }
    public void FungusGrow()
    {
        this.tire = Math.min(this.tire+1,this.type.Tire);
    }
    public boolean canHarvest()
    {
        return this.tire==this.type.Tire;
    }
    public  List<ItemStack> getDropList()
    {
        List<ItemStack> drop = new ArrayList<>();
       if(canHarvest())
       {
           for (var s : this.type.DropList)
           {
               if( random.nextInt(100)<=s.Prob)
               {
                   drop.add(s.DropItem);
               }
           }
           for (var s : this.type.SpecialDropList)
           {
               if( random.nextInt(100)<=s.Prob)
               {
                   drop.add(s.DropItem);
               }
           }
       }
        return drop;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }
}
