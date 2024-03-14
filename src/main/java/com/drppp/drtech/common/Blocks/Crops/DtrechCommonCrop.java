package com.drppp.drtech.common.Blocks.Crops;


import com.drppp.drtech.common.Items.ItemsInit;
import com.drppp.drtech.Tags;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;


public class DtrechCommonCrop extends BlockCrops {
    private Item ItemSeed;
    private Item ItemCrop;
    private static final AxisAlignedBB[] COMMON_AABB = new AxisAlignedBB[] {new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.4375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5625D, 1.0D)};

    public DtrechCommonCrop(String Name,Item itemSeed, Item itemCrop) {
        super();
        this.setRegistryName(Tags.MODID,"drtech_crop_"+Name);
        this.setTranslationKey("drtech_crop_"+Name);
        ItemSeed = itemSeed;
        ItemCrop = itemCrop;
    }

    protected Item getSeed()
    {
        return this.ItemSeed;
    }

    protected Item getCrop()
    {
        return this.ItemCrop;
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return COMMON_AABB[((Integer)state.getValue(this.getAgeProperty())).intValue()];
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if(this.getSeed() == ItemsInit.ITEM_LAPIS_SEED)
        {
            int age = this.getAge(state);
            Random rand = world instanceof World ? ((World) world).rand : new Random();
            ItemStack seed= new ItemStack(this.ItemSeed);
            if (age >= this.getMaxAge()) {
                if (!seed.isEmpty()) {
                    drops.add(seed.copy());
                    if (rand.nextInt(9) == 0) {
                        drops.add(seed.copy());
                    }
                }
                for (int i = 0; i < 4; i++) {
                    int meta  =rand.nextInt(15);
                    drops.add(new ItemStack(Items.DYE,1,meta==3?1:meta));
                }
            }
        }
        else
            super.getDrops(drops, world, pos, state, fortune);
    }
}
