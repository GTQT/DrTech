package com.drppp.drtech.Tile;

import keqing.gtqtcore.common.items.GTQTMetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityTimeTable extends TileEntity implements ITickable {

    public  ItemStackHandler inventory = new ItemStackHandler(1);
    int tick=0;
    public TileEntityTimeTable(){

    }
    @Override
    public void update() {
        if(!getWorld().isRemote && ++tick>20)
        {
            tick=0;
            ItemStack is =inventory.getStackInSlot(0);
            if(is.getItem()== GTQTMetaItems.TIME_BOTTLE.getMetaItem() && is.getMetadata()==GTQTMetaItems.TIME_BOTTLE.getMetaValue())
            {
                int time=0;
                if (is.hasTagCompound()) {
                    NBTTagCompound compound = is.getTagCompound();
                    time = compound.getInteger("storedTime");
                    time +=20;
                    if(time <=6048000)
                        compound.setInteger("storedTime", time);
                } else {
                    NBTTagCompound compound = new NBTTagCompound();
                    compound.setInteger("storedTime", time);
                    is.setTagCompound(compound);
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("TimeInveantiry"))
            this.inventory.deserializeNBT(compound.getCompoundTag("TimeInveantiry"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagCompound tag = new NBTTagCompound();
        tag = inventory.serializeNBT();
        compound.setTag("TimeInveantiry",tag);
        return compound;
    }


}
