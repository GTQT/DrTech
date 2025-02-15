package com.drppp.drtech.common.Items;

import com.drppp.drtech.Client.render.Items.RenderItemAdvancedRocket;
import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.common.Entity.EntityAdvancedRocket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemAdvancedRocket extends Item{
    public ItemAdvancedRocket() {
        this.setTranslationKey("advanced_rocket");
        this.setRegistryName("advanced_rocket");
        this.setCreativeTab(DrTechMain.DrTechTab); // 设置创造模式标签
        this.setTileEntityItemStackRenderer(new RenderItemAdvancedRocket());
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote && isAreaAir(worldIn,pos) &&!hasEntityAdvancedRocket(worldIn,pos,7))
        {
            // 生成 Rocket 实体

            EntityAdvancedRocket rocket = new EntityAdvancedRocket(worldIn);
            rocket.setPosition(pos.getX(), pos.getY()+1, pos.getZ());
            if(player.getHeldItem(hand).hasTagCompound())
            {
                ItemStack is = player.getHeldItem(hand);
                if(is.getTagCompound().hasKey("Fuel"))
                {
                    rocket.setFuelAmount(is.getTagCompound().getInteger("Fuel"));
                }
                if(is.getTagCompound().hasKey("TargetDim"))
                {
                    rocket.setDimId(is.getTagCompound().getInteger("TargetDim"));
                }
            }
            worldIn.spawnEntity(rocket);
            player.getHeldItem(hand).shrink(1);
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }
    public boolean isAreaAir(World world, BlockPos pos) {
        int xRadius = 2; // X轴范围
        int yRadius = 5; // Y轴范围
        int zRadius = 2; // Z轴范围
        // 遍历区域
        for (int x = -xRadius; x <= xRadius; x++) {
            for (int y = 1; y <= yRadius; y++) {
                for (int z = -zRadius; z <= zRadius; z++) {
                    BlockPos currentPos = pos.add(x, y, z);
                    // 检查方块是否为空气
                    if (!world.isAirBlock(currentPos)) {
                        return false; // 如果有非空气方块，返回 false
                    }
                }
            }
        }
        return true; // 如果所有方块都是空气，返回 true
    }
    public boolean hasEntityAdvancedRocket(World world, BlockPos pos, double radius) {
        // 获取实体的AABB（Axis-Aligned Bounding Box）
        AxisAlignedBB aabb = new AxisAlignedBB(
                pos.getX() - radius, pos.getY() - radius-3, pos.getZ() - radius,
                pos.getX() + radius, pos.getY() + radius+3, pos.getZ() + radius
        );
        // 获取区域内的所有实体
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, aabb);
        // 遍历实体，检查是否为EntityAdvancedRocket
        for (Entity entity : entities) {
            if (entity instanceof EntityAdvancedRocket) {
                return true; // 如果找到EntityAdvancedRocket，返回true
            }
        }
        return false; // 如果没有找到，返回false
    }

}