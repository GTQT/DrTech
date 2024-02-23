package com.drppp.drtech.Tile;

import com.drppp.drtech.Items.MetaItems.MyMetaItems;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class TileEntityGoldenSea extends TileEntity implements ITickable {

    private int tick=0;
    @Override
    public void update() {
        // 每tick调用一次
        if (!this.world.isRemote && tick++>10) { // 确保在服务器端运行
            this.tick=0;
            // 检查方块上方
            BlockPos above = pos.up();
            List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(above));

            for (EntityItem entityItem : items) {
                ItemStack stack = entityItem.getItem();

                // 检查是否为金元宝
                if (stack.getItem()== MyMetaItems.GOLD_COIN.getMetaItem() && stack.getMetadata()==MyMetaItems.GOLD_COIN.getMetaValue()) {
                    // 复制金元宝并喷射出去
                    ItemStack doubleApple = stack.copy();
                    doubleApple.setCount(2); // 设置数量为2
                    EntityItem appleEntity = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, doubleApple);
                    appleEntity.setDefaultPickupDelay();
                    world.spawnEntity(appleEntity);
                    // 移除原来的掉落物
                    entityItem.setDead();
                }
            }
        }
    }

}
