package com.drppp.drtech.common.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityTimeTable extends TileEntity implements ITickable {

    public ItemStackHandler inventory = new ItemStackHandler(1);
    private int tick = 0;

    // 最大存储时间，与 TimeBottleBehavior 保持一致（84 小时）
    private static final int MAX_TIME = 6048000;

    public TileEntityTimeTable() {
    }

    @Override
    public void update() {
        if (!getWorld().isRemote && ++tick >= 20) {
            tick = 0;

            ItemStack stack = inventory.getStackInSlot(0);
            if (stack.isEmpty()) {
                return;
            }

            String unlocalizedName = stack.getItem().getUnlocalizedNameInefficiently(stack);
            if (!unlocalizedName.equals("tool.time_bottle") && !unlocalizedName.endsWith(".tool.time_bottle")) {
                return;
            }

            NBTTagCompound compound = stack.getTagCompound();
            if (compound == null) {
                compound = new NBTTagCompound();
                stack.setTagCompound(compound);
            }

            int time = compound.getInteger("storedTime");
            if (time < MAX_TIME) {
                time += 20; // 每秒增加 20 tick
                if (time > MAX_TIME) {
                    time = MAX_TIME;
                }
                compound.setInteger("storedTime", time);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        // 修正原拼写错误 "TimeInveantiry" -> "TimeInventory"
        if (compound.hasKey("TimeInventory")) {
            this.inventory.deserializeNBT(compound.getCompoundTag("TimeInventory"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("TimeInventory", inventory.serializeNBT());
        return compound;
    }
}