package com.drppp.drtech.common.CustomCrops;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class DrtCropTileBase extends TileEntity implements ITickable {
    public int tire;//作物等级
    public int AGE=0;//作物阶段
    public int MAX_AGE=4;//最大生长阶段
    public int UPDATE_TICKS = 256;//检测时间
    public int GrowSpeed=100;//生长速度
    public int GainRate=1;//收获倍率
    public int Resistance=1;//抗性
    public int GrowingPoint = 0;//生长点
    public int[] AGE_MAX_POINT={200,200,120,230};//当前阶段最大生长点
    private int tick=0;
    public boolean sacnflag = false;
    public ItemStack seed = new ItemStack(Items.WHEAT_SEEDS);
    public DrtCropTileBase(){
    }
    public DrtCropTileBase(int growSpeed,int gainRate,int resistance,int age,int maxAge){
        this.GrowSpeed = growSpeed;
        this.GainRate = gainRate;
        this.Resistance = resistance;
        this.AGE = age;
        this.MAX_AGE = maxAge;
    }
    @Override
    public void update() {
        if(++tick>=20 && !getWorld().isRemote)
        {
            tick=0;
            int count = getWorld().rand.nextInt(3)+1 + this.GrowSpeed;
            GrowingPoint += count;
            if(CanGrow() && GrowingPoint>=AGE_MAX_POINT[AGE])
            {
                Grow();
            }
        }
    }
    public void onRightClick()
    {
        System.out.println("========================");
    }
    public void Grow() {
        if (CanGrow()) {
            this.AGE += 1;
            CropGanZhe self_block = (CropGanZhe) this.getBlockType();
            // 更新方块状态
            self_block.grow(getWorld(), getPos(), self_block.getStateFromMeta(AGE));
            // 重置生长点
            GrowingPoint = 0;
        }
    }
    public boolean CanGrow()
    {
        return this.AGE<MAX_AGE;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.AGE = compound.getInteger("Age");
        this.GrowingPoint = compound.getInteger("GrowingPoint");
        this.GrowSpeed = compound.getInteger("GrowSpeed");
        // 读取其他字段
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Age", this.AGE);
        compound.setInteger("GrowingPoint", this.GrowingPoint);
        compound.setInteger("GrowSpeed", this.GrowSpeed);
        // 保存其他字段
        return compound;
    }

    public ItemStack getSeed()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Gs",GrowSpeed);
        tag.setInteger("Gr",GainRate);
        tag.setInteger("Re",Resistance);
        var copy = seed.copy();
        copy.setTagInfo("Property",tag);
        return copy;
    }

}
